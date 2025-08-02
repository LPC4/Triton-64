package org.lpc.compiler.ast;

import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.statements.*;

public interface AstVisitor<T> {
    // Program and function definitions
    T visit(Program program);
    T visit(FunctionDef functionDef);

    // Statements
    T visit(ReturnStatement returnStatement);
    T visit(IfStatement ifStatement);
    T visit(WhileStatement whileStatement);
    T visit(Declaration declaration);
    T visit(AssignmentStatement assignmentStatement);
    T visit(ExpressionStatement expressionStatement);

    // Expressions
    T visit(BinaryOp binaryOp);
    T visit(UnaryOp unaryOp);
    T visit(FunctionCall functionCall);
    T visit(Variable variable);
    T visit(IntegerLiteral integerLiteral);
}