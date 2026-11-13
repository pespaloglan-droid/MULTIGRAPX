# MultiGrapx · Black Edition

Türkçe arayüzlü, çevrimdışı Android duvar kâğıdı ve görsel uygulaması.

## Teslim durumu
Kaynak kodu hazırlanmıştır. **Bu paket derlenmiş APK içermez.** Hazırlandığı ortamda Java, Android SDK, Gradle ve dış ağ erişimi bulunmadığından derleme, Android lint ve cihaz testleri yapılamadı. Üretime hazır veya cihazda doğrulanmış olarak değerlendirmeyin.

## Özellikler
- Siyah/antrasit arayüz ve altın vurgular.
- Duvar Kâğıtları, Görseller ve Favoriler sekmeleri.
- Money, Love, Luxury Life, Black ve Lonely: her kategoride 2, toplam 10 özgün eser.
- Kategori filtreleme, başlık arama ve cihazda saklanan favoriler.
- 4K (2160 × 3840) ve 8K (4320 × 7680) indirme.
- Ana ekran, kilit ekranı veya ikisine duvar kâğıdı uygulama.
- Görsel tasarım promptunu panoya kopyalama.
- Hesap, reklam, analitik, internet ve API anahtarı gerektirmez.

Görseller Python çizim algoritmasıyla doğrudan 4320 × 7680 pikselde üretilmiştir. Fotoğraf veya AI çıktısı değildir; özgün, minimal dijital sanattır. Promptlar tasarım betimleridir, AI ile birebir tekrar üretim garantisi vermez. Uygulama içinde AI üretim hizmeti yoktur.

## Gereksinimler
Android 11+ (API 30); derleme için JDK 17, Gradle 8.11.1, Android SDK 35 ve Build Tools 35.0.0. Android Gradle Plugin 8.9.1 kullanılır. İlk derleme internete ihtiyaç duyar.

## APK elde etme: GitHub Actions
1. ZIP'i açın. MultiGrapx klasörünün **içeriğini**, gizli `.github` klasörü dahil, kendi GitHub deponuzun köküne koyun. `settings.gradle` depo kökünde olmalı.
2. Actions > Build MultiGrapx APK > Run workflow seçin. main/master'a push da çalıştırır.
3. Başarılı koşunun Artifacts bölümündeki MultiGrapx-APK dosyasını indirin. İçinde kurulabilir `MultiGrapx-1.0.0-debug.apk` bulunur.

İş akışı yalnızca hazırlanmıştır; sizin adınıza depoya gönderilmedi ve çalıştırılmadı. CI debug imzası koşular arasında değişebilir; güncelleme çatışırsa eski debug uygulamayı kaldırmak gerekebilir ve favoriler silinir.

## Yerel derleme
Android Studio'da proje klasörünü açın, SDK yolunu ayarlayın. Wrapper JAR dahil değildir. Sistem Gradle 8.11.1 ile `gradle wrapper --gradle-version 8.11.1` çalıştırarak wrapper oluşturabilirsiniz.

```sh
gradle --no-daemon :app:assembleDebug :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Mağazaya yayın için kendi güvenli yayın anahtarınızla Signed App Bundle / APK üretin. Anahtarları ve parolaları depoya koymayın. Bu pakette yayın imzası yoktur.

## Yapı
- MainActivity.java: native Android UI ve işlemler.
- assets/catalog.json: başlık, kategori, prompt, boyut.
- assets/art: 10 tam 8K JPEG; assets/thumb: 360 × 640 önizlemeler.
- tools/generate_art.py: koleksiyonu yeniden üreten kod; Python + Pillow gerekir.
- .github/workflows/android.yml: APK derleme ve lint.

## Davranış ve sınırlar
8K indirme orijinal JPEG'i MediaStore ile Pictures/MultiGrapx klasörüne kopyalar. 4K indirme 2 oranında örneklenir. Tüm galeriye erişim izni istenmez. Duvar kâğıdı 4K örnekle uygulanır; cihaz görüntüyü kırpabilir/ölçekleyebilir. Kilit ekranı desteği üretici ve cihaz politikalarına bağlıdır. 8K ekran gösterimi garanti edilmez.

Duvar Kâğıtları ve Görseller aynı koleksiyonu paylaşır. Yönetim paneli, üyelik, çevrimiçi katalog ve AI üretimi dahil değildir. Yeni içerik katalog ve dosyalar eklenerek yeniden derlenir. İşlemler arka planda sıraya alınır; kalıcı iş kuyruğu yoktur. Uygulama sistemce öldürülürse işlem yarıda kalabilir. Düşük bellekli cihazlarda 4K işlemleri başarısız olabilir.

## Cihaz testi gerekli
Android 11 ve 15'te temiz kurulum; arama/kategoriler/favoriler; yeniden açılış; tüm 4K/8K indirmelerin gerçek boyutları; ana/kilit ekranı; prompt kopyalama; boş depolama; düşük bellek; art arda dokunma; TalkBack; büyük yazı; ekran döndürme; kesikler ve gezinme çubukları.

## İçerik izni
Dahil edilen özgün prosedürel görselleri ve kodu kullanabilir, değiştirebilir ve ticari olarak dağıtabilirsiniz. Garanti, münhasırlık veya marka uygunluğu taahhüt edilmez. Üçüncü taraf stok fotoğrafı dahil değildir.
