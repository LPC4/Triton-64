package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class Dereference implements Expression {
    private final Expression address;

    public Dereference(Expression address) {
        this.address = address;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
