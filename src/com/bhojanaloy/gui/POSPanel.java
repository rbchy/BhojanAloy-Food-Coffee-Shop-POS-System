package com.bhojanaloy.gui;

import com.bhojanaloy.dao.CategoryDAO;
import com.bhojanaloy.dao.CustomerDAO;
import com.bhojanaloy.dao.MenuItemDAO;
import com.bhojanaloy.model.Category;
import com.bhojanaloy.model.Customer;
import com.bhojanaloy.model.MenuItem;
import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import com.bhojanaloy.model.Payment;
import com.bhojanaloy.service.LoyaltyProgram;
import com.bhojanaloy.service.POSService;
import com.bhojanaloy.util.SessionManager;
import com.bhojanaloy.util.UITheme;
import com.bhojanaloy.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The main order-taking screen: browse menu, build a cart, checkout.
 * Handles walk-in dine-in/pickup as well as staff-entered phone/online
 * orders (with customer name/phone/address), routes each cart line to its
 * prep station, runs CARD/MOBILE_BANKING payments through the simulated
 * gateway, and offers a printed receipt on completion.
 */
public class POSPanel extends JPanel {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final POSService posService = new POSService();

    private final Map<Integer, OrderItem> cart = new LinkedHashMap<>(); // itemId -> OrderItem
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Item", "Qty", "Price", "Total"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);

