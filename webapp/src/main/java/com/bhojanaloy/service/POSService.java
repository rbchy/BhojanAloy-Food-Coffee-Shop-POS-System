package com.bhojanaloy.service;

import com.bhojanaloy.dao.OrderDAO;
import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import com.bhojanaloy.model.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Business logic for taking an order: cart totals, tax, order-type/source, and persistence. */
public class POSService {

    public static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% sales tax

    private final OrderDAO orderDAO = new OrderDAO();

    /** Walk-in / dine-in style order (backward-compatible overload). */
    public Order buildOrder(int employeeId, java.util.List<OrderItem> cartItems, BigDecimal discount, String paymentMethod) {
        return buildOrder(employeeId, cartItems, discount, paymentMethod, "DINE_IN", "IN_STORE", null, null, null);
    }

    /**
     * Full order builder supporting walk-in, phone/online, pickup, and delivery orders.
     * @param orderType   DINE_IN, PICKUP, or DELIVERY
     * @param orderSource IN_STORE, PHONE, or ONLINE
     */
    public Order buildOrder(int employeeId, java.util.List<OrderItem> cartItems, BigDecimal discount, String paymentMethod,
                             String orderType, String orderSource,
                             String customerName, String customerPhone, String customerAddress) {
        Order order = new Order();
        order.setOrderNumber(orderDAO.generateOrderNumber());
        order.setEmployeeId(employeeId);
        order.setItems(cartItems);
        order.setDiscount(discount == null ? BigDecimal.ZERO : discount);
        order.setPaymentMethod(paymentMethod);
        order.setOrderType(orderType != null ? orderType : "DINE_IN");
        order.setOrderSource(orderSource != null ? orderSource : "IN_STORE");
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerAddress(customerAddress);
        order.setStatus("COMPLETED");
        order.recalculateTotals(TAX_RATE);
        return order;
    }

    /** Persists a CASH (or otherwise pre-verified) order with no separate gateway payment row. */
    public int checkout(Order order) {
        return orderDAO.placeOrder(order);
    }

    /** Persists the order together with its (simulated) gateway payment transaction. */
    public int checkout(Order order, Payment payment) {
        return orderDAO.placeOrder(order, payment);
    }

    /** The real database error message from the most recent failed checkout, or null if none/succeeded. */
    public String getLastCheckoutError() {
        return orderDAO.getLastError();
    }

    public BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}