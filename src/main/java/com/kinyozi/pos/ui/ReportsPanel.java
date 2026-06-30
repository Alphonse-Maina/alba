package com.kinyozi.pos.ui;

import com.kinyozi.pos.dao.TransactionDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final TransactionDAO txDAO = new TransactionDAO();

    private JTextField fromField, toField;
    private JLabel totalRevenueLabel, totalTxLabel, avgTxLabel;
    private DefaultTableModel topServicesModel, barberPerfModel;
    private JPanel chartContainer;

    private static final Color BG    = new Color(22, 22, 36);
    private static final Color CARD  = new Color(28, 28, 45);
    private static final Color ACCENT= new Color(255, 193, 7);
    private static final Color TEXT  = new Color(220, 220, 235);
    private static final Color MUTED = new Color(130, 130, 150);
    private static final Color GREEN = new Color(76, 175, 80);

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        buildUI();
        runReport();
    }

    private void buildUI() {
        // Header / filter bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JLabel title = new JLabel("📊  Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBar.setBackground(BG);

        LocalDate today = LocalDate.now();
        fromField = field(today.minusDays(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        toField   = field(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        JButton todayBtn = grayBtn("Today");
        JButton weekBtn  = grayBtn("7 Days");
        JButton monthBtn = grayBtn("30 Days");
        JButton runBtn   = accentBtn("Run Report");

        todayBtn.addActionListener(e -> { setRange(today, today); runReport(); });
        weekBtn.addActionListener(e -> { setRange(today.minusDays(6), today); runReport(); });
        monthBtn.addActionListener(e -> { setRange(today.minusDays(29), today); runReport(); });
        runBtn.addActionListener(e -> runReport());

        filterBar.add(label("From:")); filterBar.add(fromField);
        filterBar.add(label("To:"));   filterBar.add(toField);
        filterBar.add(todayBtn); filterBar.add(weekBtn); filterBar.add(monthBtn);
        filterBar.add(runBtn);

        header.add(title, BorderLayout.WEST);
        header.add(filterBar, BorderLayout.EAST);

        // Summary cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryRow.setBackground(BG);
        summaryRow.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));

        totalRevenueLabel = new JLabel("KES 0.00");
        totalTxLabel      = new JLabel("0");
        avgTxLabel        = new JLabel("KES 0.00");

        summaryRow.add(summaryCard("💰 Total Revenue", totalRevenueLabel, GREEN));
        summaryRow.add(summaryCard("🧾 Transactions", totalTxLabel, new Color(33, 150, 243)));
        summaryRow.add(summaryCard("📈 Avg Sale Value", avgTxLabel, ACCENT));

        // Main content: chart + tables
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(CARD);
        chartContainer.setBorder(BorderFactory.createLineBorder(new Color(40,40,65)));
        chartContainer.setPreferredSize(new Dimension(0, 280));
        chartContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 12, 0));
        tablesRow.setBackground(BG);
        tablesRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        tablesRow.setPreferredSize(new Dimension(0, 280));
        tablesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        topServicesModel = new DefaultTableModel(new String[]{"Service", "Qty Sold", "Revenue"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topServicesTable = styledTable(topServicesModel);
        tablesRow.add(wrapTitled("🏆 Top Services", topServicesTable));

        barberPerfModel = new DefaultTableModel(new String[]{"Staff", "Transactions", "Revenue"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable barberTable = styledTable(barberPerfModel);
        tablesRow.add(wrapTitled("👤 Staff Performance", barberTable));

        content.add(chartContainer);
        content.add(tablesRow);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(BG);
        north.add(header, BorderLayout.NORTH);
        north.add(summaryRow, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void setRange(LocalDate from, LocalDate to) {
        fromField.setText(from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        toField.setText(to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void runReport() {
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate to   = LocalDate.parse(toField.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Daily revenue for chart + summary
            List<Object[]> daily = txDAO.getDailyRevenue(from, to);
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalTx = 0;
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Object[] row : daily) {
                LocalDate d = (LocalDate) row[0];
                BigDecimal rev = (BigDecimal) row[1];
                int count = (int) row[2];
                totalRevenue = totalRevenue.add(rev);
                totalTx += count;
                dataset.addValue(rev, "Revenue", d.format(DateTimeFormatter.ofPattern("dd MMM")));
            }

            totalRevenueLabel.setText("KES " + totalRevenue.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            totalTxLabel.setText(String.valueOf(totalTx));
            BigDecimal avg = totalTx > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalTx), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
            avgTxLabel.setText("KES " + avg.toPlainString());

            renderChart(dataset);

            // Top services
            topServicesModel.setRowCount(0);
            for (Object[] row : txDAO.getTopServices(from, to)) {
                topServicesModel.addRow(new Object[]{row[0], row[1], "KES " + ((BigDecimal) row[2]).toPlainString()});
            }

            // Staff performance
            barberPerfModel.setRowCount(0);
            for (Object[] row : txDAO.getBarberPerformance(from, to)) {
                barberPerfModel.addRow(new Object[]{row[0], row[1], "KES " + ((BigDecimal) row[2]).toPlainString()});
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use dd/MM/yyyy.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void renderChart(DefaultCategoryDataset dataset) {
        chartContainer.removeAll();
        JFreeChart chart = ChartFactory.createBarChart(
                "Revenue by Day", "Date", "Revenue (KES)",
                dataset, PlotOrientation.VERTICAL, false, true, false
        );
        chart.setBackgroundPaint(CARD);
        chart.getTitle().setPaint(TEXT);
        var plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(35, 35, 55));
        plot.setRangeGridlinePaint(new Color(50, 50, 75));
        plot.getDomainAxis().setTickLabelPaint(MUTED);
        plot.getRangeAxis().setTickLabelPaint(MUTED);
        plot.getDomainAxis().setLabelPaint(TEXT);
        plot.getRangeAxis().setLabelPaint(TEXT);
        plot.getRenderer().setSeriesPaint(0, ACCENT);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD);
        chartPanel.setPreferredSize(new Dimension(0, 260));
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private JPanel summaryCard(String title, JLabel valueLabel, Color accent) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, accent),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT);
        p.add(t);
        p.add(Box.createVerticalStrut(6));
        p.add(valueLabel);
        return p;
    }

    private JPanel wrapTitled(String title, JTable table) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CARD);
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(40,40,65)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(CARD);
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD);
        t.setForeground(TEXT);
        t.setGridColor(new Color(38, 38, 58));
        t.setRowHeight(32);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setBackground(new Color(30, 30, 50));
        t.getTableHeader().setForeground(MUTED);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(50, 50, 80));
        return t;
    }

    private JTextField field(String v) {
        JTextField f = new JTextField(v, 9);
        f.setBackground(new Color(35,35,55)); f.setForeground(TEXT); f.setCaretColor(Color.WHITE);
        return f;
    }
    private JLabel label(String t) {
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