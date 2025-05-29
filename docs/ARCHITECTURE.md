# Proje Mimarisi

Bu dokümant, Motorola 6800 Assembler projesinin genel mimarisini ve tasarım kararlarını açıklar.

## 🏗️ Genel Mimari

Proje, **modüler mimari prensiplerine** göre tasarlanmış ve MVC benzeri bir yapı kullanır:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│       UI        │    │     Parser      │    │   Assembler     │
│   (ConsoleUI)   │◄──►│  (Assembly      │◄──►│ (CodeGenerator) │
│                 │    │   Parsing)      │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Simulator     │    │      Core       │    │      Util       │
│  (CPU6800)      │◄──►│  (Data Models)  │◄──►│ (FileManager)   │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📦 Paket Yapısı

### 1. `assembler` (Root Package)
- **App.java** - Ana entry point ve orchestration

### 2. `assembler.core` (Veri Modelleri)
- **AddressingMode** - 6800'ün 7 addressing mode'u
- **Instruction** - Assembly komutları
- **Label** - Symbolic address'ler  
- **Memory** - 64KB memory modeli
- **Registers** - CPU register seti

### 3. `assembler.parser` (Parsing Katmanı)
- **AssemblyParser** - Ana parser sınıfı
- **AssemblyLine** - Parse edilmiş satır
- **LabelResolver** - İki geçişli label çözümü
- **SyntaxValidator** - Syntax doğrulama
- **TokenParser** - Lexical analysis

### 4. `assembler.assembler` (Code Generation)
- **CodeGenerator** - Machine code üretimi
- **OpcodeTable** - 197 opcode tablosu
- **CodeGenerationException** - Code gen hataları

### 5. `assembler.simulator` (CPU Simülasyonu)
- **ExecutionEngine** - Yüksek seviye execution kontrol
- **CPU6800** - Düşük seviye CPU simülasyonu
- **ExecutionResult/Status/Statistics** - Execution durumu

### 6. `assembler.ai` (AI Destekli Kod Üretimi)
- **AIAssemblyGenerator** - OpenAI API ile assembly kod üretimi

### 7. `assembler.ui` (Kullanıcı Arayüzü)
- **ConsoleUI** - Console-based kullanıcı arayüzü

### 8. `assembler.util` (Yardımcı Sınıflar)
- **FileManager** - Dosya I/O operasyonları

## 🔄 İki Geçişli Assembly Algoritması

Proje, klasik **two-pass assembler** algoritmasını kullanır:

### Birinci Geçiş (First Pass)
1. **Tokenization** - Kaynak kodu token'lara ayır
2. **Syntax Validation** - Syntax hatalarını kontrol et
3. **Address Calculation** - Her satır için adres hesapla
4. **Label Collection** - Tüm label'ları topla

### İkinci Geçiş (Second Pass)  
1. **Label Resolution** - Label referanslarını çöz
2. **Code Generation** - Machine code üret
3. **Error Checking** - Final hata kontrolü

```java
// Assembly işlem akışı
sourceCode → [Parser] → assemblyLines → [LabelResolver] → 
resolvedLines → [CodeGenerator] → machineCode
```

## 🎯 Tasarım Prensipleri

### 1. **Separation of Concerns**
- Her paket tek bir sorumluluğa sahip
- Parser sadece parsing, Generator sadece code gen
- UI logic'i core logic'ten ayrı

### 2. **Immutability**
- Core veri yapıları immutable
- Thread-safety için builder pattern kullanımı

### 3. **Error Handling**
- Checked exception'lar kritik hatalar için
- Meaningful error message'lar

### 4. **Extensibility**
- Yeni instruction'lar OpcodeTable'a eklenebilir
- Farklı UI implementation'ları (GUI) eklenebilir
- Yeni output format'ları desteklenebilir

## 🔧 Temel Sınıf İlişkileri

### Veri Akışı
```
User Input (Assembly) 
    ↓
AssemblyParser.parseSource() 
    ↓
List<AssemblyLine> 
    ↓
LabelResolver.resolveLabelReferences() 
    ↓
CodeGenerator.generateCode() 
    ↓
Map<Integer, List<Integer>> (Machine Code)
    ↓
ExecutionEngine.loadProgram() 
    ↓
CPU6800 Simulation
```

### Ana Sınıf Dependencies
```
App
├── ConsoleUI (composition)
├── AssemblyParser (composition)
├── CodeGenerator (composition)
└── ExecutionEngine (composition)
    └── CPU6800 (composition)
        ├── Memory (composition)
        └── Registers (composition)
```

## ⚡ Performance Considerasyonları

### Memory Management
- **64KB Memory array** - Tam 6800 memory space
- **HashMap** kullanımı label'lar için O(1) lookup
- **ArrayList** kullanımı assembly lines için

### Execution Speed
- **Single-step execution** debugging için
- **Continuous execution** performance için
- **Breakpoint checking** minimal overhead

## 🚀 Gelecek Genişletmeler

### Planlanan Özellikler
1. **GUI Interface** (JavaFX)
2. **Macro Support** 
3. **Additional Output Formats** (Intel HEX, S-Record)
4. **Debugger Enhancements**
5. **Unit Test Coverage**

### Architectural Improvements
- **Plugin Architecture** - Yeni instruction set'ler için
- **Configuration System** - User preferences
- **Logging Framework** - Better debugging

## 📋 Design Patterns Kullanımı

1. **Factory Pattern** - OpcodeTable'da instruction creation
2. **Builder Pattern** - Complex object construction
3. **Strategy Pattern** - Different addressing modes
4. **Observer Pattern** - UI updates (gelecek)
5. **Command Pattern** - Instruction execution

## 🎨 Code Organization

### Package Structure Philosophy
```
core/       - Domain models (no dependencies)
parser/     - Depends on core
assembler/  - Depends on core, parser  
simulator/  - Depends on core, assembler
ui/         - Depends on all (presentation layer)
util/       - Utility functions (minimal dependencies)
```

Bu mimari, **dependency inversion** ve **clean architecture** prensiplerine uyar.

---

Bu mimari, maintainability, testability ve extensibility sağlayacak şekilde tasarlanmıştır. Yeni özellik eklerken bu prensipleri takip etmeniz önerilir.
