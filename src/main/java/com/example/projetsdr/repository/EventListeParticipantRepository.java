package com.example.projetsdr.repository;

import com.example.projetsdr.model.EventParticipation;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventListeParticipantRepository {

    private final DataSource dataSource;

    public EventListeParticipantRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<EventParticipation> findAll() {
        List<EventParticipation> list = new ArrayList<>();
        String sql = "SELECT * FROM event_participations";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de lecture", e);
        }
        return list;
    }

    public List<EventParticipation> findWithFilters(Long eventId, String email, String status) {
        List<EventParticipation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM event_participations WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (eventId != null) {
            sql.append(" AND event_id = ?");
            params.add(eventId);
        }
        if (email != null && !email.isEmpty()) {
            sql.append(" AND participant_email LIKE ?");
            params.add("%" + email + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de filtrage", e);
        }
        return list;
    }

    private EventParticipation mapRow(ResultSet rs) throws SQLException {
        EventParticipation p = new EventParticipation();
        p.setId(rs.getLong("id"));
        p.setEventId(rs.getLong("event_id"));
        p.setParticipantEmail(rs.getString("participant_email"));
        p.setStatus(rs.getString("status"));
        return p;
    }

    public boolean save(EventParticipation p) {
        String sql = "UPDATE event_participations SET participant_email = ?, status = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getParticipantEmail());
            stmt.setString(2, p.getStatus());
            stmt.setLong(3, p.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de sauvegarde", e);
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM event_participations WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de suppression", e);
        }
    }

    /**
     * Nouvelle méthode pour supprimer tous les participants
     */
    public int deleteAll() {
        String sql = "DELETE FROM event_participations";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de tous les participants", e);
        }
    }

    /**
     * Méthode pour compter le nombre total de participants
     */
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM event_participations";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des participants", e);
        }
    }
}