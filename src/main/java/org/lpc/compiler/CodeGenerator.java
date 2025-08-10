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
 * Generates assembly code from the parsed AST.
 */
public class CodeGenerator implements AstVisitor<String> {
    private final Program program;

    private final CodeGenContext ctx;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;
    private final ConditionalGenerator conditionalGenerator;
    private final FunctionManager functionManager;
    private final GlobalManager globalManager;

    // Current compilation state
    private String currentFunctionEndLabel;

    public CodeGenerator(Parser parser) {
        this.program = parser.parse();
        this.ctx = new CodeGenContext();
        this.emitter = new InstructionEmitter(ctx);
        this.registerManager = new RegisterManager();
        this.stackManager = new StackManager(emitter, registerManager);
        this.conditionalGenerator = new ConditionalGenerator(ctx, emitter, registerManager);
        this.functionManager = new FunctionManager(ctx, emitter, registerManager, stackManager);
        this.globalManager = new GlobalManager(emitter, registerManager);
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
        emitter.comment("Program entry point");
        emitter.label("_start");

        // First, allocate space for global variables
        globalManager.allocateGlobals(program.globals);

        // Initialize global variables with their values
        globalManager.initializeGlobals(program.globals, this);

        // Jump to main and halt after return
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
    public String visit(GlobalDeclaration globalDeclaration) {
        // This is handled in Program.visit(), so we don't need to do anything here
        // But we need this method to implement the interface
        return null;
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
            // Check if initializer is an array literal
            if (declaration.initializer instanceof ArrayLiteral arrayLit) {
                // For array literal initialization, we need to handle it specially
                // The variable should contain an address where we store the array
                String valueReg = declaration.initializer.accept(this);
                if (valueReg != null) {
                    stackManager.storeToStack(offset, valueReg);
                    registerManager.freeRegister(valueReg);
                }
            } else {
                String valueReg = declaration.initializer.accept(this);
                stackManager.storeToStack(offset, valueReg);
                registerManager.freeRegister(valueReg);
            }
        }

        return null;
    }

    @Override
    public String visit(AssignmentStatement assignment) {
        if (assignment.target instanceof Variable var) {
            // Check if we're assigning an array literal
            if (assignment.initialValue instanceof ArrayLiteral arrayLit) {
                String valueReg = generateArrayAssignment(var, arrayLit);
                if (valueReg != null) {
                    registerManager.freeRegister(valueReg);
                }
            } else {
                String valueReg = assignment.initialValue.accept(this);

                // Check if it's a global variable first
                if (globalManager.isGlobal(var.name)) {
                    emitter.comment("Assignment to global variable: " + var.name);
                    globalManager.storeToGlobal(var.name, valueReg);
                } else {
                    emitter.comment("Assignment to local variable: " + var.name);
                    int offset = stackManager.getVariableOffset(var.name);
                    stackManager.storeToStack(offset, valueReg);
                }

                registerManager.freeRegister(valueReg);
            }
        } else if (assignment.target instanceof Dereference deref) {
            // Check if we're assigning an array literal to a dereferenced address
            if (assignment.initialValue instanceof ArrayLiteral arrayLit) {
                String addrReg = deref.address.accept(this);
                generateArrayToMemory(addrReg, arrayLit);
                registerManager.freeRegister(addrReg);
            } else {
                String addrReg = deref.address.accept(this);
                String valueReg = assignment.initialValue.accept(this);
                emitter.comment("Assignment to dereferenced address at " + addrReg);
                emitter.instruction("ST", addrReg, valueReg);
                registerManager.freeRegister(addrReg);
                registerManager.freeRegister(valueReg);
            }
        } else if (assignment.target instanceof ArrayIndex arrayIdx) {
            // Handle array index assignment: arr[i] = value
            String arrayReg = arrayIdx.array.accept(this);
            String indexReg = arrayIdx.index.accept(this);
            String valueReg = assignment.initialValue.accept(this);

            String offsetReg = registerManager.allocateRegister();
            String addrReg = registerManager.allocateRegister();

            emitter.comment("Array index assignment");

            // Calculate offset: index * 8
            emitter.instruction("LDI", offsetReg, "8");
            emitter.instruction("MUL", offsetReg, indexReg, offsetReg);

            // Calculate address: array + offset
            emitter.instruction("ADD", addrReg, arrayReg, offsetReg);

            // Store value at calculated address
            emitter.instruction("ST", addrReg, valueReg);

            registerManager.freeRegister(arrayReg);
            registerManager.freeRegister(indexReg);
            registerManager.freeRegister(valueReg);
            registerManager.freeRegister(offsetReg);
            registerManager.freeRegister(addrReg);
        } else {
            throw new IllegalStateException("Unsupported assignment target");
        }
        return null;
    }

    @Override
    public String visit(AsmStatement asmStatement) {
        emitter.comment("Inline assembly statement");
        for (String line : asmStatement.asmCode) {
            emitter.instruction(line.trim());
        }
        return null;
    }

