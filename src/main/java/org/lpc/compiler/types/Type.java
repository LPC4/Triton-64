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
    default StructType asStruct() { throw new UnsupportedOperationException("Not a struct type");}

    static Type getExpressionType(Expression expression) {
        if (expression instanceof Variable var) {
            return var.getType();
        }
        if (expression instanceof Literal<?> lit) {
            return lit.getType();
        }
        if (expression instanceof TypeConversion typeConv) {
            return typeConv.getTargetType();
        }
        if (expression instanceof BinaryOp binOp) {
            return getExpressionType(binOp.getLeft());
        }
        if (expression instanceof Dereference deref) {
            return deref.getType();
        }
        if (expression instanceof ArrayIndex arrayIdx) {
            Type arrayType = getExpressionType(arrayIdx.getArray());
            if (arrayType.isPtr()) {
                return arrayType.asPointer().getElementType();
            }
            return arrayType; // If it's not a pointer, return the array type directly
        }
        if (expression instanceof StructFieldAccess fieldAccess) {
            // Base must be pointer to struct
            Type baseType = getExpressionType(fieldAccess.getBase());
            if (baseType == null || !baseType.isPtr())
                throw new RuntimeException("Base of struct field access must be a pointer to a struct");

            Type structType = baseType.asPointer().getElementType();
            if (structType.isStruct()) {
                StructType struct = structType.asStruct();
                // Find field type in struct definition
                for (StructType.Field field : struct.getFields()) {
                    if (field.name().equals(fieldAccess.getFieldName())) {
                        return field.type();
                    }
                }
            }
            throw new RuntimeException("Struct field not found: " + fieldAccess.getFieldName());
        }
        if (expression instanceof FunctionCall funcCall) {
            return funcCall.getReturnType();
        }
        throw new RuntimeException("Unknown expression type: " + expression.getClass().getSimpleName());
    }
}
