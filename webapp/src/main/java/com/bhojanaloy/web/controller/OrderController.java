package com.bhojanaloy.web.controller;

import com.bhojanaloy.dao.MenuItemDAO;
import com.bhojanaloy.dao.OrderDAO;
import com.bhojanaloy.model.MenuItem;
import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import com.bhojanaloy.service.POSService;
import com.bhojanaloy.web.dto.ApiError;
import com.bhojanaloy.web.dto.CartLineRequest;
import com.bhojanaloy.web.dto.CheckoutRequest;
import com.bhojanaloy.web.dto.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    static final java.util.Set<String> PAYMENT_METHODS = java.util.Set.of("CASH", "CARD", "MOBILE_BANKING");
    static final java.util.Set<String> ORDER_TYPES = java.util.Set.of("DINE_IN", "PICKUP", "DELIVERY");

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final POSService posService = new POSService();

    /** Order history — used by the "orders.html" QA test page. */
    @GetMapping
    public List<OrderResponse> recent() {
        return orderDAO.getRecentOrders(50).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    /** Full receipt detail (with line items) for a single order. */
    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable int id) {
        Order order = orderDAO.getOrderById(id);
        if (order == null) {
            return ResponseEntity.status(404).body(new ApiError("Order not found"));
        }
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest req, HttpServletRequest httpRequest) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("Cart is empty"));
        }

        HttpSession session = httpRequest.getSession(false);
        Integer employeeId = (Integer) session.getAttribute("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(new ApiError(
                    "This login has no employee profile attached, so it can't place orders. Use a cashier/manager account."));
        }

        // Re-price every line server-side from the DB — never trust prices sent by the browser.
        List<MenuItem> catalog = menuItemDAO.getAll();
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartLineRequest line : req.getItems()) {
            if (line.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(new ApiError("Quantity must be positive for item " + line.getItemId()));
            }
            MenuItem menuItem = catalog.stream()
                    .filter(m -> m.getItemId() == line.getItemId() && m.isAvailable())
                    .findFirst()
                    .orElse(null);
            if (menuItem == null) {
                return ResponseEntity.badRequest().body(new ApiError("Menu item " + line.getItemId() + " is not available"));
            }
            orderItems.add(new OrderItem(menuItem.getItemId(), menuItem.getItemName(), line.getQuantity(),
                    menuItem.getPrice(), menuItem.getStationName()));
        }

        if (req.getDiscount() != null && req.getDiscount().signum() < 0) {
            // A negative "discount" would silently raise the bill.
            return ResponseEntity.badRequest().body(new ApiError("Discount cannot be negative"));
        }

        String paymentMethod = req.getPaymentMethod() != null ? req.getPaymentMethod() : "CASH";
        String orderType = req.getOrderType() != null ? req.getOrderType() : "DINE_IN";
        // Reject unknown values here with a clear 400, instead of letting MySQL's ENUM check fail
        // the insert and returning its raw SQL error message as a 409.
        if (!PAYMENT_METHODS.contains(paymentMethod)) {
            return ResponseEntity.badRequest().body(new ApiError("Unknown payment method: " + paymentMethod));
        }
        if (!ORDER_TYPES.contains(orderType)) {
            return ResponseEntity.badRequest().body(new ApiError("Unknown order type: " + orderType));
        }
        BigDecimal discount = req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO;

        Order order = posService.buildOrder(employeeId, orderItems, discount, paymentMethod,
                orderType, "ONLINE", req.getCustomerName(), req.getCustomerPhone(), req.getCustomerAddress());

        int orderId = posService.checkout(order);
        if (orderId == -1) {
            String reason = posService.getLastCheckoutError();
            return ResponseEntity.status(409).body(new ApiError(
                    reason != null ? reason : "Checkout failed (possibly insufficient stock)"));
        }

        Order saved = orderDAO.getOrderById(orderId);
        return ResponseEntity.ok(OrderResponse.from(saved));
    }
}
