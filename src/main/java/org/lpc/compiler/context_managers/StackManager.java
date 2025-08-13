package org.lpc.compiler.context_managers;

import lombok.Getter;
import org.lpc.compiler.ast.VariableType;
import org.lpc.compiler.generators.InstructionGenerator;

import java.util.*;

/**
 * Manages stack operations with natural alignment (1 for byte, 4 for int, 8 for long).
 * This implementation minimizes memory usage while maintaining ABI compliance.
 */
public class StackManager {
    // Type sizes
    private static final int BYTE_SIZE = 1;
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;

    private static final int FRAME_ALIGNMENT = 8; // 8-byte alignment for stack frames

    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;
    private final StackOperations stackOps;
    private FunctionFrame currentFrame;

    public StackManager(InstructionGenerator emitter, RegisterManager registerManager) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackOps = new StackOperations();
    }

    // Frame management
    public void startFunctionFrame(List<String> parameters, List<VariableType> parameterTypes) {
        validateNoActiveFrame();
        currentFrame = new FunctionFrame(parameters, parameterTypes);
    }

    public void endFunctionFrame() {
        validateActiveFrame();
        currentFrame = null;
    }

    public int allocateVariable(String name, VariableType type) {
        validateActiveFrame();
        int size = getSizeForType(type);
        return currentFrame.allocateLocal(name, size, type);
    }

    public int getVariableOffset(String name) {
        validateActiveFrame();
        return currentFrame.getOffset(name);
    }

    // Stack operations
    public void storeToStack(int offset, String valueReg, VariableType type) {
        stackOps.storeAtOffset(offset, valueReg, type);
    }

    public String loadFromStack(int offset, VariableType type) {
        return stackOps.loadFromOffset(offset, type);
    }

    public int getTotalFrameSize() {
        validateActiveFrame();
        return currentFrame.getTotalFrameSize();
    }

    // Helper method to get size for a type
    private int getSizeForType(VariableType type) {
        return switch (type) {
            case BYTE -> BYTE_SIZE;
            case INT -> INT_SIZE;
            case LONG -> LONG_SIZE;
            default -> throw new IllegalArgumentException("Unsupported variable type: " + type);
        };
    }

    // Helper method to align offset to boundary
    private static int alignOffset(int offset, int alignment) {
        return (offset + alignment - 1) & -alignment;
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
     * Encapsulates stack memory operations with type awareness
     */
    private class StackOperations {
        public void storeAtOffset(int offset, String valueReg, VariableType type) {
            String addrReg = calculateAddress(offset);
            try {
                switch (type) {
                    case BYTE -> emitter.storeByte(addrReg, valueReg);
                    case INT -> emitter.storeInt(addrReg, valueReg);
                    case LONG -> emitter.store(addrReg, valueReg);
                    default -> throw new IllegalArgumentException("Unknown variable type: " + type);
                }
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }

        public String loadFromOffset(int offset, VariableType type) {
            String addrReg = calculateAddress(offset);
            String valueReg = registerManager.allocateRegister("stack_load_value");
            try {
                switch (type) {
                    case BYTE -> emitter.loadByte(valueReg, addrReg);
                    case INT -> emitter.loadInt(valueReg, addrReg);
                    case LONG -> emitter.load(valueReg, addrReg);
                    default -> throw new IllegalArgumentException("Unknown variable type: " + type);
                }
                return valueReg;
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }

        private String calculateAddress(int offset) {
            String addrReg = registerManager.allocateRegister("stack_addr");
            emitter.loadImmediate(addrReg, String.valueOf(offset));
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

        public FunctionFrame(List<String> parameters, List<VariableType> parameterTypes) {
            this.variables = new VariableMap();
            this.offsetCalculator = new OffsetCalculator();

            initializeParameters(parameters, parameterTypes);
        }

        private void initializeParameters(List<String> parameters, List<VariableType> parameterTypes) {
            int paramOffset = 16; // Start after saved ra/fp (8 bytes each)

            for (int i = 0; i < parameters.size(); i++) {
                String paramName = parameters.get(i);
                VariableType type = parameterTypes.get(i);
                int size = getSizeForType(type);

                // Align to the type's natural alignment
                int alignment = Math.max(1, size);
                paramOffset = alignOffset(paramOffset, alignment);

                variables.addParameter(paramName, paramOffset, size, type);
                paramOffset += size;
            }
        }

        private int getSizeForType(VariableType type) {
            return switch (type) {
                case BYTE -> BYTE_SIZE;
                case INT -> INT_SIZE;
                case LONG -> LONG_SIZE;
            };
        }

        public int allocateLocal(String name, int size, VariableType type) {
            if (variables.exists(name)) {
                return variables.getOffset(name);
            }
            if (variables.isParameter(name)) {
                throw new IllegalArgumentException("Variable " + name + " is already a parameter");
            }

            // No individual alignment - just pack variables tightly
            int offset = offsetCalculator.getNextLocalOffset(size);
            variables.addLocal(name, offset, size, type);
            return offset;
        }

        public int getOffset(String name) {
            return variables.getOffset(name);
        }

        // Calculate frame size dynamically with only final alignment
        public int getTotalFrameSize() {
            int localSize = offsetCalculator.getTotalLocalSize();
            // Only align the final frame size to 8 bytes, not individual variables
            return (localSize + FRAME_ALIGNMENT - 1) & -FRAME_ALIGNMENT;
        }
    }

    /**
     * Manages variable name to offset mappings with size and type information
     */
    private static class VariableMap {
        private final Map<String, VariableInfo> variables = new HashMap<>();
        private final Set<String> parameters = new HashSet<>();
        @Getter
        private int localCount = 0;

        public void addParameter(String name, int offset, int size, VariableType type) {
            variables.put(name, new VariableInfo(offset, size, type));
            parameters.add(name);
        }

        public void addLocal(String name, int offset, int size, VariableType type) {
            variables.put(name, new VariableInfo(offset, size, type));
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

        private record VariableInfo(int offset, int size, VariableType type) {
        }
    }

    /**
     * Calculates stack offsets for local variables with support for variable sizes and alignment
     */
    private static class OffsetCalculator {
        private int currentLocalOffset = 0; // Tracks total local space used

        public int getNextLocalOffset(int size) {
            int offset = -currentLocalOffset - size; // Subtract size to get the start of the variable
            currentLocalOffset += size;
            return offset;
        }

        public int getTotalLocalSize() {
            return currentLocalOffset;
        }
    }
}