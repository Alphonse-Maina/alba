package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.model.Transaction;
import com.kinyozi.pos.model.TransactionItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public Transaction saveTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (receipt_number, customer_id, served_by, subtotal, " +
                "discount_amount, tax_amount, total_amount, amount_paid, change_given, " +
                "payment_method, mpesa_code, status, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id";
        try (Connection c = DatabaseConfig.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, t.getReceiptNumber());
                ps.setObject(2, t.getCustomerId(), Types.INTEGER);
                ps.setObject(3, t.getServedBy(), Types.INTEGER);
                ps.setBigDecimal(4, t.getSubtotal());
                ps.setBigDecimal(5, t.getDiscountAmount());
                ps.setBigDecimal(6, t.getTaxAmount());
                ps.setBigDecimal(7, t.getTotalAmount());
                ps.setBigDecimal(8, t.getAmountPaid());
                ps.setBigDecimal(9, t.getChangeGiven());
                ps.setString(10, t.getPaymentMethod());
                ps.setString(11, t.getMpesaCode());
                ps.setString(12, t.getStatus());
                ps.setString(13, t.getNotes());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                    // Insert items
                    for (TransactionItem item : t.getItems()) {
                        saveTransactionItem(c, t.getId(), item);
                    }
                    c.commit();
                    return t;
                }
            } catch (Exception e) {
                c.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void saveTransactionItem(Connection c, int txId, TransactionItem item) throws SQLException {
        String sql = "INSERT INTO transaction_items (transaction_id, item_type, service_id, product_id, " +
                "item_name, quantity, unit_price, total_price, barber_id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, txId);
            ps.setString(2, item.getItemType());
            ps.setObject(3, item.getServiceId(), Types.INTEGER);
            ps.setObject(4, item.getProductId(), Types.INTEGER);
            ps.setString(5, item.getItemName());
            ps.setInt(6, item.getQuantity());
            ps.setBigDecimal(7, item.getUnitPrice());
            ps.setBigDecimal(8, item.getTotalPrice());
            ps.setObject(9, item.getBarberId(), Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public List<Transaction> getTodaysTransactions() {
        return getTransactionsByDate(LocalDate.now());
    }

    public List<Transaction> getTransactionsByDate(LocalDate date) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, u.full_name as barber_name, cu.full_name as customer_name " +
                "FROM transactions t " +
                "LEFT JOIN users u ON t.served_by = u.id " +
                "LEFT JOIN customers cu ON t.customer_id = cu.id " +
                "WHERE DATE(t.created_at) = ? ORDER BY t.created_at DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTransaction(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public Transaction getTransactionWithItems(int id) {
        String sql = "SELECT t.*, u.full_name as barber_name, cu.full_name as customer_name " +
                "FROM transactions t " +
                "LEFT JOIN users u ON t.served_by = u.id " +
                "LEFT JOIN customers cu ON t.customer_id = cu.id " +
                "WHERE t.id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Transaction t = mapTransaction(rs);
                t.setItems(getItemsByTransactionId(c, id));
                return t;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private List<TransactionItem> getItemsByTransactionId(Connection c, int txId) throws SQLException {
        List<TransactionItem> items = new ArrayList<>();
        String sql = "SELECT ti.*, u.full_name as barber_name FROM transaction_items ti " +
                "LEFT JOIN users u ON ti.barber_id = u.id WHERE ti.transaction_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, txId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TransactionItem item = new TransactionItem();
                item.setId(rs.getInt("id"));
                item.setTransactionId(txId);
                item.setItemType(rs.getString("item_type"));
                item.setServiceId((Integer) rs.getObject("service_id"));
                item.setProductId((Integer) rs.getObject("product_id"));
                item.setItemName(rs.getString("item_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));
                item.setBarberId((Integer) rs.getObject("barber_id"));
                item.setBarberName(rs.getString("barber_name"));
                items.add(item);
            }
        }
        return items;
    }

    public java.math.BigDecimal getTotalRevenueByDate(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM transactions WHERE DATE(created_at) = ? AND status = 'COMPLETED'";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (Exception e) { e.printStackTrace(); }
        return java.math.BigDecimal.ZERO;
    }

    public int getTransactionCountByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE DATE(created_at) = ? AND status = 'COMPLETED'";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Revenue by date range for reports
    public List<Object[]> getDailyRevenue(LocalDate from, LocalDate to) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT DATE(created_at) as sale_date, SUM(total_amount) as revenue, COUNT(*) as tx_count " +
                "FROM transactions WHERE DATE(created_at) BETWEEN ? AND ? AND status = 'COMPLETED' " +
                "GROUP BY DATE(created_at) ORDER BY sale_date";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{rs.getDate(1).toLocalDate(), rs.getBigDecimal(2), rs.getInt(3)});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // Top services report
    public List<Object[]> getTopServices(LocalDate from, LocalDate to) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT ti.item_name, SUM(ti.quantity) as qty, SUM(ti.total_price) as revenue " +
                "FROM transaction_items ti JOIN transactions t ON ti.transaction_id = t.id " +
                "WHERE ti.item_type = 'SERVICE' AND DATE(t.created_at) BETWEEN ? AND ? " +
                "GROUP BY ti.item_name ORDER BY revenue DESC LIMIT 10";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getBigDecimal(3)});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // Barber performance
    public List<Object[]> getBarberPerformance(LocalDate from, LocalDate to) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT u.full_name, COUNT(DISTINCT t.id) as tx_count, SUM(t.total_amount) as revenue " +
                "FROM transactions t JOIN users u ON t.served_by = u.id " +
                "WHERE DATE(t.created_at) BETWEEN ? AND ? AND t.status = 'COMPLETED' " +
                "GROUP BY u.id, u.full_name ORDER BY revenue DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getBigDecimal(3)});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setReceiptNumber(rs.getString("receipt_number"));
        t.setCustomerId((Integer) rs.getObject("customer_id"));
        t.setCustomerName(rs.getString("customer_name"));
        t.setServedBy((Integer) rs.getObject("served_by"));
        t.setBarberName(rs.getString("barber_name"));
        t.setSubtotal(rs.getBigDecimal("subtotal"));
        t.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        t.setTaxAmount(rs.getBigDecimal("tax_amount"));
        t.setTotalAmount(rs.getBigDecimal("total_amount"));
        t.setAmountPaid(rs.getBigDecimal("amount_paid"));
        t.setChangeGiven(rs.getBigDecimal("change_given"));
        t.setPaymentMethod(rs.getString("payment_method"));
        t.setMpesaCode(rs.getString("mpesa_code"));
        t.setStatus(rs.getString("status"));
        t.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }
}