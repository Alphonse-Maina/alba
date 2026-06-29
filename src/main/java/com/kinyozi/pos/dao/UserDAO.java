package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, hash)) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(mapUser(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return users;
    }

    public List<User> getActiveStaff() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = TRUE ORDER BY full_name";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(mapUser(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return users;
    }

    public boolean createUser(User user, String plainPassword) {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        String sql = "INSERT INTO users (username, password_hash, full_name, role, phone, email) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, hash);
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getEmail());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name=?, role=?, phone=?, email=?, is_active=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getEmail());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean changePassword(int userId, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setPhone(rs.getString("phone"));
        u.setEmail(rs.getString("email"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}