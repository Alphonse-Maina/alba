package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.CustomerDAO;
import com.kinyozi.pos.model.Customer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class CustomersPanel extends JPanel {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private List<Customer> allCustomers;

    private static final Color BG = new Color(22, 22, 36);
    private static final Color CARD = new Color(28, 28, 45);
    private static final Color ACCENT = new Color(255, 193, 7);
    private static final Color TEXT = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);
    private static final Color BTN_GREEN = new Color(76, 175, 80);

    public CustomersPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        loadCustomers();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel title = new JLabel("👥  Customers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setBackground(BG);

        searchField = new JTextField(18);
        searchField.setBackground(new Color(35, 35, 55));
        searchField.setForeground(TEXT);
        searchField.setCaretColor(Color.WHITE);
        searchField.setToolTipText("Search by name or phone");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JButton addBtn = accentBtn("+ Add Customer");
        addBtn.addActionListener(e -> showCustomerDialog(null));

        rightBar.add(new JLabel("🔍") {{ setForeground(MUTED); }});
        rightBar.add(searchField);
        rightBar.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(rightBar, BorderLayout.EAST);

        // Stats bar
        JPanel stats = buildStatsBar();

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Phone", "Email", "Loyalty Points", "Joined", "Actions"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionsEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);

        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(BG);
        north.add(header, BorderLayout.NORTH);
        north.add(stats, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        p.setBackground(new Color(25, 25, 40));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(40, 40, 60)));
        // Will be populated after load
        return p;
    }

    private void loadCustomers() {
        allCustomers = customerDAO.getAllCustomers();
        populateTable(allCustomers);
    }

    private void populateTable(List<Customer> customers) {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getFullName(),
                    c.getPhone() != null ? c.getPhone() : "",
                    c.getEmail() != null ? c.getEmail() : "",
                    c.getLoyaltyPoints(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate().toString() : "",
                    c
            });
        }
    }

    private void filterTable() {
        if (allCustomers == null) return;
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) { populateTable(allCustomers); return; }
        List<Customer> filtered = allCustomers.stream()
                .filter(c -> c.getFullName().toLowerCase().contains(q)
                        || (c.getPhone() != null && c.getPhone().contains(q))
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)))
                .toList();
        populateTable(filtered);
    }

    private void showCustomerDialog(Customer existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Customer" : "Add Customer", true);
        dlg.setSize(400, 360);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JTextField nameF = styledField(isEdit ? existing.getFullName() : "");
        JTextField phoneF = styledField(isEdit && existing.getPhone() != null ? existing.getPhone() : "");
        JTextField emailF = styledField(isEdit && existing.getEmail() != null ? existing.getEmail() : "");
        JTextArea notesF = new JTextArea(isEdit && existing.getNotes() != null ? existing.getNotes() : "", 3, 20);
        notesF.setBackground(new Color(35, 35, 55));
        notesF.setForeground(TEXT);
        notesF.setCaretColor(Color.WHITE);

        form.add(label("Full Name *")); form.add(nameF);
        form.add(label("Phone")); form.add(phoneF);
        form.add(label("Email")); form.add(emailF);
        form.add(label("Notes")); form.add(new JScrollPane(notesF));

        JButton saveBtn = accentBtn(isEdit ? "Update" : "Save Customer");
        JButton cancelBtn = grayBtn("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Customer c = isEdit ? existing : new Customer();
            c.setFullName(name);
            c.setPhone(phoneF.getText().trim());
            c.setEmail(emailF.getText().trim());
            c.setNotes(notesF.getText().trim());
            Customer saved = customerDAO.saveCustomer(c);
            if (saved != null) {
                loadCustomers();
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to save customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(CARD);
        btnP.add(cancelBtn); btnP.add(saveBtn);

        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(CARD);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void styleTable(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TEXT);
        t.setGridColor(new Color(38, 38, 58));
        t.setRowHeight(40);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setBackground(new Color(30, 30, 50));
        t.getTableHeader().setForeground(MUTED);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(50, 50, 80));
    }

    private JTextField styledField(String text) {
        JTextField f = new JTextField(text);
        f.setBackground(new Color(35, 35, 55));
        f.setForeground(TEXT);
        f.setCaretColor(Color.WHITE);
        return f;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
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

    private JButton grayBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(50, 50, 70));
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    class ActionsRenderer extends JPanel implements TableCellRenderer {
        public ActionsRenderer() { setOpaque(true); setLayout(new FlowLayout(FlowLayout.LEFT, 4, 6)); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? new Color(50, 50, 80) : CARD);
            removeAll();
            JButton edit = miniBtn("✏ Edit", new Color(33, 150, 243));
            add(edit);
            return this;
        }
    }

    class ActionsEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        public ActionsEditor() {
            super(new JCheckBox());
            panel.setBackground(CARD);
        }
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            panel.removeAll();
            if (val instanceof Customer c) {
                JButton editBtn = miniBtn("✏ Edit", new Color(33, 150, 243));
                editBtn.addActionListener(e -> { fireEditingStopped(); showCustomerDialog(c); });
                panel.add(editBtn);
            }
            return panel;
        }
        public Object getCellEditorValue() { return null; }
    }

    private JButton miniBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}