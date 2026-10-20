package fr.antoma.spaceport_manager.ui.baggage;

import fr.antoma.spaceport_manager.dao.BaggageDao;
import fr.antoma.spaceport_manager.dao.PassengerDao;
import fr.antoma.spaceport_manager.model.Baggage;
import fr.antoma.spaceport_manager.model.BaggageType;
import fr.antoma.spaceport_manager.ui.common.EntityPanel;
import fr.antoma.spaceport_manager.ui.common.EntityTableModel.Column;

import java.util.List;

public class BaggagePanel extends EntityPanel<Baggage> {

  private final BaggageDao baggageDao;
  private final PassengerDao passengerDao;

  public BaggagePanel(BaggageDao baggageDao, PassengerDao passengerDao) {
    super(List.of(
        new Column<>("Étiquette", String.class, Baggage::tagNumber),
        new Column<>("Type", BaggageType.class, Baggage::type),
        new Column<>("Poids (kg)", Double.class, Baggage::weightKg),
        new Column<>("Fragile", Boolean.class, Baggage::fragile),
        new Column<>("Description", String.class, Baggage::description),
        new Column<>("Passager", String.class, Baggage::passengerName),
        new Column<>("Vol", String.class, Baggage::flightCode)), baggageDao);
    this.baggageDao = baggageDao;
    this.passengerDao = passengerDao;
  }

  @Override
  protected boolean openEditor(Baggage baggage) {
    return new BaggageDialog(this, baggageDao, passengerDao, baggage).showDialog();
  }

  @Override
  protected String deleteConfirmation(Baggage baggage) {
    return "Supprimer le bagage " + baggage.tagNumber() + " de " + baggage.passengerName() + " ?";
  }

  @Override
  protected String countLabel(int count) {
    return count + " bagage(s)";
  }
}
