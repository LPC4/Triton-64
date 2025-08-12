package org.lpc.compiler.ast.statements;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.Expression;

import java.util.List;

@Getter
@ToString
public class IfStatement implements Statement {
    private final Expression condition;
    private final List<Statement> thenBranch;
    private final List<Statement> elseBranch; // null if no else

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
