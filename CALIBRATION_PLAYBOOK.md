# DriveFlow Driver - Calibration Playbook (دليل المعايرة الفنية)

---

## [العربية] دليل المالك لضبط ومعايرة قراءة الشاشة (Uber & DiDi)

تستخدم تطبيقات أوبر ودي دي خوارزميات التقاط الشاشة ومطابقة النصوص (OCR) لقراءة تفاصيل الطلب. نظراً لتعدد أبعاد الشاشات وكثافات العرض (Display Densities) بين هواتف السائقين (سامسونج، شاومي، أوبو)، يتيح درايف فلو شاشة فحص ومعايرة هندسية متطورة لتعديل مربعات الاقتصاص وحفظها ومشاركتها مع جميع السائقين دون الحاجة لإعادة برمجة التطبيق.

### 1. طريقة فتح شاشة الفحص الفني (Diagnostics)
1. افتح تطبيق DriveFlow Driver وانتقل إلى تبويب **المساعدة** (Help).
2. انزل إلى أسفل الشاشة عند نص **إصدار التطبيق** (App Version).
3. اضغط على رقم الإصدار **7 مرات متتالية وسريعة**.
4. ستظهر رسالة تأكيد ويتم نقلك فوراً إلى **شاشة الفحص والتشخيص الفني** (`DiagnosticsScreen`).

### 2. التقاط صورة من تطبيق السائق (Uber / DiDi)
1. افتح تطبيق أوبر أو دي دي حتى يظهر طلب مشوار على الشاشة.
2. عد إلى شاشة المعايرة في درايف فلو واضغط على **التقاط عينة شاشة** (Capture Sample).
3. سيتم حفظ لقطة الشاشة الحالية محلياً لعرضها داخل محرر المعايرة.

### 3. تعديل مربعات الاقتصاص (Crop Rectangles)
تظهر لك صورة الطلب وعليها مربعات ملونة تحدد أماكن الحقول:
- **المربع الأخضر (السعر - Fare)**: اضبط أبعاده ليغطي خانة الأجرة والعملة بالكامل.
- **المربع الأزرق (مسافة وزمن البيك أب - Pickup)**: غطِّ به المسافة وزمن الوصول للراكب.
- **المربع البرتقالي (مسافة المشوار - Trip Distance)**: في أوبر ودي دي، تأكد من شمول المسافة الإجمالية بالكيلومتر.
- **المربع البنفسجي (تقييم الراكب - Rating)**: غطِّ به رقم التقييم النجمي (مثال: 4.85).

*استخدم أدوات التكبير/التصغير وأسهم المحاذاة الدقيقة لتثبيت المربعات.*

### 4. اختبار المعايرة مباشرة (Live OCR Test)
1. اضغط على زر **اختبار القراءة الآن** (Test OCR).
2. سيقوم المحرك بمعالجة الصورة وإظهار القيم المستخرجة:
   - السعر والعملة: `125 EGP`
   - المسافة الإجمالية: `14.2 km`
   - تقييم الراكب: `4.9`
3. تأكد أن نسبة التطابق ومستوى الثقة (Confidence) أعلى من 90%.

### 5. تصدير واستيراد الإعدادات لجميع الهواتف (JSON Export / Import)
1. بعد التأكد من دقة القراءة، اضغط على **تصدير المعايرة** (Export Calibration).
2. سيتم نسخ كود JSON للإعدادات إلى الحافظة.
3. يمكنك حفظ ملف الإعدادات وإرساله للسائقين الجدد أو دمجه في ملف الإعدادات الافتراضية للتطبيق ليتم تطبيقه على جميع الهواتف ذات نفس الأبعاد بضغطة زر عبر **استيراد المعايرة** (Import Calibration).

---

## [English] Owner Calibration Playbook for Screen Reading (Uber & DiDi)

DriveFlow Driver uses real-time screen capture and OCR crop boundaries to parse ride offers in Uber Driver and DiDi Driver. Screen sizes and resolutions differ across devices (Samsung, Xiaomi, Oppo, Vivo). This playbook guides the owner through calibrating and tuning OCR bounding boxes.

### 1. Accessing the Hidden Diagnostics Screen
1. Open DriveFlow Driver and navigate to the **Help** tab.
2. Scroll to the bottom where the **App Version** label is displayed.
3. **Tap the App Version text 7 consecutive times**.
4. The system will unlock and navigate directly to the **Diagnostics & Calibration Screen**.

### 2. Capturing Driver App Screenshot
1. Switch to Uber Driver or DiDi Driver with a live request or test card visible.
2. Return to the DriveFlow Diagnostics tool and tap **Capture Sample Frame**.
3. The captured frame will load into the bounding box editor.

### 3. Adjusting Crop Rectangles
Align the bounding zones with pixel accuracy:
- **Fare Zone (Green)**: Wrap the fare amount and currency text.
- **Pickup Zone (Blue)**: Wrap pickup distance and ETA in minutes.
- **Trip Distance Zone (Orange)**: Wrap the trip km indicator.
- **Rider Rating Zone (Purple)**: Wrap the rider star rating (e.g., 4.92).

### 4. Running Verification Test
1. Tap **Test Extraction**.
2. Verify that:
   - Fare matches the card (e.g., `85.00 EGP`).
   - Total trip distance is accurately parsed (`11.5 km`).
   - Confidence score is >= 0.90.

### 5. Exporting & Distributing Calibration JSON
1. Tap **Export Calibration JSON** to copy the calibration block.
2. Distribute this configuration snippet to other drivers via WhatsApp or pre-bake it into `SettingsStore` defaults for identical device models.
3. Drivers can paste the JSON into Diagnostics -> **Import Calibration** to sync instantly.
