package assembler.core;

/**
 * Represents a label in assembly source code
 * 
 * Labels are symbolic names that represent memory addresses.
 * They can be defined by placing them at the beginning of a line
 * followed by a colon (e.g., "LOOP:", "START:", "DATA:")
 */
public class Label {
    
    private final String name;
    private int address;
    private final int lineNumber;
    private boolean resolved;
      /**
     * Creates a new label with just a name (used during parsing)
     * @param name The label name (without colon)
     */
    public Label(String name) {
        this(name, 0, 0, false);
    }
    
    /**
     * Creates a new unresolved label with line number
     * @param name The label name (without colon)
     * @param lineNumber The source line number where defined
     */
    public Label(String name, int lineNumber) {
        this(name, lineNumber, 0, false);
    }
    
    /**
     * Creates a new label with known address
     * @param name The label name
     * @param lineNumber The source line number where defined
     * @param address The resolved memory address
     * @param resolved Whether the address is known
     */
    public Label(String name, int lineNumber, int address, boolean resolved) {
        this.name = name.toUpperCase();
        this.lineNumber = lineNumber;
        this.address = address & 0xFFFF;
        this.resolved = resolved;
    }
    
    /**
     * Gets the label name
     * @return The label name (uppercase)
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the resolved address
     * @return The 16-bit address, or 0 if not resolved
     */
    public int getAddress() {
        return address;
    }
    
    /**
     * Sets the address of this label
     * @param address The new address
     */
    public void setAddress(int address) {
        this.address = address & 0xFFFF;
        this.resolved = true;
    }
    
    /**
     * Gets the source line number where this label was defined
     * @return Line number (1-based)
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Checks if this label has been resolved to an address
     * @return true if resolved, false otherwise
     */
    public boolean isResolved() {
        return resolved;
    }
      /**
     * Creates a resolved version of this label with known address
     * @param address The resolved address
     * @return New resolved label instance
     */
    public Label resolve(int address) {
        return new Label(name, lineNumber, address, true);
    }
    
    /**
     * Creates a new resolved label with name and address (factory method)
     * @param name The label name
     * @param address The resolved memory address
     * @return New resolved label instance
     */
    public static Label createResolved(String name, int address) {
        return new Label(name, 0, address, true);
    }
    
    /**
     * Validates a label name according to 6800 assembly rules
     * @param name The label name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Must start with letter or underscore
        char first = name.charAt(0);
        if (!Character.isLetter(first) && first != '_') {
            return false;
        }
        
        // Subsequent characters must be letters, digits, or underscore
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        
        // Check length (reasonable limit)
        if (name.length() > 32) {
            return false;
        }
        
        // Check against reserved words (6800 mnemonics)
        return !isReservedWord(name.toUpperCase());
    }
    
    /**
     * Checks if a name is a reserved word (instruction mnemonic)
     * @param name The name to check (should be uppercase)
     * @return true if reserved, false otherwise
     */
    private static boolean isReservedWord(String name) {
        // Common 6800 instruction mnemonics
        String[] reserved = {
            "ABA", "ADC", "ADD", "AND", "ASL", "ASR", "BCC", "BCS", "BEQ", "BGE",
            "BGT", "BHI", "BIT", "BLE", "BLS", "BLT", "BMI", "BNE", "BPL", "BRA",
            "BSR", "BVC", "BVS", "CBA", "CLC", "CLI", "CLR", "CLV", "CMP", "COM",
            "CPX", "DAA", "DEC", "DES", "DEX", "EOR", "INC", "INS", "INX", "JMP",
            "JSR", "LDA", "LDS", "LDX", "LSR", "NEG", "NOP", "ORA", "PSH", "PUL",
            "ROL", "ROR", "RTI", "RTS", "SBA", "SBC", "SEC", "SEI", "SEV", "STA",
            "STS", "STX", "SUB", "SWI", "TAB", "TAP", "TBA", "TPA", "TST", "TSX",
            "TXS", "WAI",
            // Pseudo-instructions
            "ORG", "END", "EQU", "FCB", "FCC", "FDB", "RMB"
        };
        
        for (String word : reserved) {
            if (word.equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        if (resolved) {
            return String.format("%s: $%04X (line %d)", name, address, lineNumber);
        } else {
            return String.format("%s: UNRESOLVED (line %d)", name, lineNumber);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Label label = (Label) obj;
        return name.equals(label.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
