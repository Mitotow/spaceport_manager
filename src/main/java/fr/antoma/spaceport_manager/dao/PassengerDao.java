package fr.antoma.spaceport_manager.dao;

import fr.antoma.spaceport_manager.db.Database;
import fr.antoma.spaceport_manager.model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PassengerDao implements Dao<Passenger> {

  private static final String SELECT_ALL = """
      SELECT p.*, f.code AS flight_code,
             (SELECT COUNT(*) FROM baggage b WHERE b.passenger_id = p.id) AS baggage_count
      FROM passenger p
      LEFT JOIN flight f ON f.id = p.flight_id
      ORDER BY p.last_name, p.first_name
      """;

  private static final String INSERT = """
      INSERT INTO passenger (last_name, first_name, birth_date, origin_planet, passport_number, email, flight_id)
      VALUES (?, ?, ?, ?, ?, ?, ?)
      """;

  private static final String UPDATE = """
      UPDATE passenger
      SET last_name = ?, first_name = ?, birth_date = ?, origin_planet = ?, passport_number = ?, email = ?,
          flight_id = ?
      WHERE id = ?
      """;

  private static final String DELETE = "DELETE FROM passenger WHERE id = ?";

  private static final String IS_FLIGHT_FULL = """
      SELECT f.capacity <= (SELECT COUNT(*) FROM passenger p WHERE p.flight_id = f.id AND p.id <> ?)
      FROM flight f
      WHERE f.id = ?
      """;

  private final Database database;

  public PassengerDao(Database database) {
    this.database = database;
  }

  @Override
  public List<Passenger> findAll() {
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
        ResultSet rs = statement.executeQuery()) {
      List<Passenger> passengers = new ArrayList<>();

      while (rs.next()) {
        passengers.add(map(rs));
      }
      return passengers;
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de charger les passagers.", e);
    }
  }

  @Override
  public void save(Passenger passenger) {
    boolean creating = passenger.id() == null;
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(creating ? INSERT : UPDATE)) {
      statement.setString(1, passenger.lastName());
      statement.setString(2, passenger.firstName());
      statement.setObject(3, passenger.birthDate());
      statement.setString(4, passenger.originPlanet());
      statement.setString(5, passenger.passportNumber());
      statement.setString(6, passenger.email());
      if (passenger.flightId() == null) {
        statement.setNull(7, Types.INTEGER);
      } else {
        statement.setInt(7, passenger.flightId());
      }
      if (!creating) {
        statement.setInt(8, passenger.id());
      }
      statement.executeUpdate();
    } catch (SQLException e) {
      throw DataAccessException.onSave("Impossible d'enregistrer le passager.",
          "Un passager avec le passeport '" + passenger.passportNumber() + "' existe déjà.", e);
    }
  }

  @Override
  public void delete(int id) {
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de supprimer le passager.", e);
    }
  }

  /**
   * Indique si le vol est complet.
   *
   * @param excludedPassengerId passager à ne pas compter (celui qu'on modifie),
   *                            ou {@code null}
   */
  public boolean isFlightFull(int flightId, Integer excludedPassengerId) {
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(IS_FLIGHT_FULL)) {
      statement.setInt(1, excludedPassengerId == null ? -1 : excludedPassengerId);
      statement.setInt(2, flightId);
      try (ResultSet rs = statement.executeQuery()) {
        return rs.next() && rs.getBoolean(1);
      }
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de vérifier les places disponibles.", e);
    }
  }

  private static Passenger map(ResultSet rs) throws SQLException {
    return new Passenger(
        rs.getInt("id"),
        rs.getString("last_name"),
        rs.getString("first_name"),
        rs.getObject("birth_date", LocalDate.class),
        rs.getString("origin_planet"),
        rs.getString("passport_number"),
        rs.getString("email"),
        rs.getObject("flight_id", Integer.class),
        rs.getString("flight_code"),
        rs.getInt("baggage_count"));
  }
}