    @Override
    public String visit(Variable variable) {
        // Check if it's a global first
        if (globalManager.isGlobal(variable.name)) {
            return globalManager.loadFromGlobal(variable.name);
        }

        // Otherwise it's a local variable
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
        // Handle comparison operators - generate result in register
        if (ConditionalGenerator.isComparisonOp(binaryOp.op)) {
            return conditionalGenerator.generateComparisonResult(binaryOp, this);
        }

        // Handle logical operators (&&, ||) - generate result in register
        if (ConditionalGenerator.isLogicalOp(binaryOp.op)) {
            return conditionalGenerator.generateLogicalResult(binaryOp, this);
        }

        // Handle arithmetic/bitwise operations
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
    public String visit(LongLiteral longLiteral) {
        String reg = registerManager.allocateRegister();
        emitter.instruction("LDI", reg, String.valueOf(longLiteral.value));
        return reg;
    }

    @Override
    public String visit(Dereference dereference) {
        String addrReg = dereference.address.accept(this);
        String resultReg = registerManager.allocateRegister();
        emitter.instruction("LD", resultReg, addrReg);
        registerManager.freeRegister(addrReg);
        return resultReg;
    }

    @Override
    public String visit(ArrayLiteral arrayLiteral) {
        emitter.comment("Array literal with " + arrayLiteral.elements.size() + " elements");

        if (arrayLiteral.elements.isEmpty()) {
            // Return null pointer for empty array
            String resultReg = registerManager.allocateRegister();
            emitter.instruction("LDI", resultReg, "0");
            return resultReg;
        }

        // For array literals used in contexts where we need a value (not assignment),
        // we could allocate memory dynamically, but that's complex.
        // For now, throw an error suggesting proper usage.
        throw new IllegalStateException("Array literal must be used in assignment context. Use: var x = malloc(size); @x = [1,2,3];");
    }

    @Override
    public String visit(ArrayIndex arrayIndex) {
        emitter.comment("Array indexing");

        String arrayReg = arrayIndex.array.accept(this);
        String indexReg = arrayIndex.index.accept(this);
        String offsetReg = registerManager.allocateRegister();
        String addrReg = registerManager.allocateRegister();
        String resultReg = registerManager.allocateRegister();

        // Calculate offset: index * 8
        emitter.instruction("LDI", offsetReg, "8");
        emitter.instruction("MUL", offsetReg, indexReg, offsetReg);

        // Calculate address: array + offset
        emitter.instruction("ADD", addrReg, arrayReg, offsetReg);

        // Load value from calculated address
        emitter.instruction("LD", resultReg, addrReg);

        registerManager.freeRegister(arrayReg);
        registerManager.freeRegister(indexReg);
        registerManager.freeRegister(offsetReg);
        registerManager.freeRegister(addrReg);

        return resultReg;
    }

    // Helper methods for array operations
    private String generateArrayAssignment(Variable var, ArrayLiteral arrayLit) {
        emitter.comment("Array assignment to variable: " + var.name);

        // The variable should contain the address where we want to store the array
        String addrReg;
        if (globalManager.isGlobal(var.name)) {
            addrReg = globalManager.loadFromGlobal(var.name);
        } else {
            int offset = stackManager.getVariableOffset(var.name);
            addrReg = stackManager.loadFromStack(offset);
        }

        generateArrayToMemory(addrReg, arrayLit);
        registerManager.freeRegister(addrReg);
        return null;
    }

    private void generateArrayToMemory(String baseAddrReg, ArrayLiteral arrayLit) {
        emitter.comment("Storing array literal to memory at " + baseAddrReg);

        String offsetReg = registerManager.allocateRegister();
        String currentAddrReg = registerManager.allocateRegister();

        for (int i = 0; i < arrayLit.elements.size(); i++) {
            // Calculate address: base + (i * 8)
            emitter.instruction("LDI", offsetReg, String.valueOf(i * 8));
            emitter.instruction("ADD", currentAddrReg, baseAddrReg, offsetReg);

            // Generate code for the element value
            String elementReg = arrayLit.elements.get(i).accept(this);

            // Store the element at the calculated address
            emitter.instruction("ST", currentAddrReg, elementReg);

            registerManager.freeRegister(elementReg);
        }

        registerManager.freeRegister(offsetReg);
        registerManager.freeRegister(currentAddrReg);
    }

    // Existing helper methods
    private void generateStatements(List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(this));
    }

    private void generateBinaryOperation(BinaryOp.Op op, String resultReg, String leftReg, String rightReg) {
        switch (op) {
            case ADD -> emitter.instruction("ADD", resultReg, leftReg, rightReg);
            case SUB -> emitter.instruction("SUB", resultReg, leftReg, rightReg);
            case MUL -> emitter.instruction("MUL", resultReg, leftReg, rightReg);
            case DIV -> emitter.instruction("DIV", resultReg, leftReg, rightReg);
            case BITWISE_AND -> emitter.instruction("AND", resultReg, leftReg, rightReg);
            case BITWISE_OR -> emitter.instruction("OR", resultReg, leftReg, rightReg);
            case BITWISE_XOR -> emitter.instruction("XOR", resultReg, leftReg, rightReg);
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