    private final JPanel itemsGrid = new JPanel(new GridLayout(0, 4, 12, 12));
    private final JLabel subtotalLabel = new JLabel("$0.00");
    private final JLabel taxLabel = new JLabel("$0.00");
    private final JLabel totalLabel = new JLabel("$0.00");
    private final JTextField discountField = new JTextField("0.00", 6);
    private final JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"CASH", "CARD", "MOBILE_BANKING"});

    private final JComboBox<String> orderTypeBox = new JComboBox<>(new String[]{"Dine-in", "Pickup", "Delivery"});
    private final JComboBox<String> orderSourceBox = new JComboBox<>(new String[]{"In-Store", "Phone", "Online"});
    private final JTextField customerNameField = new JTextField();
    private final JTextField customerPhoneField = new JTextField();
    private final JTextField customerAddressField = new JTextField();

    private final JCheckBox loyaltyCheckBox = new JCheckBox("Register / apply loyalty points for this customer");
    private final JTextField loyaltyNameField = new JTextField();
    private final JTextField loyaltyPhoneField = new JTextField();
    private final JTextField loyaltyEmailField = new JTextField();

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(buildCategoryBar(), BorderLayout.NORTH);
        add(new JScrollPane(itemsGrid), BorderLayout.CENTER);
        add(buildCartPanel(), BorderLayout.EAST);

        loadCategory(0); // 0 = all
    }

    private JPanel buildCategoryBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBackground(UITheme.CREAM);
        JButton allBtn = UITheme.secondaryButton("All Items");
        allBtn.addActionListener(e -> loadCategory(0));
        bar.add(allBtn);
        for (Category c : categoryDAO.getAllActive()) {
            JButton b = UITheme.secondaryButton(c.getCategoryName());
            b.addActionListener(e -> loadCategory(c.getCategoryId()));
            bar.add(b);
        }
        return bar;
    }

    private void loadCategory(int categoryId) {
        itemsGrid.removeAll();
        List<MenuItem> items = categoryId == 0 ? menuItemDAO.getAll() : menuItemDAO.getByCategory(categoryId);
        for (MenuItem item : items) {
            if (!item.isAvailable()) continue;
            itemsGrid.add(buildItemCard(item));
        }
        itemsGrid.revalidate();
        itemsGrid.repaint();
    }

    private JPanel buildItemCard(MenuItem item) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(UITheme.WHITE);
        card.setBorder(BorderFactory.createLineBorder(UITheme.CREAM_DARKER, 1));
        card.setPreferredSize(new Dimension(150, 160));

        String imageKey = item.getStationName() != null ? item.getStationName() : item.getCategoryName();
        JLabel imageLabel = com.bhojanaloy.util.ImageUtil.buildItemImageLabel(item.getImagePath(), imageKey, 64);
        imageLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel name = new JLabel("<html><center>" + item.getItemName() + "</center></html>", SwingConstants.CENTER);
        name.setFont(UITheme.FONT_LABEL_B);
        name.setForeground(UITheme.TEXT_DARK);

        JLabel price = new JLabel("$" + item.getPrice(), SwingConstants.CENTER);
        price.setFont(UITheme.FONT_LABEL);
        price.setForeground(UITheme.ACCENT_ORANGE);

        JLabel stock = new JLabel(item.isLowStock() ? "Low stock: " + item.getStockQty() : "In stock: " + item.getStockQty(), SwingConstants.CENTER);
        stock.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        stock.setForeground(item.isLowStock() ? UITheme.ACCENT_RED : UITheme.COFFEE_LIGHT);

        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        textPanel.setOpaque(false);
        textPanel.add(name);
        textPanel.add(price);
        textPanel.add(stock);

        card.add(imageLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { addToCart(item); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(UITheme.CREAM_DARKER); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { card.setBackground(UITheme.WHITE); }
        });
        return card;
    }

    private void addToCart(MenuItem item) {
        if (item.getStockQty() <= 0) {
            JOptionPane.showMessageDialog(this, item.getItemName() + " is out of stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        OrderItem existing = cart.get(item.getItemId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cart.put(item.getItemId(), new OrderItem(item.getItemId(), item.getItemName(), 1, item.getPrice(),
                    item.getStationName() != null ? item.getStationName() : "General"));
        }
        refreshCartTable();
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.CREAM_DARKER, 1), new EmptyBorder(15, 15, 15, 15)));

        JLabel heading = UITheme.heading("Current Order");
        UITheme.styleTable(cartTable);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setPreferredSize(new Dimension(380, 200));

        JButton removeBtn = UITheme.dangerButton("Remove Selected");
        removeBtn.addActionListener(e -> removeSelected());

        JPanel totalsPanel = new JPanel(new GridLayout(4, 2, 4, 6));
        totalsPanel.setOpaque(false);
        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("Discount ($):"));
        totalsPanel.add(discountField);
        totalsPanel.add(new JLabel("Tax (8%):"));
        totalsPanel.add(taxLabel);
        JLabel totalCaption = new JLabel("TOTAL:");
        totalCaption.setFont(UITheme.FONT_HEADING);
        totalLabel.setFont(UITheme.FONT_HEADING);
        totalLabel.setForeground(UITheme.ACCENT_ORANGE);
        totalsPanel.add(totalCaption);
        totalsPanel.add(totalLabel);

        discountField.addActionListener(e -> recalcTotals());
        discountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { recalcTotals(); }
        });

        JPanel orderInfoPanel = buildOrderInfoPanel();
        JPanel loyaltyPanel = buildLoyaltyPanel();

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.setOpaque(false);
        paymentPanel.add(new JLabel("Payment Method:"));
        paymentPanel.add(paymentMethodBox);

        JButton checkoutBtn = UITheme.successButton("CHECKOUT");
        checkoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutBtn.addActionListener(e -> checkout());

        JButton clearBtn = UITheme.secondaryButton("Clear Cart");
        clearBtn.addActionListener(e -> {
            cart.clear();
            refreshCartTable();
        });

        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        bottomButtons.setOpaque(false);
        bottomButtons.add(clearBtn);
        bottomButtons.add(removeBtn);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(false);
        south.add(totalsPanel);
        south.add(Box.createVerticalStrut(6));
        south.add(orderInfoPanel);
        south.add(Box.createVerticalStrut(6));
        south.add(loyaltyPanel);
        south.add(paymentPanel);
        south.add(Box.createVerticalStrut(8));
        south.add(checkoutBtn);
        south.add(Box.createVerticalStrut(6));
        south.add(bottomButtons);

        JScrollPane southScroll = new JScrollPane(south);
        southScroll.setBorder(null);
        southScroll.getVerticalScrollBar().setUnitIncrement(16);
        // Without a bounded preferred size, a JScrollPane asks for its full content
        // height, which — now that Order Type/Source + Loyalty Program + Payment are
        // all stacked here — would squeeze the Current Order (cart items) area above
        // it down to almost nothing. Cap it so the cart stays visible and this section
        // scrolls internally instead.
        southScroll.setPreferredSize(new Dimension(380, 320));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southScroll, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildOrderInfoPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.CREAM_DARKER), "Order Type / Source",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UITheme.FONT_LABEL_B));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 4));
        row1.setOpaque(false);
        JPanel typeCol = new JPanel(new BorderLayout());
        typeCol.setOpaque(false);
        typeCol.add(new JLabel("Order Type:"), BorderLayout.NORTH);
        typeCol.add(orderTypeBox, BorderLayout.CENTER);
        JPanel sourceCol = new JPanel(new BorderLayout());
        sourceCol.setOpaque(false);
        sourceCol.add(new JLabel("Order Source:"), BorderLayout.NORTH);
        sourceCol.add(orderSourceBox, BorderLayout.CENTER);
        row1.add(typeCol);
        row1.add(sourceCol);

        JPanel customerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        customerPanel.setOpaque(false);
        customerPanel.setBorder(new EmptyBorder(6, 0, 0, 0));
        customerPanel.add(new JLabel("Customer Name (Phone/Online/Delivery):"));
        customerPanel.add(customerNameField);
        customerPanel.add(new JLabel("Customer Phone:"));
        customerPanel.add(customerPhoneField);
        customerPanel.add(new JLabel("Delivery Address:"));
        customerPanel.add(customerAddressField);

        p.add(row1);
        p.add(customerPanel);
        return p;
    }

    private JPanel buildLoyaltyPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.CREAM_DARKER), "Loyalty Program (1 pt/order, 50 pts = 1 free item)",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UITheme.FONT_LABEL_B));

        loyaltyCheckBox.setOpaque(false);
        loyaltyCheckBox.setFont(UITheme.FONT_LABEL);

        JPanel fields = new JPanel(new GridLayout(0, 1, 2, 2));
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(6, 0, 0, 0));
        fields.add(new JLabel("Customer Name:"));
        fields.add(loyaltyNameField);
        fields.add(new JLabel("Phone:"));
        fields.add(loyaltyPhoneField);
        fields.add(new JLabel("Email (optional — for new-item announcements):"));
        fields.add(loyaltyEmailField);

        setLoyaltyFieldsEnabled(false);
        loyaltyCheckBox.addActionListener(e -> setLoyaltyFieldsEnabled(loyaltyCheckBox.isSelected()));

        p.add(loyaltyCheckBox);
        p.add(fields);
        return p;
    }

    private void setLoyaltyFieldsEnabled(boolean enabled) {
        loyaltyNameField.setEnabled(enabled);
        loyaltyPhoneField.setEnabled(enabled);
        loyaltyEmailField.setEnabled(enabled);
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;
        List<Integer> keys = new ArrayList<>(cart.keySet());
        cart.remove(keys.get(row));
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (OrderItem oi : cart.values()) {
            cartModel.addRow(new Object[]{oi.getItemName(), oi.getQuantity(), "$" + oi.getUnitPrice(), "$" + oi.getLineTotal()});
        }
        recalcTotals();
    }

    private void recalcTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem oi : cart.values()) subtotal = subtotal.add(oi.getLineTotal());

        BigDecimal discount;
        try {
            discount = new BigDecimal(discountField.getText().trim());
        } catch (NumberFormatException ex) {
            discount = BigDecimal.ZERO;
        }
        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal tax = taxable.multiply(POSService.TAX_RATE);
        BigDecimal total = taxable.add(tax);

        subtotalLabel.setText("$" + posService.round(subtotal));
        taxLabel.setText("$" + posService.round(tax));
        totalLabel.setText("$" + posService.round(total));
    }

    private String mapOrderType(String uiValue) {
        switch (uiValue) {
            case "Pickup": return "PICKUP";
            case "Delivery": return "DELIVERY";
            default: return "DINE_IN";
        }
    }

    private String mapOrderSource(String uiValue) {
        switch (uiValue) {
            case "Phone": return "PHONE";
            case "Online": return "ONLINE";
            default: return "IN_STORE";
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.", "Nothing to checkout", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderType = mapOrderType((String) orderTypeBox.getSelectedItem());
        String orderSource = mapOrderSource((String) orderSourceBox.getSelectedItem());
        String customerName = customerNameField.getText().trim();
        String customerPhone = customerPhoneField.getText().trim();
        String customerAddress = customerAddressField.getText().trim();

        boolean needsCustomerInfo = !orderSource.equals("IN_STORE") || orderType.equals("DELIVERY");
        if (needsCustomerInfo && (customerName.isEmpty() || customerPhone.isEmpty())) {
            JOptionPane.showMessageDialog(this,
                    "Phone/Online/Delivery orders need at least a customer name and phone number.",
                    "Missing Customer Info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (needsCustomerInfo && !customerPhone.isEmpty() && !ValidationUtil.isValidPhone(customerPhone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number (7-15 digits).",
                    "Invalid Phone", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (orderType.equals("DELIVERY") && customerAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a delivery address.", "Missing Address", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Loyalty program: validate fields (if the cashier checked the box) ---
        String loyaltyName = loyaltyNameField.getText().trim();
        String loyaltyPhone = loyaltyPhoneField.getText().trim();
        String loyaltyEmail = loyaltyEmailField.getText().trim();
        if (loyaltyCheckBox.isSelected()) {
            if (!ValidationUtil.isNonEmpty(loyaltyName) || !ValidationUtil.isNonEmpty(loyaltyPhone)) {
                JOptionPane.showMessageDialog(this, "Enter the customer's name and phone number to register loyalty points.",
                        "Missing Loyalty Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidPhone(loyaltyPhone)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid loyalty phone number (7-15 digits).",
                        "Invalid Phone", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!loyaltyEmail.isEmpty() && !ValidationUtil.isValidEmail(loyaltyEmail)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address, or leave it blank.",
                        "Invalid Email", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        BigDecimal manualDiscount = ValidationUtil.parseNonNegativeDecimal(discountField.getText());
        if (manualDiscount == null) manualDiscount = BigDecimal.ZERO;

        // --- Loyalty program: find/create the customer, decide whether this order earns a free-item reward ---
        Customer loyaltyCustomer = null;
        boolean rewardApplied = false;
        int pointsAfterOrder = 0;
        BigDecimal loyaltyDiscount = BigDecimal.ZERO;
        String rewardItemName = null;
        if (loyaltyCheckBox.isSelected()) {
            loyaltyCustomer = customerDAO.findByPhone(loyaltyPhone);
            if (loyaltyCustomer == null) {
                Customer newCustomer = new Customer(loyaltyName, loyaltyPhone, loyaltyEmail.isEmpty() ? null : loyaltyEmail);
                int newId = customerDAO.createCustomer(newCustomer);
                if (newId <= 0) {
                    String detail = customerDAO.getLastError();
                    String msg = "Could not register the loyalty customer (database error). "
                            + "Checkout will continue without applying loyalty points.";
                    if (detail != null && !detail.isEmpty()) {
                        msg += "\n\nDetails: " + detail;
                        if (detail.toLowerCase().contains("doesn't exist") || detail.toLowerCase().contains("unknown column")) {
                            msg += "\n\nThis usually means the database is missing the loyalty-program update — "
                                    + "run database/migration_v4_loyalty.sql (or re-import the full schema) and try again.";
                        }
                    }
                    JOptionPane.showMessageDialog(this, msg, "Loyalty Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    newCustomer.setCustomerId(newId);
                    loyaltyCustomer = newCustomer;
                }
            }
            if (loyaltyCustomer != null) {
                LoyaltyProgram.Decision loyalty = LoyaltyProgram.decide(loyaltyCustomer.getLoyaltyPoints(), cart.values());
                pointsAfterOrder = loyalty.pointsAfterOrder;
                if (loyalty.rewardApplied()) {
                    loyaltyDiscount = loyalty.rewardDiscount;
                    rewardItemName = loyalty.freeItem.getItemName();
                    rewardApplied = true;
                }
            }
        }

        BigDecimal combinedDiscount = manualDiscount.add(loyaltyDiscount);
        int employeeId = SessionManager.getCurrentUser().getEmployeeId() != null
                ? SessionManager.getCurrentUser().getEmployeeId() : 1;
        String paymentMethod = (String) paymentMethodBox.getSelectedItem();

        Order order = posService.buildOrder(employeeId, new ArrayList<>(cart.values()), combinedDiscount, paymentMethod,
                orderType, orderSource,
                customerName.isEmpty() ? null : customerName,
                customerPhone.isEmpty() ? null : customerPhone,
                customerAddress.isEmpty() ? null : customerAddress);
        if (loyaltyCustomer != null) {
            order.setCustomerId(loyaltyCustomer.getCustomerId());
            order.setLoyaltyRewardItem(rewardItemName);
        }

        Payment payment;
        if (paymentMethod.equals("CASH")) {
            payment = new Payment();
            payment.setMethod("CASH");
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("SUCCESS");
        } else {
            PaymentDialog dialog = new PaymentDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    paymentMethod, order.getTotalAmount());
            dialog.setVisible(true);
            payment = dialog.getResult();
            if (payment == null) {
                // Cancelled, or never completed successfully — don't save the order.
                return;
            }
        }

        int orderId = posService.checkout(order, payment);
        if (orderId > 0) {
            order.setOrderId(orderId);
            order.setOrderDate(LocalDateTime.now());

            if (loyaltyCustomer != null) {
                customerDAO.updateLoyaltyPoints(loyaltyCustomer.getCustomerId(), pointsAfterOrder);
                if (rewardApplied) {
                    JOptionPane.showMessageDialog(this,
                            "🎉 Loyalty reward! " + rewardItemName + " is FREE on this order — points reset to 0.",
                            "Loyalty Reward Redeemed", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            if (order.isDelivery()) {
                JOptionPane.showMessageDialog(this,
                        "Order placed! Added to Delivery queue — assign a rider in the Delivery screen.",
                        "Order Complete", JOptionPane.INFORMATION_MESSAGE);
            }

            // Show the item-wise bill on screen first; the cashier/customer then
            // decides whether to print it or send it electronically (Email/SMS).
            BillPreviewDialog billDialog = new BillPreviewDialog((Frame) SwingUtilities.getWindowAncestor(this), order);
            billDialog.setVisible(true);

            cart.clear();
            discountField.setText("0.00");
            customerNameField.setText("");
            customerPhoneField.setText("");
            customerAddressField.setText("");
            orderTypeBox.setSelectedIndex(0);
            orderSourceBox.setSelectedIndex(0);
            loyaltyCheckBox.setSelected(false);
            loyaltyNameField.setText("");
            loyaltyPhoneField.setText("");
            loyaltyEmailField.setText("");
            setLoyaltyFieldsEnabled(false);
            refreshCartTable();
            loadCategory(0);
        } else {
            String detail = posService.getLastCheckoutError();
            String msg = "Checkout failed. Check stock levels and database connection.";
            if (detail != null && !detail.isEmpty()) {
                msg += "\n\nDetails: " + detail;
                if (detail.toLowerCase().contains("unknown column")) {
                    msg += "\n\nThis usually means the database is missing a recent update — "
                            + "run database/migration_v4_loyalty.sql (or re-import the full schema) and try again.";
                }
            }
            JOptionPane.showMessageDialog(this, msg, "Checkout Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
