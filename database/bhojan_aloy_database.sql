-- =====================================================================
-- BHOJAN-ALOY  |  Food & Coffee Shop POS System
-- Database Schema (MySQL 8.0+)
-- =====================================================================
DROP DATABASE IF EXISTS bhojan_aloy;
CREATE DATABASE bhojan_aloy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bhojan_aloy;

-- ---------------------------------------------------------------------
-- 1. USERS  (login / authentication)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,      -- SHA-256 + per-user salt
    salt           VARCHAR(64)  NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    role           ENUM('ADMIN','MANAGER','CASHIER') NOT NULL DEFAULT 'CASHIER',
    employee_id    INT NULL,
    is_active      BOOLEAN DEFAULT TRUE,
    last_login     DATETIME NULL,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. CATEGORIES
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id    INT AUTO_INCREMENT PRIMARY KEY,
    category_name  VARCHAR(50) NOT NULL UNIQUE,
    description    VARCHAR(255),
    display_order  INT DEFAULT 0,
    station_name   VARCHAR(50) NOT NULL DEFAULT 'General', -- which prep counter/station makes this category
    is_active      BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. MENU ITEMS
-- ---------------------------------------------------------------------
CREATE TABLE menu_items (
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    category_id    INT NOT NULL,
    item_name      VARCHAR(100) NOT NULL,
    description    VARCHAR(255),
    price          DECIMAL(10,2) NOT NULL,
    cost_price     DECIMAL(10,2) DEFAULT 0,
    stock_qty      INT DEFAULT 0,
    reorder_level  INT DEFAULT 10,
    is_available   BOOLEAN DEFAULT TRUE,
    image_path     VARCHAR(255),
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. RAW INGREDIENTS
-- ---------------------------------------------------------------------
CREATE TABLE raw_ingredients (
    ingredient_id   INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_name VARCHAR(100) NOT NULL UNIQUE,
    unit            VARCHAR(20) NOT NULL,        -- g, ml, pcs
    stock_qty       DECIMAL(10,2) DEFAULT 0,
    reorder_level   DECIMAL(10,2) DEFAULT 100,
    unit_cost       DECIMAL(10,2) DEFAULT 0,
    supplier_id     INT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. RECIPES  (how to make each menu item)
-- ---------------------------------------------------------------------
CREATE TABLE recipes (
    recipe_id       INT AUTO_INCREMENT PRIMARY KEY,
    item_id         INT NOT NULL,
    recipe_name     VARCHAR(100) NOT NULL,
    prep_time_min   INT DEFAULT 5,
    difficulty      ENUM('Easy','Medium','Hard') DEFAULT 'Easy',
    instructions    TEXT,
    yield_qty       INT DEFAULT 1,
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. RECIPE INGREDIENTS  (formula)
-- ---------------------------------------------------------------------
CREATE TABLE recipe_ingredients (
    recipe_ing_id   INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id       INT NOT NULL,
    ingredient_id   INT NOT NULL,
    quantity_needed DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id),
    FOREIGN KEY (ingredient_id) REFERENCES raw_ingredients(ingredient_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. EMPLOYEES
-- ---------------------------------------------------------------------
CREATE TABLE employees (
    employee_id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name            VARCHAR(100) NOT NULL,
    designation          VARCHAR(50),
    phone                VARCHAR(20),
    email                VARCHAR(100),
    salary               DECIMAL(10,2),         -- kept for reference/legacy; payroll uses hourly_rate below
    hourly_rate          DECIMAL(10,2) DEFAULT 0,
    payment_preference   ENUM('DIRECT_DEPOSIT','CASH','CHECK') NOT NULL DEFAULT 'CASH',
    bank_name            VARCHAR(100) NULL,
    bank_account_number  VARCHAR(50)  NULL,
    bank_routing_number  VARCHAR(50)  NULL,
    hire_date            DATE,
    is_active            BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

ALTER TABLE users ADD CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

-- ---------------------------------------------------------------------
-- 8. SUPPLIERS
-- ---------------------------------------------------------------------
CREATE TABLE suppliers (
    supplier_id     INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name   VARCHAR(100) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    address         VARCHAR(255)
) ENGINE=InnoDB;

ALTER TABLE raw_ingredients ADD CONSTRAINT fk_ingredient_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id);

-- ---------------------------------------------------------------------
-- 9. PURCHASE ORDERS
-- ---------------------------------------------------------------------
CREATE TABLE purchase_orders (
    po_id           INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id     INT NOT NULL,
    order_date      DATETIME DEFAULT CURRENT_TIMESTAMP,
    status          ENUM('PENDING','RECEIVED','CANCELLED') DEFAULT 'PENDING',
    total_amount    DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
) ENGINE=InnoDB;

CREATE TABLE purchase_order_items (
    po_item_id      INT AUTO_INCREMENT PRIMARY KEY,
    po_id           INT NOT NULL,
    ingredient_id   INT NOT NULL,
    quantity        DECIMAL(10,2) NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id),
    FOREIGN KEY (ingredient_id) REFERENCES raw_ingredients(ingredient_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9a. CUSTOMERS  (loyalty program: 1 point per order, 50 points = 1 free item)
-- ---------------------------------------------------------------------
CREATE TABLE customers (
    customer_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20)  NOT NULL UNIQUE,
    email           VARCHAR(100) NULL,       -- optional; used for "new item" announcement emails
    loyalty_points  INT NOT NULL DEFAULT 0,  -- resets to 0 once a reward is redeemed at 50
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. ORDERS (POS transactions)
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    order_id         INT AUTO_INCREMENT PRIMARY KEY,
    order_number     VARCHAR(30) NOT NULL UNIQUE,
    employee_id      INT NOT NULL,
    customer_id      INT NULL,            -- set when the customer registers for the loyalty program at checkout
    order_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal         DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount         DECIMAL(10,2) DEFAULT 0,
    tax_amount       DECIMAL(10,2) DEFAULT 0,
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method   ENUM('CASH','CARD','MOBILE_BANKING') DEFAULT 'CASH',
    order_type       ENUM('DINE_IN','PICKUP','DELIVERY') NOT NULL DEFAULT 'DINE_IN',
    order_source     ENUM('IN_STORE','PHONE','ONLINE') NOT NULL DEFAULT 'IN_STORE',
    customer_name    VARCHAR(100) NULL,   -- only needed for PHONE/ONLINE/DELIVERY orders
    customer_phone   VARCHAR(20) NULL,
    customer_address VARCHAR(255) NULL,
    loyalty_reward_item VARCHAR(100) NULL, -- name of the item comped free by a 50-point reward, if any
    status           ENUM('COMPLETED','CANCELLED','REFUNDED') DEFAULT 'COMPLETED',
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11. ORDER ITEMS
-- ---------------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id   INT AUTO_INCREMENT PRIMARY KEY,
    order_id        INT NOT NULL,
    item_id         INT NOT NULL,
    quantity        INT NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    line_total      DECIMAL(10,2) NOT NULL,
    station_name    VARCHAR(50) NOT NULL DEFAULT 'General', -- snapshot of category.station_name at order time
    prep_status     ENUM('PENDING','PREPARING','READY','SERVED') NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11a. PAYMENTS  (simulated gateway transactions for Card / Mobile Banking)
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    payment_id        INT AUTO_INCREMENT PRIMARY KEY,
    order_id          INT NOT NULL,
    method            ENUM('CASH','CARD','MOBILE_BANKING') NOT NULL,
    provider          VARCHAR(30) NULL,      -- e.g. Visa, Mastercard, bKash, Nagad, Rocket
    reference_number  VARCHAR(60) NULL,      -- gateway/transaction reference
    amount            DECIMAL(10,2) NOT NULL,
    status            ENUM('SUCCESS','FAILED','PENDING') NOT NULL DEFAULT 'PENDING',
    processed_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11b. DELIVERIES  (third-party agency / rider tracking for DELIVERY orders)
-- ---------------------------------------------------------------------
CREATE TABLE deliveries (
    delivery_id   INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL UNIQUE,
    agency_name   VARCHAR(100) NULL,   -- e.g. Pathao, Foodpanda rider, in-house rider
    rider_name    VARCHAR(100) NULL,
    rider_phone   VARCHAR(20) NULL,
    status        ENUM('UNASSIGNED','ASSIGNED','PICKED_UP','DELIVERED','CANCELLED') NOT NULL DEFAULT 'UNASSIGNED',
    assigned_at   DATETIME NULL,
    picked_up_at  DATETIME NULL,
    delivered_at  DATETIME NULL,
    notes         VARCHAR(255) NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 12. INVENTORY TRANSACTIONS  (stock movement log)
-- ---------------------------------------------------------------------
CREATE TABLE inventory_transactions (
    txn_id          INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id   INT NULL,
    item_id         INT NULL,
    txn_type        ENUM('PURCHASE_IN','SALE_OUT','ADJUSTMENT','WASTE') NOT NULL,
    quantity        DECIMAL(10,2) NOT NULL,
    txn_date        DATETIME DEFAULT CURRENT_TIMESTAMP,
    reference_id    INT NULL,
    notes           VARCHAR(255),
    FOREIGN KEY (ingredient_id) REFERENCES raw_ingredients(ingredient_id),
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 13. DAILY SALES REPORTS (cached summary)
-- ---------------------------------------------------------------------
CREATE TABLE daily_sales_reports (
    report_id       INT AUTO_INCREMENT PRIMARY KEY,
    report_date     DATE NOT NULL UNIQUE,
    total_orders    INT DEFAULT 0,
    total_sales     DECIMAL(10,2) DEFAULT 0,
    total_discount  DECIMAL(10,2) DEFAULT 0,
    total_tax       DECIMAL(10,2) DEFAULT 0,
    net_profit_est  DECIMAL(10,2) DEFAULT 0,
    generated_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 14. TIME CLOCK  (employee clock-in / clock-out)
-- ---------------------------------------------------------------------
CREATE TABLE time_clock (
    clock_id      INT AUTO_INCREMENT PRIMARY KEY,
    employee_id   INT NOT NULL,
    work_date     DATE NOT NULL,
    clock_in      DATETIME NOT NULL,
    clock_out     DATETIME NULL,           -- NULL while the employee is still clocked in
    total_hours   DECIMAL(5,2) NULL,       -- computed on clock-out
    notes         VARCHAR(255) NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 15. HOLIDAYS  (company holiday calendar — drives holiday pay rate)
-- ---------------------------------------------------------------------
CREATE TABLE holidays (
    holiday_id    INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date  DATE NOT NULL UNIQUE,
    holiday_name  VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 16. PAYROLL RUNS  (one row per employee per processed pay period)
-- ---------------------------------------------------------------------
CREATE TABLE payroll_runs (
    payroll_id        INT AUTO_INCREMENT PRIMARY KEY,
    employee_id       INT NOT NULL,
    period_start      DATE NOT NULL,
    period_end        DATE NOT NULL,
    regular_hours     DECIMAL(6,2) DEFAULT 0,
    overtime_hours    DECIMAL(6,2) DEFAULT 0,
    weekend_hours     DECIMAL(6,2) DEFAULT 0,   -- non-overtime hours worked on a weekend day
    holiday_hours     DECIMAL(6,2) DEFAULT 0,   -- non-overtime hours worked on a holiday
    hourly_rate       DECIMAL(10,2) NOT NULL,   -- snapshot of the employee's rate at run time
    gross_pay         DECIMAL(10,2) NOT NULL DEFAULT 0,
    deductions        DECIMAL(10,2) NOT NULL DEFAULT 0,
    net_pay           DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method    ENUM('DIRECT_DEPOSIT','CASH','CHECK') NOT NULL DEFAULT 'CASH',
    payment_reference VARCHAR(60) NULL,         -- simulated bank transfer ref, or blank for cash
    status            ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
    generated_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at           DATETIME NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) ENGINE=InnoDB;

-- =====================================================================
-- VIEWS  (reporting)
-- =====================================================================
CREATE VIEW v_item_sales AS
SELECT mi.item_id, mi.item_name, c.category_name,
       SUM(oi.quantity) AS total_qty_sold,
       SUM(oi.line_total) AS total_revenue
FROM order_items oi
JOIN menu_items mi ON oi.item_id = mi.item_id
JOIN categories c ON mi.category_id = c.category_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status = 'COMPLETED'
GROUP BY mi.item_id, mi.item_name, c.category_name;

CREATE VIEW v_daily_summary AS
SELECT DATE(order_date) AS sale_date,
       COUNT(order_id) AS total_orders,
       SUM(total_amount) AS total_sales,
       SUM(discount) AS total_discount,
       SUM(tax_amount) AS total_tax
FROM orders
WHERE status = 'COMPLETED'
GROUP BY DATE(order_date);

CREATE VIEW v_employee_performance AS
SELECT e.employee_id, e.full_name,
       COUNT(o.order_id) AS orders_handled,
       SUM(o.total_amount) AS total_sales
FROM employees e
LEFT JOIN orders o ON e.employee_id = o.employee_id AND o.status = 'COMPLETED'
GROUP BY e.employee_id, e.full_name;

CREATE VIEW v_low_stock_items AS
SELECT item_id, item_name, stock_qty, reorder_level
FROM menu_items
WHERE stock_qty <= reorder_level AND is_available = TRUE;

CREATE VIEW v_low_stock_ingredients AS
SELECT ingredient_id, ingredient_name, stock_qty, reorder_level, unit
FROM raw_ingredients
WHERE stock_qty <= reorder_level;

CREATE VIEW v_station_queue AS
SELECT oi.order_item_id, o.order_id, o.order_number, oi.item_id, mi.item_name, oi.quantity,
       oi.station_name, oi.prep_status, o.order_date, o.order_type, o.order_source
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
JOIN menu_items mi ON oi.item_id = mi.item_id
WHERE oi.prep_status <> 'SERVED' AND o.status = 'COMPLETED'
ORDER BY o.order_date;

CREATE VIEW v_active_deliveries AS
SELECT d.delivery_id, d.order_id, o.order_number, o.customer_name, o.customer_phone,
       o.customer_address, o.total_amount, d.agency_name, d.rider_name, d.rider_phone,
       d.status, d.assigned_at, d.picked_up_at, d.delivered_at, d.notes
FROM deliveries d
JOIN orders o ON d.order_id = o.order_id
WHERE d.status NOT IN ('DELIVERED','CANCELLED')
ORDER BY o.order_date;

CREATE VIEW v_currently_clocked_in AS
SELECT tc.clock_id, tc.employee_id, e.full_name, e.designation, tc.clock_in
FROM time_clock tc
JOIN employees e ON tc.employee_id = e.employee_id
WHERE tc.clock_out IS NULL
ORDER BY tc.clock_in;

CREATE VIEW v_payroll_history AS
SELECT pr.*, e.full_name
FROM payroll_runs pr
JOIN employees e ON pr.employee_id = e.employee_id
ORDER BY pr.period_end DESC;

-- =====================================================================
-- SAMPLE DATA
-- =====================================================================

-- Categories (station_name = which prep counter handles this category)
INSERT INTO categories (category_name, description, display_order, station_name) VALUES
('Coffee','Hot & cold coffee beverages',1,'Coffee Bar'),
('Bagels','Freshly baked bagels',2,'Bakery'),
('Donuts','Assorted donuts & pastries',3,'Bakery'),
('Sandwiches','Deli sandwiches',4,'Sandwich Station'),
('Beverages','Smoothies, juices & cold drinks',5,'Beverage Station');

-- Suppliers
INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES
('Dhaka Coffee Traders','Karim Rahman','01711000111','karim@dct.com','Motijheel, Dhaka'),
('Fresh Bakery Supplies','Nasrin Akter','01822000222','nasrin@fbs.com','Uttara, Dhaka'),
('Metro Dairy & Grocery','Shahin Islam','01933000333','shahin@metro.com','Gulshan, Dhaka');

-- Raw ingredients
INSERT INTO raw_ingredients (ingredient_name, unit, stock_qty, reorder_level, unit_cost, supplier_id) VALUES
('Espresso Beans','g',5000,500,1.20,1),
('Milk','ml',10000,1000,0.05,3),
('Sugar','g',8000,500,0.03,3),
('Chocolate Syrup','ml',3000,300,0.15,1),
('Flour','g',15000,2000,0.02,2),
('Yeast','g',1000,100,0.30,2),
('Sesame Seeds','g',2000,200,0.10,2),
('Cinnamon','g',1000,100,0.25,2),
('Raisins','g',1500,150,0.20,2),
('Cream Cheese','g',4000,400,0.18,3),
('Ham','g',3000,300,0.35,3),
('Cheese Slice','pcs',500,50,0.40,3),
('Lettuce','g',2000,200,0.06,3),
('Tomato','g',2500,250,0.05,3),
('Water','ml',20000,2000,0.00,3);

-- Menu items
INSERT INTO menu_items (category_id, item_name, description, price, cost_price, stock_qty, reorder_level) VALUES
(1,'Espresso','Rich single shot espresso',2.50,0.80,100,10),
(1,'Americano','Espresso with hot water',3.00,0.90,100,10),
(1,'Cappuccino','Espresso with steamed milk foam',3.50,1.10,100,10),
(1,'Latte','Espresso with steamed milk',3.75,1.15,100,10),
(1,'Mocha','Espresso, chocolate & steamed milk',4.25,1.40,100,10),
(2,'Plain Bagel','Classic soft bagel',2.00,0.60,50,10),
(2,'Sesame Bagel','Bagel topped with sesame seeds',2.25,0.65,50,10),
(2,'Everything Bagel','Bagel with mixed toppings',2.50,0.70,50,10),
(2,'Cinnamon Raisin Bagel','Sweet cinnamon raisin bagel',2.50,0.75,50,10),
(3,'Glazed Donut','Classic glazed donut',1.75,0.50,60,15),
(3,'Chocolate Donut','Chocolate frosted donut',2.00,0.60,60,15),
(4,'Ham & Cheese Sandwich','Ham, cheese, lettuce & tomato',4.50,1.80,40,10),
(4,'Veggie Sandwich','Lettuce, tomato & cheese',4.00,1.50,40,10),
(5,'Mango Smoothie','Fresh mango smoothie',3.50,1.20,30,10),
(5,'Orange Juice','Freshly squeezed orange juice',3.00,1.00,30,10);

-- Employees
INSERT INTO employees (full_name, designation, phone, email, salary, hourly_rate, payment_preference, bank_name, bank_account_number, bank_routing_number, hire_date) VALUES
('Rahim Uddin','Manager','01711111111','rahim@bhojanaloy.com',35000,220.00,'DIRECT_DEPOSIT','Dutch-Bangla Bank','1234567890123','090261234','2023-01-15'),
('Farida Yasmin','Senior Barista','01822222222','farida@bhojanaloy.com',22000,140.00,'DIRECT_DEPOSIT','BRAC Bank','2234567890123','060271234','2023-03-01'),
('Sabbir Ahmed','Cashier','01933333333','sabbir@bhojanaloy.com',18000,110.00,'CASH',NULL,NULL,NULL,'2023-06-10'),
('Nusrat Jahan','Baker','01644444444','nusrat@bhojanaloy.com',20000,125.00,'CASH',NULL,NULL,NULL,'2024-01-05');

-- Users (password for all sample accounts = "admin123", hashed with per-user salt)
-- These are real SHA-256+salt values generated by util.PasswordUtil, so the
-- sample accounts work immediately after import — no manual UPDATE needed.
-- To generate your own (recommended before production use), run:
--   PasswordUtil.java as a Java Application (Eclipse: Run As > Java Application)
-- and copy the printed Salt/Hash into an UPDATE statement, e.g.:
--   UPDATE users SET password_hash='<Hash>', salt='<Salt>' WHERE username='admin';
INSERT INTO users (username, password_hash, salt, full_name, role, employee_id) VALUES
('admin','c9lGa8b8WIujBoIb6qpeodXwF5+4A+4bUXW6jQpdjCo=','Ekihs0CPu28qvXmcPmW8PA==','Rahim Uddin','ADMIN',1),
('farida','c9lGa8b8WIujBoIb6qpeodXwF5+4A+4bUXW6jQpdjCo=','Ekihs0CPu28qvXmcPmW8PA==','Farida Yasmin','MANAGER',2),
('sabbir','c9lGa8b8WIujBoIb6qpeodXwF5+4A+4bUXW6jQpdjCo=','Ekihs0CPu28qvXmcPmW8PA==','Sabbir Ahmed','CASHIER',3);

-- Recipes
INSERT INTO recipes (item_id, recipe_name, prep_time_min, difficulty, instructions, yield_qty) VALUES
(1,'Perfect Espresso',2,'Easy','1. Grind 18g espresso beans fine.\n2. Tamp evenly into portafilter.\n3. Extract for 25-30 seconds into cup.\n4. Serve immediately.',1),
(3,'Cappuccino',5,'Medium','1. Pull a double espresso shot.\n2. Steam milk to microfoam (65C).\n3. Pour steamed milk, then spoon foam on top.\n4. Dust with cocoa if desired.',1),
(6,'Plain Bagels',45,'Hard','1. Mix flour, yeast, sugar and water; knead 10 min.\n2. Proof 1 hour.\n3. Shape rings, boil 30 sec each side.\n4. Bake at 220C for 20 minutes.',12);

-- Recipe ingredients
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity_needed) VALUES
(1,1,18),
(2,1,18),(2,2,150),
(3,5,500),(3,6,10),(3,15,300);

-- Sample completed order (walk-in, dine-in, cash)
INSERT INTO orders (order_number, employee_id, subtotal, discount, tax_amount, total_amount, payment_method, order_type, order_source, status) VALUES
('ORD-20260701-001',3,9.75,0,0.78,10.53,'CASH','DINE_IN','IN_STORE','COMPLETED');

INSERT INTO order_items (order_id, item_id, quantity, unit_price, line_total, station_name, prep_status) VALUES
(1,1,1,2.50,2.50,'Coffee Bar','SERVED'),
(1,6,1,2.00,2.00,'Bakery','SERVED'),
(1,3,1,3.50,3.50,'Coffee Bar','SERVED'),
(1,10,1,1.75,1.75,'Bakery','SERVED');

-- Sample delivery order (phone order, paid by mobile banking, out for delivery)
INSERT INTO orders (order_number, employee_id, subtotal, discount, tax_amount, total_amount, payment_method, order_type, order_source, customer_name, customer_phone, customer_address, status) VALUES
('ORD-20260701-002',3,7.25,0,0.58,7.83,'MOBILE_BANKING','DELIVERY','PHONE','Ayesha Rahman','01712345678','House 12, Road 5, Dhanmondi, Dhaka','COMPLETED');

INSERT INTO order_items (order_id, item_id, quantity, unit_price, line_total, station_name, prep_status) VALUES
(2,4,1,3.75,3.75,'Coffee Bar','READY'),
(2,12,1,4.50,4.50,'Sandwich Station','PREPARING');

INSERT INTO payments (order_id, method, provider, reference_number, amount, status) VALUES
(2,'MOBILE_BANKING','bKash','BKS7Y8X2ZQ',7.83,'SUCCESS');

INSERT INTO deliveries (order_id, agency_name, rider_name, rider_phone, status, assigned_at) VALUES
(2,'In-house Rider','Jasim Uddin','01555666777','ASSIGNED',NOW());

-- Company holiday calendar (drives holiday pay rate in PayrollService)
INSERT INTO holidays (holiday_date, holiday_name) VALUES
('2026-07-01','Bank Holiday'),
('2026-12-16','Victory Day');

-- Sample time clock history for Sabbir Ahmed (employee_id 3) — a completed
-- 9-hour weekday shift (1 hour of daily overtime) and a completed weekend
-- shift, so Payroll Processing has real hours to calculate against.
INSERT INTO time_clock (employee_id, work_date, clock_in, clock_out, total_hours) VALUES
(3,'2026-06-29','2026-06-29 09:00:00','2026-06-29 18:00:00',9.00),
(3,'2026-07-03','2026-07-03 09:00:00','2026-07-03 17:00:00',8.00);

-- Farida Yasmin (employee_id 2) is currently clocked in (no clock_out yet) —
-- demonstrates the "who's clocked in right now" view on the Time Clock screen.
INSERT INTO time_clock (employee_id, work_date, clock_in, clock_out, total_hours) VALUES
(2,CURDATE(),NOW(),NULL,NULL);
