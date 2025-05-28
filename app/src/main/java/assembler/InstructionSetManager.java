package assembler;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Motorola 6800 instruction set and opcode mappings.
 */
public class InstructionSetManager {
    private final Map<String, InstructionFormat> instructionMap;

    public InstructionSetManager() {
        instructionMap = new HashMap<>();
        initializeInstructions();
    }

    private void initializeInstructions() {
        // LDA (Load Accumulator A)
        instructionMap.put("LDA_IMM", new InstructionFormat("LDA", AddressingMode.IMMEDIATE, 0x86, 2));
        instructionMap.put("LDA_DIR", new InstructionFormat("LDA", AddressingMode.DIRECT, 0x96, 2));
        instructionMap.put("LDA_EXT", new InstructionFormat("LDA", AddressingMode.EXTENDED, 0xB6, 3));
        instructionMap.put("LDA_IDX", new InstructionFormat("LDA", AddressingMode.INDEXED, 0xA6, 2));
        // STA (Store Accumulator A)
        instructionMap.put("STA_DIR", new InstructionFormat("STA", AddressingMode.DIRECT, 0x97, 2));
        instructionMap.put("STA_EXT", new InstructionFormat("STA", AddressingMode.EXTENDED, 0xB7, 3));
        instructionMap.put("STA_IDX", new InstructionFormat("STA", AddressingMode.INDEXED, 0xA7, 2));
        // LDB (Load Accumulator B)
        instructionMap.put("LDB_IMM", new InstructionFormat("LDB", AddressingMode.IMMEDIATE, 0xC6, 2));
        instructionMap.put("LDB_DIR", new InstructionFormat("LDB", AddressingMode.DIRECT, 0xD6, 2));
        instructionMap.put("LDB_EXT", new InstructionFormat("LDB", AddressingMode.EXTENDED, 0xF6, 3));
        instructionMap.put("LDB_IDX", new InstructionFormat("LDB", AddressingMode.INDEXED, 0xE6, 2));
        // STB (Store Accumulator B)
        instructionMap.put("STB_DIR", new InstructionFormat("STB", AddressingMode.DIRECT, 0xD7, 2));
        instructionMap.put("STB_EXT", new InstructionFormat("STB", AddressingMode.EXTENDED, 0xF7, 3));
        instructionMap.put("STB_IDX", new InstructionFormat("STB", AddressingMode.INDEXED, 0xE7, 2));
        // LDX (Load Index Register)
        instructionMap.put("LDX_IMM", new InstructionFormat("LDX", AddressingMode.IMMEDIATE, 0xCE, 3));
        instructionMap.put("LDX_DIR", new InstructionFormat("LDX", AddressingMode.DIRECT, 0xDE, 3));
        instructionMap.put("LDX_EXT", new InstructionFormat("LDX", AddressingMode.EXTENDED, 0xFE, 4));
        instructionMap.put("LDX_IDX", new InstructionFormat("LDX", AddressingMode.INDEXED, 0xEE, 3));
        // STX (Store Index Register)
        instructionMap.put("STX_DIR", new InstructionFormat("STX", AddressingMode.DIRECT, 0xDF, 3));
        instructionMap.put("STX_EXT", new InstructionFormat("STX", AddressingMode.EXTENDED, 0xFF, 4));
        instructionMap.put("STX_IDX", new InstructionFormat("STX", AddressingMode.INDEXED, 0xEF, 3));
        // INX (Increment Index Register)
        instructionMap.put("INX_INH", new InstructionFormat("INX", AddressingMode.INHERENT, 0x08, 1));
        // DEX (Decrement Index Register)
        instructionMap.put("DEX_INH", new InstructionFormat("DEX", AddressingMode.INHERENT, 0x09, 1));
        // JMP (Jump)
        instructionMap.put("JMP_EXT", new InstructionFormat("JMP", AddressingMode.EXTENDED, 0x7E, 3));
        instructionMap.put("JMP_IDX", new InstructionFormat("JMP", AddressingMode.INDEXED, 0x6E, 2));
        // JSR (Jump to Subroutine)
        instructionMap.put("JSR_EXT", new InstructionFormat("JSR", AddressingMode.EXTENDED, 0xBD, 3));
        instructionMap.put("JSR_IDX", new InstructionFormat("JSR", AddressingMode.INDEXED, 0xAD, 2));
        // RTS (Return from Subroutine)
        instructionMap.put("RTS_INH", new InstructionFormat("RTS", AddressingMode.INHERENT, 0x39, 1));
        // NOP (No Operation)
        instructionMap.put("NOP_INH", new InstructionFormat("NOP", AddressingMode.INHERENT, 0x01, 1));
        // CLC (Clear Carry)
        instructionMap.put("CLC_INH", new InstructionFormat("CLC", AddressingMode.INHERENT, 0x0C, 1));
        // SEC (Set Carry)
        instructionMap.put("SEC_INH", new InstructionFormat("SEC", AddressingMode.INHERENT, 0x0D, 1));
        // BEQ (Branch if Equal)
        instructionMap.put("BEQ_REL", new InstructionFormat("BEQ", AddressingMode.DIRECT, 0x27, 2));
        // BNE (Branch if Not Equal)
        instructionMap.put("BNE_REL", new InstructionFormat("BNE", AddressingMode.DIRECT, 0x26, 2));
        // BRA (Branch Always)
        instructionMap.put("BRA_REL", new InstructionFormat("BRA", AddressingMode.DIRECT, 0x20, 2));
        // BSR (Branch to Subroutine)
        instructionMap.put("BSR_REL", new InstructionFormat("BSR", AddressingMode.DIRECT, 0x8D, 2));
        // SUBA
        instructionMap.put("SUBA_IMM", new InstructionFormat("SUBA", AddressingMode.IMMEDIATE, 0x80, 2));
        instructionMap.put("SUBA_DIR", new InstructionFormat("SUBA", AddressingMode.DIRECT, 0x90, 2));
        instructionMap.put("SUBA_EXT", new InstructionFormat("SUBA", AddressingMode.EXTENDED, 0xB0, 3));
        instructionMap.put("SUBA_IDX", new InstructionFormat("SUBA", AddressingMode.INDEXED, 0xA0, 2));
        // ADDA
        instructionMap.put("ADDA_IMM", new InstructionFormat("ADDA", AddressingMode.IMMEDIATE, 0x8B, 2));
        instructionMap.put("ADDA_DIR", new InstructionFormat("ADDA", AddressingMode.DIRECT, 0x9B, 2));
        instructionMap.put("ADDA_EXT", new InstructionFormat("ADDA", AddressingMode.EXTENDED, 0xBB, 3));
        instructionMap.put("ADDA_IDX", new InstructionFormat("ADDA", AddressingMode.INDEXED, 0xAB, 2));
        // ANDA
        instructionMap.put("ANDA_IMM", new InstructionFormat("ANDA", AddressingMode.IMMEDIATE, 0x84, 2));
        instructionMap.put("ANDA_DIR", new InstructionFormat("ANDA", AddressingMode.DIRECT, 0x94, 2));
        instructionMap.put("ANDA_EXT", new InstructionFormat("ANDA", AddressingMode.EXTENDED, 0xB4, 3));
        instructionMap.put("ANDA_IDX", new InstructionFormat("ANDA", AddressingMode.INDEXED, 0xA4, 2));
        // ORAA
        instructionMap.put("ORAA_IMM", new InstructionFormat("ORAA", AddressingMode.IMMEDIATE, 0x8A, 2));
        instructionMap.put("ORAA_DIR", new InstructionFormat("ORAA", AddressingMode.DIRECT, 0x9A, 2));
        instructionMap.put("ORAA_EXT", new InstructionFormat("ORAA", AddressingMode.EXTENDED, 0xBA, 3));
        instructionMap.put("ORAA_IDX", new InstructionFormat("ORAA", AddressingMode.INDEXED, 0xAA, 2));
        // EORA
        instructionMap.put("EORA_IMM", new InstructionFormat("EORA", AddressingMode.IMMEDIATE, 0x88, 2));
        instructionMap.put("EORA_DIR", new InstructionFormat("EORA", AddressingMode.DIRECT, 0x98, 2));
        instructionMap.put("EORA_EXT", new InstructionFormat("EORA", AddressingMode.EXTENDED, 0xB8, 3));
        instructionMap.put("EORA_IDX", new InstructionFormat("EORA", AddressingMode.INDEXED, 0xA8, 2));
        // COMA
        instructionMap.put("COMA_INH", new InstructionFormat("COMA", AddressingMode.INHERENT, 0x43, 1));
        // INCA
        instructionMap.put("INCA_INH", new InstructionFormat("INCA", AddressingMode.INHERENT, 0x4C, 1));
        // DECA
        instructionMap.put("DECA_INH", new InstructionFormat("DECA", AddressingMode.INHERENT, 0x4A, 1));
        // TSTA
        instructionMap.put("TSTA_INH", new InstructionFormat("TSTA", AddressingMode.INHERENT, 0x4D, 1));
        // CLRA
        instructionMap.put("CLRA_INH", new InstructionFormat("CLRA", AddressingMode.INHERENT, 0x4F, 1));
        // PSHA
        instructionMap.put("PSHA_INH", new InstructionFormat("PSHA", AddressingMode.INHERENT, 0x36, 1));
        // PULA
        instructionMap.put("PULA_INH", new InstructionFormat("PULA", AddressingMode.INHERENT, 0x32, 1));
        // SWI
        instructionMap.put("SWI_INH", new InstructionFormat("SWI", AddressingMode.INHERENT, 0x3F, 1));
        // ... Diğer komutlar burada tanımlanacak ...
    }

    public InstructionFormat getInstruction(String mnemonic, AddressingMode mode) {
        return instructionMap.get(mnemonic + "_" + mode.name().substring(0, 3));
    }

    // ... Gerekirse ek metotlar ...
}

/**
 * Represents an instruction's format and opcode.
 */
class InstructionFormat {
    public final String mnemonic;
    public final AddressingMode mode;
    public final int opcode;
    public final int length;

    public InstructionFormat(String mnemonic, AddressingMode mode, int opcode, int length) {
        this.mnemonic = mnemonic;
        this.mode = mode;
        this.opcode = opcode;
        this.length = length;
    }
}

/**
 * Supported addressing modes for Motorola 6800.
 */
enum AddressingMode {
    IMMEDIATE,
    DIRECT,
    EXTENDED,
    INDEXED,
    INHERENT
}
