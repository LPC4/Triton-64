package org.lpc.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.lpc.Globals;

public class Expander {
    public List<String> expand(String cleanedSource, SymbolTable symbolTable) {
        List<String> expandedLines = new ArrayList<>();
        String[] lines = cleanedSource.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.endsWith(":")) {
                expandedLines.add(trimmed);
            } else {
                String[] parts = trimmed.split("\\s+", 2);
                String mnemonic = parts[0].toUpperCase();
                String[] operands = parts.length > 1 ? parts[1].split(",\\s*") : new String[0];

                for (int i = 0; i < operands.length; i++) {
                    operands[i] = operands[i].trim();
                    if (symbolTable.hasSymbol(operands[i])) {
                        long addr = symbolTable.getSymbolAddress(operands[i]);
                        operands[i] = "0x" + Long.toHexString(addr);
                    }
                }

                String processedLine = mnemonic;
                if (operands.length > 0) {
                    processedLine += " " + String.join(", ", operands);
                }

                List<String> toExpand = new LinkedList<>();
                toExpand.add(processedLine);

                while (!toExpand.isEmpty()) {
                    String current = toExpand.remove(0);
                    List<String> expansion = expandLine(current);

                    if (expansion.size() == 1 && expansion.get(0).equals(current)) {
                        expandedLines.add(current);
                    } else {
                        toExpand.addAll(0, expansion); // Add expanded instructions to front
                    }
                }
            }
        }

        System.out.println("Expanded lines:");
        for (String expandedLine : expandedLines) {
            System.out.println(expandedLine);
        }

        return expandedLines;
    }

    private List<String> expandLine(String line) {
        String[] parts = line.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        String[] operands = parts.length > 1 ? parts[1].split(",\\s*") : new String[0];

        if ("LDI64".equals(mnemonic)) {
            if (operands.length != 2) {
                throw new IllegalArgumentException("LDI64 requires two operands");
            }
            return generateLDI64(operands[0], operands[1]);
        } else if ("JMP".equals(mnemonic) || "JZ".equals(mnemonic) || "JNZ".equals(mnemonic)) {
            return expandJump(mnemonic, operands);
        } else {
            return Collections.singletonList(line);
        }
    }

    private List<String> generateLDI64(String dest, String immValueStr) {
        List<String> lines = new ArrayList<>();
        long immValue = Parser.parseLongImmediate(immValueStr);
        String maskRegName = "t7";
        String tempRegName = "t8";
        String shiftRegName = "t9";

        lines.add("LDI " + maskRegName + ", 1");
        lines.add("LDI " + shiftRegName + ", 9");
        lines.add("SHL " + maskRegName + ", " + maskRegName + ", " + shiftRegName);
        lines.add("LDI " + dest + ", 0");

        for (int i = 0; i < 7; i++) {
            long chunk = (immValue >>> (i * 10)) & 0x3FF;
            int low = (int) (chunk & 0x1FF);
            int high = (int) ((chunk >>> 9) & 1);

            lines.add("LDI " + tempRegName + ", " + low);
            if (high != 0) {
                lines.add("OR " + tempRegName + ", " + tempRegName + ", " + maskRegName);
            } else {
                lines.add("NOP");
            }

            lines.add("LDI " + shiftRegName + ", " + (i * 10));
            lines.add("SHL " + tempRegName + ", " + tempRegName + ", " + shiftRegName);
            lines.add("OR " + dest + ", " + dest + ", " + tempRegName);
        }
        return lines;
    }

    private List<String> expandJump(String mnemonic, String[] operands) {
        List<String> lines = new ArrayList<>();
        String tempReg = "t6";

        if (mnemonic.equals("JMP")) {
            if (operands.length != 1) {
                throw new IllegalArgumentException("JMP requires one operand");
            }
            String target = operands[0];
            if (Parser.isRegister(target)) {
                lines.add("JMP " + target);
            } else {
                lines.addAll(generateLDI64(tempReg, target));
                lines.add("JMP " + tempReg);
            }
        } else {
            if (operands.length != 2) {
                throw new IllegalArgumentException(mnemonic + " requires two operands");
            }
            String target = operands[0];
            String condReg = operands[1];
            if (Parser.isRegister(target)) {
                lines.add(mnemonic + " " + target + ", " + condReg);
            } else {
                lines.addAll(generateLDI64(tempReg, target));
                lines.add(mnemonic + " " + tempReg + ", " + condReg);
            }
        }
        return lines;
    }
}