# Bhojan-Aloy — Food & Coffee Shop POS System

A complete Java Swing + MySQL point-of-sale system: order taking, menu &
recipe management, inventory tracking, employee/supplier records, sales
reporting, and username/password login with role-based access
(ADMIN / MANAGER / CASHIER).

## Project layout

```
Bhojan-Aloy/
├─ .project, .classpath        Eclipse project files
├─ database/
│  └─ bhojan_aloy_database.sql Full MySQL schema + sample data
├─ images/
│  └─ (put item photos here — see README_IMAGES.txt)
├─ lib/
│  └─ (place mysql-connector-j-*.jar here — see README_GET_CONNECTOR.txt)
└─ src/com/bhojanaloy/
   ├─ Main.java                 Application entry point
   ├─ config/                   DBConnection, EmailConfig (SMTP), SmsConfig (Twilio)
   ├─ model/                    19 entity classes (MenuItem, Order, Payment, Delivery, TimeClock, PayrollRun, Customer, User, ...)
   ├─ dao/                      15 DAO classes (CRUD + reporting SQL)
   ├─ service/                  POSService, PaymentService, PayrollService, ReceiptDeliveryService, NotificationService
   ├─ util/                     PasswordUtil, SessionManager, UITheme, ReceiptPrinter, PayStubPrinter, BillFormatter,
   │                            ImageUtil, ValidationUtil, SmtpMailSender, TwilioSmsSender
   └─ gui/                      Login screen (centered, circular badge) + 16 dashboard panels/dialogs (Swing),
                                including HelpDialog (5 languages) and RecipePanel (photo + 5-language steps)
```

## Setup (Eclipse)

1. **Install MySQL** (if not already) and start the server.
2. **Import the database**:
   ```
   mysql -u root -p < database/bhojan_aloy_database.sql
   ```
   This creates the `bhojan_aloy` database with 20 tables, 9 reporting
   views, and sample menu/employee/ingredient data.
3. **Create the admin login** — sample `users` rows are inserted with a
   placeholder hash. Generate real credentials by running the helper class:
   ```
   java -cp bin com.bhojanaloy.util.PasswordUtil admin123
   ```
   Copy the printed Salt/Hash into the `users` table for `admin`,
   `farida`, and `sabbir` (or simply delete those 3 rows and use the
   **Employees → Create Login for Selected** screen inside the app once
   you're logged in as any user — see step 6).
4. **Import into Eclipse**: File → Import → Existing Projects into
   Workspace → select the `Bhojan-Aloy` folder.
5. **Add the MySQL driver**: download `mysql-connector-j` and drop the
   jar into `lib/` (details in `lib/README_GET_CONNECTOR.txt`), then
   refresh the project so Eclipse picks up `.classpath`.
6. **Set your DB credentials** in
   `src/com/bhojanaloy/config/DBConnection.java` (`DB_USER`, `DB_PASSWORD`).
7. **Run** `Main.java` as a Java Application.

## First login

If you completed step 3's password hash swap, log in with:
- Username: `admin`  Password: `admin123` (ADMIN — full access)
- Username: `farida`  Password: `admin123` (MANAGER — no employee/supplier admin)
- Username: `sabbir`  Password: `admin123` (CASHIER — POS/menu/recipes/inventory only)

If you'd rather skip the manual hash step, use `PasswordUtil.createUser`
directly via `UserDAO`, or run a tiny one-off `main()` that calls
`new UserDAO().createUser("admin", "admin123", "Admin User", "ADMIN", null)`
once, then delete that helper.

## Features

- **POS**: category-tabbed menu grid, live cart, discount + 8% tax
  calculation, multiple payment methods, atomic checkout (stock is
  deducted and an inventory transaction logged in the same DB transaction
  as the order).
- **Menu Management**: add/edit/deactivate products, track price vs. cost.
- **Recipes**: a product photo, prep time/difficulty/yield, exact ingredient
  quantities, and step-by-step instructions per item — with a **Language**
  dropdown (English / বাংলা / Español / हिंदी / العربية) that switches the
  steps instantly (falls back to English if a language hasn't been
  translated yet). Add/Edit/Delete a recipe from the Recipes screen itself,
  entering each language's steps in its own tab.
- **Inventory**: raw ingredient stock levels, low-stock flags, manual
  stock adjustments (purchase-in, waste, correction) with an audit log.
- **Employees**: staff records + one-click login account creation
  (SHA-256 + per-user salt password hashing).
- **Suppliers**: contact directory for ingredient sourcing.
- **Reports**: today's summary, 30-day daily sales, item/category sales,
  employee performance, low-stock alerts — each exportable to CSV.
- **Station Display (Kitchen Display)**: every order line item is routed to
  the prep counter that makes it (Coffee Bar, Bakery, Sandwich Station, ...,
  configured per category). Staff advance each item PENDING → PREPARING →
  READY → SERVED; the screen auto-refreshes every 5 seconds.
- **Payment Gateway (simulated)**: CARD and MOBILE_BANKING checkouts open a
  gateway dialog (choose Visa/Mastercard or bKash/Nagad/Rocket, enter a
  reference), simulate a ~1 second round-trip and a realistic transaction
  reference, and record the result in the `payments` table. See "Going live
  with a real payment gateway" below.
- **Phone/Online Orders**: the POS screen has an Order Source selector
  (In-Store / Phone / Online) and an Order Type selector (Dine-in / Pickup /
  Delivery). Choosing Phone/Online or Delivery reveals customer name/phone/
  address fields so staff can key in orders taken over the phone or from an
  external channel.
- **Delivery Management**: any DELIVERY order automatically lands in the
  Delivery screen as UNASSIGNED. Staff assign it to an in-house rider or a
  third-party agency (Pathao, Foodpanda, Steadfast, etc.) — the agency
  effectively receives an order copy matching the customer, collects it from
  the shop (mark **Picked Up**), and drops it at the customer's address
  (mark **Delivered**). Customers who collect in person use Pickup order
  type instead and never enter this queue.
- **Receipt Printing**: after checkout, "Print receipt?" opens the OS print
  dialog (`util/ReceiptPrinter.java`, built on `java.awt.print` — no extra
  libraries), so any printer registered on the computer — including a
  thermal receipt printer with its standard driver installed — can print a
  formatted customer receipt.
- **Item-wise Bill Preview → Print or Send**: every checkout now opens an
  on-screen, itemized bill (`gui/BillPreviewDialog.java`) FIRST, built by
  `util/BillFormatter.java`. From there staff choose per the customer's
  wish: **🖨 Print** (same OS print dialog as before) or **✉ Send to
  Customer** (Email or SMS — pick a channel, confirm/enter the destination).
  This now sends a **real** email/SMS once you configure `EmailConfig.java`/
  `SmsConfig.java` (falls back to a simulated send with a fake reference
  until then); see "Going live with real receipt delivery" below).
