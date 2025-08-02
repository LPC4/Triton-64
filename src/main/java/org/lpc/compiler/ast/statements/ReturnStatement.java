
package org.lpc.compiler.ast.statements;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;

@ToString
public class ReturnStatement extends Statement {
    public final Expression value;
    public ReturnStatement(Expression value) {
        this.value = value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
