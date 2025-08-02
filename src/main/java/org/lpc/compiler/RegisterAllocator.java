package org.lpc.compiler;

import org.lpc.cpu.RegisterInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RegisterAllocator {
    // Available general-purpose registers (excluding t9/r31)
    private static final String[] AVAILABLE_REGS = {
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8",       // temps, t9 excluded
            "a0", "a1", "a2", "a3", "a4", "a5", "a6",                   // caller saved
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9"  // callee saved
    };

    private final Deque<String> freeRegisters = new ArrayDeque<>();
    private final Map<String, String> varToReg = new HashMap<>();       // Variable -> register mapping
    private final Map<String, Integer> regUseCount = new HashMap<>();   // Track register usage

    public RegisterAllocator() {
        // Initialize with all available registers
        for (String reg : AVAILABLE_REGS) {
            freeRegisters.push(reg);
            regUseCount.put(reg, 0);
        }
    }

    /**
     * Allocate a register for a variable
     */
    public String allocateReg(String varName) {
        if (varToReg.containsKey(varName)) {
            return varToReg.get(varName);
        }

        if (freeRegisters.isEmpty()) {
            throw new RuntimeException("Register pressure too high - no free registers");
        }

        String reg = freeRegisters.pop();
        varToReg.put(varName, reg);
        regUseCount.put(reg, regUseCount.get(reg) + 1);
        return reg;
    }

    /**
     * Free a register when a variable is no longer needed
     */
    public void freeReg(String varName) {
        String reg = varToReg.get(varName);
        if (reg != null) {
            int count = regUseCount.get(reg) - 1;
            regUseCount.put(reg, count);

            if (count == 0) {
                freeRegisters.push(reg);
            }
            varToReg.remove(varName);
        }
    }

    /**
     * Get the register currently assigned to a variable
     */
    public String getReg(String varName) {
        return varToReg.get(varName);
    }

    /**
     * Temporarily allocate a register (for intermediate calculations)
     */
    public String allocateTemp() {
        // Prefer t0-t8 first for temporaries
        for (int i = 22; i <= 30; i++) { // t0-t8 (r22-r30)
            String reg = RegisterInfo.REG_NAMES[i];
            if (freeRegisters.contains(reg)) {
                freeRegisters.remove(reg);
                regUseCount.put(reg, 1);
                return reg;
            }
        }

        // Fall back to other registers if needed
        return allocateReg("__temp" + System.nanoTime());
    }

    /**
     * Free a temporary register
     */
    public void freeTemp(String reg) {
        Integer count = regUseCount.get(reg);
        if (count != null && count > 0) {
            regUseCount.put(reg, count - 1);
            if (count - 1 == 0) {
                freeRegisters.push(reg);
            }
        }
    }
}