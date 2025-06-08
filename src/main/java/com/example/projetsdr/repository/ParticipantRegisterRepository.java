package com.example.projetsdr.repository;

import com.example.projetsdr.model.Participant;

import javax.sql.DataSource;
import java.sql.*;

public class ParticipantRegisterRepository {
    private final DataSource dataSource;

    public ParticipantRegisterRepository(DataSource dataSource) {
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

    public boolean save(Participant participant) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO participants (nom, email, mot_de_passe) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, participant.getNom());
            ps.setString(2, participant.getEmail());
            ps.setString(3, participant.getPassword());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}