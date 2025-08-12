package org.lpc.compiler;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.*;
import org.lpc.compiler.codegen.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Generates assembly code from the parsed Abstract Syntax Tree (AST).
 *
 * This class implements the Visitor pattern to traverse the AST and generate
 * corresponding assembly instructions using various specialized managers
 * for different aspects of code generation.
 */
public final class CodeGenerator implements AstVisitor<String> {

    // Core dependencies
    private final Program program;
    private final CodeGenContext context;
    private final InstructionEmitter emitter;

    // Specialized managers
    private final RegisterManager registerManager;
    private final StackManager stackManager;
    private final ConditionalGenerator conditionalGenerator;
    private final FunctionManager functionManager;
    private final GlobalManager globalManager;

    // State tracking
    private String currentFunctionEndLabel;

    public CodeGenerator(final Parser parser) {
        Objects.requireNonNull(parser, "Parser cannot be null");

        this.program = parser.parse();
        this.context = new CodeGenContext();
        this.emitter = new InstructionEmitter(context);

        // Initialize managers
        this.registerManager = new RegisterManager();
        this.stackManager = new StackManager(emitter, registerManager);
        this.conditionalGenerator = new ConditionalGenerator(context, emitter, registerManager);
        this.functionManager = new FunctionManager(context, emitter, registerManager, stackManager);
        this.globalManager = new GlobalManager(emitter, registerManager);
    }

    public List<String> generate() {
        try {
            program.accept(this);
            return context.getAssembly();
        } catch (final Exception e) {
            throw new CodeGenerationException("Failed to generate code for program", e);
        }
    }

    @Override
    public String visit(final Program program) {
        emitter.comment("Program entry point");
        emitter.label("_start");

        // Initialize global state
        globalManager.allocateGlobals(program.getGlobals());
        globalManager.initializeGlobals(program.getGlobals(), this);

        // Setup main execution
        emitter.instruction("JAL", "main", "ra");
        emitter.instruction("HLT");

        // Generate all function definitions
        generateFunctions(program.getFunctions());

        return null;
    }

    @Override
    public String visit(final FunctionDef functionDef) {
        final String functionName = functionDef.getName();

        try {
            currentFunctionEndLabel = context.generateLabel(functionName + "_end");

            functionManager.generateFunction(
                    functionName,
                    functionDef.getParameters(),
                    functionDef.getParameterTypes(),
                    functionDef.getBody(),
                    this::generateStatements,
                    currentFunctionEndLabel
            );

            return null;
        } catch (final Exception e) {
            throw new CodeGenerationException("Failed to generate function: " + functionName, e);
        } finally {
            currentFunctionEndLabel = null;
        }
    }

    @Override
    public String visit(final GlobalDeclaration globalDeclaration) {
        // Global declarations are handled in Program.visit()
        return null;
    }

    @Override
    public String visit(final ReturnStatement returnStatement) {
        emitter.comment("return statement");

        Optional.ofNullable(returnStatement.getValue())
                .ifPresent(value -> {
                    final String valueReg = value.accept(this);
                    emitter.instruction("MOV", "a0", valueReg);
                    registerManager.freeRegister(valueReg);
                });

        emitter.instruction("JMP", currentFunctionEndLabel);
        return null;
    }

    @Override
    public String visit(final IfStatement ifStatement) {
        emitter.comment("If statement");

        final String elseLabel = context.generateLabel("else");
        final String endLabel = context.generateLabel("endif");

        conditionalGenerator.generateConditionalJump(ifStatement.getCondition(), elseLabel, this);
        generateStatements(ifStatement.getThenBranch());

        if (hasElseBranch(ifStatement)) {
            emitter.instruction("JMP", endLabel);
            emitter.label(elseLabel);
            generateStatements(ifStatement.getElseBranch());
            emitter.label(endLabel);
        } else {
            emitter.label(elseLabel);
        }

        return null;
    }

    @Override
    public String visit(final WhileStatement whileStatement) {
        emitter.comment("While loop");

        final String loopLabel = context.generateLabel("loop");
        final String endLabel = context.generateLabel("endloop");

        emitter.label(loopLabel);
        conditionalGenerator.generateConditionalJump(whileStatement.getCondition(), endLabel, this);
        generateStatements(whileStatement.getBody());
        emitter.instruction("JMP", loopLabel);
        emitter.label(endLabel);

        return null;
    }

