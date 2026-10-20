package fr.antoma.spaceport_manager.ui.common;

import javax.swing.*;
import java.awt.*;

public final class Dialogs {

  private Dialogs() {
  }

  public static void error(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
  }

  public static void warning(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Attention", JOptionPane.WARNING_MESSAGE);
  }

  public static boolean confirm(Component parent, String message) {
    return JOptionPane.showConfirmDialog(parent, message, "Confirmation",
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
  }
}
