package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.UserDAO;
import com.kinyozi.pos.model.User;
import com.kinyozi.pos.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Kinyozi & Spa POS - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(18, 18, 28));

        // Header panel with logo area
        JPanel header = new JPanel();
        header.setBackground(new Color(18, 18, 28));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(40, 30, 20, 30));

        JLabel logoLabel = new JLabel("✂", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        logoLabel.setForeground(new Color(255, 193, 7));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Kinyozi & Spa POS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Point of Sale System", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(150, 150, 170));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logoLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subLabel);

        // Form panel
        JPanel form = new JPanel();
        form.setBackground(new Color(28, 28, 42));
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 30, 30, 30),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
                        BorderFactory.createEmptyBorder(30, 30, 30, 30)
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;

        // Username
        JLabel userLbl = new JLabel("Username");
        userLbl.setForeground(new Color(180, 180, 200));
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 0;
        form.add(userLbl, gbc);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(280, 40));
        gbc.gridy = 1;
        form.add(usernameField, gbc);

        // Password
        JLabel passLbl = new JLabel("Password");
        passLbl.setForeground(new Color(180, 180, 200));
        passLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 2;
        form.add(passLbl, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(280, 40));
        gbc.gridy = 3;
        form.add(passwordField, gbc);

        // Login button
        loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(new Color(255, 193, 7));
        loginBtn.setForeground(new Color(18, 18, 28));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(280, 44));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        gbc.gridy = 4;
        gbc.insets = new Insets(18, 0, 6, 0);
        form.add(loginBtn, gbc);

        // Hint
        JLabel hint = new JLabel("Default: admin / admin123");
        hint.setForeground(new Color(100, 100, 120));
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 0, 0);
        form.add(hint, gbc);

        // Actions
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            protected User doInBackground() { return userDAO.authenticate(username, password); }
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        SessionManager.setCurrentUser(user);
                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Login error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In");
            }
        };
        worker.execute();
    }
}