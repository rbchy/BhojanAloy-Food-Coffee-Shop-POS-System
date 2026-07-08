-- =====================================================================
-- BHOJAN-ALOY — Migration v2
-- Adds: kitchen/station routing, payment gateway records, delivery
-- tracking, and order type/source + customer info — WITHOUT dropping
-- your existing database or losing existing orders.
--
-- Run this only if you already imported the original schema and have
-- data you want to keep. If you're starting fresh, just re-import the
-- full bhojan_aloy_database.sql instead (it already includes everything
-- below).
--
-- Usage:  mysql -u root -p bhojan_aloy < migration_v2_stations_payments_delivery.sql
-- =====================================================================
USE bhojan_aloy;

-- 1. Add station routing to categories
ALTER TABLE categories
    ADD COLUMN station_name VARCHAR(50) NOT NULL DEFAULT 'General' AFTER display_order;

UPDATE categories SET station_name = 'Coffee Bar'        WHERE category_name = 'Coffee';
UPDATE categories SET station_name = 'Bakery'            WHERE category_name IN ('Bagels','Donuts');
UPDATE categories SET station_name = 'Sandwich Station'  WHERE category_name = 'Sandwiches';
UPDATE categories SET station_name = 'Beverage Station'  WHERE category_name = 'Beverages';

-- 2. Extend orders with type/source/customer info
ALTER TABLE orders
    ADD COLUMN order_type ENUM('DINE_IN','PICKUP','DELIVERY') NOT NULL DEFAULT 'DINE_IN' AFTER payment_method,
    ADD COLUMN order_source ENUM('IN_STORE','PHONE','ONLINE') NOT NULL DEFAULT 'IN_STORE' AFTER order_type,
    ADD COLUMN customer_name VARCHAR(100) NULL AFTER order_source,
    ADD COLUMN customer_phone VARCHAR(20) NULL AFTER customer_name,
    ADD COLUMN customer_address VARCHAR(255) NULL AFTER customer_phone;

-- 3. Extend order_items with station routing + prep status
ALTER TABLE order_items
    ADD COLUMN station_name VARCHAR(50) NOT NULL DEFAULT 'General' AFTER line_total,
    ADD COLUMN prep_status ENUM('PENDING','PREPARING','READY','SERVED') NOT NULL DEFAULT 'PENDING' AFTER station_name;

-- Backfill station_name on existing order_items from their category
UPDATE order_items oi
JOIN menu_items mi ON oi.item_id = mi.item_id
JOIN categories c ON mi.category_id = c.category_id
SET oi.station_name = c.station_name;

-- Mark all pre-existing order items as SERVED (they were already sold before this feature existed)
UPDATE order_items SET prep_status = 'SERVED' WHERE prep_status = 'PENDING';

-- 4. New payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id        INT AUTO_INCREMENT PRIMARY KEY,
    order_id          INT NOT NULL,
    method            ENUM('CASH','CARD','MOBILE_BANKING') NOT NULL,
    provider          VARCHAR(30) NULL,
    reference_number  VARCHAR(60) NULL,
    amount            DECIMAL(10,2) NOT NULL,
    status            ENUM('SUCCESS','FAILED','PENDING') NOT NULL DEFAULT 'PENDING',
    processed_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. New deliveries table
CREATE TABLE IF NOT EXISTS deliveries (
    delivery_id   INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL UNIQUE,
    agency_name   VARCHAR(100) NULL,
    rider_name    VARCHAR(100) NULL,
    rider_phone   VARCHAR(20) NULL,
    status        ENUM('UNASSIGNED','ASSIGNED','PICKED_UP','DELIVERED','CANCELLED') NOT NULL DEFAULT 'UNASSIGNED',
    assigned_at   DATETIME NULL,
    picked_up_at  DATETIME NULL,
    delivered_at  DATETIME NULL,
    notes         VARCHAR(255) NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. New reporting views
CREATE OR REPLACE VIEW v_station_queue AS
SELECT oi.order_item_id, o.order_id, o.order_number, oi.item_id, mi.item_name, oi.quantity,
       oi.station_name, oi.prep_status, o.order_date, o.order_type, o.order_source
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
JOIN menu_items mi ON oi.item_id = mi.item_id
WHERE oi.prep_status <> 'SERVED' AND o.status = 'COMPLETED'
ORDER BY o.order_date;

CREATE OR REPLACE VIEW v_active_deliveries AS
SELECT d.delivery_id, d.order_id, o.order_number, o.customer_name, o.customer_phone,
       o.customer_address, o.total_amount, d.agency_name, d.rider_name, d.rider_phone,
       d.status, d.assigned_at, d.picked_up_at, d.delivered_at, d.notes
FROM deliveries d
JOIN orders o ON d.order_id = o.order_id
WHERE d.status NOT IN ('DELIVERED','CANCELLED')
ORDER BY o.order_date;

SELECT 'Migration v2 completed successfully.' AS result;
