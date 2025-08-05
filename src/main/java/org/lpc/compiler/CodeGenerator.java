package org.lpc.compiler;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.*;
import org.lpc.compiler.codegen.*;

import java.util.List;

/**
 * Main code generator that coordinates the compilation process.
 * Refactored to separate concerns and improve maintainability.
 */
public class CodeGenerator implements AstVisitor<String> {
    private final CodeGenContext ctx;
    private final Program program;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;
    private final ConditionalGenerator conditionalGenerator;
    private final FunctionManager functionManager;

    // Current compilation state
    private String currentFunctionEndLabel;

    public CodeGenerator(Parser parser) {
        this.program = parser.parse();
        this.ctx = new CodeGenContext();
        this.emitter = new InstructionEmitter(ctx);
        this.registerManager = new RegisterManager(ctx);
        this.stackManager = new StackManager(ctx, emitter, registerManager);
        this.conditionalGenerator = new ConditionalGenerator(ctx, emitter, registerManager);
        this.functionManager = new FunctionManager(ctx, emitter, registerManager, stackManager);
    }

    public List<String> generate() {
        try {
            program.accept(this);
            return ctx.getAssembly();
        } catch (Exception e) {
            throw new CodeGenerationException("Failed to generate code for program", e);
        }
    }

    @Override
    public String visit(Program program) {
        // JAL to main, HLT after return
        emitter.comment("Program entry point");
        emitter.label("_start");
        emitter.instruction("JAL", "main", "ra");
        emitter.instruction("HLT");

        // Generate all functions
        program.functions.forEach(func -> {
            try {
                func.accept(this);
            } catch (Exception e) {
                throw new CodeGenerationException("Failed to generate function: " + func.name, e);
            }
        });
        return null;
    }

    @Override
    public String visit(FunctionDef functionDef) {
        emitter.comment("Function: " + functionDef.name);

        try {
            currentFunctionEndLabel = ctx.generateLabel(functionDef.name + "_end");

            functionManager.generateFunction(
                    functionDef.name,
                    functionDef.parameters,
                    functionDef.body,
                    this::generateStatements,
                    currentFunctionEndLabel
            );

            return null;
        } catch (Exception e) {
            throw new CodeGenerationException("Failed to generate function: " + functionDef.name, e);
        } finally {
            currentFunctionEndLabel = null;
        }
    }

    @Override
    public String visit(ReturnStatement returnStatement) {
        emitter.comment("return statement");

        if (returnStatement.value != null) {
            String valueReg = returnStatement.value.accept(this);
            emitter.instruction("MOV", "a0", valueReg);
            registerManager.freeRegister(valueReg);
        }

        emitter.instruction("JMP", currentFunctionEndLabel);
        return null;
    }

    @Override
    public String visit(IfStatement ifStatement) {
        emitter.comment("If statement");

        String elseLabel = ctx.generateLabel("else");
        String endLabel = ctx.generateLabel("endif");

        conditionalGenerator.generateConditionalJump(ifStatement.condition, elseLabel, this);
        generateStatements(ifStatement.thenBranch);

        if (ifStatement.elseBranch != null && !ifStatement.elseBranch.isEmpty()) {
            emitter.instruction("JMP", endLabel);
            emitter.label(elseLabel);
            generateStatements(ifStatement.elseBranch);
            emitter.label(endLabel);
        } else {
            emitter.label(elseLabel);
        }

        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        emitter.comment("While loop");

        String loopLabel = ctx.generateLabel("loop");
        String endLabel = ctx.generateLabel("endloop");

        emitter.label(loopLabel);
        conditionalGenerator.generateConditionalJump(whileStatement.condition, endLabel, this);
        generateStatements(whileStatement.body);
        emitter.instruction("JMP", loopLabel);
        emitter.label(endLabel);

        return null;
    }

    @Override
    public String visit(Declaration declaration) {
        emitter.comment("Declaration: " + declaration.name);

        // Allocate variable space in frame
        int offset = stackManager.allocateVariable(declaration.name);

        if (declaration.initializer != null) {
            String valueReg = declaration.initializer.accept(this);
            stackManager.storeToStack(offset, valueReg);
            registerManager.freeRegister(valueReg);
        }

        return null;
    }

