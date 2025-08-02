package org.lpc.compiler.ast.expressions;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

@ToString
public class UnaryOp extends Expression {
    public final String operator;
    public final Expression operand;

    public UnaryOp(String operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
