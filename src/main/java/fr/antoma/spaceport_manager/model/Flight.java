package fr.antoma.spaceport_manager.model;

import java.time.LocalDateTime;

/**
 * Vol au départ ou à l'arrivée du spatioport.
 *
 * @param id             identifiant, {@code null} pour un vol pas encore
 *                       enregistré
 * @param passengerCount nombre de passagers affectés
 */
public record Flight(Integer id, String code, String ship, String origin, String destination,
    LocalDateTime departure, LocalDateTime arrival, String dock, int capacity,
    FlightStatus status, int passengerCount) implements Identifiable {
}
