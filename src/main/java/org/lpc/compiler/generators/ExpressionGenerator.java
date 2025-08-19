package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.context_managers.*;
import org.lpc.compiler.types.*;

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
    private final CodeGenerator codeGenerator;

    public ExpressionGenerator(
            InstructionGenerator emitter,
            RegisterManager registerManager,
            StackManager stackManager,
            ConditionalGenerator conditionalGenerator,
            FunctionGenerator functionGenerator,
            GlobalManager globalManager,
            CodeGenerator codeGenerator) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
        this.conditionalGenerator = conditionalGenerator;
        this.functionGenerator = functionGenerator;
        this.globalManager = globalManager;
        this.codeGenerator = codeGenerator;
    }

    // ========================================================================
    // Core Expression Handlers
    // ========================================================================

    public String visitBinaryOp(BinaryOp binaryOp) {
        BinaryOp.Op operator = binaryOp.getOp();
        if (ConditionalGenerator.isComparisonOp(operator)) {
            return conditionalGenerator.generateComparisonResult(binaryOp, codeGenerator);
        }
        if (ConditionalGenerator.isLogicalOp(operator)) {
            return conditionalGenerator.generateLogicalResult(binaryOp, codeGenerator);
        }
        return handleArithmeticBinaryOp(binaryOp);
    }

    public String visitUnaryOp(UnaryOp unaryOp) {
        emitter.comment("Unary operation: " + unaryOp.getOp());
        String operandReg = unaryOp.getOperand().accept(codeGenerator);
        String resultReg = registerManager.allocateRegister();

        try {
            generateUnaryOperation(unaryOp.getOp(), resultReg, operandReg);
            return resultReg;
        } finally {
            registerManager.freeRegister(operandReg);
        }
    }

    public String visitFunctionCall(FunctionCall functionCall) {
        emitter.comment("Function call: " + functionCall.getName());
        return functionGenerator.generateFunctionCall(
                functionCall.getName(),
                functionCall.getArguments(),
                codeGenerator
        );
    }

    public String visitLiteral(Literal<?> literal) {
        emitter.comment("Literal value: " + literal.getValue());
        String reg = registerManager.allocateRegister();
        try {
            emitter.loadImmediate(reg, String.valueOf(literal.getValue()));
            return reg;
        } catch (Exception e) {
            registerManager.freeRegister(reg);
            throw e;
        }
    }

    public String visitVariable(Variable variable) {
        emitter.comment("Variable access: " + variable.getName());
        return globalManager.isGlobal(variable.getName())
                ? loadGlobalVariable(variable)
                : loadLocalVariable(variable);
    }

    public String visitDereference(Dereference dereference) {
        emitter.comment("Dereference operation");
        String addrReg = null;
        String resultReg = registerManager.allocateRegister();

        try {
            addrReg = dereference.getAddress().accept(codeGenerator);
            handleDereferenceType(dereference, addrReg, resultReg);
            return resultReg;
        } finally {
            if (addrReg != null) {
                registerManager.freeRegister(addrReg);
            }
        }
    }

    public String visitTypeConversion(TypeConversion conversion) {
        emitter.comment("Type conversion to " + conversion.getTargetType());
        String sourceReg = null;
        String resultReg = registerManager.allocateRegister("conversion_result");

        try {
            sourceReg = conversion.getExpression().accept(codeGenerator);
            performTypeConversion(
                    Type.getExpressionType(conversion.getExpression()),
                    conversion.getTargetType(),
                    sourceReg,
                    resultReg
            );
            return resultReg;
        } finally {
            if (sourceReg != null) {
                registerManager.freeRegister(sourceReg);
            }
        }
    }

    public String visitStructFieldAccess(StructFieldAccess structAccess) {
        emitter.comment("Struct field access: " + structAccess.getFieldName());
        String baseReg = structAccess.getBase().accept(codeGenerator);
        String resultReg = registerManager.allocateRegister();

        try {
            String addrReg = calculateStructFieldAddress(baseReg, structAccess.getFieldOffset());
            try {
                emitter.generateTypedLoad(resultReg, addrReg, structAccess.getFieldType());
                return resultReg;
            } finally {
                registerManager.freeRegister(addrReg);
            }
        } finally {
            registerManager.freeRegister(baseReg);
        }
    }

    public String visitArrayLiteral(ArrayLiteral ignoredArrayLiteral) {
        throw new IllegalStateException(
                "Array literal must be used in assignment context. " +
                        "Use: var x = malloc(size); @x = [1,2,3];"
        );
    }

    public String visitArrayIndex(ArrayIndex arrayIndex) {
        emitter.comment("Array indexing operation");
        String arrayReg = null;
        String indexReg = null;

        try {
            arrayReg = arrayIndex.getArray().accept(codeGenerator);
            indexReg = arrayIndex.getIndex().accept(codeGenerator);
            return handleArrayIndexing(arrayIndex, arrayReg, indexReg);
        } finally {
            freeRegisters(arrayReg, indexReg);
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private String handleArithmeticBinaryOp(BinaryOp binaryOp) {
        emitter.comment("Arithmetic operation: " + binaryOp.getOp());
        String leftReg = null;
        String rightReg = null;
        String resultReg = registerManager.allocateRegister();

        try {
            leftReg = binaryOp.getLeft().accept(codeGenerator);
            rightReg = binaryOp.getRight().accept(codeGenerator);
            emitter.generateBinaryOperation(
                    binaryOp.getOp(),
                    resultReg,
                    leftReg,
                    rightReg
            );
            return resultReg;
        } finally {
            freeRegisters(leftReg, rightReg);
        }
    }

    private void generateUnaryOperation(UnaryOp.Op operator, String resultReg, String operandReg) {
        switch (operator) {
            case NEG -> emitter.negate(resultReg, operandReg);
            case NOT -> emitter.not(resultReg, operandReg);
            case BIN_NOT -> {
                emitter.not(resultReg, operandReg);
                emitter.and(resultReg, resultReg, "1"); // Mask to 1 bit
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported unary operator: " + operator
            );
        }
    }

    private String loadGlobalVariable(Variable variable) {
        return globalManager.loadFromGlobal(
                variable.getName(),
                variable.getType()
        );
    }

    private String loadLocalVariable(Variable variable) {
        return stackManager.loadFromStack(
                stackManager.getVariableOffset(variable.getName()),
                variable.getType()
        );
    }

    private void handleDereferenceType(Dereference dereference, String addrReg, String resultReg) {
        Type derefType = dereference.getType();
        if (derefType != null && !derefType.isPtr()) {
            handleRawPointerDereference(derefType, addrReg, resultReg);
        } else {
            handleTypedPointerDereference(dereference, addrReg, resultReg);
        }
    }

    private void handleRawPointerDereference(Type derefType, String addrReg, String resultReg) {
        switch (derefType) {
            case PrimitiveType.BYTE -> emitter.loadByte(resultReg, addrReg);
            case PrimitiveType.INT -> emitter.loadInt(resultReg, addrReg);
            case PrimitiveType.LONG -> emitter.load(resultReg, addrReg);
            default -> throw new IllegalArgumentException(
                    "Unsupported dereference type: " + derefType
            );
        }
    }

    private void handleTypedPointerDereference(Dereference dereference, String addrReg, String resultReg) {
        Type addrType = Type.getExpressionType(dereference.getAddress());
        if (!addrType.isPtr()) {
            throw new IllegalArgumentException(
                    "Dereference requires pointer type, but got: " + addrType
            );
        }

        Type elementType = addrType.asPointer().getElementType();
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

    private String calculateStructFieldAddress(String baseReg, int offset) {
        String addrReg = registerManager.allocateRegister();
        emitter.instruction("LDI", addrReg, String.valueOf(offset));
        emitter.instruction("ADD", addrReg, baseReg, addrReg);
        return addrReg;
    }

    private void performTypeConversion(
            Type sourceType,
            Type targetType,
            String sourceReg,
            String resultReg) {

        new TypeConversionGenerator(emitter, registerManager)
                .convert(sourceType, targetType, sourceReg, resultReg);
    }

    private String handleArrayIndexing(ArrayIndex arrayIndex, String arrayReg, String indexReg) {
        validateArrayIndexingType(arrayIndex);

        String offsetReg = registerManager.allocateRegister();
        String addrReg = registerManager.allocateRegister();
        String resultReg = registerManager.allocateRegister();

        try {
            calculateArrayElementAddress(arrayIndex, arrayReg, indexReg, offsetReg, addrReg);
            loadArrayElementValue(arrayIndex, addrReg, resultReg);
            return resultReg;
        } finally {
            freeRegisters(offsetReg, addrReg);
        }
    }

    private void validateArrayIndexingType(ArrayIndex arrayIndex) {
        Type arrayType = Type.getExpressionType(arrayIndex.getArray());
        if (!arrayType.isPtr()) {
            throw new IllegalArgumentException(
                    "Array indexing requires pointer type, but got: " + arrayType +
                            " (use type conversion like byte@ptr for raw pointers)"
            );
        }
    }

    private void calculateArrayElementAddress(
            ArrayIndex arrayIndex,
            String arrayReg,
            String indexReg,
            String offsetReg,
            String addrReg) {

        PointerType ptrType = Type.getExpressionType(arrayIndex.getArray()).asPointer();
        int elementSize = ptrType.getElementType().getSize();

        emitter.instruction("LDI", offsetReg, String.valueOf(elementSize));
        emitter.instruction("MUL", offsetReg, indexReg, offsetReg);
        emitter.instruction("ADD", addrReg, arrayReg, offsetReg);
    }

    private void loadArrayElementValue(ArrayIndex arrayIndex, String addrReg, String resultReg) {
        Type elementType = Type.getExpressionType(arrayIndex.getArray()).asPointer().getElementType();
        emitter.generateTypedLoad(resultReg, addrReg, elementType);
    }

    private void freeRegisters(String... registers) {
        for (String reg : registers) {
            if (reg != null) {
                registerManager.freeRegister(reg);
            }
        }
    }
}