    @Override
    public String visit(final Declaration declaration) {
        emitter.comment("Declaration: " + declaration.getName());

        final int offset = stackManager.allocateVariable(declaration.getName(), declaration.getType());

        Optional.ofNullable(declaration.getInitializer())
                .ifPresent(initializer -> {
                    final String valueReg = initializer.accept(this);
                    stackManager.storeToStack(offset, valueReg, declaration.getType());
                    registerManager.freeRegister(valueReg);
                });

        return null;
    }

    @Override
    public String visit(final AssignmentStatement assignment) {
        final Expression target = assignment.getTarget();

        return switch (target) {
            case Variable var -> {
                handleVariableAssignment(var, assignment.getInitialValue());
                yield null;
            }
            case Dereference deref -> {
                handleDereferenceAssignment(deref, assignment.getInitialValue());
                yield null;
            }
            case ArrayIndex arrayIdx -> {
                handleArrayIndexAssignment(arrayIdx, assignment.getInitialValue());
                yield null;
            }
            case null -> throw new IllegalStateException("Assignment target cannot be null");
            default -> throw new IllegalStateException("Unsupported assignment target: " + target.getClass());
        };
    }

    @Override
    public String visit(final AsmStatement asmStatement) {
        emitter.comment("Inline assembly statement");
        asmStatement.getAsmCode()
                .stream()
                .map(String::trim)
                .forEach(emitter::instruction);
        return null;
    }

    @Override
    public String visit(final Variable variable) {
        final String variableName = variable.getName();

        if (globalManager.isGlobal(variableName)) {
            return globalManager.loadFromGlobal(variableName, variable.getType());
        }

        final int offset = stackManager.getVariableOffset(variableName);
        return stackManager.loadFromStack(offset, variable.getType());
    }

    @Override
    public String visit(final ExpressionStatement expressionStatement) {
        emitter.comment("Expression statement");

        final String resultReg = expressionStatement.getExpression().accept(this);
        if (resultReg != null) {
            registerManager.freeRegister(resultReg);
        }

        return null;
    }

    @Override
    public String visit(final BinaryOp binaryOp) {
        final BinaryOp.Op operator = binaryOp.getOp();

        // Handle comparison operators
        if (ConditionalGenerator.isComparisonOp(operator)) {
            return conditionalGenerator.generateComparisonResult(binaryOp, this);
        }

        // Handle logical operators
        if (ConditionalGenerator.isLogicalOp(operator)) {
            return conditionalGenerator.generateLogicalResult(binaryOp, this);
        }

        // Handle arithmetic/bitwise operations
        return generateArithmeticOperation(binaryOp);
    }

    @Override
    public String visit(final UnaryOp unaryOp) {
        emitter.comment("Unary operation: " + unaryOp.getOp());

        final String operandReg = unaryOp.getOperand().accept(this);
        final String resultReg = registerManager.allocateRegister();

        generateUnaryOperation(unaryOp.getOp(), resultReg, operandReg);

        registerManager.freeRegister(operandReg);
        return resultReg;
    }

    @Override
    public String visit(final FunctionCall functionCall) {
        emitter.comment("Function call: " + functionCall.getName());

        validateArgumentCount(functionCall);

        return functionManager.generateFunctionCall(
                functionCall.getName(),
                functionCall.getArguments(),
                this
        );
    }

    @Override
    public String visit(final Literal<?> literal) {
        final String reg = registerManager.allocateRegister();
        emitter.instruction("LDI", reg, String.valueOf(literal.getValue()));
        return reg;
    }

    @Override
    public String visit(final Dereference dereference) {
        final String addrReg = dereference.getAddress().accept(this);
        final String resultReg = registerManager.allocateRegister();

        emitter.instruction("LD", resultReg, addrReg);
        registerManager.freeRegister(addrReg);

        return resultReg;
    }

