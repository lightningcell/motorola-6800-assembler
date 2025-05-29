# Parser Paketi

`assembler.parser` paketi, Motorola 6800 assembly kaynak kodunu parse etme ve lexical analysis işlemlerinden sorumludur. İki geçişli assembly algoritmasının ilk fazını gerçekleştirir.

## 📦 Paket İçeriği

| Sınıf | Açıklama | Sorumluluk |
|-------|----------|------------|
| **AssemblyParser** | Ana parser sınıfı | Kaynak kodu parse eder ve AssemblyLine'lara çevirir |
| **AssemblyLine** | Parse edilmiş satır modeli | Bir assembly satırının tüm bileşenlerini saklar |
| **LabelResolver** | Label çözümleme | İki geçişli label resolution algoritması |
| **SyntaxValidator** | Syntax doğrulama | Assembly syntax kurallarını kontrol eder |
| **TokenParser** | Lexical analyzer | Kaynak kodu token'lara ayırır |
| **ParseException** | Parse hataları | Parse işlemi sırasında oluşan hatalar |

## 🔍 Parsing Süreci

### 1. Genel Parse Akışı

```
Source Code (String)
    │
    ▼
TokenParser.tokenize() → List<String> tokens
    │
    ▼  
SyntaxValidator.validate() → Syntax Check
    │
    ▼
AssemblyParser.parseLine() → AssemblyLine objects
    │
    ▼
LabelResolver.resolve() → Resolved references
    │
    ▼
List<AssemblyLine> (Ready for code generation)
```

### 2. İki Geçişli Algorithm

**First Pass (Address Calculation):**
```java
// 1. Her satırı parse et
// 2. Label'ları topla  
// 3. Address'leri hesapla
// 4. Symbol table oluştur
```

**Second Pass (Label Resolution):**
```java
// 1. Label referanslarını resolve et
// 2. Forward reference'ları çöz
// 3. Error checking yap
```

## 📋 Sınıf Detayları

### 1. AssemblyParser (Ana Parser)

Ana parsing logic'ini içerir:

```java
public class AssemblyParser {
    private final SyntaxValidator validator;
    private final LabelResolver labelResolver;
    private final TokenParser tokenParser;
    
    // Ana parse metodu
    public List<AssemblyLine> parseSource(String sourceCode) throws ParseException;
    
    // Label resolution
    public void resolveLabelReferences(List<AssemblyLine> assemblyLines) throws ParseException;
}
```

**Key Methods:**
- `parseSource()` - Kaynak kodu parse eder
- `parseLine()` - Tek bir satırı parse eder
- `resolveLabelReferences()` - Label'ları resolve eder
- `parseOperand()` - Operand'ları parse eder

**Parse Line Örneği:**
```java
// Input: "START   LDA #$FF    ; Load 255"
AssemblyLine line = parser.parseLine("START   LDA #$FF    ; Load 255", 1);

// Result:
// line.getLabel() → Label("START")
// line.getInstruction() → Instruction("LDA", IMMEDIATE, "#$FF")  
// line.getComment() → "; Load 255"
```

### 2. AssemblyLine (Parse Edilmiş Satır)

Bir assembly satırının tüm bileşenlerini saklar:

```java
public class AssemblyLine {
    private final int lineNumber;           // Kaynak satır numarası
    private final String sourceLine;        // Orijinal kaynak satır
    private Label label;                    // Label (opsiyonel)
    private Instruction instruction;        // Assembly instruction (opsiyonel)
    private String comment;                 // Comment (opsiyonel)
    private int address;                    // Memory address
    private byte[] machineCode;             // Generated machine code
    private String pseudoInstruction;       // ORG, END, etc.
    private Object pseudoOperand;           // Pseudo operand
    private boolean isComment;              // Sadece comment mi?
    private boolean isEmpty;                // Boş satır mı?
}
```

**AssemblyLine Türleri:**
```java
// 1. Label Definition
"START:"                    → hasLabel=true, instruction=null

// 2. Instruction with Label  
"LOOP    LDA $0300"        → hasLabel=true, hasInstruction=true

// 3. Instruction Only
"        STA $0400"        → hasLabel=false, hasInstruction=true

// 4. Pseudo Instruction
"        ORG $0200"        → isPseudoOp=true

// 5. Comment Only
";  This is a comment"     → isComment=true

// 6. Empty Line
""                         → isEmpty=true
```

