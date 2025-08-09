package org.lpc.compiler.ast.statements;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

public class GlobalDeclaration extends Statement {
    public final String name;
    public final Expression initializer;

    public GlobalDeclaration(String name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
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