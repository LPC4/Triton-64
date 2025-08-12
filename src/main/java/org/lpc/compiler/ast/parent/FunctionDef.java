package org.lpc.compiler.ast.parent;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.statements.Statement;

import java.util.List;

@ToString
@Getter
public class FunctionDef implements AstNode {
    private final String name;
    private final List<String> parameters;
    private final List<VariableType> parameterTypes;
    private final List<Statement> body;
    private final VariableType returnType;

    public FunctionDef(String name, List<String> parameters, List<Statement> body, List<VariableType> parameterTypes, VariableType returnType) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
