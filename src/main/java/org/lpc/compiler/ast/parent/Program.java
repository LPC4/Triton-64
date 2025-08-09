package org.lpc.compiler.ast.parent;

import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.statements.GlobalDeclaration;

import java.util.List;

public class Program extends AstNode {
    public final List<GlobalDeclaration> globals;
    public final List<FunctionDef> functions;

    public Program(List<GlobalDeclaration> globals, List<FunctionDef> functions) {
        this.globals = globals;
        this.functions = functions;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Program{\n");
        if (!globals.isEmpty()) {
            sb.append("  globals=[\n");
            for (GlobalDeclaration global : globals) {
                sb.append("    ").append(global).append("\n");
            }
            sb.append("  ],\n");
        }
        sb.append("  functions=[\n");
        for (FunctionDef func : functions) {
            sb.append("    ").append(func).append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
}