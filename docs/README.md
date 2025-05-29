# Motorola 6800 Assembler Documentation

This documentation provides a comprehensive guide for the Java-based Motorola 6800 assembler that translates assembly code to machine code and includes CPU simulation capabilities.

## 📁 Documentation Contents

### Overview Documents
- **[Architecture Overview](ARCHITECTURE.md)** - Project architecture and design decisions
- **[Getting Started Guide](GETTING_STARTED.md)** - Setup guide for new developers
- **[Program Flow](PROGRAM_FLOW.md)** - How the application works end-to-end

### Package Documentation
- **[Core Package](core/README.md)** - Core data structures and models
- **[Parser Package](parser/README.md)** - Assembly code parsing operations
- **[Assembler Package](assembler/README.md)** - Machine code generation
- **[Simulator Package](simulator/README.md)** - CPU simulation engine
- **[UI Package](ui/README.md)** - User interface components
- **[Util Package](util/README.md)** - Utility classes and file operations

### Reference Guides
- **[Class Reference](CLASS_LIST.md)** - Detailed descriptions of all classes
- **[Exception Handling](EXCEPTIONS.md)** - Error handling strategies and exception types
- **[Motorola 6800 Instruction Set](INSTRUCTION_SET.md)** - Complete instruction reference

## 🚀 Quick Start

1. **Understand the project:** Read [ARCHITECTURE.md](ARCHITECTURE.md) first
2. **Setup development:** Follow [GETTING_STARTED.md](GETTING_STARTED.md) guide  
3. **Study the code:** Learn the main flow with [PROGRAM_FLOW.md](PROGRAM_FLOW.md)
4. **Make first changes:** Start with each package's README file

## 🎯 Project Features

- **Complete 6800 instruction set** with all addressing modes
- **Two-pass assembler** algorithm for label resolution
- **Label resolution** and pseudo-instruction support
- **Step-by-step debugging** with breakpoint management
- **Memory and register visualization**
- **File I/O operations** for assembly programs
- **Multiple output formats** (binary, Intel HEX, listing files)

## 📊 Project Metrics

- **Total Classes:** 22 classes across 6 packages
- **Package Count:** 6 specialized packages  
- **Supported Format:** Motorola 6800 Assembly Language
- **UI Type:** Console-based (GUI planned for future)
- **Test Coverage:** Unit tests for core functionality

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   User Input    │───▶│     Parser       │───▶│   Assembler     │
│  (Assembly)     │    │   (2-pass)       │    │ (Code Gen)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Simulator     │◀───│   Core Models    │◀───│  Machine Code   │
│  (6800 CPU)     │    │ (Memory/Regs)    │    │   (Output)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🔍 Detaylı İnceleme

Her bir paket ve sınıfın detaylı açıklaması için yukarıdaki linklerden ilgili dokümantasyona geçin. Dokümantasyon şu konuları kapsar:

- **Sınıf amacı ve sorumluluğu**
- **Public API kullanımı**
- **Diğer sınıflarla ilişkiler**
- **Örnek kod kullanımları**
- **Geliştirme notları**

## 💡 Katkıda Bulunma

Projeye katkıda bulunmak için:
1. İlgili paket dokümantasyonunu inceleyin
2. [GETTING_STARTED.md](GETTING_STARTED.md) geliştirme ortamı kurulumunu yapın
3. Değişiklik yapmadan önce mimariyi anlayın
4. Test yazın ve dokümantasyonu güncelleyin

---
*Bu dokümantasyon, Motorola 6800 Assembler projesini anlama ve geliştirmeye başlamanız için hazırlanmıştır.*
