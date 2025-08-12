package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import org.lpc.compiler.ast.AstVisitor;

@Getter
public class ArrayIndex implements Expression {
    private final Expression array;
    private final Expression index;

    public ArrayIndex(Expression array, Expression index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return getArray() + "[" + getIndex() + "]";
    }
}