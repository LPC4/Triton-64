package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.types.Type;

import java.util.List;

@Getter
public class ArrayLiteral implements Expression {
    private final List<Expression> elements;
    private final Type type;

    public ArrayLiteral(List<Expression> elements, Type type) {
        this.elements = elements;
        this.type = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "[" + String.join(", ", getElements().stream().map(Object::toString).toList()) + "]";
    }
}