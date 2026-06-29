package com.kinyozi.pos.dao;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.model.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name as customer_name, c.phone as customer_phone, " +
                "u.full_name as barber_name, s.name as service_name " +
                "FROM appointments a " +
                "LEFT JOIN customers c ON a.customer_id = c.id " +
                "LEFT JOIN users u ON a.barber_id = u.id " +
                "LEFT JOIN services s ON a.service_id = s.id " +
                "WHERE a.appointment_date = ? ORDER BY a.appointment_time";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAppointment(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> getTodaysAppointments() {
        return getAppointmentsByDate(LocalDate.now());
    }

    public Appointment saveAppointment(Appointment a) {
        if (a.getId() == 0) {
            String sql = "INSERT INTO appointments (customer_id, barber_id, service_id, appointment_date, appointment_time, status, notes) " +
                    "VALUES (?,?,?,?,?,?,?) RETURNING id";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setObject(1, a.getCustomerId(), Types.INTEGER);
                ps.setObject(2, a.getBarberId(), Types.INTEGER);
                ps.setObject(3, a.getServiceId(), Types.INTEGER);
                ps.setDate(4, Date.valueOf(a.getAppointmentDate()));
                ps.setTime(5, Time.valueOf(a.getAppointmentTime()));
                ps.setString(6, a.getStatus() != null ? a.getStatus() : "SCHEDULED");
                ps.setString(7, a.getNotes());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { a.setId(rs.getInt(1)); return a; }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            String sql = "UPDATE appointments SET customer_id=?, barber_id=?, service_id=?, appointment_date=?, " +
                    "appointment_time=?, status=?, notes=? WHERE id=?";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setObject(1, a.getCustomerId(), Types.INTEGER);
                ps.setObject(2, a.getBarberId(), Types.INTEGER);
                ps.setObject(3, a.getServiceId(), Types.INTEGER);
                ps.setDate(4, Date.valueOf(a.getAppointmentDate()));
                ps.setTime(5, Time.valueOf(a.getAppointmentTime()));
                ps.setString(6, a.getStatus());
                ps.setString(7, a.getNotes());
                ps.setInt(8, a.getId());
                ps.executeUpdate();
                return a;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE appointments SET status=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setCustomerId((Integer) rs.getObject("customer_id"));
        a.setCustomerName(rs.getString("customer_name"));
        a.setCustomerPhone(rs.getString("customer_phone"));
        a.setBarberId((Integer) rs.getObject("barber_id"));
        a.setBarberName(rs.getString("barber_name"));
        a.setServiceId((Integer) rs.getObject("service_id"));
        a.setServiceName(rs.getString("service_name"));
        Date d = rs.getDate("appointment_date");
        if (d != null) a.setAppointmentDate(d.toLocalDate());
        Time t = rs.getTime("appointment_time");
        if (t != null) a.setAppointmentTime(t.toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }
}