- **Item Images**: the POS menu grid and Menu Management screen show a
  picture for each item. Add a real photo via Menu Items → Image Path →
  **Browse...** (see `images/README_IMAGES.txt`); items without a photo
  fall back to a category-appropriate emoji (☕🥯🍩🥪🍕🍔...) so the screen
  never looks empty.
- **In-app Help, multilingual**: a **❓ Help** button on the login screen and
  on the main dashboard's top bar opens a full user guide covering every
  screen (`gui/HelpDialog.java` / `gui/HelpContent.java`). A language
  dropdown inside the dialog switches the entire guide between English,
  বাংলা, Español, हिंदी, and العربية.
- **Login screen**: a circular illustrated badge/emblem (`images/login_banner.png`,
  generated — not a real photo, same reasoning as item images below) sits
  above the sign-in form, and the whole username/password/button block is
  centered both horizontally and vertically in the window. The username/
  password fields and Sign In button are a slightly smaller, tidier size
  for a cleaner look.
- **Hover feedback**: every themed button in the app (menu buttons, category
  filters, dialog actions, POS item cards, and the main dashboard's sidebar
  navigation) darkens slightly on mouse-over via `UITheme.addHoverEffect(...)`.
  On macOS, Swing's Aqua look-and-feel normally ignores a button's background
  color, which silently broke this effect on the sidebar; it now forces a
  cross-platform button UI so the hover color reliably shows on macOS too.
- **Editable payroll calculation**: the Regular/Overtime/Weekend/Holiday
  hour fields and a Deductions field in Payroll Processing are editable —
  fix a missed clock-out or add a manual adjustment, click **Recalculate**
  (or just **Process Payment**, which recalculates automatically) to update
  Gross/Net pay before saving.
- **Field validation**: Menu Items, Employees, Payroll edits, and the
  checkout's loyalty fields all validate their input (positive prices,
  non-negative hours/stock, valid email/phone formats) via
  `util/ValidationUtil.java` before saving.
