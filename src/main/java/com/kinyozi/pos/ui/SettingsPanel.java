package com.kinyozi.pos.ui;

import com.kinyozi.pos.config.DatabaseConfig;
import com.kinyozi.pos.dao.SettingsDAO;
import com.kinyozi.pos.util.SessionManager;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final SettingsDAO settingsDAO = new SettingsDAO();

    private JTextField shopNameF, shopAddrF, shopPhoneF, footerF, taxRateF, loyaltyRateF, currencyF, mpesaF;
    private JTextField dbHostF, dbPortF, dbNameF, dbUserF;
    private JPasswordField dbPassF;
    private JLabel dbStatusLabel;

    private static final Color BG    = new Color(22, 22, 36);
    private static final Color CARD  = new Color(28, 28, 45);
    private static final Color ACCENT= new Color(255, 193, 7);
    private static final Color TEXT  = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);
    private static final Color GREEN = new Color(76, 175, 80);
    private static final Color RED   = new Color(229, 57, 53);

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        loadSettings();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        JLabel title = new JLabel("⚙  Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        header.add(title, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        content.add(buildBusinessSection());
        content.add(Box.createVerticalStrut(16));
        if (SessionManager.isAdmin()) {
            content.add(buildDatabaseSection());
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildBusinessSection() {
        JPanel section = sectionPanel("🏪  Business Information");

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(4, 0, 16, 0));

        shopNameF    = field("");
        shopAddrF    = field("");
        shopPhoneF   = field("");
        footerF      = field("");
        taxRateF     = field("");
        loyaltyRateF = field("");
        currencyF    = field("");
        mpesaF       = field("");

        form.add(lbl("Shop Name:"));            form.add(shopNameF);
        form.add(lbl("Shop Address:"));         form.add(shopAddrF);
        form.add(lbl("Shop Phone:"));           form.add(shopPhoneF);
        form.add(lbl("Currency Symbol:"));      form.add(currencyF);
        form.add(lbl("Tax Rate (%):"));         form.add(taxRateF);
        form.add(lbl("Loyalty: KES per Point:"));form.add(loyaltyRateF);
        form.add(lbl("M-Pesa Paybill:"));       form.add(mpesaF);
        form.add(lbl("Receipt Footer:"));       form.add(footerF);

        JButton saveBtn = accentBtn("💾 Save Business Settings");
        saveBtn.addActionListener(e -> saveBusinessSettings());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(CARD);
        btnRow.add(saveBtn);

        section.add(form);
        section.add(btnRow);
        return section;
    }

    private JPanel buildDatabaseSection() {
        JPanel section = sectionPanel("🗄  Database Connection (PostgreSQL)");

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(4, 0, 16, 0));

        dbHostF = field("localhost");
        dbPortF = field("5432");
        dbNameF = field("kinyozi_pos");
        dbUserF = field("postgres");
        dbPassF = new JPasswordField();
        dbPassF.setBackground(new Color(35,35,55)); dbPassF.setForeground(TEXT); dbPassF.setCaretColor(Color.WHITE);

        form.add(lbl("Host:"));     form.add(dbHostF);
        form.add(lbl("Port:"));     form.add(dbPortF);
        form.add(lbl("Database:")); form.add(dbNameF);
        form.add(lbl("Username:")); form.add(dbUserF);
        form.add(lbl("Password:")); form.add(dbPassF);

        dbStatusLabel = new JLabel("● Checking connection...");
        dbStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbStatusLabel.setForeground(MUTED);

        JButton testBtn = grayBtn("Test Connection");
        JButton saveBtn = accentBtn("💾 Save & Reconnect");

        testBtn.addActionListener(e -> testConnection());
        saveBtn.addActionListener(e -> saveDbSettings());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(CARD);
        btnRow.add(testBtn);
        btnRow.add(saveBtn);
        btnRow.add(dbStatusLabel);

        section.add(form);
        section.add(btnRow);

        // Initial status check
        SwingUtilities.invokeLater(this::testConnection);

        return section;
    }

    private void loadSettings() {
        shopNameF.setText(settingsDAO.get("shop_name", "Kinyozi & Spa"));
        shopAddrF.setText(settingsDAO.get("shop_address", ""));
        shopPhoneF.setText(settingsDAO.get("shop_phone", ""));
        footerF.setText(settingsDAO.get("receipt_footer", "Thank you for your visit!"));
        taxRateF.setText(settingsDAO.get("tax_rate", "0"));
        loyaltyRateF.setText(settingsDAO.get("loyalty_points_rate", "10"));
        currencyF.setText(settingsDAO.get("currency_symbol", "KES"));
        mpesaF.setText(settingsDAO.get("mpesa_paybill", ""));
    }

    private void saveBusinessSettings() {
        settingsDAO.set("shop_name", shopNameF.getText().trim());
        settingsDAO.set("shop_address", shopAddrF.getText().trim());
        settingsDAO.set("shop_phone", shopPhoneF.getText().trim());
        settingsDAO.set("receipt_footer", footerF.getText().trim());
        settingsDAO.set("tax_rate", taxRateF.getText().trim());
        settingsDAO.set("loyalty_points_rate", loyaltyRateF.getText().trim());
        settingsDAO.set("currency_symbol", currencyF.getText().trim());
        settingsDAO.set("mpesa_paybill", mpesaF.getText().trim());
        JOptionPane.showMessageDialog(this, "Business settings saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void testConnection() {
        dbStatusLabel.setText("● Checking connection...");
        dbStatusLabel.setForeground(MUTED);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            protected Boolean doInBackground() { return DatabaseConfig.testConnection(); }
            protected void done() {
                try {
                    boolean ok = get();
                    dbStatusLabel.setText(ok ? "● Connected" : "● Disconnected");
                    dbStatusLabel.setForeground(ok ? GREEN : RED);
                } catch (Exception e) {
                    dbStatusLabel.setText("● Error checking connection");
                    dbStatusLabel.setForeground(RED);
                }
            }
        };
        worker.execute();
    }

    private void saveDbSettings() {
        String host = dbHostF.getText().trim();
        String port = dbPortF.getText().trim();
        String name = dbNameF.getText().trim();
        String user = dbUserF.getText().trim();
        String pass = new String(dbPassF.getPassword());

        if (host.isEmpty() || port.isEmpty() || name.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All database fields except password are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseConfig.updateConnection(host, port, name, user, pass);
        testConnection();
        JOptionPane.showMessageDialog(this,
                "Database settings updated. If you see 'Connected' above, the new connection is active.\n" +
                        "Note: settings are not persisted to disk automatically — update db.properties before rebuilding the .exe to keep them permanently.",
                "Reconnected", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---- helpers ----
    private JPanel sectionPanel(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(CARD);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40,40,65)),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(ACCENT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
        section.add(lbl);
        return section;
    }
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
}