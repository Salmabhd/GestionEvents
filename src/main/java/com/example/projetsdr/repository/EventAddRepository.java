package com.example.projetsdr.repository;

import com.example.projetsdr.model.Event;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventAddRepository {
    private static final Logger LOGGER = Logger.getLogger(EventAddRepository.class.getName());
    private final DataSource dataSource;

    public EventAddRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String INSERT_EVENT =
            "INSERT INTO events(title, description, event_date, venue, city, category, status, " +
                    "max_participants, tickets_sold, ticket_price, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_EVENTS =
            "SELECT * FROM events ORDER BY event_date ASC";

    private static final String SELECT_EVENT_BY_ID =
            "SELECT * FROM events WHERE id = ?";

    private static final String UPDATE_EVENT =
            "UPDATE events SET title = ?, description = ?, event_date = ?, venue = ?, city = ?, " +
                    "category = ?, status = ?, max_participants = ?, tickets_sold = ?, ticket_price = ?, " +
                    "updated_at = ? WHERE id = ?";

    private static final String DELETE_EVENT =
            "DELETE FROM events WHERE id = ?";

    private static final String COUNT_EVENTS =
            "SELECT COUNT(*) FROM events";

    public boolean save(Event event) {
        LOGGER.info("Tentative de sauvegarde de l'événement: " + event.getTitle());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_EVENT, Statement.RETURN_GENERATED_KEYS)) {

            LOGGER.info("Connexion obtenue, préparation des paramètres...");

            // Définir les timestamps
            LocalDateTime now = LocalDateTime.now();
            event.setCreatedAt(now);
            event.setUpdatedAt(now);

            // Définir les valeurs par défaut si nécessaires
            if (event.getTicketsSold() == null) {
                event.setTicketsSold(0);
            }
            if (event.getStatus() == null) {
                event.setStatus("ACTIVE");
            }

            setEventParameters(stmt, event);

            LOGGER.info("Exécution de la requête INSERT...");
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Nombre de lignes affectées: " + rowsAffected);

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long generatedId = generatedKeys.getLong(1);
                        event.setId(generatedId);
                        LOGGER.info("ID généré pour l'événement: " + generatedId);
                    }
                }
                LOGGER.info("Sauvegarde réussie!");
                return true;
            } else {
                LOGGER.warning("Aucune ligne n'a été affectée lors de l'insertion");
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la sauvegarde: " + e.getMessage(), e);
            LOGGER.severe("Code d'erreur SQL: " + e.getErrorCode());
            LOGGER.severe("État SQL: " + e.getSQLState());
            return false;
        }
    }

    public List<Event> findAll() {
        List<Event> events = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_EVENTS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des événements", e);
        }

        return events;
    }

    public Event findById(Long id) {
        if (id == null) {
            return null;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EVENT_BY_ID)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvent(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche par ID: " + id, e);
        }

        return null;
    }

    public boolean update(Event event) {
        if (event == null || event.getId() == null) {
            return false;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_EVENT)) {

            // Mettre à jour le timestamp
            event.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(event.getEventDate()));
            stmt.setString(4, event.getVenue());
            stmt.setString(5, event.getCity());
            stmt.setString(6, event.getCategory());
            stmt.setString(7, event.getStatus());
            stmt.setInt(8, event.getMaxParticipants());
            stmt.setInt(9, event.getTicketsSold() != null ? event.getTicketsSold() : 0);

            if (event.getTicketPrice() != null) {
                stmt.setDouble(10, event.getTicketPrice());
            } else {
                stmt.setNull(10, Types.DOUBLE);
            }

            stmt.setTimestamp(11, Timestamp.valueOf(event.getUpdatedAt()));
            stmt.setLong(12, event.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour", e);
            return false;
        }
    }

    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_EVENT)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression", e);
            return false;
        }
    }

    public int count() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_EVENTS);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du comptage", e);
            return 0;
        }
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getLong("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));

        Timestamp timestamp = rs.getTimestamp("event_date");
        if (timestamp != null) {
            event.setEventDate(timestamp.toLocalDateTime());
        }

        event.setVenue(rs.getString("venue"));
        event.setCity(rs.getString("city"));
        event.setCategory(rs.getString("category"));
        event.setStatus(rs.getString("status"));
        event.setMaxParticipants(rs.getInt("max_participants"));
        event.setTicketsSold(rs.getInt("tickets_sold"));

        Double ticketPrice = rs.getDouble("ticket_price");
        if (!rs.wasNull()) {
            event.setTicketPrice(ticketPrice);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            event.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            event.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return event;
    }

    private void setEventParameters(PreparedStatement stmt, Event event) throws SQLException {
        LOGGER.info("Paramètres de l'événement:");
        LOGGER.info("1. Title: " + event.getTitle());
        LOGGER.info("2. Description: " + event.getDescription());
        LOGGER.info("3. Event Date: " + event.getEventDate());
        LOGGER.info("4. Venue: " + event.getVenue());
        LOGGER.info("5. City: " + event.getCity());
        LOGGER.info("6. Category: " + event.getCategory());
        LOGGER.info("7. Status: " + event.getStatus());
        LOGGER.info("8. Max Participants: " + event.getMaxParticipants());
        LOGGER.info("9. Tickets Sold: " + event.getTicketsSold());
        LOGGER.info("10. Ticket Price: " + event.getTicketPrice());

        stmt.setString(1, event.getTitle());
        stmt.setString(2, event.getDescription());
        stmt.setTimestamp(3, Timestamp.valueOf(event.getEventDate()));
        stmt.setString(4, event.getVenue());
        stmt.setString(5, event.getCity());
        stmt.setString(6, event.getCategory());
        stmt.setString(7, event.getStatus());
        stmt.setInt(8, event.getMaxParticipants());
        stmt.setInt(9, event.getTicketsSold() != null ? event.getTicketsSold() : 0);

        if (event.getTicketPrice() != null) {
            stmt.setDouble(10, event.getTicketPrice());
        } else {
            stmt.setNull(10, Types.DOUBLE);
        }

        stmt.setTimestamp(11, Timestamp.valueOf(event.getCreatedAt()));
        stmt.setTimestamp(12, Timestamp.valueOf(event.getUpdatedAt()));
    }
}