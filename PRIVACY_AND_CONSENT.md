# DriveFlow Driver - Privacy, Security & Consent Commitments

---

## [العربية] وثيقة الخصوصية والأمان والتعهدات القانونية

تضع منظومة **DriveFlow** خصوصية وأمان السائق والركاب في المقام الأول وفق المبادئ الصارمة التالية:

### 1. ماذا يقرأ التطبيق تحديداً؟
- يقرأ التطبيق واجهة ثلاثة تطبيقات فقط: **inDrive (sinet.startup.inDriver)**، و **Uber Driver (com.ubercab.driver)**، و **DiDi Driver (com.didiglobal.driver)**.
- البيانات المقروءة تقتصر حصرياً على:
  1. سعر الأجرة المعروض والعملة.
  2. مسافة وزمن الوصول للراكب (Pickup distance & minutes).
  3. مسافة المشوار الإجمالية (Trip distance).
  4. تقييم الراكب وعدد رحلاته (إن توفر).
  5. اسم المنطقة أو الشارع فقط لاحتساب التوجه والمسافة.
  6. ملاحظات الراكب (للكشف عن الكلمات المحظورة فقط).

### 2. ما لا يفعله التطبيق إطلاقاً:
- ❌ **لا يلمس الشاشة نهائياً**: التطبيق لا يملك أي دالة تحكم أو ضغط (`performAction` أو `dispatchGesture` محظورة ومحذوفة تماماً من الشفرة المصدرية). التطبيق يقرأ ويحسب ويعرض فقط.
- ❌ **لا يقبل ولا يرفض ولا يفاوض**: حرية القرار والقبول أو الرفض ترجع للسائق 100%.
- ❌ **لا يخزن أو يسجل أي بيانات للركاب**: أسماء الركاب أو أرقام هواتفهم أو سجل مشاويرهم لا تُسجل ولا تُخزن في أي قاعدة بيانات محلية، وجميع الحسابات الرياضية تتم في الذاكرة العشوائية اللحظية (RAM) وتختفي فور إغلاق الطلب.

### 3. انعدام الخوادم الخارجية (Zero Server Architecture):
- التطبيق **يعمل بالكامل بدون خادم (Offline)**.
- لا توجد خوادم تتبع، ولا إعلانات، ولا تحليلات طرف ثالث.
- الشيء الوحيد الذي يخرج من الهاتف هو الرسالة النصية التي يرسلها السائق بنفسه وبإرادته الكاملة عبر تطبيق واتساب إلى مالك الخدمة لتفعيل الاشتراك، وتتضمن: (كود الجهاز، اسم الكابتن، رقم هاتفه، وإيصال الدفع). ولا تحتوي على أي بيانات متعلقة بالركاب.

### 4. إخلاء مسؤولية بخصوص شروط تطبيقات التوصيل:
- بعض شركات تطبيقات التوصيل (مثل أوبر، إن درايف، دي دي) قد تنص في شروط استخدامها على قيود متعلقة باستخدام برامج قراءة الشاشة أو النوافذ العائمة المساعدة.
- استخدامك للتطبيق يتم بمسؤوليتك واختيارك الشخصي كأداة حاسبة مساعدة لتحسين كفاءة استهلاك وقود سيارتك ومعرفة الجدوى الاقتصادية للمشاوير المعروضة عليك.

---

## [English] Privacy, Security & Legal Commitments

DriveFlow is architected with a strict privacy-first, zero-server philosophy.

1. **Scope of Inspection**:
   - The AccessibilityReader inspects only foreground windows belonging to:
     - `sinet.startup.inDriver`
     - `com.ubercab.driver`
     - `com.didiglobal.driver`
   - It ignores all other system and user applications.

2. **Read-Only Guarantee**:
   - The codebase strictly omits and forbids calls to `AccessibilityNodeInfo.performAction` and `AccessibilityService.dispatchGesture`.
   - The app has no mechanical ability to accept, decline, or counter-offer requests.

3. **Ephemeral Processing & No Passenger Storage**:
   - Offer evaluation occurs purely in transient volatile memory.
   - No passenger names, personal identifiers, contact numbers, or trip histories are written to flash storage or databases.

4. **Zero Backend Servers**:
   - DriveFlow operates completely standalone on the driver's phone.
   - There are no telemetry analytics, tracking SDKs, or cloud databases.
   - The only outbound network communication is when the driver voluntarily taps to open WhatsApp with their device identifier for license issuance.

5. **Platform Terms Notice**:
   - Drivers are advised that ride-hailing platforms may update their driver partner terms regarding third-party overlay utilities. Use of this advisory tool is at the driver's discretion.
