package org.lpc.compiler.generators;

import org.lpc.compiler.ast.expressions.BinaryOp;
import org.lpc.compiler.context_managers.ContextManager;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Handles the emission of assembly instructions and manages formatting.
 * Provides a clean interface for generating assembly code.
 */
public class InstructionGenerator {
    private static final String ASSEMBLY_INDENT = "    ";
    private static final String COMMENT_PREFIX = ASSEMBLY_INDENT + "; ";

    private final ContextManager ctx;

    public InstructionGenerator(ContextManager ctx) {
        this.ctx = ctx;
    }

    public void comment(String comment) {
        // handle multi-line comments
        String[] lines = comment.split("\n");
        for (String line : lines) {
            ctx.addAssembly(COMMENT_PREFIX + line);
        }
    }

    public void blankLine() {
        ctx.addAssembly("");
    }

    public void sectionHeader(String title) {
        blankLine();
        String line = "=" + "=".repeat(Math.max(0, 50 - title.length())) + "=";
        comment(line);
        comment(title);
        comment(line);
    }

    public void label(String label) {
        ctx.addAssembly(label + ":");
    }

    public void instruction(String mnemonic, String... operands) {
        if (operands == null || operands.length == 0) {
            ctx.addAssembly(ASSEMBLY_INDENT + mnemonic);
            return;
        }
        // Join operands, trimming whitespace and filtering out empty strings
        String operandString = Arrays.stream(operands)
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));

        if (operandString.isEmpty()) {
            ctx.addAssembly(ASSEMBLY_INDENT + mnemonic);
        } else {
            ctx.addAssembly(String.format("%s%s %s", ASSEMBLY_INDENT, mnemonic, operandString));
        }
    }

    public void instructionWithComment(String comment, String mnemonic, String... operands) {
        String operandString = Arrays.stream(operands)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));

        String instruction = operandString.isEmpty() ?
                ASSEMBLY_INDENT + mnemonic :
                String.format("%s%s %s", ASSEMBLY_INDENT, mnemonic, operandString);

        ctx.addAssembly(String.format("%-40s ; %s", instruction, comment));
    }

    public void conditionalJump(String condition, String target, String testRegister) {
        switch (condition.toUpperCase()) {
            case "JZ", "ZERO" -> instruction("JZ", target, testRegister);
            case "JNZ", "NONZERO" -> instruction("JNZ", target, testRegister);
            case "JPP", "POSITIVE" -> instruction("JPP", target, testRegister);
            case "JPN", "NEGATIVE" -> instruction("JPN", target, testRegister);
            default -> throw new IllegalArgumentException("Unknown condition: " + condition);
        }
    }

    public void loadImmediate(String destReg, String value) {
        instruction("LDI", destReg, value);
    }

    public void move(String destReg, String srcReg) {
        instruction("MOV", destReg, srcReg);
    }

    public void push(String register) {
        instruction("PUSH", register);
    }

    public void pop(String register) {
        instruction("POP", register);
    }

    public void jump(String target) {
        instruction("JMP", target);
    }

    public void jumpAndLink(String target, String linkRegister) {
        instruction("JAL", target, linkRegister);
    }

    public void load(String destReg, String srcReg) {
        instruction("LD", destReg, srcReg);
    }

    public void store(String addrReg, String valueReg) {
        instruction("ST", addrReg, valueReg);
    }

    public void loadByte(String destReg, String addrReg) {
        instruction("LB", destReg, addrReg);
    }

    public void storeByte(String addrReg, String valueReg) {
        instruction("SB", addrReg, valueReg);
    }

    public void loadInt(String destReg, String addrReg) {
        instruction("LI", destReg, addrReg);
    }

    public void storeInt(String addrReg, String valueReg) {
        instruction("SI", addrReg, valueReg);
    }

    public void add(String destReg, String leftReg, String rightReg) {
        instruction("ADD", destReg, leftReg, rightReg);
    }

    public void subtract(String destReg, String leftReg, String rightReg) {
        instruction("SUB", destReg, leftReg, rightReg);
    }

    public void multiply(String destReg, String leftReg, String rightReg) {
        instruction("MUL", destReg, leftReg, rightReg);
    }

    public void divide(String destReg, String leftReg, String rightReg) {
        instruction("DIV", destReg, leftReg, rightReg);
    }

    public void and(String destReg, String leftReg, String rightReg) {
        instruction("AND", destReg, leftReg, rightReg);
    }

    public void or(String destReg, String leftReg, String rightReg) {
        instruction("OR", destReg, leftReg, rightReg);
    }

    public void xor(String destReg, String leftReg, String rightReg) {
        instruction("XOR", destReg, leftReg, rightReg);
    }

    public void not(String destReg, String srcReg) {
        instruction("NOT", destReg, srcReg);
    }

    public void negate(String destReg, String srcReg) {
        instruction("NEG", destReg, srcReg);
    }

    public void shiftLeft(String destReg, String leftReg, String rightReg) {
        instruction("SHL", destReg, leftReg, rightReg);
    }

    public void shiftRight(String destReg, String leftReg, String rightReg) {
        instruction("SHR", destReg, leftReg, rightReg);
    }

    public void shiftArithmeticRight(String destReg, String leftReg, String rightReg) {
        instruction("SAR", destReg, leftReg, rightReg);
    }

    public void halt() {
        instruction("HLT");
    }

    public void generateBinaryOperation(final BinaryOp.Op op, final String resultReg,
                                         final String leftReg, final String rightReg) {
        switch (op) {
            case ADD -> instruction("ADD", resultReg, leftReg, rightReg);
            case SUB -> instruction("SUB", resultReg, leftReg, rightReg);
            case MUL -> instruction("MUL", resultReg, leftReg, rightReg);
            case DIV -> instruction("DIV", resultReg, leftReg, rightReg);
            case AND -> instruction("AND", resultReg, leftReg, rightReg);
            case OR  -> instruction("OR", resultReg, leftReg, rightReg);
            case XOR -> instruction("XOR", resultReg, leftReg, rightReg);
            case SHL -> instruction("SHL", resultReg, leftReg, rightReg);
            case SHR -> instruction("SHR", resultReg, leftReg, rightReg);
            case SAR -> instruction("SAR", resultReg, leftReg, rightReg);
            default -> throw new IllegalArgumentException("Unsupported binary operator: " + op);
        }
    }
}