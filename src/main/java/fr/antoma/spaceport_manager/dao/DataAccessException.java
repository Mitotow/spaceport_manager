package fr.antoma.spaceport_manager.dao;

import java.sql.SQLException;

/**
 * Erreur d'accès aux données, avec un message destiné à l'utilisateur.
 */
public class DataAccessException extends RuntimeException {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DataAccessException of(String message, SQLException e) {
        return new DataAccessException(message + "\n\n" + e.getMessage(), e);
    }

    /**
     * Traduit une erreur d'enregistrement, en remplaçant la violation d'unicité par un message explicite.
     */
    public static DataAccessException onSave(String message, String duplicateMessage, SQLException e) {
        if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
            return new DataAccessException(duplicateMessage, e);
        }
        return of(message, e);
    }
}
