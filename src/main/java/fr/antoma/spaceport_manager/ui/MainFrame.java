package fr.antoma.spaceport_manager.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import fr.antoma.spaceport_manager.dao.BaggageDao;
import fr.antoma.spaceport_manager.dao.DataAccessException;
import fr.antoma.spaceport_manager.dao.FlightDao;
import fr.antoma.spaceport_manager.dao.PassengerDao;
import fr.antoma.spaceport_manager.dao.UserDao;
import fr.antoma.spaceport_manager.db.Database;
import fr.antoma.spaceport_manager.db.DatabaseConfig;
import fr.antoma.spaceport_manager.model.User;
import fr.antoma.spaceport_manager.ui.baggage.BaggagePanel;
import fr.antoma.spaceport_manager.ui.common.Dialogs;
import fr.antoma.spaceport_manager.ui.common.EntityPanel;
import fr.antoma.spaceport_manager.ui.common.Images;
import fr.antoma.spaceport_manager.ui.flight.FlightPanel;
import fr.antoma.spaceport_manager.ui.passenger.PassengerPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class MainFrame extends JFrame {

  private final Database database;
  private final User user;
  private final UserDao userDao;
  private final JTabbedPane tabs = new JTabbedPane();
  private final JLabel statusLabel = new JLabel();

  private boolean darkTheme;
  private Color accentColor = new Color(0x2675BF);

  public MainFrame(Database database, User user) {
    super("Spaceport Manager");
    this.database = database;
    this.user = user;
    this.userDao = new UserDao(database);
    setIconImage(Images.windowIcon());

    FlightDao flightDao = new FlightDao(database);
    PassengerDao passengerDao = new PassengerDao(database);
    BaggageDao baggageDao = new BaggageDao(database);

    tabs.addTab("Vols", new FlightPanel(flightDao, passengerDao));
    tabs.addTab("Passagers", new PassengerPanel(passengerDao, flightDao));
    tabs.addTab("Bagages", new BaggagePanel(baggageDao, passengerDao));
    // Les onglets dépendent les uns des autres (compteurs, noms) : on recharge à
    // chaque changement
    tabs.addChangeListener(e -> refreshCurrentTab());

    statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    updateStatus();

    setJMenuBar(createMenuBar());
    add(tabs, BorderLayout.CENTER);
    add(statusLabel, BorderLayout.SOUTH);

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(1200, 720);
    setMinimumSize(new Dimension(800, 500));
    setLocationRelativeTo(null);

    refreshCurrentTab();
  }

  private EntityPanel<?> currentPanel() {
    return (EntityPanel<?>) tabs.getSelectedComponent();
  }

  private void refreshCurrentTab() {
    currentPanel().refresh();
  }

  private void updateStatus() {
    DatabaseConfig config = database.getConfig();
    statusLabel.setText("Utilisateur : " + user.login() + "   |   Base : " + config.user() + " @ " + config.url());
  }

  private JMenuBar createMenuBar() {
    JMenu fileMenu = new JMenu("Fichier");
    JMenuItem exportItem = new JMenuItem("Exporter l'onglet en CSV...");
    exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcut()));
    exportItem.addActionListener(e -> exportCurrentTab());
    JMenuItem newUserItem = new JMenuItem("Nouvel utilisateur...");
    newUserItem.addActionListener(e -> createUser());
    JMenuItem connectionItem = new JMenuItem("Connexion à la base...");
    connectionItem.addActionListener(e -> changeConnection());
    JMenuItem quitItem = new JMenuItem("Quitter");
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, menuShortcut()));
    quitItem.addActionListener(e -> dispose());
    fileMenu.add(exportItem);
    fileMenu.addSeparator();
    fileMenu.add(newUserItem);
    fileMenu.add(connectionItem);
    fileMenu.addSeparator();
    fileMenu.add(quitItem);

    JMenu viewMenu = new JMenu("Affichage");
    JRadioButtonMenuItem lightItem = new JRadioButtonMenuItem("Thème clair", true);
    JRadioButtonMenuItem darkItem = new JRadioButtonMenuItem("Thème sombre");
    lightItem.addActionListener(e -> setDarkTheme(false));
    darkItem.addActionListener(e -> setDarkTheme(true));
    ButtonGroup themeGroup = new ButtonGroup();
    themeGroup.add(lightItem);
    themeGroup.add(darkItem);
    JMenuItem accentItem = new JMenuItem("Couleur d'accent...");
    accentItem.addActionListener(e -> chooseAccentColor());
    JCheckBoxMenuItem statusBarItem = new JCheckBoxMenuItem("Barre d'état", true);
    statusBarItem.addActionListener(e -> statusLabel.setVisible(statusBarItem.isSelected()));
    viewMenu.add(lightItem);
    viewMenu.add(darkItem);
    viewMenu.addSeparator();
    viewMenu.add(accentItem);
    viewMenu.add(statusBarItem);

    JMenu helpMenu = new JMenu("Aide");
    JMenuItem aboutItem = new JMenuItem("À propos");
    aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
        "Spaceport Manager\nGestion des vols, passagers et bagages d'un spatioport.",
        "À propos", JOptionPane.INFORMATION_MESSAGE, Images.logo(64)));
    helpMenu.add(aboutItem);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(helpMenu);
    return menuBar;
  }

  /** Touche de raccourci des menus (Ctrl, ou Cmd sur macOS). */
  private static int menuShortcut() {
    return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
  }

  private void exportCurrentTab() {
    String tabName = tabs.getTitleAt(tabs.getSelectedIndex());
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Exporter '" + tabName + "' en CSV");
    chooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
    chooser.setSelectedFile(new File(tabName.toLowerCase() + ".csv"));
    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File file = chooser.getSelectedFile();
    if (!file.getName().toLowerCase().endsWith(".csv")) {
      file = new File(file.getPath() + ".csv");
    }
    if (file.exists() && !Dialogs.confirm(this, "Le fichier " + file.getName() + " existe déjà. Le remplacer ?")) {
      return;
    }
    try {
      currentPanel().exportCsv(file.toPath());
      JOptionPane.showMessageDialog(this, "Export terminé :\n" + file.getAbsolutePath(),
          "Export CSV", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
      Dialogs.error(this, "Impossible d'écrire le fichier :\n" + e.getMessage());
    }
  }

  private void createUser() {
    String login = JOptionPane.showInputDialog(this, "Identifiant du nouvel utilisateur :",
        "Nouvel utilisateur", JOptionPane.QUESTION_MESSAGE);
    if (login == null || login.isBlank()) {
      return;
    }
    login = login.strip();

    JPasswordField passwordField = new JPasswordField(18);
    int choice = JOptionPane.showConfirmDialog(this,
        new Object[] { "Mot de passe de '" + login + "' :", passwordField },
        "Nouvel utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    char[] password = passwordField.getPassword();
    try {
      if (choice != JOptionPane.OK_OPTION) {
        return;
      }
      if (password.length == 0) {
        Dialogs.warning(this, "Le mot de passe ne peut pas être vide.");
        return;
      }
      if (!Dialogs.confirm(this, "Créer l'utilisateur '" + login + "' ?")) {
        return;
      }
      userDao.create(login, password);
      JOptionPane.showMessageDialog(this, "L'utilisateur '" + login + "' a été créé.",
          "Nouvel utilisateur", JOptionPane.INFORMATION_MESSAGE);
    } catch (DataAccessException e) {
      Dialogs.error(this, e.getMessage());
    } finally {
      Arrays.fill(password, '\0');
    }
  }

  private void setDarkTheme(boolean dark) {
    darkTheme = dark;
    applyTheme();
  }

  private void chooseAccentColor() {
    Color color = JColorChooser.showDialog(this, "Couleur d'accent", accentColor);
    if (color == null) {
      return;
    }
    accentColor = color;
    applyTheme();
  }

  private void applyTheme() {
    FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor",
        String.format("#%06x", accentColor.getRGB() & 0xFFFFFF)));
    if (darkTheme) {
      FlatDarculaLaf.setup();
    } else {
      FlatIntelliJLaf.setup();
    }
    FlatLaf.updateUI();
  }

  private void changeConnection() {
    DatabaseConfig config = database.getConfig();
    String error = null;
    while (true) {
      config = ConnectionDialog.show(this, config, error);
      if (config == null) {
        return;
      }
      try {
        database.reconfigure(config);
        break;
      } catch (SQLException e) {
        error = e.getMessage();
      }
    }
    try {
      config.save();
    } catch (IOException e) {
      Dialogs.warning(this, "Connexion établie, mais impossible d'enregistrer " + DatabaseConfig.FILE
          + " :\n" + e.getMessage());
    }
    updateStatus();
    refreshCurrentTab();
  }
}
