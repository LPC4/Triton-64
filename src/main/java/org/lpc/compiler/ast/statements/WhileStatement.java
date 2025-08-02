package org.lpc.compiler.ast.statements;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

import java.util.List;

@ToString
public class WhileStatement extends Statement {
    public final Expression condition;
    public final List<Statement> body;
    public WhileStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
