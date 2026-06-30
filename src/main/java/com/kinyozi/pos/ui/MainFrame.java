package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.TransactionDAO;
import com.kinyozi.pos.dao.ProductDAO;
import com.kinyozi.pos.dao.AppointmentDAO;
import com.kinyozi.pos.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel statusBar;
    private JLabel clockLabel;

    // Sidebar buttons
    private JButton[] navButtons;
    private int activeNav = 0;

    private static final Color SIDEBAR_BG = new Color(18, 18, 32);
    private static final Color SIDEBAR_ACTIVE = new Color(255, 193, 7);
    private static final Color SIDEBAR_HOVER = new Color(35, 35, 55);
    private static final Color CONTENT_BG = new Color(22, 22, 36);
    private static final Color ACCENT = new Color(255, 193, 7);

    public MainFrame() {
        setTitle("Kinyozi & Spa POS System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        buildUI();
        startClock();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CONTENT_BG);

        // === SIDEBAR ===
        JPanel sidebar = buildSidebar();

        // === CONTENT AREA ===
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);
        contentPanel.add(new SalePanel(), "SALE");
        contentPanel.add(new AppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(new CustomersPanel(), "CUSTOMERS");
        contentPanel.add(new ServicesPanel(), "SERVICES");
        contentPanel.add(new InventoryPanel(), "INVENTORY");
        contentPanel.add(new ReportsPanel(), "REPORTS");
        contentPanel.add(new StaffPanel(), "STAFF");
        contentPanel.add(new SettingsPanel(), "SETTINGS");

        // === STATUS BAR ===
        JPanel statusPanel = buildStatusBar();

        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        root.add(statusPanel, BorderLayout.SOUTH);
        setContentPane(root);

        cardLayout.show(contentPanel, "SALE");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(40, 40, 60)));

        // Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        logoPanel.setMaximumSize(new Dimension(200, 120));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emoji = new JLabel("✂", SwingConstants.CENTER);
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        emoji.setForeground(ACCENT);
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Kinyozi POS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(emoji);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(title);
        sidebar.add(logoPanel);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 40, 60));
        sep.setMaximumSize(new Dimension(200, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        // Nav items
        String[][] navItems = {
                {"🛒", "New Sale", "SALE"},
                {"📅", "Appointments", "APPOINTMENTS"},
                {"👥", "Customers", "CUSTOMERS"},
                {"✂", "Services", "SERVICES"},
                {"📦", "Inventory", "INVENTORY"},
                {"📊", "Reports", "REPORTS"},
                {"👤", "Staff", "STAFF"},
                {"⚙", "Settings", "SETTINGS"},
        };

        navButtons = new JButton[navItems.length];
        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            final String card = navItems[i][2];
            JButton btn = createNavButton(navItems[i][0], navItems[i][1]);
            navButtons[i] = btn;

            // Hide staff/settings for non-managers
            if ((card.equals("STAFF") || card.equals("SETTINGS")) && !SessionManager.isManager()) {
                btn.setVisible(false);
            }

            btn.addActionListener(e -> {
                setActiveNav(idx);
                cardLayout.show(contentPanel, card);
            });
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());

        // User info at bottom
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(25, 25, 42));
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        userPanel.setMaximumSize(new Dimension(200, 90));

        JLabel userLbl = new JLabel("👤 " + SessionManager.getCurrentUser().getFullName());
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(Color.WHITE);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLbl = new JLabel(SessionManager.getCurrentUser().getRole());
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLbl.setForeground(new Color(150, 150, 170));
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoutBtn.setForeground(new Color(200, 100, 100));
        logoutBtn.setBackground(new Color(25, 25, 42));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> logout());

        userPanel.add(userLbl);
        userPanel.add(Box.createVerticalStrut(2));
        userPanel.add(roleLbl);
        userPanel.add(Box.createVerticalStrut(4));
        userPanel.add(logoutBtn);
        sidebar.add(userPanel);

        setActiveNav(0);
        return sidebar;
    }

    private JButton createNavButton(String icon, String label) {
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 180, 200));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 48));
        btn.setMinimumSize(new Dimension(200, 48));
        btn.setPreferredSize(new Dimension(200, 48));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.getBackground() != SIDEBAR_ACTIVE) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.getBackground() != SIDEBAR_ACTIVE) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        return btn;
    }

    private void setActiveNav(int idx) {
        activeNav = idx;
        for (int i = 0; i < navButtons.length; i++) {
            if (i == idx) {
                navButtons[i].setBackground(new Color(40, 35, 10));
                navButtons[i].setForeground(ACCENT);
            } else {
                navButtons[i].setBackground(SIDEBAR_BG);
                navButtons[i].setForeground(new Color(180, 180, 200));
            }
        }
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 25));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 60)));
        panel.setPreferredSize(new Dimension(0, 28));

        statusBar = new JLabel("  Kinyozi & Spa POS — Ready");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.setForeground(new Color(120, 120, 140));

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clockLabel.setForeground(new Color(120, 120, 140));
        clockLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        panel.add(statusBar, BorderLayout.WEST);
        panel.add(clockLabel, BorderLayout.EAST);
        return panel;
    }

    private void startClock() {
        Timer t = new Timer(1000, e -> {
            String time = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss"));
            clockLabel.setText(time + "  ");
        });
        t.start();
    }

    public void setStatus(String msg) {
        statusBar.setText("  " + msg);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Sign out of the system?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}