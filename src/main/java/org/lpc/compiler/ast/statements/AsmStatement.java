package org.lpc.compiler.ast.statements;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;

import java.util.List;

@Getter
@ToString
public class AsmStatement implements Statement {
    private final List<String> asmCode;

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
