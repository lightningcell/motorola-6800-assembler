package assembler.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility class for file I/O operations.
 * Handles loading and saving assembly source files and machine code.
 * 
 * @author Motorola 6800 Assembler Team
 */
public class FileManager {
    
    /**
     * Load assembly source code from file.
     * 
     * @param filePath Path to the assembly source file
     * @return Source code as string
     * @throws IOException if file cannot be read
     */
    public static String loadSourceFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }
    
    /**
     * Save assembly source code to file.
     * 
     * @param filePath Path to save the file
     * @param sourceCode Assembly source code
     * @throws IOException if file cannot be written
     */
    public static void saveSourceFile(String filePath, String sourceCode) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(path, sourceCode);
    }
    
    /**
     * Save machine code to binary file.
     * 
     * @param filePath Path to save the binary file
     * @param machineCode Map of addresses to machine code bytes
     * @throws IOException if file cannot be written
     */
    public static void saveBinaryFile(String filePath, Map<Integer, List<Integer>> machineCode) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Find memory range
            int minAddr = machineCode.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
            int maxAddr = machineCode.keySet().stream().mapToInt(addr -> {
                List<Integer> bytes = machineCode.get(addr);
                return addr + bytes.size() - 1;
            }).max().orElse(0);
            
            // Write contiguous memory image
            for (int addr = minAddr; addr <= maxAddr; addr++) {
                byte value = 0;
                
                // Find which block contains this address
                for (Map.Entry<Integer, List<Integer>> entry : machineCode.entrySet()) {
                    int blockStart = entry.getKey();
                    List<Integer> bytes = entry.getValue();
                    int blockEnd = blockStart + bytes.size() - 1;
                    
                    if (addr >= blockStart && addr <= blockEnd) {
                        value = (byte) (bytes.get(addr - blockStart) & 0xFF);
                        break;
                    }
                }
                
                fos.write(value);
            }
        }
    }
    
    /**
     * Save machine code to Intel HEX format.
     * 
     * @param filePath Path to save the HEX file
     * @param machineCode Map of addresses to machine code bytes
     * @throws IOException if file cannot be written
     */
    public static void saveHexFile(String filePath, Map<Integer, List<Integer>> machineCode) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Map.Entry<Integer, List<Integer>> entry : machineCode.entrySet()) {
                int address = entry.getKey();
                List<Integer> bytes = entry.getValue();
                
                // Write in 16-byte chunks
                for (int i = 0; i < bytes.size(); i += 16) {
                    int chunkSize = Math.min(16, bytes.size() - i);
                    int chunkAddr = address + i;
                    
                    StringBuilder line = new StringBuilder();
                    line.append(String.format(":%02X%04X00", chunkSize, chunkAddr));
                    
                    int checksum = chunkSize + (chunkAddr >> 8) + (chunkAddr & 0xFF);
                    
                    for (int j = 0; j < chunkSize; j++) {
                        int byteValue = bytes.get(i + j);
                        line.append(String.format("%02X", byteValue));
                        checksum += byteValue;
                    }
                    
                    checksum = (256 - (checksum & 0xFF)) & 0xFF;
                    line.append(String.format("%02X", checksum));
                    
                    writer.println(line.toString());
                }
            }
            
            // End of file record
            writer.println(":00000001FF");
        }
    }
    
    /**
     * Save assembly listing to file.
     * Shows line-by-line mapping of assembly to machine code.
     * 
     * @param filePath Path to save the listing file
     * @param assemblyLines List of assembly lines with machine code
     * @throws IOException if file cannot be written
     */
    public static void saveListingFile(String filePath, List<Object> assemblyLines) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Motorola 6800 Assembler Listing");
            writer.println("================================");
            writer.println();
            writer.printf("%-6s %-8s %-12s %s%n", "Line", "Address", "Machine Code", "Source");
            writer.println("------------------------------------------------------------");
            
            for (Object lineObj : assemblyLines) {
                // This would need to be properly typed based on your AssemblyLine implementation
                // For now, using Object to avoid compilation issues
                writer.println(lineObj.toString());
            }
        }
    }
    
    /**
     * Create example assembly program file.
     * 
     * @param filePath Path to create the example file
     * @throws IOException if file cannot be written
     */
    public static void createExampleProgram(String filePath) throws IOException {
        String exampleCode = "; Motorola 6800 Example Program\n" +
                           "; Simple loop to add numbers 1-10\n" +
                           "\n" +
                           "        ORG     $0100       ; Start at address $0100\n" +
                           "\n" +
                           "START:  LDA     #$00        ; Clear accumulator\n" +
                           "        STA     SUM         ; Initialize sum to 0\n" +
                           "        LDA     #$01        ; Load counter with 1\n" +
                           "\n" +
                           "LOOP:   ADD     SUM         ; Add current sum\n" +
                           "        STA     SUM         ; Store new sum\n" +
                           "        LDA     COUNTER     ; Load counter\n" +
                           "        ADD     #$01        ; Increment counter\n" +
                           "        STA     COUNTER     ; Store counter\n" +
                           "        CMP     #$0A        ; Compare with 10\n" +
                           "        BNE     LOOP        ; Branch if not equal\n" +
                           "\n" +
                           "        SWI                 ; Halt program\n" +
                           "\n" +
                           "; Variables\n" +
                           "COUNTER: FCB    $01         ; Counter variable\n" +
                           "SUM:     FCB    $00         ; Sum variable\n" +
                           "\n" +
                           "        END                 ; End of program\n";
                           
        saveSourceFile(filePath, exampleCode);
    }
    
    /**
     * Get file extension from file path.
     */
    public static String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(lastDot + 1).toLowerCase();
    }
    
    /**
     * Check if file exists.
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Create backup of existing file.
     */
    public static void backupFile(String filePath) throws IOException {
        Path source = Paths.get(filePath);
        if (Files.exists(source)) {
            Path backup = Paths.get(filePath + ".bak");
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
