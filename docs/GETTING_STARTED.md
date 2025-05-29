# Başlangıç Kılavuzu

Bu kılavuz, projeye yeni katılan geliştiriciler için hazırlanmıştır.

## 🚀 Hızlı Başlangıç

### 1. Proje Kurulumu

```bash
# Projeyi klonlayın (veya dosyaları indirin)
cd d:\motorola-6800-assembler

# Gradle ile build edin
.\gradlew build

# Uygulamayı çalıştırın
.\gradlew run
```

### 2. İlk Çalıştırma

Uygulama console-based bir interface sunar:

```
=====================================================
    Motorola 6800 Assembler & Simulator v1.0
=====================================================
Welcome to the Motorola 6800 development environment!

Main Menu:
1. Input Assembly Code
2. Load Program from File  
3. Assemble Program
4. View Machine Code
5. Simulate Program
6. Save Program
7. Show Instruction Set
8. Create Example Program
0. Exit

Enter your choice: 
```

### 3. İlk Assembly Programı

Menüden **"1. Input Assembly Code"** seçin ve şu örnek kodu girin:

```assembly
        ORG $0200        ; Program start address
START   LDA #$FF         ; Load 255 into accumulator A
        STA $0300        ; Store to memory location $0300
        LDB #$01         ; Load 1 into accumulator B
        ABA              ; Add B to A
        SWI              ; Software interrupt (halt)
        END              ; End of program
```

## 📚 Proje Yapısını Anlama

### Ana Dosyalar

```
app/src/main/java/assembler/
├── App.java                    # 🎯 ANA ENTRY POINT
├── core/                       # 📦 Temel veri yapıları
│   ├── AddressingMode.java     # 7 addressing mode enum
│   ├── Instruction.java        # Assembly instruction modeli
│   ├── Label.java             # Label (symbolic address) modeli
│   ├── Memory.java            # 64KB memory simülasyonu
│   └── Registers.java         # CPU register seti
├── parser/                     # 🔍 Assembly parsing
│   ├── AssemblyParser.java    # Ana parser sınıfı
│   ├── AssemblyLine.java      # Parse edilmiş satır
│   ├── LabelResolver.java     # Label referans çözümü
│   ├── SyntaxValidator.java   # Syntax kontrolü
│   └── TokenParser.java       # Token ayırma
├── assembler/                  # ⚙️ Machine code üretimi
│   ├── CodeGenerator.java     # Machine code generator
│   ├── OpcodeTable.java       # 197 opcode tablosu
│   └── CodeGenerationException.java
├── simulator/                  # 🖥️ CPU simülasyonu
│   ├── ExecutionEngine.java   # Yüksek seviye execution
│   ├── CPU6800.java          # Düşük seviye CPU sim
│   ├── ExecutionResult.java   # Execution sonuçları
│   ├── ExecutionStatistics.java
│   └── ExecutionStatus.java
├── ui/                        # 🖼️ Kullanıcı arayüzü
│   └── ConsoleUI.java         # Console interface
└── util/                      # 🛠️ Yardımcı sınıflar
    └── FileManager.java       # Dosya I/O işlemleri
```

## 🎯 İlk Değişikliğinizi Yapma

### Senaryo: Yeni bir instruction ekleme

1. **OpcodeTable.java** dosyasını açın
2. `initializeInstructions()` metodunda yeni instruction ekleyin:

```java
// Örnek: ROL A (Rotate Left Accumulator A) 
addInstruction("ROL", AddressingMode.INHERENT, 0x49, 2, "Rotate Left Accumulator A");
```

3. **SyntaxValidator.java** dosyasında mnemonic'i reserved words'e ekleyin

4. Test edin:
   ```assembly
   ROL A    ; Yeni instruction'ınızı test edin
   ```

### Senaryo: Yeni UI feature ekleme

1. **ConsoleUI.java** dosyasını açın
2. Yeni menu item ekleyin
3. **App.java** main loop'una yeni case ekleyin

