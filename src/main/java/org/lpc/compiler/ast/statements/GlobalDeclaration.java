package org.lpc.compiler.ast.statements;

import lombok.Getter;
import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

@Getter
public class GlobalDeclaration extends Statement {
    private final String name;
    private final Expression initializer;
    private final VariableType type;

    public GlobalDeclaration(String name, Expression initializer, VariableType type) {
        this.name = name;
        this.initializer = initializer;
        this.type = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "GlobalDeclaration{" +
                "name='" + name + '\'' +
                ", initializer=" + initializer +
                '}';
    }
}