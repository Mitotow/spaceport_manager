package fr.antoma.spaceport_manager.ui.common;

import fr.antoma.spaceport_manager.dao.DataAccessException;
import fr.antoma.spaceport_manager.dao.Dao;
import fr.antoma.spaceport_manager.model.Identifiable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Onglet générique : barre d'outils (ajout, modification, suppression, recherche) et table triable.
 * Les sous-classes fournissent les colonnes et le formulaire d'édition.
 */
public abstract class EntityPanel<T extends Identifiable> extends JPanel {

  protected final JTable table;
  private final Dao<T> dao;
  private final EntityTableModel<T> model;
  private final TableRowSorter<EntityTableModel<T>> sorter;
  private final JToolBar toolBar = new JToolBar();
  private final Component toolBarGlue = Box.createHorizontalGlue();
  private final JTextField searchField = new JTextField(18);
  private final JLabel countLabel = new JLabel();
  private final List<JButton> selectionButtons = new ArrayList<>();

  protected EntityPanel(List<EntityTableModel.Column<T>> columns, Dao<T> dao) {
    super(new BorderLayout());
    this.dao = dao;

    model = new EntityTableModel<>(columns);
    sorter = new TableRowSorter<>(model);
    table = new JTable(model);
    table.setRowSorter(sorter);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getTableHeader().setReorderingAllowed(false);
    table.setDefaultRenderer(LocalDateTime.class, Renderers.table(LocalDateTime.class, Formats.DATE_TIME::format));
    table.setDefaultRenderer(LocalDate.class, Renderers.table(LocalDate.class, Formats.DATE::format));

    table.getSelectionModel().addListSelectionListener(e -> updateButtons());
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) {
          onEdit();
        }
      }
    });
    table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
    table.getActionMap().put("deleteRow", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDelete();
      }
    });

    searchField.putClientProperty("JTextField.placeholderText", "Rechercher...");
    searchField.putClientProperty("JTextField.showClearButton", true);
    searchField.setMaximumSize(searchField.getPreferredSize());
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        applyFilter();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        applyFilter();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        applyFilter();
      }
    });

    JButton addButton = new JButton("Ajouter");
    JButton refreshButton = new JButton("Actualiser");
    addButton.addActionListener(e -> onAdd());
    refreshButton.addActionListener(e -> refresh());

    toolBar.setFloatable(false);
    toolBar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    toolBar.add(addButton);
    addSelectionAction("Modifier", this::edit);
    addSelectionAction("Supprimer", this::deleteWithConfirmation);
    toolBar.add(refreshButton);
    toolBar.addSeparator();
    toolBar.add(toolBarGlue);
    toolBar.add(searchField);

    countLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(countLabel, BorderLayout.SOUTH);
    updateButtons();
  }

  /**
   * Ouvre le formulaire de création ({@code item == null}) ou de modification ;
   * vrai si enregistré.
   */
  protected abstract boolean openEditor(T item);

  protected abstract String deleteConfirmation(T item);

  protected abstract String countLabel(int count);

  /**
   * Ajoute à la barre d'outils un bouton qui agit sur la ligne sélectionnée
   * (désactivé tant qu'aucune ligne n'est sélectionnée).
   */
  protected void addSelectionAction(String label, Consumer<T> action) {
    JButton button = new JButton(label);
    button.addActionListener(e -> {
      T item = selected();
      if (item != null) {
        action.accept(item);
      }
    });
    selectionButtons.add(button);
    // Inséré avant la zone de recherche (index -1 = à la fin, tant qu'elle n'est
    // pas encore ajoutée)
    toolBar.add(button, toolBar.getComponentIndex(toolBarGlue));
    updateButtons();
  }

  public void refresh() {
    try {
      model.setRows(dao.findAll());
    } catch (DataAccessException e) {
      model.setRows(List.of());
      Dialogs.error(this, e.getMessage());
    }
    updateCount();
    updateButtons();
  }

  /**
   * Écrit les lignes affichées (filtre de recherche appliqué) dans un fichier CSV
   * séparé par des points-virgules, lisible par Excel ou LibreOffice.
   */
  public void exportCsv(Path file) throws IOException {
    List<String> lines = new ArrayList<>();

    List<String> header = new ArrayList<>();
    for (int column = 0; column < model.getColumnCount(); column++) {
      header.add(csvCell(model.getColumnName(column)));
    }
    lines.add(String.join(";", header));

    for (int row = 0; row < table.getRowCount(); row++) {
      List<String> cells = new ArrayList<>();
      for (int column = 0; column < table.getColumnCount(); column++) {
        cells.add(csvCell(table.getValueAt(row, column)));
      }
      lines.add(String.join(";", cells));
    }
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private static String csvCell(Object value) {
    String text = switch (value) {
      case null -> "";
      case LocalDateTime dateTime -> Formats.DATE_TIME.format(dateTime);
      case LocalDate date -> Formats.DATE.format(date);
      case Boolean bool -> bool ? "Oui" : "Non";
      default -> value.toString();
    };
    if (text.contains(";") || text.contains("\"") || text.contains("\n")) {
      text = "\"" + text.replace("\"", "\"\"") + "\"";
    }
    return text;
  }

  protected T selected() {
    int viewRow = table.getSelectedRow();
    // L'index affiché diffère de l'index du modèle quand la table est triée ou
    // filtrée
    return viewRow < 0 ? null : model.getRow(table.convertRowIndexToModel(viewRow));
  }

  private void onAdd() {
    edit(null);
  }

  private void onEdit() {
    T item = selected();
    if (item != null) {
      edit(item);
    }
  }

  private void edit(T item) {
    try {
      if (openEditor(item)) {
        refresh();
      }
    } catch (DataAccessException e) {
      // Les formulaires chargent leurs listes déroulantes depuis la base
      Dialogs.error(this, e.getMessage());
    }
  }

  private void onDelete() {
    T item = selected();
    if (item != null) {
      deleteWithConfirmation(item);
    }
  }

  private void deleteWithConfirmation(T item) {
    if (!Dialogs.confirm(this, deleteConfirmation(item))) {
      return;
    }
    try {
      dao.delete(item.id());
    } catch (DataAccessException e) {
      Dialogs.error(this, e.getMessage());
    }
    refresh();
  }

  private void applyFilter() {
    String text = searchField.getText().strip();
    sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    updateCount();
  }

  private void updateButtons() {
    boolean hasSelection = table.getSelectedRow() >= 0;
    for (JButton button : selectionButtons) {
      button.setEnabled(hasSelection);
    }
  }

  private void updateCount() {
    int visible = table.getRowCount();
    int total = model.getRowCount();
    countLabel.setText(visible == total ? countLabel(total) : countLabel(visible) + " sur " + total);
  }
}
