package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class BinaryOp implements Expression {
    public enum Op {
        // Arithmetic
        ADD, SUB, MUL, DIV, MOD,
        // Comparison
        LT, LE, GT, GE, EQ, NE,
        // Logical
        LOGICAL_AND, LOGICAL_OR,
        // Bitwise
        AND, OR, XOR,
        SHL, SHR, SAR
    }

    private final Op op;
    private final Expression left;
    private final Expression right;

    public BinaryOp(String op, Expression left, Expression right) {
        this(convertStringToOp(op), left, right);
    }

    public BinaryOp(Op op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    private static Op convertStringToOp(String op) {
        return switch (op) {
            case "+" -> Op.ADD;
            case "-" -> Op.SUB;
            case "*" -> Op.MUL;
            case "/" -> Op.DIV;
            case "%" -> Op.MOD;
            case "<" -> Op.LT;
            case "<=" -> Op.LE;
            case ">" -> Op.GT;
            case ">=" -> Op.GE;
            case "==" -> Op.EQ;
            case "!=" -> Op.NE;
            case "&&" -> Op.LOGICAL_AND;
            case "||" -> Op.LOGICAL_OR;
            case "&" -> Op.AND;
            case "|" -> Op.OR;
            case "^" -> Op.XOR;
            case "<<" -> Op.SHL;
            case ">>" -> Op.SHR;
            case ">>>" -> Op.SAR; // Arithmetic right shift
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
