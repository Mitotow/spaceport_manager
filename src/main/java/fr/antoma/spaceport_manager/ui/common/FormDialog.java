package fr.antoma.spaceport_manager.ui.common;

import fr.antoma.spaceport_manager.dao.DataAccessException;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Fenêtre modale de saisie : un formulaire libellé/champ et des boutons
 * Enregistrer/Annuler.
 * La fenêtre ne se ferme que si la saisie est valide et que l'enregistrement a
 * réussi.
 *
 * @param <T> type de l'objet édité
 */
public abstract class FormDialog<T> extends JDialog {

  private final JPanel form = new JPanel(new GridBagLayout());
  private int row;
  private boolean saved;

  protected FormDialog(Component parent, String title) {
    super(SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  /**
   * Construit l'objet à partir des champs, ou lève une exception si la saisie est
   * invalide.
   */
  protected abstract T buildValue() throws ValidationException;

  protected abstract void persist(T value);

  protected abstract String saveConfirmation(T value);

  protected void addField(String label, JComponent field) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = row++;
    c.insets = new Insets(4, 4, 4, 4);

    c.gridx = 0;
    c.anchor = GridBagConstraints.LINE_END;
    form.add(new JLabel(label), c);

    c.gridx = 1;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    form.add(field, c);
  }

  /**
   * À appeler à la fin du constructeur des sous-classes, une fois les champs
   * ajoutés.
   */
  protected void finishLayout() {
    JButton saveButton = new JButton("Enregistrer");
    JButton cancelButton = new JButton("Annuler");
    saveButton.addActionListener(e -> onSave());
    cancelButton.addActionListener(e -> dispose());

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    buttons.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    buttons.add(saveButton);
    buttons.add(cancelButton);

    JPanel content = new JPanel(new BorderLayout());
    content.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));
    content.add(form, BorderLayout.CENTER);
    content.add(buttons, BorderLayout.SOUTH);
    setContentPane(content);

    getRootPane().setDefaultButton(saveButton);
    getRootPane().registerKeyboardAction(e -> dispose(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    pack();
    setMinimumSize(getSize());
    setLocationRelativeTo(getOwner());
  }

  public boolean showDialog() {
    setVisible(true);
    return saved;
  }

  private void onSave() {
    try {
      T value = buildValue();
      if (!Dialogs.confirm(this, saveConfirmation(value))) {
        return;
      }
      persist(value);
      saved = true;
      dispose();
    } catch (ValidationException e) {
      Dialogs.warning(this, e.getMessage());
    } catch (DataAccessException e) {
      Dialogs.error(this, e.getMessage());
    }
  }

  protected static String requiredText(JTextField field, String label) throws ValidationException {
    String text = field.getText().strip();
    if (text.isEmpty()) {
      throw new ValidationException("Le champ '" + label + "' est obligatoire.");
    }
    return text;
  }

  protected static String requiredText(JComboBox<String> combo, String label) throws ValidationException {
    Object value = combo.isEditable() ? combo.getEditor().getItem() : combo.getSelectedItem();
    String text = value == null ? "" : value.toString().strip();
    if (text.isEmpty()) {
      throw new ValidationException("Le champ '" + label + "' est obligatoire.");
    }
    return text;
  }

  protected static String optionalText(JTextComponent field) {
    String text = field.getText().strip();
    return text.isEmpty() ? null : text;
  }

  protected static JComboBox<String> editableCombo(String[] suggestions, String value) {
    JComboBox<String> combo = new JComboBox<>(suggestions);
    combo.setEditable(true);
    combo.setSelectedItem(value);
    return combo;
  }
}
