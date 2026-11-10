package com.bhojanaloy.gui;

/** Multilingual (English / বাংলা / Español / हिंदी / العربية) in-app user guide shown by HelpDialog. */
public class HelpContent {

    private HelpContent() {}

    public static final String ENGLISH_HTML =
        "<html><body style='font-family:Segoe UI; font-size:10pt; padding:8px;'>" +
        "<h1>BHOJAN-ALOY — User Guide</h1>" +
        "<p>A complete Point-of-Sale system for a food &amp; coffee shop: order taking, " +
        "kitchen routing, payments, delivery, inventory, and payroll — all in one app.</p>" +

        "<h2>1. Logging In</h2>" +
        "<p>Enter your username and password. Your access depends on your role:</p>" +
        "<ul>" +
        "<li><b>ADMIN</b> — sees every screen, including Employees, Payroll, Suppliers, Reports.</li>" +
        "<li><b>MANAGER</b> — same as Admin except cannot be restricted; day-to-day management access.</li>" +
        "<li><b>CASHIER</b> — sees POS, Station Display, Menu, Recipes, Inventory, Delivery, Time Clock only.</li>" +
        "</ul>" +

        "<h2>2. Point of Sale (POS)</h2>" +
        "<p>Click a category to filter the menu grid, or 'All Items' to see everything. " +
        "Click an item card to add it to the current order (cart) on the right. " +
        "Set a discount if needed — tax (8%) is calculated automatically.</p>" +
        "<p><b>Order Type / Source:</b> choose Dine-in, Pickup, or Delivery, and whether the order " +
        "came from a walk-in customer (In-Store), a Phone call, or Online. Phone/Online/Delivery orders " +
        "require the customer's name and phone number; Delivery additionally requires an address.</p>" +
        "<p><b>Payment:</b> choose Cash, Card, or Mobile Banking. Card/Mobile Banking opens a payment " +
        "gateway screen (simulated — no live bank account connected yet) where you pick a provider " +
        "(Visa/Mastercard or bKash/Nagad/Rocket) and confirm.</p>" +
        "<p><b>Checkout:</b> after payment, stock is deducted automatically and the order is sent to the " +
        "Station Display for the kitchen. A <b>Bill Preview</b> then appears — you can Print it, or Send " +
        "it electronically (SMS/Email) to the customer, whichever they prefer.</p>" +

        "<h2>3. Station Display (Kitchen Display)</h2>" +
        "<p>Shows every pending order item grouped by the prep counter that makes it (Coffee Bar, " +
        "Bakery, Sandwich Station, etc. — configured per menu category). Staff click through " +
        "<b>Start Preparing → Mark Ready → Served/Picked Up</b> as they work. The screen refreshes " +
        "automatically every 5 seconds.</p>" +

        "<h2>4. Menu Items</h2>" +
        "<p>Add, edit, or deactivate products. Each item can have a photo — click Browse to pick an " +
        "image file, or leave it blank to show a category emoji instead.</p>" +

        "<h2>5. Recipes</h2>" +
        "<p>Shows step-by-step prep instructions and the exact ingredients/quantities needed for " +
        "each menu item — handy for training new staff.</p>" +

        "<h2>6. Inventory</h2>" +
        "<p>Tracks raw ingredient stock levels. Low-stock items are flagged automatically. Use " +
        "<b>Adjust Stock</b> to record a delivery, waste, or correction — every change is logged.</p>" +

        "<h2>7. Delivery</h2>" +
        "<p>Every Delivery-type order lands here as <b>UNASSIGNED</b>. Assign it to an in-house rider " +
        "or a third-party agency (Pathao, Foodpanda, Steadfast, etc.) — they effectively receive a copy " +
        "of the order matching the customer, collect it from the shop (<b>Mark Picked Up</b>), and drop " +
        "it at the customer's address (<b>Mark Delivered</b>). Customers who collect in person use " +
        "Pickup order type instead and never appear here.</p>" +

        "<h2>8. Time Clock</h2>" +
        "<p>Employees clock in/out here. Admins/Managers can clock any employee in or out (useful if " +
        "not everyone has their own login); Cashiers can only clock themselves. A live list shows who's " +
        "currently on shift.</p>" +

        "<h2>9. Payroll (Manager/Admin only)</h2>" +
        "<p>Pick an employee and a date range, click <b>Calculate Hours &amp; Pay</b> to pull their " +
        "completed shifts. The first 8 hours/day are Regular pay; anything beyond is Overtime (1.5x). " +
        "Shifts on a company holiday pay 2.0x, weekends (Friday/Saturday by default) pay 1.5x, and " +
        "overtime stacks on top. Use <b>Manage Holidays</b> to build the holiday calendar. Then " +
        "<b>Process Payment</b> via Direct Deposit (simulated bank transfer), Cash, or Check, and " +
        "optionally print a pay stub.</p>" +

        "<h2>10. Employees</h2>" +
        "<p>Manage staff records, hourly pay rate, payment preference, and bank details (for Direct " +
        "Deposit). Create a login account for an employee via <b>Create Login for Selected</b>.</p>" +

        "<h2>11. Suppliers &amp; Reports</h2>" +
        "<p>Suppliers is a contact directory for ingredient sourcing. Reports shows today's summary, " +
        "daily/item/category sales, employee performance, and low-stock alerts — each exportable to CSV.</p>" +

