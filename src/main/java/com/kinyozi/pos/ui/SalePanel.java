package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.*;
import com.kinyozi.pos.model.*;
import com.kinyozi.pos.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.*;
import java.util.*;
import java.util.List;

public class SalePanel extends JPanel {
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    // Cart state
    private final DefaultTableModel cartModel;
    private final List<TransactionItem> cartItems = new ArrayList<>();
    private Customer selectedCustomer;
    private User selectedBarber;

    // Totals
    private JLabel subtotalLabel, discountLabel, totalLabel;
    private JTextField discountField;
    private JComboBox<String> paymentMethodBox;
    private JTextField amountPaidField, mpesaCodeField;
    private JLabel changeLabel;
    private JComboBox<User> barberCombo;
    private JTextField customerSearch;
    private JLabel customerLabel;

    private static final Color BG = new Color(22, 22, 36);
    private static final Color CARD_BG = new Color(28, 28, 45);
    private static final Color ACCENT = new Color(255, 193, 7);
    private static final Color BTN_GREEN = new Color(76, 175, 80);
    private static final Color BTN_RED = new Color(229, 57, 53);
    private static final Color TEXT = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);

    public SalePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        cartModel = new DefaultTableModel(
                new String[]{"Item", "Type", "Qty", "Price", "Total", ""}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 5; }
            public Class<?> getColumnClass(int c) { return c == 5 ? JButton.class : String.class; }
        };

        buildUI();
        loadBarbers();
    }

    private void buildUI() {
        // ---- LEFT: Services/Products panel ----
        JPanel leftPanel = buildServiceProductPanel();

        // ---- RIGHT: Cart + Payment ----
        JPanel rightPanel = buildCartPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(620);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(BG);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildServiceProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        // Top: customer & barber selection
        JPanel topBar = new JPanel(new GridLayout(1, 2, 10, 0));
        topBar.setBackground(BG);

        // Customer
        JPanel custPanel = card("Customer");
        JPanel custRow = new JPanel(new BorderLayout(6, 0));
        custRow.setBackground(CARD_BG);
        customerSearch = new JTextField();
        customerSearch.setToolTipText("Search by name or phone");
        customerSearch.setBackground(new Color(35, 35, 55));
        customerSearch.setForeground(TEXT);
        customerSearch.setCaretColor(Color.WHITE);
        JButton findCustBtn = accentBtn("Find");
        customerLabel = new JLabel("Walk-in Customer");
        customerLabel.setForeground(MUTED);
        customerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        JButton clearCustBtn = smallBtn("✕");
        clearCustBtn.addActionListener(e -> clearCustomer());
        JPanel custBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        custBtnRow.setBackground(CARD_BG);
        custBtnRow.add(customerLabel);
        custBtnRow.add(clearCustBtn);
        custRow.add(customerSearch, BorderLayout.CENTER);
        custRow.add(findCustBtn, BorderLayout.EAST);
        custPanel.add(custRow);
        custPanel.add(Box.createVerticalStrut(4));
        custPanel.add(custBtnRow);
        findCustBtn.addActionListener(e -> searchCustomer());
        customerSearch.addActionListener(e -> searchCustomer());

        // Barber
        JPanel barberPanel = card("Served By");
        barberCombo = new JComboBox<>();
        barberCombo.setBackground(new Color(35, 35, 55));
        barberCombo.setForeground(TEXT);
        barberPanel.add(barberCombo);

        topBar.add(custPanel);
        topBar.add(barberPanel);

        // Service Buttons Grid
        JPanel serviceHeader = new JPanel(new BorderLayout());
        serviceHeader.setBackground(BG);
        JLabel servLbl = new JLabel("Services & Products");
        servLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        servLbl.setForeground(TEXT);
        JButton refreshBtn = smallBtn("↻");
        refreshBtn.addActionListener(e -> refreshServiceGrid((JPanel) ((BorderLayout)((JPanel)refreshBtn.getParent().getParent()).getLayout()).getLayoutComponent(BorderLayout.CENTER)));
        serviceHeader.add(servLbl, BorderLayout.WEST);
        serviceHeader.add(refreshBtn, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(buildServiceGrid());
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(topBar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(BG);
        center.add(serviceHeader, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildServiceGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
        grid.setBackground(BG);
        grid.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // Services
        List<Service> services = serviceDAO.getAllActiveServices();
        String currentCat = "";
        for (Service s : services) {
            if (!s.getCategoryName().equals(currentCat)) {
                currentCat = s.getCategoryName();
                JLabel catLbl = new JLabel("  " + currentCat.toUpperCase());
                catLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                catLbl.setForeground(ACCENT);
                catLbl.setBackground(new Color(35, 30, 8));
                catLbl.setOpaque(true);
                catLbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
                JPanel span = new JPanel(new BorderLayout());
                span.setBackground(BG);
                span.add(catLbl);
                // Add category spanning across 3 columns
                grid.add(wrapInCol3(span, grid));
            }
            JButton btn = serviceBtn(s.getName(), "KES " + s.getPrice().toPlainString(), new Color(30, 50, 80));
            btn.addActionListener(e -> addServiceToCart(s));
            grid.add(btn);
        }

        // Spacer if odd
        while (grid.getComponentCount() % 3 != 0) grid.add(new JPanel() {{ setBackground(BG); }});

        // Products section header
        JLabel prodHdr = new JLabel("  PRODUCTS");
        prodHdr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        prodHdr.setForeground(new Color(76, 175, 80));
        prodHdr.setBackground(new Color(8, 35, 15));
        prodHdr.setOpaque(true);
        prodHdr.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        JPanel prodSpan = new JPanel(new BorderLayout());
        prodSpan.setBackground(BG);
        prodSpan.add(prodHdr);
        grid.add(wrapInCol3(prodSpan, grid));

        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            Color c = p.isLowStock() ? new Color(60, 25, 25) : new Color(25, 50, 30);
            String stock = p.isLowStock() ? "⚠ " + p.getStockQuantity() + " left" : "Stock: " + p.getStockQuantity();
            JButton btn = serviceBtn(p.getName(), "KES " + p.getSellingPrice().toPlainString() + "\n" + stock, c);
            if (p.getStockQuantity() == 0) {
                btn.setEnabled(false);
                btn.setText("<html><center>" + p.getName() + "<br><small>OUT OF STOCK</small></center></html>");
            }
            btn.addActionListener(e -> addProductToCart(p));
            grid.add(btn);
        }

        while (grid.getComponentCount() % 3 != 0) grid.add(new JPanel() {{ setBackground(BG); }});
        return grid;
    }

    private JPanel wrapInCol3(JPanel content, JPanel grid) {
        // Category headers span 3 cols — we add content and 2 invisible panels
        // Actually GridLayout doesn't support colspan; we'll use a JPanel trick
        return content; // just add normally; we'll fill 2 more empties after
    }

    private void addCategoryLabel(JPanel grid, String cat) {
        JPanel empty1 = new JPanel(); empty1.setBackground(BG);
        JPanel empty2 = new JPanel(); empty2.setBackground(BG);
        grid.add(empty1);
        grid.add(empty2);
    }

    private void refreshServiceGrid(JPanel container) {
        if (container != null) {
            container.removeAll();
            container.add(buildServiceGrid());
            container.revalidate();
            container.repaint();
        }
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));

        // Header
        JLabel header = new JLabel("🛒  Cart");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(TEXT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        // Cart table
        JTable cartTable = new JTable(cartModel);
        cartTable.setBackground(CARD_BG);
        cartTable.setForeground(TEXT);
        cartTable.setGridColor(new Color(40, 40, 60));
        cartTable.setRowHeight(36);
        cartTable.setShowVerticalLines(false);
        cartTable.getTableHeader().setBackground(new Color(30, 30, 50));
        cartTable.getTableHeader().setForeground(MUTED);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.setSelectionBackground(new Color(50, 50, 80));

        // Remove button column
        cartTable.getColumn("").setCellRenderer(new ButtonRenderer("Remove", BTN_RED));
        cartTable.getColumn("").setCellEditor(new ButtonEditor(new JCheckBox(), cartTable));
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(5).setMaxWidth(70);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 65)));
        cartScroll.setBackground(CARD_BG);
        cartScroll.getViewport().setBackground(CARD_BG);

        // Totals
        JPanel totalsPanel = buildTotalsPanel();

        // Payment
        JPanel paymentPanel = buildPaymentPanel();

        // Checkout button
        JButton checkoutBtn = new JButton("✔  Complete Sale");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        checkoutBtn.setBackground(BTN_GREEN);
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkoutBtn.setPreferredSize(new Dimension(0, 50));
        checkoutBtn.setBorderPainted(false);
        checkoutBtn.addActionListener(e -> completeSale());

        JButton clearBtn = new JButton("🗑  Clear Cart");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setBackground(new Color(50, 50, 70));
        clearBtn.setForeground(new Color(200, 100, 100));
        clearBtn.setFocusPainted(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearCart());

        JPanel btnPanel = new JPanel(new BorderLayout(6, 0));
        btnPanel.setBackground(BG);
        btnPanel.add(checkoutBtn, BorderLayout.CENTER);
        btnPanel.add(clearBtn, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(BG);
        bottom.add(totalsPanel, BorderLayout.NORTH);
        bottom.add(paymentPanel, BorderLayout.CENTER);
        bottom.add(btnPanel, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(cartScroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTotalsPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 65)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        subtotalLabel = totalsLabel("KES 0.00");
        discountLabel = totalsLabel("KES 0.00");
        totalLabel = new JLabel("KES 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(ACCENT);

        p.add(mutedLabel("Subtotal:")); p.add(subtotalLabel);

        JPanel discRow = new JPanel(new BorderLayout(4, 0));
        discRow.setBackground(CARD_BG);
        discRow.add(mutedLabel("Discount:"), BorderLayout.WEST);
        discountField = new JTextField("0");
        discountField.setPreferredSize(new Dimension(60, 24));
        discountField.setBackground(new Color(35, 35, 55));
        discountField.setForeground(TEXT);
        discountField.setCaretColor(Color.WHITE);
        discountField.addActionListener(e -> updateTotals());
        discountField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { updateTotals(); }
        });
        discRow.add(discountField, BorderLayout.CENTER);
        discRow.add(new JLabel("KES ", SwingConstants.RIGHT) {{
            setForeground(MUTED);
        }}, BorderLayout.WEST);

        p.add(new JLabel("Discount (KES):") {{ setForeground(MUTED); setFont(new Font("Segoe UI", Font.PLAIN, 12)); }});
        p.add(discountField);
        p.add(new JSeparator() {{ setForeground(new Color(50, 50, 70)); }});
        p.add(new JSeparator() {{ setForeground(new Color(50, 50, 70)); }});
        p.add(new JLabel("TOTAL:") {{ setForeground(TEXT); setFont(new Font("Segoe UI", Font.BOLD, 14)); }});
        p.add(totalLabel);

        return p;
    }

    private JPanel buildPaymentPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 65)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        paymentMethodBox = new JComboBox<>(new String[]{"CASH", "MPESA", "CARD"});
        paymentMethodBox.setBackground(new Color(35, 35, 55));
        paymentMethodBox.setForeground(TEXT);
        paymentMethodBox.addActionListener(e -> onPaymentMethodChange());

        amountPaidField = new JTextField("0");
        amountPaidField.setBackground(new Color(35, 35, 55));
        amountPaidField.setForeground(TEXT);
        amountPaidField.setCaretColor(Color.WHITE);
        amountPaidField.addActionListener(e -> updateChange());
        amountPaidField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { updateChange(); }
        });

        mpesaCodeField = new JTextField();
        mpesaCodeField.setBackground(new Color(35, 35, 55));
        mpesaCodeField.setForeground(TEXT);
        mpesaCodeField.setCaretColor(Color.WHITE);
        mpesaCodeField.setVisible(false);

        changeLabel = new JLabel("KES 0.00");
        changeLabel.setForeground(BTN_GREEN);
        changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        p.add(mutedLabel("Payment:")); p.add(paymentMethodBox);
        p.add(mutedLabel("Amount Paid:")); p.add(amountPaidField);
        p.add(mutedLabel("M-Pesa Code:")); p.add(mpesaCodeField);
        p.add(mutedLabel("Change:")); p.add(changeLabel);

        return p;
    }

    private void onPaymentMethodChange() {
        String method = (String) paymentMethodBox.getSelectedItem();
        boolean isMpesa = "MPESA".equals(method);
        mpesaCodeField.setVisible(isMpesa);
        if ("CARD".equals(method)) {
            // Auto-fill total for card
            amountPaidField.setText(getTotalAmount().toPlainString());
            updateChange();
        }
        revalidate(); repaint();
    }

    private void searchCustomer() {
        String query = customerSearch.getText().trim();
        if (query.isEmpty()) return;

        List<Customer> results = customerDAO.searchCustomers(query);
        if (results.isEmpty()) {
            int add = JOptionPane.showConfirmDialog(this,
                    "No customer found. Add new customer?", "Not Found", JOptionPane.YES_NO_OPTION);
            if (add == JOptionPane.YES_OPTION) {
                showAddCustomerDialog();
            }
            return;
        }

        if (results.size() == 1) {
            setCustomer(results.get(0));
            return;
        }

        // Multiple — show selector
        String[] names = results.stream().map(c -> c.getFullName() + " - " + c.getPhone()).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this,
                "Select customer:", "Search Results",
                JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (chosen != null) {
            int idx = Arrays.asList(names).indexOf(chosen);
            if (idx >= 0) setCustomer(results.get(idx));
        }
    }

    private void setCustomer(Customer c) {
        selectedCustomer = c;
        customerLabel.setText("✓ " + c.getFullName() + " (" + c.getLoyaltyPoints() + " pts)");
        customerLabel.setForeground(BTN_GREEN);
    }

    private void clearCustomer() {
        selectedCustomer = null;
        customerLabel.setText("Walk-in Customer");
        customerLabel.setForeground(MUTED);
        customerSearch.setText("");
    }

    private void showAddCustomerDialog() {
        JTextField nameF = new JTextField();
        JTextField phoneF = new JTextField();
        Object[] fields = {"Full Name:", nameF, "Phone:", phoneF};
        int res = JOptionPane.showConfirmDialog(this, fields, "Add Customer", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            Customer c = new Customer(nameF.getText().trim(), phoneF.getText().trim());
            c = customerDAO.saveCustomer(c);
            if (c != null) setCustomer(c);
        }
    }

    private void addServiceToCart(Service s) {
        for (TransactionItem item : cartItems) {
            if ("SERVICE".equals(item.getItemType()) && s.getId() == (item.getServiceId() != null ? item.getServiceId() : -1)) {
                item.setQuantity(item.getQuantity() + 1);
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                refreshCart();
                return;
            }
        }
        TransactionItem item = new TransactionItem("SERVICE", s.getName(), 1, s.getPrice());
        item.setServiceId(s.getId());
        cartItems.add(item);
        refreshCart();
    }

    private void addProductToCart(Product p) {
        if (p.getStockQuantity() <= 0) {
            JOptionPane.showMessageDialog(this, "Product is out of stock!", "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (TransactionItem item : cartItems) {
            if ("PRODUCT".equals(item.getItemType()) && p.getId() == (item.getProductId() != null ? item.getProductId() : -1)) {
                item.setQuantity(item.getQuantity() + 1);
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                refreshCart();
                return;
            }
        }
        TransactionItem item = new TransactionItem("PRODUCT", p.getName(), 1, p.getSellingPrice());
        item.setProductId(p.getId());
        cartItems.add(item);
        refreshCart();
    }

    private void removeCartItem(int row) {
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            refreshCart();
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (TransactionItem item : cartItems) {
            cartModel.addRow(new Object[]{
                    item.getItemName(),
                    item.getItemType(),
                    item.getQuantity(),
                    "KES " + item.getUnitPrice().toPlainString(),
                    "KES " + item.getTotalPrice().toPlainString(),
                    "Remove"
            });
        }
        updateTotals();
    }

    private BigDecimal getSubtotal() {
        return cartItems.stream()
                .map(TransactionItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getDiscount() {
        try { return new BigDecimal(discountField.getText().trim()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private BigDecimal getTotalAmount() {
        return getSubtotal().subtract(getDiscount()).max(BigDecimal.ZERO);
    }

    private void updateTotals() {
        BigDecimal sub = getSubtotal();
        BigDecimal disc = getDiscount();
        BigDecimal total = getTotalAmount();
        subtotalLabel.setText("KES " + sub.setScale(2, RoundingMode.HALF_UP).toPlainString());
        discountLabel.setText("KES " + disc.setScale(2, RoundingMode.HALF_UP).toPlainString());
        totalLabel.setText("KES " + total.setScale(2, RoundingMode.HALF_UP).toPlainString());
        updateChange();
    }

    private void updateChange() {
        try {
            BigDecimal paid = new BigDecimal(amountPaidField.getText().trim());
            BigDecimal total = getTotalAmount();
            BigDecimal change = paid.subtract(total);
            changeLabel.setText("KES " + change.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString());
            changeLabel.setForeground(change.compareTo(BigDecimal.ZERO) >= 0 ? BTN_GREEN : BTN_RED);
        } catch (Exception e) {
            changeLabel.setText("KES 0.00");
        }
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal total = getTotalAmount();
        BigDecimal paid;
        try { paid = new BigDecimal(amountPaidField.getText().trim()); }
        catch (Exception e) { paid = BigDecimal.ZERO; }

        String method = (String) paymentMethodBox.getSelectedItem();
        if ("CASH".equals(method) && paid.compareTo(total) < 0) {
            JOptionPane.showMessageDialog(this, "Amount paid is less than total!", "Payment Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build transaction
        Transaction tx = new Transaction();
        tx.setReceiptNumber(ReceiptUtil.generateReceiptNumber());
        tx.setSubtotal(getSubtotal());
        tx.setDiscountAmount(getDiscount());
        tx.setTaxAmount(BigDecimal.ZERO);
        tx.setTotalAmount(total);
        tx.setAmountPaid(paid);
        tx.setChangeGiven(paid.subtract(total).max(BigDecimal.ZERO));
        tx.setPaymentMethod(method);
        tx.setMpesaCode(mpesaCodeField.getText().trim());
        tx.setStatus("COMPLETED");

        if (selectedCustomer != null) {
            tx.setCustomerId(selectedCustomer.getId());
            tx.setCustomerName(selectedCustomer.getFullName());
        }

        User barber = (User) barberCombo.getSelectedItem();
        if (barber != null) {
            tx.setServedBy(barber.getId());
            tx.setBarberName(barber.getFullName());
        }

        for (TransactionItem item : cartItems) {
            if (barber != null) item.setBarberId(barber.getId());
            tx.addItem(item);
        }

        // Save
        Transaction saved = transactionDAO.saveTransaction(tx);
        if (saved != null) {
            // Deduct product stock
            for (TransactionItem item : cartItems) {
                if ("PRODUCT".equals(item.getItemType()) && item.getProductId() != null) {
                    productDAO.deductStock(item.getProductId(), item.getQuantity());
                }
            }
            // Add loyalty points
            if (selectedCustomer != null) {
                int rate = Integer.parseInt(settingsDAO.get("loyalty_points_rate", "10"));
                int points = total.divide(BigDecimal.valueOf(rate), 0, RoundingMode.DOWN).intValue();
                customerDAO.addLoyaltyPoints(selectedCustomer.getId(), points);
            }

            // Show receipt
            tx.setItems(new ArrayList<>(cartItems));
            String receipt = ReceiptUtil.generateReceiptText(tx);
            showReceiptDialog(receipt);

            // Clear cart
            clearCart();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save transaction!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReceiptDialog(String receipt) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Receipt", true);
        dialog.setSize(460, 600);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea(receipt);
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(250, 250, 245));
        area.setForeground(Color.BLACK);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton printBtn = new JButton("🖨 Print");
        printBtn.addActionListener(e -> {
            try { area.print(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(printBtn);
        btnP.add(closeBtn);

        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(btnP, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void clearCart() {
        cartItems.clear();
        cartModel.setRowCount(0);
        discountField.setText("0");
        amountPaidField.setText("0");
        mpesaCodeField.setText("");
        updateTotals();
    }

    private void loadBarbers() {
        List<User> staff = userDAO.getActiveStaff();
        barberCombo.addItem(null);
        for (User u : staff) barberCombo.addItem(u);
        if (staff.contains(SessionManager.getCurrentUser())) {
            barberCombo.setSelectedItem(SessionManager.getCurrentUser());
        }
    }

    // ---- Helper components ----
    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 65)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(MUTED);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        return p;
    }

    private JButton serviceBtn(String name, String subtitle, Color bg) {
        String html = "<html><center><b>" + name + "</b><br><small>" + subtitle.replace("\n", "<br>") + "</small></center></html>";
        JButton btn = new JButton(html);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 70));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 110), 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        return btn;
    }

    private JButton accentBtn(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(new Color(20, 20, 20));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton smallBtn(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 60));
        btn.setForeground(MUTED);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JLabel totalsLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    // Table button renderer
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text, Color bg) {
            setText(text);
            setBackground(bg);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int editingRow;
        private JTable table;

        public ButtonEditor(JCheckBox cb, JTable table) {
            super(cb);
            this.table = table;
            button = new JButton("Remove");
            button.setBackground(BTN_RED);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.addActionListener(e -> {
                fireEditingStopped();
                removeCartItem(editingRow);
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return button;
        }

        public Object getCellEditorValue() { return "Remove"; }
    }
}