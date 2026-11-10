package com.bhojanaloy.util;

import com.bhojanaloy.model.PayrollRun;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/** Prints a formatted employee pay stub using java.awt.print — same approach as ReceiptPrinter. */
public class PayStubPrinter implements Printable {

    private final PayrollRun run;

    public PayStubPrinter(PayrollRun run) {
        this.run = run;
    }

    public static void printPayStub(PayrollRun run, Component parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new PayStubPrinter(run));
        job.setJobName("Bhojan-Aloy Pay Stub - " + run.getEmployeeName());
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
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));

        int lineHeight = 16;
        int y = lineHeight;
        int width = (int) pageFormat.getImageableWidth();

        y = centeredLine(g2d, "BHOJAN-ALOY", width, y, lineHeight);
        y = centeredLine(g2d, "Employee Pay Stub", width, y, lineHeight);
        y = divider(g2d, width, y, lineHeight);

        g2d.drawString("Employee : " + run.getEmployeeName(), 0, y); y += lineHeight;
        g2d.drawString("Period   : " + run.getPeriodStart() + "  to  " + run.getPeriodEnd(), 0, y); y += lineHeight;
        g2d.drawString("Rate     : $" + run.getHourlyRate() + " / hour", 0, y); y += lineHeight;
        y = divider(g2d, width, y, lineHeight);

        g2d.drawString(String.format("%-22s %10s", "Regular Hours", run.getRegularHours()), 0, y); y += lineHeight;
        g2d.drawString(String.format("%-22s %10s", "Overtime Hours", run.getOvertimeHours()), 0, y); y += lineHeight;
        g2d.drawString(String.format("%-22s %10s", "Weekend Hours", run.getWeekendHours()), 0, y); y += lineHeight;
        g2d.drawString(String.format("%-22s %10s", "Holiday Hours", run.getHolidayHours()), 0, y); y += lineHeight;
        y = divider(g2d, width, y, lineHeight);

        g2d.drawString(String.format("%-22s %10s", "Gross Pay", "$" + run.getGrossPay()), 0, y); y += lineHeight;
        g2d.drawString(String.format("%-22s %10s", "Deductions", "-$" + run.getDeductions()), 0, y); y += lineHeight;
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.drawString(String.format("%-22s %10s", "NET PAY", "$" + run.getNetPay()), 0, y); y += lineHeight;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));

        g2d.drawString("Payment Method: " + run.getPaymentMethod(), 0, y); y += lineHeight;
        if (run.getPaymentReference() != null) {
            g2d.drawString("Reference     : " + run.getPaymentReference(), 0, y); y += lineHeight;
        }
        g2d.drawString("Status        : " + run.getStatus(), 0, y); y += lineHeight;

        y = divider(g2d, width, y, lineHeight);
        centeredLine(g2d, "Thank you for your hard work!", width, y, lineHeight);

        return PAGE_EXISTS;
    }

    private int centeredLine(Graphics2D g2d, String text, int width, int y, int lineHeight) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = Math.max(0, (width - fm.stringWidth(text)) / 2);
        g2d.drawString(text, x, y);
        return y + lineHeight;
    }

    private int divider(Graphics2D g2d, int width, int y, int lineHeight) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width / 6; i++) sb.append('-');
        g2d.drawString(sb.toString(), 0, y);
        return y + lineHeight;
    }
}
