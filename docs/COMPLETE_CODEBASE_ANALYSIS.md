# Motorola 6800 Assembler - Tam Kod Analizi

Bu döküman, Motorola 6800 Assembler projesinin tüm Java sınıflarını ve mimari yapısını detaylı olarak analiz eder.

## 📋 Proje Genel Bakış

Bu proje, Motorola 6800 mikroişlemcisi için:
- **Assembly kodunu makine koduna çeviren** iki geçişli assembler
- **Adım adım debugging** desteği olan CPU simülatörü
- **Memory ve register görselleştirme** imkanı
- **Dosya I/O operasyonları** ile program yükleme/kaydetme
- **Konsol tabanlı kullanıcı arayüzü**

sağlayan kapsamlı bir geliştirme ortamıdır.

## 🏗️ Mimari Yapı

```
App.java (Main Entry Point)
     │
     ├── ConsoleUI          (Kullanıcı Etkileşimi)
     ├── AssemblyParser     (Kod Parsing)
     ├── CodeGenerator      (Makine Kodu Üretimi)
     ├── ExecutionEngine    (Simülasyon Kontrolü)
     └── FileManager        (Dosya İşlemleri)
            │
            ├── Core Data Models
            │   ├── Instruction
            │   ├── AddressingMode
            │   ├── Label
            │   ├── Memory
            │   └── Registers
            │
            ├── Parser Components
            │   ├── AssemblyLine
            │   ├── LabelResolver
            │   ├── SyntaxValidator
            │   └── TokenParser
            │
            └── Simulator Components
                ├── CPU6800
                ├── ExecutionResult
                ├── ExecutionStatus
                └── ExecutionStatistics
```

## 📦 Paket ve Sınıf Detayları

### 1. **assembler** (Root Package)

#### App.java - Ana Orkestrasyon Sınıfı
- **Sorumluluk**: Uygulamanın ana entry point'i ve tüm bileşenlerin koordinasyonu
- **Ana İşlevler**:
  - Kullanıcı menü döngüsü yönetimi
  - Assembly işlem pipeline'ı kontrolü
  - Simülasyon oturum yönetimi
  - Hata yönetimi ve kullanıcı geri bildirimi

**Temel Metotlar**:
```java
public static void main(String[] args)     // Program başlangıcı
public void run()                          // Ana döngü
private void assembleProgram()             // İki geçişli assembly süreci
private void simulateProgram()             // Simülasyon oturum yönetimi
private void calculateAddresses()          // Adres hesaplama (first pass)
```

### 2. **assembler.core** (Veri Modelleri)

