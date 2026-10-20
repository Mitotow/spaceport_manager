package fr.antoma.spaceport_manager.ui.flight;

import fr.antoma.spaceport_manager.dao.FlightDao;
import fr.antoma.spaceport_manager.model.Flight;
import fr.antoma.spaceport_manager.model.FlightStatus;
import fr.antoma.spaceport_manager.ui.common.DateSpinners;
import fr.antoma.spaceport_manager.ui.common.FormDialog;
import fr.antoma.spaceport_manager.ui.common.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FlightDialog extends FormDialog<Flight> {

  private static final String HOME_SPACEPORT = "Terre - Spatioport Antoma";

  private static final String[] LOCATIONS = {
      HOME_SPACEPORT,
      "Lune - Base Tranquillité",
      "Mars - Olympus Port",
      "Mars - Valles Hub",
      "Cérès - Station Dawn",
      "Europe - Station Cryos",
      "Ganymède - Port Galilée",
      "Titan - Kraken Harbor",
  };

  private final FlightDao flightDao;
  private final Flight existing;

  private final JTextField codeField = new JTextField(20);
  private final JTextField shipField = new JTextField(20);
  private final JComboBox<String> originField;
  private final JComboBox<String> destinationField;
  private final JSpinner departureField;
  private final JSpinner arrivalField;
  private final JTextField dockField = new JTextField(20);
  private final JSpinner capacityField;
  private final JComboBox<FlightStatus> statusField = new JComboBox<>(FlightStatus.values());

  public FlightDialog(Component parent, FlightDao flightDao, Flight existing) {
    super(parent, existing == null ? "Nouveau vol" : "Modifier le vol " + existing.code());
    this.flightDao = flightDao;
    this.existing = existing;

    LocalDateTime defaultDeparture = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.HOURS);
    originField = editableCombo(LOCATIONS, existing == null ? HOME_SPACEPORT : existing.origin());
    destinationField = editableCombo(LOCATIONS, existing == null ? "" : existing.destination());
    departureField = DateSpinners.dateTime(existing == null ? defaultDeparture : existing.departure());
    arrivalField = DateSpinners.dateTime(existing == null ? defaultDeparture.plusDays(3) : existing.arrival());
    capacityField = new JSpinner(new SpinnerNumberModel(existing == null ? 100 : existing.capacity(), 1, 10_000, 1));

    if (existing != null) {
      codeField.setText(existing.code());
      shipField.setText(existing.ship());
      dockField.setText(existing.dock());
      statusField.setSelectedItem(existing.status());
    }
    codeField.putClientProperty("JTextField.placeholderText", "ex. SPX-101");
    dockField.putClientProperty("JTextField.placeholderText", "ex. Q-04");

    addField("Code du vol *", codeField);
    addField("Vaisseau *", shipField);
    addField("Origine *", originField);
    addField("Destination *", destinationField);
    addField("Départ *", departureField);
    addField("Arrivée *", arrivalField);
    addField("Quai", dockField);
    addField("Capacité *", capacityField);
    addField("Statut", statusField);
    finishLayout();
  }

  @Override
  protected Flight buildValue() throws ValidationException {
    String code = requiredText(codeField, "Code du vol").toUpperCase();
    String ship = requiredText(shipField, "Vaisseau");
    String origin = requiredText(originField, "Origine");
    String destination = requiredText(destinationField, "Destination");
    LocalDateTime departure = DateSpinners.getDateTime(departureField);
    LocalDateTime arrival = DateSpinners.getDateTime(arrivalField);
    int capacity = (Integer) capacityField.getValue();
    int passengerCount = existing == null ? 0 : existing.passengerCount();

    // Règles de gestion
    if (origin.equalsIgnoreCase(destination)) {
      throw new ValidationException("L'origine et la destination doivent être différentes.");
    }
    if (!arrival.isAfter(departure)) {
      throw new ValidationException("L'arrivée doit être postérieure au départ.");
    }
    if (capacity < passengerCount) {
      throw new ValidationException("La capacité ne peut pas être inférieure au nombre de passagers déjà "
          + "affectés (" + passengerCount + ").");
    }

    return new Flight(existing == null ? null : existing.id(), code, ship, origin, destination,
        departure, arrival, optionalText(dockField), capacity,
        (FlightStatus) statusField.getSelectedItem(), passengerCount);
  }

  @Override
  protected void persist(Flight flight) {
    flightDao.save(flight);
  }

  @Override
  protected String saveConfirmation(Flight flight) {
    return existing == null
        ? "Ajouter le vol " + flight.code() + " à destination de " + flight.destination() + " ?"
        : "Enregistrer les modifications du vol " + flight.code() + " ?";
  }
}
