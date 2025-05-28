package assembler;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates machine code from parsed assembly lines.
 */
public class CodeGenerator {
    private final InstructionSetManager instructionSet;
    private final LabelResolver labelResolver;

    public CodeGenerator(InstructionSetManager instructionSet, LabelResolver labelResolver) {
        this.instructionSet = instructionSet;
        this.labelResolver = labelResolver;
    }

    /**
     * Generates machine code for the given assembly lines.
     * @param lines List of parsed AssemblyLine objects
     * @return List of bytes representing the machine code
     */
    public List<Byte> generate(List<AssemblyLine> lines) {
        List<Byte> machineCode = new ArrayList<>();
        int currentAddress = 0;
        for (AssemblyLine line : lines) {
            if (line.mnemonic == null) {
                // Yorum veya boş satır
                continue;
            }
            // Pseudo-instructions (ORG, END, vb.)
            if (line.mnemonic.equalsIgnoreCase("ORG")) {
                try {
                    currentAddress = parseNumber(line.operand);
                } catch (Exception e) {
                    System.err.println("[HATA] Satır " + line.lineNumber + ": ORG operandı geçersiz: " + line.operand);
                }
                continue;
            }
            if (line.mnemonic.equalsIgnoreCase("END")) {
                break;
            }
            // Adresleme modu tespiti
            AddressingMode mode = detectAddressingMode(line.operand);
            System.out.printf("[DEBUG] Satır %d: mnemonic=%s, operand=%s, mode=%s, anahtar=%s\n", line.lineNumber, line.mnemonic, line.operand, mode, line.mnemonic + "_" + mode.name().substring(0, 3));

            InstructionFormat format = instructionSet.getInstruction(line.mnemonic.toUpperCase(), mode);
            if (format == null) {
                System.err.println("[HATA] Satır " + line.lineNumber + ": Geçersiz komut veya adresleme modu: " + line.mnemonic + " " + (line.operand != null ? line.operand : ""));
                continue;
            }
            machineCode.add((byte) format.opcode);
            // Operand ekle
            try {
                if (mode == AddressingMode.IMMEDIATE) {
                    int value = parseNumber(line.operand.replace("#", ""));
                    machineCode.add((byte) value);
                } else if (mode == AddressingMode.DIRECT || mode == AddressingMode.INDEXED) {
                    int addr = parseNumber(line.operand.replace(",X", ""));
                    machineCode.add((byte) addr);
                } else if (mode == AddressingMode.EXTENDED) {
                    int addr = parseNumber(line.operand);
                    machineCode.add((byte) ((addr >> 8) & 0xFF));
                    machineCode.add((byte) (addr & 0xFF));
                }
                // INHERENT modda operand eklenmez
            } catch (Exception e) {
                System.err.println("[HATA] Satır " + line.lineNumber + ": Operand hatası: " + line.operand);
            }
            currentAddress += format.length;
        }
        return machineCode;
    }

    private AddressingMode detectAddressingMode(String operand) {
        if (operand == null) return AddressingMode.INHERENT;
        operand = operand.trim();
        if (operand.startsWith("#")) return AddressingMode.IMMEDIATE;
        if (operand.endsWith(",X")) return AddressingMode.INDEXED;
        if (operand.length() == 2 || operand.length() == 3) return AddressingMode.DIRECT;
        if (operand.length() == 4) return AddressingMode.EXTENDED;
        // Branch ve diğer özel durumlar için ek kontrol gerekebilir
        return AddressingMode.DIRECT;
    }

    private int parseNumber(String s) {
        s = s.trim().replace("$", "");
        if (s.startsWith("0x") || s.startsWith("0X")) {
            return Integer.parseInt(s.substring(2), 16);
        } else if (s.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(s, 16);
        } else {
            return Integer.parseInt(s);
        }
    }
}
