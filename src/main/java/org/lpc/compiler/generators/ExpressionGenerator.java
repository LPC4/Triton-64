package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.context_managers.*;

/**
 * Handles code generation for expressions.
 */
public class ExpressionGenerator {
    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;
    private final ConditionalGenerator conditionalGenerator;
    private final FunctionGenerator functionGenerator;
    private final GlobalManager globalManager;

    // Reference to the main generator for recursive calls
    private final CodeGenerator astVisitor;

    public ExpressionGenerator(
            InstructionGenerator emitter,
            RegisterManager registerManager,
            StackManager stackManager,
            ConditionalGenerator conditionalGenerator,
            FunctionGenerator functionGenerator,
            GlobalManager globalManager,
            CodeGenerator astVisitor) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
        this.conditionalGenerator = conditionalGenerator;
        this.functionGenerator = functionGenerator;
        this.globalManager = globalManager;
        this.astVisitor = astVisitor;
    }

    public String visitBinaryOp(BinaryOp binaryOp) {
        final BinaryOp.Op operator = binaryOp.getOp();

        // Handle comparison operators
        if (ConditionalGenerator.isComparisonOp(operator)) {
            return conditionalGenerator.generateComparisonResult(binaryOp, astVisitor);
        }

        // Handle logical operators
        if (ConditionalGenerator.isLogicalOp(operator)) {
            return conditionalGenerator.generateLogicalResult(binaryOp, astVisitor);
        }

        // Handle arithmetic/bitwise operations
        return generateArithmeticOperation(binaryOp);
    }

    private String generateArithmeticOperation(BinaryOp binaryOp) {
        emitter.comment("Binary operation: " + binaryOp.getOp());

        final String leftReg = binaryOp.getLeft().accept(astVisitor);
        final String rightReg = binaryOp.getRight().accept(astVisitor);
        final String resultReg = registerManager.allocateRegister();

        try {
            emitter.generateBinaryOperation(binaryOp.getOp(), resultReg, leftReg, rightReg);
            return resultReg;
        } finally {
            registerManager.freeRegister(leftReg);
            registerManager.freeRegister(rightReg);
        }
    }

    public String visitUnaryOp(UnaryOp unaryOp) {
        emitter.comment("Unary operation: " + unaryOp.getOp());

        final String operandReg = unaryOp.getOperand().accept(astVisitor);
        final String resultReg = registerManager.allocateRegister();

        generateUnaryOperation(unaryOp.getOp(), resultReg, operandReg);

        registerManager.freeRegister(operandReg);
        return resultReg;
    }

    private void generateUnaryOperation(UnaryOp.Op operator, String resultReg, String operandReg) {
        switch (operator) {
            case NEG -> emitter.instruction("NEG", resultReg, operandReg);
            case NOT -> emitter.instruction("NOT", resultReg, operandReg);
            default -> throw new IllegalArgumentException("Unsupported unary operator: " + operator);
        }
    }

    public String visitFunctionCall(FunctionCall functionCall) {
        emitter.comment("Function call: " + functionCall.getName());

        validateArgumentCount(functionCall);

        return functionGenerator.generateFunctionCall(
                functionCall.getName(),
                functionCall.getArguments(),
                astVisitor
        );
    }

    private void validateArgumentCount(FunctionCall functionCall) {
        if (functionCall.getArguments().size() > 7) {
            throw new IllegalArgumentException("Too many arguments for function: " + functionCall.getName());
        }
    }

    public String visitLiteral(Literal<?> literal) {
        final String reg = registerManager.allocateRegister();
        // can cast literal.getValue() to long safely, others are bytes or ints
        emitter.loadImmediate(reg, String.valueOf(literal.getValue()));
        return reg;
    }

    public String visitVariable(Variable variable) {
        final String variableName = variable.getName();

        if (globalManager.isGlobal(variableName)) {
            return globalManager.loadFromGlobal(variableName, variable.getType());
        }

        final int offset = stackManager.getVariableOffset(variableName);
        return stackManager.loadFromStack(offset, variable.getType());
    }

    public String visitDereference(Dereference dereference) {
        final String addrReg = dereference.getAddress().accept(astVisitor);
        final String resultReg = registerManager.allocateRegister();

        emitter.load(resultReg, addrReg);
        registerManager.freeRegister(addrReg);

        return resultReg;
    }

    public String visitTypeConversion(TypeConversion conversion) {
        emitter.comment("Type conversion to " + conversion.getTargetType());

        final String sourceReg = conversion.getExpression().accept(astVisitor);
        final String resultReg = registerManager.allocateRegister("conversion_result");

        final VariableType sourceType = getExpressionType(conversion.getExpression());
        final VariableType targetType = conversion.getTargetType();

        performTypeConversion(sourceType, targetType, sourceReg, resultReg);

        registerManager.freeRegister(sourceReg);
        return resultReg;
    }

    private void performTypeConversion(VariableType sourceType, VariableType targetType,
                                       String sourceReg, String resultReg) {
        TypeConverter converter = new TypeConverter(emitter, registerManager);
        converter.convert(sourceType, targetType, sourceReg, resultReg);
    }

    public String visitArrayLiteral(ArrayLiteral arrayLiteral) {
        emitter.comment("Array literal with " + arrayLiteral.getElements().size() + " elements");

        if (arrayLiteral.getElements().isEmpty()) {
            final String resultReg = registerManager.allocateRegister();
            emitter.loadImmediate(resultReg, "0");
            return resultReg;
        }

        throw new IllegalStateException(
                "Array literal must be used in assignment context. Use: var x = malloc(size); @x = [1,2,3];"
        );
    }

    public String visitArrayIndex(ArrayIndex arrayIndex) {
        emitter.comment("Array indexing");

        final String arrayReg = arrayIndex.getArray().accept(astVisitor);
        final String indexReg = arrayIndex.getIndex().accept(astVisitor);

        try {
            return performArrayIndexing(arrayReg, indexReg);
        } finally {
            registerManager.freeRegister(arrayReg);
            registerManager.freeRegister(indexReg);
        }
    }

    private String performArrayIndexing(String arrayReg, String indexReg) {
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

    private VariableType getExpressionType(Expression expression) {
        return switch (expression) {
            case Variable var -> var.getType();
            case Literal<?> lit -> lit.getType();
            case TypeConversion typeConv -> typeConv.getTargetType();
            case BinaryOp binOp -> getExpressionType(binOp.getLeft());
            case null, default -> VariableType.LONG; // Default fallback
        };
    }
}