package org.lpc.compiler.ast.parent;

import lombok.ToString;
import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;

import java.util.List;

@ToString
public class FunctionDef extends AstNode {
    public final String name;
    public final List<String> parameters;
    public final List<Statement> body;
    public FunctionDef(String name, List<String> parameters, List<Statement> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
