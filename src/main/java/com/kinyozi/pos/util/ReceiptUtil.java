package com.kinyozi.pos.util;

import com.kinyozi.pos.dao.SettingsDAO;
import com.kinyozi.pos.model.Transaction;
import com.kinyozi.pos.model.TransactionItem;

import java.time.format.DateTimeFormatter;

public class ReceiptUtil {
    private static final int WIDTH = 42;
    private static final String LINE = "=".repeat(WIDTH);
    private static final String DLINE = "-".repeat(WIDTH);

    public static String generateReceiptText(Transaction t) {
        SettingsDAO settings = new SettingsDAO();
        StringBuilder sb = new StringBuilder();

        String shopName = settings.get("shop_name", "Kinyozi & Spa");
        String shopAddr = settings.get("shop_address", "");
        String shopPhone = settings.get("shop_phone", "");
        String footer = settings.get("receipt_footer", "Thank you for your visit!");
        String currency = settings.get("currency_symbol", "KES");

        sb.append(center(shopName)).append("\n");
        if (!shopAddr.isEmpty()) sb.append(center(shopAddr)).append("\n");
        if (!shopPhone.isEmpty()) sb.append(center("Tel: " + shopPhone)).append("\n");
        sb.append(LINE).append("\n");

        sb.append("Receipt: ").append(t.getReceiptNumber()).append("\n");
        if (t.getCreatedAt() != null) {
            sb.append("Date: ").append(t.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }
        if (t.getBarberName() != null) {
            sb.append("Staff: ").append(t.getBarberName()).append("\n");
        }
        if (t.getCustomerName() != null) {
            sb.append("Customer: ").append(t.getCustomerName()).append("\n");
        }
        sb.append(DLINE).append("\n");
        sb.append(padRight("Item", 24)).append(padLeft("Qty", 5))
                .append(padLeft("Total", 12)).append("\n");
        sb.append(DLINE).append("\n");

        for (TransactionItem item : t.getItems()) {
            String name = item.getItemName();
            if (name.length() > 24) name = name.substring(0, 22) + "..";
            sb.append(padRight(name, 24))
                    .append(padLeft(String.valueOf(item.getQuantity()), 5))
                    .append(padLeft(currency + " " + item.getTotalPrice().toPlainString(), 12))
                    .append("\n");
            sb.append("  @ " + currency + " " + item.getUnitPrice().toPlainString() + " each\n");
        }

        sb.append(DLINE).append("\n");
        sb.append(padRight("Subtotal:", 28)).append(padLeft(currency + " " + t.getSubtotal().toPlainString(), 14)).append("\n");

        if (t.getDiscountAmount() != null && t.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(padRight("Discount:", 28)).append(padLeft("- " + currency + " " + t.getDiscountAmount().toPlainString(), 14)).append("\n");
        }
        if (t.getTaxAmount() != null && t.getTaxAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(padRight("Tax:", 28)).append(padLeft(currency + " " + t.getTaxAmount().toPlainString(), 14)).append("\n");
        }
        sb.append(LINE).append("\n");
        sb.append(padRight("TOTAL:", 28)).append(padLeft(currency + " " + t.getTotalAmount().toPlainString(), 14)).append("\n");
        sb.append(LINE).append("\n");
        sb.append(padRight("Payment: " + t.getPaymentMethod(), 28))
                .append(padLeft(currency + " " + t.getAmountPaid().toPlainString(), 14)).append("\n");
        if (t.getChangeGiven() != null && t.getChangeGiven().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(padRight("Change:", 28)).append(padLeft(currency + " " + t.getChangeGiven().toPlainString(), 14)).append("\n");
        }
        if (t.getMpesaCode() != null && !t.getMpesaCode().isEmpty()) {
            sb.append("M-Pesa Ref: ").append(t.getMpesaCode()).append("\n");
        }
        sb.append(LINE).append("\n");
        sb.append(center(footer)).append("\n");

        return sb.toString();
    }

    private static String center(String text) {
        if (text == null || text.isEmpty()) return "";
        int pad = (WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + text;
    }

    private static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text.substring(0, length);
        return text + " ".repeat(length - text.length());
    }

    private static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text.substring(0, length);
        return " ".repeat(length - text.length()) + text;
    }

    public static String generateReceiptNumber() {
        return "RCP" + System.currentTimeMillis();
    }
}