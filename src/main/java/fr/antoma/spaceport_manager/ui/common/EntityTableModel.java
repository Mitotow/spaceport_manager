package fr.antoma.spaceport_manager.ui.common;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Modèle de table en lecture seule dont les colonnes sont décrites par des
 * fonctions d'accès.
 */
public class EntityTableModel<T> extends AbstractTableModel {

  /**
   * Colonne : titre, type des valeurs (pour le tri et le rendu) et fonction de
   * lecture de la valeur.
   */
  public record Column<T>(String name, Class<?> type, Function<? super T, ?> value) {
  }

  private final List<Column<T>> columns;
  private List<T> rows = new ArrayList<>();

  public EntityTableModel(List<Column<T>> columns) {
    this.columns = List.copyOf(columns);
  }

  public void setRows(List<T> rows) {
    this.rows = new ArrayList<>(rows);
    fireTableDataChanged();
  }

  public T getRow(int index) {
    return rows.get(index);
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public int getColumnCount() {
    return columns.size();
  }

  @Override
  public String getColumnName(int column) {
    return columns.get(column).name();
  }

  @Override
  public Class<?> getColumnClass(int column) {
    return columns.get(column).type();
  }

  @Override
  public Object getValueAt(int row, int column) {
    return columns.get(column).value().apply(rows.get(row));
  }
}
