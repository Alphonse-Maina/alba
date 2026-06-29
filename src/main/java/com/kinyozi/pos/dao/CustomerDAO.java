package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapCustomer(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Customer> searchCustomers(String query) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE LOWER(full_name) LIKE ? OR phone LIKE ? ORDER BY full_name";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String q = "%" + query.toLowerCase() + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCustomer(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCustomer(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public Customer saveCustomer(Customer cust) {
        if (cust.getId() == 0) {
            String sql = "INSERT INTO customers (full_name, phone, email, notes) VALUES (?,?,?,?) RETURNING id";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, cust.getFullName());
                ps.setString(2, cust.getPhone());
                ps.setString(3, cust.getEmail());
                ps.setString(4, cust.getNotes());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    cust.setId(rs.getInt(1));
                    return cust;
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            String sql = "UPDATE customers SET full_name=?, phone=?, email=?, notes=? WHERE id=?";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, cust.getFullName());
                ps.setString(2, cust.getPhone());
                ps.setString(3, cust.getEmail());
                ps.setString(4, cust.getNotes());
                ps.setInt(5, cust.getId());
                ps.executeUpdate();
                return cust;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    public boolean addLoyaltyPoints(int customerId, int points) {
        String sql = "UPDATE customers SET loyalty_points = loyalty_points + ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer cust = new Customer();
        cust.setId(rs.getInt("id"));
        cust.setFullName(rs.getString("full_name"));
        cust.setPhone(rs.getString("phone"));
        cust.setEmail(rs.getString("email"));
        cust.setLoyaltyPoints(rs.getInt("loyalty_points"));
        cust.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) cust.setCreatedAt(ts.toLocalDateTime());
        return cust;
    }
}