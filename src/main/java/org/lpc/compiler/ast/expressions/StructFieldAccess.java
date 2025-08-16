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
    private final Type fieldType;
    private final int fieldOffset;

    public StructFieldAccess(Expression base, String fieldName, Type fieldType, int fieldOffset) {
        this.base = base;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldOffset = fieldOffset;
    }

    // this was returning null for like 4 hours before I realised
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}