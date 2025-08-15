package org.lpc.compiler;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.StructDef;
import org.lpc.compiler.context_managers.*;
import org.lpc.compiler.ast.statements.*;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.generators.*;
import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.StructType;
import org.lpc.compiler.types.Type;

import java.util.List;
import java.util.Objects;

/**
 * Main entry point for code generation.
 * Delegates to specialized generators for different aspects of code generation.
 */
@SuppressWarnings("FieldCanBeLocal")
public final class CodeGenerator implements AstVisitor<String> {

    // Core dependencies
    private final Program program;
    private final ContextManager context;
    private final InstructionGenerator emitter;

    // Specialized managers
    private final RegisterManager registerManager;
    private final StackManager stackManager;
    private final ConditionalGenerator conditionalGenerator;
    private final FunctionGenerator functionGenerator;
    private final GlobalManager globalManager;

    // Specialized generators
    private final ProgramGenerator programGenerator;
    private final StatementGenerator statementGenerator;
    private final ExpressionGenerator expressionGenerator;

    public CodeGenerator(final Parser parser) {
        Objects.requireNonNull(parser, "Parser cannot be null");

        this.program = parser.parse();
        this.context = new ContextManager();
        this.emitter = new InstructionGenerator(context);

        // Initialize managers
        this.registerManager = new RegisterManager();
        this.stackManager = new StackManager(emitter, registerManager);
        this.conditionalGenerator = new ConditionalGenerator(context, emitter, registerManager);
        this.functionGenerator = new FunctionGenerator(context, emitter, registerManager, stackManager);
        this.globalManager = new GlobalManager(emitter, registerManager);


        // Initialize generators
        this.programGenerator = new ProgramGenerator(
                emitter, functionGenerator, globalManager, context,
                this);
        this.statementGenerator = new StatementGenerator(
                emitter, registerManager, stackManager, conditionalGenerator,
                globalManager, context,  this);
        this.expressionGenerator = new ExpressionGenerator(
                emitter, registerManager, stackManager, conditionalGenerator,
                functionGenerator, globalManager, this);
    }

    public List<String> generate() {
        try {
            program.accept(this);
            return context.getAssembly();
        } catch (Exception e) {
            throw new CodeGenerationException("Failed to generate code for program", e);
        }
    }

    @Override
    public String visit(Program program) {
        return programGenerator.visitProgram(program);
    }

    @Override
    public String visit(FunctionDef functionDef) {
        return programGenerator.visitFunctionDef(functionDef);
    }

    @Override
    public String visit(StructDef structDef) {
        return programGenerator.visitStructDef(structDef);
    }

    @Override
    public String visit(GlobalDeclaration globalDeclaration) {
        return programGenerator.visitGlobalDeclaration(globalDeclaration);
    }

    // Statement visitors
    @Override
    public String visit(ReturnStatement returnStatement) {
        return statementGenerator.visitReturnStatement(returnStatement);
    }

    @Override
    public String visit(IfStatement ifStatement) {
        return statementGenerator.visitIfStatement(ifStatement);
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        return statementGenerator.visitWhileStatement(whileStatement);
    }

    @Override
    public String visit(Declaration declaration) {
        return statementGenerator.visitDeclaration(declaration);
    }

    @Override
    public String visit(AssignmentStatement assignment) {
        return statementGenerator.visitAssignmentStatement(assignment);
    }

    @Override
    public String visit(AsmStatement asmStatement) {
        return statementGenerator.visitAsmStatement(asmStatement);
    }

    @Override
    public String visit(ExpressionStatement expressionStatement) {
        return statementGenerator.visitExpressionStatement(expressionStatement);
    }

    // Expression visitors
    @Override
    public String visit(BinaryOp binaryOp) {
        return expressionGenerator.visitBinaryOp(binaryOp);
    }

    @Override
    public String visit(UnaryOp unaryOp) {
        return expressionGenerator.visitUnaryOp(unaryOp);
    }

    @Override
    public String visit(FunctionCall functionCall) {
        return expressionGenerator.visitFunctionCall(functionCall);
    }

    @Override
    public String visit(Literal<?> literal) {
        return expressionGenerator.visitLiteral(literal);
    }

    @Override
    public String visit(Variable variable) {
        return expressionGenerator.visitVariable(variable);
    }

    @Override
    public String visit(Dereference dereference) {
        return expressionGenerator.visitDereference(dereference);
    }

    @Override
    public String visit(TypeConversion conversion) {
        return expressionGenerator.visitTypeConversion(conversion);
    }

    @Override
    public String visit(StructFieldAccess structAccess) {
        return expressionGenerator.visitStructFieldAccess(structAccess);
    }

    @Override
    public String visit(ArrayLiteral arrayLiteral) {
        return expressionGenerator.visitArrayLiteral(arrayLiteral);
    }

    @Override
    public String visit(ArrayIndex arrayIndex) {
        return expressionGenerator.visitArrayIndex(arrayIndex);
    }

    public static final class CodeGenerationException extends RuntimeException {
        public CodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}