package com.bhojanaloy.dao;

import com.bhojanaloy.config.DBConnection;
import com.bhojanaloy.model.Customer;
import com.bhojanaloy.model.MenuItem;
import com.bhojanaloy.model.Order;
import com.bhojanaloy.model.OrderItem;
import com.bhojanaloy.model.Payment;
import com.bhojanaloy.model.User;
import com.bhojanaloy.service.POSService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs against a real MySQL loaded with database/bhojan_aloy_database.sql (CI does this).
 * Skipped when DB_URL is not set, so "mvn verify" on a laptop without MySQL still passes.
 */
class DatabaseIT {

    private final POSService pos = new POSService();
    private final MenuItemDAO menu = new MenuItemDAO();
    private final OrderDAO orders = new OrderDAO();

    @BeforeAll
    static void needsADatabase() {
        String url = System.getenv("DB_URL");
        assumeTrue(url != null && !url.isEmpty(), "DB_URL not set - skipping database tests");
        assertTrue(DBConnection.testConnection(), "cannot reach " + DBConnection.url());
    }

    private static long count(String sql, Object... params) throws Exception {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private MenuItem menuItem(String name) {
        return menu.getAll().stream().filter(m -> m.getItemName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("sample menu item " + name + " missing"));
    }


    // ---------------------------------------------------------------- login

    @Test
    void theSampleAccountsLogInWithAdmin123() {
        UserDAO users = new UserDAO();
        User admin = users.authenticate("admin", "admin123");
        assertNotNull(admin, "admin / admin123");
        assertEquals("ADMIN", admin.getRole());
        assertEquals("CASHIER", users.authenticate("sabbir", "admin123").getRole());
        assertNull(users.authenticate("admin", "wrong"));
        assertNull(users.authenticate("nobody", "admin123"));
        assertNull(users.authenticate("admin' OR '1'='1", "x"), "SQL injection must not log in");
    }

    @Test
    void aNewUserCanBeCreatedAndLogIn() {
        UserDAO users = new UserDAO();
        String name = "it_user_" + System.nanoTime();
        assertTrue(users.createUser(name, "Str0ng!pass", "IT User", "CASHIER", null));
        assertNotNull(users.authenticate(name, "Str0ng!pass"));
        assertNull(users.authenticate(name, "str0ng!pass"));
    }

    // ---------------------------------------------------------------- checkout

    @Test
    void checkoutSavesTheOrderDeductsStockAndLogsEverythingInOneGo() throws Exception {
        MenuItem latte = menuItem("Latte");
        MenuItem bagel = menuItem("Plain Bagel");
        int latteBefore = latte.getStockQty(), bagelBefore = bagel.getStockQty();

        Order order = pos.buildOrder(1,
                List.of(new OrderItem(latte.getItemId(), latte.getItemName(), 2, latte.getPrice(), "Coffee Bar"),
                        new OrderItem(bagel.getItemId(), bagel.getItemName(), 1, bagel.getPrice(), "Bakery")),
                new BigDecimal("0.50"), "CARD", "DELIVERY", "PHONE", "IT Customer", "01700000000", "Road 1");
        Payment payment = new Payment();
        payment.setMethod("CARD");
        payment.setProvider("Visa");
        payment.setReferenceNumber("IT-" + System.nanoTime());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus("SUCCESS");

        int id = pos.checkout(order, payment);
        assertTrue(id > 0, "checkout failed: " + pos.getLastCheckoutError());

        assertEquals(latteBefore - 2, menuItem("Latte").getStockQty());
        assertEquals(bagelBefore - 1, menuItem("Plain Bagel").getStockQty());
        assertEquals(2, count("SELECT COUNT(*) FROM order_items WHERE order_id = ?", id));
        assertEquals(2, count("SELECT COUNT(*) FROM inventory_transactions WHERE reference_id = ? AND txn_type = 'SALE_OUT'", id));
        assertEquals(1, count("SELECT COUNT(*) FROM payments WHERE order_id = ? AND status = 'SUCCESS'", id));
        assertEquals(1, count("SELECT COUNT(*) FROM deliveries WHERE order_id = ? AND status = 'UNASSIGNED'", id));

        Order saved = orders.getOrderById(id);
        assertEquals(order.getSubtotal(), saved.getSubtotal());
        assertEquals(order.getTaxAmount(), saved.getTaxAmount());
        assertEquals(order.getTotalAmount(), saved.getTotalAmount(), "what the customer paid = what was stored");
        assertEquals(2, saved.getItems().size());
    }

    @Test
    void insufficientStockRollsBackTheWholeOrder() throws Exception {
        MenuItem espresso = menuItem("Espresso");
        MenuItem mocha = menuItem("Mocha");
        int espressoBefore = espresso.getStockQty(), mochaBefore = mocha.getStockQty();
        long ordersBefore = count("SELECT COUNT(*) FROM orders");

        // The first line is fine; the second asks for more than is in stock.
        Order order = pos.buildOrder(1,
                List.of(new OrderItem(espresso.getItemId(), espresso.getItemName(), 1, espresso.getPrice()),
                        new OrderItem(mocha.getItemId(), mocha.getItemName(), mochaBefore + 1, mocha.getPrice())),
                BigDecimal.ZERO, "CASH");

        assertEquals(-1, pos.checkout(order));
        assertTrue(pos.getLastCheckoutError().contains("Insufficient stock for item: Mocha"), pos.getLastCheckoutError());
        assertEquals(espressoBefore, menuItem("Espresso").getStockQty(), "the espresso deduction must be rolled back");
        assertEquals(mochaBefore, menuItem("Mocha").getStockQty());
        assertEquals(ordersBefore, count("SELECT COUNT(*) FROM orders"), "no half-saved order");
    }

    @Test
    void manyOrdersInARowGetUniqueOrderNumbers() throws Exception {
        MenuItem americano = menuItem("Americano");
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            Order o = pos.buildOrder(1, List.of(new OrderItem(americano.getItemId(), americano.getItemName(), 1,
                    americano.getPrice())), BigDecimal.ZERO, "CASH");
            assertTrue(pos.checkout(o) > 0, "order " + i + ": " + pos.getLastCheckoutError());
            assertTrue(numbers.add(o.getOrderNumber()), "duplicate " + o.getOrderNumber());
            assertTrue(o.getOrderNumber().length() <= 30, "orders.order_number is VARCHAR(30)");
        }
        for (int i = 0; i < 5000; i++) {
            assertTrue(numbers.add(orders.generateOrderNumber()), "generator repeated itself");
        }
    }

