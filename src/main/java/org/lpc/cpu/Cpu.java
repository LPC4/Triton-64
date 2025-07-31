package org.lpc.cpu;

import lombok.Getter;
import lombok.Setter;
import org.lpc.memory.Memory;
import org.lpc.rom.ROMData;

import java.util.Arrays;

import static org.lpc.assembler.InstructionInfo.OPCODE_NAMES;
import static org.lpc.memory.MemoryMap.ROM_SIZE;

@Getter
@Setter
public class Cpu {
    public static final int REGISTER_COUNT = 32;

    private final long[] registers;
    private long programCounter;
    private boolean isHalted;
    private final Memory memory;

    public Cpu(Memory memory) {
        this.memory = memory;
        registers = new long[REGISTER_COUNT];
        reset();

        initROM(memory);
    }

    public long getRegister(int index) {
        if (index < 0 || index >= registers.length) {
            throw new IllegalArgumentException("Invalid register index: " + index);
        }
        return registers[index];
    }

    public void setRegister(int index, long value) {
        if (index < 0 || index >= registers.length) {
            throw new IllegalArgumentException("Invalid register index: " + index);
        }
        registers[index] = value;
    }

    public void reset() {
        Arrays.fill(registers, 0);
        programCounter = 0;
        isHalted = false;
    }

    /** Execute instructions from memory until halted */
    public void run() {
        int nopCounter = 0;
        final int maxNopCount = 16; // Maximum NOP count before halting

        while (!isHalted && nopCounter < maxNopCount) {
            // Fetch 32-bit instruction from memory
            int instr = memory.readInt(programCounter);
            // Increment program counter (instructions are 4 bytes)
            programCounter += 4;
            // Decode instruction
            int opcode = InstructionSet.getOpcode(instr);
            int dest = InstructionSet.getDest(instr);
            int src = InstructionSet.getSrc(instr);
            int src2 = InstructionSet.getSrc2(instr);
            int imm = InstructionSet.getImmediate(instr);

            if (opcode == InstructionSet.OP_NOP) {
                nopCounter++;
            } else {
                nopCounter = 0; // Reset NOP counter on any other instruction
            }

            executeInstruction(opcode, dest, src, src2, imm);
        }
    }

    /** Execute a single instruction based on its opcode */
    private void executeInstruction(int opcode, int dest, int src, int src2, int imm) {
        switch (opcode) {
            case InstructionSet.OP_NOP:
                // No operation
                break;

            case InstructionSet.OP_HLT:
                isHalted = true;
                break;

            case InstructionSet.OP_MOV:
                setRegister(dest, getRegister(src));
                break;

            case InstructionSet.OP_NOT:
                setRegister(dest, ~getRegister(src));
                break;

            case InstructionSet.OP_NEG:
                setRegister(dest, -getRegister(src));
                break;

            case InstructionSet.OP_ADD:
                setRegister(dest, getRegister(src) + getRegister(src2));
                break;

            case InstructionSet.OP_SUB:
                setRegister(dest, getRegister(src) - getRegister(src2));
                break;

            case InstructionSet.OP_MUL:
                setRegister(dest, getRegister(src) * getRegister(src2));
                break;

            case InstructionSet.OP_DIV:
                long divisor = getRegister(src2);
                if (divisor == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                setRegister(dest, getRegister(src) / divisor);
                break;

            case InstructionSet.OP_AND:
                setRegister(dest, getRegister(src) & getRegister(src2));
                break;

            case InstructionSet.OP_OR:
                setRegister(dest, getRegister(src) | getRegister(src2));
                break;

            case InstructionSet.OP_XOR:
                setRegister(dest, getRegister(src) ^ getRegister(src2));
                break;

            case InstructionSet.OP_SHL:
                setRegister(dest, getRegister(src) << getRegister(src2));
                break;

            case InstructionSet.OP_SHR:
                setRegister(dest, getRegister(src) >>> getRegister(src2));
                break;

            case InstructionSet.OP_SAR:
                setRegister(dest, getRegister(src) >> getRegister(src2));
                break;

            case InstructionSet.OP_JMP:
                programCounter = getRegister(dest);
                break;

            case InstructionSet.OP_JZ:
                if (getRegister(src) == 0)
                    programCounter = getRegister(dest);
                break;

            case InstructionSet.OP_JNZ:
                if (getRegister(src) != 0)
                    programCounter = getRegister(dest);
                break;

            case InstructionSet.OP_LD:
                long addr = getRegister(src);
                long value = memory.readLong(addr);
                setRegister(dest, value);
                break;

            case InstructionSet.OP_ST:
                long stAddr = getRegister(dest);
                long stValue = getRegister(src);
                memory.writeLong(stAddr, stValue);
                break;

            case InstructionSet.OP_LDI:
                setRegister(dest, imm);
                break;

            default:
                throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }
    }

    private void initROM(Memory memory) {
        byte[] romBytes = ROMData.ROM;

        if (romBytes.length > ROM_SIZE)
            throw new IllegalArgumentException("ROM data too large");

        memory.initializeROM(romBytes);
    }

    public void printRegisters() {
        System.out.println("=".repeat(100));
        System.out.println("CPU REGISTERS");
        System.out.println("=".repeat(100));

        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 2; col++) {
                int regIndex = row + (col * 16);
                if (regIndex < registers.length) {
                    String regName = RegisterInfo.REG_NAMES[regIndex];
                    long value = registers[regIndex];
                    System.out.printf("%-3s: %20d (0x%016X)  ", regName, value, value);
                }
            }
            System.out.println();
        }

        System.out.println("-".repeat(100));
        System.out.printf("Program Counter: 0x%016X (%d)%n", programCounter, programCounter);
        System.out.printf("CPU Status: %s%n", isHalted ? "HALTED" : "RUNNING");
        System.out.println("=".repeat(100));
    }
}