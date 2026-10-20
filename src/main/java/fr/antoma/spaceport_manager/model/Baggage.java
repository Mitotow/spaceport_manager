package fr.antoma.spaceport_manager.model;

public record Baggage(Integer id, String tagNumber, BaggageType type, double weightKg, String description,
    boolean fragile, int passengerId, String passengerName, String flightCode)
    implements Identifiable {
}
