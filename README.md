# Beşiktaş • Qara Qartal — Android vidceti

Müstəqil azarkeş tətbiqi: Android ana ekranına 4×2 Beşiktaş vidceti, şəffaf qartal dizaynı və kliklə açılan canlı xəbər lenti.

- Vidcetə basdıqda xəbər ekranı açılır.
- **Futbol**, **Basketbol**, **Transfer**, **Klub** bölmələri.
- Google News RSS mənbəyindən canlı xəbərlər; oxumaq üçün xəbər başlığına toxunun.
- Xəbərlər yeniləmə düyməsi ilə yenilənir; rəsmi Beşiktaş saytına ayrıca keçid var.
- Bu proqram Beşiktaş JK-nın rəsmi tətbiqi deyil.

## APK yüklə

**Actions → Android APK →** ən son uğurlu icra → **Artifacts → besiktas-widget-apk**. ZIP-i açın, `app-debug.apk` faylını Android telefonuna quraşdırın.

Vidcet əlavə etmək: telefonun ana ekranında boş yerə uzun basın → **Widgets / Vidcetlər → Beşiktaş • Kartal**.

## Mənbə və texnologiya

Java, Android SDK 35, minimum Android 8.0. GitHub Actions Gradle 8.7 və AGP 8.6.1 istifadə edir. GitHub Actions-un yaratdığı APK test/debug imzasına malikdir; Play Store üçün ayrıca release signing tələb olunur. İstifadə üçün telefonun tətbiq quraşdırmasına icazəsi lazımdır. Google News RSS əlçatanlığı dəyişə bilər.

### Dizayn

Qartal klassik klub gerbi deyil; qarışıq qara-gümüş rəngli şəffaf vektor motividir, əsas ekranda fon üzərində uçan siluet təsiri verir.
