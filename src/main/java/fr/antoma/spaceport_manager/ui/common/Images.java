package fr.antoma.spaceport_manager.ui.common;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public final class Images {

  private static final URL LOGO = Objects.requireNonNull(Images.class.getResource("/images/logo.png"),
      "Image introuvable : /images/logo.png");

  private Images() {
  }

  /** Logo de l'application redimensionné en carré de {@code size} pixels. */
  public static ImageIcon logo(int size) {
    Image image = new ImageIcon(LOGO).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
    return new ImageIcon(image);
  }

  public static Image windowIcon() {
    return new ImageIcon(LOGO).getImage();
  }
}
