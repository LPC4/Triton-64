package org.lpc.cpu;

public class InstructionSet {
    /*—— Instruction Set Architecture —————————*/

    // Special operations
    public static final int OP_NOP    = 0x00; // No operation, does nothing
    public static final int OP_HLT   = 0x01; // Stops CPU execution

    // 2-register operations (dest = op(src))
    public static final int OP_MOV    = 0x10; // Copy src to dest
    public static final int OP_NOT    = 0x11; // Bitwise NOT (one's complement) of src to dest
    public static final int OP_NEG    = 0x12; // Arithmetic negation (two's complement) of src to dest

    // 3-register operations (dest = src1 op src2)
    public static final int OP_ADD    = 0x20; // Integer addition
    public static final int OP_SUB    = 0x21; // Integer subtraction
    public static final int OP_MUL    = 0x22; // Integer multiplication
    public static final int OP_DIV    = 0x23; // Integer division (quotient)
    public static final int OP_AND    = 0x24; // Bitwise AND
    public static final int OP_OR     = 0x25; // Bitwise inclusive OR
    public static final int OP_XOR    = 0x26; // Bitwise exclusive OR
    public static final int OP_SHL    = 0x27; // Logical shift left
    public static final int OP_SHR    = 0x28; // Logical shift right (unsigned)
    public static final int OP_SAR    = 0x29; // Arithmetic shift right (signed)

    // Control flow operations (jump to dest based on src)
    public static final int OP_JMP    = 0x30; // Unconditional jump to dest
    public static final int OP_JZ     = 0x31; // Jump to dest if src is zero
    public static final int OP_JNZ    = 0x32; // Jump to dest if src is not zero

    // Memory operations
    public static final int OP_LD     = 0x40; // Load from memory: dest = mem[src]
    public static final int OP_ST     = 0x41; // Store to memory: mem[dest] = src

    // Immediate operation
    public static final int OP_LDI    = 0x50; // Load immediate value into dest (10-bit signed)


    /*—— Instruction Format —————————*/

    public static final int OPCODE_BITS = 7;            // Bits for opcode
    public static final int DEST_BITS = 5;              // Bits for destination register
    public static final int SRC_BITS = 5;               // Bits for source register 1
    public static final int SRC2_BITS = 5;              // Bits for source register 2
    public static final int IMM_BITS = 10;              // Bits for immediate (-512 to 511)


    /*—— Instruction Encoding —————————*/

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
        return (imm ^ signBit) - signBit; // Sign-extend 10-bit immediate
    }

    public static int encodeInstruction(int opcode, int dest, int src, int src2, int imm) {
        imm &= (1 << IMM_BITS) - 1; // Mask to 10 bits
        return (opcode & ((1 << OPCODE_BITS) - 1)) |
                ((dest & ((1 << DEST_BITS) - 1)) << OPCODE_BITS) |
                ((src & ((1 << SRC_BITS) - 1)) << (OPCODE_BITS + DEST_BITS)) |
                ((src2 & ((1 << SRC2_BITS) - 1)) << (OPCODE_BITS + DEST_BITS + SRC_BITS)) |
                (imm << (OPCODE_BITS + DEST_BITS + SRC_BITS + SRC2_BITS));
    }
}