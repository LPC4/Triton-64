package org.lpc.compiler.ast.statements;

import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.Statement;

import java.util.List;

public class AsmStatement extends Statement {
    public final List<String> asmCode;

    public AsmStatement(String asmCode) {
        this.asmCode = asmCode.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
