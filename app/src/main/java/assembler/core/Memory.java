package assembler.core;

/**
 * Represents the memory system of the Motorola 6800 processor
 * 
 * The 6800 has a 16-bit address bus allowing access to 64KB of memory space.
 * Memory is organized as:
 * - $0000-$00FF: Zero page (fast direct addressing)
 * - $0100-$01FF: Stack area (default)
 * - $0200-$FFFF: General purpose memory
 * - $FFFE-$FFFF: Reset vector (PC loaded from here on reset)
 */
public class Memory {
    
    /** Total memory size (64KB) */
    public static final int MEMORY_SIZE = 65536;
    
    /** Zero page size (256 bytes) */
    public static final int ZERO_PAGE_SIZE = 256;
    
    /** Reset vector address */
    public static final int RESET_VECTOR = 0xFFFE;
    
    /** Memory array */
    private final int[] memory;
    
    /**
     * Creates a new memory system initialized to zero
     */
    public Memory() {
        memory = new int[MEMORY_SIZE];
        clear();
    }
    
    /**
     * Clears all memory to zero
     */
    public void clear() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0x00;
        }
    }
    
    /**
     * Reads a byte from memory
     * @param address The 16-bit address to read from
     * @return The byte value at that address
     */
    public int readByte(int address) {
        address &= 0xFFFF; // Ensure 16-bit address
        return memory[address] & 0xFF;
    }
    
    /**
     * Writes a byte to memory
     * @param address The 16-bit address to write to
     * @param value The byte value to write
     */
    public void writeByte(int address, int value) {
        address &= 0xFFFF; // Ensure 16-bit address
        memory[address] = value & 0xFF;
    }
    
    /**
     * Reads a 16-bit word from memory (big-endian)
     * @param address The address of the high byte
     * @return The 16-bit word value
     */
    public int readWord(int address) {
        int highByte = readByte(address);
        int lowByte = readByte(address + 1);
        return (highByte << 8) | lowByte;
    }
    
    /**
     * Writes a 16-bit word to memory (big-endian)
     * @param address The address for the high byte
     * @param value The 16-bit word value to write
     */
    public void writeWord(int address, int value) {
        value &= 0xFFFF;
        writeByte(address, (value >> 8) & 0xFF);     // High byte
        writeByte(address + 1, value & 0xFF);        // Low byte
    }
    
    /**
     * Loads binary data into memory starting at specified address
     * @param startAddress The starting address
     * @param data The byte array to load
     */
    public void loadData(int startAddress, int[] data) {
        for (int i = 0; i < data.length; i++) {
            int address = (startAddress + i) & 0xFFFF;
            if (address < MEMORY_SIZE) {
                memory[address] = data[i] & 0xFF;
            }
        }
    }
    
    /**
     * Extracts a portion of memory as a byte array
     * @param startAddress Starting address
     * @param length Number of bytes to extract
     * @return Byte array containing the memory contents
     */
    public int[] extractData(int startAddress, int length) {
        int[] data = new int[length];
        for (int i = 0; i < length; i++) {
            int address = (startAddress + i) & 0xFFFF;
            data[i] = memory[address];
        }
        return data;
    }
    
    /**
     * Gets the reset vector (address where PC starts)
     * @return The 16-bit reset vector value
     */
    public int getResetVector() {
        return readWord(RESET_VECTOR);
    }
    
    /**
     * Sets the reset vector
     * @param address The 16-bit address to set as reset vector
     */
    public void setResetVector(int address) {
        writeWord(RESET_VECTOR, address);
    }
    
    /**
     * Checks if an address is in zero page
     * @param address The address to check
     * @return true if address is in zero page (0x00-0xFF)
     */
    public boolean isZeroPage(int address) {
        return (address & 0xFF00) == 0;
    }
    
    /**
     * Gets a hex dump of memory for a given range
     * @param startAddress Starting address
     * @param length Number of bytes to dump
     * @return Formatted hex dump string
     */
    public String getHexDump(int startAddress, int length) {
        StringBuilder sb = new StringBuilder();
        int address = startAddress & 0xFFFF;
        
        for (int i = 0; i < length; i += 16) {
            sb.append(String.format("%04X: ", address + i));
            
            // Hex bytes
            for (int j = 0; j < 16 && (i + j) < length; j++) {
                int addr = (address + i + j) & 0xFFFF;
                sb.append(String.format("%02X ", memory[addr]));
            }
            
            // Padding for incomplete lines
            for (int j = length - i; j < 16; j++) {
                sb.append("   ");
            }
            
            sb.append(" ");
            
            // ASCII representation
            for (int j = 0; j < 16 && (i + j) < length; j++) {
                int addr = (address + i + j) & 0xFFFF;
                int value = memory[addr];
                char c = (value >= 32 && value <= 126) ? (char) value : '.';
                sb.append(c);
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Memory: %d bytes, Reset Vector: $%04X", 
                           MEMORY_SIZE, getResetVector());
    }
}
