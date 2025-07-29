package org.lpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lpc.cpu.Cpu.REGISTER_COUNT;

public class Globals {
    /*—— Register Aliases —————————*/

    public static final Map<String, Integer> REG_ALIAS;
    public static final String[] REG_NAMES = new String[REGISTER_COUNT];
    public static final Map<Integer, String> OPCODE_NAMES;

    static {
        // Define register roles: {primary alias, numeric alias, index}
        String[][] registerDefinitions = {
                {"ra", "r0", "0"}, // Return address (link register)
                {"sp", "r1", "1"}, // Stack pointer
                {"hp", "r2", "2"}, // Heap pointer
                {"gp", "r3", "3"}, // Global pointer
                {"tp", "r4", "4"}, // Thread pointer
                // Saved registers (callee-saved, s0-s9, r5-r14)
                {"s0", "r5", "5"}, {"s1", "r6", "6"}, {"s2", "r7", "7"}, {"s3", "r8", "8"}, {"s4", "r9", "9"},
                {"s5", "r10", "10"}, {"s6", "r11", "11"}, {"s7", "r12", "12"}, {"s8", "r13", "13"}, {"s9", "r14", "14"},
                // Argument registers (caller-saved, a0-a6, r15-r21)
                {"a0", "r15", "15"}, {"a1", "r16", "16"}, {"a2", "r17", "17"}, {"a3", "r18", "18"}, {"a4", "r19", "19"},
                {"a5", "r20", "20"}, {"a6", "r21", "21"},
                // Temporary registers (caller-saved, t0-t9, r22-r31)
                // t5-9 are used by the assembler for temporary values
                {"t0", "r22", "22"}, {"t1", "r23", "23"}, {"t2", "r24", "24"}, {"t3", "r25", "25"}, {"t4", "r26", "26"},
                {"t5", "r27", "27"}, {"t6", "r28", "28"}, {"t7", "r29", "29"}, {"t8", "r30", "30"}, {"t9", "r31", "31"}
        };

        Map<String, Integer> aliasMap = new HashMap<>();
        for (int i = 0; i < REGISTER_COUNT; i++) {
            // Populate REG_ALIAS with primary and numeric aliases
            aliasMap.put(registerDefinitions[i][0], Integer.parseInt(registerDefinitions[i][2]));
            aliasMap.put(registerDefinitions[i][1], Integer.parseInt(registerDefinitions[i][2]));
            // Populate REG_NAMES with primary alias
            REG_NAMES[i] = registerDefinitions[i][0];
        }

        REG_ALIAS = Collections.unmodifiableMap(aliasMap);

        OPCODE_NAMES = new HashMap<>(){
            {
                put(0x00, "NOP");
                put(0x01, "HLT");
                put(0x10, "MOV");
                put(0x11, "NOT");
                put(0x12, "NEG");
                put(0x20, "ADD");
                put(0x21, "SUB");
                put(0x22, "MUL");
                put(0x23, "DIV");
                put(0x24, "AND");
                put(0x25, "OR");
                put(0x26, "XOR");
                put(0x27, "SHL");
                put(0x28, "SHR");
                put(0x29, "SAR");
                put(0x30, "JMP");
                put(0x31, "JZ");
                put(0x32, "JNZ");
                put(0x40, "LD");
                put(0x41, "ST");
                put(0x50, "LDI");
            }
        };
    }
}