#### AddressingMode.java - Adreslenme Türleri
- **Sorumluluk**: 6800'ün 7 adreslenme türünü enum olarak tanımlar
- **Türler**:
  - `INHERENT`: Operand yok (NOP, TAB)
  - `IMMEDIATE`: Sabit değer (#$FF)
  - `DIRECT`: Zero page adresi ($80)
  - `EXTENDED`: 16-bit adres ($1000)
  - `INDEXED`: Index register + offset ($10,X)
  - `RELATIVE`: Relatif offset (branch komutları)
  - `PSEUDO`: Assembler direktifleri

#### Instruction.java - Komut Modeli
- **Sorumluluk**: Assembly komutlarını temsil eder
- **İçerik**: Mnemonic, addressing mode, operand, opcode, cycle sayısı
- **Kullanım**: Parsing ve code generation aşamalarında

#### Label.java - Sembolik Adresler
- **Sorumluluk**: Label tanımları ve referansları
- **Özellikler**: İsim, değer, tanımlanma durumu

#### Memory.java - Bellek Modeli
- **Sorumluluk**: 64KB bellek simülasyonu
- **Özellikler**: Byte-level okuma/yazma, adres doğrulama

#### Registers.java - CPU Register Seti
- **Sorumluluk**: 6800 CPU registerlerini model eder
- **Registerler**: A, B, X (Index), SP (Stack Pointer), PC (Program Counter), CCR (Condition Code)

### 3. **assembler.parser** (Parsing Katmanı)

#### AssemblyParser.java - Ana Parser Sınıfı
- **Sorumluluk**: Assembly kod çözümleme ve doğrulama
- **İki Geçişli Algoritma**:
  1. **First Pass**: Tokenization, syntax validation, address calculation
  2. **Second Pass**: Label resolution, final validation

**Ana İşlem Akışı**:
```java
parseSource() → parseLine() → determineAddressingMode() → validateInstruction()
```

#### AssemblyLine.java - Parse Edilmiş Satır
- **Sorumluluk**: Tek bir assembly satırının tüm bilgilerini tutar
- **İçerik**: Kaynak kodu, label, instruction, pseudo-op, adres, makine kodu

#### LabelResolver.java - Label Çözümleyici
- **Sorumluluk**: İki geçişli label çözümü
- **İşlevler**: Label tanımları toplama, referansları çözümleme
- **Inner Class**: `LabelReference` - unresolved label referansları

#### SyntaxValidator.java - Sözdizimi Doğrulayıcı
- **Sorumluluk**: Assembly kod sözdizimi kontrolü
- **Doğrulamalar**: Label isimleri, komut geçerliliği, operand formatları

#### TokenParser.java - Lexical Analyzer
- **Sorumluluk**: Kaynak kodu token'lara ayırma
- **Token Türleri**: Label'lar, komutlar, operandlar, sayısal değerler

### 4. **assembler.assembler** (Code Generation)

#### CodeGenerator.java - Makine Kodu Üretici
- **Sorumluluk**: Parse edilmiş assembly'den makine kodu üretir
- **Desteklenen Çıktılar**: Raw binary, Intel HEX format
- **İşlem**: Opcode lookup, operand encoding, address resolution

**Ana İşlem Akışı**:
```java
generateCode() → generateLineCode() → [generateInstructionCode() | generatePseudoOpCode()]
```

#### OpcodeTable.java - Opcode Tablosu
- **Sorumluluk**: 6800'ün 197 opcode'unu yönetir
- **Organize Etme**: Mnemonic + AddressingMode kombinasyonları
- **Bilgiler**: Opcode, cycle sayısı, açıklama

**Örnek Girişler**:
```java
addInstruction("LDA", IMMEDIATE, 0x86, 2, "Load accumulator A immediate");
addInstruction("BEQ", RELATIVE, 0x27, 4, "Branch if equal (Z=1)");
```

#### CodeGenerationException.java - Code Gen Hataları
- **Sorumluluk**: Code generation sürecindeki hataları yönetir

### 5. **assembler.simulator** (CPU Simülasyonu)

#### ExecutionEngine.java - Simülasyon Kontrolcüsü
- **Sorumluluk**: Yüksek seviye simülasyon yönetimi
- **Özellikler**: Program yükleme, adım adım çalıştırma, breakpoint yönetimi
- **İstatistikler**: Çalıştırılan komut sayısı, süre ölçümü

#### CPU6800.java - Düşük Seviye CPU Simülatörü
- **Sorumluluk**: 6800 CPU'nun cycle-accurate simülasyonu
- **İşlevler**: Instruction fetch, decode, execute cycle'ı
- **Durum**: Register değerleri, flag'ler, stack durumu

#### ExecutionResult.java - Çalıştırma Sonucu
- **Sorumluluk**: Tek bir instruction execution sonucunu tutar
- **Bilgiler**: Status, PC değeri, çalıştırılan satır, mesajlar

#### ExecutionStatus.java - Durum Enum'u
- **Değerler**: `RUNNING`, `HALTED`, `BREAKPOINT`, `ERROR`

#### ExecutionStatistics.java - İstatistik Toplayıcı
- **Sorumluluk**: Simülasyon performance metriklerini toplar
- **Metrikler**: Toplam instruction sayısı, çalışma süresi, ortalama hız

### 6. **assembler.ai** (AI Destekli Kod Üretimi)

#### AIAssemblyGenerator.java
- **Purpose**: OpenAI API kullanarak assembly kod üretimi
- **Key Features**:
  - Natural language to assembly translation
  - GPT-4o model integration
  - HTTP client management
  - JSON request/response handling
  - Error handling ve timeout management
- **Dependencies**: OkHttp3, Jackson, OpenAI API
- **Integration**: UI layer ile entegrasyon

### 7. **assembler.ui** (Kullanıcı Arayüzü)

#### ConsoleUI.java - Konsol Arayüzü
- **Sorumluluk**: Kullanıcı ile tüm etkileşimleri yönetir
- **Özellikler**: 
  - Menü sistemleri (ana menü, simülatör menü, breakpoint yönetimi)
  - Input/output formatting
  - Machine code listing görüntüleme
  - Register ve memory dump'ları

**Ana Interface Metotları**:
```java
public int showMainMenu()                           // Ana menü
public String getMultiLineInput()                   // Assembly kod girişi
public void showMachineCode()                       // Makine kod listesi
public void showRegisters(Registers registers)      // Register durumu
public void showMemory(Memory memory, int start, int length)  // Memory dump
```

### 8. **assembler.util** (Yardımcı Sınıflar)

#### FileManager.java - Dosya Operasyonları
- **Sorumluluk**: Tüm dosya I/O işlemlerini yönetir
- **Desteklenen Formatlar**:
  - Assembly source files (.asm)
  - Binary machine code (.bin)
  - Intel HEX format (.hex)
  - Listing files (.lst)

**Ana Metotlar**:
```java
public static String loadSourceFile(String path)                    // Kaynak dosya yükleme
public static void saveSourceFile(String path, String content)      // Kaynak dosya kaydetme
public static void saveBinaryFile(String path, Map<Integer, List<Integer>> code)  // Binary kaydetme
public static void saveHexFile(String path, Map<Integer, List<Integer>> code)     // Intel HEX kaydetme
public static void createExampleProgram(String filename)            // Örnek program oluşturma
```

## 🔄 Program Akış Diyagramı

```
[Program Başlangıcı]
         │
         ▼
[Ana Menü Döngüsü] ◄──────┐
         │                │
         ▼                │
[Kullanıcı Seçimi]        │
         │                │
         ├─1─► [Assembly Kod Girişi] ─────┐
         ├─2─► [Dosyadan Yükleme] ───────┤
         ├─3─► [Assembly İşlemi] ◄───────┘
         │         │
         │         ├─► [Parse Source Code]
         │         ├─► [Calculate Addresses]
         │         ├─► [Resolve Labels]
         │         └─► [Generate Machine Code]
         │
         ├─4─► [Makine Kod Görüntüleme]
         ├─5─► [Simülasyon Modu]
         │         │
         │         ├─► [Program Yükleme]
         │         ├─► [Step Execution]
         │         ├─► [Run to Breakpoint]
         │         ├─► [Register/Memory View]
         │         └─► [Breakpoint Management]
         │
         ├─6─► [Program Kaydetme]
         ├─7─► [Instruction Set Görüntüleme]
         ├─8─► [Örnek Program Oluşturma]
         └─0─► [Çıkış]
                │
                ▼
         [Program Sonu]
```

## 📊 Kod Metrikleri

- **Toplam Sınıf Sayısı**: 22 sınıf
- **Paket Sayısı**: 6 özelleşmiş paket
- **Desteklenen Instruction**: 72 temel komut, 197 opcode
- **Addressing Mode**: 7 farklı adreslenme türü
- **Test Coverage**: Core functionality için unit testler
- **LOC (Lines of Code)**: Yaklaşık 3000+ satır

## 🎯 Teknik Özellikler

### Assembly İşlemci Özellikleri
- **İki geçişli algoritma** ile optimize edilmiş label çözümü
- **Pseudo-instruction desteği**: ORG, END, EQU, FCB, FDB
- **Error handling**: Detaylı hata mesajları ve satır numarası bilgisi
- **Multiple output format**: Binary, Intel HEX, listing file

### Simülatör Özellikleri
- **Cycle-accurate execution**: Gerçek 6800 timing'i
- **Breakpoint sistemi**: Adres bazlı debugging
- **Step-by-step execution**: Komut seviyesinde debugging
- **Memory/Register inspection**: Real-time durum görüntüleme

### Dosya Sistemi Desteği
- **Auto-detect encoding**: UTF-8 ve ASCII dosya desteği
- **Cross-platform paths**: Windows/Linux/Mac uyumluluğu
- **Backup creation**: Otomatik yedekleme mekanizması

Bu analiz, projeye yeni katılan geliştiricilerin kodbase'i hızlıca anlaması ve katkıda bulunmaya başlaması için kapsamlı bir rehber sağlamaktadır.
