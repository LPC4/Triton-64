package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.types.Type;

import javax.annotation.Nullable;
import java.util.List;

@Getter
@ToString
public class FunctionCall implements Expression {
    private final String name;
    private final List<Expression> arguments;
    private final Type returnType;

    public FunctionCall(String name, List<Expression> arguments, @Nullable Type returnType) {
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
