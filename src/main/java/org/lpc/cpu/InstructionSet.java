package org.lpc.cpu;

/**
 * Instruction Set Architecture (ISA) definition for the LPC CPU.
 *
 * Includes:
 * - Encodable instruction opcodes and formats
 * - Syntax and semantics for each instruction
 * - Description of pseudo-instructions (macro expansions)
 * - Decoding/encoding helpers for instruction packing
 *
 * Syntax format:
 *   INSTR rdest, rsrc1, rsrc2    ; For 3-register instructions
 *   INSTR rdest, rsrc            ; For 2-register instructions
 *   INSTR rdest, imm             ; For immediate loads (LDI)
 *   JMP  rdest                   ; For jump-to-register
 *   JMP  label                   ; For pseudo-instruction (loads label addr into a temp, then JMP)
 */
public final class InstructionSet {

    /*—— Real Instructions —————————*/

    // Special operations
    public static final int OP_NOP = 0x00; // NOP                            ; No operation
    public static final int OP_HLT = 0x01; // HLT                            ; Halt CPU

    // 2-register ops: rdest = op(rsrc)
    public static final int OP_MOV = 0x10; // MOV rdest, rsrc                ; Copy
    public static final int OP_NOT = 0x11; // NOT rdest, rsrc                ; Bitwise NOT
    public static final int OP_NEG = 0x12; // NEG rdest, rsrc                ; Arithmetic negation

    // 3-register ops: rdest = rsrc1 OP rsrc2
    public static final int OP_ADD = 0x20; // ADD rdest, rsrc1, rsrc2        ; Add
    public static final int OP_SUB = 0x21; // SUB rdest, rsrc1, rsrc2        ; Subtract
    public static final int OP_MUL = 0x22; // MUL rdest, rsrc1, rsrc2        ; Multiply
    public static final int OP_DIV = 0x23; // DIV rdest, rsrc1, rsrc2        ; Divide
    public static final int OP_AND = 0x24; // AND rdest, rsrc1, rsrc2        ; Bitwise AND
    public static final int OP_OR  = 0x25; // OR  rdest, rsrc1, rsrc2        ; Bitwise OR
    public static final int OP_XOR = 0x26; // XOR rdest, rsrc1, rsrc2        ; Bitwise XOR
    public static final int OP_SHL = 0x27; // SHL rdest, rsrc1, rsrc2        ; Logical shift left
    public static final int OP_SHR = 0x28; // SHR rdest, rsrc1, rsrc2        ; Logical shift right
    public static final int OP_SAR = 0x29; // SAR rdest, rsrc1, rsrc2        ; Arithmetic shift right

    // Control flow
    public static final int OP_JMP = 0x30; // JMP rdest                      ; PC = rdest
    public static final int OP_JZ  = 0x31; // JZ  rdest, rsrc                ; if (rsrc == 0) PC = rdest
    public static final int OP_JNZ = 0x32; // JNZ rdest, rsrc                ; if (rsrc != 0) PC = rdest
    public static final int OP_JPP = 0x33; // JPP rdest, rsrc                ; if (rsrc > 0)  PC = rdest
    public static final int OP_JPN = 0x34; // JPN rdest, rsrc                ; if (rsrc < 0)  PC = rdest
    public static final int OP_JAL = 0x35; // JAL rdest, rsrc                ; Jump and link (store return addr in rsrc)

    // Memory
    public static final int OP_LD  = 0x40; // LD  rdest, rsrc                ; rdest = mem[rsrc]
    public static final int OP_ST  = 0x41; // ST  rdest, rsrc                ; mem[rdest] = rsrc

    // Immediate load (sign-extended 10-bit)
    public static final int OP_LDI = 0x50; // LDI rdest, imm10               ; rdest = imm (-512 to +511)

    /*—— Pseudo-Instructions (Assembler Expansions) ————————— */
    // These are assembler conveniences, not encoded directly.
    // They may clobber t9.

    // — Labels
    // label:                           ; Defines label for jump targets

    // — Control flow (auto-loads label addr into temp, temps are preserved with stack)
    // JMP label                        ; Loads label into temp, JMP temp
    // JZ  label                        ; Loads label into temp, JZ temp, cond
    // JNZ label                        ; Loads label into temp, JNZ temp, cond

    // — Immediate loads
    // LDI  rdest, imm64                ; Safe multi-word load, preserves temps
    // LDIU rdest, imm64                ; Unsafe ROM-time load, clobbers temps (fast, compact)

    // — Stack macros
    // PUSH r1, r2, ...                 ; Decrement SP, store regs to stack, clobbers t9
    // POP  r1, r2, ...                 ; Load regs from stack, increment SP, clobbers t9

    // — Immediate ALU ops
    // INSTR rdest, rsrc1, imm          ; Expanded into temp + register operation

    /*—— Instruction Format —————————*/

    public static final int OPCODE_BITS = 7;
    public static final int DEST_BITS   = 5;
    public static final int SRC_BITS    = 5;
    public static final int SRC2_BITS   = 5;
    public static final int IMM_BITS    = 10; // Sign-extended [-512, 511]

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
        return (imm ^ signBit) - signBit; // Sign-extend
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
