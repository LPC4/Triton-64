package org.lpc.compiler.generators;

import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.context_managers.RegisterManager;

/**
 * Handles type conversions between different variable types with proper sign extension.
 *
 * Corrected the shift amounts for proper sign extension:
 * - BYTE to INT: 24-bit shifts (was incorrectly using 25)
 * - BYTE to LONG: 56-bit shifts (was incorrectly using 57)
 * - Fixed signExtendByteToLong to use consistent 56-bit shifts
 */
public class TypeConversionGenerator {
    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;
    public TypeConversionGenerator(InstructionGenerator emitter, RegisterManager registerManager) {
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    public void convert(Type sourceType, Type targetType,
                        String sourceReg, String resultReg) {
        switch (sourceType) {
            case PrimitiveType.BYTE -> convertFromByte(targetType, sourceReg, resultReg);
            case PrimitiveType.INT -> convertFromInt(targetType, sourceReg, resultReg);
            case PrimitiveType.LONG -> convertFromLong(targetType, sourceReg, resultReg);
            default -> {
                // All pointers are stored as 64-bit values - pointer-to-pointer conversions are no-ops
                emitter.move(resultReg, sourceReg);
            }
        }
    }

    private void convertFromByte(Type targetType, String sourceReg, String resultReg) {
        switch (targetType) {
            case PrimitiveType.INT  -> {
                emitter.comment("Converting BYTE to INT (sign extend 8→32)");
                emitter.shiftLeft(resultReg, sourceReg, "24");
                emitter.shiftArithmeticRight(resultReg, resultReg, "24");
            }
            case PrimitiveType.LONG -> {
                emitter.comment("Converting BYTE to LONG (sign extend 8→64)");
                emitter.shiftLeft(resultReg, sourceReg, "56");
                emitter.shiftArithmeticRight(resultReg, resultReg, "56");
            }
            default -> {
                if (targetType.isPtr()) {
                    emitter.comment("Converting BYTE to POINTER (sign extend 8→64)");
                    emitter.shiftLeft(resultReg, sourceReg, "56");
                    emitter.shiftArithmeticRight(resultReg, resultReg, "56");
                } else {
                    emitter.comment("BYTE to BYTE (no-op)");
                    emitter.move(resultReg, sourceReg);
                }
            }
        }
    }

    private void convertFromInt(Type targetType, String sourceReg, String resultReg) {
        switch (targetType) {
            case PrimitiveType.LONG -> {
                emitter.comment("Converting INT to LONG (sign extend 32→64)");
                emitter.shiftLeft(resultReg, sourceReg, "32");
                emitter.shiftArithmeticRight(resultReg, resultReg, "32");
            }
            case PrimitiveType.BYTE -> {
                emitter.comment("Converting INT to BYTE (truncate 32→8 and sign-extend to 64)");
                truncateWithMask(sourceReg, resultReg, 0xFF);
                signExtendByteToLong(resultReg);
            }
            default -> {
                if (targetType.isPtr()) {
                    emitter.comment("Converting INT to POINTER (sign extend 32→64)");
                    emitter.shiftLeft(resultReg, sourceReg, "32");
                    emitter.shiftArithmeticRight(resultReg, resultReg, "32");
                } else {
                    emitter.comment("INT to INT (no-op)");
                    emitter.move(resultReg, sourceReg);
                }
            }
        }
    }

    private void convertFromLong(Type targetType, String sourceReg, String resultReg) {
        switch (targetType) {
            case PrimitiveType.BYTE -> {
                emitter.comment("Converting LONG to BYTE (truncate 64→8 and sign-extend to 64)");
                truncateWithMask(sourceReg, resultReg, 0xFF);
                signExtendByteToLong(resultReg);
            }
            case PrimitiveType.INT -> {
                emitter.comment("Converting LONG to INT (truncate 64→32 and sign-extend to 64)");
                truncateWithMask(sourceReg, resultReg, 0xFFFFFFFFL);
                signExtendIntToLong(resultReg);
            }
            default -> {
                if (targetType.isPtr()) {
                    emitter.comment("Converting LONG to POINTER (no-op)");
                    emitter.move(resultReg, sourceReg);
                } else {
                    emitter.comment("LONG to LONG (no-op)");
                    emitter.move(resultReg, sourceReg);
                }
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