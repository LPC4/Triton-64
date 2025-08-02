package org.lpc.compiler.ast.statements;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

@ToString
public class Declaration extends Statement {
    public final String name;
    public final Expression initializer;

    public Declaration(String name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
