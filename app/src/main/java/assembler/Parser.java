package assembler;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Motorola 6800 assembly code into structured AssemblyLine objects.
 */
public class Parser {
    /**
     * Parses the given assembly code lines.
     * @param lines List of code lines
     * @return List of parsed AssemblyLine objects
     */
    public List<AssemblyLine> parse(List<String> lines) {
        List<AssemblyLine> result = new ArrayList<>();
        int lineNumber = 1;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith(";")) {
                lineNumber++;
                continue;
            }
            String label = null, mnemonic = null, operand = null, comment = null;
            String[] parts = trimmed.split(";", 2);
            String codePart = parts[0].trim();
            if (parts.length > 1) comment = parts[1].trim();
            String[] tokens = codePart.split("\\s+", 3);
            if (tokens.length == 3) {
                label = tokens[0];
                mnemonic = tokens[1];
                operand = tokens[2];
            } else if (tokens.length == 2) {
                // Label veya operand olabilir, kontrol et
                if (tokens[0].endsWith(":")) {
                    label = tokens[0].substring(0, tokens[0].length() - 1);
                    mnemonic = tokens[1];
                } else {
                    mnemonic = tokens[0];
                    operand = tokens[1];
                }
            } else if (tokens.length == 1) {
                mnemonic = tokens[0];
            }
            // Temel sözdizimi kontrolü
            if (mnemonic == null || mnemonic.isEmpty()) {
                System.err.println("[HATA] Satır " + lineNumber + ": Komut eksik veya hatalı satır: '" + line + "'");
                lineNumber++;
                continue;
            }
            result.add(new AssemblyLine(lineNumber, label, mnemonic, operand, comment));
            lineNumber++;
        }
        return result;
    }
}
