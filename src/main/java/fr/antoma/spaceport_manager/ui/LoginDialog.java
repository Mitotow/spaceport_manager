package fr.antoma.spaceport_manager.ui;

import fr.antoma.spaceport_manager.dao.DataAccessException;
import fr.antoma.spaceport_manager.dao.UserDao;
import fr.antoma.spaceport_manager.model.User;
import fr.antoma.spaceport_manager.ui.common.Dialogs;
import fr.antoma.spaceport_manager.ui.common.Images;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * Fenêtre de connexion : vérifie le login et le mot de passe dans la table
 * {@code app_user}.
 * L'application se ferme après trois tentatives échouées.
 */
public class LoginDialog extends JDialog {

  private static final int MAX_ATTEMPTS = 3;

  private final UserDao userDao;
  private final JTextField loginField = new JTextField(18);
  private final JPasswordField passwordField = new JPasswordField(18);
  private final JCheckBox showPasswordBox = new JCheckBox("Afficher le mot de passe");
  private final char defaultEchoChar;

  private int failedAttempts;
  private User user;

  public LoginDialog(UserDao userDao) {
    super((Frame) null, "Spaceport Manager - Connexion", true);
    this.userDao = userDao;
    this.defaultEchoChar = passwordField.getEchoChar();

    setIconImage(Images.windowIcon());
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    JLabel logoLabel = new JLabel(Images.logo(96));
    JLabel titleLabel = new JLabel("Spaceport Manager");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
    JPanel header = new JPanel(new BorderLayout(0, 8));
    header.add(logoLabel, BorderLayout.CENTER);
    header.add(titleLabel, BorderLayout.SOUTH);
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4);
    c.anchor = GridBagConstraints.LINE_END;
    c.gridx = 0;
    c.gridy = 0;
    form.add(new JLabel("Identifiant :"), c);
    c.gridy = 1;
    form.add(new JLabel("Mot de passe :"), c);
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    form.add(loginField, c);
    c.gridy = 1;
    form.add(passwordField, c);
    c.gridy = 2;
    form.add(showPasswordBox, c);

    // Echo char 0 : les caractères sont affichés en clair
    showPasswordBox
        .addActionListener(e -> passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : defaultEchoChar));

    JButton loginButton = new JButton("Se connecter");
    JButton quitButton = new JButton("Quitter");
    loginButton.addActionListener(e -> onLogin());
    quitButton.addActionListener(e -> dispose());
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    buttons.add(loginButton);
    buttons.add(quitButton);

    JPanel content = new JPanel(new BorderLayout(0, 12));
    content.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
    content.add(header, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);
    content.add(buttons, BorderLayout.SOUTH);
    setContentPane(content);
    getRootPane().setDefaultButton(loginButton);

    pack();
    setResizable(false);
    setLocationRelativeTo(null);
  }

  /**
   * Affiche la fenêtre (bloquante).
   *
   * @return l'utilisateur connecté, ou {@code null} si la connexion a été
   *         abandonnée ou refusée
   */
  public User showDialog() {
    setVisible(true);
    return user;
  }

  private void onLogin() {
    String login = loginField.getText().strip();
    char[] password = passwordField.getPassword();
    if (login.isEmpty() || password.length == 0) {
      Dialogs.warning(this, "Veuillez saisir votre identifiant et votre mot de passe.");
      return;
    }

    Optional<User> result;
    try {
      result = userDao.authenticate(login, password);
    } catch (DataAccessException e) {
      Dialogs.error(this, e.getMessage());
      return;
    } finally {
      // On efface le mot de passe de la mémoire dès qu'il n'est plus utile
      Arrays.fill(password, '\0');
    }

    if (result.isPresent()) {
      user = result.get();
      dispose();
      return;
    }

    failedAttempts++;
    if (failedAttempts >= MAX_ATTEMPTS) {
      Dialogs.error(this, "Trop de tentatives échouées. L'application va se fermer.");
      dispose();
      return;
    }
    Dialogs.warning(this, "Identifiant ou mot de passe incorrect.\nTentatives restantes : "
        + (MAX_ATTEMPTS - failedAttempts));
    passwordField.setText("");
    passwordField.requestFocusInWindow();
  }
}
