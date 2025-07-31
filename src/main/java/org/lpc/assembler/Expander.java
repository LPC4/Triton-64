package org.lpc.assembler;

import java.util.*;

/**
 * Expands pseudo-instructions into sequences of basic instructions.
 *
 * WARNING: The following registers are RESERVED for pseudo-instruction expansion
 * and MUST NOT be used in user code:
 * - t6: Jump target temporary
 * - t7: LDI mask register
 * - t8: LDI temporary register
 * - t9: LDI shift register
 */
public class Expander {

    public abstract static class InstructionExpander {
        public abstract List<String> expand(String[] operands, SymbolTable symbolTable);
        public abstract int size(String[] operands);

        protected final void validateOperandCount(String[] operands, int expected, String operation) {
            if (operands.length != expected) {
                throw new IllegalArgumentException(operation + " requires " + expected + " operand(s), got " + operands.length);
            }
        }

        protected final void validateRegister(String operand, String operation) {
            if (!Parser.isRegister(operand)) {
                throw new IllegalArgumentException(operation + " operand must be a register: " + operand);
            }
        }
    }

    // Value ranges and instruction counts
    private static final int LDI_MIN_VALUE = -512;  // 10-bit signed
    private static final int LDI_MAX_VALUE = 511;
    private static final int LDI16_INSTRUCTIONS = 14; // 4 setup + 5*2 chunks
    private static final int LDI32_INSTRUCTIONS = 24; // 4 setup + 5*4 chunks
    private static final int LDI64_INSTRUCTIONS = 39; // 4 setup + 5*7 chunks

    // Reserved temporary registers - DO NOT USE IN USER CODE
    private static final String JMP_TEMP_REG = "t6";
    private static final String LDI_MASK_REG = "t7";
    private static final String LDI_TEMP_REG = "t8";
    private static final String LDI_SHIFT_REG = "t9";
    private static final String STACK_POINTER = "sp";

    private final Map<String, InstructionExpander> expanders = new HashMap<>();

    public Expander() {
        initializeExpanders();
    }

    private void initializeExpanders() {
        expanders.put("LDI", new LDIExpander());
        expanders.put("LDI16", new LDI16Expander());
        expanders.put("LDI32", new LDI32Expander());
        expanders.put("LDI64", new LDI64Expander());
        expanders.put("JMP", new JMPExpander());
        expanders.put("JZ", new JZExpander());
        expanders.put("JNZ", new JNZExpander());
        expanders.put("PUSH", new PushExpander());
        expanders.put("POP", new PopExpander());
    }

    public int getExpansionSize(String mnemonic, String[] operands) {
        InstructionExpander expander = expanders.get(mnemonic);
        return expander != null ? expander.size(operands) : 1;
    }

