package org.lpc.compiler.codegen;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.expressions.BinaryOp;
import org.lpc.compiler.ast.parent.Expression;


public class ConditionalGenerator {
    private final CodeGenContext ctx;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;

    public ConditionalGenerator(CodeGenContext ctx, InstructionEmitter emitter, RegisterManager registerManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    /**
     * Generate a conditional jump based on an expression
     * @param condition The condition expression to evaluate
     * @param falseLabel Label to jump to if condition is false
     * @param visitor The code generator visitor for evaluating expressions
     */
    public void generateConditionalJump(Expression condition, String falseLabel, CodeGenerator visitor) {
        if (condition instanceof BinaryOp binaryOp && isComparisonOp(binaryOp.op)) {
            generateComparisonJump(binaryOp, falseLabel, true, visitor);
        } else {
            // Non-comparison condition - evaluate and test for zero
            String conditionReg = condition.accept(visitor);
            emitter.conditionalJump("JZ", falseLabel, conditionReg);
            registerManager.freeRegister(conditionReg);
        }
    }

    /**
     * Generate a comparison jump instruction
     * @param comparison The binary comparison operation
     * @param jumpLabel Label to jump to
     * @param jumpOnFalse If true, jump when comparison is false; if false, jump when true
     * @param visitor The code generator visitor
     */
    public void generateComparisonJump(BinaryOp comparison, String jumpLabel, boolean jumpOnFalse, CodeGenerator visitor) {
        emitter.comment("Comparison: " + comparison.left + " " + comparison.op + " " + comparison.right);

        String leftReg = comparison.left.accept(visitor);
        String rightReg = comparison.right.accept(visitor);

        BinaryOp.Op op = jumpOnFalse ? invertComparison(comparison.op) : comparison.op;

        generateComparisonInstructions(op, leftReg, rightReg, jumpLabel);

        registerManager.freeRegister(leftReg);
        registerManager.freeRegister(rightReg);
    }

    /**
     * Generate the actual comparison and jump instructions
     */
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

    /**
     * Generate >= comparison (jump if not negative)
     */
    private void generateGreaterEqualJump(String tempReg, String jumpLabel) {
        String skipLabel = ctx.generateLabel("skip_ge");
        emitter.conditionalJump("JPN", skipLabel, tempReg);  // Skip if negative
        emitter.jump(jumpLabel);                              // Jump if >= 0
        emitter.label(skipLabel);
    }

    /**
     * Generate <= comparison (jump if not positive)
     */
    private void generateLessEqualJump(String tempReg, String jumpLabel) {
        String skipLabel = ctx.generateLabel("skip_le");
        emitter.conditionalJump("JPP", skipLabel, tempReg);  // Skip if positive
        emitter.jump(jumpLabel);                              // Jump if <= 0
        emitter.label(skipLabel);
    }

    /**
     * Check if an operator is a comparison operator
     */
    public static boolean isComparisonOp(BinaryOp.Op op) {
        return switch (op) {
            case GT, LT, GE, LE, EQ, NE -> true;
            default -> false;
        };
    }

    /**
     * Invert a comparison operator for conditional jumps
     */
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
}