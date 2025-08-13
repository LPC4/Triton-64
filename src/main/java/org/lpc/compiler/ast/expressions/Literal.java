package org.lpc.compiler.ast.expressions;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.ast.AstVisitor;

@Getter
@ToString
public class Literal<V> implements Expression {
    private final V value;
    private final Type type;

    private Literal(V value, Type type) {
        this.value = value;
        this.type = type;
    }

    public static Literal<Byte> ofByte(byte value) {
        return new Literal<>(value, PrimitiveType.BYTE);
    }

    public static Literal<Integer> ofInt(int value) {
        return new Literal<>(value, PrimitiveType.INT);
    }

    public static Literal<Long> ofLong(long value) {
        return new Literal<>(value, PrimitiveType.LONG);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
