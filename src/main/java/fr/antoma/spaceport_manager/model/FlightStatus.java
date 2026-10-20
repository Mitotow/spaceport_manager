package fr.antoma.spaceport_manager.model;

public enum FlightStatus {
  SCHEDULED("Programmé"),
  BOARDING("Embarquement"),
  DELAYED("Retardé"),
  DEPARTED("Parti"),
  ARRIVED("Arrivé"),
  CANCELLED("Annulé");

  private final String label;

  FlightStatus(String label) {
    this.label = label;
  }

  /** Indique si de nouveaux passagers peuvent encore être affectés au vol. */
  public boolean isOpenForBoarding() {
    return this == SCHEDULED || this == BOARDING || this == DELAYED;
  }

  @Override
  public String toString() {
    return label;
  }
}
