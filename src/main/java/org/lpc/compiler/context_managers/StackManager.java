package org.lpc.compiler.context_managers;

import lombok.Getter;
import org.lpc.compiler.types.PointerType;
import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.generators.InstructionGenerator;

import java.util.*;

/**
 * Manages stack operations with natural alignment (1 for byte, 4 for int, 8 for long).
 * This implementation minimizes memory usage while maintaining ABI compliance.
 */
public class StackManager {
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
    public void startFunctionFrame(List<String> parameters, List<Type> parameterTypes) {
        validateNoActiveFrame();
        currentFrame = new FunctionFrame(parameters, parameterTypes);
    }

    public void endFunctionFrame() {
        validateActiveFrame();
        currentFrame = null;
    }

    public int allocateVariable(String name, Type type) {
        validateActiveFrame();
        int size = type.getSize();
        return currentFrame.allocateLocal(name, size, type);
    }

    public int getVariableOffset(String name) {
        validateActiveFrame();
        return currentFrame.getOffset(name);
    }

    // Stack operations
    public void storeToStack(int offset, String valueReg, Type type) {
        stackOps.storeAtOffset(offset, valueReg, type);
    }

    public String loadFromStack(int offset, Type type) {
        return stackOps.loadFromOffset(offset, type);
    }

    public int getTotalFrameSize() {
        validateActiveFrame();
        return currentFrame.getTotalFrameSize();
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

    public Type getVariableType(String name) {
        validateActiveFrame();
        if (!currentFrame.getVariables().exists(name)) {
            throw new IllegalArgumentException("Variable not found: " + name);
        }
        return currentFrame.getVariables().getStringVariableInfoHashMap().get(name).type;
    }

    /**
     * Encapsulates stack memory operations with type awareness
     */
    private class StackOperations {
        public void storeAtOffset(int offset, String valueReg, Type type) {
            String addrReg = calculateAddress(offset);
            try {
                if (type.isPrimitive()) {
                    storePrimitive(addrReg, valueReg, type.asPrimitive());
                } else if (type.isPtr()) {
                    emitter.store(addrReg, valueReg); // Store pointer address (long)
                } else {
                    throw new IllegalArgumentException("Unknown variable type: " + type);
                }
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }

        public String loadFromOffset(int offset, Type type) {
            String addrReg = calculateAddress(offset);
            String valueReg = registerManager.allocateRegister("stack_load_value");

            try {
                if (type.isPrimitive()) {
                    loadPrimitive(valueReg, addrReg, type.asPrimitive());
                    return valueReg;
                } else if (type.isPtr()) {
                    emitter.load(valueReg, addrReg); // Load pointer address (long)
                    return valueReg;
                } else {
                    throw new IllegalArgumentException("Unknown variable type: " + type);
                }
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

        private void loadPrimitive(String valueReg, String addrReg, PrimitiveType type) {
            switch (type) {
                case PrimitiveType.BYTE -> emitter.loadByte(valueReg, addrReg);
                case PrimitiveType.INT -> emitter.loadInt(valueReg, addrReg);
                case PrimitiveType.LONG -> emitter.load(valueReg, addrReg);
            }
        }

        private void storePrimitive(String addrReg, String valueReg, PrimitiveType type) {
            switch (type) {
                case PrimitiveType.BYTE -> emitter.storeByte(addrReg, valueReg);
                case PrimitiveType.INT -> emitter.storeInt(addrReg, valueReg);
                case PrimitiveType.LONG -> emitter.store(addrReg, valueReg);
            }
        }
    }

    /**
     * Represents a function's stack frame with parameter and local variable management
     */
    @Getter
    private static class FunctionFrame {
        private final VariableMap variables;
        private final OffsetCalculator offsetCalculator;

        public FunctionFrame(List<String> parameters, List<Type> parameterTypes) {
            this.variables = new VariableMap();
            this.offsetCalculator = new OffsetCalculator();

            initializeParameters(parameters, parameterTypes);
        }

        private void initializeParameters(List<String> parameters, List<Type> parameterTypes) {
            int paramOffset = 16; // Start after saved ra/fp (8 bytes each)

            for (int i = 0; i < parameters.size(); i++) {
                String paramName = parameters.get(i);
                Type type = parameterTypes.get(i);
                int size = type.getSize();

                // Align to the type's natural alignment
                int alignment = Math.max(1, size);
                paramOffset = alignOffset(paramOffset, alignment);

                variables.addParameter(paramName, paramOffset, size, type);
                paramOffset += size;
            }
        }

        public int allocateLocal(String name, int size, Type type) {
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
    @Getter
    private static class VariableMap {
        private final Map<String, VariableInfo> stringVariableInfoHashMap = new HashMap<>();
        private final Set<String> parameters = new HashSet<>();
        @Getter
        private int localCount = 0;

        public void addParameter(String name, int offset, int size, Type type) {
            stringVariableInfoHashMap.put(name, new VariableInfo(offset, size, type));
            parameters.add(name);
        }

        public void addLocal(String name, int offset, int size, Type type) {
            stringVariableInfoHashMap.put(name, new VariableInfo(offset, size, type));
            localCount++;
        }

        public boolean exists(String name) {
            return stringVariableInfoHashMap.containsKey(name);
        }

        public boolean isParameter(String name) {
            return parameters.contains(name);
        }

        public int getOffset(String name) {
            VariableInfo info = stringVariableInfoHashMap.get(name);
            if (info == null) {
                throw new IllegalArgumentException("Variable not found: " + name);
            }
            return info.offset;
        }

        private record VariableInfo(int offset, int size, Type type) {
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