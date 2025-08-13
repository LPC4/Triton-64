package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class TypeConversion implements Expression {
    private final Expression expression;
    private final Type targetType;

    public TypeConversion(Expression expression, Type targetType) {
        this.expression = expression;
        this.targetType = targetType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}