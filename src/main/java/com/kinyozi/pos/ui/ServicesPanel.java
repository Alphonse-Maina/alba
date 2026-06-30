package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.ServiceDAO;
import com.kinyozi.pos.model.Service;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ServicesPanel extends JPanel {
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    private static final Color BG    = new Color(22, 22, 36);
    private static final Color CARD  = new Color(28, 28, 45);
    private static final Color ACCENT= new Color(255, 193, 7);
    private static final Color TEXT  = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);

    public ServicesPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        load();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel title = new JLabel("✂  Services");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JButton addBtn = accentBtn("+ Add Service");
        addBtn.addActionListener(e -> showDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Category", "Service Name", "Price (KES)", "Duration (min)", "Active", "Actions"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        table = new JTable(tableModel);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(38, 38, 58));
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(30, 30, 50));
        table.getTableHeader().setForeground(MUTED);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(50, 50, 80));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionsEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void load() {
        tableModel.setRowCount(0);
        for (Service s : serviceDAO.getAllServices()) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getCategoryName(),
                    s.getName(),
                    s.getPrice().toPlainString(),
                    s.getDurationMinutes(),
                    s.isActive() ? "✓" : "✗",
                    s
            });
        }
    }

    private void showDialog(Service existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Service" : "Add Service", true);
        dlg.setSize(420, 340);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(CARD);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Category combo - get from DB
        String[] cats = {"Haircut", "Shaving", "Hair Treatment", "Spa Services", "Nail Care"};
        JComboBox<String> catBox = new JComboBox<>(cats);
        catBox.setBackground(new Color(35, 35, 55));
        catBox.setForeground(TEXT);
        if (isEdit && existing.getCategoryName() != null) catBox.setSelectedItem(existing.getCategoryName());

        JTextField nameF = field(isEdit ? existing.getName() : "");
        JTextField priceF = field(isEdit ? existing.getPrice().toPlainString() : "");
        JTextField durF = field(isEdit ? String.valueOf(existing.getDurationMinutes()) : "30");
        JTextArea descF = new JTextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "", 2, 10);
        descF.setBackground(new Color(35, 35, 55)); descF.setForeground(TEXT); descF.setCaretColor(Color.WHITE);
        JCheckBox activeBox = new JCheckBox();
        activeBox.setBackground(CARD);
        activeBox.setSelected(!isEdit || existing.isActive());

        form.add(lbl("Category:")); form.add(catBox);
        form.add(lbl("Service Name *")); form.add(nameF);
        form.add(lbl("Price (KES) *")); form.add(priceF);
        form.add(lbl("Duration (minutes)")); form.add(durF);
        form.add(lbl("Description")); form.add(new JScrollPane(descF));
        form.add(lbl("Active")); form.add(activeBox);

        JButton saveBtn = accentBtn(isEdit ? "Update" : "Save");
        JButton cancelBtn = grayBtn("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String priceStr = priceF.getText().trim();
            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and price are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Service s = isEdit ? existing : new Service();
                s.setCategoryId(catBox.getSelectedIndex() + 1);
                s.setCategoryName((String) catBox.getSelectedItem());
                s.setName(name);
                s.setPrice(new BigDecimal(priceStr));
                s.setDurationMinutes(Integer.parseInt(durF.getText().trim().isEmpty() ? "30" : durF.getText().trim()));
                s.setDescription(descF.getText().trim());
                s.setActive(activeBox.isSelected());
                if (serviceDAO.saveService(s)) { load(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Failed to save service.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid price or duration.", "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(CARD);
        btnP.add(cancelBtn); btnP.add(saveBtn);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ---- helpers ----
    private JTextField field(String val) {
        JTextField f = new JTextField(val);
        f.setBackground(new Color(35, 35, 55));
        f.setForeground(TEXT); f.setCaretColor(Color.WHITE);
        return f;
    }
    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); return l;
    }
    private JButton accentBtn(String t) {
        JButton b = new JButton(t); b.setBackground(ACCENT);
        b.setForeground(new Color(20,20,20)); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton grayBtn(String t) {
        JButton b = new JButton(t); b.setBackground(new Color(50,50,70));
        b.setForeground(TEXT); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton miniBtn(String t, Color bg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    class ActionsRenderer extends JPanel implements TableCellRenderer {
        public ActionsRenderer() { setOpaque(true); setLayout(new FlowLayout(FlowLayout.LEFT, 4, 6)); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? new Color(50,50,80) : CARD);
            removeAll();
            add(miniBtn("✏ Edit", new Color(33,150,243)));
            add(miniBtn("🗑 Delete", new Color(229,57,53)));
            return this;
        }
    }

    class ActionsEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        public ActionsEditor() { super(new JCheckBox()); panel.setBackground(CARD); }
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            panel.removeAll();
            if (val instanceof Service s) {
                JButton editBtn = miniBtn("✏ Edit", new Color(33,150,243));
                JButton delBtn  = miniBtn("🗑 Delete", new Color(229,57,53));
                editBtn.addActionListener(e -> { fireEditingStopped(); showDialog(s); });
                delBtn.addActionListener(e -> {
                    int ok = JOptionPane.showConfirmDialog(ServicesPanel.this,
                            "Deactivate service: " + s.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (ok == JOptionPane.YES_OPTION) { serviceDAO.deleteService(s.getId()); fireEditingStopped(); load(); }
                });
                panel.add(editBtn); panel.add(delBtn);
            }
            return panel;
        }
        public Object getCellEditorValue() { return null; }
    }
}