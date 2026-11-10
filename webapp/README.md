# Bhojan-Aloy Web Demo (QA Automation)

এটা মূল Swing ডেস্কটপ অ্যাপের POS ফ্লো-র একটা ওয়েব ভার্সন — পাবলিক QA automation
ডেমো (Selenium / WebdriverIO / Playwright) হিসেবে ব্যবহারের জন্য বানানো। মূল
`DAO`/`Service`/`Model` ক্লাসগুলো অপরিবর্তিত রিইউজ করা হয়েছে (`src/com/bhojanaloy/{dao,model,service,util}`
থেকে হুবহু কপি করা) — শুধু `DBConnection` env var সাপোর্ট করার জন্য একটু বদলানো হয়েছে,
আর Swing UI-র বদলে একটা Spring Boot REST API + প্লেইন HTML/JS ফ্রন্টএন্ড যোগ হয়েছে।

## যা আছে (স্কোপ)

- লগইন/লগআউট (সেশন-বেসড)
- মেনু দেখা (`/api/menu`)
- কার্টে আইটেম যোগ, চেকআউট (`/api/orders/checkout`)
- অর্ডার হিস্ট্রি + একটা অর্ডারের বিস্তারিত (`/api/orders`, `/api/orders/{id}`)
- হেলথ চেক (`/api/health`)

ইনভেন্টরি ম্যানেজমেন্ট, পেরোল, ডেলিভারি ড্যাশবোর্ড ইত্যাদি (Swing-এ যা আছে)
ইচ্ছাকৃতভাবে বাদ দেওয়া হয়েছে — QA প্র্যাকটিসের জন্য মূল POS ফ্লো-ই যথেষ্ট।

## লগইন ক্রেডেনশিয়াল (স্যাম্পল ডেটা থেকে)

মূল প্রজেক্টের README অনুযায়ী (`admin123` পাসওয়ার্ড দিয়ে):
- `admin` / `admin123` — ADMIN
- `farida` / `admin123` — MANAGER (employee_id সহ, checkout করতে পারবে)
- `sabbir` / `admin123` — CASHIER (employee_id সহ, checkout করতে পারবে)

যদি লগইন ফেইল করে, মূল প্রজেক্টের README-র "First login" সেকশন অনুযায়ী
`PasswordUtil` দিয়ে হ্যাশ রিজেনারেট করে নিন।

## লোকালি বিল্ড ও রান করা

প্রয়োজন: JDK 11+, Maven (`brew install maven` ম্যাকে)।

```
cd webapp
mvn clean package
java -jar target/bhojanaloy-webapp.jar
```

ডিফল্টভাবে এটা `localhost:3306`-এর `bhojan_aloy` ডাটাবেসে (user: root,
password: `Admin123!`) কানেক্ট করার চেষ্টা করবে — ঠিক ডেস্কটপ অ্যাপের মতোই।
আলাদা DB credential দিতে চাইলে এনভায়রনমেন্ট ভ্যারিয়েবল সেট করুন:

```
DB_URL="jdbc:mysql://HOST:3306/bhojan_aloy?useSSL=false&serverTimezone=UTC" \
DB_USER=bhojan_web \
DB_PASSWORD=your_password \
java -jar target/bhojanaloy-webapp.jar
```

তারপর ব্রাউজারে `http://localhost:8080` খুললে লগইন পেজ দেখাবে।

## VM-এ ডিপ্লয় করা (Oracle Cloud Free VM ধরে)

আগের কথোপকথনে দেওয়া ধাপ ১-৭ (VM তৈরি, Java+MySQL+Nginx ইনস্টল, ডাটাবেস ইমপোর্ট)
অনুসরণ করার পর:

1. জার ফাইল কপি করুন:
   ```
   scp -i key.pem target/bhojanaloy-webapp.jar ubuntu@SERVER_IP:~
   ```
2. `deploy/bhojanaloy-web.service` ফাইলটা `/etc/systemd/system/`-এ কপি করে
   `DB_PASSWORD` বসিয়ে চালু করুন:
   ```
   sudo cp deploy/bhojanaloy-web.service /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable --now bhojanaloy-web
   sudo systemctl status bhojanaloy-web
   ```
3. `deploy/nginx-bhojanaloy.conf`-এ আপনার ডোমেইন/IP বসিয়ে
   `/etc/nginx/sites-available/` + `sites-enabled/`-এ লিংক করুন, তারপর
   `sudo certbot --nginx -d আপনার_ডোমেইন` দিয়ে ফ্রি SSL নিন।
4. প্রতিদিন ডেমো ডেটা রিসেট করতে `deploy/reset_demo_data.sql` cron-এ বসান
   (ফাইলের ভেতরের কমেন্টে exact cron লাইন আছে)।

## QA Automation-এর জন্য নোট

সব গুরুত্বপূর্ণ এলিমেন্টে স্থিতিশীল `id` বসানো আছে, যাতে Selenium/WebdriverIO/
Playwright locator লিখতে সহজ হয়:
- `#username`, `#password`, `#login-btn`, `#login-error`
- `#menu-item-{itemId}`, `#add-item-{itemId}`
- `#cart-row-{itemId}`, `#qty-increase-{itemId}`, `#qty-decrease-{itemId}`, `#remove-{itemId}`
- `#checkout-btn`, `#checkout-error`, `#checkout-success` (`data-order-id` অ্যাট্রিবিউটসহ)
- `#orders-table`, প্রতি সারিতে `data-order-id`

## সীমাবদ্ধতা / প্রোডাকশন নয়

এটা একটা পাবলিক **টেস্টিং ডেমো**, প্রোডাকশন সিস্টেম না:
- পাসওয়ার্ড হ্যাশিং SHA-256+salt (bcrypt/argon2 না) — শুধু ডেমো ডেটার জন্য ঠিক আছে
- রেট লিমিটিং/CAPTCHA নেই — পাবলিক এক্সপোজারে abuse হতে পারে, প্রয়োজনে Nginx-এ
  `limit_req` যোগ করুন
- ডেমো ডেটা নিয়মিত রিসেট করা জরুরি (উপরে দেখুন)
