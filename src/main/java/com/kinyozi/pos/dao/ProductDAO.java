package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE ORDER BY name";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapProduct(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE AND stock_quantity <= reorder_level ORDER BY stock_quantity";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapProduct(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean saveProduct(Product p) {
        if (p.getId() == 0) {
            String sql = "INSERT INTO products (name, description, barcode, selling_price, cost_price, stock_quantity, reorder_level) VALUES (?,?,?,?,?,?,?)";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getName());
                ps.setString(2, p.getDescription());
                ps.setString(3, p.getBarcode());
                ps.setBigDecimal(4, p.getSellingPrice());
                ps.setBigDecimal(5, p.getCostPrice());
                ps.setInt(6, p.getStockQuantity());
                ps.setInt(7, p.getReorderLevel());
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        } else {
            String sql = "UPDATE products SET name=?, description=?, barcode=?, selling_price=?, cost_price=?, stock_quantity=?, reorder_level=?, is_active=? WHERE id=?";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getName());
                ps.setString(2, p.getDescription());
                ps.setString(3, p.getBarcode());
                ps.setBigDecimal(4, p.getSellingPrice());
                ps.setBigDecimal(5, p.getCostPrice());
                ps.setInt(6, p.getStockQuantity());
                ps.setInt(7, p.getReorderLevel());
                ps.setBoolean(8, p.isActive());
                ps.setInt(9, p.getId());
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); return false; }
        }
    }

    public boolean adjustStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean deductStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setBarcode(rs.getString("barcode"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setActive(rs.getBoolean("is_active"));
        return p;
    }
}