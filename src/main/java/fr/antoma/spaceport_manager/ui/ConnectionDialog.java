package fr.antoma.spaceport_manager.ui;

import fr.antoma.spaceport_manager.db.DatabaseConfig;

import javax.swing.*;
import java.awt.*;

public final class ConnectionDialog {

  private ConnectionDialog() {
  }

  /**
   * Affiche le formulaire pré-rempli avec {@code current}.
   *
   * @param error message de la dernière erreur de connexion, ou {@code null}
   * @return la configuration saisie, ou {@code null} si l'utilisateur annule
   */
  public static DatabaseConfig show(Component parent, DatabaseConfig current, String error) {
    JTextField urlField = new JTextField(current.url(), 40);
    JTextField userField = new JTextField(current.user(), 40);
    JPasswordField passwordField = new JPasswordField(current.password(), 40);

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4);
    c.fill = GridBagConstraints.HORIZONTAL;
    int row = 0;

    if (error != null) {
      JLabel errorLabel = new JLabel("<html><body style='width: 380px'>Connexion impossible : "
          + escapeHtml(error) + "</body></html>");
      errorLabel.setForeground(new Color(0xE53935));
      c.gridx = 0;
      c.gridy = row++;
      c.gridwidth = 2;
      panel.add(errorLabel, c);
      c.gridwidth = 1;
    }
    row = addRow(panel, c, row, "URL JDBC :", urlField);
    row = addRow(panel, c, row, "Utilisateur :", userField);
    addRow(panel, c, row, "Mot de passe :", passwordField);

    int choice = JOptionPane.showConfirmDialog(parent, panel, "Connexion à MySQL",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) {
      return null;
    }
    return new DatabaseConfig(urlField.getText().strip(), userField.getText().strip(),
        new String(passwordField.getPassword()));
  }

  private static int addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
    c.gridy = row;
    c.gridx = 0;
    c.weightx = 0;
    panel.add(new JLabel(label), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(field, c);
    return row + 1;
  }

  private static String escapeHtml(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
