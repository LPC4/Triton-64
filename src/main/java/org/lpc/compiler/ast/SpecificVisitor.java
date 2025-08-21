package org.lpc.compiler.ast;

import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.StructDef;
import org.lpc.compiler.ast.statements.*;

/**
 * A visitor interface with all methods defaulted to null.
 * Extend and override only the methods you need.
 */
public interface SpecificVisitor<T> extends AstVisitor<T> {
    // Parent

    default T visit(Program program) {
        return null;
    }

    default T visit(FunctionDef functionDef) {
        return null;
    }

    default T visit(StructDef structDef) {
        return null;
    }

    // Statements

    default T visit(ReturnStatement returnStatement) {
        return null;
    }

    default T visit(IfStatement ifStatement) {
        return null;
    }

    default T visit(WhileStatement whileStatement) {
        return null;
    }

    default T visit(Declaration declaration) {
        return null;
    }

    default T visit(AssignmentStatement assignmentStatement) {
        return null;
    }

    default T visit(ExpressionStatement expressionStatement) {
        return null;
    }

    default T visit(AsmStatement asmStatement) {
        return null;
    }

    default T visit(GlobalDeclaration globalDeclaration) {
        return null;
    }

    // Expressions

    default T visit(BinaryOp binaryOp) {
        return null;
    }

    default T visit(UnaryOp unaryOp) {
        return null;
    }

    default T visit(FunctionCall functionCall) {
        return null;
    }

    default T visit(Variable variable) {
        return null;
    }

    default T visit(Literal<?> literal) {
        return null;
    }

    default T visit(Dereference dereference) {
        return null;
    }

    default T visit(ArrayLiteral arrayLiteral) {
        return null;
    }

    default T visit(ArrayIndex arrayIndex) {
        return null;
    }

    default T visit(TypeConversion typeConversion) {
        return null;
    }

    default T visit(StructFieldAccess structAccess) {
        return null;
    }
}