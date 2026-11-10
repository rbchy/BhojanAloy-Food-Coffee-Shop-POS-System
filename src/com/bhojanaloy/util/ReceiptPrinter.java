package com.bhojanaloy.util;

import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

/**
 * Prints a formatted customer receipt to any printer configured on this
 * computer (including "Save as PDF" / thermal receipt printers registered
 * as a standard OS printer) using Java's built-in Printable/PrinterJob API —
 * no extra libraries or vendor-specific ESC/POS driver required. For a
 * dedicated thermal receipt printer, install its standard driver in
 * macOS/Windows first; it will then show up in the printer chooser below.
 */
public class ReceiptPrinter implements Printable {

    private final Order order;

    public ReceiptPrinter(Order order) {
        this.order = order;
    }

    /** Opens the OS print dialog so the cashier can pick the receipt/customer printer. */
    public static void printReceipt(Order order, Component parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ReceiptPrinter(order));
        job.setJobName("Bhojan-Aloy Receipt " + order.getOrderNumber());
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                javax.swing.JOptionPane.showMessageDialog(parent,
                        "Printing failed: " + e.getMessage(), "Print Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));

        int lineHeight = 14;
        int y = lineHeight;
        int width = (int) pageFormat.getImageableWidth();

        y = centeredLine(g2d, "BHOJAN-ALOY", width, y, lineHeight, true);
        y = centeredLine(g2d, "Food & Coffee Shop", width, y, lineHeight, false);
        y = divider(g2d, width, y, lineHeight);

        g2d.drawString("Order #: " + order.getOrderNumber(), 0, y); y += lineHeight;
        g2d.drawString("Date   : " + order.getOrderDate(), 0, y); y += lineHeight;
        g2d.drawString("Type   : " + order.getOrderType() + " (" + order.getOrderSource() + ")", 0, y); y += lineHeight;
        if (order.getCustomerName() != null && !order.getCustomerName().isEmpty()) {
            g2d.drawString("Customer: " + order.getCustomerName(), 0, y); y += lineHeight;
            if (order.getCustomerPhone() != null) { g2d.drawString("Phone   : " + order.getCustomerPhone(), 0, y); y += lineHeight; }
            if (order.isDelivery() && order.getCustomerAddress() != null) {
                g2d.drawString("Address : " + order.getCustomerAddress(), 0, y); y += lineHeight;
            }
        }
        y = divider(g2d, width, y, lineHeight);

        List<OrderItem> items = order.getItems();
        if (items != null) {
            for (OrderItem oi : items) {
                String line = String.format("%-22s x%-3d %8s", trim(oi.getItemName(), 22), oi.getQuantity(), "$" + oi.getLineTotal());
                g2d.drawString(line, 0, y);
                y += lineHeight;
            }
        }
        y = divider(g2d, width, y, lineHeight);

        g2d.drawString(String.format("%-22s %14s", "Subtotal", "$" + order.getSubtotal()), 0, y); y += lineHeight;
        g2d.drawString(String.format("%-22s %14s", "Discount", "-$" + order.getDiscount()), 0, y); y += lineHeight;
        g2d.drawString(String.format("%-22s %14s", "Tax", "$" + order.getTaxAmount()), 0, y); y += lineHeight;
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2d.drawString(String.format("%-22s %14s", "TOTAL", "$" + order.getTotalAmount()), 0, y); y += lineHeight;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2d.drawString("Payment: " + order.getPaymentMethod(), 0, y); y += lineHeight;

        y = divider(g2d, width, y, lineHeight);
        y = centeredLine(g2d, "Thank you for visiting Bhojan-Aloy!", width, y, lineHeight, false);
        centeredLine(g2d, "Please come again.", width, y, lineHeight, false);

        return PAGE_EXISTS;
    }

    private int centeredLine(Graphics2D g2d, String text, int width, int y, int lineHeight, boolean bold) {
        Font original = g2d.getFont();
        if (bold) g2d.setFont(original.deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g2d.getFontMetrics();
        int x = Math.max(0, (width - fm.stringWidth(text)) / 2);
        g2d.drawString(text, x, y);
        g2d.setFont(original);
        return y + lineHeight;
    }

    private int divider(Graphics2D g2d, int width, int y, int lineHeight) {
        StringBuilder sb = new StringBuilder();
        int dashCount = width / 6;
        for (int i = 0; i < dashCount; i++) sb.append('-');
        g2d.drawString(sb.toString(), 0, y);
        return y + lineHeight;
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