    @Override
    public String visit(final TypeConversion conversion) {
        emitter.comment("Type conversion to " + conversion.getTargetType());

        final String sourceReg = conversion.getExpression().accept(this);
        final String resultReg = registerManager.allocateRegister("conversion_result");

        final VariableType sourceType = getExpressionType(conversion.getExpression());
        final VariableType targetType = conversion.getTargetType();

        performTypeConversion(sourceType, targetType, sourceReg, resultReg);

        registerManager.freeRegister(sourceReg);
        return resultReg;
    }

    @Override
    public String visit(final ArrayLiteral arrayLiteral) {
        emitter.comment("Array literal with " + arrayLiteral.getElements().size() + " elements");

        if (arrayLiteral.getElements().isEmpty()) {
            final String resultReg = registerManager.allocateRegister();
            emitter.instruction("LDI", resultReg, "0");
            return resultReg;
        }

        throw new IllegalStateException(
                "Array literal must be used in assignment context. Use: var x = malloc(size); @x = [1,2,3];"
        );
    }

    @Override
    public String visit(final ArrayIndex arrayIndex) {
        emitter.comment("Array indexing");

        final String arrayReg = arrayIndex.getArray().accept(this);
        final String indexReg = arrayIndex.getIndex().accept(this);

        try {
            return performArrayIndexing(arrayReg, indexReg);
        } finally {
            registerManager.freeRegister(arrayReg);
            registerManager.freeRegister(indexReg);
        }
    }

    // Helper methods organized by functionality

    private void generateFunctions(final List<FunctionDef> functions) {
        functions.forEach(func -> {
            try {
                func.accept(this);
            } catch (final Exception e) {
                throw new CodeGenerationException("Failed to generate function: " + func.getName(), e);
            }
        });
    }

    private boolean hasElseBranch(final IfStatement ifStatement) {
        final List<Statement> elseBranch = ifStatement.getElseBranch();
        return elseBranch != null && !elseBranch.isEmpty();
    }

    private void handleVariableAssignment(final Variable var, final Expression value) {
        if (value instanceof ArrayLiteral arrayLit) {
            generateArrayAssignment(var, arrayLit);
        } else {
            final String valueReg = value.accept(this);

            if (globalManager.isGlobal(var.getName())) {
                emitter.comment("Assignment to global variable: " + var.getName());
                globalManager.storeToGlobal(var.getName(), valueReg, var.getType());
            } else {
                emitter.comment("Assignment to local variable: " + var.getName());
                final int offset = stackManager.getVariableOffset(var.getName());
                stackManager.storeToStack(offset, valueReg, var.getType());
            }

            registerManager.freeRegister(valueReg);
        }
    }

