package org.lpc.compiler.ast.expressions;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

public class Dereference extends Expression {
    public final Expression address;

    public Dereference(Expression address) {
        this.address = address;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