        "<h2>12. Loyalty Program</h2>" +
        "<p>At checkout, tick <b>\"Register / apply loyalty points\"</b> and enter the customer's name " +
        "and phone (email optional). Every completed order earns 1 point. Once a customer reaches " +
        "<b>50 points</b>, their cheapest cart item is automatically made <b>FREE</b> on that order, and " +
        "their points reset to 0. Registering an email also lets them receive an emailed announcement " +
        "whenever a new menu item is added.</p>" +

        "<h2>Notes on simulated integrations</h2>" +
        "<p>Payment Gateway, Direct Deposit, and Email/SMS sending are all <b>simulated</b> in this " +
        "build — there's no live merchant account, bank API, or email/SMS provider connected yet. " +
        "Everything else (orders, stock, payroll math, reports, loyalty points) is fully real and saved " +
        "to your MySQL database. See README.md for exactly where a developer would plug in real providers.</p>" +
        "</body></html>";

    public static final String BANGLA_HTML =
        "<html><body style='font-family:Segoe UI; font-size:10pt; padding:8px;'>" +
        "<h1>ভোজন-আলয় — ব্যবহারকারী গাইড</h1>" +
        "<p>একটি সম্পূর্ণ ফুড অ্যান্ড কফি শপ POS সিস্টেম — অর্ডার নেওয়া, রান্নাঘরে রাউটিং, পেমেন্ট, " +
        "ডেলিভারি, ইনভেন্টরি এবং পে-রোল — সব একটা অ্যাপে।</p>" +

        "<h2>১. লগইন</h2>" +
        "<p>আপনার ইউজারনেম ও পাসওয়ার্ড দিন। আপনার রোল অনুযায়ী অ্যাক্সেস ঠিক হয়:</p>" +
        "<ul>" +
        "<li><b>ADMIN</b> — সব স্ক্রিন দেখতে পাবেন, Employees, Payroll, Suppliers, Reports সহ।</li>" +
        "<li><b>MANAGER</b> — প্রায় Admin-এর মতোই, দৈনন্দিন ব্যবস্থাপনার অ্যাক্সেস।</li>" +
        "<li><b>CASHIER</b> — শুধু POS, Station Display, Menu, Recipes, Inventory, Delivery, Time Clock দেখবেন।</li>" +
        "</ul>" +

        "<h2>২. Point of Sale (POS)</h2>" +
        "<p>ক্যাটাগরিতে ক্লিক করে মেনু ফিল্টার করুন, বা 'All Items' দিয়ে সব দেখুন। কোনো আইটেম কার্ডে " +
        "ক্লিক করলে সেটা ডানপাশে চলমান অর্ডারে (কার্টে) যোগ হবে। প্রয়োজনে ডিসকাউন্ট বসান — ৮% ট্যাক্স " +
        "স্বয়ংক্রিয়ভাবে হিসাব হয়।</p>" +
        "<p><b>Order Type / Source:</b> Dine-in, Pickup, বা Delivery বেছে নিন, এবং অর্ডারটা এসেছে " +
        "ওয়াক-ইন কাস্টমার (In-Store), ফোন কল, নাকি অনলাইন থেকে সেটাও বেছে নিন। Phone/Online/Delivery " +
        "অর্ডারে কাস্টমারের নাম ও ফোন নাম্বার লাগবে; Delivery-তে আরও ঠিকানা লাগবে।</p>" +
        "<p><b>Payment:</b> Cash, Card, বা Mobile Banking বেছে নিন। Card/Mobile Banking বেছে নিলে একটা " +
        "পেমেন্ট গেটওয়ে স্ক্রিন খুলবে (সিমুলেটেড — এখনো কোনো আসল ব্যাংক অ্যাকাউন্ট যুক্ত নেই) যেখানে " +
        "প্রোভাইডার (Visa/Mastercard বা bKash/Nagad/Rocket) বেছে কনফার্ম করবেন।</p>" +
        "<p><b>Checkout:</b> পেমেন্টের পর স্টক স্বয়ংক্রিয়ভাবে কমে যাবে এবং অর্ডারটা রান্নাঘরের জন্য " +
        "Station Display-তে চলে যাবে। এরপর একটা <b>Bill Preview</b> দেখাবে — সেখান থেকে Print করতে " +
        "পারেন, বা কাস্টমারের পছন্দ অনুযায়ী SMS/Email-এ পাঠাতে পারেন।</p>" +

        "<h2>৩. Station Display (কিচেন ডিসপ্লে)</h2>" +
        "<p>প্রতিটা পেন্ডিং অর্ডার আইটেম তার প্রস্তুতকারী কাউন্টার অনুযায়ী (Coffee Bar, Bakery, Sandwich " +
        "Station ইত্যাদি — মেনু ক্যাটাগরি অনুযায়ী ঠিক করা) আলাদা করে দেখায়। স্টাফরা কাজ করার সময় " +
        "<b>Start Preparing → Mark Ready → Served/Picked Up</b> বাটনে ক্লিক করে যাবেন। স্ক্রিনটা প্রতি " +
        "৫ সেকেন্ডে নিজে থেকেই রিফ্রেশ হয়।</p>" +

        "<h2>৪. Menu Items</h2>" +
        "<p>পণ্য যোগ, এডিট বা বন্ধ করুন। প্রতিটা আইটেমে একটা ছবি দেওয়া যায় — Browse চেপে ছবি বেছে নিন, " +
        "না দিলে একটা ক্যাটাগরি ইমোজি দেখাবে।</p>" +

