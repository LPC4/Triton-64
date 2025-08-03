package org.lpc.compiler;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CodeGenContext {
    private static final String TEMP_REGISTER_PREFIX = "temp_";
    private static final String ASSEMBLY_INDENT = "    ";
    private static final String COMMENT_PREFIX = ASSEMBLY_INDENT + "; ";

    private final RegisterAllocator registerAllocator;
    private final List<String> assembly;
    private final Deque<String> savedTemporaries;
    private int labelCounter = 0;
    private String lastGeneratedLabel;
    private FunctionFrame currentFrame;

    public CodeGenContext() {
        this.registerAllocator = new RegisterAllocator();
        this.assembly = new ArrayList<>();
        this.savedTemporaries = new ArrayDeque<>();
    }

    // Assembly generation methods
    public void addAssembly(String code) {
        assembly.add(code);
    }

    public void addComment(String comment) {
        assembly.add(COMMENT_PREFIX + comment);
    }

    public void addInstruction(String mnemonic, String... operands) {
        if (operands.length == 0) {
            assembly.add(String.format("%s%s", ASSEMBLY_INDENT, mnemonic));
        } else {
            String operandString = Arrays.stream(operands)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
            assembly.add(String.format("%s%s %s", ASSEMBLY_INDENT, mnemonic, operandString));
        }
    }

    public void addLabel(String label) {
        assembly.add(label + ":");
    }

    // Label generation
    public String generateLabel(String prefix) {
        lastGeneratedLabel = String.format("%s_%d", prefix, labelCounter++);
        return lastGeneratedLabel;
    }

    // Register allocation methods
    public String allocateTempRegister() {
        return registerAllocator.allocateTemp();
    }

    public void freeRegister(String register) {
        registerAllocator.freeTemp(register);
    }

    public void resetRegisterAllocator() {
        registerAllocator.reset();
    }

    // Stack frame management
    public void startFunctionFrame(List<String> parameters) {
        currentFrame = new FunctionFrame(parameters);
    }

    public void endFunctionFrame() {
        currentFrame = null;
    }

    public int allocateLocalVariable(String name) {
        return currentFrame.allocateLocal(name);
    }

    public int getVariableOffset(String name) {
        return currentFrame.getOffset(name);
    }

    public int getFrameSize() {
        return currentFrame != null ? currentFrame.getFrameSize() : 0;
    }

    // Temporary register save/restore
    public void saveLiveTemporaries() {
        Set<String> liveTemps = registerAllocator.getLiveTemporaries();
        liveTemps.stream()
                .sorted(Comparator.comparingInt(this::extractRegisterIndex))
                .forEach(reg -> {
                    addInstruction("PUSH", reg, "; Saving live temp");
                    savedTemporaries.push(reg);
                });
    }

    public void restoreLiveTemporaries() {
        while (!savedTemporaries.isEmpty()) {
            String reg = savedTemporaries.pop();
            addInstruction("POP", reg, "; Restoring live temp");
        }
    }

    private int extractRegisterIndex(String register) {
        return Integer.parseInt(register.substring(1));
    }

    // Function frame class for stack management
    private static class FunctionFrame {
        private final Map<String, Integer> varOffsets = new HashMap<>();
        private int localCount = 0;
        private final List<String> parameters;

        public FunctionFrame(List<String> parameters) {
            this.parameters = parameters;
            // Parameters are at negative offsets (below FP)
            for (int i = 0; i < parameters.size(); i++) {
                varOffsets.put(parameters.get(i), -(i + 1));
            }
        }

        public int allocateLocal(String name) {
            int offset = parameters.size() + localCount + 1;  // +1 for saved FP
            varOffsets.put(name, offset);
            localCount++;
            return offset;
        }

        public int getOffset(String name) {
            return varOffsets.get(name);
        }

        public int getFrameSize() {
            return parameters.size() + localCount + 2;  // +2 for FP and RA
        }
    }

    // Simplified register allocator (temps only)
    public static class RegisterAllocator {
        private final Queue<String> freeTempRegisters = new LinkedList<>();
        private final Set<String> allocatedTemps = new HashSet<>();

        public RegisterAllocator() {
            reset();
        }

        public void reset() {
            freeTempRegisters.clear();
            allocatedTemps.clear();
            // Initialize with t0-t9 registers
            for (int i = 0; i <= 9; i++) {
                freeTempRegisters.add("t" + i);
            }
        }

        public String allocateTemp() {
            if (freeTempRegisters.isEmpty()) {
                throw new IllegalStateException("No free temporary registers");
            }
            String reg = freeTempRegisters.poll();
            allocatedTemps.add(reg);
            return reg;
        }

        public void freeTemp(String reg) {
            if (allocatedTemps.remove(reg)) {
                freeTempRegisters.add(reg);
            }
        }

        public Set<String> getLiveTemporaries() {
            return new HashSet<>(allocatedTemps);
        }
    }
}