package org.lpc.compiler.ast.expressions;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

@ToString
public class UnaryOp extends Expression {
    public enum Op { NEG, NOT }
    public final Op op;
    public final Expression operand;

    public UnaryOp(String op, Expression operand) {
        this(convertStringToOp(op), operand);
    }
    public UnaryOp(Op op, Expression operand) {
        this.op = op;
        this.operand = operand;
    }
    private static Op convertStringToOp(String op) {
        return switch (op) {
            case "-" -> Op.NEG;
            case "~" -> Op.NOT;
            default -> throw new IllegalArgumentException("Unknown unary operator: " + op);
        };
    }


    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
