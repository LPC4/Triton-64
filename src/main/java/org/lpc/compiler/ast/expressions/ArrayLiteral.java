package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

import java.util.List;

@Getter
public class ArrayLiteral extends Expression {
    private final List<Expression> elements;

    public ArrayLiteral(List<Expression> elements) {
        this.elements = elements;
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