package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

@Getter
@ToString
public class TypeConversion extends Expression {
    private final Expression expression;
    private final VariableType targetType;

    public TypeConversion(Expression expression, VariableType targetType) {
        this.expression = expression;
        this.targetType = targetType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}