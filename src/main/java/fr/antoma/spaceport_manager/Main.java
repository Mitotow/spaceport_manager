package fr.antoma.spaceport_manager;

import com.formdev.flatlaf.FlatIntelliJLaf;
import fr.antoma.spaceport_manager.dao.DataAccessException;
import fr.antoma.spaceport_manager.dao.UserDao;
import fr.antoma.spaceport_manager.db.Database;
import fr.antoma.spaceport_manager.db.DatabaseConfig;
import fr.antoma.spaceport_manager.model.User;
import fr.antoma.spaceport_manager.ui.ConnectionDialog;
import fr.antoma.spaceport_manager.ui.LoginDialog;
import fr.antoma.spaceport_manager.ui.MainFrame;
import fr.antoma.spaceport_manager.ui.common.Dialogs;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;

public class Main {

  public static void main(String[] args) {
    FlatIntelliJLaf.setup();

    // Toute l'interface Swing est créée dans le thread graphique (EDT)
    SwingUtilities.invokeLater(() -> {
      Database database = connect();
      if (database == null) {
        System.exit(0);
      }

      UserDao userDao = new UserDao(database);
      try {
        userDao.createDefaultUserIfNone();
      } catch (DataAccessException e) {
        Dialogs.error(null, e.getMessage());
        System.exit(1);
      }

      User user = new LoginDialog(userDao).showDialog();
      if (user == null) {
        System.exit(0);
      }

      new MainFrame(database, user).setVisible(true);
    });
  }

  /**
   * Redemande les paramètres tant que la connexion échoue. Sans db.properties, le formulaire
   * s'affiche directement.
   */
  private static Database connect() {
    DatabaseConfig config = DatabaseConfig.load();
    boolean askUser = !DatabaseConfig.exists();
    String error = null;

    while (true) {
      if (askUser) {
        config = ConnectionDialog.show(null, config, error);
        if (config == null) {
          return null;
        }
      }
      try {
        Database database = new Database(config);
        database.initialize();
        saveConfig(config);
        return database;
      } catch (SQLException e) {
        error = e.getMessage();
        askUser = true;
      }
    }
  }

  private static void saveConfig(DatabaseConfig config) {
    try {
      config.save();
    } catch (IOException e) {
      System.err.println("Impossible d'enregistrer " + DatabaseConfig.FILE + " : " + e.getMessage());
    }
  }
}