## 🧪 Test Etme

### Assembly Code Test Etme

```assembly
; Test programı örneği
        ORG $0100
        LDA #$55         ; Test value
        LDB #$AA         ; Test value  
        ABA              ; A = A + B = $FF
        STA $0200        ; Store result
        SWI              ; Halt
        END
```

### Expected Output:
- **Machine Code:** `86 55 C6 AA 1B 97 00 3F`
- **Final A Register:** $FF
- **Memory[0x0200]:** $FF

## 🐛 Debug Teknikleri

### 1. Parser Debug
```java
// AssemblyParser içinde debug print'ler ekleyin
System.out.println("Parsing line: " + sourceLine);
System.out.println("Tokens: " + tokens);
```

### 2. Code Generation Debug
```java
// CodeGenerator içinde opcode'ları kontrol edin
System.out.printf("Generated: %02X for %s%n", opcode, instruction.getMnemonic());
```

### 3. Simulation Debug
- Step-by-step execution kullanın
- Register değerlerini kontrol edin
- Memory dump'ları alın

## 📋 Geliştirme Workflow'u

### 1. **Yeni Feature Eklemek**
```
1. İlgili paket dokümantasyonunu oku
2. Interface'leri ve contracts'ları anla
3. Unit test yaz (gelecek için)
4. Implementation'ı gerçekleştir
5. Integration test yap
6. Dokümantasyonu güncelle
```

### 2. **Bug Fix Workflow**
```
1. Bug'ı reproduce et
2. Debug information topla
3. Root cause analysis yap  
4. Fix'i implement et
5. Regression test yap
```

## 🔧 Geliştirme Araçları

### IDE Konfigürasyonu
- **Java 11+** gerekli
- **Gradle** build tool
- **IDE:** IntelliJ IDEA / Eclipse / VSCode

### Useful Shortcuts
```java
// Memory address format helper
String.format("$%04X", address)  // $1234

// Hex byte format  
String.format("%02X", byteValue) // FF

// Binary format
Integer.toBinaryString(value)    // 11111111
```

## 📖 Önemli Kavramlar

### 1. **Two-Pass Assembly**
- **Pass 1:** Label'ları topla, address'leri hesapla
- **Pass 2:** Label'ları resolve et, machine code üret

### 2. **Addressing Modes (6800)**
- **Inherent:** `NOP` (operand yok)
- **Immediate:** `LDA #$FF` (sabit değer)
- **Direct:** `LDA $80` (zero page, 0-255)
- **Extended:** `LDA $1234` (16-bit address)
- **Indexed:** `LDA $10,X` (X register + offset)
- **Relative:** `BRA LOOP` (branch instructions)

### 3. **CPU Registers (6800)**
- **A, B:** 8-bit accumulators
- **X:** 16-bit index register
- **SP:** 16-bit stack pointer  
- **PC:** 16-bit program counter
- **CC:** 8-bit condition codes (C,V,Z,N,I,H)

## 🚨 Yaygın Hatalar ve Çözümleri

### 1. **Label Resolution Hatası**
```
Error: Unresolved label reference 'LOOP'
```
**Çözüm:** Label'ın tanımlandığından emin olun: `LOOP:`

### 2. **Address Range Hatası**  
```
Error: Address out of range for direct addressing
```
**Çözüm:** Direct addressing için 0-255 aralığında olmalı

### 3. **Invalid Operand**
```
Error: Invalid operand format
```
**Çözüm:** Hex: `$FF`, Decimal: `255`, Binary: `%11111111`

## 📞 Yardım Alma

1. **Paket dokümantasyonlarını** inceleyin: `docs/*/README.md`
2. **Kod örneklerini** çalıştırın
3. **Console debug** çıktılarını kullanın
4. **Step-by-step simulation** ile test edin

---

Bu kılavuz ile projeye başlayabilir ve ilk değişikliklerinizi yapabilirsiniz. Detaylı bilgi için diğer dokümantasyon dosyalarını inceleyin.