    @Override
    public String visit(AssignmentStatement assignment) {
        emitter.comment("Assignment: " + assignment.name);

        int offset = stackManager.getVariableOffset(assignment.name);
        String valueReg = assignment.initialValue.accept(this);
        stackManager.storeToStack(offset, valueReg);
        registerManager.freeRegister(valueReg);

        return null;
    }

    @Override
    public String visit(Variable variable) {
        int offset = stackManager.getVariableOffset(variable.name);
        return stackManager.loadFromStack(offset);
    }

    @Override
    public String visit(ExpressionStatement expressionStatement) {
        emitter.comment("Expression statement");

        String resultReg = expressionStatement.expression.accept(this);
        if (resultReg != null) {
            registerManager.freeRegister(resultReg);
        }

        return null;
    }

    @Override
    public String visit(BinaryOp binaryOp) {
        if (ConditionalGenerator.isComparisonOp(binaryOp.op)) {
            throw new IllegalStateException("Comparison operators should be handled in conditional contexts");
        }

        emitter.comment("Binary operation: " + binaryOp.op);

        String leftReg = binaryOp.left.accept(this);
        String rightReg = binaryOp.right.accept(this);
        String resultReg = registerManager.allocateRegister();

        generateBinaryOperation(binaryOp.op, resultReg, leftReg, rightReg);

        registerManager.freeRegister(leftReg);
        registerManager.freeRegister(rightReg);

        return resultReg;
    }

    @Override
    public String visit(UnaryOp unaryOp) {
        emitter.comment("Unary operation: " + unaryOp.op);

        String operandReg = unaryOp.operand.accept(this);
        String resultReg = registerManager.allocateRegister();

        switch (unaryOp.op) {
            case NEG -> emitter.instruction("NEG", resultReg, operandReg);
            case NOT -> emitter.instruction("NOT", resultReg, operandReg);
            default -> throw new IllegalArgumentException("Unsupported unary operator: " + unaryOp.op);
        }

        registerManager.freeRegister(operandReg);
        return resultReg;
    }

    @Override
    public String visit(FunctionCall functionCall) {
        emitter.comment("Function call: " + functionCall.name);

        if (functionCall.arguments.size() > 7) {
            throw new IllegalArgumentException("Too many arguments for function: " + functionCall.name);
        }

        return functionManager.generateFunctionCall(functionCall.name, functionCall.arguments, this);
    }

    @Override
    public String visit(IntegerLiteral integerLiteral) {
        String reg = registerManager.allocateRegister();
        emitter.instruction("LDI", reg, String.valueOf(integerLiteral.value));
        return reg;
    }

    // Helper methods
    private void generateStatements(List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(this));
    }

    private void generateBinaryOperation(BinaryOp.Op op, String resultReg, String leftReg, String rightReg) {
        switch (op) {
            case ADD -> emitter.instruction("ADD", resultReg, leftReg, rightReg);
            case SUB -> emitter.instruction("SUB", resultReg, leftReg, rightReg);
            case MUL -> emitter.instruction("MUL", resultReg, leftReg, rightReg);
            case DIV -> emitter.instruction("DIV", resultReg, leftReg, rightReg);
            case AND -> emitter.instruction("AND", resultReg, leftReg, rightReg);
            case OR  -> emitter.instruction("OR",  resultReg, leftReg, rightReg);
            case XOR -> emitter.instruction("XOR", resultReg, leftReg, rightReg);
            case SHL -> emitter.instruction("SHL", resultReg, leftReg, rightReg);
            case SHR -> emitter.instruction("SHR", resultReg, leftReg, rightReg);
            case SAR -> emitter.instruction("SAR", resultReg, leftReg, rightReg);
            default -> throw new IllegalArgumentException("Unsupported binary operator: " + op);
        }
    }

    // Functional interface for generating statement blocks
    @FunctionalInterface
    public interface StatementBlockGenerator {
        void generate(List<Statement> statements);
    }

    public static class CodeGenerationException extends RuntimeException {
        public CodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}