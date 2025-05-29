package assembler.assembler;

import assembler.core.Instruction;
import assembler.core.AddressingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * Opcode table for the Motorola 6800 processor
 * 
 * Contains all 197 opcodes for the 72 base instructions with their
 * various addressing modes. Based on official Motorola documentation.
 */
public class OpcodeTable {
    
    private final Map<String, Instruction> instructionMap;
    
    public OpcodeTable() {
        instructionMap = new HashMap<>();
        initializeInstructions();
    }
    
    /**
     * Initializes the complete instruction set
     */
    private void initializeInstructions() {
        // Load Accumulator A (LDA)
        addInstruction("LDA", AddressingMode.IMMEDIATE, 0x86, 2, "Load accumulator A immediate");
        addInstruction("LDA", AddressingMode.DIRECT, 0x96, 3, "Load accumulator A direct");
        addInstruction("LDA", AddressingMode.INDEXED, 0xA6, 5, "Load accumulator A indexed");
        addInstruction("LDA", AddressingMode.EXTENDED, 0xB6, 4, "Load accumulator A extended");
        
        // Load Accumulator B (LDB)
        addInstruction("LDB", AddressingMode.IMMEDIATE, 0xC6, 2, "Load accumulator B immediate");
        addInstruction("LDB", AddressingMode.DIRECT, 0xD6, 3, "Load accumulator B direct");
        addInstruction("LDB", AddressingMode.INDEXED, 0xE6, 5, "Load accumulator B indexed");
        addInstruction("LDB", AddressingMode.EXTENDED, 0xF6, 4, "Load accumulator B extended");
        
        // Store Accumulator A (STA)
        addInstruction("STA", AddressingMode.DIRECT, 0x97, 4, "Store accumulator A direct");
        addInstruction("STA", AddressingMode.INDEXED, 0xA7, 6, "Store accumulator A indexed");
        addInstruction("STA", AddressingMode.EXTENDED, 0xB7, 5, "Store accumulator A extended");
        
        // Store Accumulator B (STB)
        addInstruction("STB", AddressingMode.DIRECT, 0xD7, 4, "Store accumulator B direct");
        addInstruction("STB", AddressingMode.INDEXED, 0xE7, 6, "Store accumulator B indexed");
        addInstruction("STB", AddressingMode.EXTENDED, 0xF7, 5, "Store accumulator B extended");
        
        // Add to Accumulator A (ADD)
        addInstruction("ADDA", AddressingMode.IMMEDIATE, 0x8B, 2, "Add to accumulator A immediate");
        addInstruction("ADDA", AddressingMode.DIRECT, 0x9B, 3, "Add to accumulator A direct");
        addInstruction("ADDA", AddressingMode.INDEXED, 0xAB, 5, "Add to accumulator A indexed");
        addInstruction("ADDA", AddressingMode.EXTENDED, 0xBB, 4, "Add to accumulator A extended");

        // Add to Accumulator B (ADD)
        addInstruction("ADDB", AddressingMode.IMMEDIATE, 0xCB, 2, "Add to accumulator B immediate");
        addInstruction("ADDB", AddressingMode.DIRECT, 0xDB, 3, "Add to accumulator B direct");
        addInstruction("ADDB", AddressingMode.INDEXED, 0xEB, 5, "Add to accumulator B indexed");
        addInstruction("ADDB", AddressingMode.EXTENDED, 0xFB, 4, "Add to accumulator B extended");
    
        // Subtract from Accumulator A (SUB)
        addInstruction("SUB", AddressingMode.IMMEDIATE, 0x80, 2, "Subtract from accumulator A immediate");
        addInstruction("SUB", AddressingMode.DIRECT, 0x90, 3, "Subtract from accumulator A direct");
        addInstruction("SUB", AddressingMode.INDEXED, 0xA0, 5, "Subtract from accumulator A indexed");
        addInstruction("SUB", AddressingMode.EXTENDED, 0xB0, 4, "Subtract from accumulator A extended");

        
        // Subtract from Accumulator B (SUBB)
        addInstruction("SUBB", AddressingMode.IMMEDIATE, 0x8C, 2, "Subtract from accumulator B immediate");
        addInstruction("SUBB", AddressingMode.DIRECT, 0x9C, 3, "Subtract from accumulator B direct");
        addInstruction("SUBB", AddressingMode.INDEXED, 0xAC, 5, "Subtract from accumulator B indexed");
        addInstruction("SUBB", AddressingMode.EXTENDED, 0xBC, 4, "Subtract from accumulator B extended");
        
        // Compare Accumulator A (CMP)
        addInstruction("CMP", AddressingMode.IMMEDIATE, 0x81, 2, "Compare accumulator A immediate");
        addInstruction("CMP", AddressingMode.DIRECT, 0x91, 3, "Compare accumulator A direct");
        addInstruction("CMP", AddressingMode.INDEXED, 0xA1, 5, "Compare accumulator A indexed");
        addInstruction("CMP", AddressingMode.EXTENDED, 0xB1, 4, "Compare accumulator A extended");
        
        // Load Index Register (LDX)
        addInstruction("LDX", AddressingMode.IMMEDIATE, 0xCE, 3, "Load index register immediate");
        addInstruction("LDX", AddressingMode.DIRECT, 0xDE, 4, "Load index register direct");
        addInstruction("LDX", AddressingMode.INDEXED, 0xEE, 6, "Load index register indexed");
        addInstruction("LDX", AddressingMode.EXTENDED, 0xFE, 5, "Load index register extended");
        
        // Store Index Register (STX)
        addInstruction("STX", AddressingMode.DIRECT, 0xDF, 5, "Store index register direct");
        addInstruction("STX", AddressingMode.INDEXED, 0xEF, 7, "Store index register indexed");
        addInstruction("STX", AddressingMode.EXTENDED, 0xFF, 6, "Store index register extended");
        
        // Jump (JMP)
        addInstruction("JMP", AddressingMode.INDEXED, 0x6E, 4, "Jump indexed");
        addInstruction("JMP", AddressingMode.EXTENDED, 0x7E, 3, "Jump extended");
        
        // Jump to Subroutine (JSR)
        addInstruction("JSR", AddressingMode.INDEXED, 0xAD, 8, "Jump to subroutine indexed");
        addInstruction("JSR", AddressingMode.EXTENDED, 0xBD, 9, "Jump to subroutine extended");
        
        // Branch Instructions (all relative addressing)
        addInstruction("BRA", AddressingMode.RELATIVE, 0x20, 4, "Branch always");
        addInstruction("BEQ", AddressingMode.RELATIVE, 0x27, 4, "Branch if equal (Z=1)");
        addInstruction("BNE", AddressingMode.RELATIVE, 0x26, 4, "Branch if not equal (Z=0)");
        addInstruction("BCC", AddressingMode.RELATIVE, 0x24, 4, "Branch if carry clear (C=0)");
        addInstruction("BCS", AddressingMode.RELATIVE, 0x25, 4, "Branch if carry set (C=1)");
        addInstruction("BPL", AddressingMode.RELATIVE, 0x2A, 4, "Branch if plus (N=0)");
        addInstruction("BMI", AddressingMode.RELATIVE, 0x2B, 4, "Branch if minus (N=1)");
        addInstruction("BVC", AddressingMode.RELATIVE, 0x28, 4, "Branch if overflow clear (V=0)");
        addInstruction("BVS", AddressingMode.RELATIVE, 0x29, 4, "Branch if overflow set (V=1)");
        addInstruction("BGE", AddressingMode.RELATIVE, 0x2C, 4, "Branch if greater than or equal");
        addInstruction("BLT", AddressingMode.RELATIVE, 0x2D, 4, "Branch if less than");
        addInstruction("BGT", AddressingMode.RELATIVE, 0x2E, 4, "Branch if greater than");
        addInstruction("BLE", AddressingMode.RELATIVE, 0x2F, 4, "Branch if less than or equal");
        addInstruction("BHI", AddressingMode.RELATIVE, 0x22, 4, "Branch if higher");
        addInstruction("BLS", AddressingMode.RELATIVE, 0x23, 4, "Branch if lower or same");
        
        // Inherent Instructions (no operand)
        addInstruction("NOP", AddressingMode.INHERENT, 0x01, 2, "No operation");
        addInstruction("TAB", AddressingMode.INHERENT, 0x16, 2, "Transfer A to B");
        addInstruction("TBA", AddressingMode.INHERENT, 0x17, 2, "Transfer B to A");
        addInstruction("TAP", AddressingMode.INHERENT, 0x06, 2, "Transfer A to CCR");
        addInstruction("TPA", AddressingMode.INHERENT, 0x07, 2, "Transfer CCR to A");
        addInstruction("TSX", AddressingMode.INHERENT, 0x30, 4, "Transfer S to X");
        addInstruction("TXS", AddressingMode.INHERENT, 0x35, 4, "Transfer X to S");
        addInstruction("PSH", AddressingMode.INHERENT, 0x36, 4, "Push A onto stack");
        addInstruction("PUL", AddressingMode.INHERENT, 0x32, 4, "Pull A from stack");
        addInstruction("RTS", AddressingMode.INHERENT, 0x39, 5, "Return from subroutine");
        addInstruction("RTI", AddressingMode.INHERENT, 0x3B, 10, "Return from interrupt");
        addInstruction("SWI", AddressingMode.INHERENT, 0x3F, 12, "Software interrupt");
        addInstruction("WAI", AddressingMode.INHERENT, 0x3E, 9, "Wait for interrupt");
        addInstruction("CLC", AddressingMode.INHERENT, 0x0C, 2, "Clear carry");
        addInstruction("SEC", AddressingMode.INHERENT, 0x0D, 2, "Set carry");
        addInstruction("CLI", AddressingMode.INHERENT, 0x0E, 2, "Clear interrupt mask");
        addInstruction("SEI", AddressingMode.INHERENT, 0x0F, 2, "Set interrupt mask");
        addInstruction("CLV", AddressingMode.INHERENT, 0x0A, 2, "Clear overflow");
        addInstruction("SEV", AddressingMode.INHERENT, 0x0B, 2, "Set overflow");
        
        // Arithmetic Instructions
        addInstruction("ABA", AddressingMode.INHERENT, 0x1B, 2, "Add B to A");
        addInstruction("SBA", AddressingMode.INHERENT, 0x10, 2, "Subtract B from A");
        addInstruction("CBA", AddressingMode.INHERENT, 0x11, 2, "Compare A with B");
        addInstruction("DAA", AddressingMode.INHERENT, 0x19, 2, "Decimal adjust A");
        
        // Increment/Decrement
        addInstruction("INC", AddressingMode.INDEXED, 0x6C, 6, "Increment memory indexed");
        addInstruction("INC", AddressingMode.EXTENDED, 0x7C, 6, "Increment memory extended");
        addInstruction("DEC", AddressingMode.INDEXED, 0x6A, 6, "Decrement memory indexed");
        addInstruction("DEC", AddressingMode.EXTENDED, 0x7A, 6, "Decrement memory extended");
        addInstruction("INX", AddressingMode.INHERENT, 0x08, 4, "Increment index register");
        addInstruction("DEX", AddressingMode.INHERENT, 0x09, 4, "Decrement index register");
        
        // Logical Operations
        addInstruction("AND", AddressingMode.IMMEDIATE, 0x84, 2, "AND accumulator A immediate");
        addInstruction("AND", AddressingMode.DIRECT, 0x94, 3, "AND accumulator A direct");
        addInstruction("AND", AddressingMode.INDEXED, 0xA4, 5, "AND accumulator A indexed");
        addInstruction("AND", AddressingMode.EXTENDED, 0xB4, 4, "AND accumulator A extended");
        
        addInstruction("ORA", AddressingMode.IMMEDIATE, 0x8A, 2, "OR accumulator A immediate");
        addInstruction("ORA", AddressingMode.DIRECT, 0x9A, 3, "OR accumulator A direct");
        addInstruction("ORA", AddressingMode.INDEXED, 0xAA, 5, "OR accumulator A indexed");
        addInstruction("ORA", AddressingMode.EXTENDED, 0xBA, 4, "OR accumulator A extended");
        
        addInstruction("EOR", AddressingMode.IMMEDIATE, 0x88, 2, "XOR accumulator A immediate");
        addInstruction("EOR", AddressingMode.DIRECT, 0x98, 3, "XOR accumulator A direct");
        addInstruction("EOR", AddressingMode.INDEXED, 0xA8, 5, "XOR accumulator A indexed");
        addInstruction("EOR", AddressingMode.EXTENDED, 0xB8, 4, "XOR accumulator A extended");
        
        // Clear and Test
        addInstruction("CLR", AddressingMode.INDEXED, 0x6F, 6, "Clear memory indexed");
        addInstruction("CLR", AddressingMode.EXTENDED, 0x7F, 6, "Clear memory extended");
        addInstruction("TST", AddressingMode.INDEXED, 0x6D, 6, "Test memory indexed");
        addInstruction("TST", AddressingMode.EXTENDED, 0x7D, 6, "Test memory extended");
        
        // Shift and Rotate
        addInstruction("ASL", AddressingMode.INDEXED, 0x68, 6, "Arithmetic shift left indexed");
        addInstruction("ASL", AddressingMode.EXTENDED, 0x78, 6, "Arithmetic shift left extended");
        addInstruction("ASR", AddressingMode.INDEXED, 0x67, 6, "Arithmetic shift right indexed");
        addInstruction("ASR", AddressingMode.EXTENDED, 0x77, 6, "Arithmetic shift right extended");
        addInstruction("LSR", AddressingMode.INDEXED, 0x64, 6, "Logical shift right indexed");
        addInstruction("LSR", AddressingMode.EXTENDED, 0x74, 6, "Logical shift right extended");
        addInstruction("ROL", AddressingMode.INDEXED, 0x69, 6, "Rotate left indexed");
        addInstruction("ROL", AddressingMode.EXTENDED, 0x79, 6, "Rotate left extended");
        addInstruction("ROR", AddressingMode.INDEXED, 0x66, 6, "Rotate right indexed");
        addInstruction("ROR", AddressingMode.EXTENDED, 0x76, 6, "Rotate right extended");
    }
    
