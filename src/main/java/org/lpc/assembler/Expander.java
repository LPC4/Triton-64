package org.lpc.assembler;

import lombok.Getter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Expander class for handling instruction expansion in assembly code.
 * .
 * This class provides functionality to expand pseudo-instructions with immediate values,
 * register operations, and stack management. It allocates temporary registers
 * from a pool (t0-t8) while avoiding conflicts with instruction operands.
 * .
 * Register Usage:
 * - t9: Reserved as scratch register for assembler internal operations, do not use in code!
 * - t0-t8: Available temporary register pool, allocated as needed, saved/restored on stack
 * - sp: Stack pointer for safe temporary storage
 */
public class Expander {

    // Instruction encoding constants
    private static final int LDI_MIN_VALUE = -512;
    private static final int LDI_MAX_VALUE = 511;
    private static final int INSTRUCTIONS_PER_STACK_OP = 3;
    private static final int LDI_SETUP_INSTRUCTIONS = 4;
    private static final int INSTRUCTIONS_PER_CHUNK = 5;
    private static final int MAX_CHUNKS = 7; // 64-bit = 7 chunks of 10 bits

    // Register definitions
    private static final String STACK_POINTER = "sp";
    private static final String SCRATCH_REG = "t9"; // t9 will get overwritten by LDI, PUSH, POP, etc.
    private static final List<String> TEMP_POOL = List.of(
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8"
    );

    // Supported three-register operations
    private static final Set<String> IMMEDIATE_EXPANDED_OPERATIONS = Set.of(
            "ADD", "SUB", "MUL", "DIV", "AND", "OR", "XOR", "SHL", "SHR", "SAR", "MOD"
    );

    // Supported conditional jump operations
    private static final Set<String> CONDITIONAL_JUMPS = Set.of(
            "JZ", "JNZ", "JPP", "JPN", "JAL"
    );

    private final Map<String, InstructionExpander> expanders = new HashMap<>();

    public Expander() {
        initializeExpanders();
    }

    private void initializeExpanders() {
        // Unsafe expander for ROM initialization
        expanders.put("LDIU", new UnsafeLDIExpander());
        // Safe expanders using stack preservation
        expanders.put("LDI", new SafeLDIExpander());

        // Jump instructions
        expanders.put("JMP", new JumpExpander("JMP", false, 1));
        CONDITIONAL_JUMPS.forEach(op -> {
            expanders.put(op, new JumpExpander(op, true, 2));
        });

        // Stack operations
        expanders.put("PUSH", new StackExpander(true));
        expanders.put("POP", new StackExpander(false));

        // Three-register immediate operations
        IMMEDIATE_EXPANDED_OPERATIONS.forEach(op -> {
            expanders.put(op, new ImmediateExpander(op));
        });
    }

    public int getExpansionSize(String mnemonic, String[] operands) {
        return expanders.containsKey(mnemonic) ?
                expanders.get(mnemonic).size(operands) : 1;
    }

