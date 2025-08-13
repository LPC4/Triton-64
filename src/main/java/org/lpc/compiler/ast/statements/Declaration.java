package org.lpc.compiler.ast.statements;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.Expression;

@Getter
@ToString
public class Declaration implements Statement {
    private final String name;
    private final Expression initializer;
    private final Type type;

    public Declaration(String name, Expression initializer, Type type) {
        this.name = name;
        this.initializer = initializer;
        this.type = type;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
