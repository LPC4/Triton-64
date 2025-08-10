package org.lpc.compiler.ast.expressions;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

import java.util.List;

public class ArrayLiteral extends Expression {
    public final List<Expression> elements;

    public ArrayLiteral(List<Expression> elements) {
        this.elements = elements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "[" + String.join(", ", elements.stream().map(Object::toString).toList()) + "]";
    }
}