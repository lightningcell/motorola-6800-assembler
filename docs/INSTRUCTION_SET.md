# Motorola 6800 Instruction Set Reference

This document provides a comprehensive reference for the Motorola 6800 instruction set as implemented in the assembler. Each instruction includes syntax, addressing modes, opcodes, and examples.

## Table of Contents

- [Instruction Format](#instruction-format)
- [Addressing Modes](#addressing-modes)
- [Status Flags](#status-flags)
- [Instruction Categories](#instruction-categories)
  - [Load and Store Instructions](#load-and-store-instructions)
  - [Arithmetic Instructions](#arithmetic-instructions)
  - [Logic Instructions](#logic-instructions)
  - [Shift and Rotate Instructions](#shift-and-rotate-instructions)
  - [Branch Instructions](#branch-instructions)
  - [Jump and Subroutine Instructions](#jump-and-subroutine-instructions)
  - [Stack Instructions](#stack-instructions)
  - [System Instructions](#system-instructions)
  - [Pseudo-Instructions](#pseudo-instructions)

---

## Instruction Format

Assembly instructions follow this general format:
```
[LABEL:]  MNEMONIC  [OPERAND]  [; COMMENT]
```

**Examples**:
```assembly
START:    LDA      #$FF       ; Load immediate value
          STA      $0100      ; Store to memory
LOOP:     BNE      LOOP       ; Branch to label
```

---

## Addressing Modes

The 6800 supports several addressing modes for accessing operands:

| Mode | Syntax | Description | Example |
|------|--------|-------------|---------|
| **Immediate** | `#$nn` | Operand is the value itself | `LDA #$FF` |
| **Direct** | `$nn` | Operand is an 8-bit address (zero page) | `LDA $80` |
| **Extended** | `$nnnn` | Operand is a 16-bit address | `LDA $1000` |
| **Indexed** | `$nn,X` | Address = operand + Index Register | `LDA $10,X` |
| **Inherent** | _(none)_ | No operand required | `NOP` |
| **Relative** | `label` | 8-bit signed offset for branches | `BNE LOOP` |

---

## Status Flags

The 6800 has a 6-bit status register with these flags:

| Flag | Bit | Name | Description |
|------|-----|------|-------------|
| **C** | 0 | Carry | Set when arithmetic produces carry/borrow |
| **V** | 1 | Overflow | Set when signed arithmetic overflows |
| **Z** | 2 | Zero | Set when result is zero |
| **N** | 3 | Negative | Set when result is negative (bit 7 = 1) |
| **I** | 4 | Interrupt | Interrupt disable flag |
| **H** | 5 | Half-carry | Carry from bit 3 to bit 4 |

---

## Instruction Categories

### Load and Store Instructions

#### LDA - Load Accumulator
Load a value into the accumulator.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `LDA #$nn` | 86 | 2 | 2 | N,Z |
| Direct | `LDA $nn` | 96 | 2 | 3 | N,Z |
| Extended | `LDA $nnnn` | B6 | 3 | 4 | N,Z |
| Indexed | `LDA $nn,X` | A6 | 2 | 4 | N,Z |

**Examples**:
```assembly
LDA #$FF        ; Load immediate value $FF
LDA $80         ; Load from zero page address $80
LDA $1000       ; Load from extended address $1000
LDA $10,X       ; Load from address $10 + X register
```

#### STA - Store Accumulator
Store the accumulator value to memory.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Direct | `STA $nn` | 97 | 2 | 4 | N,Z |
| Extended | `STA $nnnn` | B7 | 3 | 5 | N,Z |
| Indexed | `STA $nn,X` | A7 | 2 | 5 | N,Z |

**Examples**:
```assembly
STA $80         ; Store to zero page address $80
STA $1000       ; Store to extended address $1000
STA $10,X       ; Store to address $10 + X register
```

#### LDX - Load Index Register
Load a 16-bit value into the index register.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `LDX #$nnnn` | CE | 3 | 3 | N,Z |
| Direct | `LDX $nn` | DE | 2 | 4 | N,Z |
| Extended | `LDX $nnnn` | FE | 3 | 5 | N,Z |
| Indexed | `LDX $nn,X` | EE | 2 | 5 | N,Z |

#### STX - Store Index Register
Store the 16-bit index register to memory.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Direct | `STX $nn` | DF | 2 | 5 | N,Z |
| Extended | `STX $nnnn` | FF | 3 | 6 | N,Z |
| Indexed | `STX $nn,X` | EF | 2 | 6 | N,Z |

---

### Arithmetic Instructions

#### ADD - Add to Accumulator
Add memory value to accumulator.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `ADD #$nn` | 8B | 2 | 2 | H,N,Z,V,C |
| Direct | `ADD $nn` | 9B | 2 | 3 | H,N,Z,V,C |
| Extended | `ADD $nnnn` | BB | 3 | 4 | H,N,Z,V,C |
| Indexed | `ADD $nn,X` | AB | 2 | 4 | H,N,Z,V,C |

#### ADC - Add with Carry
Add memory value plus carry flag to accumulator.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `ADC #$nn` | 89 | 2 | 2 | H,N,Z,V,C |
| Direct | `ADC $nn` | 99 | 2 | 3 | H,N,Z,V,C |
| Extended | `ADC $nnnn` | B9 | 3 | 4 | H,N,Z,V,C |
| Indexed | `ADC $nn,X` | A9 | 2 | 4 | H,N,Z,V,C |

#### SUB - Subtract from Accumulator
Subtract memory value from accumulator.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `SUB #$nn` | 80 | 2 | 2 | N,Z,V,C |
| Direct | `SUB $nn` | 90 | 2 | 3 | N,Z,V,C |
| Extended | `SUB $nnnn` | B0 | 3 | 4 | N,Z,V,C |
| Indexed | `SUB $nn,X` | A0 | 2 | 4 | N,Z,V,C |

#### SBC - Subtract with Carry
Subtract memory value and carry flag from accumulator.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `SBC #$nn` | 82 | 2 | 2 | N,Z,V,C |
| Direct | `SBC $nn` | 92 | 2 | 3 | N,Z,V,C |
| Extended | `SBC $nnnn` | B2 | 3 | 4 | N,Z,V,C |
| Indexed | `SBC $nn,X` | A2 | 2 | 4 | N,Z,V,C |

---

### Logic Instructions

#### AND - Logical AND
Perform bitwise AND between accumulator and memory.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `AND #$nn` | 84 | 2 | 2 | N,Z,V=0 |
| Direct | `AND $nn` | 94 | 2 | 3 | N,Z,V=0 |
| Extended | `AND $nnnn` | B4 | 3 | 4 | N,Z,V=0 |
| Indexed | `AND $nn,X` | A4 | 2 | 4 | N,Z,V=0 |

#### ORA - Logical OR
Perform bitwise OR between accumulator and memory.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `ORA #$nn` | 8A | 2 | 2 | N,Z,V=0 |
| Direct | `ORA $nn` | 9A | 2 | 3 | N,Z,V=0 |
| Extended | `ORA $nnnn` | BA | 3 | 4 | N,Z,V=0 |
| Indexed | `ORA $nn,X` | AA | 2 | 4 | N,Z,V=0 |

#### EOR - Exclusive OR
Perform bitwise exclusive OR between accumulator and memory.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `EOR #$nn` | 88 | 2 | 2 | N,Z,V=0 |
| Direct | `EOR $nn` | 98 | 2 | 3 | N,Z,V=0 |
| Extended | `EOR $nnnn` | B8 | 3 | 4 | N,Z,V=0 |
| Indexed | `EOR $nn,X` | A8 | 2 | 4 | N,Z,V=0 |

#### CMP - Compare Accumulator
Compare accumulator with memory (subtract without storing result).

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Immediate | `CMP #$nn` | 81 | 2 | 2 | N,Z,V,C |
| Direct | `CMP $nn` | 91 | 2 | 3 | N,Z,V,C |
| Extended | `CMP $nnnn` | B1 | 3 | 4 | N,Z,V,C |
| Indexed | `CMP $nn,X` | A1 | 2 | 4 | N,Z,V,C |

---

### Shift and Rotate Instructions

#### ASL - Arithmetic Shift Left
Shift accumulator or memory left by one bit.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Accumulator | `ASL` | 48 | 1 | 2 | N,Z,V,C |
| Extended | `ASL $nnnn` | 78 | 3 | 6 | N,Z,V,C |
| Indexed | `ASL $nn,X` | 68 | 2 | 6 | N,Z,V,C |

#### ASR - Arithmetic Shift Right
Shift accumulator or memory right by one bit (sign extend).

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Accumulator | `ASR` | 47 | 1 | 2 | N,Z,V,C |
| Extended | `ASR $nnnn` | 77 | 3 | 6 | N,Z,V,C |
| Indexed | `ASR $nn,X` | 67 | 2 | 6 | N,Z,V,C |

#### LSR - Logical Shift Right
Shift accumulator or memory right by one bit (zero fill).

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Accumulator | `LSR` | 44 | 1 | 2 | N=0,Z,V,C |
| Extended | `LSR $nnnn` | 74 | 3 | 6 | N=0,Z,V,C |
| Indexed | `LSR $nn,X` | 64 | 2 | 6 | N=0,Z,V,C |

#### ROL - Rotate Left
Rotate accumulator or memory left through carry.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Accumulator | `ROL` | 49 | 1 | 2 | N,Z,V,C |
| Extended | `ROL $nnnn` | 79 | 3 | 6 | N,Z,V,C |
| Indexed | `ROL $nn,X` | 69 | 2 | 6 | N,Z,V,C |

#### ROR - Rotate Right
Rotate accumulator or memory right through carry.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Accumulator | `ROR` | 46 | 1 | 2 | N,Z,V,C |
| Extended | `ROR $nnnn` | 76 | 3 | 6 | N,Z,V,C |
| Indexed | `ROR $nn,X` | 66 | 2 | 6 | N,Z,V,C |

---

### Branch Instructions

All branch instructions use relative addressing mode with 8-bit signed offset (-128 to +127).

#### BCC - Branch if Carry Clear
Branch if C flag is clear.

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BCC label` | 24 | 2 | 4/3 | C = 0 |

#### BCS - Branch if Carry Set
Branch if C flag is set.

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BCS label` | 25 | 2 | 4/3 | C = 1 |

#### BEQ - Branch if Equal
Branch if Z flag is set (result was zero).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BEQ label` | 27 | 2 | 4/3 | Z = 1 |

#### BNE - Branch if Not Equal
Branch if Z flag is clear (result was not zero).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BNE label` | 26 | 2 | 4/3 | Z = 0 |

#### BMI - Branch if Minus
Branch if N flag is set (result was negative).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BMI label` | 2B | 2 | 4/3 | N = 1 |

#### BPL - Branch if Plus
Branch if N flag is clear (result was positive).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BPL label` | 2A | 2 | 4/3 | N = 0 |

#### BVS - Branch if Overflow Set
Branch if V flag is set.

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BVS label` | 29 | 2 | 4/3 | V = 1 |

#### BVC - Branch if Overflow Clear
Branch if V flag is clear.

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BVC label` | 28 | 2 | 4/3 | V = 0 |

#### BHI - Branch if Higher
Branch if C = 0 and Z = 0 (unsigned greater than).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BHI label` | 22 | 2 | 4/3 | C = 0 AND Z = 0 |

#### BLS - Branch if Lower or Same
Branch if C = 1 or Z = 1 (unsigned less than or equal).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BLS label` | 23 | 2 | 4/3 | C = 1 OR Z = 1 |

#### BGE - Branch if Greater or Equal
Branch if N ⊕ V = 0 (signed greater than or equal).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BGE label` | 2C | 2 | 4/3 | N ⊕ V = 0 |

#### BLT - Branch if Less Than
Branch if N ⊕ V = 1 (signed less than).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BLT label` | 2D | 2 | 4/3 | N ⊕ V = 1 |

#### BGT - Branch if Greater Than
Branch if Z = 0 AND N ⊕ V = 0 (signed greater than).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BGT label` | 2E | 2 | 4/3 | Z = 0 AND N ⊕ V = 0 |

#### BLE - Branch if Less or Equal
Branch if Z = 1 OR N ⊕ V = 1 (signed less than or equal).

| Syntax | Opcode | Bytes | Cycles | Condition |
|--------|--------|-------|--------|-----------|
| `BLE label` | 2F | 2 | 4/3 | Z = 1 OR N ⊕ V = 1 |

---

### Jump and Subroutine Instructions

#### JMP - Jump
Jump to specified address.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Extended | `JMP $nnnn` | 7E | 3 | 4 |
| Indexed | `JMP $nn,X` | 6E | 2 | 4 |

#### JSR - Jump to Subroutine
Save return address on stack and jump to subroutine.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Extended | `JSR $nnnn` | BD | 3 | 8 |
| Indexed | `JSR $nn,X` | AD | 2 | 8 |

#### RTS - Return from Subroutine
Pull return address from stack and return.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Inherent | `RTS` | 39 | 1 | 5 |

#### RTI - Return from Interrupt
Pull processor status and return address from stack.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Inherent | `RTI` | 3B | 1 | 10 |

---

### Stack Instructions

#### PSH - Push onto Stack
Push accumulator or index register onto stack.

| Register | Syntax | Opcode | Bytes | Cycles |
|----------|--------|--------|-------|--------|
| Accumulator | `PSH` | 36 | 1 | 4 |
| Index | `PSX` | 3C | 1 | 4 |

#### PUL - Pull from Stack
Pull from stack into accumulator or index register.

| Register | Syntax | Opcode | Bytes | Cycles |
|----------|--------|--------|-------|--------|
| Accumulator | `PUL` | 32 | 1 | 4 |
| Index | `PLX` | 38 | 1 | 5 |

---

### System Instructions

#### NOP - No Operation
Do nothing for one instruction cycle.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Inherent | `NOP` | 01 | 1 | 2 |

#### SWI - Software Interrupt
Generate software interrupt.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Inherent | `SWI` | 3F | 1 | 12 |

#### WAI - Wait for Interrupt
Wait for interrupt signal.

| Mode | Syntax | Opcode | Bytes | Cycles |
|------|--------|--------|-------|--------|
| Inherent | `WAI` | 3E | 1 | 9 |

#### CLI - Clear Interrupt Mask
Clear interrupt disable flag.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Inherent | `CLI` | 0E | 1 | 2 | I = 0 |

#### SEI - Set Interrupt Mask
Set interrupt disable flag.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Inherent | `SEI` | 0F | 1 | 2 | I = 1 |

#### CLC - Clear Carry
Clear carry flag.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Inherent | `CLC` | 0C | 1 | 2 | C = 0 |

#### SEC - Set Carry
Set carry flag.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Inherent | `SEC` | 0D | 1 | 2 | C = 1 |

#### CLV - Clear Overflow
Clear overflow flag.

| Mode | Syntax | Opcode | Bytes | Cycles | Flags |
|------|--------|--------|-------|--------|-------|
| Inherent | `CLV` | 0A | 1 | 2 | V = 0 |

---

### Pseudo-Instructions

These are assembler directives, not actual 6800 instructions.

#### ORG - Set Origin
Set the assembly address for following code.

**Syntax**: `ORG $address`
**Example**: `ORG $0100`

#### END - End of Program
Mark the end of the assembly program.

**Syntax**: `END`

#### FCB - Form Constant Byte
Define a byte constant in memory.

**Syntax**: `FCB $value` or `FCB value`
**Examples**:
```assembly
VALUE:  FCB $FF         ; Single byte
DATA:   FCB $12,$34,$56 ; Multiple bytes
```

#### FDB - Form Double Byte
Define a 16-bit word constant in memory.

**Syntax**: `FDB $value` or `FDB value`
**Examples**:
```assembly
ADDR:   FDB $1000       ; 16-bit address
SIZE:   FDB 1024        ; Decimal value
```

#### RMB - Reserve Memory Block
Reserve a block of memory (filled with zeros).

**Syntax**: `RMB count`
**Example**:
```assembly
BUFFER: RMB 100         ; Reserve 100 bytes
```

---

## Programming Examples

### Example 1: Simple Addition
```assembly
        ORG     $0100       ; Start at address $0100

        LDA     #$05        ; Load 5 into accumulator
        ADD     #$03        ; Add 3 to accumulator
        STA     RESULT      ; Store result in memory

        SWI                 ; Halt program

RESULT: FCB     $00         ; Reserve byte for result
        END
```

### Example 2: Loop Counter
```assembly
        ORG     $0200

START:  LDA     #$0A        ; Load counter with 10
        STA     COUNTER     ; Store counter

LOOP:   LDA     COUNTER     ; Load current counter
        SUB     #$01        ; Subtract 1
        STA     COUNTER     ; Store new counter
        BNE     LOOP        ; Branch if not zero

        SWI                 ; Halt when done

COUNTER: FCB    $00         ; Counter variable
        END
```

### Example 3: Subroutine Call
```assembly
        ORG     $0300

MAIN:   LDA     #$FF        ; Load test value
        JSR     DOUBLE      ; Call subroutine
        STA     RESULT      ; Store doubled value
        SWI                 ; Halt

; Subroutine to double accumulator value
DOUBLE: ASL                 ; Shift left (multiply by 2)
        RTS                 ; Return to caller

RESULT: FCB     $00         ; Result storage
        END
```

### Example 4: Table Lookup
```assembly
        ORG     $0400

        LDX     #TABLE      ; Point to table
        LDA     #$02        ; Index value
        LDA     $00,X       ; Load from table[A]
        STA     RESULT      ; Store result
        SWI

TABLE:  FCB     $10,$20,$30,$40  ; Lookup table
RESULT: FCB     $00
        END
```

This instruction set reference provides comprehensive coverage of the Motorola 6800's capabilities as implemented in the assembler. Each instruction includes all necessary information for programming and understanding the processor's operation.
