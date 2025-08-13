package org.lpc.compiler.types;

import lombok.Getter;


public record PointerType(@Getter Type elementType) implements Type {
    @Override
    public boolean isPtr() {
        return true;
    }

    @Override
    public int getSize() {
        return PrimitiveType.LONG.getSize();
    }

    @Override
    public PointerType asPointer() {
        return this;
    }
}