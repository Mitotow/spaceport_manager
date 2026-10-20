package fr.antoma.spaceport_manager.ui.passenger;

import fr.antoma.spaceport_manager.dao.FlightDao;
import fr.antoma.spaceport_manager.dao.PassengerDao;
import fr.antoma.spaceport_manager.model.Flight;
import fr.antoma.spaceport_manager.model.Passenger;
import fr.antoma.spaceport_manager.ui.common.DateSpinners;
import fr.antoma.spaceport_manager.ui.common.Formats;
import fr.antoma.spaceport_manager.ui.common.FormDialog;
import fr.antoma.spaceport_manager.ui.common.Renderers;
import fr.antoma.spaceport_manager.ui.common.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

public class PassengerDialog extends FormDialog<Passenger> {

  private static final String[] PLANETS = { "Terre", "Lune", "Mars", "Cérès", "Europe", "Ganymède", "Titan" };

  private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  private final PassengerDao passengerDao;
  private final Passenger existing;

  private final JTextField lastNameField = new JTextField(20);
  private final JTextField firstNameField = new JTextField(20);
  private final JSpinner birthDateField;
  private final JComboBox<String> originPlanetField;
  private final JTextField passportField = new JTextField(20);
  private final JTextField emailField = new JTextField(20);
  private final JComboBox<Flight> flightField = new JComboBox<>();

  public PassengerDialog(Component parent, PassengerDao passengerDao, FlightDao flightDao, Passenger existing) {
    super(parent, existing == null ? "Nouveau passager" : "Modifier " + existing.fullName());
    this.passengerDao = passengerDao;
    this.existing = existing;

    birthDateField = DateSpinners.date(existing == null ? LocalDate.of(1990, 1, 1) : existing.birthDate());
    originPlanetField = editableCombo(PLANETS, existing == null ? "Terre" : existing.originPlanet());

    // Seuls les vols ouverts à l'embarquement sont proposés, plus le vol actuel du
    // passager
    Integer currentFlightId = existing == null ? null : existing.flightId();
    flightField.addItem(null);
    for (Flight flight : flightDao.findAll()) {
      if (flight.status().isOpenForBoarding() || flight.id().equals(currentFlightId)) {
        flightField.addItem(flight);
        if (flight.id().equals(currentFlightId)) {
          flightField.setSelectedItem(flight);
        }
      }
    }
    flightField.setRenderer(Renderers.list(Flight.class, PassengerDialog::describe, "(aucun vol)"));

    if (existing != null) {
      lastNameField.setText(existing.lastName());
      firstNameField.setText(existing.firstName());
      passportField.setText(existing.passportNumber());
      emailField.setText(existing.email());
    }
    passportField.putClientProperty("JTextField.placeholderText", "ex. TER-123456");

    addField("Nom *", lastNameField);
    addField("Prénom *", firstNameField);
    addField("Date de naissance *", birthDateField);
    addField("Planète d'origine *", originPlanetField);
    addField("N° de passeport *", passportField);
    addField("Email", emailField);
    addField("Vol", flightField);
    finishLayout();
  }

  private static String describe(Flight flight) {
    return flight.code() + " - " + flight.destination()
        + " (" + Formats.DATE_TIME.format(flight.departure()) + ", "
        + flight.passengerCount() + "/" + flight.capacity() + " places)";
  }

  @Override
  protected Passenger buildValue() throws ValidationException {
    String lastName = requiredText(lastNameField, "Nom");
    String firstName = requiredText(firstNameField, "Prénom");
    LocalDate birthDate = DateSpinners.getDate(birthDateField);
    String originPlanet = requiredText(originPlanetField, "Planète d'origine");
    String passport = requiredText(passportField, "N° de passeport").toUpperCase();
    String email = optionalText(emailField);
    Flight flight = (Flight) flightField.getSelectedItem();
    Integer flightId = flight == null ? null : flight.id();

    if (birthDate.isAfter(LocalDate.now())) {
      throw new ValidationException("La date de naissance ne peut pas être dans le futur.");
    }
    if (email != null && !EMAIL.matcher(email).matches()) {
      throw new ValidationException("L'adresse email n'est pas valide.");
    }
    // La place n'est vérifiée que si le passager change de vol
    boolean flightChanged = !Objects.equals(flightId, existing == null ? null : existing.flightId());
    Integer passengerId = existing == null ? null : existing.id();
    if (flightId != null && flightChanged && passengerDao.isFlightFull(flightId, passengerId)) {
      throw new ValidationException("Le vol " + flight.code() + " est complet.");
    }

    return new Passenger(passengerId, lastName, firstName, birthDate, originPlanet, passport, email,
        flightId, flight == null ? null : flight.code(), existing == null ? 0 : existing.baggageCount());
  }

  @Override
  protected void persist(Passenger passenger) {
    passengerDao.save(passenger);
  }

  @Override
  protected String saveConfirmation(Passenger passenger) {
    return existing == null
        ? "Ajouter le passager " + passenger.fullName() + " ?"
        : "Enregistrer les modifications du passager " + passenger.fullName() + " ?";
  }
}