        "<h2>৫. Recipes</h2>" +
        "<p>প্রতিটা মেনু আইটেম কীভাবে বানাতে হয় তার ধাপে ধাপে নির্দেশনা ও কী কী উপকরণ কতটুকু লাগবে " +
        "তা দেখায় — নতুন স্টাফ ট্রেনিং করাতে কাজে লাগে।</p>" +

        "<h2>৬. Inventory</h2>" +
        "<p>কাঁচামালের স্টক ট্র্যাক করে। কম স্টক থাকলে স্বয়ংক্রিয়ভাবে সতর্কতা দেখায়। " +
        "<b>Adjust Stock</b> দিয়ে নতুন ডেলিভারি, নষ্ট হওয়া মাল, বা সংশোধন রেকর্ড করুন — প্রতিটা " +
        "পরিবর্তন লগ হয়ে থাকে।</p>" +

        "<h2>৭. Delivery</h2>" +
        "<p>প্রতিটা Delivery-টাইপ অর্ডার এখানে <b>UNASSIGNED</b> হিসেবে আসে। এটা কোনো ইন-হাউজ রাইডার " +
        "বা তৃতীয়-পক্ষ এজেন্সিকে (Pathao, Foodpanda, Steadfast ইত্যাদি) অ্যাসাইন করুন — তারা কাস্টমারের " +
        "সাথে মিলিয়ে অর্ডারের একটা কপি পায়, দোকান থেকে সংগ্রহ করে (<b>Mark Picked Up</b>), এবং কাস্টমারের " +
        "ঠিকানায় পৌঁছে দেয় (<b>Mark Delivered</b>)। যারা নিজে এসে খাবার নিয়ে যান তারা Pickup " +
        "টাইপ ব্যবহার করেন, এখানে আসেন না।</p>" +

        "<h2>৮. Time Clock</h2>" +
        "<p>এখানে কর্মচারীরা Clock In/Out করেন। Admin/Manager যেকোনো কর্মচারীকে ক্লক ইন/আউট করাতে পারেন " +
        "(সবার নিজের লগইন না থাকলেও কাজে লাগে); Cashier শুধু নিজেরটা করতে পারবেন। কে এখন কাজে আছে " +
        "তার একটা লাইভ লিস্টও দেখা যায়।</p>" +

        "<h2>৯. Payroll (শুধু Manager/Admin)</h2>" +
        "<p>একজন কর্মচারী ও তারিখের রেঞ্জ বেছে <b>Calculate Hours &amp; Pay</b> চাপুন — এটা তার শেষ করা " +
        "শিফটগুলো নিয়ে হিসাব করবে। দিনে প্রথম ৮ ঘণ্টা Regular পে; এর বেশি হলে Overtime (১.৫ গুণ)। " +
        "কোম্পানির ছুটির দিনে কাজ করলে ২.০ গুণ, উইকেন্ডে (ডিফল্ট শুক্র/শনি) ১.৫ গুণ, আর ওভারটাইম এর " +
        "ওপরেও যোগ হয়। <b>Manage Holidays</b> দিয়ে ছুটির ক্যালেন্ডার বানান। এরপর <b>Process Payment</b> " +
        "দিয়ে Direct Deposit (সিমুলেটেড ব্যাংক ট্রান্সফার), Cash, বা Check-এ পেমেন্ট করুন, চাইলে " +
        "পে-স্টাব প্রিন্ট করুন।</p>" +

        "<h2>১০. Employees</h2>" +
        "<p>স্টাফের তথ্য, ঘণ্টাপ্রতি বেতনের হার, পেমেন্ট পছন্দ, আর ব্যাংক তথ্য (Direct Deposit-এর জন্য) " +
        "ম্যানেজ করুন। <b>Create Login for Selected</b> দিয়ে কোনো কর্মচারীর লগইন অ্যাকাউন্ট বানান।</p>" +

        "<h2>১১. Suppliers ও Reports</h2>" +
        "<p>Suppliers হলো কাঁচামাল সরবরাহকারীদের কন্টাক্ট ডিরেক্টরি। Reports-এ আজকের সারাংশ, দৈনিক/আইটেম/" +
        "ক্যাটাগরি বিক্রয়, কর্মচারী পারফরম্যান্স, আর কম স্টকের সতর্কতা দেখা যায় — সবই CSV করে এক্সপোর্ট " +
        "করা যায়।</p>" +

        "<h2>১২. Loyalty Program (লয়্যালটি পয়েন্ট)</h2>" +
        "<p>চেকআউটের সময় <b>\"Register / apply loyalty points\"</b> চেকবক্সে টিক দিয়ে কাস্টমারের নাম " +
        "ও ফোন নাম্বার দিন (ইমেইল ঐচ্ছিক)। প্রতিটা সম্পূর্ণ অর্ডারে ১ পয়েন্ট যোগ হয়। কাস্টমারের পয়েন্ট " +
        "<b>৫০</b>-এ পৌঁছালে, সেই অর্ডারের সবচেয়ে কম দামি আইটেমটা স্বয়ংক্রিয়ভাবে <b>ফ্রি</b> হয়ে যাবে, " +
        "আর পয়েন্ট আবার ০ থেকে শুরু হবে। ইমেইল দিলে নতুন কোনো মেনু আইটেম যোগ হলে তারা একটা ইমেইলও পাবেন।</p>" +

