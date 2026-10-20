package fr.antoma.spaceport_manager.db;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Paramètres de connexion à MySQL, lus depuis et enregistrés dans le fichier {@code db.properties}
 * du répertoire de lancement.
 */
public record DatabaseConfig(String url, String user, String password) {

    public static final Path FILE = Path.of("db.properties");

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/spaceport_manager?createDatabaseIfNotExist=true&connectTimeout=5000";

    public static boolean exists() {
        return Files.exists(FILE);
    }

    public static DatabaseConfig load() {
        Properties properties = new Properties();
        if (exists()) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Lecture de " + FILE + " impossible : " + e.getMessage());
            }
        }
        return new DatabaseConfig(
                properties.getProperty("db.url", DEFAULT_URL),
                properties.getProperty("db.user", "root"),
                properties.getProperty("db.password", ""));
    }

    public void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("db.url", url);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", password);
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            properties.store(writer, "Connexion MySQL de Spaceport Manager");
        }
    }
}
