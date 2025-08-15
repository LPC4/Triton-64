package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.types.Type;

@Getter
@ToString
public class StructFieldAccess implements Expression {
    private final Expression base;
    private final String fieldName;
    @Setter private Type fieldType;  // always set by the visitor
    @Setter private int fieldOffset;

    public StructFieldAccess(Expression base, String fieldName) {
        this.base = base;
        this.fieldName = fieldName;
    }

    // this was returning null for like 4 hours before I realised
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}