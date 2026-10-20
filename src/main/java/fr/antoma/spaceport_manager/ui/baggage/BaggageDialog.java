package fr.antoma.spaceport_manager.ui.baggage;

import fr.antoma.spaceport_manager.dao.BaggageDao;
import fr.antoma.spaceport_manager.dao.PassengerDao;
import fr.antoma.spaceport_manager.model.Baggage;
import fr.antoma.spaceport_manager.model.BaggageType;
import fr.antoma.spaceport_manager.model.Passenger;
import fr.antoma.spaceport_manager.ui.common.FormDialog;
import fr.antoma.spaceport_manager.ui.common.Renderers;
import fr.antoma.spaceport_manager.ui.common.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BaggageDialog extends FormDialog<Baggage> {

  private static final int MAX_DESCRIPTION_LENGTH = 255;

  private final BaggageDao baggageDao;
  private final Baggage existing;

  private final JTextField tagField = new JTextField(20);
  private final JComboBox<Passenger> passengerField = new JComboBox<>();
  // Un bouton radio par type de bagage réunit dans un ButtonGroup
  private final Map<BaggageType, JRadioButton> typeButtons = new EnumMap<>(BaggageType.class);
  private final ButtonGroup typeGroup = new ButtonGroup();
  private final JSpinner weightField;
  private final JLabel weightHint = new JLabel();
  private final JCheckBox fragileBox = new JCheckBox("Manipuler avec précaution");
  private final JTextArea descriptionArea = new JTextArea(3, 20);

  public BaggageDialog(Component parent, BaggageDao baggageDao, PassengerDao passengerDao, Baggage existing) {
    super(parent, existing == null ? "Nouveau bagage" : "Modifier le bagage " + existing.tagNumber());
    this.baggageDao = baggageDao;
    this.existing = existing;

    for (Passenger passenger : passengerDao.findAll()) {
      passengerField.addItem(passenger);
      if (existing != null && passenger.id() == existing.passengerId()) {
        passengerField.setSelectedItem(passenger);
      }
    }
    passengerField.setRenderer(Renderers.list(Passenger.class, BaggageDialog::describe, "(choisir un passager)"));
    if (existing == null) {
      passengerField.setSelectedItem(null);
    }

    JPanel typePanel = new JPanel(new GridLayout(0, 2));
    for (BaggageType type : BaggageType.values()) {
      JRadioButton button = new JRadioButton(type.toString());
      button.addActionListener(e -> updateWeightHint());
      typeGroup.add(button);
      typeButtons.put(type, button);
      typePanel.add(button);
    }
    typeButtons.get(existing == null ? BaggageType.CABIN : existing.type()).setSelected(true);

    weightField = new JSpinner(new SpinnerNumberModel(existing == null ? 10.0 : existing.weightKg(), 0.1, 999.0, 0.5));
    weightHint.setForeground(UIManager.getColor("Label.disabledForeground"));
    updateWeightHint();

    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    if (existing != null) {
      tagField.setText(existing.tagNumber());
      fragileBox.setSelected(existing.fragile());
      descriptionArea.setText(existing.description());
    } else {
      tagField.setText(generateTag());
    }

    addField("N° d'étiquette *", tagField);
    addField("Passager *", passengerField);
    addField("Type *", typePanel);
    addField("Poids (kg) *", weightField);
    addField("", weightHint);
    addField("Fragile", fragileBox);
    addField("Description", new JScrollPane(descriptionArea));
    finishLayout();
  }

  private static String describe(Passenger passenger) {
    String flight = passenger.flightCode() == null ? "sans vol" : "vol " + passenger.flightCode();
    return passenger.fullName() + " - " + passenger.passportNumber() + " (" + flight + ")";
  }

  private static String generateTag() {
    return "BG-" + ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
  }

  private BaggageType selectedType() {
    for (Map.Entry<BaggageType, JRadioButton> entry : typeButtons.entrySet()) {
      if (entry.getValue().isSelected()) {
        return entry.getKey();
      }
    }
    return BaggageType.CABIN;
  }

  private void updateWeightHint() {
    weightHint.setText("Poids maximum pour ce type : " + formatKg(selectedType().maxWeightKg()));
  }

  private static String formatKg(double kg) {
    return String.format("%.1f kg", kg);
  }

  @Override
  protected Baggage buildValue() throws ValidationException {
    String tag = requiredText(tagField, "N° d'étiquette").toUpperCase();
    Passenger passenger = (Passenger) passengerField.getSelectedItem();
    BaggageType type = selectedType();
    double weight = ((Number) weightField.getValue()).doubleValue();
    String description = optionalText(descriptionArea);

    if (passenger == null) {
      throw new ValidationException("Veuillez choisir le passager propriétaire du bagage.");
    }
    if (weight > type.maxWeightKg()) {
      throw new ValidationException("Un bagage de type '" + type + "' ne peut pas dépasser "
          + formatKg(type.maxWeightKg()) + ".");
    }
    if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
      throw new ValidationException("La description ne doit pas dépasser " + MAX_DESCRIPTION_LENGTH
          + " caractères.");
    }

    return new Baggage(existing == null ? null : existing.id(), tag, type, weight, description,
        fragileBox.isSelected(), passenger.id(), passenger.fullName(), passenger.flightCode());
  }

  @Override
  protected void persist(Baggage baggage) {
    baggageDao.save(baggage);
  }

  @Override
  protected String saveConfirmation(Baggage baggage) {
    return existing == null
        ? "Ajouter le bagage " + baggage.tagNumber() + " pour " + baggage.passengerName() + " ?"
        : "Enregistrer les modifications du bagage " + baggage.tagNumber() + " ?";
  }
}
