package org.lpc.compiler.ast.statements;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

@ToString
@Getter
public class AssignmentStatement extends Statement {
    private final Expression target;
    private final Expression initialValue;

    public AssignmentStatement(Expression target, Expression initialValue) {
        this.target = target;
        this.initialValue = initialValue;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
