package fr.antoma.spaceport_manager.ui.common;

import java.time.format.DateTimeFormatter;

public final class Formats {

  public static final String DATE_PATTERN = "dd/MM/yyyy";
  public static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";

  public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern(DATE_PATTERN);
  public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

  private Formats() {
  }
}
