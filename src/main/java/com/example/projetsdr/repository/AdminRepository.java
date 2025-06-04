package com.example.projetsdr.repository;


import com.example.projetsdr.model.Admin;

import javax.sql.DataSource;
import java.sql.*;

public class AdminRepository {
    private final DataSource dataSource;

    public AdminRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Admin findByEmailAndPassword(String email, String password) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, email, password FROM admins WHERE email = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setEmail(rs.getString("email"));
                admin.setPassword(rs.getString("password"));
                return admin;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