        "<h2>সিমুলেটেড ইন্টিগ্রেশন সম্পর্কে নোট</h2>" +
        "<p>Payment Gateway, Direct Deposit, আর Email/SMS পাঠানো — এই তিনটাই এই ভার্সনে " +
        "<b>সিমুলেটেড</b> (এখনো কোনো আসল মার্চেন্ট অ্যাকাউন্ট, ব্যাংক API, বা ইমেইল/SMS প্রোভাইডার যুক্ত " +
        "নেই)। বাকি সবকিছু (অর্ডার, স্টক, পে-রোল হিসাব, রিপোর্ট, লয়্যালটি পয়েন্ট) সম্পূর্ণ আসল এবং আপনার " +
        "MySQL ডাটাবেসে সেভ হয়। কোথায় আসল প্রোভাইডার বসাতে হবে তার বিস্তারিত README.md ফাইলে আছে।</p>" +
        "</body></html>";

    public static final String SPANISH_HTML =
        "<html><body style='font-family:Segoe UI; font-size:10pt; padding:8px;'>" +
        "<h1>BHOJAN-ALOY — Guía del Usuario</h1>" +
        "<p>Un sistema completo de punto de venta para una cafetería/restaurante: toma de pedidos, " +
        "enrutamiento a cocina, pagos, entregas, inventario y nómina — todo en una sola aplicación.</p>" +

        "<h2>1. Iniciar Sesión</h2>" +
        "<p>Ingrese su usuario y contraseña. Su acceso depende de su rol:</p>" +
        "<ul>" +
        "<li><b>ADMIN</b> — ve todas las pantallas, incluyendo Empleados, Nómina, Proveedores, Reportes.</li>" +
        "<li><b>MANAGER</b> — igual que Admin pero sin restricciones; acceso de gestión diaria.</li>" +
        "<li><b>CASHIER</b> — solo ve POS, Pantalla de Estación, Menú, Recetas, Inventario, Entregas, Reloj de Tiempo.</li>" +
        "</ul>" +

        "<h2>2. Punto de Venta (POS)</h2>" +
        "<p>Haga clic en una categoría para filtrar el menú, o 'All Items' para ver todo. Haga clic en " +
        "una tarjeta de producto para agregarlo al pedido actual (carrito) a la derecha. Aplique un " +
        "descuento si es necesario — el impuesto (8%) se calcula automáticamente.</p>" +
        "<p><b>Tipo/Origen del Pedido:</b> elija Comer aquí, Recoger, o Entrega, y si el pedido vino de " +
        "un cliente presencial (En Tienda), una llamada telefónica, o en línea. Los pedidos por " +
        "Teléfono/En línea/Entrega requieren el nombre y teléfono del cliente; Entrega además requiere " +
        "una dirección.</p>" +
        "<p><b>Pago:</b> elija Efectivo, Tarjeta, o Banca Móvil. Tarjeta/Banca Móvil abre una pantalla de " +
        "pasarela de pago (simulada — aún no hay una cuenta bancaria real conectada) donde elige un " +
        "proveedor (Visa/Mastercard o bKash/Nagad/Rocket) y confirma.</p>" +
        "<p><b>Pago final (Checkout):</b> después del pago, el stock se descuenta automáticamente y el " +
        "pedido se envía a la Pantalla de Estación para la cocina. Luego aparece una <b>Vista Previa de " +
        "la Factura</b> — puede imprimirla, o enviarla electrónicamente (SMS/Email) al cliente, según " +
        "prefiera.</p>" +

        "<h2>3. Pantalla de Estación (Pantalla de Cocina)</h2>" +
        "<p>Muestra cada artículo de pedido pendiente agrupado por el puesto de preparación que lo hace " +
        "(Barra de Café, Panadería, Estación de Sándwiches, etc. — configurado por categoría de menú). " +
        "El personal hace clic en <b>Empezar a Preparar → Marcar Listo → Servido/Recogido</b> mientras " +
        "trabaja. La pantalla se actualiza automáticamente cada 5 segundos.</p>" +

        "<h2>4. Artículos del Menú</h2>" +
        "<p>Agregue, edite, o desactive productos. Cada artículo puede tener una foto — haga clic en " +
        "Examinar para elegir un archivo de imagen, o déjelo en blanco para mostrar un emoji de " +
        "categoría en su lugar.</p>" +

        "<h2>5. Recetas</h2>" +
        "<p>Muestra instrucciones de preparación paso a paso y los ingredientes/cantidades exactas " +
        "necesarias para cada artículo del menú — útil para entrenar personal nuevo.</p>" +

        "<h2>6. Inventario</h2>" +
        "<p>Rastrea los niveles de stock de ingredientes crudos. Los artículos con poco stock se marcan " +
        "automáticamente. Use <b>Ajustar Stock</b> para registrar una entrega, desperdicio, o " +
        "corrección — cada cambio queda registrado.</p>" +

        "<h2>7. Entregas</h2>" +
        "<p>Todo pedido tipo Entrega llega aquí como <b>UNASSIGNED</b> (sin asignar). Asígnelo a un " +
        "repartidor propio o a una agencia externa (Pathao, Foodpanda, Steadfast, etc.) — ellos reciben " +
        "una copia del pedido correspondiente al cliente, lo recogen de la tienda (<b>Marcar Recogido</b>), " +
        "y lo entregan en la dirección del cliente (<b>Marcar Entregado</b>). Los clientes que recogen en " +
        "persona usan el tipo Recoger en su lugar y nunca aparecen aquí.</p>" +

