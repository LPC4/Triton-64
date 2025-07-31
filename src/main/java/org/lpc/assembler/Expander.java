package org.lpc.assembler;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Expander {
    public interface ExpanderFunc {
        List<String> expand(String[] operands, SymbolTable symbolTable);
    }

    private final Map<String, ExpanderFunc> expansions;
    private final Map<String, Function<String[], Integer>> expansionSizes;

    private static final int LDI64_TOTAL_INSTRUCTIONS = 39; // 4 + 5*7
    private static final int LDI32_TOTAL_INSTRUCTIONS = 24; // 4 + 5*4
    private static final int LDI16_TOTAL_INSTRUCTIONS = 14; // 4 + 5*2

    public Expander() {
        expansions = new HashMap<>();
        expansionSizes = new HashMap<>();

        // LDI64: 46 instructions
        expansions.put("LDI64", this::generateLDI64);
        expansionSizes.put("LDI64", ops -> LDI64_TOTAL_INSTRUCTIONS);

        // LDI32: 28 instructions (4 + 4*6)
        expansions.put("LDI32", this::generateLDI32);
        expansionSizes.put("LDI32", ops -> LDI32_TOTAL_INSTRUCTIONS);

        // LDI16: 16 instructions (4 + 2*6)
        expansions.put("LDI16", this::generateLDI16);
        expansionSizes.put("LDI16", ops -> LDI16_TOTAL_INSTRUCTIONS);

        // JMP: Size depends on operand type
        expansions.put("JMP", (ops, st) -> expandJump("JMP", ops, st));
        expansionSizes.put("JMP", ops ->
                (ops.length == 1 && Parser.isRegister(ops[0])) ? 1 : (LDI64_TOTAL_INSTRUCTIONS + 1));

        // JZ/JNZ: Similar to JMP
        expansions.put("JZ", (ops, st) -> expandJump("JZ", ops, st));
        expansions.put("JNZ", (ops, st) -> expandJump("JNZ", ops, st));
        expansionSizes.put("JZ", ops ->
                (ops.length == 2 && Parser.isRegister(ops[0])) ? 1 : (LDI64_TOTAL_INSTRUCTIONS + 1));
        expansionSizes.put("JNZ", ops ->
                (ops.length == 2 && Parser.isRegister(ops[0])) ? 1 : (LDI64_TOTAL_INSTRUCTIONS + 1));
    }

    public int getExpansionSize(String mnemonic, String[] operands) {
        Function<String[], Integer> sizeFunc = expansionSizes.get(mnemonic);
        return (sizeFunc != null) ? sizeFunc.apply(operands) : 1;
    }

    public List<String> expand(List<String> lines, SymbolTable symbolTable) {
        List<String> expandedLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.endsWith(":")) {
                expandedLines.add(trimmed);
            } else {
                String[] parts = trimmed.split("\\s+", 2);
                String mnemonic = parts[0].toUpperCase();
                String[] operands = parts.length > 1 ?
                        parts[1].split(",\\s*") : new String[0];

                ExpanderFunc expander = expansions.get(mnemonic);
                if (expander != null) {
                    expandedLines.addAll(expander.expand(operands, symbolTable));
                } else {
                    expandedLines.add(trimmed);
                }
            }
        }
        return expandedLines;
    }

    private List<String> generateLDI64(String[] operands, SymbolTable st) {
        return generateLDIX(operands, st, 7); // 64-bit needs 7 chunks
    }

    private List<String> generateLDI32(String[] operands, SymbolTable st) {
        return generateLDIX(operands, st, 4); // 32-bit needs 4 chunks
    }

    private List<String> generateLDI16(String[] operands, SymbolTable st) {
        return generateLDIX(operands, st, 2); // 16-bit needs 2 chunks
    }

    private List<String> generateLDIX(String[] operands, SymbolTable st, int chunks) {
        if (operands.length != 2) throw new IllegalArgumentException("LDIx requires two operands");
        String dest = operands[0];
        String immValueStr = operands[1];
        long immValue = Parser.isImmediate(immValueStr) ?
                Parser.parseLongImmediate(immValueStr) : st.getSymbolAddress(immValueStr);

        List<String> lines = new ArrayList<>();
        String maskReg = "t7", tempReg = "t8", shiftReg = "t9";

        lines.add("LDI " + maskReg + ", 1");
        lines.add("LDI " + shiftReg + ", 9");
        lines.add("SHL " + maskReg + ", " + maskReg + ", " + shiftReg);
        lines.add("LDI " + dest + ", 0");

        for (int i = 0; i < chunks; i++) {
            long chunk = (immValue >>> (i * 10)) & 0x3FF;
            int low = (int) (chunk & 0x1FF);
            int high = (int) ((chunk >>> 9) & 1);

            lines.add("LDI " + tempReg + ", " + low);
            if (high != 0) {
                lines.add("OR " + tempReg + ", " + tempReg + ", " + maskReg);
            } else {
                lines.add("NOP");
            }
            lines.add("LDI " + shiftReg + ", " + (i * 10));
            lines.add("SHL " + tempReg + ", " + tempReg + ", " + shiftReg);
            lines.add("OR " + dest + ", " + dest + ", " + tempReg);
        }
        return lines;
    }

    private List<String> expandJump(String mnemonic, String[] ops, SymbolTable st) {
        List<String> lines = new ArrayList<>();
        String tempReg = "t6";
        boolean isConditional = !mnemonic.equals("JMP");

        if (isConditional && ops.length != 2) {
            throw new IllegalArgumentException(mnemonic + " requires two operands");
        } else if (!isConditional && ops.length != 1) {
            throw new IllegalArgumentException("JMP requires one operand");
        }

        String target = ops[0];
        if (Parser.isRegister(target)) {
            lines.add(mnemonic + " " + String.join(", ", ops));
        } else {
            lines.addAll(generateLDI64(new String[]{tempReg, target}, st));
            lines.add(mnemonic + " " + tempReg + (isConditional ? ", " + ops[1] : ""));
        }
        return lines;
    }
}