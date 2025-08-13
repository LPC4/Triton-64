package org.lpc.compiler.types;

public sealed interface Type permits PrimitiveType, PointerType {
    int getSize();
    default boolean isPrimitive() { return false; }
    default boolean isPtr() { return false; }

    default PrimitiveType asPrimitive() {
        throw new UnsupportedOperationException("Not a primitive type");
    }
    default PointerType asPointer() {
        throw new UnsupportedOperationException("Not a pointer type");
    }
}
