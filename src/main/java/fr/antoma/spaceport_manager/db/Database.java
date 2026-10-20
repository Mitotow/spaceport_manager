package fr.antoma.spaceport_manager.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class Database {

  // volatile : la configuration peut être remplacée depuis l'IHM
  private volatile DatabaseConfig config;

  public Database(DatabaseConfig config) {
    this.config = config;
  }

  public DatabaseConfig getConfig() {
    return config;
  }

  /** Ouvre une nouvelle connexion JDBC, à fermer par l'appelant. */
  public Connection getConnection() throws SQLException {
    DatabaseConfig current = config;
    return DriverManager.getConnection(current.url(), current.user(), current.password());
  }

  public void initialize() throws SQLException {
    try (Connection connection = getConnection()) {
      runScript(connection, "/sql/schema.sql");
      // Les bases créées avant l'ajout de la colonne "fragile" sont mises à jour.
      // Il s'agit ici d'un système de migration très basique.
      addColumnIfMissing(connection, "baggage", "fragile", "BOOLEAN NOT NULL DEFAULT FALSE");
      if (isEmpty(connection)) {
        // On seed la base de données pour avoir des données
        runScript(connection, "/sql/seed.sql");
      }
    }
  }

  /**
   * Bascule sur une nouvelle configuration, uniquement si la connexion avec
   * celle-ci réussit.
   */
  public void reconfigure(DatabaseConfig newConfig) throws SQLException {
    new Database(newConfig).initialize();
    this.config = newConfig;
  }

  private static void addColumnIfMissing(Connection connection, String table, String column, String definition)
      throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, table, column)) {
      if (rs.next()) {
        return;
      }
    }
    try (Statement statement = connection.createStatement()) {
      statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }
  }

  private static boolean isEmpty(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM flight")) {
      return rs.next() && rs.getInt(1) == 0;
    }
  }

  private static void runScript(Connection connection, String resource) throws SQLException {
    String script;
    try (InputStream in = Database.class.getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("Script SQL introuvable : " + resource);
      }
      script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    try (Statement statement = connection.createStatement()) {
      for (String sql : script.split(";")) {
        String cleaned = sql.lines()
            .filter(line -> !line.strip().startsWith("--"))
            .collect(Collectors.joining("\n"))
            .strip();
        if (!cleaned.isEmpty()) {
          statement.execute(cleaned);
        }
      }
    }
  }
}
