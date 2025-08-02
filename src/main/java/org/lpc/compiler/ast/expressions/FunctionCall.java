package org.lpc.compiler.ast.expressions;

import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Expression;

import java.util.List;

@ToString
public class FunctionCall extends Expression {
    public final String name;
    public final List<Expression> arguments;
    public FunctionCall(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