### 3. LabelResolver (Label Çözümleyici)

İki geçişli label resolution algoritmasını implement eder:

```java
public class LabelResolver {
    private final Map<String, Label> labels;                    // Symbol table
    private final List<LabelReference> unresolvedReferences;    // Forward references
    
    // Label tanımlama
    public void addLabel(String labelName, int address) throws ParseException;
    
    // Label referansı ekleme
    public void addLabelReference(String labelName, AssemblyLine line, int operandIndex);
    
    // Tüm referansları resolve etme
    public void resolveAllReferences() throws ParseException;
}
```

**Label Resolution Süreci:**
```java
// First Pass: Label definitions
labelResolver.addLabel("START", 0x0200);
labelResolver.addLabel("LOOP", 0x0210);
labelResolver.addLabel("END", 0x0220);

// Second Pass: Reference resolution  
"JMP START" → resolvedOperand = 0x0200
"BEQ LOOP"  → resolvedOperand = 0x0210
"JMP END"   → resolvedOperand = 0x0220
```

**Forward Reference Handling:**
```assembly
        JMP LATER    ; Forward reference - henüz bilinmiyor
        NOP
LATER   SWI          ; Label tanımı - şimdi resolve edilebilir
```

### 4. SyntaxValidator (Syntax Kontrolcü)

Assembly syntax kurallarını kontrol eder:

```java
public class SyntaxValidator {
    private final OpcodeTable opcodeTable;
    private final Set<String> reservedWords;
    
    // Label name validation
    public void validateLabelName(String labelName);
    
    // Instruction validation
    public void validateInstruction(String mnemonic, AddressingMode mode);
    
    // Operand validation  
    public void validateOperand(String operand, AddressingMode mode);
}
```

**Validation Rules:**
```java
// Label validation
- Must start with letter or underscore
- Can contain letters, digits, underscore
- Cannot be reserved word (LDA, STA, etc.)
- Maximum 32 characters

// Instruction validation
- Must be valid 6800 mnemonic
- Addressing mode must be supported for instruction
- Operand format must match addressing mode

// Operand validation
- Hex: $00-$FFFF
- Decimal: 0-65535  
- Binary: %00000000-%11111111
- Character: 'A', 'Z'
```

### 5. TokenParser (Lexical Analyzer)

Kaynak kodu token'lara ayırır:

```java
public class TokenParser {
    // Ana tokenization metodu
    public List<String> tokenize(String sourceLine);
    
    // Token türü belirleme
    public TokenType getTokenType(String token);
    
    // Numeric value parsing
    public int parseNumericValue(String token);
}
```

**Token Types:**
```java
public enum TokenType {
    LABEL,          // "START:"
    INSTRUCTION,    // "LDA", "STA"  
    OPERAND,        // "$FF", "LOOP"
    COMMENT,        // "; comment"
    IMMEDIATE,      // "#"
    COMMA,          // ","
    HEX_NUMBER,     // "$FF"
    DECIMAL_NUMBER, // "255"
    BINARY_NUMBER,  // "%11111111"
    STRING_LITERAL, // "Hello"
    UNKNOWN
}
```

**Tokenization Örneği:**
```java
// Input: "START   LDA #$FF,X   ; Load indexed"
List<String> tokens = tokenParser.tokenize(input);

// Result:
// ["START", "LDA", "#", "$FF", ",", "X", ";", "Load", "indexed"]
```

**Token Patterns:**
```java
private static final Pattern[] TOKEN_PATTERNS = {
    Pattern.compile("\\s+"),                    // Whitespace
    Pattern.compile(";.*"),                     // Comment
    Pattern.compile("[A-Za-z_][A-Za-z0-9_]*:"), // Label
    Pattern.compile("\\$[0-9A-Fa-f]+"),        // Hex number
    Pattern.compile("%[01]+"),                  // Binary number
    Pattern.compile("[0-9]+"),                  // Decimal number
    Pattern.compile("#"),                       // Immediate prefix
    Pattern.compile(","),                       // Comma
    Pattern.compile("[A-Za-z_][A-Za-z0-9_]*"), // Identifier
    // ... other patterns
};
```

