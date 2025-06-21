package com.example.projetsdr.repository;

import com.example.projetsdr.model.Event;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class EventAddRepository {
    private static final Logger LOGGER = Logger.getLogger(EventAddRepository.class.getName());

    @Resource(lookup = "java:/MySqldms_db") // Adjust JNDI name as needed
    private DataSource dataSource;

    // Default constructor for CDI
    public EventAddRepository() {
    }

    // Keep existing constructor for manual instantiation if needed
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
                    "category = ?, status = ?, max_participants = ?, ticket_price = ?, updated_at = ? " +
                    "WHERE id = ?";

    private static final String DELETE_EVENT =
            "DELETE FROM events WHERE id = ?";

    private static final String COUNT_EVENTS =
            "SELECT COUNT(*) FROM events";

    public boolean save(Event event) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_EVENT, Statement.RETURN_GENERATED_KEYS)) {

            setEventParameters(stmt, event, false);
            stmt.setInt(9, event.getTicketsSold() != null ? event.getTicketsSold() : 0);
            stmt.setTimestamp(11, Timestamp.valueOf(event.getCreatedAt()));
            stmt.setTimestamp(12, Timestamp.valueOf(event.getUpdatedAt()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        event.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la sauvegarde", e);
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

            setEventParameters(stmt, event, true);
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(11, event.getId());

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

        // Gestion sécurisée des timestamps null
        Timestamp eventDateTs = rs.getTimestamp("event_date");
        if (eventDateTs != null) {
            event.setEventDate(eventDateTs.toLocalDateTime());
        }

        event.setVenue(rs.getString("venue"));
        event.setCity(rs.getString("city"));
        event.setCategory(rs.getString("category")); // String au lieu d'enum
        event.setStatus(rs.getString("status")); // String au lieu d'enum

        // Gestion des valeurs nullables
        Integer maxParticipants = rs.getObject("max_participants", Integer.class);
        event.setMaxParticipants(maxParticipants);

        Integer ticketsSold = rs.getObject("tickets_sold", Integer.class);
        event.setTicketsSold(ticketsSold != null ? ticketsSold : 0);

        Double ticketPrice = rs.getObject("ticket_price", Double.class);
        event.setTicketPrice(ticketPrice);

        // Gestion sécurisée des timestamps
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            event.setCreatedAt(createdAtTs.toLocalDateTime());
        }

        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            event.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }

        return event;
    }

    private void setEventParameters(PreparedStatement stmt, Event event, boolean isUpdate) throws SQLException {
        int index = 1;
        stmt.setString(index++, event.getTitle());
        stmt.setString(index++, event.getDescription());
        stmt.setTimestamp(index++, Timestamp.valueOf(event.getEventDate()));
        stmt.setString(index++, event.getVenue());
        stmt.setString(index++, event.getCity());
        stmt.setString(index++, event.getCategory()); // String au lieu d'enum.name()
        stmt.setString(index++, event.getStatus()); // String au lieu d'enum.name()
        stmt.setObject(index++, event.getMaxParticipants(), Types.INTEGER);

        if (!isUpdate) {
            // Pour l'insertion, on inclut tickets_sold
            stmt.setObject(index++, event.getTicketsSold(), Types.INTEGER);
        }

        stmt.setObject(index++, event.getTicketPrice(), Types.DOUBLE);
    }
}