package org.lpc.cpu;

/**
 * Defines the instruction set architecture for the LPC CPU.
 * This class contains constants for opcodes, instruction formats,
 * and methods for encoding and decoding instructions.
 *
 * Syntax for real and pseudo-instructions is given as comments next to each opcode.
 */
public final class InstructionSet {

    /*—— Real Instructions —————————*/

    // Special operations
    public static final int OP_NOP = 0x00; // NOP                            ; No operation
    public static final int OP_HLT = 0x01; // HLT                            ; Halt execution

    // 2-register operations (dest = op(src))
    public static final int OP_MOV = 0x10; // MOV rdest, rsrc                ; rdest = rsrc
    public static final int OP_NOT = 0x11; // NOT rdest, rsrc                ; rdest = ~rsrc
    public static final int OP_NEG = 0x12; // NEG rdest, rsrc                ; rdest = -rsrc

    // 3-register operations (dest = src1 op src2)
    public static final int OP_ADD = 0x20; // ADD rdest, rsrc1, rsrc2        ; rdest = rsrc1 + rsrc2
    public static final int OP_SUB = 0x21; // SUB rdest, rsrc1, rsrc2        ; rdest = rsrc1 - rsrc2
    public static final int OP_MUL = 0x22; // MUL rdest, rsrc1, rsrc2        ; rdest = rsrc1 * rsrc2
    public static final int OP_DIV = 0x23; // DIV rdest, rsrc1, rsrc2        ; rdest = rsrc1 / rsrc2
    public static final int OP_AND = 0x24; // AND rdest, rsrc1, rsrc2        ; rdest = rsrc1 & rsrc2
    public static final int OP_OR  = 0x25; // OR  rdest, rsrc1, rsrc2        ; rdest = rsrc1 | rsrc2
    public static final int OP_XOR = 0x26; // XOR rdest, rsrc1, rsrc2        ; rdest = rsrc1 ^ rsrc2
    public static final int OP_SHL = 0x27; // SHL rdest, rsrc1, rsrc2        ; rdest = rsrc1 << rsrc2
    public static final int OP_SHR = 0x28; // SHR rdest, rsrc1, rsrc2        ; rdest = rsrc1 >>> rsrc2
    public static final int OP_SAR = 0x29; // SAR rdest, rsrc1, rsrc2        ; rdest = rsrc1 >> rsrc2

    // Control flow operations
    public static final int OP_JMP = 0x30; // JMP rdest                      ; Jump to address in rdest
    public static final int OP_JZ  = 0x31; // JZ  rdest, rsrc                ; Jump to rdest if rsrc == 0
    public static final int OP_JNZ = 0x32; // JNZ rdest, rsrc                ; Jump to rdest if rsrc != 0
    public static final int OP_JPP = 0x33; // JPP rdest, rsrc                ; Jump to rdest if rsrc > 0
    public static final int OP_JPN = 0x34; // JNN rdest, rsrc                ; Jump to rdest if rsrc < 0

    // Memory operations
    public static final int OP_LD  = 0x40; // LD rdest, rsrc                 ; rdest = mem[rsrc]
    public static final int OP_ST  = 0x41; // ST rdest, rsrc                 ; mem[rdest] = rsrc

    // Immediate operation (sign-extended 10-bit immediate)
    public static final int OP_LDI = 0x50; // LDI rdest, imm10               ; rdest = imm (-512 to 511)

    // Function calls
    public static final int OP_LNK = 0x60; // LNK rdest                      ; Link to rdest (save PC)


    /*—— Pseudo-Instructions (Assembler Expansions) ————————— */
    //  These are not encoded directly, but expanded into basic instructions.

    // label:                        ; Label definition, used for jumps
    // JMP label                     ; Loads label address into a register and jumps, temps are restored
    // JZ  label                     ; Loads label address into a register and jumps if zero, temps are restored
    // JNZ label                     ; Same but for non-zero

    // LDI  rdest, imm64             ; Safe load, temps are restored
    // LDIU rdest, imm64             ; Unsafe raw load clobbering temp regs (used for ROM)

    // PUSH r1, r2, ...              ; Expands to stack store sequence, t9 is clobbered
    // POP  r1, r2, ...              ; Same but for loading

    // INSTR rdest, rsrc1, imm       ; Uses temp registers for immediate operations, temps are restored

    /*—— Instruction Format —————————*/

    public static final int OPCODE_BITS = 7;
    public static final int DEST_BITS   = 5;
    public static final int SRC_BITS    = 5;
    public static final int SRC2_BITS   = 5;
    public static final int IMM_BITS    = 10; // signed [-512, 511]

    /*—— Decoding Methods —————————*/

    public static int getOpcode(int instr) {
        return instr & ((1 << OPCODE_BITS) - 1);
    }

    public static int getDest(int instr) {
        return (instr >>> OPCODE_BITS) & ((1 << DEST_BITS) - 1);
    }

    public static int getSrc(int instr) {
        return (instr >>> (OPCODE_BITS + DEST_BITS)) & ((1 << SRC_BITS) - 1);
    }

    public static int getSrc2(int instr) {
        return (instr >>> (OPCODE_BITS + DEST_BITS + SRC_BITS)) & ((1 << SRC2_BITS) - 1);
    }

    public static int getImmediate(int instr) {
        int imm = (instr >>> (OPCODE_BITS + DEST_BITS + SRC_BITS + SRC2_BITS)) & ((1 << IMM_BITS) - 1);
        int signBit = 1 << (IMM_BITS - 1);
        return (imm ^ signBit) - signBit; // Sign extend
    }

    /*—— Encoding Method —————————*/

    public static int encodeInstruction(int opcode, int dest, int src, int src2, int imm) {
        imm &= (1 << IMM_BITS) - 1;
        return (opcode & ((1 << OPCODE_BITS) - 1)) |
                ((dest & ((1 << DEST_BITS) - 1)) << OPCODE_BITS) |
                ((src & ((1 << SRC_BITS) - 1)) << (OPCODE_BITS + DEST_BITS)) |
                ((src2 & ((1 << SRC2_BITS) - 1)) << (OPCODE_BITS + DEST_BITS + SRC_BITS)) |
                (imm << (OPCODE_BITS + DEST_BITS + SRC_BITS + SRC2_BITS));
    }
}
