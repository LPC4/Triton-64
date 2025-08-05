package org.lpc.compiler.codegen;

import java.util.*;

/**
 * Manages register allocation and tracking.
 * Handles temporary register allocation and provides debugging information.
 */
public class RegisterManager {
    private final CodeGenContext ctx;
    private final RegisterAllocator allocator;

    // Debug tracking
    private final Map<String, String> registerUsage = new HashMap<>();
    private final Set<String> pinnedRegisters = new HashSet<>();

    public RegisterManager(CodeGenContext ctx) {
        this.ctx = ctx;
        this.allocator = new RegisterAllocator();
    }

    /**
     * Allocate a temporary register
     */
    public String allocateRegister() {
        return allocateRegister("temp");
    }

    /**
     * Allocate a temporary register with a usage description for debugging
     */
    public String allocateRegister(String usage) {
        String reg = allocator.allocateTemp();
        registerUsage.put(reg, usage);
        return reg;
    }

    /**
     * Free a previously allocated register
     */
    public void freeRegister(String register) {
        if (pinnedRegisters.contains(register)) {
            throw new IllegalStateException("Cannot free pinned register: " + register);
        }

        registerUsage.remove(register);
        allocator.freeTemp(register);
    }

    /**
     * Pin a register to prevent it from being freed accidentally
     */
    public void pinRegister(String register, String reason) {
        pinnedRegisters.add(register);
        registerUsage.put(register, "PINNED: " + reason);
    }

    /**
     * Unpin a previously pinned register
     */
    public void unpinRegister(String register) {
        pinnedRegisters.remove(register);
        registerUsage.remove(register);
    }

    /**
     * Get all currently allocated registers
     */
    public Set<String> getAllocatedRegisters() {
        return allocator.getAllocatedTemps();
    }

    /**
     * Get live temporary registers (for save/restore operations)
     */
    public Set<String> getLiveTemporaries() {
        return allocator.getLiveTemporaries();
    }

    /**
     * Reset the register allocator (typically at function boundaries)
     */
    public void reset() {
        if (!pinnedRegisters.isEmpty()) {
            throw new IllegalStateException("Cannot reset with pinned registers: " + pinnedRegisters);
        }

        registerUsage.clear();
        allocator.reset();
    }

    /**
     * Check if a register is currently allocated
     */
    public boolean isAllocated(String register) {
        return getAllocatedRegisters().contains(register);
    }

    /**
     * Get debug information about current register usage
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Register Usage:\n");

        for (Map.Entry<String, String> entry : registerUsage.entrySet()) {
            sb.append(String.format("  %s: %s\n", entry.getKey(), entry.getValue()));
        }

        sb.append("Free registers: ").append(allocator.getFreeRegisters()).append("\n");
        sb.append("Pinned registers: ").append(pinnedRegisters).append("\n");

        return sb.toString();
    }

    /**
     * Validate that all registers are properly freed (for debugging)
     */
    public void validateAllFreed() {
        Set<String> allocated = getAllocatedRegisters();
        if (!allocated.isEmpty()) {
            throw new IllegalStateException("Registers still allocated: " + allocated +
                    "\nUsage: " + registerUsage);
        }
    }

    /**
     * Register allocator implementation
     */
    private static class RegisterAllocator {
        private final Queue<String> freeRegisters = new LinkedList<>();
        private final Set<String> allocatedRegisters = new HashSet<>();

        public RegisterAllocator() {
            reset();
        }

        public void reset() {
            freeRegisters.clear();
            allocatedRegisters.clear();

            // Add temporary registers t0-t8 (t9 reserved for assembler)
            for (int i = 0; i <= 8; i++) {
                freeRegisters.add("t" + i);
            }
        }

        public String allocateTemp() {
            if (freeRegisters.isEmpty()) {
                throw new RegisterAllocationException(
                        "No free temporary registers available. Allocated: " + allocatedRegisters
                );
            }

            String reg = freeRegisters.poll();
            allocatedRegisters.add(reg);
            return reg;
        }

        public void freeTemp(String register) {
            if (allocatedRegisters.remove(register)) {
                // Insert in order to maintain consistent allocation patterns
                if (freeRegisters.isEmpty()) {
                    freeRegisters.add(register);
                } else {
                    // Simple insertion sort for small register set
                    List<String> temp = new ArrayList<>(freeRegisters);
                    temp.add(register);
                    temp.sort((a, b) -> {
                        int aNum = Integer.parseInt(a.substring(1));
                        int bNum = Integer.parseInt(b.substring(1));
                        return Integer.compare(aNum, bNum);
                    });
                    freeRegisters.clear();
                    freeRegisters.addAll(temp);
                }
            }
        }

        public Set<String> getAllocatedTemps() {
            return new HashSet<>(allocatedRegisters);
        }

        public Set<String> getLiveTemporaries() {
            return getAllocatedTemps();
        }

        public Queue<String> getFreeRegisters() {
            return new LinkedList<>(freeRegisters);
        }
    }

    public static class RegisterAllocationException extends RuntimeException {
        public RegisterAllocationException(String message) {
            super(message);
        }
    }
}