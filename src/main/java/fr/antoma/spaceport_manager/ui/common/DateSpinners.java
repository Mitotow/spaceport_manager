package fr.antoma.spaceport_manager.ui.common;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Champs de saisie de date basés sur {@link JSpinner}, convertis depuis et vers
 * {@code java.time}.
 */
public final class DateSpinners {

  private DateSpinners() {
  }

  public static JSpinner dateTime(LocalDateTime initial) {
    Date date = Date.from(initial.atZone(ZoneId.systemDefault()).toInstant());
    return create(date, Calendar.MINUTE, Formats.DATE_TIME_PATTERN);
  }

  public static JSpinner date(LocalDate initial) {
    Date date = Date.from(initial.atStartOfDay(ZoneId.systemDefault()).toInstant());
    return create(date, Calendar.DAY_OF_MONTH, Formats.DATE_PATTERN);
  }

  public static LocalDateTime getDateTime(JSpinner spinner) {
    return toLocalDateTime(spinner).withSecond(0).withNano(0);
  }

  public static LocalDate getDate(JSpinner spinner) {
    return toLocalDateTime(spinner).toLocalDate();
  }

  private static JSpinner create(Date initial, int calendarField, String pattern) {
    JSpinner spinner = new JSpinner(new SpinnerDateModel(initial, null, null, calendarField));
    spinner.setEditor(new JSpinner.DateEditor(spinner, pattern));
    return spinner;
  }

  private static LocalDateTime toLocalDateTime(JSpinner spinner) {
    Date date = (Date) spinner.getValue();
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
