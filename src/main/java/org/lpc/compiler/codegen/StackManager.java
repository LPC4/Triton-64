package org.lpc.compiler.codegen;

import lombok.Getter;
import lombok.Setter;

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

    private FunctionFrame currentFrame;

    public StackManager(CodeGenContext ctx, InstructionEmitter emitter, RegisterManager registerManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    public void startFunctionFrame(List<String> parameters) {
        if (currentFrame != null) {
            throw new IllegalStateException("Cannot start new frame - frame already active");
        }
        currentFrame = new FunctionFrame(parameters);
    }

    public void endFunctionFrame() {
        if (currentFrame == null) {
            throw new IllegalStateException("No active frame to end");
        }
        currentFrame = null;
    }

    public int allocateVariable(String name) {
        if (currentFrame == null) {
            throw new IllegalStateException("No active function frame");
        }
        return currentFrame.allocateLocal(name);
    }

    public int getVariableOffset(String name) {
        if (currentFrame == null) {
            throw new IllegalStateException("No active function frame");
        }
        return currentFrame.getOffset(name);
    }

    public int getLocalVariableCount() {
        return currentFrame != null ? currentFrame.getLocalCount() : 0;
    }

    public int getFrameSize() {
        return currentFrame != null ? currentFrame.getLocalCount() : 0;
    }

    public void storeToStack(int offset, String valueReg) {
        String addrReg = registerManager.allocateRegister("stack_store_addr");

        emitter.loadImmediate(addrReg, offset);
        emitter.add(addrReg, "fp", addrReg);
        emitter.store(addrReg, valueReg);

        registerManager.freeRegister(addrReg);
    }

    public String loadFromStack(int offset) {
        String addrReg = registerManager.allocateRegister("stack_load_addr");
        String valueReg = registerManager.allocateRegister("stack_load_value");

        emitter.loadImmediate(addrReg, offset);
        emitter.add(addrReg, "fp", addrReg);
        emitter.load(valueReg, addrReg);

        registerManager.freeRegister(addrReg);
        return valueReg;
    }

    public int getTotalFrameSize() {
        return currentFrame != null ? currentFrame.getLocalCount() : 0;
    }

    /**
     * Function frame implementation
     */
    private static class FunctionFrame {
        private final Map<String, Integer> variableOffsets = new HashMap<>();
        private final Set<String> parameters;
        private int nextLocalOffset;
        @Setter
        @Getter
        private int localCount;

        public FunctionFrame(List<String> parameters) {
            this.parameters = new HashSet<>(parameters);
            int paramOffset = 16;  // Fixed offset after ra/fp
            for (String param : parameters) {
                variableOffsets.put(param, paramOffset);
                paramOffset += WORD_SIZE;
            }
            nextLocalOffset = -WORD_SIZE;  // First local at fp-8
        }

        public int allocateLocal(String name) {
            if (variableOffsets.containsKey(name)) {
                return variableOffsets.get(name);
            }

            if (parameters.contains(name)) {
                throw new IllegalArgumentException("Variable " + name + " is already a parameter");
            }

            int offset = nextLocalOffset;
            variableOffsets.put(name, offset);
            nextLocalOffset -= WORD_SIZE;
            localCount++;

            return offset;
        }

        public int getOffset(String name) {
            Integer offset = variableOffsets.get(name);
            if (offset == null) {
                throw new IllegalArgumentException("Variable not found: " + name);
            }
            return offset;
        }

        public boolean hasVariable(String name) {
            return variableOffsets.containsKey(name);
        }

        public int getLocalFrameSize() {
            return localCount;
        }
    }
}