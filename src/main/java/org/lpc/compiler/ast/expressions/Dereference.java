package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.types.Type;

@Getter
@ToString
public class Dereference implements Expression {
    private final Expression address;
    private final Type type;

    public Dereference(Expression address, Type type) {
        this.address = address;
        this.type = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
