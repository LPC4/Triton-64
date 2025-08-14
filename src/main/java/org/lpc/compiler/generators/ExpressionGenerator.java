package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.types.PointerType;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.context_managers.*;
import java.util.List;

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
            case NEG -> emitter.negate(resultReg, operandReg);
            case NOT -> emitter.not(resultReg, operandReg);
            case BIN_NOT -> {
                emitter.not(resultReg, operandReg);
                emitter.and(resultReg, resultReg, "1"); // should be expanded by assembler
            }
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
        final Type derefType = dereference.getType(); // Target type for dereference

        // Handle raw pointers (long) converted via @ operator
        if (derefType != null && !derefType.isPtr()) {
            switch (derefType) {
                case PrimitiveType.BYTE -> emitter.loadByte(resultReg, addrReg);
                case PrimitiveType.INT -> emitter.loadInt(resultReg, addrReg);
                case PrimitiveType.LONG -> emitter.load(resultReg, addrReg);
                default -> throw new IllegalArgumentException(
                        "Unsupported dereference type: " + derefType
                );
            }
        }
        // Handle typed pointers (already pointer type)
        else {
            Type addrType = getExpressionType(dereference.getAddress());
            if (!addrType.isPtr()) {
                throw new IllegalArgumentException(
                        "Dereference requires pointer type, but got: " + addrType
                );
            }

            PointerType ptrType = addrType.asPointer();
            Type elementType = ptrType.getElementType();
            switch (elementType) {
                case PrimitiveType.BYTE -> emitter.loadByte(resultReg, addrReg);
                case PrimitiveType.INT -> emitter.loadInt(resultReg, addrReg);
                case PrimitiveType.LONG -> emitter.load(resultReg, addrReg);
                default -> {
                    if (elementType.isPtr()) {
                        emitter.load(resultReg, addrReg); // Pointers stored as longs
                    } else {
                        throw new IllegalArgumentException(
                                "Unsupported pointer element type: " + elementType
                        );
                    }
                }
            }
        }

        registerManager.freeRegister(addrReg);
        return resultReg;
    }

    public String visitTypeConversion(TypeConversion conversion) {
        emitter.comment("Type conversion to " + conversion.getTargetType());
        final String sourceReg = conversion.getExpression().accept(astVisitor);
        final String resultReg = registerManager.allocateRegister("conversion_result");
        final Type sourceType = getExpressionType(conversion.getExpression());
        final Type targetType = conversion.getTargetType();
        performTypeConversion(sourceType, targetType, sourceReg, resultReg);
        registerManager.freeRegister(sourceReg);
        return resultReg;
    }

    private void performTypeConversion(Type sourceType, Type targetType,
                                       String sourceReg, String resultReg) {
        TypeConversionGenerator converter = new TypeConversionGenerator(emitter, registerManager);
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
            return performArrayIndexing(arrayReg, indexReg, arrayIndex);
        } finally {
            registerManager.freeRegister(arrayReg);
            registerManager.freeRegister(indexReg);
        }
    }

    private String performArrayIndexing(String arrayReg, String indexReg, ArrayIndex arrayIndex) {
        final String offsetReg = registerManager.allocateRegister();
        final String addrReg = registerManager.allocateRegister();
        final String resultReg = registerManager.allocateRegister();
        try {
            // ENFORCE: Array indexing requires a pointer type
            Type arrayType = getExpressionType(arrayIndex.getArray());
            if (!arrayType.isPtr()) {
                throw new IllegalArgumentException(
                        "Array indexing requires pointer type, but got: " + arrayType +
                                " (use type conversion like byte@ptr for raw pointers)"
                );
            }

            PointerType ptrType = arrayType.asPointer();
            Type elementType = ptrType.getElementType();
            int elementSize = elementType.getSize();

            // Calculate address: array + (index * elementSize)
            emitter.instruction("LDI", offsetReg, String.valueOf(elementSize));
            emitter.instruction("MUL", offsetReg, indexReg, offsetReg);
            emitter.instruction("ADD", addrReg, arrayReg, offsetReg);

            // Load based on element type
            switch (elementType) {
                case PrimitiveType.BYTE -> emitter.loadByte(resultReg, addrReg);
                case PrimitiveType.INT -> emitter.loadInt(resultReg, addrReg);
                case PrimitiveType.LONG -> emitter.load(resultReg, addrReg);
                default -> {
                    if (elementType.isPtr()) {
                        emitter.load(resultReg, addrReg); // Pointers stored as longs
                    } else {
                        throw new IllegalArgumentException("Unsupported array element type: " + elementType);
                    }
                }
            }
            return resultReg;
        } finally {
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(addrReg);
        }
    }

    private Type getExpressionType(Expression expression) {
        if (expression instanceof Variable var) {
            return var.getType();
        } else if (expression instanceof Literal<?> lit) {
            return lit.getType();
        } else if (expression instanceof TypeConversion typeConv) {
            return typeConv.getTargetType();
        } else if (expression instanceof BinaryOp binOp) {
            return getExpressionType(binOp.getLeft());
        } else if (expression instanceof Dereference deref) {
            return deref.getType();
        } else if (expression instanceof ArrayIndex arrayIdx) {
            Type arrayType = getExpressionType(arrayIdx.getArray());
            if (arrayType.isPtr()) {
                return arrayType.asPointer().getElementType();
            }
            return PrimitiveType.LONG;
        }
        // Default fallback
        return PrimitiveType.LONG;
    }
}