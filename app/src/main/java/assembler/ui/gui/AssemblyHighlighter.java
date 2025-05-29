package assembler.ui.gui;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Motorola 6800 Assembly language.
 * Provides color coding for instructions, labels, comments, and operands.
 * 
 * @author Motorola 6800 Assembler Team
 */
public class AssemblyHighlighter {
    
    // Motorola 6800 instruction patterns
    private static final String[] INSTRUCTIONS = {
        // Data Movement
        "LDA", "LDB", "LDX", "STA", "STB", "STX", "TAB", "TBA", "TSX", "TXS",
        
        // Arithmetic
        "ADDA", "ADDB", "SUBA", "SUBB", "INCA", "INCB", "DECA", "DECB", "CMPA", "CMPB",
        "ADCA", "ADCB", "SBCA", "SBCB", "MUL", "DAA",
        
        // Logic
        "ANDA", "ANDB", "ORAA", "ORAB", "EORA", "EORB", "COMA", "COMB", "NEGA", "NEGB",
        "TSTA", "TSTB", "CLRA", "CLRB", "CLR",
        
        // Shift and Rotate
        "ASLA", "ASLB", "ASL", "ASRA", "ASRB", "ASR", "LSRA", "LSRB", "LSR",
        "ROLA", "ROLB", "ROL", "RORA", "RORB", "ROR",
        
        // Branch
        "BRA", "BEQ", "BNE", "BCC", "BCS", "BPL", "BMI", "BVC", "BVS", "BGE", "BLT", "BGT", "BLE",
        "BHI", "BLS", "JMP", "JSR", "RTS", "RTI",
        
        // Stack
        "PSHA", "PSHB", "PULA", "PULB", "PSH", "PUL",
        
        // Index Register
        "INX", "DEX", "CPX",
        
        // Condition Code
        "CLC", "SEC", "CLI", "SEI", "CLV", "SEV", "TAP", "TPA",
        
        // Control
        "NOP", "SWI", "WAI", "HLT"
    };
    
    // Pseudo-instructions
    private static final String[] PSEUDO_OPS = {
        "ORG", "END", "EQU", "FCB", "FDB", "RMB", "FCC"
    };
    
    // Build regex patterns
    private static final String INSTRUCTION_PATTERN = "\\b(" + String.join("|", INSTRUCTIONS) + ")\\b";
    private static final String PSEUDO_OP_PATTERN = "\\b(" + String.join("|", PSEUDO_OPS) + ")\\b";
    private static final String LABEL_PATTERN = "^\\s*[A-Za-z_][A-Za-z0-9_]*:";
    private static final String COMMENT_PATTERN = ";[^\r\n]*";
    private static final String HEX_NUMBER_PATTERN = "\\$[0-9A-Fa-f]+";
    private static final String BINARY_NUMBER_PATTERN = "%[01]+";
    private static final String DECIMAL_NUMBER_PATTERN = "\\b[0-9]+\\b";
    private static final String IMMEDIATE_PATTERN = "#\\$?[0-9A-Fa-f]+";
    private static final String INDEXED_PATTERN = ",[XY]\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    
    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<LABEL>" + LABEL_PATTERN + ")"
            + "|(?<INSTRUCTION>" + INSTRUCTION_PATTERN + ")"
            + "|(?<PSEUDOOP>" + PSEUDO_OP_PATTERN + ")"
            + "|(?<IMMEDIATE>" + IMMEDIATE_PATTERN + ")"
            + "|(?<INDEXED>" + INDEXED_PATTERN + ")"
            + "|(?<HEX>" + HEX_NUMBER_PATTERN + ")"
            + "|(?<BINARY>" + BINARY_NUMBER_PATTERN + ")"
            + "|(?<DECIMAL>" + DECIMAL_NUMBER_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
    );
    
    /**
     * Compute syntax highlighting for the given text.
     * 
     * @param text The assembly source code text
     * @return StyleSpans containing highlighting information
     */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        while (matcher.find()) {
            String styleClass = null;
            
            if (matcher.group("COMMENT") != null) {
                styleClass = "comment";
            } else if (matcher.group("LABEL") != null) {
                styleClass = "label";
            } else if (matcher.group("INSTRUCTION") != null) {
                styleClass = "instruction";
            } else if (matcher.group("PSEUDOOP") != null) {
                styleClass = "pseudo-op";
            } else if (matcher.group("IMMEDIATE") != null) {
                styleClass = "immediate";
            } else if (matcher.group("INDEXED") != null) {
                styleClass = "indexed";
            } else if (matcher.group("HEX") != null) {
                styleClass = "hex-number";
            } else if (matcher.group("BINARY") != null) {
                styleClass = "binary-number";
            } else if (matcher.group("DECIMAL") != null) {
                styleClass = "decimal-number";
            } else if (matcher.group("STRING") != null) {
                styleClass = "string";
            }
            
            if (styleClass != null) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
        }
        
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
