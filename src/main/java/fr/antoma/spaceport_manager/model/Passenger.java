package fr.antoma.spaceport_manager.model;

import java.time.LocalDate;

public record Passenger(Integer id, String lastName, String firstName, LocalDate birthDate,
    String originPlanet, String passportNumber, String email,
    Integer flightId, String flightCode, int baggageCount) implements Identifiable {

  public String fullName() {
    return lastName.toUpperCase() + " " + firstName;
  }
}
