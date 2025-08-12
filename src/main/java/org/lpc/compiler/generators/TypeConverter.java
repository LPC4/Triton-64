package org.lpc.compiler.generators;

import org.lpc.compiler.VariableType;
import org.lpc.compiler.context_managers.RegisterManager;

/**
 * Handles type conversions between different variable types with proper sign extension.
 *
 * Corrected the shift amounts for proper sign extension:
 * - BYTE to INT: 24-bit shifts (was incorrectly using 25)
 * - BYTE to LONG: 56-bit shifts (was incorrectly using 57)
 * - Fixed signExtendByteToLong to use consistent 56-bit shifts
 */
public class TypeConverter {
    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;

    public TypeConverter(InstructionGenerator emitter, RegisterManager registerManager) {
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    public void convert(VariableType sourceType, VariableType targetType,
                        String sourceReg, String resultReg) {

        if (sourceType == VariableType.BYTE) {
            convertFromByte(targetType, sourceReg, resultReg);
        } else if (sourceType == VariableType.INT) {
            convertFromInt(targetType, sourceReg, resultReg);
        } else if (sourceType == VariableType.LONG) {
            convertFromLong(targetType, sourceReg, resultReg);
        } else {
            // Unknown or same type, no conversion needed
            emitter.move(resultReg, sourceReg);
        }
    }

    private void convertFromByte(VariableType targetType, String sourceReg, String resultReg) {
        switch (targetType) {
            case INT -> {
                emitter.comment("Converting BYTE to INT (sign extend 8→32)");
                emitter.shiftLeft(resultReg, sourceReg, "24");
                emitter.shiftArithmeticRight(resultReg, resultReg, "24");
            }
            case LONG -> {
                emitter.comment("Converting BYTE to LONG (sign extend 8→64)");
                emitter.shiftLeft(resultReg, sourceReg, "56");
                emitter.shiftArithmeticRight(resultReg, resultReg, "56");
            }
            default -> {
                emitter.comment("BYTE to BYTE (no-op)");
                emitter.move(resultReg, sourceReg);
            }
        }
    }

    private void convertFromInt(VariableType targetType, String sourceReg, String resultReg) {
        switch (targetType) {
            case LONG -> {
                emitter.comment("Converting INT to LONG (sign extend 32→64)");
                emitter.shiftLeft(resultReg, sourceReg, "32");
                emitter.shiftArithmeticRight(resultReg, resultReg, "32");
            }
            case BYTE -> {
                emitter.comment("Converting INT to BYTE (truncate 32→8 and sign-extend to 64)");
                truncateWithMask(sourceReg, resultReg, 0xFF);
                signExtendByteToLong(resultReg);
            }
            default -> {
                emitter.comment("INT to INT (no-op)");
                emitter.move(resultReg, sourceReg);
            }
        }
    }

    private void convertFromLong(VariableType targetType, String sourceReg, String resultReg) {
        switch (targetType) {
            case BYTE -> {
                emitter.comment("Converting LONG to BYTE (truncate 64→8 and sign-extend to 64)");
                truncateWithMask(sourceReg, resultReg, 0xFF);
                signExtendByteToLong(resultReg);
            }
            case INT -> {
                emitter.comment("Converting LONG to INT (truncate 64→32 and sign-extend to 64)");
                truncateWithMask(sourceReg, resultReg, 0xFFFFFFFFL);
                signExtendIntToLong(resultReg);
            }
            default -> {
                emitter.comment("LONG to LONG (no-op)");
                emitter.move(resultReg, sourceReg);
            }
        }
    }

    private void truncateWithMask(String sourceReg, String resultReg, long mask) {
        String maskReg = registerManager.allocateRegister("mask");
        try {
            emitter.loadImmediate(maskReg, String.valueOf(mask));
            emitter.and(resultReg, sourceReg, maskReg);
        } finally {
            registerManager.freeRegister(maskReg);
        }
    }

    public void signExtendByteToLong(String reg) {
        emitter.shiftLeft(reg, reg, "56");
        emitter.shiftArithmeticRight(reg, reg, "56");
    }

    public void signExtendIntToLong(String reg) {
        emitter.shiftLeft(reg, reg, "32");
        emitter.shiftArithmeticRight(reg, reg, "32");
    }
}