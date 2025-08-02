package org.lpc.compiler;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.*;

import java.util.List;

public class CodeGenerator implements AstVisitor<Void> {
    CodeGenContext context;

    CodeGenerator (CodeGenContext context) {
        this.context = context;
    }

    @Override
    public Void visit(Program program) {
        for (FunctionDef functionDef : program.functions) {
            functionDef.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionDef functionDef) {
        List<Statement> body = functionDef.body;
        List<String> parameters = functionDef.parameters;
        String name = functionDef.name;

        // Here you would generate code for the function definition

        return null;
    }

    @Override
    public Void visit(ReturnStatement returnStatement) {
        return null;
    }

    @Override
    public Void visit(IfStatement ifStatement) {
        return null;
    }

    @Override
    public Void visit(WhileStatement whileStatement) {
        return null;
    }

    @Override
    public Void visit(Declaration declaration) {
        return null;
    }

    @Override
    public Void visit(AssignmentStatement assignmentStatement) {
        return null;
    }

    @Override
    public Void visit(ExpressionStatement expressionStatement) {
        return null;
    }

    @Override
    public Void visit(BinaryOp binaryOp) {
        return null;
    }

    @Override
    public Void visit(UnaryOp unaryOp) {
        return null;
    }

    @Override
    public Void visit(FunctionCall functionCall) {
        return null;
    }

    @Override
    public Void visit(Variable variable) {
        return null;
    }

    @Override
    public Void visit(IntegerLiteral integerLiteral) {
        return null;
    }
}
