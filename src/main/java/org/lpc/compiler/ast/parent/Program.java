package org.lpc.compiler.ast.parent;

import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;

import java.util.List;

public class Program extends AstNode {
    public final List<FunctionDef> functions;
    public Program(List<FunctionDef> functions) {
        this.functions = functions;
    }

    @Override
    public String toString() {
        // print nicely with indentation
        StringBuilder sb = new StringBuilder();
        sb.append("Program:\n");
        for (FunctionDef function : functions) {
            sb.append("  ").append(function.name).append(":\n");
            for (Statement statement : function.body) {
                sb.append("    ").append(statement.toString().replace("\n", "\n    ")).append("\n");
            }
        }
        return sb.toString();
    }
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

