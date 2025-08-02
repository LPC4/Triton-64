package org.lpc.compiler.ast.statements;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

import java.util.List;

@ToString
public class IfStatement extends Statement {
    public final Expression condition;
    public final List<Statement> thenBranch;
    public final List<Statement> elseBranch; // null if no else
    public IfStatement(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