        "<h2>8. Reloj de Tiempo</h2>" +
        "<p>Los empleados marcan entrada/salida aquí. Admins/Gerentes pueden marcar entrada/salida de " +
        "cualquier empleado (útil si no todos tienen su propio inicio de sesión); los Cajeros solo " +
        "pueden marcarse a sí mismos. Una lista en vivo muestra quién está actualmente en turno.</p>" +

        "<h2>9. Nómina (solo Gerente/Admin)</h2>" +
        "<p>Elija un empleado y un rango de fechas, haga clic en <b>Calcular Horas y Pago</b> para " +
        "obtener sus turnos completados. Las primeras 8 horas/día son pago Regular; lo que exceda es " +
        "Tiempo Extra (1.5x). Los turnos en un feriado de la empresa pagan 2.0x, los fines de semana " +
        "(viernes/sábado por defecto) pagan 1.5x, y el tiempo extra se suma encima. Use <b>Gestionar " +
        "Feriados</b> para construir el calendario de feriados. Luego <b>Procesar Pago</b> vía Depósito " +
        "Directo (transferencia bancaria simulada), Efectivo, o Cheque, y opcionalmente imprima un " +
        "recibo de pago.</p>" +

        "<h2>10. Empleados</h2>" +
        "<p>Gestione registros de personal, tarifa de pago por hora, preferencia de pago, y datos " +
        "bancarios (para Depósito Directo). Cree una cuenta de inicio de sesión para un empleado con " +
        "<b>Crear Inicio de Sesión para el Seleccionado</b>.</p>" +

        "<h2>11. Proveedores y Reportes</h2>" +
        "<p>Proveedores es un directorio de contactos para abastecimiento de ingredientes. Reportes " +
        "muestra el resumen de hoy, ventas diarias/por artículo/categoría, rendimiento de empleados, y " +
        "alertas de poco stock — todo exportable a CSV.</p>" +

        "<h2>12. Programa de Lealtad</h2>" +
        "<p>En el pago, marque <b>\"Register / apply loyalty points\"</b> e ingrese el nombre y teléfono " +
        "del cliente (email opcional). Cada pedido completado gana 1 punto. Cuando un cliente alcanza " +
        "<b>50 puntos</b>, el artículo más barato de su carrito se hace <b>GRATIS</b> automáticamente en " +
        "ese pedido, y sus puntos se reinician a 0. Registrar un email también les permite recibir un " +
        "anuncio por correo cada vez que se agrega un nuevo artículo al menú.</p>" +

        "<h2>Notas sobre integraciones simuladas</h2>" +
        "<p>La Pasarela de Pago, el Depósito Directo, y el envío de Email/SMS están todos " +
        "<b>simulados</b> en esta versión — todavía no hay una cuenta de comerciante real, API bancaria, " +
        "o proveedor de email/SMS conectado. Todo lo demás (pedidos, stock, cálculos de nómina, " +
        "reportes, puntos de lealtad) es completamente real y se guarda en su base de datos MySQL. " +
        "Vea README.md para saber exactamente dónde un desarrollador conectaría proveedores reales.</p>" +
        "</body></html>";

    public static final String HINDI_HTML =
        "<html><body style='font-family:Segoe UI; font-size:10pt; padding:8px;'>" +
        "<h1>भोजन-आलय — उपयोगकर्ता गाइड</h1>" +
        "<p>एक पूर्ण फूड एंड कॉफी शॉप POS सिस्टम — ऑर्डर लेना, किचन रूटिंग, भुगतान, डिलीवरी, इन्वेंटरी " +
        "और पेरोल — सब एक ही ऐप में।</p>" +

        "<h2>1. लॉग इन</h2>" +
        "<p>अपना यूज़रनेम और पासवर्ड डालें। आपकी एक्सेस आपकी भूमिका पर निर्भर करती है:</p>" +
        "<ul>" +
        "<li><b>ADMIN</b> — हर स्क्रीन देख सकते हैं, जिसमें Employees, Payroll, Suppliers, Reports शामिल हैं।</li>" +
        "<li><b>MANAGER</b> — Admin जैसा ही, रोज़ाना प्रबंधन की एक्सेस।</li>" +
        "<li><b>CASHIER</b> — सिर्फ POS, Station Display, Menu, Recipes, Inventory, Delivery, Time Clock देख सकते हैं।</li>" +
        "</ul>" +

        "<h2>2. पॉइंट ऑफ़ सेल (POS)</h2>" +
        "<p>मेनू को फ़िल्टर करने के लिए किसी कैटेगरी पर क्लिक करें, या सब देखने के लिए 'All Items' पर। " +
        "किसी आइटम कार्ड पर क्लिक करके उसे दाईं ओर मौजूदा ऑर्डर (कार्ट) में जोड़ें। ज़रूरत हो तो डिस्काउंट " +
        "लगाएं — 8% टैक्स अपने आप जुड़ जाता है।</p>" +
        "<p><b>ऑर्डर टाइप / सोर्स:</b> Dine-in, Pickup, या Delivery चुनें, और यह भी कि ऑर्डर वॉक-इन " +
        "ग्राहक (In-Store), फ़ोन कॉल, या ऑनलाइन से आया है। Phone/Online/Delivery ऑर्डर में ग्राहक का नाम " +
        "और फ़ोन नंबर ज़रूरी है; Delivery में पता भी चाहिए।</p>" +
        "<p><b>भुगतान:</b> Cash, Card, या Mobile Banking चुनें। Card/Mobile Banking चुनने पर एक पेमेंट " +
        "गेटवे स्क्रीन खुलती है (सिम्युलेटेड — अभी कोई असली बैंक खाता नहीं जुड़ा है) जहाँ आप प्रोवाइडर " +
        "(Visa/Mastercard या bKash/Nagad/Rocket) चुनकर कन्फर्म करते हैं।</p>" +
        "<p><b>चेकआउट:</b> भुगतान के बाद स्टॉक अपने आप घट जाता है और ऑर्डर किचन के लिए Station Display " +
        "पर भेज दिया जाता है। इसके बाद एक <b>Bill Preview</b> दिखता है — आप उसे प्रिंट कर सकते हैं, या " +
        "ग्राहक की पसंद के अनुसार SMS/Email से भेज सकते हैं।</p>" +

