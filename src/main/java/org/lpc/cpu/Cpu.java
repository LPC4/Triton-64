package org.lpc.cpu;

import lombok.Getter;
import org.lpc.memory.Memory;
import org.lpc.rom.ROMData;

import java.util.Arrays;

import static org.lpc.memory.MemoryMap.ROM_SIZE;

@Getter
public class Cpu {
    public static final int REGISTER_COUNT = 32;
    private final long[] registers = new long[REGISTER_COUNT];
    private long programCounter = 0;
    private boolean isHalted = false;

    private final Memory memory;

    public Cpu(Memory memory) {
        this.memory = memory;
        initROM();
    }

    public void run() {
        int nopCount = 0, maxNopCount = 16;

        while (!isHalted && nopCount < maxNopCount) {
            int raw = memory.readInt(programCounter);
            programCounter += 4;

            int opcode = InstructionSet.getOpcode(raw);
            int dest = InstructionSet.getDest(raw);
            int src1 = InstructionSet.getSrc(raw);
            int src2 = InstructionSet.getSrc2(raw);
            int imm = InstructionSet.getImmediate(raw);

            if (opcode == InstructionSet.OP_NOP) {
                nopCount++;
            } else {
                nopCount = 0;
            }

            execute(opcode, dest, src1, src2, imm);
        }
    }

    private void execute(int opcode, int dest, int src1, int src2, int imm) {
        switch (opcode) {
            case InstructionSet.OP_NOP -> {}
            case InstructionSet.OP_HLT -> isHalted = true;
            case InstructionSet.OP_MOV -> set(dest, get(src1));
            case InstructionSet.OP_NOT -> set(dest, ~get(src1));
            case InstructionSet.OP_NEG -> set(dest, -get(src1));
            case InstructionSet.OP_ADD -> set(dest, get(src1) + get(src2));
            case InstructionSet.OP_SUB -> set(dest, get(src1) - get(src2));
            case InstructionSet.OP_MUL -> set(dest, get(src1) * get(src2));
            case InstructionSet.OP_DIV -> {
                long divisor = get(src2);
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                set(dest, get(src1) / divisor);
            }
            case InstructionSet.OP_AND -> set(dest, get(src1) & get(src2));
            case InstructionSet.OP_OR  -> set(dest, get(src1) | get(src2));
            case InstructionSet.OP_XOR -> set(dest, get(src1) ^ get(src2));
            case InstructionSet.OP_SHL -> set(dest, get(src1) << get(src2));
            case InstructionSet.OP_SHR -> set(dest, get(src1) >>> get(src2));
            case InstructionSet.OP_SAR -> set(dest, get(src1) >> get(src2));
            // PC already incremented by 4 so can use it directly
            // fuckass bug
            case InstructionSet.OP_JMP -> programCounter = get(dest);
            case InstructionSet.OP_JZ  -> { if (get(src1) == 0) programCounter = get(dest); }
            case InstructionSet.OP_JNZ -> { if (get(src1) != 0) programCounter = get(dest); }
            case InstructionSet.OP_JPP -> { if (get(src1) > 0) programCounter = get(dest); }
            case InstructionSet.OP_JPN -> { if (get(src1) < 0) programCounter = get(dest); }
            case InstructionSet.OP_JAL -> { set(src1, programCounter); programCounter = get(dest);}
            case InstructionSet.OP_LD  -> set(dest, memory.readLong(get(src1)));
            case InstructionSet.OP_ST  -> memory.writeLong(get(dest), get(src1));
            case InstructionSet.OP_LDI -> set(dest, imm);
            default -> throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }
    }

    private void initROM() {
        byte[] rom = ROMData.ROM;
        if (rom.length > ROM_SIZE) throw new IllegalArgumentException("ROM too large");
        memory.initializeROM(rom);
    }

    public long getRegister(int index) {
        validateRegister(index);
        return registers[index];
    }

    private long get(int reg) {
        validateRegister(reg);
        return registers[reg];
    }

    private void set(int reg, long value) {
        validateRegister(reg);
        registers[reg] = value;
    }

    private void validateRegister(int index) {
        if (index < 0 || index >= REGISTER_COUNT) {
            throw new IllegalArgumentException("Invalid register index: " + index);
        }
    }

    public void printRegisters() {
        System.out.println("=".repeat(100));
        System.out.println("CPU REGISTERS");
        System.out.println("=".repeat(100));

        for (int i = 0; i < REGISTER_COUNT / 2; i++) {
            int j = i + (REGISTER_COUNT / 2);
            System.out.printf(
                    "%-3s: %20d (0x%016X)    %-3s: %20d (0x%016X)%n",
                    RegisterInfo.REG_NAMES[i], registers[i], registers[i],
                    RegisterInfo.REG_NAMES[j], registers[j], registers[j]
            );
        }

        System.out.println("-".repeat(100));
        System.out.printf("Program Counter: 0x%016X (%d)%n", programCounter, programCounter);
        System.out.printf("CPU Status: %s%n", isHalted ? "HALTED" : "RUNNING");
        System.out.println("=".repeat(100));
    }
}