    public List<String> expand(List<String> lines, SymbolTable symbolTable) {
        return lines.stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .flatMap(line -> line.endsWith(":") ? // If it's a label, keep it
                        Stream.of(line) :
                        expandInstruction(line, symbolTable).stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> expandInstruction(String instruction, SymbolTable symbolTable) {
        String[] parts = instruction.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        String[] operands = parts.length > 1 ?
                parts[1].split(",\\s*") : new String[0];

        return expanders.containsKey(mnemonic) ?
                expanders.get(mnemonic).expand(operands, symbolTable) :
                List.of(instruction);
    }

    // Abstract base class for instruction expanders
    abstract static class InstructionExpander {
        public abstract List<String> expand(String[] operands, SymbolTable symbolTable);
        public abstract int size(String[] operands);

        protected final void validateOperandCount(String[] operands, int expected, String operation) {
            if (operands.length != expected) {
                throw new IllegalArgumentException(
                        operation + " requires " + expected + " operand(s), got " + operands.length);
            }
        }

        protected final void validateRegister(String operand, String operation) {
            if (!Parser.isRegister(operand)) {
                throw new IllegalArgumentException(
                        operation + " operand must be a register: " + operand);
            }
        }
    }

    // Helper class for intelligent temporary register allocation
    static class TempAllocator {
        private final Set<String> usedInOperands;
        @Getter private final List<String> allocated = new ArrayList<>();
        private int nextIndex = 0;

        TempAllocator(String[] operands) {
            HashSet<String> strings = new HashSet<>();
            for (String operand : operands) {
                if (Parser.isRegister(operand)) {
                    strings.add(operand);
                }
            }
            usedInOperands = strings;
        }

        String allocate() {
            while (nextIndex < TEMP_POOL.size()) {
                String candidate = TEMP_POOL.get(nextIndex++);
                if (!usedInOperands.contains(candidate)) {
                    allocated.add(candidate);
                    return candidate;
                }
            }
            throw new RuntimeException("No available temporary registers");
        }
    }

    // Utility methods for stack operations
    private static class StackUtils {
        static void pushTemps(List<String> lines, String... regs) {
            for (String reg : regs) {
                lines.add("LDI " + SCRATCH_REG + ", 8");
                lines.add("SUB " + STACK_POINTER + ", " + STACK_POINTER + ", " + SCRATCH_REG);
                lines.add("ST " + STACK_POINTER + ", " + reg);
            }
        }

        static void popTemps(List<String> lines, String... regs) {
            for (int i = regs.length - 1; i >= 0; i--) {
                String reg = regs[i];
                lines.add("LD " + reg + ", " + STACK_POINTER);
                lines.add("LDI " + SCRATCH_REG + ", 8");
                lines.add("ADD " + STACK_POINTER + ", " + STACK_POINTER + ", " + SCRATCH_REG);
            }
        }
    }

    // Utility methods for value parsing and range checking
    private static class ValueUtils {
        static boolean isInRange(long value, long min, long max) {
            return value >= min && value <= max;
        }

        static long parseValue(String valueStr, SymbolTable symbolTable) {
            return Parser.isImmediate(valueStr) ?
                    Parser.parseLongImmediate(valueStr) :
                    symbolTable.getSymbolAddress(valueStr);
        }

        static int getChunksForValue(long value) {
            if (isInRange(value, Short.MIN_VALUE, Short.MAX_VALUE)) return 2;
            if (isInRange(value, Integer.MIN_VALUE, Integer.MAX_VALUE)) return 4;
            return MAX_CHUNKS;
        }
    }

    // Base class for LDI expansion logic
    abstract static class BaseLDIExpander extends InstructionExpander {
        protected List<String> expandLDI(String[] operands, SymbolTable st, boolean useStack) {
            validateOperandCount(operands, 2, getInstructionName());
            String dest = operands[0];
            boolean isLabel = Parser.isLabel(operands[1]);
            long immValue = ValueUtils.parseValue(operands[1], st);

            if (ValueUtils.isInRange(immValue, LDI_MIN_VALUE, LDI_MAX_VALUE)) {
                return List.of("LDI " + dest + ", " + immValue);
            }

            if (!isLabel) {
                return expandLargeImmediateDynamic(operands, dest, immValue, useStack);
            } else {
                // For labels, always expand to 64-bit immediate, for consistency in size
                return expandLargeImmediate64Bit(operands, dest, immValue, useStack);
            }
        }

        private List<String> expandLargeImmediateDynamic(String[] operands, String dest, long immValue, boolean useStack) {
            TempAllocator allocator = new TempAllocator(operands);
            String maskReg = allocator.allocate();
            String shiftReg = allocator.allocate();
            int chunks = ValueUtils.getChunksForValue(immValue);

            List<String> lines = new ArrayList<>();

            if (useStack) StackUtils.pushTemps(lines, maskReg, shiftReg);
            setupMaskAndDestination(lines, dest, maskReg, shiftReg);
            processChunks(lines, dest, immValue, chunks, maskReg, shiftReg);
            if (useStack) StackUtils.popTemps(lines, maskReg, shiftReg);

            return lines;
        }

        private List<String> expandLargeImmediate64Bit(String[] operands, String dest, long immValue, boolean useStack) {
            TempAllocator allocator = new TempAllocator(operands);
            String maskReg = allocator.allocate();
            String shiftReg = allocator.allocate();

            List<String> lines = new ArrayList<>();

            if (useStack) StackUtils.pushTemps(lines, maskReg, shiftReg);
            setupMaskAndDestination(lines, dest, maskReg, shiftReg);
            processChunks(lines, dest, immValue, MAX_CHUNKS, maskReg, shiftReg);
            if (useStack) StackUtils.popTemps(lines, maskReg, shiftReg);

            return lines;
        }

        private void setupMaskAndDestination(List<String> lines, String dest, String maskReg, String shiftReg) {
            lines.add("LDI " + maskReg + ", 1");
            lines.add("LDI " + shiftReg + ", 9");
            lines.add("SHL " + maskReg + ", " + maskReg + ", " + shiftReg);
            lines.add("LDI " + dest + ", 0");
        }

        private void processChunks(List<String> lines, String dest, long immValue, int chunks, String maskReg, String shiftReg) {
            for (int i = 0; i < chunks; i++) {
                processChunk(lines, dest, immValue, i, maskReg, shiftReg);
            }
        }

        private void processChunk(List<String> lines, String dest, long immValue, int chunkIndex, String maskReg, String shiftReg) {
            long chunk = (immValue >>> (chunkIndex * 10)) & 0x3FF;
            int low9Bits = (int) (chunk & 0x1FF);
            int highBit = (int) ((chunk >>> 9) & 1);

            lines.add("LDI " + SCRATCH_REG + ", " + low9Bits);
            lines.add(highBit != 0 ?
                    "OR " + SCRATCH_REG + ", " + SCRATCH_REG + ", " + maskReg :
                    "NOP");
            lines.add("LDI " + shiftReg + ", " + (chunkIndex * 10));
            lines.add("SHL " + SCRATCH_REG + ", " + SCRATCH_REG + ", " + shiftReg);
            lines.add("OR " + dest + ", " + dest + ", " + SCRATCH_REG);
        }

        protected int calculateSize(String[] operands, boolean useStack) {
            if (operands.length != 2) return 1;

            try {
                long value = Parser.parseLongImmediate(operands[1]);
                if (ValueUtils.isInRange(value, LDI_MIN_VALUE, LDI_MAX_VALUE)) return 1;

                int chunks = ValueUtils.getChunksForValue(value);
                int stackOps = useStack ? 2 * (2 * INSTRUCTIONS_PER_STACK_OP) : 0;
                return stackOps + LDI_SETUP_INSTRUCTIONS + (INSTRUCTIONS_PER_CHUNK * chunks);
            } catch (Exception e) {
                // if label, always return 64-bit size
                int stackOps = useStack ? 2 * (2 * INSTRUCTIONS_PER_STACK_OP) : 0;
                return stackOps + LDI_SETUP_INSTRUCTIONS + (INSTRUCTIONS_PER_CHUNK * MAX_CHUNKS);
            }
        }

        protected abstract String getInstructionName();
    }

    // Unsafe LDI for ROM initialization (no stack operations)
    static class UnsafeLDIExpander extends BaseLDIExpander {
        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            return expandLDI(operands, st, false);
        }

        @Override
        public int size(String[] operands) {
            return calculateSize(operands, false);
        }

        @Override
        protected String getInstructionName() {
            return "LDIU";
        }
    }

    // Safe LDI with stack preservation
    static class SafeLDIExpander extends BaseLDIExpander {
        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            return expandLDI(operands, st, true);
        }

        @Override
        public int size(String[] operands) {
            return calculateSize(operands, true);
        }

        @Override
        protected String getInstructionName() {
            return "LDI";
        }
    }

