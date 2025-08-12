package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.VariableType;
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
    private final ContextManager codeGenContext;

    // Reference to the main generator for recursive calls
    private final CodeGenerator astVisitor;

    public StatementGenerator(
            InstructionGenerator emitter,
            RegisterManager registerManager,
            StackManager stackManager,
            ConditionalGenerator conditionalGenerator,
            GlobalManager globalManager,
            ContextManager context,
            ContextManager codeGenContext,
            CodeGenerator astVisitor) {
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
        this.conditionalGenerator = conditionalGenerator;
        this.globalManager = globalManager;
        this.context = context;
        this.codeGenContext = codeGenContext;
        this.astVisitor = astVisitor;
    }

    public String visitReturnStatement(ReturnStatement returnStatement) {
        emitter.comment("return statement");

        Optional.ofNullable(returnStatement.getValue()).ifPresent(value -> {
            String valueReg = value.accept(astVisitor);
            emitter.move("a0", valueReg);
            registerManager.freeRegister(valueReg);
        });

        String currentFunctionEndLabel = codeGenContext.getCurrentFunctionEndLabel();
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
                generateArrayToMemory(addrReg, arrayLit);
            } else {
                String valueReg = value.accept(astVisitor);
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

    private void handleArrayIndexAssignment(ArrayIndex arrayIdx, Expression value) {
        String arrayReg = arrayIdx.getArray().accept(astVisitor);
        String indexReg = arrayIdx.getIndex().accept(astVisitor);
        String valueReg = value.accept(astVisitor);

        String offsetReg = registerManager.allocateRegister();
        String addrReg = registerManager.allocateRegister();

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
                ? globalManager.loadFromGlobal(var.getName(), VariableType.LONG)
                : stackManager.loadFromStack(stackManager.getVariableOffset(var.getName()), VariableType.LONG);

        try {
            generateArrayToMemory(addrReg, arrayLit);
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void generateArrayToMemory(String baseAddrReg, ArrayLiteral arrayLit) {
        emitter.comment("Storing array literal to memory at " + baseAddrReg);

        String offsetReg = registerManager.allocateRegister();
        String currentAddrReg = registerManager.allocateRegister();

        try {
            List<Expression> elements = arrayLit.getElements();
            for (int i = 0; i < elements.size(); i++) {
                // Calculate address: base + (i * 8)
                emitter.instruction("LDI", offsetReg, String.valueOf(i * 8));
                emitter.instruction("ADD", currentAddrReg, baseAddrReg, offsetReg);

                String elementReg = elements.get(i).accept(astVisitor);
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

    private void generateStatements(List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(astVisitor));
    }

    private boolean hasElseBranch(IfStatement ifStatement) {
        List<Statement> elseBranch = ifStatement.getElseBranch();
        return elseBranch != null && !elseBranch.isEmpty();
    }
}