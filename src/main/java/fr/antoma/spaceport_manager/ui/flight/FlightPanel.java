package fr.antoma.spaceport_manager.ui.flight;

import fr.antoma.spaceport_manager.dao.DataAccessException;
import fr.antoma.spaceport_manager.dao.FlightDao;
import fr.antoma.spaceport_manager.dao.PassengerDao;
import fr.antoma.spaceport_manager.model.Flight;
import fr.antoma.spaceport_manager.model.FlightStatus;
import fr.antoma.spaceport_manager.ui.common.Dialogs;
import fr.antoma.spaceport_manager.ui.common.EntityPanel;
import fr.antoma.spaceport_manager.ui.common.EntityTableModel.Column;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Onglet Vols : liste des vols, changement rapide de statut et liste des
 * passagers d'un vol.
 */
public class FlightPanel extends EntityPanel<Flight> {

  private final FlightDao flightDao;
  private final PassengerDao passengerDao;

  public FlightPanel(FlightDao flightDao, PassengerDao passengerDao) {
    super(List.of(
        new Column<>("Code", String.class, Flight::code),
        new Column<>("Vaisseau", String.class, Flight::ship),
        new Column<>("Origine", String.class, Flight::origin),
        new Column<>("Destination", String.class, Flight::destination),
        new Column<>("Départ", LocalDateTime.class, Flight::departure),
        new Column<>("Arrivée", LocalDateTime.class, Flight::arrival),
        new Column<>("Quai", String.class, Flight::dock),
        new Column<>("Passagers", Integer.class, Flight::passengerCount),
        new Column<>("Capacité", Integer.class, Flight::capacity),
        new Column<>("Statut", FlightStatus.class, Flight::status)), flightDao);
    this.flightDao = flightDao;
    this.passengerDao = passengerDao;
    table.setDefaultRenderer(FlightStatus.class, new StatusRenderer());

    addSelectionAction("Changer le statut", this::changeStatus);
    addSelectionAction("Passagers du vol", this::showPassengers);
  }

  @Override
  protected boolean openEditor(Flight flight) {
    return new FlightDialog(this, flightDao, flight).showDialog();
  }

  @Override
  protected String deleteConfirmation(Flight flight) {
    String message = "Supprimer le vol " + flight.code() + " à destination de " + flight.destination() + " ?";
    if (flight.passengerCount() > 0) {
      message += "\nSes " + flight.passengerCount() + " passager(s) ne seront plus affectés à aucun vol.";
    }
    return message;
  }

  @Override
  protected String countLabel(int count) {
    return count + " vol(s)";
  }

  private void changeStatus(Flight flight) {
    FlightStatus status = (FlightStatus) JOptionPane.showInputDialog(this,
        "Nouveau statut du vol " + flight.code() + " :", "Changer le statut",
        JOptionPane.QUESTION_MESSAGE, null, FlightStatus.values(), flight.status());
    if (status == null || status == flight.status()) {
      return;
    }
    if (!Dialogs.confirm(this, "Passer le vol " + flight.code() + " de '" + flight.status()
        + "' à '" + status + "' ?")) {
      return;
    }
    try {
      flightDao.updateStatus(flight.id(), status);
    } catch (DataAccessException e) {
      Dialogs.error(this, e.getMessage());
    }
    refresh();
  }

  private void showPassengers(Flight flight) {
    List<String> names;
    try {
      names = passengerDao.findAll().stream()
          .filter(passenger -> flight.id().equals(passenger.flightId()))
          .map(passenger -> passenger.fullName() + " - " + passenger.passportNumber())
          .toList();
    } catch (DataAccessException e) {
      Dialogs.error(this, e.getMessage());
      return;
    }
    String title = "Passagers du vol " + flight.code() + " (" + names.size() + "/" + flight.capacity() + ")";
    if (names.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Aucun passager n'est affecté à ce vol.", title,
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    JList<String> list = new JList<>(names.toArray(String[]::new));
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(new Dimension(380, 200));
    JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
  }

  private static class StatusRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (!isSelected && value instanceof FlightStatus status) {
        Color color = switch (status) {
          case BOARDING -> new Color(0x1E88E5);
          case DELAYED -> new Color(0xEF6C00);
          case CANCELLED -> new Color(0xE53935);
          case DEPARTED, ARRIVED -> new Color(0x43A047);
          case SCHEDULED -> table.getForeground();
        };
        setForeground(color);
      }
      return this;
    }
  }
}