## 🎯 Parsing Örnekleri

### Complete Example

```assembly
; Sample 6800 program
        ORG $0200        ; Set origin
START   LDA #$FF         ; Load immediate
        STA $0300        ; Store to memory  
LOOP    DEC $0300        ; Decrement memory
        BNE LOOP         ; Branch if not zero
        SWI              ; Software interrupt
        END              ; End of program
```

**Parse Result:**
```java
AssemblyLine[0]: ORG pseudo-op, operand=$0200, address=$0200
AssemblyLine[1]: Label="START", LDA #$FF, address=$0200
AssemblyLine[2]: STA $0300, address=$0202  
AssemblyLine[3]: Label="LOOP", DEC $0300, address=$0205
AssemblyLine[4]: BNE LOOP (resolved to $0205), address=$0208
AssemblyLine[5]: SWI, address=$020A
AssemblyLine[6]: END pseudo-op
```

### Error Handling Örnekleri

```java
// 1. Invalid label name
"123LABEL:" → ParseException("Label cannot start with digit")

// 2. Unknown instruction
"BADOP #$FF" → ParseException("Unknown instruction: BADOP")

// 3. Invalid addressing mode
"LDA ,X" → ParseException("Invalid addressing mode for LDA")

// 4. Unresolved label
"JMP NOWHERE" → ParseException("Unresolved label reference: NOWHERE")

// 5. Invalid operand format
"LDA #$GG" → ParseException("Invalid hex digit in operand")
```

## 🔧 Advanced Features

### 1. Pseudo-Instructions

```java
// ORG - Set assembly origin
"ORG $0200" → pseudoOp="ORG", operand=0x0200

// EQU - Define constant
"MAXVAL EQU $FF" → label="MAXVAL", value=0xFF

// FCB - Form Constant Byte
"FCB $12,$34,$56" → bytes=[0x12, 0x34, 0x56]

// FDB - Form Double Byte (16-bit)
"FDB $1234" → bytes=[0x12, 0x34]
```

### 2. Expression Evaluation

```java
// Arithmetic expressions (gelecek feature)
"LDA #MAXVAL+1"     → evaluate to numeric value
"ORG START+$10"     → calculate address
"FCB 'A'+1"         → character arithmetic
```

### 3. String Handling

```java
// String literals
"FCB 'HELLO'"       → [0x48, 0x45, 0x4C, 0x4C, 0x4F]
"FCC 'TEXT',0"      → form character constant with null terminator
```

## 🧪 Test Scenarios

### 1. Basic Parsing Test
```java
String source = """
    ORG $0200
    LDA #$FF
    STA $0300
    """;
    
List<AssemblyLine> result = parser.parseSource(source);
assertEquals(3, result.size());
assertEquals("LDA", result.get(1).getInstruction().getMnemonic());
```

### 2. Label Resolution Test
```java
String source = """
    JMP LATER
    NOP  
LATER SWI
    """;
    
List<AssemblyLine> result = parser.parseSource(source);
parser.resolveLabelReferences(result);
// LATER label should resolve to correct address
```

### 3. Error Handling Test
```java
assertThrows(ParseException.class, () -> {
    parser.parseSource("INVALID_INSTRUCTION #$FF");
});
```

## 🚀 Performance Considerations

### 1. Memory Usage
- **String interning** for repeated mnemonics
- **Lazy evaluation** for complex expressions
- **Efficient regex** patterns

### 2. Parse Speed
- **Single pass tokenization**
- **Hashtable lookup** for opcodes
- **Minimal object creation**

### 3. Error Recovery
- **Continue parsing** after non-fatal errors
- **Collect multiple errors** in single pass
- **Meaningful error locations**

---

Parser paketi, assembly kaynak kodunu internal representation'a çeviren kritik bir bileşendir. Error handling ve performance optimizasyonları önemlidir.
