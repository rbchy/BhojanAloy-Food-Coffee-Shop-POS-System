-- Resets transactional demo data so public QA testers always start from a
-- clean slate. Run this nightly via cron on the VM:
--   0 3 * * * mysql -u bhojan_web -p'YOUR_PASSWORD' bhojan_aloy < /home/ubuntu/reset_demo_data.sql
--
-- NOTE: This clears orders/payments/inventory history but does NOT restore
-- stock_qty on menu_items, since the original stock levels aren't tracked
-- separately. If you want exact stock reset too, snapshot menu_items into a
-- backup table once after seeding, then add an UPDATE ... JOIN restoring
-- stock_qty from that snapshot here.

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM payments;
DELETE FROM deliveries;
DELETE FROM inventory_transactions;
DELETE FROM order_items;
DELETE FROM orders;

SET FOREIGN_KEY_CHECKS = 1;
