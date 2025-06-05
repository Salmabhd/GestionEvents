package com.example.projetsdr.repository;

import com.example.projetsdr.model.Participant;

import javax.sql.DataSource;
import java.sql.*;

public class ParticipantRepository {
    private final DataSource dataSource;

    public ParticipantRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Participant findByEmailAndPassword(String email, String password) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, nom, email, mot_de_passe FROM participants WHERE email = ? AND mot_de_passe = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Participant participant = new Participant();
                participant.setId(rs.getInt("id"));
                participant.setNom(rs.getString("nom"));
                participant.setEmail(rs.getString("email"));
                participant.setPassword(rs.getString("mot_de_passe"));
                return participant;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}