package fr.antoma.spaceport_manager.dao;

import fr.antoma.spaceport_manager.db.Database;
import fr.antoma.spaceport_manager.model.Baggage;
import fr.antoma.spaceport_manager.model.BaggageType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaggageDao implements Dao<Baggage> {

  private static final String SELECT_ALL = """
      SELECT b.*, CONCAT(UPPER(p.last_name), ' ', p.first_name) AS passenger_name, f.code AS flight_code
      FROM baggage b
      JOIN passenger p ON p.id = b.passenger_id
      LEFT JOIN flight f ON f.id = p.flight_id
      ORDER BY b.tag_number
      """;

  private static final String INSERT = """
      INSERT INTO baggage (tag_number, type, weight_kg, description, fragile, passenger_id)
      VALUES (?, ?, ?, ?, ?, ?)
      """;

  private static final String UPDATE = """
      UPDATE baggage
      SET tag_number = ?, type = ?, weight_kg = ?, description = ?, fragile = ?, passenger_id = ?
      WHERE id = ?
      """;

  private static final String DELETE = "DELETE FROM baggage WHERE id = ?";

  private final Database database;

  public BaggageDao(Database database) {
    this.database = database;
  }

  @Override
  public List<Baggage> findAll() {
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
        ResultSet rs = statement.executeQuery()) {
      List<Baggage> baggages = new ArrayList<>();
      while (rs.next()) {
        baggages.add(map(rs));
      }
      return baggages;
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de charger les bagages.", e);
    }
  }

  @Override
  public void save(Baggage baggage) {
    boolean creating = baggage.id() == null;
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(creating ? INSERT : UPDATE)) {
      statement.setString(1, baggage.tagNumber());
      statement.setString(2, baggage.type().name());
      statement.setDouble(3, baggage.weightKg());
      statement.setString(4, baggage.description());
      statement.setBoolean(5, baggage.fragile());
      statement.setInt(6, baggage.passengerId());
      if (!creating) {
        statement.setInt(7, baggage.id());
      }
      statement.executeUpdate();
    } catch (SQLException e) {
      throw DataAccessException.onSave("Impossible d'enregistrer le bagage.",
          "Un bagage avec l'étiquette '" + baggage.tagNumber() + "' existe déjà.", e);
    }
  }

  @Override
  public void delete(int id) {
    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw DataAccessException.of("Impossible de supprimer le bagage.", e);
    }
  }

  private static Baggage map(ResultSet rs) throws SQLException {
    return new Baggage(
        rs.getInt("id"),
        rs.getString("tag_number"),
        BaggageType.valueOf(rs.getString("type")),
        rs.getDouble("weight_kg"),
        rs.getString("description"),
        rs.getBoolean("fragile"),
        rs.getInt("passenger_id"),
        rs.getString("passenger_name"),
        rs.getString("flight_code"));
  }
}
