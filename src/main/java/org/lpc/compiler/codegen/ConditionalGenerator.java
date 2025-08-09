package org.lpc.compiler.codegen;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.expressions.BinaryOp;
import org.lpc.compiler.ast.parent.Expression;

/**
 * Generates conditional jumps and logical operations in assembly code
 * based on the provided expressions and conditions.
 */
public class ConditionalGenerator {
    private final CodeGenContext ctx;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;

    public ConditionalGenerator(CodeGenContext ctx, InstructionEmitter emitter, RegisterManager registerManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    public void generateConditionalJump(Expression condition, String falseLabel, CodeGenerator visitor) {
        if (condition instanceof BinaryOp binaryOp) {
            if (isLogicalOp(binaryOp.op)) {
                generateLogicalJump(binaryOp, falseLabel, visitor);
            } else if (isComparisonOp(binaryOp.op)) {
                generateComparisonJump(binaryOp, falseLabel, true, visitor);
            } else {
                // Other binary operations - evaluate and test for zero
                String conditionReg = condition.accept(visitor);
                emitter.conditionalJump("JZ", falseLabel, conditionReg);
                registerManager.freeRegister(conditionReg);
            }
        } else {
            // Non-binary condition - evaluate and test for zero
            String conditionReg = condition.accept(visitor);
            emitter.conditionalJump("JZ", falseLabel, conditionReg);
            registerManager.freeRegister(conditionReg);
        }
    }

    private void generateLogicalJump(BinaryOp logical, String falseLabel, CodeGenerator visitor) {
        if (logical.op == BinaryOp.Op.LOGICAL_AND) {
            generateAndJump(logical, falseLabel, visitor);
        } else if (logical.op == BinaryOp.Op.LOGICAL_OR) {
            generateOrJump(logical, falseLabel, visitor);
        } else {
            throw new IllegalArgumentException("Expected logical operator, got: " + logical.op);
        }
    }

    private void generateAndJump(BinaryOp andOp, String falseLabel, CodeGenerator visitor) {
        emitter.comment("Logical AND: " + andOp.left + " && " + andOp.right);

        // If left is false, jump to false label (short-circuit)
        generateConditionalJump(andOp.left, falseLabel, visitor);

        // If we reach here, left was true, so evaluate right
        generateConditionalJump(andOp.right, falseLabel, visitor);
    }

    private void generateOrJump(BinaryOp orOp, String falseLabel, CodeGenerator visitor) {
        emitter.comment("Logical OR: " + orOp.left + " || " + orOp.right);

        String rightEvalLabel = ctx.generateLabel("or_eval_right");

        // If left is false, jump to evaluate right
        generateConditionalJump(orOp.left, rightEvalLabel, visitor);

        // Left was true, so overall condition is true - don't jump to false label
        String skipLabel = ctx.generateLabel("or_skip");
        emitter.jump(skipLabel);

        // Evaluate right operand
        emitter.label(rightEvalLabel);
        generateConditionalJump(orOp.right, falseLabel, visitor);

        emitter.label(skipLabel);
    }

    public void generateConditionalJumpOnTrue(Expression condition, String trueLabel, CodeGenerator visitor) {
        if (condition instanceof BinaryOp binaryOp) {
            if (isLogicalOp(binaryOp.op)) {
                generateLogicalJumpOnTrue(binaryOp, trueLabel, visitor);
            } else if (isComparisonOp(binaryOp.op)) {
                generateComparisonJump(binaryOp, trueLabel, false, visitor);
            } else {
                // Other binary operations - evaluate and test for non-zero
                String conditionReg = condition.accept(visitor);
                emitter.conditionalJump("JNZ", trueLabel, conditionReg);
                registerManager.freeRegister(conditionReg);
            }
        } else {
            // Non-binary condition - evaluate and test for non-zero
            String conditionReg = condition.accept(visitor);
            emitter.conditionalJump("JNZ", trueLabel, conditionReg);
            registerManager.freeRegister(conditionReg);
        }
    }

    private void generateLogicalJumpOnTrue(BinaryOp logical, String trueLabel, CodeGenerator visitor) {
        if (logical.op == BinaryOp.Op.LOGICAL_AND) {
            generateAndJumpOnTrue(logical, trueLabel, visitor);
        } else if (logical.op == BinaryOp.Op.LOGICAL_OR) {
            generateOrJumpOnTrue(logical, trueLabel, visitor);
        } else {
            throw new IllegalArgumentException("Expected logical operator, got: " + logical.op);
        }
    }

    private void generateAndJumpOnTrue(BinaryOp andOp, String trueLabel, CodeGenerator visitor) {
        emitter.comment("Logical AND (jump on true): " + andOp.left + " && " + andOp.right);

        String falseLabel = ctx.generateLabel("and_false");

        // If left is false, skip to end
        generateConditionalJump(andOp.left, falseLabel, visitor);

        // If we reach here, left was true, so check right
        generateConditionalJumpOnTrue(andOp.right, trueLabel, visitor);

        emitter.label(falseLabel);
    }

    private void generateOrJumpOnTrue(BinaryOp orOp, String trueLabel, CodeGenerator visitor) {
        emitter.comment("Logical OR (jump on true): " + orOp.left + " || " + orOp.right);

        // If left is true, jump to true label
        generateConditionalJumpOnTrue(orOp.left, trueLabel, visitor);

        // If left was false, check right
        generateConditionalJumpOnTrue(orOp.right, trueLabel, visitor);
    }

    public void generateComparisonJump(BinaryOp comparison, String jumpLabel, boolean jumpOnFalse, CodeGenerator visitor) {
        emitter.comment("Comparison: " + comparison.left + " " + comparison.op + " " + comparison.right);

        String leftReg = comparison.left.accept(visitor);
        String rightReg = comparison.right.accept(visitor);

        BinaryOp.Op op = jumpOnFalse ? invertComparison(comparison.op) : comparison.op;

        generateComparisonInstructions(op, leftReg, rightReg, jumpLabel);

        registerManager.freeRegister(leftReg);
        registerManager.freeRegister(rightReg);
    }

    private void generateComparisonInstructions(BinaryOp.Op op, String leftReg, String rightReg, String jumpLabel) {
        String tempReg = registerManager.allocateRegister("comparison_temp");

        // Perform subtraction: leftReg - rightReg
        emitter.subtract(tempReg, leftReg, rightReg);

        switch (op) {
            case GT -> {
                emitter.comment("Jump if " + leftReg + " > " + rightReg);
                emitter.conditionalJump("JPP", jumpLabel, tempReg);
            }
            case LT -> {
                emitter.comment("Jump if " + leftReg + " < " + rightReg);
                emitter.conditionalJump("JPN", jumpLabel, tempReg);
            }
            case EQ -> {
                emitter.comment("Jump if " + leftReg + " == " + rightReg);
                emitter.conditionalJump("JZ", jumpLabel, tempReg);
            }
            case NE -> {
                emitter.comment("Jump if " + leftReg + " != " + rightReg);
                emitter.conditionalJump("JNZ", jumpLabel, tempReg);
            }
            case GE -> {
                emitter.comment("Jump if " + leftReg + " >= " + rightReg);
                generateGreaterEqualJump(tempReg, jumpLabel);
            }
            case LE -> {
                emitter.comment("Jump if " + leftReg + " <= " + rightReg);
                generateLessEqualJump(tempReg, jumpLabel);
            }
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + op);
        }

        registerManager.freeRegister(tempReg);
    }