    public List<String> expand(List<String> lines, SymbolTable symbolTable) {
        List<String> expandedLines = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.endsWith(":")) {
                expandedLines.add(trimmed);
            } else {
                expandedLines.addAll(expandInstruction(trimmed, symbolTable));
            }
        }

        return expandedLines;
    }

    private List<String> expandInstruction(String instruction, SymbolTable symbolTable) {
        String[] parts = instruction.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        String[] operands = parts.length > 1 ? parts[1].split(",\\s*") : new String[0];

        InstructionExpander expander = expanders.get(mnemonic);
        return expander != null ? expander.expand(operands, symbolTable) :
                Collections.singletonList(instruction);
    }

    // Utility methods
    private static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    private static long parseValue(String valueStr, SymbolTable symbolTable) {
        return Parser.isImmediate(valueStr) ?
                Parser.parseLongImmediate(valueStr) :
                symbolTable.getSymbolAddress(valueStr);
    }

    // Auto-selecting LDI expander
    private static class LDIExpander extends InstructionExpander {
        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            validateOperandCount(operands, 2, "LDI");

            try {
                long value = Parser.parseLongImmediate(operands[1]);
                return expandForValue(value, operands, st);
            } catch (NumberFormatException e) {
                // Symbol reference - use LDI64 for safety
                return new LDI64Expander().expand(operands, st);
            }
        }

        private List<String> expandForValue(long value, String[] operands, SymbolTable st) {
            if (isInRange(value, LDI_MIN_VALUE, LDI_MAX_VALUE)) {
                return Collections.singletonList("LDI " + operands[0] + ", " + operands[1]);
            } else if (isInRange(value, Short.MIN_VALUE, Short.MAX_VALUE)) {
                return new LDI16Expander().expand(operands, st);
            } else if (isInRange(value, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
                return new LDI32Expander().expand(operands, st);
            } else {
                return new LDI64Expander().expand(operands, st);
            }
        }

        @Override
        public int size(String[] operands) {
            if (operands.length != 2) return 1;

            try {
                long value = Parser.parseLongImmediate(operands[1]);
                return getSizeForValue(value);
            } catch (NumberFormatException e) {
                return LDI64_INSTRUCTIONS; // Symbol reference
            }
        }

        private int getSizeForValue(long value) {
            if (isInRange(value, LDI_MIN_VALUE, LDI_MAX_VALUE)) return 1;
            if (isInRange(value, Short.MIN_VALUE, Short.MAX_VALUE)) return LDI16_INSTRUCTIONS;
            if (isInRange(value, Integer.MIN_VALUE, Integer.MAX_VALUE)) return LDI32_INSTRUCTIONS;
            return LDI64_INSTRUCTIONS;
        }
    }

    // Base class for fixed-size LDI variants
    private abstract static class LDIXExpander extends InstructionExpander {
        protected abstract int getChunks();
        protected abstract int getInstructionCount();

        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            validateOperandCount(operands, 2, "LDI expansion");

            String dest = operands[0];
            long immValue = parseValue(operands[1], st);

            List<String> lines = new ArrayList<>();
            setupMaskAndDestination(lines, dest);
            processChunks(lines, dest, immValue);

            return lines;
        }

        private void setupMaskAndDestination(List<String> lines, String dest) {
            lines.add("LDI " + LDI_MASK_REG + ", 1");
            lines.add("LDI " + LDI_SHIFT_REG + ", 9");
            lines.add("SHL " + LDI_MASK_REG + ", " + LDI_MASK_REG + ", " + LDI_SHIFT_REG);
            lines.add("LDI " + dest + ", 0");
        }

        private void processChunks(List<String> lines, String dest, long immValue) {
            for (int i = 0; i < getChunks(); i++) {
                processChunk(lines, dest, immValue, i);
            }
        }

        private void processChunk(List<String> lines, String dest, long immValue, int chunkIndex) {
            long chunk = (immValue >>> (chunkIndex * 10)) & 0x3FF;
            int low9Bits = (int) (chunk & 0x1FF);
            int highBit = (int) ((chunk >>> 9) & 1);

            lines.add("LDI " + LDI_TEMP_REG + ", " + low9Bits);

            if (highBit != 0) {
                lines.add("OR " + LDI_TEMP_REG + ", " + LDI_TEMP_REG + ", " + LDI_MASK_REG);
            } else {
                lines.add("NOP");
            }

            lines.add("LDI " + LDI_SHIFT_REG + ", " + (chunkIndex * 10));
            lines.add("SHL " + LDI_TEMP_REG + ", " + LDI_TEMP_REG + ", " + LDI_SHIFT_REG);
            lines.add("OR " + dest + ", " + dest + ", " + LDI_TEMP_REG);
        }

        @Override
        public int size(String[] operands) {
            return getInstructionCount();
        }
    }

    private static class LDI16Expander extends LDIXExpander {
        @Override protected int getChunks() { return 2; }
        @Override protected int getInstructionCount() { return LDI16_INSTRUCTIONS; }
    }

    private static class LDI32Expander extends LDIXExpander {
        @Override protected int getChunks() { return 4; }
        @Override protected int getInstructionCount() { return LDI32_INSTRUCTIONS; }
    }

    private static class LDI64Expander extends LDIXExpander {
        @Override protected int getChunks() { return 7; }
        @Override protected int getInstructionCount() { return LDI64_INSTRUCTIONS; }
    }

    // Base class for jump instructions
    private abstract static class JumpExpander extends InstructionExpander {
        protected abstract String getMnemonic();
        protected abstract boolean isConditional();
        protected abstract int getExpectedOperands();

        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            validateOperandCount(operands, getExpectedOperands(), getMnemonic());

            String target = operands[0];
            if (Parser.isRegister(target)) {
                return Collections.singletonList(getMnemonic() + " " + String.join(", ", operands));
            } else {
                return expandWithAddressLoad(operands, st);
            }
        }

        private List<String> expandWithAddressLoad(String[] operands, SymbolTable st) {
            List<String> lines = new ArrayList<>(
                    new LDI64Expander().expand(new String[]{JMP_TEMP_REG, operands[0]}, st)
            );

            String jumpInstruction = getMnemonic() + " " + JMP_TEMP_REG;
            if (isConditional()) {
                jumpInstruction += ", " + operands[1];
            }
            lines.add(jumpInstruction);

            return lines;
        }

        @Override
        public int size(String[] operands) {
            if (operands.length != getExpectedOperands()) return 1;
            return Parser.isRegister(operands[0]) ? 1 : LDI64_INSTRUCTIONS + 1;
        }
    }

    private static class JMPExpander extends JumpExpander {
        @Override protected String getMnemonic() { return "JMP"; }
        @Override protected boolean isConditional() { return false; }
        @Override protected int getExpectedOperands() { return 1; }
    }

    private static class JZExpander extends JumpExpander {
        @Override protected String getMnemonic() { return "JZ"; }
        @Override protected boolean isConditional() { return true; }
        @Override protected int getExpectedOperands() { return 2; }
    }

    private static class JNZExpander extends JumpExpander {
        @Override protected String getMnemonic() { return "JNZ"; }
        @Override protected boolean isConditional() { return true; }
        @Override protected int getExpectedOperands() { return 2; }
    }

    // Stack operation expanders
    private static class PushExpander extends InstructionExpander {
        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            List<String> lines = new ArrayList<>();

            for (String reg : operands) {
                validateRegister(reg, "PUSH");
                lines.add("LDI " + LDI_TEMP_REG + ", 8");
                lines.add("SUB " + STACK_POINTER + ", " + STACK_POINTER + ", " + LDI_TEMP_REG);
                lines.add("ST " + STACK_POINTER + ", " + reg);
            }

            return lines;
        }

        @Override
        public int size(String[] operands) {
            return operands.length * 3;
        }
    }

    private static class PopExpander extends InstructionExpander {
        @Override
        public List<String> expand(String[] operands, SymbolTable st) {
            List<String> lines = new ArrayList<>();

            // Process in reverse order to maintain stack discipline
            for (int i = operands.length - 1; i >= 0; i--) {
                String reg = operands[i];
                validateRegister(reg, "POP");
                lines.add("LD " + reg + ", " + STACK_POINTER);
                lines.add("LDI " + LDI_TEMP_REG + ", 8");
                lines.add("ADD " + STACK_POINTER + ", " + STACK_POINTER + ", " + LDI_TEMP_REG);
            }

            return lines;
        }

        @Override
        public int size(String[] operands) {
            return operands.length * 3;
        }
    }
}