        "<h2>3. Station Display (किचन डिस्प्ले)</h2>" +
        "<p>हर पेंडिंग ऑर्डर आइटम को उस काउंटर के हिसाब से दिखाता है जो उसे बनाता है (Coffee Bar, " +
        "Bakery, Sandwich Station, आदि — मेनू कैटेगरी के हिसाब से सेट किया गया)। स्टाफ काम करते समय " +
        "<b>Start Preparing → Mark Ready → Served/Picked Up</b> पर क्लिक करते जाते हैं। स्क्रीन हर 5 " +
        "सेकंड में अपने आप रिफ्रेश होती है।</p>" +

        "<h2>4. मेनू आइटम</h2>" +
        "<p>प्रोडक्ट जोड़ें, एडिट करें, या बंद करें। हर आइटम की एक फ़ोटो हो सकती है — इमेज फ़ाइल चुनने के " +
        "लिए Browse पर क्लिक करें, या खाली छोड़ने पर एक कैटेगरी इमोजी दिखेगी।</p>" +

        "<h2>5. रेसिपी</h2>" +
        "<p>हर मेनू आइटम बनाने के चरण-दर-चरण निर्देश और ज़रूरी सामग्री/मात्रा दिखाता है — नए स्टाफ को " +
        "ट्रेनिंग देने में मददगार।</p>" +

        "<h2>6. इन्वेंटरी</h2>" +
        "<p>कच्चे माल के स्टॉक स्तर को ट्रैक करता है। कम स्टॉक वाले आइटम अपने आप फ़्लैग हो जाते हैं। " +
        "<b>Adjust Stock</b> से डिलीवरी, बर्बादी, या सुधार दर्ज करें — हर बदलाव लॉग होता है।</p>" +

        "<h2>7. डिलीवरी</h2>" +
        "<p>हर Delivery टाइप ऑर्डर यहाँ <b>UNASSIGNED</b> के रूप में आता है। इसे किसी इन-हाउस राइडर या " +
        "थर्ड-पार्टी एजेंसी (Pathao, Foodpanda, Steadfast, आदि) को असाइन करें — उन्हें ग्राहक से मेल " +
        "खाती ऑर्डर की एक कॉपी मिलती है, वे दुकान से उठाते हैं (<b>Mark Picked Up</b>), और ग्राहक के पते " +
        "पर पहुँचाते हैं (<b>Mark Delivered</b>)। जो ग्राहक खुद आकर ले जाते हैं वे Pickup टाइप इस्तेमाल " +
        "करते हैं और यहाँ कभी नहीं आते।</p>" +

        "<h2>8. Time Clock</h2>" +
        "<p>कर्मचारी यहाँ क्लॉक इन/आउट करते हैं। Admin/Manager किसी भी कर्मचारी को क्लॉक इन/आउट कर सकते " +
        "हैं (जब सबका अपना लॉगिन न हो तो उपयोगी); Cashier सिर्फ़ खुद को क्लॉक कर सकते हैं। एक लाइव सूची " +
        "दिखाती है कि अभी कौन शिफ्ट पर है।</p>" +

        "<h2>9. Payroll (सिर्फ़ Manager/Admin)</h2>" +
        "<p>एक कर्मचारी और तारीख़ की सीमा चुनें, <b>Calculate Hours &amp; Pay</b> दबाएँ ताकि उनकी पूरी " +
        "हो चुकी शिफ्टें दिखें। दिन के पहले 8 घंटे Regular पे होते हैं; उससे ज़्यादा Overtime (1.5 गुना) " +
        "होता है। कंपनी की छुट्टी वाले दिन 2.0 गुना, वीकेंड (डिफ़ॉल्ट शुक्र/शनि) पर 1.5 गुना मिलता है, और " +
        "ओवरटाइम इसके ऊपर भी जुड़ता है। छुट्टियों का कैलेंडर बनाने के लिए <b>Manage Holidays</b> " +
        "इस्तेमाल करें। फिर <b>Process Payment</b> से Direct Deposit (सिम्युलेटेड बैंक ट्रांसफर), Cash, " +
        "या Check से भुगतान करें, और चाहें तो पे-स्टब प्रिंट करें।</p>" +

        "<h2>10. Employees</h2>" +
        "<p>स्टाफ रिकॉर्ड, प्रति घंटा वेतन दर, भुगतान की पसंद, और बैंक विवरण (Direct Deposit के लिए) " +
        "मैनेज करें। किसी कर्मचारी के लिए <b>Create Login for Selected</b> से लॉगिन खाता बनाएँ।</p>" +

