package fr.antoma.spaceport_manager.dao;

import fr.antoma.spaceport_manager.db.Database;
import fr.antoma.spaceport_manager.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDao {

  /**
   * Si la table est vide, il y aura création d'un compte {@code admin} avec un
   * mot de passe {@code admin}
   */
  public static final String DEFAULT_LOGIN = "admin";
  public static final String DEFAULT_PASSWORD = "admin";

  private static final String SELECT_BY_LOGIN = "SELECT id, login, password_salt, password_hash FROM app_user WHERE login = ?";

  private static final String INSERT = "INSERT INTO app_user (login, password_salt, password_hash) VALUES (?, ?, ?)";

  private final Database database;

  public UserDao(Database database) {
    this.database = database;
  }

  /**
   * Vérifie le login et le mot de passe.
   *
   * @return l'utilisateur si les identifiants sont corrects, sinon un
   *         {@code Optional} vide
   */
  public Optional<User> authenticate(String login, char[] password) {
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_BY_LOGIN)) {
      statement.setString(1, login);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        String hash = PasswordHasher.hash(rs.getString("password_salt"), password);
        if (!PasswordHasher.matches(rs.getString("password_hash"), hash)) {
          return Optional.empty();
        }
        return Optional.of(new User(rs.getInt("id"), rs.getString("login")));
      }
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de vérifier les identifiants.", e);
    }
  }

  public void create(String login, char[] password) {
    String salt = PasswordHasher.newSalt();
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setString(1, login);
      statement.setString(2, salt);
      statement.setString(3, PasswordHasher.hash(salt, password));
      statement.executeUpdate();
    } catch (SQLException e) {
      throw DataAccessException.onSave("Impossible de créer l'utilisateur.",
          "L'utilisateur '" + login + "' existe déjà.", e);
    }
  }

  public void createDefaultUserIfNone() {
    try (Connection connection = database.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM app_user")) {
      if (rs.next() && rs.getInt(1) > 0) {
        return;
      }
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de lire les utilisateurs.", e);
    }
    create(DEFAULT_LOGIN, DEFAULT_PASSWORD.toCharArray());
  }
}
