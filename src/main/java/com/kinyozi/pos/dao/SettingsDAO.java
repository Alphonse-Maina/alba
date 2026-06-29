package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;

import java.sql.*;

public class SettingsDAO {

    public String get(String key, String defaultValue) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (Exception e) { /* ignore */ }
        return defaultValue;
    }

    public boolean set(String key, String value) {
        String sql = "INSERT INTO settings (key, value) VALUES (?,?) ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}