    // ---------------------------------------------------------------- loyalty

    @Test
    void aLoyaltyCustomerIsFoundByPhoneAndKeepsTheirPoints() {
        CustomerDAO customers = new CustomerDAO();
        String phone = "017" + (System.nanoTime() % 100000000L);
        Customer c = new Customer("IT Loyal", phone, "loyal@example.com");
        int id = customers.createCustomer(c);
        assertTrue(id > 0, customers.getLastError());
        assertTrue(customers.updateLoyaltyPoints(id, 49));
        Customer found = customers.findByPhone(phone);
        assertNotNull(found);
        assertEquals(49, found.getLoyaltyPoints());
        assertNull(customers.findByPhone("000"));
    }

    // ---------------------------------------------------------------- recipes (5 languages)

    @Test
    void recipesKeepAllFiveLanguagesOnAFreshInstall() {
        RecipeDAO recipes = new RecipeDAO();
        com.bhojanaloy.model.Recipe cappuccino = recipes.getAll().stream()
                .filter(r -> r.getRecipeName().equals("Cappuccino")).findFirst()
                .orElseThrow(() -> new AssertionError("sample Cappuccino recipe missing"));
        assertNotNull(cappuccino.getInstructionsBn(), "Bangla steps must be in the full schema, not only in migration_v5");
        assertTrue(cappuccino.getInstructionsBn().contains("এসপ্রেসো"), cappuccino.getInstructionsBn());

        com.bhojanaloy.model.Recipe r = new com.bhojanaloy.model.Recipe();
        r.setItemId(menuItem("Mocha").getItemId());
        r.setRecipeName("IT Mocha " + System.nanoTime());
        r.setDifficulty("Easy");
        r.setPrepTimeMin(4);
        r.setYieldQty(1);
        r.setInstructions("1. Pull a shot.");
        r.setInstructionsBn("১. একটি শট নিন।");
        r.setInstructionsAr("1. اسحب جرعة.");
        assertTrue(recipes.addRecipe(r), "recipe insert failed - see the SQL error above");
        com.bhojanaloy.model.Recipe back = recipes.getAll().stream()
                .filter(x -> x.getRecipeName().equals(r.getRecipeName())).findFirst().orElseThrow();
        assertEquals("১. একটি শট নিন।", back.getInstructionsBn(), "utf8mb4 round trip");
        assertEquals("1. اسحب جرعة.", back.getInstructionsAr());
    }

    // ---------------------------------------------------------------- schema vs code

    @Test
    void allNineReportingViewsRun() throws Exception {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            for (String v : List.of("v_item_sales", "v_daily_summary", "v_employee_performance", "v_low_stock_items",
                    "v_low_stock_ingredients", "v_station_queue", "v_active_deliveries", "v_currently_clocked_in",
                    "v_payroll_history")) {
                try (ResultSet rs = st.executeQuery("SELECT * FROM " + v + " LIMIT 1")) {
                    assertTrue(rs.getMetaData().getColumnCount() > 0, v);
                } catch (Exception e) {
                    fail(v + ": " + e.getMessage());
                }
            }
        }
        assertFalse(new ReportDAO().getItemSales().isEmpty(), "sample orders should show up in item sales");
    }

    /**
     * The DAOs catch SQLException and print it, so a column the code expects but the schema lacks
     * would only show up as an empty screen. Call every no-argument read method of every DAO and
     * fail if any of them printed a SQL error.
     */
    @Test
    void everyDaoReadMethodMatchesTheSchema() throws Exception {
        List<Class<?>> daos = List.of(CategoryDAO.class, CustomerDAO.class, DeliveryDAO.class, EmployeeDAO.class,
                HolidayDAO.class, MenuItemDAO.class, OrderDAO.class, PaymentDAO.class, PayrollDAO.class,
                RawIngredientDAO.class, RecipeDAO.class, ReportDAO.class, SupplierDAO.class, TimeClockDAO.class,
                UserDAO.class);
        PrintStream originalErr = System.err;
        StringBuilder failures = new StringBuilder();
        int called = 0;
        for (Class<?> dao : daos) {
            Object instance = dao.getDeclaredConstructor().newInstance();
            for (Method m : dao.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers()) || m.getParameterCount() != 0) continue;
                if (!(List.class.isAssignableFrom(m.getReturnType()) || Map.class.isAssignableFrom(m.getReturnType()))) continue;
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                System.setErr(new PrintStream(err, true, "UTF-8"));
                try {
                    m.invoke(instance);
                } finally {
                    System.setErr(originalErr);
                }
                called++;
                String printed = err.toString("UTF-8");
                if (printed.contains("SQL")) {
                    failures.append(dao.getSimpleName()).append('.').append(m.getName()).append(": ")
                            .append(printed.lines().findFirst().orElse("")).append('\n');
                }
            }
        }
        assertTrue(called >= 15, "only " + called + " DAO methods found");
        assertEquals("", failures.toString());
    }
}
