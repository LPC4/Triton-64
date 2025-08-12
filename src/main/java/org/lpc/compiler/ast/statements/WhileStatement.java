package org.lpc.compiler.ast.statements;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.Expression;

import java.util.List;

@Getter
@ToString
public class WhileStatement implements Statement {
    private final Expression condition;
    private final List<Statement> body;

    public WhileStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
