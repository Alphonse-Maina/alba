package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> getAllActiveServices() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT s.*, sc.name as category_name FROM services s " +
                "JOIN service_categories sc ON s.category_id = sc.id " +
                "WHERE s.is_active = TRUE ORDER BY sc.name, s.name";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapService(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Service> getAllServices() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT s.*, sc.name as category_name FROM services s " +
                "JOIN service_categories sc ON s.category_id = sc.id ORDER BY sc.name, s.name";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapService(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean saveService(Service s) {
        if (s.getId() == 0) {
            String sql = "INSERT INTO services (category_id, name, description, price, duration_minutes) VALUES (?,?,?,?,?)";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, s.getCategoryId());
                ps.setString(2, s.getName());
                ps.setString(3, s.getDescription());
                ps.setBigDecimal(4, s.getPrice());
                ps.setInt(5, s.getDurationMinutes());
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        } else {
            String sql = "UPDATE services SET category_id=?, name=?, description=?, price=?, duration_minutes=?, is_active=? WHERE id=?";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, s.getCategoryId());
                ps.setString(2, s.getName());
                ps.setString(3, s.getDescription());
                ps.setBigDecimal(4, s.getPrice());
                ps.setInt(5, s.getDurationMinutes());
                ps.setBoolean(6, s.isActive());
                ps.setInt(7, s.getId());
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        }
    }

    public boolean deleteService(int id) {
        String sql = "UPDATE services SET is_active = FALSE WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private Service mapService(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("id"));
        s.setCategoryId(rs.getInt("category_id"));
        s.setCategoryName(rs.getString("category_name"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setPrice(rs.getBigDecimal("price"));
        s.setDurationMinutes(rs.getInt("duration_minutes"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}