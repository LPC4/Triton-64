package org.lpc.compiler.ast.expressions;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

@ToString
public class BinaryOp extends Expression {
    public enum Op { ADD, SUB, MUL, DIV, MOD, LT, LE, GT, GE, EQ, NE, AND, OR }

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
            case "&&" -> Op.AND;
            case "||" -> Op.OR;
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}