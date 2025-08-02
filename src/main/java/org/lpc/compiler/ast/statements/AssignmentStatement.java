package org.lpc.compiler.ast.statements;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

@ToString
public class AssignmentStatement extends Statement {
    public final String name;
    public final Expression initialValue;
    public AssignmentStatement(String name, Expression initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
