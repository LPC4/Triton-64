package org.lpc.cpu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lpc.cpu.Cpu.REGISTER_COUNT;

/**
 * Provides information about the CPU registers, including their names and aliases.
 * This class is immutable and contains a static map of register aliases to their indices.
 * Don't use t9 as it gets clobbered by assembler.
 */
public final class RegisterInfo {
    public static final Map<String, Integer> REG_ALIAS;
    public static final String[] REG_NAMES = new String[REGISTER_COUNT];

    private enum Register {
        RA(0, "r0"),
        SP(1, "r1"),
        FP(2, "r2"),
        GP(3, "r3"),
        HP(4, "r4"),

        S0(5, "r5"), S1(6, "r6"), S2(7, "r7"), S3(8, "r8"), S4(9, "r9"),
        S5(10, "r10"), S6(11, "r11"), S7(12, "r12"), S8(13, "r13"), S9(14, "r14"),

        A0(15, "r15"), A1(16, "r16"), A2(17, "r17"), A3(18, "r18"),
        A4(19, "r19"), A5(20, "r20"), A6(21, "r21"),

        T0(22, "r22"), T1(23, "r23"), T2(24, "r24"), T3(25, "r25"), T4(26, "r26"),
        T5(27, "r27"), T6(28, "r28"), T7(29, "r29"), T8(30, "r30"), T9(31, "r31");

        final int index;
        final String numericAlias;

        Register(int index, String numericAlias) {
            this.index = index;
            this.numericAlias = numericAlias;
        }
    }

    static {
        Map<String, Integer> aliasMap = new HashMap<>();

        for (Register reg : Register.values()) {
            String name = reg.name().toLowerCase();
            aliasMap.put(name, reg.index);
            aliasMap.put(reg.numericAlias, reg.index);
            REG_NAMES[reg.index] = name;
        }

        REG_ALIAS = Collections.unmodifiableMap(aliasMap);
    }

    private RegisterInfo() {}
}
