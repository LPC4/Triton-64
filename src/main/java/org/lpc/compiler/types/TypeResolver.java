package org.lpc.compiler.types;

import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.*;
import org.lpc.compiler.ast.statements.*;

public class TypeResolver implements AstVisitor<Type> {

    // Expression visitors
    @Override
    public Type visit(ArrayIndex arrayIndex) {
        Type arrayType = arrayIndex.getArray().accept(this);
        if (arrayType.isPtr()) {
            return arrayType.asPointer().getElementType();
        }
        return arrayType; // If it's not a pointer, return the array type directly
    }

    @Override
    public Type visit(ArrayLiteral arrayLiteral) {
        return new PointerType(arrayLiteral.getType());
    }

    @Override
    public Type visit(BinaryOp binaryOp) {
        return binaryOp.getLeft().accept(this);
    }

    @Override
    public Type visit(Dereference dereference) {
        return dereference.getType();
    }

    @Override
    public Type visit(FunctionCall functionCall) {
        return functionCall.getReturnType();
    }

    @Override
    public Type visit(Literal<?> literal) {
        return literal.getType();
    }

    @Override
    public Type visit(StructFieldAccess fieldAccess) {
        // Base must be pointer to struct
        Type baseType = fieldAccess.getBase().accept(this);
        if (baseType == null || !baseType.isPtr()) {
            throw new RuntimeException("Base of struct field access must be a pointer to a struct");
        }

        Type structType = baseType.asPointer().getElementType();
        if (!structType.isStruct())
            throw new RuntimeException("Base of struct field access must be a struct type");

        StructType struct = structType.asStruct();
        // Find field type in struct definition
        for (StructType.Field field : struct.getFields()) {
            if (field.name().equals(fieldAccess.getFieldName())) {
                return field.type();
            }
        }

        throw new RuntimeException("Struct field not found: " + fieldAccess.getFieldName());
    }

    @Override
    public Type visit(TypeConversion typeConversion) {
        return typeConversion.getTargetType();
    }

    @Override
    public Type visit(UnaryOp unaryOp) {
        // Handle unary operations - might need different logic based on operator
        return unaryOp.getOperand().accept(this);
    }

    @Override
    public Type visit(Variable variable) {
        return variable.getType();
    }

    // Parent visitors - these are not expressions, so they throw exceptions
    @Override
    public Type visit(FunctionDef functionDef) {
        throw new UnsupportedOperationException("Cannot get type of FunctionDef");
    }

    @Override
    public Type visit(Program program) {
        throw new UnsupportedOperationException("Cannot get type of Program");
    }

    @Override
    public Type visit(StructDef structDef) {
        throw new UnsupportedOperationException("Cannot get type of StructDef");
    }

    // Statement visitors - these are not expressions, so they throw exceptions
    @Override
    public Type visit(AsmStatement asmStatement) {
        throw new UnsupportedOperationException("Cannot get type of AsmStatement");
    }

    @Override
    public Type visit(AssignmentStatement assignmentStatement) {
        throw new UnsupportedOperationException("Cannot get type of AssignmentStatement");
    }

    @Override
    public Type visit(Declaration declaration) {
        throw new UnsupportedOperationException("Cannot get type of Declaration");
    }

    @Override
    public Type visit(ExpressionStatement expressionStatement) {
        throw new UnsupportedOperationException("Cannot get type of ExpressionStatement");
    }

    @Override
    public Type visit(GlobalDeclaration globalDeclaration) {
        throw new UnsupportedOperationException("Cannot get type of GlobalDeclaration");
    }

    @Override
    public Type visit(IfStatement ifStatement) {
        throw new UnsupportedOperationException("Cannot get type of IfStatement");
    }

    @Override
    public Type visit(ReturnStatement returnStatement) {
        throw new UnsupportedOperationException("Cannot get type of ReturnStatement");
    }

    @Override
    public Type visit(WhileStatement whileStatement) {
        throw new UnsupportedOperationException("Cannot get type of WhileStatement");
    }
}