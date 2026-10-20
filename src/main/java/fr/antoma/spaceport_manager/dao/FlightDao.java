package fr.antoma.spaceport_manager.dao;

import fr.antoma.spaceport_manager.db.Database;
import fr.antoma.spaceport_manager.model.Flight;
import fr.antoma.spaceport_manager.model.FlightStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDao implements Dao<Flight> {

    private static final String SELECT_ALL = """
            SELECT f.*, (SELECT COUNT(*) FROM passenger p WHERE p.flight_id = f.id) AS passenger_count
            FROM flight f
            ORDER BY f.departure_time
            """;

    private static final String INSERT = """
            INSERT INTO flight (code, ship, origin, destination, departure_time, arrival_time, dock, capacity, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE flight
            SET code = ?, ship = ?, origin = ?, destination = ?, departure_time = ?, arrival_time = ?,
                dock = ?, capacity = ?, status = ?
            WHERE id = ?
            """;

    private static final String DELETE = "DELETE FROM flight WHERE id = ?";

    private static final String UPDATE_STATUS = "UPDATE flight SET status = ? WHERE id = ?";

    private final Database database;

    public FlightDao(Database database) {
        this.database = database;
    }

    @Override
    public List<Flight> findAll() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = statement.executeQuery()) {
            List<Flight> flights = new ArrayList<>();
            while (rs.next()) {
                flights.add(map(rs));
            }
            return flights;
        } catch (SQLException e) {
            throw DataAccessException.of("Impossible de charger les vols.", e);
        }
    }

    @Override
    public void save(Flight flight) {
        boolean creating = flight.id() == null;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(creating ? INSERT : UPDATE)) {
            statement.setString(1, flight.code());
            statement.setString(2, flight.ship());
            statement.setString(3, flight.origin());
            statement.setString(4, flight.destination());
            statement.setObject(5, flight.departure());
            statement.setObject(6, flight.arrival());
            statement.setString(7, flight.dock());
            statement.setInt(8, flight.capacity());
            statement.setString(9, flight.status().name());
            if (!creating) {
                statement.setInt(10, flight.id());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.onSave("Impossible d'enregistrer le vol.",
                    "Un vol avec le code '" + flight.code() + "' existe déjà.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.of("Impossible de supprimer le vol.", e);
        }
    }

    public void updateStatus(int id, FlightStatus status) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {
            statement.setString(1, status.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.of("Impossible de changer le statut du vol.", e);
        }
    }

    private static Flight map(ResultSet rs) throws SQLException {
        return new Flight(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("ship"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getObject("departure_time", LocalDateTime.class),
                rs.getObject("arrival_time", LocalDateTime.class),
                rs.getString("dock"),
                rs.getInt("capacity"),
                FlightStatus.valueOf(rs.getString("status")),
                rs.getInt("passenger_count"));
    }
}
