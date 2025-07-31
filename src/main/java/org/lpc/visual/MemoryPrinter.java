package org.lpc.visual;

import org.lpc.memory.Memory;
import org.lpc.cpu.InstructionSet;

import static org.lpc.cpu.RegisterInfo.REG_NAMES;
import static org.lpc.memory.MemoryMap.*;

/**
 * Utility class for printing formatted memory dumps with assembly instructions.
 * Displays memory in pages of 40 instructions (160 bytes) starting from RAM.
 */
public class MemoryPrinter {
    private static final int INSTRUCTIONS_PER_PAGE = 40;
    private static final int BYTES_PER_INSTRUCTION = 4;
    private static final int BYTES_PER_PAGE = INSTRUCTIONS_PER_PAGE * BYTES_PER_INSTRUCTION;
    private static final int INSTRUCTIONS_PER_ROW = 4;
    private static final int BYTES_PER_ROW = INSTRUCTIONS_PER_ROW * BYTES_PER_INSTRUCTION;

    private final Memory memory;

    public MemoryPrinter(Memory memory) {
        this.memory = memory;
    }

    /**
     * Prints a memory page starting from RAM base address
     * @param pageNumber Page number (0-based)
     */
    public void printPage(int pageNumber) {
        long startAddress = RAM_BASE + (pageNumber * BYTES_PER_PAGE);
        printMemoryRange(startAddress, BYTES_PER_PAGE, "RAM Page " + pageNumber);
    }

