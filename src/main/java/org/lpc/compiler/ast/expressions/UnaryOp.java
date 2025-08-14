package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class UnaryOp implements Expression {
    public enum Op { NEG, NOT, BIN_NOT }
    private final Op op;
    private final Expression operand;

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
            case "!" -> Op.BIN_NOT;
            default -> throw new IllegalArgumentException("Unknown unary operator: " + op);
        };
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