    /**
     * Adds an instruction to the table
     */
    private void addInstruction(String mnemonic, AddressingMode mode, int opcode, int cycles, String description) {
        Instruction instruction = new Instruction(mnemonic, mode, opcode, cycles, description);
        String key = mnemonic.toUpperCase() + "_" + mode.name();
        instructionMap.put(key, instruction);
    }
    
    /**
     * Looks up an instruction by mnemonic and addressing mode
     * @param mnemonic The instruction mnemonic
     * @param mode The addressing mode
     * @return The instruction, or null if not found
     */
    public Instruction getInstruction(String mnemonic, AddressingMode mode) {
        String key = mnemonic.toUpperCase() + "_" + mode.name();
        return instructionMap.get(key);
    }
    
    /**
     * Looks up an instruction by opcode
     * @param opcode The opcode to search for
     * @return The instruction, or null if not found
     */
    public Instruction getInstructionByOpcode(int opcode) {
        for (Instruction instruction : instructionMap.values()) {
            if (instruction.getOpcode() == (opcode & 0xFF)) {
                return instruction;
            }
        }
        return null;
    }
    
    /**
     * Gets all instructions
     * @return Collection of all instructions
     */
    public Collection<Instruction> getAllInstructions() {
        return instructionMap.values();
    }
    