    /**
     * Prints multiple consecutive pages
     * @param startPage Starting page number
     * @param pageCount Number of pages to print
     */
    public void printPages(int startPage, int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            printPage(startPage + i);
            if (i < pageCount - 1) {
                System.out.println(); // Blank line between pages
            }
        }
    }

    /**
     * Prints memory starting from RAM base with specified instruction count
     * @param instructionCount Number of instructions to display
     */
    public void printInstructions(int instructionCount) {
        int byteCount = instructionCount * BYTES_PER_INSTRUCTION;
        printMemoryRange(RAM_BASE, byteCount,
                "RAM Instructions (0-" + (instructionCount - 1) + ")");
    }

    /**
     * Prints a specific memory range with formatting
     * @param startAddress Starting memory address
     * @param byteCount Number of bytes to display
     * @param title Title for the memory dump
     */
    public void printMemoryRange(long startAddress, int byteCount, String title) {
        // Validate address range
        if (startAddress < 0 || startAddress + byteCount > getTotalMemorySize()) {
            throw new IllegalArgumentException("Memory range out of bounds");
        }

        System.out.println("=".repeat(80));
        System.out.println(title);
        System.out.println("Address Range: 0x" + Long.toHexString(startAddress) +
                " - 0x" + Long.toHexString(startAddress + byteCount - 1));
        System.out.println("=".repeat(80));

        // Print header
        System.out.printf("%-10s", "Address");
        for (int i = 0; i < INSTRUCTIONS_PER_ROW; i++) {
            System.out.printf(" %8s", "Instr" + i);
        }
        System.out.printf("  %-16s", "ASCII");
        System.out.println();
        System.out.println("-".repeat(80));

        // Print memory rows (hex and ASCII only)
        for (int offset = 0; offset < byteCount; offset += BYTES_PER_ROW) {
            long rowAddress = startAddress + offset;
            int remainingBytes = Math.min(BYTES_PER_ROW, byteCount - offset);

            printMemoryRow(rowAddress, remainingBytes);
        }

        System.out.println("-".repeat(80));
        System.out.println("DISASSEMBLY:");
        System.out.println("-".repeat(80));

        // Print disassembly section
        for (int offset = 0; offset < byteCount; offset += BYTES_PER_INSTRUCTION) {
            long instrAddress = startAddress + offset;
            if (offset < byteCount) {
                try {
                    int instruction = memory.readInt(instrAddress);
                    String assembly = disassemble(instruction);
                    System.out.printf("0x%08X:  %-40s  ; 0x%08X%n",
                            instrAddress, assembly, instruction);
                } catch (Exception e) {
                    System.out.printf("0x%08X:  %-40s  ; ????????%n",
                            instrAddress, "???");
                }
            }
        }

        System.out.println("=".repeat(80));
    }

    /**
     * Prints a single row of memory (4 instructions = 16 bytes)
     * @param address Starting address of the row
     * @param byteCount Number of bytes in this row (may be less than 16 for last row)
     */
    private void printMemoryRow(long address, int byteCount) {
        // Print address
        System.out.printf("0x%08X", address);

        // Read and print instructions
        StringBuilder asciiBuilder = new StringBuilder();
        StringBuilder assemblyBuilder = new StringBuilder();

        for (int i = 0; i < BYTES_PER_ROW; i += BYTES_PER_INSTRUCTION) {
            if (i < byteCount) {
                // Read 4-byte instruction
                try {
                    int instruction = memory.readInt(address + i);
                    System.out.printf(" %08X", instruction);

                    // Build ASCII representation
                    for (int j = 0; j < BYTES_PER_INSTRUCTION && (i + j) < byteCount; j++) {
                        byte b = memory.readByte(address + i + j);
                        char c = (b >= 32 && b <= 126) ? (char) b : '.';
                        asciiBuilder.append(c);
                    }

                    // Build assembly representation
                    String assembly = disassemble(instruction);
                    if (assemblyBuilder.length() > 0) {
                        assemblyBuilder.append(" | ");
                    }
                    assemblyBuilder.append(assembly);

                } catch (Exception e) {
                    System.out.printf(" %8s", "????????");
                    asciiBuilder.append("????");
                    if (assemblyBuilder.length() > 0) {
                        assemblyBuilder.append(" | ");
                    }
                    assemblyBuilder.append("???");
                }
            } else {
                // Padding for incomplete rows
                System.out.printf(" %8s", "");
            }
        }

        // Print ASCII representation
        System.out.printf("  %-16s", asciiBuilder.toString());

        // Print assembly representation
        System.out.printf("  %-60s", assemblyBuilder.toString());
        System.out.println();
    }

    /**
     * Disassembles a 32-bit instruction into assembly format
     * @param instruction The 32-bit instruction to disassemble
     * @return String representation of the assembly instruction
     */
    private String disassemble(int instruction) {
        int opcode = InstructionSet.getOpcode(instruction);
        int dest = InstructionSet.getDest(instruction);
        int src = InstructionSet.getSrc(instruction);
        int src2 = InstructionSet.getSrc2(instruction);
        int imm = InstructionSet.getImmediate(instruction);

        String destReg = getRegisterName(dest);
        String srcReg = getRegisterName(src);
        String src2Reg = getRegisterName(src2);

        switch (opcode) {
            case InstructionSet.OP_NOP:
                return "nop";
            case InstructionSet.OP_HLT:
                return "hlt";

            // 2-register operations
            case InstructionSet.OP_MOV:
                return String.format("mov %s, %s", destReg, srcReg);
            case InstructionSet.OP_NOT:
                return String.format("not %s, %s", destReg, srcReg);
            case InstructionSet.OP_NEG:
                return String.format("neg %s, %s", destReg, srcReg);

            // 3-register operations
            case InstructionSet.OP_ADD:
                return String.format("add %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_SUB:
                return String.format("sub %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_MUL:
                return String.format("mul %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_DIV:
                return String.format("div %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_AND:
                return String.format("and %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_OR:
                return String.format("or %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_XOR:
                return String.format("xor %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_SHL:
                return String.format("shl %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_SHR:
                return String.format("shr %s, %s, %s", destReg, srcReg, src2Reg);
            case InstructionSet.OP_SAR:
                return String.format("sar %s, %s, %s", destReg, srcReg, src2Reg);

            // Control flow operations
            case InstructionSet.OP_JMP:
                return String.format("jmp %s", destReg);
            case InstructionSet.OP_JZ:
                return String.format("jz %s, %s", destReg, srcReg);
            case InstructionSet.OP_JNZ:
                return String.format("jnz %s, %s", destReg, srcReg);

            // Memory operations
            case InstructionSet.OP_LD:
                return String.format("ld %s, [%s]", destReg, srcReg);
            case InstructionSet.OP_ST:
                return String.format("st [%s], %s", destReg, srcReg);

            // Immediate operation
            case InstructionSet.OP_LDI:
                return String.format("ldi %s, #%d", destReg, imm);

            default:
                return String.format("??? 0x%02X", opcode);
        }
    }

    /**
     * Gets the register name for a given register index
     * @param regIndex Register index (0-31)
     * @return Register name (e.g., "ra", "sp", "t0")
     */
    private String getRegisterName(int regIndex) {
        if (regIndex >= 0 && regIndex < REG_NAMES.length) {
            return REG_NAMES[regIndex];
        }
        return "r" + regIndex; // Fallback to numeric name
    }

    /**
     * Prints memory regions overview
     */
    public void printMemoryLayout() {
        System.out.println("=".repeat(100));
        System.out.println("MEMORY LAYOUT");
        System.out.println("=".repeat(100));
        System.out.printf("ROM:   0x%08X - 0x%08X (%d bytes)%n",
                ROM_BASE, ROM_BASE + ROM_SIZE - 1, ROM_SIZE);
        System.out.printf("RAM:   0x%08X - 0x%08X (%d bytes)%n",
                RAM_BASE, RAM_BASE + RAM_SIZE - 1, RAM_SIZE);
        System.out.printf("MMIO:  0x%08X - 0x%08X (%d bytes)%n",
                MMIO_BASE, MMIO_BASE + MMIO_SIZE - 1, MMIO_SIZE);
        System.out.printf("FB:    0x%08X - 0x%08X (%d bytes)%n",
                FB_BASE, FB_BASE + FB_SIZE - 1, FB_SIZE);
        System.out.println("=".repeat(100));
    }

    /**
     * Searches for a specific value in RAM and prints surrounding context
     * @param searchValue Value to search for
     * @param contextInstructions Number of instructions before/after to show
     */
    public void searchAndPrint(int searchValue, int contextInstructions) {
        long ramStart = RAM_BASE;
        long ramEnd = RAM_BASE + RAM_SIZE - 4;

        System.out.println("Searching RAM for value: 0x" + Integer.toHexString(searchValue));
        boolean found = false;

        for (long addr = ramStart; addr <= ramEnd; addr += 4) {
            try {
                if (memory.readInt(addr) == searchValue) {
                    found = true;
                    System.out.println("Found at address: 0x" + Long.toHexString(addr));

                    // Print context around the found value
                    long contextStart = Math.max(ramStart, addr - (contextInstructions * 4));
                    long contextEnd = Math.min(ramEnd, addr + (contextInstructions * 4));
                    int contextBytes = (int)(contextEnd - contextStart + 4);

                    printMemoryRange(contextStart, contextBytes,
                            "Context around 0x" + Long.toHexString(addr));
                }
            } catch (Exception e) {
                // Skip invalid addresses
            }
        }

        if (!found) {
            System.out.println("Value not found in RAM");
        }
    }

    private long getTotalMemorySize() {
        return ROM_SIZE + RAM_SIZE + MMIO_SIZE + FB_SIZE;
    }

    // Convenience methods for common operations
    public void printFirstPage() {
        printPage(0);
    }

    public void printFirst40Instructions() {
        printInstructions(40);
    }

    public int getInstructionsPerPage() {
        return INSTRUCTIONS_PER_PAGE;
    }

    public int getMaxPageNumber() {
        return (int) (RAM_SIZE / BYTES_PER_PAGE) - 1;
    }
}