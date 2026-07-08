-- =====================================================================
-- BHOJAN-ALOY — Migration v3
-- Adds: employee time clock (clock-in/out), company holiday calendar,
-- and payroll processing (hourly rate, overtime/weekend/holiday pay,
-- direct deposit / cash payout) — WITHOUT dropping your existing database.
--
-- Run this only if you already imported an earlier schema (original or v2)
-- and have data you want to keep. If you're starting fresh, just import
-- the full bhojan_aloy_database.sql instead (it already includes everything
-- below).
--
-- Usage:  mysql -u root -p bhojan_aloy < migration_v3_payroll.sql
-- =====================================================================
USE bhojan_aloy;

-- 1. Extend employees with hourly pay rate + payment preference + bank info
ALTER TABLE employees
    ADD COLUMN hourly_rate DECIMAL(10,2) DEFAULT 0 AFTER salary,
    ADD COLUMN payment_preference ENUM('DIRECT_DEPOSIT','CASH','CHECK') NOT NULL DEFAULT 'CASH' AFTER hourly_rate,
    ADD COLUMN bank_name VARCHAR(100) NULL AFTER payment_preference,
    ADD COLUMN bank_account_number VARCHAR(50) NULL AFTER bank_name,
    ADD COLUMN bank_routing_number VARCHAR(50) NULL AFTER bank_account_number;

-- Reasonable starter hourly rates so payroll calculations aren't $0 immediately —
-- adjust these to your actual pay rates via the Employees screen afterward.
UPDATE employees SET hourly_rate = 220.00, payment_preference = 'DIRECT_DEPOSIT' WHERE designation = 'Manager';
UPDATE employees SET hourly_rate = 140.00 WHERE hourly_rate = 0 AND designation LIKE '%Barista%';
UPDATE employees SET hourly_rate = 110.00 WHERE hourly_rate = 0 AND designation LIKE '%Cashier%';
UPDATE employees SET hourly_rate = 125.00 WHERE hourly_rate = 0;

-- 2. Time clock (clock-in / clock-out)
CREATE TABLE IF NOT EXISTS time_clock (
    clock_id      INT AUTO_INCREMENT PRIMARY KEY,
    employee_id   INT NOT NULL,
    work_date     DATE NOT NULL,
    clock_in      DATETIME NOT NULL,
    clock_out     DATETIME NULL,
    total_hours   DECIMAL(5,2) NULL,
    notes         VARCHAR(255) NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) ENGINE=InnoDB;

-- 3. Company holiday calendar
CREATE TABLE IF NOT EXISTS holidays (
    holiday_id    INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date  DATE NOT NULL UNIQUE,
    holiday_name  VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- 4. Payroll runs
CREATE TABLE IF NOT EXISTS payroll_runs (
    payroll_id        INT AUTO_INCREMENT PRIMARY KEY,
    employee_id       INT NOT NULL,
    period_start      DATE NOT NULL,
    period_end        DATE NOT NULL,
    regular_hours     DECIMAL(6,2) DEFAULT 0,
    overtime_hours    DECIMAL(6,2) DEFAULT 0,
    weekend_hours     DECIMAL(6,2) DEFAULT 0,
    holiday_hours     DECIMAL(6,2) DEFAULT 0,
    hourly_rate       DECIMAL(10,2) NOT NULL,
    gross_pay         DECIMAL(10,2) NOT NULL DEFAULT 0,
    deductions        DECIMAL(10,2) NOT NULL DEFAULT 0,
    net_pay           DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method    ENUM('DIRECT_DEPOSIT','CASH','CHECK') NOT NULL DEFAULT 'CASH',
    payment_reference VARCHAR(60) NULL,
    status            ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
    generated_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at           DATETIME NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) ENGINE=InnoDB;

-- 5. Reporting views
CREATE OR REPLACE VIEW v_currently_clocked_in AS
SELECT tc.clock_id, tc.employee_id, e.full_name, e.designation, tc.clock_in
FROM time_clock tc
JOIN employees e ON tc.employee_id = e.employee_id
WHERE tc.clock_out IS NULL
ORDER BY tc.clock_in;

CREATE OR REPLACE VIEW v_payroll_history AS
SELECT pr.*, e.full_name
FROM payroll_runs pr
JOIN employees e ON pr.employee_id = e.employee_id
ORDER BY pr.period_end DESC;

SELECT 'Migration v3 (payroll) completed successfully.' AS result;