        "<h2>11. Suppliers और Reports</h2>" +
        "<p>Suppliers कच्चे माल की सोर्सिंग के लिए एक कॉन्टैक्ट डायरेक्टरी है। Reports में आज का सारांश, " +
        "दैनिक/आइटम/कैटेगरी बिक्री, कर्मचारी प्रदर्शन, और कम स्टॉक अलर्ट दिखते हैं — सभी CSV में " +
        "एक्सपोर्ट किए जा सकते हैं।</p>" +

        "<h2>12. लॉयल्टी प्रोग्राम</h2>" +
        "<p>चेकआउट पर <b>\"Register / apply loyalty points\"</b> पर टिक करें और ग्राहक का नाम व फ़ोन " +
        "नंबर डालें (ईमेल वैकल्पिक)। हर पूरे ऑर्डर पर 1 पॉइंट मिलता है। जब ग्राहक के <b>50 पॉइंट</b> हो " +
        "जाते हैं, तो उस ऑर्डर में उनके कार्ट का सबसे सस्ता आइटम अपने आप <b>मुफ़्त</b> हो जाता है, और " +
        "पॉइंट फिर से 0 से शुरू होते हैं। ईमेल रजिस्टर करने पर नया मेनू आइटम जुड़ने पर उन्हें ईमेल भी " +
        "मिलेगा।</p>" +

        "<h2>सिम्युलेटेड इंटीग्रेशन के बारे में नोट्स</h2>" +
        "<p>Payment Gateway, Direct Deposit, और Email/SMS भेजना — ये तीनों इस वर्शन में " +
        "<b>सिम्युलेटेड</b> हैं (अभी कोई असली मर्चेंट अकाउंट, बैंक API, या ईमेल/SMS प्रोवाइडर नहीं जुड़ा " +
        "है)। बाक़ी सब कुछ (ऑर्डर, स्टॉक, पेरोल गणना, रिपोर्ट, लॉयल्टी पॉइंट) पूरी तरह असली है और आपके " +
        "MySQL डेटाबेस में सेव होता है। असली प्रोवाइडर कहाँ जोड़ें, इसका पूरा विवरण README.md में है।</p>" +
        "</body></html>";

    public static final String ARABIC_HTML =
        "<html><body dir='rtl' style='font-family:Segoe UI; font-size:10pt; padding:8px;'>" +
        "<h1>بهوجان-آلوي — دليل المستخدم</h1>" +
        "<p>نظام نقاط بيع كامل لمطعم ومقهى: استلام الطلبات، توجيهها إلى المطبخ، المدفوعات، التوصيل، " +
        "المخزون، والرواتب — كل ذلك في تطبيق واحد.</p>" +

        "<h2>1. تسجيل الدخول</h2>" +
        "<p>أدخل اسم المستخدم وكلمة المرور. صلاحياتك تعتمد على دورك:</p>" +
        "<ul>" +
        "<li><b>ADMIN</b> — يرى جميع الشاشات، بما فيها الموظفون والرواتب والموردون والتقارير.</li>" +
        "<li><b>MANAGER</b> — مثل المدير تمامًا لكن دون قيود؛ صلاحيات الإدارة اليومية.</li>" +
        "<li><b>CASHIER</b> — يرى فقط نقطة البيع وشاشة المحطة والقائمة والوصفات والمخزون والتوصيل وساعة الدوام.</li>" +
        "</ul>" +

        "<h2>2. نقطة البيع (POS)</h2>" +
        "<p>انقر على فئة لتصفية قائمة الطعام، أو 'All Items' لعرض الكل. انقر على بطاقة صنف لإضافته إلى " +
        "الطلب الحالي (السلة) على الجانب. حدد خصمًا إذا لزم الأمر — تُحسب الضريبة (8%) تلقائيًا.</p>" +
        "<p><b>نوع/مصدر الطلب:</b> اختر تناول في المكان، استلام، أو توصيل، وما إذا كان الطلب من عميل " +
        "حضر شخصيًا، أو عبر الهاتف، أو عبر الإنترنت. طلبات الهاتف/الإنترنت/التوصيل تتطلب اسم العميل " +
        "ورقم هاتفه؛ التوصيل يتطلب أيضًا عنوانًا.</p>" +
        "<p><b>الدفع:</b> اختر نقدًا، بطاقة، أو دفعًا عبر الهاتف المحمول. تفتح البطاقة/الدفع عبر الهاتف " +
        "شاشة بوابة دفع (محاكاة — لا يوجد حساب مصرفي حقيقي متصل بعد) حيث تختار مزودًا " +
        "(Visa/Mastercard أو bKash/Nagad/Rocket) وتؤكد.</p>" +
        "<p><b>إتمام الطلب:</b> بعد الدفع، يُخصم المخزون تلقائيًا ويُرسل الطلب إلى شاشة المحطة للمطبخ. " +
        "ثم تظهر <b>معاينة الفاتورة</b> — يمكنك طباعتها، أو إرسالها إلكترونيًا (رسالة نصية/بريد " +
        "إلكتروني) للعميل حسب رغبته.</p>" +

        "<h2>3. شاشة المحطة (شاشة المطبخ)</h2>" +
        "<p>تعرض كل صنف طلب معلّق مجمّعًا حسب محطة التحضير التي تعدّه (بار القهوة، المخبز، محطة " +
        "الساندويتش، إلخ — يتم إعدادها حسب فئة القائمة). ينقر الموظفون على <b>بدء التحضير ← وضع علامة " +
        "جاهز ← تم التقديم/الاستلام</b> أثناء العمل. تتحدّث الشاشة تلقائيًا كل 5 ثوانٍ.</p>" +

