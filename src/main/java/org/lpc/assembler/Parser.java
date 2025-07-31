package org.lpc.assembler;

import org.lpc.cpu.RegisterInfo;

public class Parser {
    public static long parseLongImmediate(String immStr) {
        String trimmed = immStr.trim();
        try {
            if (trimmed.startsWith("0x")) {
                return Long.parseLong(trimmed.substring(2), 16);
            } else if (trimmed.startsWith("0b")) {
                return Long.parseLong(trimmed.substring(2), 2);
            } else {
                return Long.parseLong(trimmed);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid immediate: " + immStr);
        }
    }

    // Update isImmediate to use new parsing logic
    public static boolean isImmediate(String operand) {
        try {
            parseLongImmediate(operand);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isRegister(String operand) {
        String trimmed = operand.toLowerCase().trim();
        return RegisterInfo.REG_ALIAS.containsKey(trimmed);
    }

    public static boolean isLabel(String operand) {
        return !isRegister(operand) && !isImmediate(operand);
    }

    public static void validateOperandCount(String[] operands, int expected, String instruction) {
        if (operands.length != expected) {
            throw new IllegalArgumentException(
                    String.format("Invalid operand count for %s: expected %d, got %d",
                            instruction, expected, operands.length));
        }
    }

    public static int parseRegister(String operand) {
        String trimmed = operand.toLowerCase().trim();
        if (RegisterInfo.REG_ALIAS.containsKey(trimmed)) {
            return RegisterInfo.REG_ALIAS.get(trimmed);
        } else {
            throw new IllegalArgumentException("Invalid register: " + operand);
        }
    }
}