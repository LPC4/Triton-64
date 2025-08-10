package org.lpc.compiler.ast.expressions;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

public class ArrayIndex extends Expression {
    public final Expression array;
    public final Expression index;

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
        return array + "[" + index + "]";
    }
}