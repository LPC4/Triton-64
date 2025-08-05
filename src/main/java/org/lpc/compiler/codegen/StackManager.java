package org.lpc.compiler.codegen;

import lombok.Getter;

import java.util.*;

/**
 * Manages stack operations and variable storage.
 * Handles stack frame management and variable offset calculations.
 */
public class StackManager {
    private static final int WORD_SIZE = 8; // 64-bit words

    private final CodeGenContext ctx;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final StackOperations stackOps;

    private FunctionFrame currentFrame;

    public StackManager(CodeGenContext ctx, InstructionEmitter emitter, RegisterManager registerManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackOps = new StackOperations();
    }

    // Frame management
    public void startFunctionFrame(List<String> parameters) {
        validateNoActiveFrame();
        currentFrame = new FunctionFrame(parameters);
    }

    public void endFunctionFrame() {
        validateActiveFrame();
        currentFrame = null;
    }

    // Variable management
    public int allocateVariable(String name) {
        validateActiveFrame();
        return currentFrame.allocateLocal(name);
    }

    public int getVariableOffset(String name) {
        validateActiveFrame();
        return currentFrame.getOffset(name);
    }

    // Frame information
    public int getLocalVariableCount() {
        return currentFrame != null ? currentFrame.getLocalCount() : 0;
    }

    public int getFrameSize() {
        return getLocalVariableCount();
    }

    // Stack operations
    public void storeToStack(int offset, String valueReg) {
        stackOps.storeAtOffset(offset, valueReg);
    }

    public String loadFromStack(int offset) {
        return stackOps.loadFromOffset(offset);
    }

    // Legacy compatibility
    public int getTotalFrameSize() {
        return getFrameSize();
    }

    // Validation helpers
    private void validateActiveFrame() {
        if (currentFrame == null) {
            throw new IllegalStateException("No active function frame");
        }
    }

    private void validateNoActiveFrame() {
        if (currentFrame != null) {
            throw new IllegalStateException("Cannot start new frame - frame already active");
        }
    }

    /**
     * Encapsulates stack memory operations
     */
    private class StackOperations {
        public void storeAtOffset(int offset, String valueReg) {
            String addrReg = calculateAddress(offset);
            try {
                emitter.store(addrReg, valueReg);
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }

        public String loadFromOffset(int offset) {
            String addrReg = calculateAddress(offset);
            String valueReg = registerManager.allocateRegister("stack_load_value");

            try {
                emitter.load(valueReg, addrReg);
                return valueReg;
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }

        private String calculateAddress(int offset) {
            String addrReg = registerManager.allocateRegister("stack_addr");
            emitter.loadImmediate(addrReg, offset);
            emitter.add(addrReg, "fp", addrReg);
            return addrReg;
        }
    }

    /**
     * Represents a function's stack frame with parameter and local variable management
     */
    private static class FunctionFrame {
        private final VariableMap variables;
        private final OffsetCalculator offsetCalculator;

        public FunctionFrame(List<String> parameters) {
            this.variables = new VariableMap();
            this.offsetCalculator = new OffsetCalculator();

            // Initialize parameters with positive offsets (above frame pointer)
            initializeParameters(parameters);
        }

        private void initializeParameters(List<String> parameters) {
            int paramOffset = 16; // Start after saved ra/fp
            for (String param : parameters) {
                variables.addParameter(param, paramOffset);
                paramOffset += WORD_SIZE;
            }
        }

        public int allocateLocal(String name) {
            if (variables.exists(name)) {
                return variables.getOffset(name);
            }

            if (variables.isParameter(name)) {
                throw new IllegalArgumentException("Variable " + name + " is already a parameter");
            }

            int offset = offsetCalculator.getNextLocalOffset();
            variables.addLocal(name, offset);
            return offset;
        }

        public int getOffset(String name) {
            return variables.getOffset(name);
        }

        public int getLocalCount() {
            return variables.getLocalCount();
        }
    }

    /**
     * Manages variable name to offset mappings
     */
    private static class VariableMap {
        private final Map<String, Integer> variableOffsets = new HashMap<>();
        private final Set<String> parameters = new HashSet<>();
        @Getter
        private int localCount = 0;

        public void addParameter(String name, int offset) {
            variableOffsets.put(name, offset);
            parameters.add(name);
        }

        public void addLocal(String name, int offset) {
            variableOffsets.put(name, offset);
            localCount++;
        }

        public boolean exists(String name) {
            return variableOffsets.containsKey(name);
        }

        public boolean isParameter(String name) {
            return parameters.contains(name);
        }

        public int getOffset(String name) {
            Integer offset = variableOffsets.get(name);
            if (offset == null) {
                throw new IllegalArgumentException("Variable not found: " + name);
            }
            return offset;
        }
    }

    /**
     * Calculates stack offsets for local variables
     */
    private static class OffsetCalculator {
        private int nextLocalOffset = -WORD_SIZE; // Start at fp-8

        public int getNextLocalOffset() {
            int current = nextLocalOffset;
            nextLocalOffset -= WORD_SIZE;
            return current;
        }
    }
}