package fr.antoma.spaceport_manager.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.function.Function;

/**
 * Rendus personnalisés pour les tables et les listes déroulantes.
 */
public final class Renderers {

  private Renderers() {
  }

  public static <V> DefaultTableCellRenderer table(Class<V> type, Function<V, String> formatter) {
    return new DefaultTableCellRenderer() {
      @Override
      protected void setValue(Object value) {
        setText(value == null ? "" : formatter.apply(type.cast(value)));
      }
    };
  }

  public static <V> ListCellRenderer<Object> list(Class<V> type, Function<V, String> formatter, String nullText) {
    return new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        String text = value == null ? nullText : formatter.apply(type.cast(value));
        return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
      }
    };
  }
}
