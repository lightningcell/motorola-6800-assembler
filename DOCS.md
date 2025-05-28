# Asmj.java Geliştirici Dökümantasyonu

Bu doküman, `Asmj.java` dosyasındaki ana fonksiyonların ve sınıfların açıklamalarını, kullanım amaçlarını ve Motorola 6800 assembler projesinde nasıl kullanılabileceğini özetler. Projenin gereksinimleri ve mimarisi için referans: `May 21, 2025 01-35-32 AM Markdown Content.md`.

---

## Genel Bakış

`Asmj.java`, Motorola 6800 için bir assembler'ın çekirdek mantığını içerir. Assembly kodunu makine koduna çevirme, sembol tablosu yönetimi, hata raporlama ve çıktı üretimi gibi temel işlevleri sağlar. 

---

## Temel Sınıflar ve Fonksiyonlar

### 1. **Asmj (Ana Sınıf)**
- **Amaç:** Assembler'ın ana akışını ve temel işlemlerini yönetir.
- **Kullanım:** Konsol veya GUI üzerinden çağrılır, dosya veya metin girdisi alır, çıktıları üretir.

#### Ana Metotlar:

- **assemble(...)**
  - Assembly kodunu okur, işler ve çıktıları üretir.
  - Parametreler: Girdi akışları, çıktı akışları, sembol listeleri.
  - Kullanım: Projenin ana derleme fonksiyonu olarak çağrılır.

- **pass0(...)**
  - Assembly satırlarını okur, makro ve temel satır analizini yapar.
  - Makro tanımları ve kaynak satırlarının ilk işlenmesi.

- **pass1(...)**
  - Sembollerin ve adreslerin çözülmesi, makro ve koşullu derleme işlemleri.
  - Label çözümlemesi ve sembol tablosu oluşturma.

- **pass2(...)**
  - Makine kodu üretimi ve hafızaya yazılması.
  - Sembolleri ve adresleri kullanarak gerçek kod üretimi.

- **emitListing(...)**
  - Assembly ve makine kodu eşlemesini, hata mesajlarını ve çıktıları listeler.
  - Kullanıcıya satır-satır eşleme tablosu sunmak için kullanılır.

- **emitErrors(...)**
  - Hataları toplar ve kullanıcıya raporlar.

- **writeBinary(...)**
  - Makine kodunu ikili dosya olarak yazar.

- **writeSRecords(...)**
  - S-record formatında çıktı üretir (isteğe bağlı).

- **formatCode(...) / formatHex(...)**
  - Adres ve makine kodu çıktısını biçimlendirir.

- **error(...)**
  - Bir satıra hata mesajı ekler.

---

### 2. **LineContext (İç Sınıf)**
- **Amaç:** Assembly dosyasındaki satırların ve makro/koşullu derleme bağlamının yönetimi.
- **Kullanım:** Satırların işlenmesi, makro ve koşullu blokların takibi.

#### Temel Metotlar:
- **refreshState, getTopConditional, popContext, pushContext, processInstructions, beginMacro, beginIf, assertIf, assertPastElse, hideInMacro, assertEndOfContext, getDepth**
- Makro ve koşullu derleme bloklarının yönetimi için kullanılır.

---

### 3. **Yardımcı Fonksiyonlar**
- **pad(...)**: Stringleri sabit uzunluğa getirir.
- **die(...)**: Kritik hata durumunda programı sonlandırır.
- **usage(...)**: Komut satırı kullanım bilgisini gösterir.

---

## Proje Akışı ve Asmj.java'nın Kullanımı

1. **Kod Girişi:**
   - Kullanıcıdan assembly kodu alınır (dosya veya metin).
2. **Derleme (assemble):**
   - `assemble()` fonksiyonu çağrılır.
   - Sırasıyla `pass0`, `pass1`, `pass2` çalışır.
3. **Label ve Semboller:**
   - `pass1` ile label ve semboller çözülür.
4. **Makine Kodu Üretimi:**
   - `pass2` ile makine kodu üretilir ve hafızaya yazılır.
5. **Çıktı ve Hata Yönetimi:**
   - `emitListing` ile assembly-makine kodu eşlemesi ve hata raporu üretilir.
   - `emitErrors` ile hata mesajları kullanıcıya sunulur.
6. **Dosya Çıktıları:**
   - `writeBinary` ve `writeSRecords` ile dosya çıktıları alınabilir.

---

## GUI Entegrasyonu İçin Notlar
- `assemble()` fonksiyonu, GUI'den gelen kodu işleyip çıktı ve hata akışlarını GUI'ye yönlendirmek için kullanılabilir.
- `emitListing` fonksiyonu, satır-satır eşleme tablosu için kullanılabilir.
- Hatalar, kullanıcıya dostça mesajlar olarak gösterilebilir.

---

## Genişletme ve Test
- `Asmj.java`'nın fonksiyonları modülerdir, birim testler için uygundur.
- Semboller, makrolar, koşullu derleme ve hata yönetimi ayrı ayrı test edilebilir.

---

## Sonuç
Bu doküman, `Asmj.java` dosyasının ana fonksiyonlarını ve Motorola 6800 assembler projesinde nasıl kullanılacağını özetler. Daha fazla detay için her fonksiyonun JavaDoc açıklamalarına ve ilgili sınıflara bakınız.
