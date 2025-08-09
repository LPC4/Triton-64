package org.lpc.compiler.ast.expressions;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

@ToString
public class BinaryOp extends Expression {
    public enum Op {
        // Arithmetic
        ADD, SUB, MUL, DIV, MOD,
        // Comparison
        LT, LE, GT, GE, EQ, NE,
        // Logical
        LOGICAL_AND, LOGICAL_OR,
        // Bitwise
        BITWISE_AND, BITWISE_OR, BITWISE_XOR,
        SHL, SHR, SAR
    }

    public final Op op;
    public final Expression left;
    public final Expression right;

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
            case "&" -> Op.BITWISE_AND;
            case "|" -> Op.BITWISE_OR;
            case "^" -> Op.BITWISE_XOR;
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
