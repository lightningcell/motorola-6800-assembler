# 🖥️ Motorola 6800 Assembler

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.0+-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![AI Powered](https://img.shields.io/badge/AI-Powered-purple.svg)](docs/ai/README.md)

Modern bir Java tabanlı **Motorola 6800 assembler** ve simülatör uygulaması. Assembly kodunu makine koduna dönüştürür, etkileşimli hata ayıklama sağlar ve **yapay zeka destekli kod üretimi** ile geliştirilmiştir.

## ✨ Özellikler

### 🔧 Temel Assembler Özellikleri
- **Tam 6800 instruction set desteği** (72 instruction, 197 opcode)
- **İki geçişli assembler algoritması** ile label resolution
- **Pseudo-instruction desteği** (ORG, END, FCB, FDB, EQU)
- **Çoklu çıktı formatları** (binary, Intel HEX, listing)
- **Sözdizimi doğrulama** ve kullanıcı dostu hata mesajları

### 🤖 AI-Powered Kod Üretimi
- **OpenAI GPT-4o entegrasyonu** ile assembly kod üretimi
- **Doğal dil girişi** ile program açıklaması
- **Motorola 6800 optimized prompts** 
- **Anında kod gözden geçirme** ve onay sistemi
- **Seamless entegrasyon** ana assembler ile

### 🎮 CPU Simülatörü
- **Gerçek zamanlı 6800 CPU simülasyonu**
- **Adım adım hata ayıklama** (step-by-step debugging)
- **Breakpoint yönetimi** ve memory inspection
- **Register ve memory görselleştirme**
- **Program execution istatistikleri**

### 💻 Kullanıcı Arayüzü
- **İnteraktif konsol arayüzü**
- **Kod editörü** ve dosya I/O operations
- **Gerçek zamanlı assembler feedback**
- **Detaylı execution logs** ve error reporting

## 🚀 Hızlı Başlangıç

### Önkoşullar
- **Java 17+** (OpenJDK veya Oracle JDK)
- **Gradle 8.0+** (Wrapper dahil)
- **OpenAI API Key** (AI özellikleri için, opsiyonel)

### Kurulum ve Çalıştırma

#### 1. Repository'yi klonlayın
```powershell
git clone https://github.com/lightningcell/motorola-6800-assembler.git
cd motorola-6800-assembler
```

#### 2. Projeyi derleyin
```powershell
.\gradlew build
```

#### 3. Uygulamayı çalıştırın
```powershell
.\gradlew run
```

#### 4. AI Demo'yu deneyin (opsiyonel)
```powershell
.\AI_DEMO.ps1
```

## 📋 Kullanım Örnekleri

### Basit Assembly Program
```assembly
        ORG $1000           ; Program start address
        LDA #$05            ; Load immediate value 5
        ADDA #$03           ; Add immediate value 3  
        STA $2000           ; Store result to memory
        END                 ; End of program
```

### AI ile Kod Üretimi
1. Ana menüden **9 - AI Assembly Generator** seçin
2. API anahtarınızı ayarlayın (option 1)
3. Doğal dilde program açıklaması yapın:
   ```
   "İki sayıyı toplayan ve sonucu hafızaya yazan program"
   ```
4. AI tarafından üretilen kodu gözden geçirin ve onaylayın
5. Normal assembler işlemlerini kullanın (derleme, simülasyon)

### Program Simülasyonu
```powershell
# Ana menüden assembly kodunu girin (option 1)
# Programı derleyin (option 3)  
# Simülatörü başlatın (option 5)
# Step-by-step execution veya tam çalıştırma
# Register ve memory durumunu inceleyin
```

## 🏗️ Proje Mimarisi

```
📦 motorola-6800-assembler/
├── 📁 app/                    # Ana uygulama kodu
│   ├── 📁 src/main/java/assembler/
│   │   ├── 🤖 ai/             # AI kod üretimi modülü
│   │   ├── ⚙️ assembler/       # Makine kodu üretimi
│   │   ├── 🧠 core/           # Temel veri yapıları
│   │   ├── 📝 parser/         # Assembly parser
│   │   ├── 🎮 simulator/      # CPU simülatörü
│   │   ├── 🖥️ ui/             # Kullanıcı arayüzü
│   │   └── 🛠️ util/           # Yardımcı sınıflar
│   └── 📄 build.gradle        # Proje bağımlılıkları
├── 📚 docs/                   # Kapsamlı dokümantasyon
│   ├── 🤖 ai/                 # AI modülü dokümantasyonu
│   ├── 📖 ARCHITECTURE.md     # Mimari açıklaması
│   ├── 🚀 GETTING_STARTED.md  # Geliştirici kılavuzu
│   └── 📋 CLASS_LIST.md       # Sınıf referansları
├── 🎬 AI_DEMO.ps1             # AI demo scripti
├── 📄 AI_INTEGRATION_SUMMARY.md
└── ⚖️ LICENSE                 # MIT License
```

## 🎯 Desteklenen Motorola 6800 Instructions

### Data Movement
- **LDA, LDB, LDX** - Load accumulator/index register
- **STA, STB, STX** - Store accumulator/index register
- **TAB, TBA, TSX, TXS** - Transfer between registers

### Arithmetic Operations  
- **ADDA, ADDB, ADCA, ADCB** - Add with/without carry
- **SUBA, SUBB, SBCA, SBCB** - Subtract with/without carry
- **INCA, INCB, DECA, DECB** - Increment/decrement

### Logic Operations
- **ANDA, ANDB, ORA, ORB** - Bitwise AND/OR
- **EORA, EORB, COMA, COMB** - Exclusive OR/complement
- **ASLA, ASLB, ASRA, ASRB** - Arithmetic shift

### Control Flow
- **JMP, JSR, RTS, RTI** - Jump and subroutine calls
- **BRA, BEQ, BNE, BCC, BCS** - Conditional branches
- **BPL, BMI, BVC, BVS** - Flag-based branches

### System Operations
- **NOP, SWI, WAI** - No operation, software interrupt, wait
- **SEC, CLC, SEI, CLI** - Set/clear flags

## 🤖 AI Özellikleri

### Desteklenen Prompts
```
✅ "İki sayıyı toplayan program yaz"
✅ "1'den 10'a kadar sayan döngü oluştur"  
✅ "Hafızadan değer oku, arttır ve geri yaz"
✅ "Fibonacci dizisinin ilk 5 elemanını hesapla"
✅ "Basit hesap makinesi yap"
```

### AI Konfigürasyonu
- **Model**: GPT-4o (latest)
- **Max Tokens**: 1000
- **Temperature**: 0.3 (tutarlı çıktı için)
- **Timeout**: 30 saniye

## 📚 Dokümantasyon

### Geliştirici Dokümantasyonu
- **[Architecture Guide](docs/ARCHITECTURE.md)** - Mimari açıklaması
- **[Developer Guide](docs/DEVELOPER_GUIDE.md)** - Geliştirici kılavuzu  
- **[API Reference](docs/CLASS_LIST.md)** - Sınıf referansları

### AI Modülü
- **[AI Documentation](docs/ai/README.md)** - Teknik detaylar
- **[AI Testing Guide](docs/ai/AI_TESTING_GUIDE.md)** - Test prosedürleri
- **[Integration Summary](AI_INTEGRATION_SUMMARY.md)** - Entegrasyon özeti

### Kullanıcı Kılavuzları
- **[Getting Started](docs/GETTING_STARTED.md)** - Hızlı başlangıç
- **[Instruction Set](docs/INSTRUCTION_SET.md)** - 6800 komut referansı
- **[Program Examples](example/)** - Örnek programlar

### Yaygın Sorunlar

**Build Hatası**
```powershell
.\gradlew clean build         # Clean build deneyin
```

**AI API Hatası**  
```
❌ 'API key not configured'
✅ OpenAI API anahtarını ayarlayın (option 1)
```

**Assembly Hatası**
```
❌ 'Unknown instruction: XYZ'  
✅ Instruction set dokümantasyonunu kontrol edin
```

**Simülasyon Hatası**
```
❌ 'Program counter out of bounds'
✅ ORG direktifi ve program boyutunu kontrol edin
```

### Log ve Debug
```powershell
.\gradlew run --debug         # Debug modu
.\gradlew run --info          # Verbose logging
```

## 📄 Lisans

Bu proje **MIT License** altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.


- **Issues**: [GitHub Issues](https://github.com/lightningcell/motorola-6800-assembler/issues)

---

## 🚀 Hemen Başlayın!

```powershell
git clone https://github.com/lightningcell/motorola-6800-assembler.git
cd motorola-6800-assembler
.\gradlew run
```

**🤖 AI özellikleri için OpenAI API anahtarınızı hazır bulundurun!**

---

*Motorola 6800 Assembler - Modern assembly programming with AI assistance* 🖥️✨