- **Customer Loyalty Program**: at checkout, tick "Register / apply loyalty
  points" and enter the customer's name + phone (email optional). Every
  completed order earns 1 point; at 50 points the cheapest item in that
  order is automatically made free and the point count resets to 0. See
  "Customer Loyalty Program" below.
- **New-item email announcements**: whenever a brand-new menu item is saved
  in Menu Management, every registered loyalty customer with an email on
  file is automatically emailed (simulated) an announcement about it.

## Going live with a real payment gateway

`service/PaymentService.java` currently **simulates** the gateway call (≈95%
approval, fake reference number, ~1s delay) since no live merchant account
is configured. To accept real payments:
1. Sign up with a gateway (e.g. SSLCommerz or a card processor for
   Card, or bKash/Nagad/Rocket's merchant Checkout API for mobile banking).
2. Add their Java SDK (or call their REST API with `java.net.http.HttpClient`)
   inside `PaymentService.processPayment(...)`, replacing the
   `RANDOM.nextInt(100) < 95` simulation with the gateway's real response.
3. Use the gateway's returned transaction ID as `Payment.referenceNumber`
   and its response/status code to set `Payment.status` (SUCCESS/FAILED).
No other code needs to change — `PaymentDialog`, `OrderDAO`, and the
`payments` table already expect exactly this shape.

## Payroll: time clock, overtime/weekend/holiday pay, direct deposit

- **Time Clock** (sidebar, all roles): each employee clocks in/out. Admins
  and Managers can select and clock any employee (handy since not every
  staff member needs their own login); Cashiers can only clock themselves.
  A "Currently Clocked In" list shows who's on shift right now.
- **Payroll Processing** (sidebar, Manager/Admin only): pick an employee and
  a pay period, click **Calculate Hours & Pay** to pull their completed
  shifts and compute a breakdown, then **Process Payment**.
- **No duplicate records on correction**: if you edit the hours/deductions
  fields and click **Process Payment** again for the same employee + pay
  period, the app detects the existing payroll run and asks whether to
  overwrite it (instead of silently creating a second row). You can also
  select any row in **Payroll History** and click **Delete Selected** to
  remove a mistaken or duplicate entry outright.
- **Pay calculation** (`service/PayrollService.java`): for each shift,
  the first 8 hours/day (`DAILY_REGULAR_CAP`) are Regular pay; anything
  beyond that is Overtime (1.5x). If the shift falls on a company holiday
  (managed via the **Manage Holidays** button) it's paid at 2.0x instead of
  1.0x; if it falls on a weekend (Friday/Saturday by default — see
  `WEEKEND_DAYS`, change to Saturday/Sunday etc. as needed) it's 1.5x.
  Overtime stacks on top of whichever day-type multiplier applies (e.g.
  holiday overtime = 2.0 × 1.5 = 3.0x). All thresholds/multipliers are
  constants at the top of `PayrollService` — edit them to match your local
  labor law or company policy.
- **Payout**: choose Direct Deposit (simulated bank transfer — see below),
  Cash, or Check at the moment of processing, regardless of the employee's
  stored default preference. A **Print pay stub** prompt follows, using the
  same `java.awt.print` approach as customer receipts.
- **Employees screen**: each employee now has an hourly rate, a payment
  preference, and bank details (name/account/routing number) editable via
  **Edit Selected (Pay & Bank Info)**.

### Going live with real direct deposit / bank transfers
`PayrollService.simulateDirectDeposit(...)` currently fakes a bank transfer
(random reference, ~1s delay) since no live business banking/ACH
integration is configured. To pay real money:
1. Set up a business bank transfer / ACH API with your bank or a payroll
   processor (e.g. a local bank's corporate API, or a service like Deel/
   Gusto if you outsource payroll entirely).
2. Replace the body of `simulateDirectDeposit` with a real transfer call
   using the employee's `bank_name` / `bank_account_number` /
   `bank_routing_number`, and return the bank's real transaction reference.
3. `DirectDepositDialog`, `PayrollPanel`, and the `payroll_runs` table
   already expect this shape — no other changes needed.

## Going live with real receipt delivery (Email/SMS)
`service/ReceiptDeliveryService.send(...)` now sends **real** emails and SMS
— no external jar required. Until you configure real credentials it
automatically falls back to the old simulated send (fake reference,
~0.5-1.1s delay), so nothing breaks out of the box.

**Email (Gmail SMTP, via `config/EmailConfig.java`)**
1. Turn on 2-Step Verification on the Gmail account you want to send from.
2. Create an **App Password**: Google Account → Security → 2-Step
   Verification → App passwords → generate one for "Mail".
3. Open `config/EmailConfig.java` and set `SMTP_USERNAME` to that Gmail
   address and `SMTP_APP_PASSWORD` to the 16-character app password.
4. Save. `util/SmtpMailSender.java` talks to `smtp.gmail.com:465` directly
   over `javax.net.ssl.SSLSocketFactory` — plain JDK classes, no JavaMail/
   jakarta.mail jar needed.

**SMS (Twilio REST API, via `config/SmsConfig.java`)**
1. Create a free Twilio account and get a trial phone number.
2. From the Twilio Console, copy your **Account SID** and **Auth Token**.
3. Open `config/SmsConfig.java` and set `TWILIO_ACCOUNT_SID`,
   `TWILIO_AUTH_TOKEN`, and `TWILIO_FROM_NUMBER` (your Twilio number, e.g.
   `+15551234567`).
4. Save. `util/TwilioSmsSender.java` posts directly to Twilio's HTTPS API
   via `java.net.http.HttpClient` — no Twilio SDK jar needed. Trial accounts
   can only text phone numbers you've verified in the Twilio console; verify
   the customer's number there first, or upgrade the account for unrestricted
   sending.

Until both files are filled in with real values, `EmailConfig.isConfigured()`
/ `SmsConfig.isConfigured()` return `false` and receipts keep using the
original simulated send — so it's safe to leave either one (or both)
unconfigured.

## Item photos
No real photos are bundled with this project (to avoid using unlicensed
internet images) — items show an emoji placeholder until you add your own.
See `images/README_IMAGES.txt` for the recommended photo size/format, and
add a photo to any item via **Menu Items → Image Path → Browse...**.

## In-app Help (English / বাংলা / Español / हिंदी / العربية)
Click **❓ Help** on the login screen or the main dashboard's top bar for a
full built-in user guide — every screen, plus a note on which integrations
(payment gateway, direct deposit, receipt delivery) are simulated. The
**Language** dropdown in the top-right of the Help window switches the
whole guide between all five languages instantly; the content lives in
`gui/HelpContent.java` if you want to edit, extend, or add another language.

## Customer Loyalty Program
1. At checkout, tick **"Register / apply loyalty points for this
   customer"** and enter their name and phone number (email is optional,
   but needed to receive new-item announcements).
