package org.lpc.compiler.types;

import lombok.Getter;

@Getter
public enum PrimitiveType implements Type {
    BYTE(1),
    INT(4),
    LONG(8);

    private final int size;

    PrimitiveType(int size) {
        this.size = size;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public PrimitiveType asPrimitive() {
        return this;
    }
}