        "<h2>4. أصناف القائمة</h2>" +
        "<p>أضف أو عدّل أو عطّل المنتجات. يمكن أن يكون لكل صنف صورة — انقر على استعراض لاختيار ملف " +
        "صورة، أو اتركه فارغًا لعرض رمز تعبيري للفئة بدلاً من ذلك.</p>" +

        "<h2>5. الوصفات</h2>" +
        "<p>تعرض تعليمات التحضير خطوة بخطوة والمكونات/الكميات الدقيقة اللازمة لكل صنف — مفيدة لتدريب " +
        "الموظفين الجدد.</p>" +

        "<h2>6. المخزون</h2>" +
        "<p>يتتبع مستويات مخزون المكونات الخام. تُعلَّم الأصناف منخفضة المخزون تلقائيًا. استخدم " +
        "<b>تعديل المخزون</b> لتسجيل توريد، هدر، أو تصحيح — يُسجَّل كل تغيير.</p>" +

        "<h2>7. التوصيل</h2>" +
        "<p>كل طلب من نوع توصيل يصل إلى هنا بحالة <b>غير معيّن</b>. عيّنه لسائق داخلي أو وكالة خارجية " +
        "(Pathao، Foodpanda، Steadfast، إلخ) — يستلمون نسخة من الطلب مطابقة للعميل، يجمعونها من المتجر " +
        "(<b>وضع علامة تم الاستلام</b>)، ويوصلونها إلى عنوان العميل (<b>وضع علامة تم التوصيل</b>). " +
        "العملاء الذين يستلمون شخصيًا يستخدمون نوع الاستلام بدلاً من ذلك ولا يظهرون هنا أبدًا.</p>" +

        "<h2>8. ساعة الدوام</h2>" +
        "<p>يسجّل الموظفون حضورهم وانصرافهم هنا. يمكن للمدراء تسجيل حضور/انصراف أي موظف (مفيد إذا لم " +
        "يكن لدى الجميع حساب دخول خاص بهم)؛ الكاشير يمكنه تسجيل نفسه فقط. تعرض قائمة مباشرة من هو في " +
        "نوبة عمل الآن.</p>" +

        "<h2>9. الرواتب (للمدير فقط)</h2>" +
        "<p>اختر موظفًا ونطاق تاريخ، وانقر على <b>حساب الساعات والأجر</b> لجلب نوباته المكتملة. أول 8 " +
        "ساعات/يوم أجر عادي؛ وما يتجاوزها عمل إضافي (1.5x). النوبات في عطلة الشركة تُدفع 2.0x، وعطلات " +
        "نهاية الأسبوع (الجمعة/السبت افتراضيًا) تُدفع 1.5x، والعمل الإضافي يُضاف فوق ذلك. استخدم " +
        "<b>إدارة العطلات</b> لبناء تقويم العطلات. ثم <b>معالجة الدفع</b> عبر الإيداع المباشر (تحويل " +
        "مصرفي محاكى)، نقدًا، أو شيكًا، واطبع قسيمة الراتب اختياريًا.</p>" +

        "<h2>10. الموظفون</h2>" +
        "<p>أدر سجلات الموظفين، وأجر الساعة، وتفضيل الدفع، والبيانات المصرفية (للإيداع المباشر). أنشئ " +
        "حساب دخول لموظف عبر <b>إنشاء دخول للمحدد</b>.</p>" +

        "<h2>11. الموردون والتقارير</h2>" +
        "<p>الموردون دليل جهات اتصال لتوريد المكونات. تعرض التقارير ملخص اليوم، والمبيعات اليومية/حسب " +
        "الصنف/الفئة، وأداء الموظفين، وتنبيهات انخفاض المخزون — كلها قابلة للتصدير كملف CSV.</p>" +

        "<h2>12. برنامج الولاء</h2>" +
        "<p>عند إتمام الطلب، ضع علامة على <b>\"Register / apply loyalty points\"</b> وأدخل اسم العميل " +
        "ورقم هاتفه (البريد الإلكتروني اختياري). كل طلب مكتمل يكسب نقطة واحدة. عند وصول العميل إلى " +
        "<b>50 نقطة</b>، يصبح أرخص صنف في سلته <b>مجانيًا</b> تلقائيًا في ذلك الطلب، وتُعاد نقاطه إلى " +
        "الصفر. تسجيل البريد الإلكتروني يتيح لهم أيضًا تلقي إشعار عند إضافة صنف جديد للقائمة.</p>" +

        "<h2>ملاحظات حول عمليات الدمج المحاكاة</h2>" +
        "<p>بوابة الدفع، والإيداع المباشر، وإرسال البريد الإلكتروني/الرسائل النصية كلها <b>محاكاة</b> " +
        "في هذا الإصدار — لا يوجد حساب تاجر حقيقي، أو واجهة مصرفية، أو مزود بريد/رسائل نصية متصل بعد. " +
        "كل شيء آخر (الطلبات، المخزون، حسابات الرواتب، التقارير، نقاط الولاء) حقيقي بالكامل ويُحفظ في " +
        "قاعدة بيانات MySQL الخاصة بك. راجع README.md لمعرفة أين يمكن للمطوّر ربط مزودين حقيقيين.</p>" +
        "</body></html>";
}
