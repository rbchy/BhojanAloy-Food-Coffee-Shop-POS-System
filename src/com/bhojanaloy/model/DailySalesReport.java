package com.bhojanaloy.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesReport {
    private int reportId;
    private LocalDate reportDate;
    private int totalOrders;
    private BigDecimal totalSales;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal netProfitEst;

    public DailySalesReport() {}

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }

    public BigDecimal getTotalTax() { return totalTax; }
    public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }

    public BigDecimal getNetProfitEst() { return netProfitEst; }
    public void setNetProfitEst(BigDecimal netProfitEst) { this.netProfitEst = netProfitEst; }

    @Override
    public String toString() { return reportDate + ": $" + totalSales; }
}
