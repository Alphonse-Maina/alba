package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.UserDAO;
import com.kinyozi.pos.model.User;
import com.kinyozi.pos.util.SessionManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class StaffPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    private static final Color BG    = new Color(22, 22, 36);
    private static final Color CARD  = new Color(28, 28, 45);
    private static final Color ACCENT= new Color(255, 193, 7);
    private static final Color TEXT  = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);
    private static final Color GREEN = new Color(76, 175, 80);
    private static final Color RED   = new Color(229, 57, 53);

    public StaffPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        load();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel title = new JLabel("👤  Staff Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JButton addBtn = accentBtn("+ Add Staff Member");
        addBtn.addActionListener(e -> showDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Full Name", "Username", "Role", "Phone", "Email", "Status", "Actions"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(tableModel);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(38, 38, 58));
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(30, 30, 50));
        table.getTableHeader().setForeground(MUTED);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(50, 50, 80));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setMaxWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(220);
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionsEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void load() {
        tableModel.setRowCount(0);
        for (User u : userDAO.getAllUsers()) {
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getFullName(),
                    u.getUsername(),
                    u.getRole(),
                    u.getPhone() != null ? u.getPhone() : "",
                    u.getEmail() != null ? u.getEmail() : "",
                    u.isActive() ? "✓ Active" : "✗ Inactive",
                    u
            });
        }
    }

    private void showDialog(User existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Staff Member" : "Add Staff Member", true);
        dlg.setSize(430, isEdit ? 420 : 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(CARD);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JTextField fullNameF = field(isEdit ? existing.getFullName() : "");
        JTextField usernameF = field(isEdit ? existing.getUsername() : "");
        usernameF.setEditable(!isEdit); // username locked after creation
        JPasswordField passF = new JPasswordField();
        passF.setBackground(new Color(35,35,55)); passF.setForeground(TEXT); passF.setCaretColor(Color.WHITE);

        String[] roles = {"ADMIN", "MANAGER", "BARBER", "RECEPTIONIST"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setBackground(new Color(35,35,55)); roleBox.setForeground(TEXT);
        if (isEdit) roleBox.setSelectedItem(existing.getRole());

        JTextField phoneF = field(isEdit && existing.getPhone()!=null ? existing.getPhone() : "");
        JTextField emailF = field(isEdit && existing.getEmail()!=null ? existing.getEmail() : "");

        JCheckBox activeBox = new JCheckBox();
        activeBox.setBackground(CARD);
        activeBox.setSelected(!isEdit || existing.isActive());

        form.add(lbl("Full Name *")); form.add(fullNameF);
        form.add(lbl("Username *"));  form.add(usernameF);
        if (!isEdit) { form.add(lbl("Password *")); form.add(passF); }
        form.add(lbl("Role"));        form.add(roleBox);
        form.add(lbl("Phone"));       form.add(phoneF);
        form.add(lbl("Email"));       form.add(emailF);
        if (isEdit) { form.add(lbl("Active")); form.add(activeBox); }

        JButton saveBtn = accentBtn(isEdit ? "Update" : "Create Staff");
        JButton cancelBtn = grayBtn("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String name = fullNameF.getText().trim();
            String uname = usernameF.getText().trim();
            if (name.isEmpty() || uname.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and username are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (isEdit) {
                existing.setFullName(name);
                existing.setRole((String) roleBox.getSelectedItem());
                existing.setPhone(phoneF.getText().trim());
                existing.setEmail(emailF.getText().trim());
                existing.setActive(activeBox.isSelected());
                if (userDAO.updateUser(existing)) { load(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Failed to update staff member.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                String pass = new String(passF.getPassword());
                if (pass.length() < 4) {
                    JOptionPane.showMessageDialog(dlg, "Password must be at least 4 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                User u = new User();
                u.setFullName(name);
                u.setUsername(uname);
                u.setRole((String) roleBox.getSelectedItem());
                u.setPhone(phoneF.getText().trim());
                u.setEmail(emailF.getText().trim());
                if (userDAO.createUser(u, pass)) { load(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Failed to create staff member (username may already exist).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(CARD);
        btnP.add(cancelBtn); btnP.add(saveBtn);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showResetPasswordDialog(User u) {
        JPasswordField newPassF = new JPasswordField();
        Object[] fields = {"New password for " + u.getFullName() + ":", newPassF};
        int res = JOptionPane.showConfirmDialog(this, fields, "Reset Password", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String pass = new String(newPassF.getPassword());
            if (pass.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (userDAO.changePassword(u.getId(), pass)) {
                JOptionPane.showMessageDialog(this, "Password reset successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---- helpers ----
    private JTextField field(String v) {
        JTextField f = new JTextField(v);
        f.setBackground(new Color(35,35,55)); f.setForeground(TEXT); f.setCaretColor(Color.WHITE); return f;
    }
    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setForeground(MUTED); l.setFont(new Font("Segoe UI", Font.PLAIN, 12)); return l;
    }
    private JButton accentBtn(String t) {
        JButton b = new JButton(t); b.setBackground(ACCENT); b.setForeground(new Color(20,20,20));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton grayBtn(String t) {
        JButton b = new JButton(t); b.setBackground(new Color(50,50,70)); b.setForeground(TEXT);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton miniBtn(String t, Color bg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    class ActionsRenderer extends JPanel implements TableCellRenderer {
        public ActionsRenderer() { setOpaque(true); setLayout(new FlowLayout(FlowLayout.LEFT, 4, 6)); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? new Color(50,50,80) : CARD);
            removeAll();
            add(miniBtn("✏ Edit", new Color(33,150,243)));
            add(miniBtn("🔑 Reset PW", new Color(255,152,0)));
            return this;
        }
    }

    class ActionsEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        public ActionsEditor() { super(new JCheckBox()); panel.setBackground(CARD); }
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            panel.removeAll();
            if (val instanceof User u) {
                JButton editBtn  = miniBtn("✏ Edit", new Color(33,150,243));
                JButton resetBtn = miniBtn("🔑 Reset PW", new Color(255,152,0));
                editBtn.addActionListener(e  -> { fireEditingStopped(); showDialog(u); });
                resetBtn.addActionListener(e -> { fireEditingStopped(); showResetPasswordDialog(u); });
                panel.add(editBtn);
                // Don't allow resetting own password or other admin's password here for safety, but keep simple
                panel.add(resetBtn);
            }
            return panel;
        }
        public Object getCellEditorValue() { return null; }
    }
}