package org.lpc.compiler.types;

import org.lpc.compiler.ast.expressions.*;

public sealed interface Type permits PointerType, PrimitiveType, StructType {
    int getSize();

    default boolean isPrimitive() { return false; }
    default boolean isPtr() { return false; }
    default boolean isStruct() { return false; }

    default PrimitiveType asPrimitive() {
        throw new UnsupportedOperationException("Not a primitive type");
    }

    default PointerType asPointer() {
        throw new UnsupportedOperationException("Not a pointer type");
    }

    default StructType asStruct() {
        throw new UnsupportedOperationException("Not a struct type");
    }

    TypeResolver TYPE_RESOLVER = new TypeResolver();
    static Type getExpressionType(Expression expression) {
        return expression.accept(TYPE_RESOLVER);
    }
}
