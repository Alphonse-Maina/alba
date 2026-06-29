package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.*;
import com.kinyozi.pos.model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentsPanel extends JPanel {
    private final AppointmentDAO apptDAO = new AppointmentDAO();
    private final CustomerDAO custDAO = new CustomerDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    private DefaultTableModel tableModel;
    private JTable table;
    private LocalDate viewDate = LocalDate.now();
    private JLabel dateLabel;

    private static final Color BG = new Color(22, 22, 36);
    private static final Color CARD = new Color(28, 28, 45);
    private static final Color ACCENT = new Color(255, 193, 7);
    private static final Color TEXT = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);

    public AppointmentsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        buildUI();
        loadAppointments();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel title = new JLabel("📅  Appointments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JPanel dateNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dateNav.setBackground(BG);
        JButton prevBtn = navBtn("◀");
        dateLabel = new JLabel(viewDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        dateLabel.setForeground(ACCENT);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton nextBtn = navBtn("▶");
        JButton todayBtn = navBtn("Today");
        JButton addBtn = accentBtn("+ New Appointment");

        prevBtn.addActionListener(e -> { viewDate = viewDate.minusDays(1); updateDateLabel(); loadAppointments(); });
        nextBtn.addActionListener(e -> { viewDate = viewDate.plusDays(1); updateDateLabel(); loadAppointments(); });
        todayBtn.addActionListener(e -> { viewDate = LocalDate.now(); updateDateLabel(); loadAppointments(); });
        addBtn.addActionListener(e -> showAddDialog());

        dateNav.add(prevBtn); dateNav.add(dateLabel); dateNav.add(nextBtn);
        dateNav.add(Box.createHorizontalStrut(12));
        dateNav.add(todayBtn); dateNav.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(dateNav, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Time", "Customer", "Phone", "Service", "Barber", "Status", "Actions"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        table = new JTable(tableModel);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(40, 40, 60));
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(30, 30, 50));
        table.getTableHeader().setForeground(MUTED);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(50, 50, 80));

        table.getColumnModel().getColumn(6).setCellRenderer(new StatusActionsRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new StatusActionsEditor(table));
        table.getColumnModel().getColumn(6).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 65)));
        scroll.setBackground(CARD);
        scroll.getViewport().setBackground(CARD);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        List<Appointment> appts = apptDAO.getAppointmentsByDate(viewDate);
        for (Appointment a : appts) {
            tableModel.addRow(new Object[]{
                    a.getAppointmentTime() != null ? a.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "--",
                    a.getCustomerName() != null ? a.getCustomerName() : "Walk-in",
                    a.getCustomerPhone() != null ? a.getCustomerPhone() : "",
                    a.getServiceName() != null ? a.getServiceName() : "",
                    a.getBarberName() != null ? a.getBarberName() : "",
                    a.getStatus(),
                    a
            });
        }
    }

    private void updateDateLabel() {
        dateLabel.setText(viewDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Appointment", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField custField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<User> barberBox = new JComboBox<>();
        JComboBox<Service> serviceBox = new JComboBox<>();
        JTextField dateField = new JTextField(viewDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField timeField = new JTextField("09:00");
        JTextArea notesField = new JTextArea(2, 20);

        barberBox.addItem(null);
        userDAO.getActiveStaff().forEach(barberBox::addItem);
        serviceBox.addItem(null);
        serviceDAO.getAllActiveServices().forEach(serviceBox::addItem);

        styleFormField(custField); styleFormField(phoneField);
        styleFormField(dateField); styleFormField(timeField);
        notesField.setBackground(new Color(35, 35, 55));
        notesField.setForeground(TEXT);
        barberBox.setBackground(new Color(35, 35, 55)); barberBox.setForeground(TEXT);
        serviceBox.setBackground(new Color(35, 35, 55)); serviceBox.setForeground(TEXT);

        form.add(label("Customer Name:")); form.add(custField);
        form.add(label("Phone:")); form.add(phoneField);
        form.add(label("Barber:")); form.add(barberBox);
        form.add(label("Service:")); form.add(serviceBox);
        form.add(label("Date (dd/MM/yyyy):")); form.add(dateField);
        form.add(label("Time (HH:mm):")); form.add(timeField);
        form.add(label("Notes:")); form.add(new JScrollPane(notesField));

        JButton saveBtn = accentBtn("Save Appointment");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e -> {
            try {
                Appointment a = new Appointment();
                String custName = custField.getText().trim();
                if (!custName.isEmpty()) {
                    Customer c = new Customer(custName, phoneField.getText().trim());
                    c = custDAO.saveCustomer(c);
                    if (c != null) a.setCustomerId(c.getId());
                    a.setCustomerName(custName);
                }
                if (barberBox.getSelectedItem() != null)
                    a.setBarberId(((User) barberBox.getSelectedItem()).getId());
                if (serviceBox.getSelectedItem() != null)
                    a.setServiceId(((Service) serviceBox.getSelectedItem()).getId());
                a.setAppointmentDate(LocalDate.parse(dateField.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                a.setAppointmentTime(LocalTime.parse(timeField.getText().trim()));
                a.setStatus("SCHEDULED");
                a.setNotes(notesField.getText().trim());
                apptDAO.saveAppointment(a);
                loadAppointments();
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(CARD);
        btnPanel.add(cancelBtn); btnPanel.add(saveBtn);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void styleFormField(JTextField f) {
        f.setBackground(new Color(35, 35, 55));
        f.setForeground(TEXT);
        f.setCaretColor(Color.WHITE);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(40, 40, 60));
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton accentBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(new Color(20, 20, 20));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Status cell renderer
    class StatusActionsRenderer extends JPanel implements TableCellRenderer {
        public StatusActionsRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
            setBackground(sel ? new Color(50, 50, 80) : CARD);
            removeAll();
            if (val instanceof Appointment a) {
                JLabel lbl = new JLabel(a.getStatusDisplayText());
                lbl.setForeground(statusColor(a.getStatus()));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                add(lbl);
            }
            return this;
        }
        private Color statusColor(String s) {
            return switch (s) {
                case "SCHEDULED" -> new Color(100, 160, 255);
                case "IN_PROGRESS" -> ACCENT;
                case "COMPLETED" -> new Color(76, 175, 80);
                case "CANCELLED", "NO_SHOW" -> new Color(229, 57, 53);
                default -> TEXT;
            };
        }
    }

    class StatusActionsEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        private JTable parentTable;
        public StatusActionsEditor(JTable t) {
            super(new JCheckBox());
            parentTable = t;
            panel.setBackground(CARD);
        }
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            panel.removeAll();
            if (val instanceof Appointment a) {
                for (String[] status : new String[][]{{"Start", "IN_PROGRESS"}, {"Done", "COMPLETED"}, {"Cancel", "CANCELLED"}}) {
                    JButton btn = new JButton(status[0]);
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    btn.setBackground(new Color(40, 40, 65));
                    btn.setForeground(TEXT);
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    final String newStatus = status[1];
                    btn.addActionListener(e -> {
                        apptDAO.updateStatus(a.getId(), newStatus);
                        fireEditingStopped();
                        loadAppointments();
                    });
                    panel.add(btn);
                }
            }
            return panel;
        }
        public Object getCellEditorValue() { return null; }
    }
}