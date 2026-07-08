-- =====================================================================
-- BHOJAN-ALOY — Migration v5: Multilingual Recipe Instructions
-- Run this AFTER migration_v2/v3/v4 if you are upgrading an existing
-- database instead of re-importing the full schema.
-- =====================================================================
USE bhojan_aloy;

ALTER TABLE recipes
    ADD COLUMN instructions_bn TEXT NULL AFTER instructions,
    ADD COLUMN instructions_es TEXT NULL AFTER instructions_bn,
    ADD COLUMN instructions_hi TEXT NULL AFTER instructions_es,
    ADD COLUMN instructions_ar TEXT NULL AFTER instructions_hi;

-- Optional: translate your existing sample recipes into Bangla/Spanish/Hindi/Arabic.
-- (Safe to skip — the Recipes screen falls back to showing the English
-- instructions whenever a language's translation is empty.)
UPDATE recipes SET
    instructions_bn = '১. ১৮ গ্রাম এসপ্রেসো বিন মিহি করে গ্রাইন্ড করুন।\n২. পোর্টাফিল্টারে সমানভাবে ট্যাম্প করুন।\n৩. কাপে ২৫-৩০ সেকেন্ড ধরে এক্সট্র্যাক্ট করুন।\n৪. সাথে সাথে পরিবেশন করুন।',
    instructions_es = '1. Muela finamente 18g de granos de espresso.\n2. Prensa uniformemente en el portafiltro.\n3. Extrae durante 25-30 segundos en la taza.\n4. Sirve inmediatamente.',
    instructions_hi = '1. 18 ग्राम एस्प्रेसो बीन्स को बारीक पीस लें।\n2. पोर्टाफिल्टर में समान रूप से दबाएं।\n3. कप में 25-30 सेकंड तक एक्सट्रैक्ट करें।\n4. तुरंत परोसें।',
    instructions_ar = '1. اطحن 18 جرامًا من حبوب الإسبريسو ناعمًا.\n2. اضغط بالتساوي في حامل الفلتر.\n3. استخلص لمدة 25-30 ثانية في الكوب.\n4. قدّمه فورًا.'
WHERE recipe_name = 'Perfect Espresso';

UPDATE recipes SET
    instructions_bn = '১. একটা ডাবল এসপ্রেসো শট নিন।\n২. দুধ স্টিম করে মাইক্রোফোম করুন (৬৫°সে)।\n৩. স্টিম করা দুধ ঢালুন, তারপর ওপরে ফোম চামচ দিয়ে দিন।\n৪. ইচ্ছে হলে ওপরে কোকো গুঁড়ো ছিটিয়ে দিন।',
    instructions_es = '1. Extrae un espresso doble.\n2. Vaporiza la leche hasta obtener microespuma (65°C).\n3. Vierte la leche vaporizada y luego coloca la espuma encima con una cuchara.\n4. Espolvorea con cacao si lo deseas.',
    instructions_hi = '1. एक डबल एस्प्रेसो शॉट निकालें।\n2. दूध को माइक्रोफोम तक भाप दें (65°C)।\n3. भाप वाला दूध डालें, फिर ऊपर से फोम चम्मच से डालें।\n4. चाहें तो कोको छिड़कें।',
    instructions_ar = '1. اسحب جرعة إسبريسو مزدوجة.\n2. بخّر الحليب حتى يصبح رغوة دقيقة (65 درجة مئوية).\n3. اسكب الحليب المبخر ثم ضع الرغوة فوقه بالملعقة.\n4. رشّ الكاكاو إذا رغبت.'
WHERE recipe_name = 'Cappuccino';

UPDATE recipes SET
    instructions_bn = '১. ময়দা, ইস্ট, চিনি ও পানি মিশিয়ে ১০ মিনিট মথুন (নিড করুন)।\n২. ১ ঘণ্টা প্রুফ (ফুলতে দিন)।\n৩. রিং আকারে গড়ে প্রতি পাশে ৩০ সেকেন্ড করে সিদ্ধ করুন।\n৪. ২২০°সে তাপমাত্রায় ২০ মিনিট বেক করুন।',
    instructions_es = '1. Mezcla harina, levadura, azúcar y agua; amasa 10 min.\n2. Deja reposar (fermentar) 1 hora.\n3. Forma los anillos y hierve 30 segundos por cada lado.\n4. Hornea a 220°C durante 20 minutos.',
    instructions_hi = '1. आटा, यीस्ट, चीनी और पानी मिलाएं; 10 मिनट गूंधें।\n2. 1 घंटे के लिए प्रूफ (फूलने) दें।\n3. रिंग का आकार दें, हर तरफ 30 सेकंड उबालें।\n4. 220°C पर 20 मिनट बेक करें।',
    instructions_ar = '1. اخلط الدقيق والخميرة والسكر والماء؛ اعجن لمدة 10 دقائق.\n2. اترك العجين يرتاح لمدة ساعة واحدة.\n3. شكّل الحلقات واسلقها 30 ثانية على كل جانب.\n4. اخبز على حرارة 220 درجة مئوية لمدة 20 دقيقة.'
WHERE recipe_name = 'Plain Bagels';
