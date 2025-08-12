package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class Variable implements Expression {
    private final String name;
    private final VariableType type;

    public Variable(String name, VariableType type) {
        this.name = name;
        this.type = type;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
