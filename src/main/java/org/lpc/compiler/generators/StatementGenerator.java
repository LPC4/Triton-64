package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.statements.*;
import org.lpc.compiler.context_managers.*;
import org.lpc.compiler.types.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles code generation for statements.
 */
public class StatementGenerator {
    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;
    private final ConditionalGenerator conditionalGenerator;
    private final GlobalManager globalManager;
    private final ContextManager context;
    private final CodeGenerator codeGenerator;

    public StatementGenerator(
            InstructionGenerator emitter,
            RegisterManager registerManager,
            StackManager stackManager,
            ConditionalGenerator conditionalGenerator,
            GlobalManager globalManager,
            ContextManager context,
            CodeGenerator codeGenerator) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
        this.conditionalGenerator = conditionalGenerator;
        this.globalManager = globalManager;
        this.context = context;
        this.codeGenerator = codeGenerator;
    }

    // ========================================================================
    // Statement Handlers
    // ========================================================================

    public String visitReturnStatement(ReturnStatement returnStatement) {
        emitter.comment("Return statement");
        Optional<Expression> returnValue = Optional.ofNullable(returnStatement.getValue());
        returnValue.ifPresent(value -> {
            String valueReg = value.accept(codeGenerator);
            emitter.move("a0", valueReg);
            registerManager.freeRegister(valueReg);
        });

        String functionEndLabel = context.getCurrentFunctionEndLabel();
        if (functionEndLabel == null) {
            throw new IllegalStateException("Return statement outside of function");
        }
        emitter.jump(functionEndLabel);
        return null;
    }

    public String visitIfStatement(IfStatement ifStatement) {
        emitter.comment("If statement");
        String elseLabel = context.generateLabel("else");
        String endLabel = context.generateLabel("endif");

        // Generate condition check
        conditionalGenerator.generateConditionalJump(
                ifStatement.getCondition(),
                elseLabel,
                codeGenerator
        );

        // Generate 'then' branch
        generateStatements(ifStatement.getThenBranch());

        // Handle optional 'else' branch
        if (hasElseBranch(ifStatement)) {
            emitter.jump(endLabel);
            emitter.label(elseLabel);
            generateStatements(ifStatement.getElseBranch());
            emitter.label(endLabel);
        } else {
            emitter.label(elseLabel);
        }
        return null;
    }

    public String visitWhileStatement(WhileStatement whileStatement) {
        emitter.comment("While loop");
        String loopLabel = context.generateLabel("loop");
        String endLabel = context.generateLabel("endloop");

        emitter.label(loopLabel);
        conditionalGenerator.generateConditionalJump(
                whileStatement.getCondition(),
                endLabel,
                codeGenerator
        );
        generateStatements(whileStatement.getBody());
        emitter.jump(loopLabel);
        emitter.label(endLabel);
        return null;
    }

    public String visitDeclaration(Declaration declaration) {
        emitter.comment("Variable declaration: " + declaration.getName());
        int offset = stackManager.allocateVariable(
                declaration.getName(),
                declaration.getType()
        );

        Optional<Expression> initializer = Optional.ofNullable(declaration.getInitializer());

        initializer.ifPresent(expr -> {
            String valueReg;
            if (expr instanceof ArrayLiteral arrayLiteral) {
                valueReg = registerManager.allocateRegister("array_init");
                generateArrayToMemory(
                        valueReg,
                        arrayLiteral,
                        declaration.getType().asPointer().getElementType()
                );
            } else {
                valueReg = expr.accept(codeGenerator);
            }

            stackManager.storeToStack(offset, valueReg, declaration.getType());
            registerManager.freeRegister(valueReg);
        });
        return null;
    }

    public String visitAssignmentStatement(AssignmentStatement assignment) {
        switch (assignment.getTarget()) {
            case Variable var ->
                    handleVariableAssignment(var, assignment.getInitialValue());
            case Dereference deref ->
                    handleDereferenceAssignment(deref, assignment.getInitialValue());
            case ArrayIndex arrayIdx ->
                    handleArrayIndexAssignment(arrayIdx, assignment.getInitialValue());
            case StructFieldAccess structAccess ->
                    handleStructFieldAssignment(structAccess, assignment.getInitialValue());

            case null -> throw new IllegalStateException("Assignment target cannot be null");
            default -> throw new IllegalStateException("Unsupported assignment target: " + assignment.getTarget().getClass());
        }
        return null;
    }

    public String visitAsmStatement(AsmStatement asmStatement) {
        emitter.comment("Inline assembly statement");
        asmStatement.getAsmCode().stream()
                .map(String::trim)
                .forEach(emitter::instruction);
        return null;
    }

    public String visitExpressionStatement(ExpressionStatement expressionStatement) {
        emitter.comment("Expression statement");
        String resultReg = expressionStatement.getExpression().accept(codeGenerator);
        if (resultReg != null) {
            registerManager.freeRegister(resultReg);
        }
        return null;
    }

    // ========================================================================
    // Assignment Handlers
    // ========================================================================

    private void handleVariableAssignment(Variable var, Expression value) {
        emitter.comment("Variable assignment: " + var.getName());
        if (value instanceof ArrayLiteral arrayLiteral) {
            generateArrayAssignment(var, arrayLiteral);
            return;
        }

        String valueReg = value.accept(codeGenerator);
        if (globalManager.isGlobal(var.getName())) {
            globalManager.storeToGlobal(var.getName(), valueReg, var.getType());
        } else {
            int offset = stackManager.getVariableOffset(var.getName());
            stackManager.storeToStack(offset, valueReg, var.getType());
        }
        registerManager.freeRegister(valueReg);
    }

    private void handleDereferenceAssignment(Dereference deref, Expression value) {
        String addrReg = deref.getAddress().accept(codeGenerator);
        emitter.comment("Dereference assignment to address: " + addrReg);

        try {
            if (value instanceof ArrayLiteral arrayLiteral) {
                handleArrayLiteralDereference(deref, arrayLiteral, addrReg);
            } else {
                handleRegularDereference(deref, value, addrReg);
            }
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void handleArrayIndexAssignment(ArrayIndex arrayIdx, Expression value) {
        String arrayReg = arrayIdx.getArray().accept(codeGenerator);
        String indexReg = arrayIdx.getIndex().accept(codeGenerator);
        String valueReg = value.accept(codeGenerator);
        String offsetReg = registerManager.allocateRegister();
        String addrReg = registerManager.allocateRegister();

        try {
            emitter.comment("Array index assignment");
            validateArrayIndexingType(arrayIdx);
            calculateArrayElementAddress(arrayIdx, arrayReg, indexReg, offsetReg, addrReg);
            storeArrayElementValue(arrayIdx, valueReg, addrReg);
        } finally {
            freeRegisters(arrayReg, indexReg, valueReg, offsetReg, addrReg);
        }
    }

    private void handleStructFieldAssignment(StructFieldAccess structAccess, Expression value) {
        emitter.comment("Struct field assignment: " + structAccess.getFieldName());
        String baseReg = structAccess.getBase().accept(codeGenerator);
        String addrReg = registerManager.allocateRegister();

        try {
            int offset = structAccess.getFieldOffset();
            calculateStructFieldAddress(baseReg, offset, addrReg);
            storeStructFieldValue(structAccess, value, addrReg);
        } finally {
            freeRegisters(baseReg, addrReg);
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private void handleArrayLiteralDereference(Dereference deref, ArrayLiteral arrayLiteral, String addrReg) {
        Type addressType = Type.getExpressionType(deref.getAddress());
        if (!addressType.isPtr()) {
            throw new IllegalStateException(
                    "Array literals require typed pointer, but got: " + addressType
            );
        }
        generateArrayToMemory(
                addrReg,
                arrayLiteral,
                addressType.asPointer().getElementType()
        );
    }

    private void handleRegularDereference(Dereference deref, Expression value, String addrReg) {
        String valueReg = value.accept(codeGenerator);
        try {
            Type storeType = determineStoreType(deref, value);
            emitter.comment("Store " + storeType + " to address " + addrReg);
            emitter.generateTypedStore(addrReg, valueReg, storeType);
        } finally {
            registerManager.freeRegister(valueReg);
        }
    }

    private Type determineStoreType(Dereference deref, Expression value) {
        return Optional.ofNullable(deref.getType())
                .orElseGet(() -> Type.getExpressionType(value));
    }

    private void validateArrayIndexingType(ArrayIndex arrayIdx) {
        Type arrayType = Type.getExpressionType(arrayIdx.getArray());
        if (!arrayType.isPtr()) {
            throw new IllegalArgumentException(
                    "Array indexing requires pointer type, but got: " + arrayType +
                            " (use type conversion like byte@ptr for raw pointers)"
            );
        }
    }

    private void calculateArrayElementAddress(
            ArrayIndex arrayIdx,
            String arrayReg,
            String indexReg,
            String offsetReg,
            String addrReg) {

        PointerType ptrType = Type.getExpressionType(arrayIdx.getArray()).asPointer();
        int elementSize = ptrType.getElementType().getSize();

        emitter.instruction("LDI", offsetReg, String.valueOf(elementSize));
        emitter.instruction("MUL", offsetReg, indexReg, offsetReg);
        emitter.instruction("ADD", addrReg, arrayReg, offsetReg);
    }

    private void storeArrayElementValue(ArrayIndex arrayIdx, String valueReg, String addrReg) {
        Type elementType = Type.getExpressionType(arrayIdx.getArray()).asPointer().getElementType();
        emitter.generateTypedStore(addrReg, valueReg, elementType);
    }

    private void calculateStructFieldAddress(String baseReg, int offset, String addrReg) {
        emitter.instruction("LDI", addrReg, String.valueOf(offset));
        emitter.instruction("ADD", addrReg, baseReg, addrReg);
    }

    private void storeStructFieldValue(StructFieldAccess structAccess, Expression value, String addrReg) {
        String valueReg = value.accept(codeGenerator);
        try {
            emitter.generateTypedStore(addrReg, valueReg, structAccess.getFieldType());
        } finally {
            registerManager.freeRegister(valueReg);
        }
    }

    private void generateArrayAssignment(Variable var, ArrayLiteral arrayLiteral) {
        emitter.comment("Array assignment to variable: " + var.getName());
        String addrReg = resolveVariableAddress(var);

        try {
            Type elementType = determineArrayElementType(var);
            generateArrayToMemory(addrReg, arrayLiteral, elementType);
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private String resolveVariableAddress(Variable var) {
        if (globalManager.isGlobal(var.getName())) {
            return globalManager.loadFromGlobal(var.getName(), PrimitiveType.LONG);
        }
        int offset = stackManager.getVariableOffset(var.getName());
        return stackManager.loadFromStack(offset, PrimitiveType.LONG);
    }

    private Type determineArrayElementType(Variable var) {
        Type varType = Type.getExpressionType(var);
        return varType.isPtr()
                ? varType.asPointer().getElementType()
                : PrimitiveType.LONG;
    }

    private void generateArrayToMemory(String baseAddrReg, ArrayLiteral arrayLiteral, Type elementType) {
        emitter.comment("Storing array literal to memory at " + baseAddrReg);
        validateArrayElementType(elementType);

        String offsetReg = registerManager.allocateRegister();
        String currentAddrReg = registerManager.allocateRegister();
        int elementSize = elementType.getSize();

        try {
            for (int i = 0; i < arrayLiteral.getElements().size(); i++) {
                Expression element = arrayLiteral.getElements().get(i);
                calculateElementAddress(baseAddrReg, offsetReg, currentAddrReg, i, elementSize);
                storeArrayElement(element, currentAddrReg, elementType);
            }
        } finally {
            freeRegisters(offsetReg, currentAddrReg);
        }
    }

    private void validateArrayElementType(Type elementType) {
        if (elementType == null || (!elementType.isPtr() && !elementType.isPrimitive())) {
            throw new IllegalStateException(
                    "Array literals require typed pointer target, but got: " + elementType
            );
        }
    }

    private void calculateElementAddress(
            String baseAddrReg,
            String offsetReg,
            String currentAddrReg,
            int index,
            int elementSize) {

        emitter.instruction("LDI", offsetReg, String.valueOf(index * elementSize));
        emitter.instruction("ADD", currentAddrReg, baseAddrReg, offsetReg);
    }

    private void storeArrayElement(Expression element, String addrReg, Type elementType) {
        String elementReg = element.accept(codeGenerator);
        try {
            emitter.generateTypedStore(addrReg, elementReg, elementType);
        } finally {
            registerManager.freeRegister(elementReg);
        }
    }

    private void freeRegisters(String... registers) {
        for (String reg : registers) {
            if (reg != null) {
                registerManager.freeRegister(reg);
            }
        }
    }

    private void generateStatements(List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(codeGenerator));
    }

    private boolean hasElseBranch(IfStatement ifStatement) {
        return ifStatement.getElseBranch() != null
                && !ifStatement.getElseBranch().isEmpty();
    }
}