2. The customer is looked up by phone number (`dao/CustomerDAO.java`); if
   they're new, a `customers` row is created for them.
3. Every completed order adds **1 point**. When their running total
   reaches **50 points**, the cheapest item in that order's cart is made
   **FREE** automatically (shown as a "Loyalty Reward" line on the Bill
   Preview/receipt), and their points reset to 0.
4. Registering with an email also opts them into "new menu item" email
   announcements (simulated — see `service/NotificationService.java`).

The point-earn rate (1/order) and reward (free cheapest item at 50 points)
are both straightforward to change in `POSPanel.checkout()` if you'd rather
earn points per amount spent, or give a percentage discount instead of a
free item.

## Troubleshooting: "Checkout failed" message
As of this version, the checkout error dialog also shows the exact database
error under "Details:" instead of only a generic message. If it says
something like `Unknown column 'customer_id'`, your database is missing the
loyalty-program update — run `database/migration_v4_loyalty.sql` (or
re-import the full `bhojan_aloy_database.sql` if you don't need to keep
existing data) and try again. Other details (e.g. "Insufficient stock for
item: X") point to the real cause directly.

## Notes

- Password hashing uses SHA-256 with a random per-user salt
  (`util/PasswordUtil.java`) — adequate for a single-location desktop app;
  swap in bcrypt/argon2 if you later expose this over a network.
- The 8% tax rate lives in `service/POSService.TAX_RATE` — change it there.
- `PurchaseOrder` / `PurchaseOrderItem` tables and models are included in
  the schema for future purchase-order tracking but don't yet have a
  dedicated screen — extend `SupplierPanel` or add a new panel using the
  same pattern as `InventoryPanel`.
- Already have data from before this update? Run the migration scripts in
  order instead of re-importing the full schema — each adds its new
  columns/tables via `ALTER`/`CREATE TABLE IF NOT EXISTS` without dropping
  your existing database:
  `database/migration_v2_stations_payments_delivery.sql` then
  `database/migration_v3_payroll.sql` then
  `database/migration_v4_loyalty.sql` then
  `database/migration_v5_recipe_i18n.sql`. Fresh installs should just use
  `bhojan_aloy_database.sql` as before (it already includes everything).
