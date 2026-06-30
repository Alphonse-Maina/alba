package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.ProductDAO;
import com.kinyozi.pos.model.Product;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lowStockLabel;

    private static final Color BG    = new Color(22, 22, 36);
    private static final Color CARD  = new Color(28, 28, 45);
    private static final Color ACCENT= new Color(255, 193, 7);
    private static final Color TEXT  = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);
    private static final Color WARN  = new Color(255, 152, 0);
    private static final Color DANGER= new Color(229, 57, 53);

    public InventoryPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        load();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JLabel title = new JLabel("📦  Inventory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setBackground(BG);

        lowStockLabel = new JLabel();
        lowStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lowStockLabel.setForeground(WARN);

        JButton addBtn = accentBtn("+ Add Product");
        JButton refreshBtn = grayBtn("↻ Refresh");
        addBtn.addActionListener(e -> showDialog(null));
        refreshBtn.addActionListener(e -> load());

        rightBar.add(lowStockLabel);
        rightBar.add(refreshBtn);
        rightBar.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(rightBar, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Product Name", "Barcode", "Sell Price", "Cost Price", "Stock", "Reorder At", "Status", "Actions"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };

        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String status = (String) getModel().getValueAt(row, 7);
                if ("LOW STOCK".equals(status)) c.setForeground(WARN);
                else if ("OUT OF STOCK".equals(status)) c.setForeground(DANGER);
                else if (!isRowSelected(row)) c.setForeground(TEXT);
                return c;
            }
        };

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
        table.getColumnModel().getColumn(8).setPreferredWidth(200);
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionsEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void load() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        int lowCount = 0;
        for (Product p : products) {
            String status;
            if (p.getStockQuantity() == 0) status = "OUT OF STOCK";
            else if (p.isLowStock()) { status = "LOW STOCK"; lowCount++; }
            else status = "OK";

            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getBarcode() != null ? p.getBarcode() : "",
                    "KES " + p.getSellingPrice().toPlainString(),
                    "KES " + p.getCostPrice().toPlainString(),
                    p.getStockQuantity(),
                    p.getReorderLevel(),
                    status,
                    p
            });
        }
        lowStockLabel.setText(lowCount > 0 ? "⚠ " + lowCount + " low stock item(s)" : "");
    }

    private void showDialog(Product existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Product" : "Add Product", true);
        dlg.setSize(430, 400);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(CARD);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JTextField nameF     = field(isEdit ? existing.getName() : "");
        JTextField barcodeF  = field(isEdit && existing.getBarcode()!=null ? existing.getBarcode() : "");
        JTextField sellF     = field(isEdit ? existing.getSellingPrice().toPlainString() : "");
        JTextField costF     = field(isEdit ? existing.getCostPrice().toPlainString() : "0");
        JTextField stockF    = field(isEdit ? String.valueOf(existing.getStockQuantity()) : "0");
        JTextField reorderF  = field(isEdit ? String.valueOf(existing.getReorderLevel()) : "5");
        JTextArea descF      = new JTextArea(isEdit && existing.getDescription()!=null ? existing.getDescription() : "", 2, 10);
        descF.setBackground(new Color(35,35,55)); descF.setForeground(TEXT); descF.setCaretColor(Color.WHITE);

        form.add(lbl("Product Name *")); form.add(nameF);
        form.add(lbl("Barcode"));        form.add(barcodeF);
        form.add(lbl("Selling Price *")); form.add(sellF);
        form.add(lbl("Cost Price"));     form.add(costF);
        form.add(lbl("Stock Qty"));      form.add(stockF);
        form.add(lbl("Reorder Level"));  form.add(reorderF);
        form.add(lbl("Description"));    form.add(new JScrollPane(descF));

        JButton saveBtn   = accentBtn(isEdit ? "Update" : "Save Product");
        JButton cancelBtn = grayBtn("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String sell = sellF.getText().trim();
            if (name.isEmpty() || sell.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and selling price are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Product p = isEdit ? existing : new Product();
                p.setName(name);
                p.setBarcode(barcodeF.getText().trim());
                p.setSellingPrice(new BigDecimal(sell));
                p.setCostPrice(costF.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(costF.getText().trim()));
                p.setStockQuantity(Integer.parseInt(stockF.getText().trim().isEmpty() ? "0" : stockF.getText().trim()));
                p.setReorderLevel(Integer.parseInt(reorderF.getText().trim().isEmpty() ? "5" : reorderF.getText().trim()));
                p.setDescription(descF.getText().trim());
                p.setActive(true);
                if (productDAO.saveProduct(p)) { load(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Failed to save product.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid number entered.", "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(CARD);
        btnP.add(cancelBtn); btnP.add(saveBtn);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showStockAdjustDialog(Product p) {
        JTextField qtyF = field("0");
        String[] opts = {"Add Stock", "Remove Stock"};
        JComboBox<String> typeBox = new JComboBox<>(opts);
        typeBox.setBackground(new Color(35,35,55)); typeBox.setForeground(TEXT);

        Object[] fields = {"Current Stock: " + p.getStockQuantity(), "", "Adjustment Type:", typeBox, "Quantity:", qtyF};
        int res = JOptionPane.showConfirmDialog(this, fields, "Adjust Stock: " + p.getName(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int qty = Integer.parseInt(qtyF.getText().trim());
                if (qty <= 0) return;
                boolean ok;
                if (typeBox.getSelectedIndex() == 0) ok = productDAO.adjustStock(p.getId(), qty);
                else ok = productDAO.deductStock(p.getId(), qty);
                if (ok) load();
                else JOptionPane.showMessageDialog(this, "Stock adjustment failed (insufficient stock?)", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.WARNING_MESSAGE);
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
            add(miniBtn("📦 Stock", new Color(103,58,183)));
            return this;
        }
    }

    class ActionsEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        public ActionsEditor() { super(new JCheckBox()); panel.setBackground(CARD); }
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            panel.removeAll();
            if (val instanceof Product p) {
                JButton editBtn  = miniBtn("✏ Edit", new Color(33,150,243));
                JButton stockBtn = miniBtn("📦 Stock", new Color(103,58,183));
                editBtn.addActionListener(e  -> { fireEditingStopped(); showDialog(p); });
                stockBtn.addActionListener(e -> { fireEditingStopped(); showStockAdjustDialog(p); });
                panel.add(editBtn); panel.add(stockBtn);
            }
            return panel;
        }
        public Object getCellEditorValue() { return null; }
    }
}