    private void handleDereferenceAssignment(final Dereference deref, final Expression value) {
        final String addrReg = deref.getAddress().accept(this);

        try {
            if (value instanceof ArrayLiteral arrayLit) {
                generateArrayToMemory(addrReg, arrayLit);
            } else {
                final String valueReg = value.accept(this);
                try {
                    emitter.comment("Assignment to dereferenced address at " + addrReg);
                    emitter.instruction("ST", addrReg, valueReg);
                } finally {
                    registerManager.freeRegister(valueReg);
                }
            }
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void handleArrayIndexAssignment(final ArrayIndex arrayIdx, final Expression value) {
        final String arrayReg = arrayIdx.getArray().accept(this);
        final String indexReg = arrayIdx.getIndex().accept(this);
        final String valueReg = value.accept(this);

        final String offsetReg = registerManager.allocateRegister();
        final String addrReg = registerManager.allocateRegister();

        try {
            emitter.comment("Array index assignment");

            // Calculate address: array + (index * 8)
            emitter.instruction("LDI", offsetReg, "8");
            emitter.instruction("MUL", offsetReg, indexReg, offsetReg);
            emitter.instruction("ADD", addrReg, arrayReg, offsetReg);
            emitter.instruction("ST", addrReg, valueReg);
        } finally {
            registerManager.freeRegister(arrayReg);
            registerManager.freeRegister(indexReg);
            registerManager.freeRegister(valueReg);
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(addrReg);
        }
    }

    private String generateArithmeticOperation(final BinaryOp binaryOp) {
        emitter.comment("Binary operation: " + binaryOp.getOp());

        final String leftReg = binaryOp.getLeft().accept(this);
        final String rightReg = binaryOp.getRight().accept(this);
        final String resultReg = registerManager.allocateRegister();

        try {
            generateBinaryOperation(binaryOp.getOp(), resultReg, leftReg, rightReg);
            return resultReg;
        } finally {
            registerManager.freeRegister(leftReg);
            registerManager.freeRegister(rightReg);
        }
    }

    private void generateUnaryOperation(final UnaryOp.Op operator, final String resultReg, final String operandReg) {
        switch (operator) {
            case NEG -> emitter.instruction("NEG", resultReg, operandReg);
            case NOT -> emitter.instruction("NOT", resultReg, operandReg);
            default -> throw new IllegalArgumentException("Unsupported unary operator: " + operator);
        }
    }

    private void validateArgumentCount(final FunctionCall functionCall) {
        if (functionCall.getArguments().size() > 7) {
            throw new IllegalArgumentException("Too many arguments for function: " + functionCall.getName());
        }
    }

    private void performTypeConversion(final VariableType sourceType, final VariableType targetType,
                                       final String sourceReg, final String resultReg) {

        final TypeConverter converter = new TypeConverter(emitter, registerManager);
        converter.convert(sourceType, targetType, sourceReg, resultReg);
    }

    private String performArrayIndexing(final String arrayReg, final String indexReg) {
        final String offsetReg = registerManager.allocateRegister();
        final String addrReg = registerManager.allocateRegister();
        final String resultReg = registerManager.allocateRegister();

        try {
            // Calculate address: array + (index * 8)
            emitter.instruction("LDI", offsetReg, "8");
            emitter.instruction("MUL", offsetReg, indexReg, offsetReg);
            emitter.instruction("ADD", addrReg, arrayReg, offsetReg);
            emitter.instruction("LD", resultReg, addrReg);

            return resultReg;
        } finally {
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(addrReg);
        }
    }

    private void generateArrayAssignment(final Variable var, final ArrayLiteral arrayLit) {
        emitter.comment("Array assignment to variable: " + var.getName());

        final String addrReg = globalManager.isGlobal(var.getName())
                ? globalManager.loadFromGlobal(var.getName(), VariableType.LONG)
                : stackManager.loadFromStack(stackManager.getVariableOffset(var.getName()), VariableType.LONG);

        try {
            generateArrayToMemory(addrReg, arrayLit);
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void generateArrayToMemory(final String baseAddrReg, final ArrayLiteral arrayLit) {
        emitter.comment("Storing array literal to memory at " + baseAddrReg);

        final String offsetReg = registerManager.allocateRegister();
        final String currentAddrReg = registerManager.allocateRegister();

        try {
            final List<Expression> elements = arrayLit.getElements();
            for (int i = 0; i < elements.size(); i++) {
                // Calculate address: base + (i * 8)
                emitter.instruction("LDI", offsetReg, String.valueOf(i * 8));
                emitter.instruction("ADD", currentAddrReg, baseAddrReg, offsetReg);

                final String elementReg = elements.get(i).accept(this);
                try {
                    emitter.instruction("ST", currentAddrReg, elementReg);
                } finally {
                    registerManager.freeRegister(elementReg);
                }
            }
        } finally {
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(currentAddrReg);
        }
    }

    private void generateStatements(final List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(this));
    }

    private void generateBinaryOperation(final BinaryOp.Op op, final String resultReg,
                                         final String leftReg, final String rightReg) {
        switch (op) {
            case ADD -> emitter.instruction("ADD", resultReg, leftReg, rightReg);
            case SUB -> emitter.instruction("SUB", resultReg, leftReg, rightReg);
            case MUL -> emitter.instruction("MUL", resultReg, leftReg, rightReg);
            case DIV -> emitter.instruction("DIV", resultReg, leftReg, rightReg);
            case AND -> emitter.instruction("AND", resultReg, leftReg, rightReg);
            case OR  -> emitter.instruction("OR", resultReg, leftReg, rightReg);
            case XOR -> emitter.instruction("XOR", resultReg, leftReg, rightReg);
            case SHL -> emitter.instruction("SHL", resultReg, leftReg, rightReg);
            case SHR -> emitter.instruction("SHR", resultReg, leftReg, rightReg);
            case SAR -> emitter.instruction("SAR", resultReg, leftReg, rightReg);
            default -> throw new IllegalArgumentException("Unsupported binary operator: " + op);
        }
    }

    private VariableType getExpressionType(final Expression expression) {
        return switch (expression) {
            case Variable var -> var.getType();
            case Literal<?> lit -> lit.getType();
            case TypeConversion typeConv -> typeConv.getTargetType();
            case BinaryOp binOp -> getExpressionType(binOp.getLeft());
            case null -> VariableType.LONG; // Default fallback
            default -> VariableType.LONG; // Default fallback
        };
    }

    /**
     * Functional interface for generating statement blocks.
     */
    @FunctionalInterface
    public interface StatementBlockGenerator {
        void generate(List<Statement> statements);
    }

    /**
     * Exception thrown when code generation fails.
     */
    public static final class CodeGenerationException extends RuntimeException {
        public CodeGenerationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Helper class for type conversions.
     */
    private static final class TypeConverter {
        private final InstructionEmitter emitter;
        private final RegisterManager registerManager;

        private TypeConverter(final InstructionEmitter emitter, final RegisterManager registerManager) {
            this.emitter = emitter;
            this.registerManager = registerManager;
        }

        private void convert(final VariableType sourceType, final VariableType targetType,
                             final String sourceReg, final String resultReg) {

            if (sourceType == VariableType.BYTE) {
                convertFromByte(targetType, sourceReg, resultReg);
            } else if (sourceType == VariableType.INT) {
                convertFromInt(targetType, sourceReg, resultReg);
            } else if (sourceType == VariableType.LONG) {
                convertFromLong(targetType, sourceReg, resultReg);
            } else {
                // Unknown or same type - just move
                emitter.move(resultReg, sourceReg);
            }
        }

        private void convertFromByte(final VariableType targetType, final String sourceReg, final String resultReg) {
            switch (targetType) {
                case INT -> {
                    emitter.comment("Converting BYTE to INT (sign extend 8→32)");
                    emitter.shiftLeft(resultReg, sourceReg, "24");
                    emitter.shiftArithmeticRight(resultReg, resultReg, "24");
                }
                case LONG -> {
                    emitter.comment("Converting BYTE to LONG (sign extend 8→64)");
                    emitter.shiftLeft(resultReg, sourceReg, "56");
                    emitter.shiftArithmeticRight(resultReg, resultReg, "56");
                }
                default -> {
                    emitter.comment("BYTE to BYTE (no-op)");
                    emitter.move(resultReg, sourceReg);
                }
            }
        }

        private void convertFromInt(final VariableType targetType, final String sourceReg, final String resultReg) {
            switch (targetType) {
                case LONG -> {
                    emitter.comment("Converting INT to LONG (sign extend 32→64)");
                    emitter.shiftLeft(resultReg, sourceReg, "32");
                    emitter.shiftArithmeticRight(resultReg, resultReg, "32");
                }
                case BYTE -> {
                    emitter.comment("Converting INT to BYTE (truncate 32→8)");
                    truncateWithMask(sourceReg, resultReg, 0xFF);
                }
                default -> {
                    emitter.comment("INT to INT (no-op)");
                    emitter.move(resultReg, sourceReg);
                }
            }
        }

        private void convertFromLong(final VariableType targetType, final String sourceReg, final String resultReg) {
            switch (targetType) {
                case BYTE -> {
                    emitter.comment("Converting LONG to BYTE (truncate 64→8)");
                    truncateWithMask(sourceReg, resultReg, 0xFF);
                }
                case INT -> {
                    emitter.comment("Converting LONG to INT (truncate 64→32)");
                    truncateWithMask(sourceReg, resultReg, 0xFFFFFFFFL);
                }
                default -> {
                    emitter.comment("LONG to LONG (no-op)");
                    emitter.move(resultReg, sourceReg);
                }
            }
        }

        private void truncateWithMask(final String sourceReg, final String resultReg, final long mask) {
            final String maskReg = registerManager.allocateRegister("mask");
            try {
                emitter.loadImmediate(maskReg, mask);
                emitter.and(resultReg, sourceReg, maskReg);
            } finally {
                registerManager.freeRegister(maskReg);
            }
        }
    }
}