    // Jump instruction expander
    static class JumpExpander extends InstructionExpander {
        private final String mnemonic;
        private final boolean isConditional;
        private final int expectedOperands;

        JumpExpander(String mnemonic, boolean isConditional, int expectedOperands) {
            this.mnemonic = mnemonic;
            this.isConditional = isConditional;
            this.expectedOperands = expectedOperands;
        }

        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            validateOperandCount(operands, expectedOperands, mnemonic);
            String target = operands[0];

            if (Parser.isRegister(target)) {
                return List.of(mnemonic + " " + String.join(", ", operands));
            }

            TempAllocator allocator = new TempAllocator(operands);
            String jumpTempReg = allocator.allocate();

            List<String> lines = new ArrayList<>(
                    new SafeLDIExpander().expand(new String[]{jumpTempReg, target}, st)
            );

            String jumpInstruction = mnemonic + " " + jumpTempReg;
            if (isConditional) {
                jumpInstruction += ", " + operands[1];
            }
            lines.add(jumpInstruction);

            return lines;
        }

        @Override
        public int size(String[] operands) {
            if (operands.length != expectedOperands) return 1;

            if (Parser.isRegister(operands[0])) {
                return 1;
            }

            return new SafeLDIExpander().calculateSize(new String[]{"dummy", operands[0]}, true) + 1;
        }
    }

    // Stack operations (PUSH/POP)
    static class StackExpander extends InstructionExpander {
        private final boolean isPush;

        StackExpander(boolean isPush) {
            this.isPush = isPush;
        }

        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            List<String> lines = new ArrayList<>();
            String operation = isPush ? "PUSH" : "POP";

            if (isPush) {
                Arrays.stream(operands).forEach(reg -> {
                    validateRegister(reg, operation);
                    lines.add("LDI " + SCRATCH_REG + ", 8");
                    lines.add("SUB " + STACK_POINTER + ", " + STACK_POINTER + ", " + SCRATCH_REG);
                    lines.add("ST " + STACK_POINTER + ", " + reg);
                });
            } else {
                for (int i = operands.length - 1; i >= 0; i--) {
                    String reg = operands[i];
                    validateRegister(reg, operation);
                    lines.add("LD " + reg + ", " + STACK_POINTER);
                    lines.add("LDI " + SCRATCH_REG + ", 8");
                    lines.add("ADD " + STACK_POINTER + ", " + STACK_POINTER + ", " + SCRATCH_REG);
                }
            }

            return lines;
        }

        @Override
        public int size(String[] operands) {
            return operands.length * INSTRUCTIONS_PER_STACK_OP;
        }
    }

    // Three-register immediate operations
    static class ImmediateExpander extends InstructionExpander {
        private final String mnemonic;

        ImmediateExpander(String mnemonic) {
            this.mnemonic = mnemonic;
        }

        @Override
        public List<String> expand(String[] operands, SymbolTable symbolTable) {
            validateOperandCount(operands, 3, mnemonic);
            String dest = operands[0];
            String src = operands[1];
            String imm = operands[2];

            if (Parser.isRegister(imm)) {
                return List.of(mnemonic + " " + dest + ", " + src + ", " + imm);
            }

            TempAllocator allocator = new TempAllocator(operands);
            String tempReg = allocator.allocate();

            List<String> lines = new ArrayList<>(
                    new SafeLDIExpander().expand(new String[]{tempReg, imm}, symbolTable)
            );
            lines.add(mnemonic + " " + dest + ", " + src + ", " + tempReg);

            return lines;
        }

        @Override
        public int size(String[] operands) {
            if (operands.length != 3) return 1;

            if (Parser.isRegister(operands[2])) {
                return 1;
            }

            return new SafeLDIExpander().calculateSize(new String[]{"dummy", operands[2]}, true) + 1;
        }
    }
}