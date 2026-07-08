-- =====================================================================
-- BHOJAN-ALOY — Migration v4: Customer Loyalty Program
-- Run this AFTER migration_v2_... and migration_v3_payroll.sql if you are
-- upgrading an existing database instead of re-importing the full schema.
-- =====================================================================
USE bhojan_aloy;

-- ---------------------------------------------------------------------
-- New table: customers
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20)  NOT NULL UNIQUE,
    email           VARCHAR(100) NULL,
    loyalty_points  INT NOT NULL DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Link orders to a registered loyalty customer (nullable — walk-ins who
-- don't register are unaffected) and record which item (if any) was
-- comped free by a 50-point reward.
-- ---------------------------------------------------------------------
ALTER TABLE orders
    ADD COLUMN customer_id INT NULL AFTER employee_id,
    ADD COLUMN loyalty_reward_item VARCHAR(100) NULL AFTER customer_address,
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL;
