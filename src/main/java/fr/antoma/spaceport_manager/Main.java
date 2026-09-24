package fr.antoma.spaceport_manager;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    FlatIntelliJLaf.setup();

    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Spaceport Manager");
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setSize(1024, 768);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
