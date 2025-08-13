package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.VariableType;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class Literal<V> implements Expression {
    private final V value;
    private final VariableType type;

    private Literal(V value, VariableType type) {
        this.value = value;
        this.type = type;
    }

    public static Literal<Byte> ofByte(byte value) {
        return new Literal<>(value, VariableType.BYTE);
    }

    public static Literal<Integer> ofInt(int value) {
        return new Literal<>(value, VariableType.INT);
    }

    public static Literal<Long> ofLong(long value) {
        return new Literal<>(value, VariableType.LONG);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
