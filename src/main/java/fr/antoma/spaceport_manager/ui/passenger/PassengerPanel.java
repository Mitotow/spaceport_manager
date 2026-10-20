package fr.antoma.spaceport_manager.ui.passenger;

import fr.antoma.spaceport_manager.dao.FlightDao;
import fr.antoma.spaceport_manager.dao.PassengerDao;
import fr.antoma.spaceport_manager.model.Passenger;
import fr.antoma.spaceport_manager.ui.common.EntityPanel;
import fr.antoma.spaceport_manager.ui.common.EntityTableModel.Column;

import java.time.LocalDate;
import java.util.List;

public class PassengerPanel extends EntityPanel<Passenger> {

  private final PassengerDao passengerDao;
  private final FlightDao flightDao;

  public PassengerPanel(PassengerDao passengerDao, FlightDao flightDao) {
    super(List.of(
        new Column<>("Nom", String.class, Passenger::lastName),
        new Column<>("Prénom", String.class, Passenger::firstName),
        new Column<>("Naissance", LocalDate.class, Passenger::birthDate),
        new Column<>("Planète d'origine", String.class, Passenger::originPlanet),
        new Column<>("Passeport", String.class, Passenger::passportNumber),
        new Column<>("Email", String.class, Passenger::email),
        new Column<>("Vol", String.class, Passenger::flightCode),
        new Column<>("Bagages", Integer.class, Passenger::baggageCount)), passengerDao);
    this.passengerDao = passengerDao;
    this.flightDao = flightDao;
  }

  @Override
  protected boolean openEditor(Passenger passenger) {
    return new PassengerDialog(this, passengerDao, flightDao, passenger).showDialog();
  }

  @Override
  protected String deleteConfirmation(Passenger passenger) {
    String message = "Supprimer le passager " + passenger.fullName() + " ?";
    if (passenger.baggageCount() > 0) {
      message += "\nSes " + passenger.baggageCount() + " bagage(s) seront également supprimés.";
    }
    return message;
  }

  @Override
  protected String countLabel(int count) {
    return count + " passager(s)";
  }
}
