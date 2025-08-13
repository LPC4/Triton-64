package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.types.PointerType;
import org.lpc.compiler.ast.statements.*;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.context_managers.*;
import java.util.List;
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
    // Reference to the main generator for recursive calls
    private final CodeGenerator astVisitor;
    public StatementGenerator(
            InstructionGenerator emitter,
            RegisterManager registerManager,
            StackManager stackManager,
            ConditionalGenerator conditionalGenerator,
            GlobalManager globalManager,
            ContextManager context,
            CodeGenerator astVisitor) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
        this.conditionalGenerator = conditionalGenerator;
        this.globalManager = globalManager;
        this.context = context;
        this.astVisitor = astVisitor;
    }

    public String visitReturnStatement(ReturnStatement returnStatement) {
        emitter.comment("return statement");
        Optional.ofNullable(returnStatement.getValue()).ifPresent(value -> {
            String valueReg = value.accept(astVisitor);
            emitter.move("a0", valueReg);
            registerManager.freeRegister(valueReg);
        });
        String currentFunctionEndLabel = context.getCurrentFunctionEndLabel();
        if (currentFunctionEndLabel == null) {
            throw new IllegalStateException("Return statement outside of function");
        }
        emitter.jump(currentFunctionEndLabel);
        return null;
    }

    public String visitIfStatement(IfStatement ifStatement) {
        emitter.comment("If statement");
        String elseLabel = context.generateLabel("else");
        String endLabel = context.generateLabel("endif");
        conditionalGenerator.generateConditionalJump(ifStatement.getCondition(), elseLabel, astVisitor);
        generateStatements(ifStatement.getThenBranch());
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
        conditionalGenerator.generateConditionalJump(whileStatement.getCondition(), endLabel, astVisitor);
        generateStatements(whileStatement.getBody());
        emitter.jump(loopLabel);
        emitter.label(endLabel);
        return null;
    }

    public String visitDeclaration(Declaration declaration) {
        emitter.comment("Declaration: " + declaration.getName());
        int offset = stackManager.allocateVariable(declaration.getName(), declaration.getType());
        Optional.ofNullable(declaration.getInitializer()).ifPresent(initializer -> {
            String valueReg = initializer.accept(astVisitor);
            stackManager.storeToStack(offset, valueReg, declaration.getType());
            registerManager.freeRegister(valueReg);
        });
        return null;
    }

    public String visitAssignmentStatement(AssignmentStatement assignment) {
        Expression target = assignment.getTarget();
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

    private void handleVariableAssignment(Variable var, Expression value) {
        if (value instanceof ArrayLiteral arrayLit) {
            generateArrayAssignment(var, arrayLit);
        } else {
            String valueReg = value.accept(astVisitor);
            if (globalManager.isGlobal(var.getName())) {
                emitter.comment("Assignment to global variable: " + var.getName());
                globalManager.storeToGlobal(var.getName(), valueReg, var.getType());
            } else {
                emitter.comment("Assignment to local variable: " + var.getName());
                int offset = stackManager.getVariableOffset(var.getName());
                stackManager.storeToStack(offset, valueReg, var.getType());
            }
            registerManager.freeRegister(valueReg);
        }
    }

    private void handleDereferenceAssignment(Dereference deref, Expression value) {
        String addrReg = deref.getAddress().accept(astVisitor);
        try {
            if (value instanceof ArrayLiteral arrayLit) {
                // Handle array literals (only valid for typed pointers)
                Type addrType = getExpressionType(deref.getAddress());
                if (!addrType.isPtr()) {
                    throw new IllegalStateException(
                            "Array literals require typed pointer, but got: " + addrType
                    );
                }
                generateArrayToMemory(addrReg, arrayLit, addrType.asPointer().getElementType());
            } else {
                String valueReg = value.accept(astVisitor);
                try {
                    // Use dereference target type for storage
                    Type storeType = deref.getType() != null
                            ? deref.getType()
                            : getExpressionType(value);

                    emitter.comment("Store " + storeType + " to address " + addrReg);
                    generateTypedStore(addrReg, valueReg, storeType);
                } finally {
                    registerManager.freeRegister(valueReg);
                }
            }
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void handleArrayIndexAssignment(ArrayIndex arrayIdx, Expression value) {
        String arrayReg = arrayIdx.getArray().accept(astVisitor);
        String indexReg = arrayIdx.getIndex().accept(astVisitor);
        String valueReg = value.accept(astVisitor);
        String offsetReg = registerManager.allocateRegister();
        String addrReg = registerManager.allocateRegister();
        try {
            emitter.comment("Array index assignment");
            // ENFORCE: Array indexing requires a pointer type
            Type arrayType = getExpressionType(arrayIdx.getArray());
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

            // Store based on element type
            generateTypedStore(addrReg, valueReg, elementType);
        } finally {
            registerManager.freeRegister(arrayReg);
            registerManager.freeRegister(indexReg);
            registerManager.freeRegister(valueReg);
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(addrReg);
        }
    }

    public String visitAsmStatement(AsmStatement asmStatement) {
        emitter.comment("Inline assembly statement");
        asmStatement.getAsmCode()
                .stream()
                .map(String::trim)
                .forEach(emitter::instruction);
        return null;
    }

    public String visitExpressionStatement(ExpressionStatement expressionStatement) {
        emitter.comment("Expression statement");
        String resultReg = expressionStatement.getExpression().accept(astVisitor);
        if (resultReg != null) {
            registerManager.freeRegister(resultReg);
        }
        return null;
    }

    private void generateArrayAssignment(Variable var, ArrayLiteral arrayLit) {
        emitter.comment("Array assignment to variable: " + var.getName());
        String addrReg = globalManager.isGlobal(var.getName())
                ? globalManager.loadFromGlobal(var.getName(), PrimitiveType.LONG)
                : stackManager.loadFromStack(stackManager.getVariableOffset(var.getName()), PrimitiveType.LONG);
        try {
            // Get the variable type to determine element type
            Type varType = getExpressionType(var);
            Type elementType;
            if (varType.isPtr()) {
                elementType = varType.asPointer().getElementType();
            } else {
                elementType = PrimitiveType.LONG; // Default
            }

            generateArrayToMemory(addrReg, arrayLit, elementType);
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void generateArrayToMemory(String baseAddrReg, ArrayLiteral arrayLit, Type elementType) {
        emitter.comment("Storing array literal to memory at " + baseAddrReg);

        // ENFORCE: Only valid for typed pointers
        if (elementType == null || (!elementType.isPtr() && !elementType.isPrimitive())) {
            throw new IllegalStateException(
                    "Array literals require typed pointer target, but got: " + elementType
            );
        }

        String offsetReg = registerManager.allocateRegister();
        String currentAddrReg = registerManager.allocateRegister();
        try {
            List<Expression> elements = arrayLit.getElements();
            int elementSize = elementType.getSize();
            for (int i = 0; i < elements.size(); i++) {
                // Calculate address: base + (i * elementSize)
                emitter.instruction("LDI", offsetReg, String.valueOf(i * elementSize));
                emitter.instruction("ADD", currentAddrReg, baseAddrReg, offsetReg);
                String elementReg = elements.get(i).accept(astVisitor);
                try {
                    generateTypedStore(currentAddrReg, elementReg, elementType);
                } finally {
                    registerManager.freeRegister(elementReg);
                }
            }
        } finally {
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(currentAddrReg);
        }
    }

    /**
     * Generates appropriate store instruction based on the target type
     */
    private void generateTypedStore(String addrReg, String valueReg, Type targetType) {
        switch (targetType) {
            case PrimitiveType.BYTE -> emitter.instruction("SB", addrReg, valueReg);  // Store byte (1 byte)
            case PrimitiveType.INT -> emitter.instruction("SI", addrReg, valueReg);   // Store int (4 bytes)
            case PrimitiveType.LONG -> emitter.instruction("ST", addrReg, valueReg);   // Store long (8 bytes)
            default -> {
                if (targetType.isPtr()) {
                    emitter.instruction("ST", addrReg, valueReg);  // Pointers are stored as longs
                } else {
                    throw new IllegalStateException("Unsupported store type: " + targetType);
                }
            }
        }
    }

    private Type getExpressionType(Expression expr) {
        // TODO: Implement proper type inference/checking
        if (expr instanceof Variable var) {
            // Look up variable type from symbol table
            if (globalManager.isGlobal(var.getName())) {
                return globalManager.getVariableType(var.getName());
            } else {
                return stackManager.getVariableType(var.getName());
            }
        } else if (expr instanceof Literal<?> lit) {
            return lit.getType();
        } else if (expr instanceof Dereference deref) {
            return deref.getType();
        } else if (expr instanceof ArrayIndex arrayIdx) {
            Type arrayType = getExpressionType(arrayIdx.getArray());
            if (arrayType.isPtr()) {
                return arrayType.asPointer().getElementType();
            }
            // For non-pointer types (shouldn't happen for arrays), assume long
            return PrimitiveType.LONG;
        }
        // Default to long for now
        return PrimitiveType.LONG;
    }

    private void generateStatements(List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(astVisitor));
    }

    private boolean hasElseBranch(IfStatement ifStatement) {
        List<Statement> elseBranch = ifStatement.getElseBranch();
        return elseBranch != null && !elseBranch.isEmpty();
    }
}