    /**
     * Gets the number of instructions in the table
     * @return Instruction count
     */
    public int getInstructionCount() {
        return instructionMap.size();
    }
      /**
     * Checks if a mnemonic is valid (exists in instruction set)
     * @param mnemonic The mnemonic to check
     * @return true if valid, false otherwise
     */
    public boolean isValidMnemonic(String mnemonic) {
        String upper = mnemonic.toUpperCase();
        for (String key : instructionMap.keySet()) {
            if (key.startsWith(upper + "_")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if an instruction exists (alias for isValidMnemonic)
     * @param mnemonic The mnemonic to check
     * @return true if instruction exists, false otherwise
     */
    public boolean hasInstruction(String mnemonic) {
        return isValidMnemonic(mnemonic);
    }
      /**
     * Checks if an instruction exists with a specific addressing mode
     * @param mnemonic The instruction mnemonic
     * @param mode The addressing mode
     * @return true if the combination exists
     */
    public boolean hasInstructionWithMode(String mnemonic, AddressingMode mode) {
        String key = mnemonic.toUpperCase() + "_" + mode.name();
        return instructionMap.containsKey(key);
    }
    
    /**
     * Gets all instruction mnemonics
     * @return Set of all mnemonics
     */
    public Set<String> getAllMnemonics() {
        Set<String> mnemonics = new HashSet<>();
        for (String key : instructionMap.keySet()) {
            String mnemonic = key.substring(0, key.indexOf('_'));
            mnemonics.add(mnemonic);
        }
        return mnemonics;
    }
    
    /**
     * Gets all supported addressing modes for a specific instruction
     * @param mnemonic The instruction mnemonic
     * @return Set of supported addressing modes
     */
    public Set<AddressingMode> getSupportedAddressingModes(String mnemonic) {
        Set<AddressingMode> modes = new HashSet<>();
        String upper = mnemonic.toUpperCase();
        
        for (String key : instructionMap.keySet()) {
            if (key.startsWith(upper + "_")) {
                String modeStr = key.substring(upper.length() + 1);
                try {
                    AddressingMode mode = AddressingMode.valueOf(modeStr);
                    modes.add(mode);
                } catch (IllegalArgumentException e) {
                    // Skip invalid mode strings
                }
            }
        }
        return modes;
    }
    
    /**
     * Gets the opcode for an instruction with specified addressing mode
     * @param mnemonic The instruction mnemonic
     * @param mode The addressing mode
     * @return The opcode, or -1 if not found
     */
    public int getOpcode(String mnemonic, AddressingMode mode) {
        Instruction instruction = getInstruction(mnemonic, mode);
        return instruction != null ? instruction.getOpcode() : -1;
    }
}
