package org.lpc.compiler.codegen;

import lombok.Getter;

import java.util.*;

/**
 * Manages stack operations and variable storage.
 * Handles stack frame management and variable offset calculations with support for variable-sized allocations.
 */
public class StackManager {
    private static final int DEFAULT_WORD_SIZE = 8; // 64-bit words
    private static final int STACK_ALIGNMENT = 8; // Align to 8-byte boundaries

    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final StackOperations stackOps;

    private FunctionFrame currentFrame;

    public StackManager(InstructionEmitter emitter, RegisterManager registerManager) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackOps = new StackOperations();
    }

    // Frame management
    public void startFunctionFrame(List<String> parameters) {
        startFunctionFrame(parameters, DEFAULT_WORD_SIZE);
    }

    public void startFunctionFrame(List<String> parameters, int parameterSize) {
        validateNoActiveFrame();
        currentFrame = new FunctionFrame(parameters, parameterSize);
    }

    public void endFunctionFrame() {
        validateActiveFrame();
        currentFrame = null;
    }

    public int allocateVariable(String name, int size) {
        validateActiveFrame();
        return currentFrame.allocateLocal(name, size);
    }

    public int getVariableOffset(String name) {
        validateActiveFrame();
        return currentFrame.getOffset(name);
    }

    // Stack operations
    public void storeToStack(int offset, String valueReg) {
        stackOps.storeAtOffset(offset, valueReg);
    }

    public String loadFromStack(int offset) {
        return stackOps.loadFromOffset(offset);
    }

    // Helper method to align sizes to next boundary
    // Examples: 1->8, 8->8, 9->16, 16->16, 17->24
    private static int alignSize(int size) {
        return ((size + STACK_ALIGNMENT - 1) / STACK_ALIGNMENT) * STACK_ALIGNMENT;
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
            this(parameters, DEFAULT_WORD_SIZE);
        }

        public FunctionFrame(List<String> parameters, int parameterSize) {
            this.variables = new VariableMap();
            this.offsetCalculator = new OffsetCalculator();

            // Initialize parameters with positive offsets (above frame pointer)
            initializeParameters(parameters, parameterSize);
        }

        private void initializeParameters(List<String> parameters, int parameterSize) {
            int paramOffset = 16; // Start after saved ra/fp
            for (String param : parameters) {
                int alignedSize = alignSize(parameterSize);
                variables.addParameter(param, paramOffset, alignedSize);
                paramOffset += alignedSize;
            }
        }

        public int allocateLocal(String name, int size) {
            if (variables.exists(name)) {
                return variables.getOffset(name);
            }

            if (variables.isParameter(name)) {
                throw new IllegalArgumentException("Variable " + name + " is already a parameter");
            }

            int alignedSize = alignSize(size);
            int offset = offsetCalculator.getNextLocalOffset(alignedSize);
            variables.addLocal(name, offset, alignedSize);
            return offset;
        }

        public int getOffset(String name) {
            return variables.getOffset(name);
        }

        public int getSize(String name) {
            return variables.getSize(name);
        }

        public int getLocalCount() {
            return variables.getLocalCount();
        }

        public int getTotalLocalSize() {
            return offsetCalculator.getTotalLocalSize();
        }
    }

    /**
     * Manages variable name to offset mappings with size information
     */
    private static class VariableMap {
        private final Map<String, VariableInfo> variables = new HashMap<>();
        private final Set<String> parameters = new HashSet<>();
        @Getter
        private int localCount = 0;

        public void addParameter(String name, int offset, int size) {
            variables.put(name, new VariableInfo(offset, size));
            parameters.add(name);
        }

        public void addLocal(String name, int offset, int size) {
            variables.put(name, new VariableInfo(offset, size));
            localCount++;
        }

        public boolean exists(String name) {
            return variables.containsKey(name);
        }

        public boolean isParameter(String name) {
            return parameters.contains(name);
        }

        public int getOffset(String name) {
            VariableInfo info = variables.get(name);
            if (info == null) {
                throw new IllegalArgumentException("Variable not found: " + name);
            }
            return info.offset;
        }

        public int getSize(String name) {
            VariableInfo info = variables.get(name);
            if (info == null) {
                throw new IllegalArgumentException("Variable not found: " + name);
            }
            return info.size;
        }

        private static class VariableInfo {
            final int offset;
            final int size;

            VariableInfo(int offset, int size) {
                this.offset = offset;
                this.size = size;
            }
        }
    }

    /**
     * Calculates stack offsets for local variables with support for variable sizes
     */
    private static class OffsetCalculator {
        private int currentLocalOffset = 0; // Tracks total local space used

        public int getNextLocalOffset(int size) {
            currentLocalOffset += size;
            return -currentLocalOffset; // Negative offset from fp
        }

        public int getTotalLocalSize() {
            return currentLocalOffset;
        }
    }
}