package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;

import java.util.List;

@Getter
@ToString
public class FunctionCall implements Expression {
    private final String name;
    private final List<Expression> arguments;

    public FunctionCall(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
