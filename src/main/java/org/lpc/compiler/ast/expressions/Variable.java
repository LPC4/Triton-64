package org.lpc.compiler.ast.expressions;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

@ToString
public class Variable extends Expression {
    public final String name;
    public Variable(String name) {
        this.name = name;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