    private void generateGreaterEqualJump(String tempReg, String jumpLabel) {
        String skipLabel = ctx.generateLabel("skip_ge");
        emitter.conditionalJump("JPN", skipLabel, tempReg);  // Skip if negative
        emitter.jump(jumpLabel);                              // Jump if >= 0
        emitter.label(skipLabel);
    }

    private void generateLessEqualJump(String tempReg, String jumpLabel) {
        String skipLabel = ctx.generateLabel("skip_le");
        emitter.conditionalJump("JPP", skipLabel, tempReg);  // Skip if positive
        emitter.jump(jumpLabel);                              // Jump if <= 0
        emitter.label(skipLabel);
    }

    public static boolean isComparisonOp(BinaryOp.Op op) {
        return switch (op) {
            case GT, LT, GE, LE, EQ, NE -> true;
            default -> false;
        };
    }

    public static boolean isLogicalOp(BinaryOp.Op op) {
        return switch (op) {
            case LOGICAL_AND, LOGICAL_OR -> true;
            default -> false;
        };
    }

    private BinaryOp.Op invertComparison(BinaryOp.Op op) {
        return switch (op) {
            case GT -> BinaryOp.Op.LE;
            case LT -> BinaryOp.Op.GE;
            case GE -> BinaryOp.Op.LT;
            case LE -> BinaryOp.Op.GT;
            case EQ -> BinaryOp.Op.NE;
            case NE -> BinaryOp.Op.EQ;
            default -> throw new IllegalArgumentException("Cannot invert non-comparison operator: " + op);
        };
    }

    public String generateComparisonResult(BinaryOp comparison, CodeGenerator visitor) {
        String resultReg = registerManager.allocateRegister("comparison_result");
        String trueLabel = ctx.generateLabel("comp_true");
        String endLabel = ctx.generateLabel("comp_end");

        // Initialize result to 0 (false)
        emitter.loadImmediate(resultReg, 0);

        // Generate comparison that jumps to true label if condition holds
        generateComparisonJump(comparison, trueLabel, false, visitor);

        // Jump over the true case
        emitter.jump(endLabel);

        // True case: set result to 1
        emitter.label(trueLabel);
        emitter.loadImmediate(resultReg, 1);

        emitter.label(endLabel);
        return resultReg;
    }

    public String generateLogicalResult(BinaryOp logical, CodeGenerator visitor) {
        String resultReg = registerManager.allocateRegister("logical_result");
        String trueLabel = ctx.generateLabel("logical_true");
        String endLabel = ctx.generateLabel("logical_end");

        // Initialize result to 0 (false)
        emitter.loadImmediate(resultReg, 0);

        // Generate logical expression that jumps to true label if condition holds
        generateLogicalJumpOnTrue(logical, trueLabel, visitor);

        // Jump over the true case
        emitter.jump(endLabel);

        // True case: set result to 1
        emitter.label(trueLabel);
        emitter.loadImmediate(resultReg, 1);

        emitter.label(endLabel);
        return resultReg;
    }
}