package org.lpc.compiler.codegen;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.Declaration;
import org.lpc.compiler.ast.statements.IfStatement;
import org.lpc.compiler.ast.statements.WhileStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manages function generation including prologue, epilogue, and calling conventions.
 * Handles the complexity of function setup and teardown.
 */
public class FunctionManager {
    private static final int WORD_SIZE = 8;
    private static final int STACK_ALIGNMENT = 16;

    private final CodeGenContext ctx;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;

    // Helper for analyzing function structure
    private final FunctionAnalyzer analyzer;

    public FunctionManager(CodeGenContext ctx, InstructionEmitter emitter,
                           RegisterManager registerManager, StackManager stackManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
        this.analyzer = new FunctionAnalyzer();
    }

    /**
     * Generate a complete function with prologue and epilogue
     */
    public void generateFunction(String name, List<String> parameters, List<Statement> body,
                                 CodeGenerator.StatementBlockGenerator bodyGenerator, String endLabel) {
        emitter.sectionHeader("Function: " + name);
        emitter.label(name);

        registerManager.reset();
        stackManager.startFunctionFrame(parameters);

        try {
            FunctionInfo info = analyzer.analyze(body, parameters);

            generateFunctionPrologue(info.localVariableCount());
            generateFunctionBody(body, bodyGenerator, info);
            generateFunctionEpilogue(endLabel, name, info.localVariableCount());

        } finally {
            stackManager.endFunctionFrame();
            validateRegisterState(name);
        }
    }

    /**
     * Represents analyzed function information
     */
    private record FunctionInfo(int localVariableCount, List<Statement> declarations, List<Statement> otherStatements) {}

    /**
     * Analyzes function structure and extracts key information
     */
    private class FunctionAnalyzer {
        public FunctionInfo analyze(List<Statement> body, List<String> parameters) {
            int localVarCount = countAllLocalVariables(body, parameters);

            List<Statement> declarations = extractDeclarations(body, parameters);
            List<Statement> otherStatements = extractNonDeclarations(body);

            return new FunctionInfo(localVarCount, declarations, otherStatements);
        }

        private int countAllLocalVariables(List<Statement> statements, List<String> parameters) {
            return new VariableCounter(parameters).count(statements);
        }

        private List<Statement> extractDeclarations(List<Statement> body, List<String> parameters) {
            Set<String> paramSet = Set.copyOf(parameters);
            return body.stream()
                    .filter(Declaration.class::isInstance)
                    .map(Declaration.class::cast)
                    .filter(decl -> !paramSet.contains(decl.name))
                    .map(Statement.class::cast)
                    .toList();
        }

        private List<Statement> extractNonDeclarations(List<Statement> body) {
            return body.stream()
                    .filter(stmt -> !(stmt instanceof Declaration))
                    .toList();
        }
    }

    /**
     * Counts variables recursively through nested statements
     */
    private static class VariableCounter {
        private final Set<String> parameters;

        public VariableCounter(List<String> parameters) {
            this.parameters = Set.copyOf(parameters);
        }

        public int count(List<Statement> statements) {
            return statements.stream()
                    .mapToInt(this::countInStatement)
                    .sum();
        }

        private int countInStatement(Statement stmt) {
            return switch (stmt) {
                case Declaration decl when !parameters.contains(decl.name) -> 1;
                case IfStatement ifStmt -> countInIfStatement(ifStmt);
                case WhileStatement whileStmt -> count(whileStmt.body);
                default -> 0;
            };
        }

        private int countInIfStatement(IfStatement ifStmt) {
            int count = count(ifStmt.thenBranch);
            if (ifStmt.elseBranch != null) {
                count += count(ifStmt.elseBranch);
            }
            return count;
        }
    }

    /**
     * Generate function prologue with stack allocation
     */
    private void generateFunctionPrologue(int localVarCount) {
        emitter.comment("Function prologue");
        emitter.push("ra");
        emitter.push("fp");
        emitter.move("fp", "sp");

        if (localVarCount > 0) {
            allocateStackSpace(localVarCount);
        }
    }

    /**
     * Generate the function body (declarations first, then other statements)
     */
    private void generateFunctionBody(List<Statement> body,
                                      CodeGenerator.StatementBlockGenerator bodyGenerator,
                                      FunctionInfo info) {
        if (!info.declarations().isEmpty()) {
            emitter.comment("Processing local variable declarations");
            bodyGenerator.generate(info.declarations());
        }

        if (!info.otherStatements().isEmpty()) {
            emitter.comment("Processing function body");
            bodyGenerator.generate(info.otherStatements());
        }
    }

    /**
     * Generate function epilogue with stack cleanup
     */
    private void generateFunctionEpilogue(String endLabel, String functionName, int localVarCount) {
        emitter.comment("Function epilogue");
        emitter.label(endLabel);

        if (localVarCount > 0) {
            deallocateStackSpace(localVarCount);
        }

        emitter.pop("fp");
        emitter.pop("ra");
        emitter.instructionWithComment("Return from " + functionName, "JMP", "ra");
    }

    /**
     * Allocate aligned stack space for local variables
     */
    private void allocateStackSpace(int localVarCount) {
        int frameSize = localVarCount * WORD_SIZE;
        int alignedSize = alignToStackBoundary(frameSize);

        String temp = registerManager.allocateRegister("frame_alloc");
        try {
            emitter.loadImmediate(temp, alignedSize);
            emitter.subtract("sp", "sp", temp);
            emitter.comment("Allocated " + alignedSize + " bytes for " + localVarCount + " local variables");
        } finally {
            registerManager.freeRegister(temp);
        }
    }

    /**
     * Deallocate stack space for local variables
     */
    private void deallocateStackSpace(int localVarCount) {
        int frameSize = localVarCount * WORD_SIZE;
        int alignedSize = alignToStackBoundary(frameSize);

        String temp = registerManager.allocateRegister("frame_dealloc");
        try {
            emitter.loadImmediate(temp, alignedSize);
            emitter.add("sp", "sp", temp);
            emitter.comment("Deallocated " + alignedSize + " bytes for frame");
        } finally {
            registerManager.freeRegister(temp);
        }
    }

    /**
     * Align frame size to stack boundary requirements
     */
    private int alignToStackBoundary(int size) {
        return (size + STACK_ALIGNMENT - 1) & ~(STACK_ALIGNMENT - 1);
    }

    /**
     * Validate that all registers were properly freed
     */
    private void validateRegisterState(String functionName) {
        try {
            registerManager.validateAllFreed();
        } catch (Exception e) {
            emitter.comment("WARNING: Register leak detected in function " + functionName);
            emitter.comment("Debug info: " + registerManager.getDebugInfo());
        }
    }

    /**
     * Generate a function call with proper calling convention
     */
    public String generateFunctionCall(String functionName, List<Expression> arguments, CodeGenerator visitor) {
        emitter.comment("Preparing function call: " + functionName);

        if (arguments.size() > 7) {
            throw new IllegalArgumentException("Too many arguments for function: " + functionName);
        }

        return new FunctionCallGenerator().generateCall(functionName, arguments, visitor);
    }

    /**
     * Handles the complexity of function call generation
     */
    private class FunctionCallGenerator {
        public String generateCall(String functionName, List<Expression> arguments, CodeGenerator visitor) {
            saveLiveTemporaries();

            try {
                List<String> argRegs = evaluateArguments(arguments, visitor);
                pushArgumentsToStack(argRegs);

                emitter.jumpAndLink(functionName, "ra");

                cleanupArgumentStack(argRegs.size());

                return captureReturnValue();
            } finally {
                restoreLiveTemporaries();
            }
        }

        private List<String> evaluateArguments(List<Expression> arguments, CodeGenerator visitor) {
            List<String> argRegs = new ArrayList<>();
            for (int i = 0; i < arguments.size(); i++) {
                emitter.comment("Evaluating argument " + i);
                String reg = arguments.get(i).accept(visitor);
                argRegs.add(reg);
            }
            return argRegs;
        }

        private void pushArgumentsToStack(List<String> argRegs) {
            for (String argReg : argRegs) {
                emitter.push(argReg);
                registerManager.freeRegister(argReg);
            }
        }

        private void cleanupArgumentStack(int argCount) {
            if (argCount > 0) {
                String temp = registerManager.allocateRegister("stack_cleanup");
                try {
                    emitter.loadImmediate(temp, (long) argCount * WORD_SIZE);
                    emitter.add("sp", "sp", temp);
                    emitter.comment("Cleaned up " + argCount + " arguments from stack");
                } finally {
                    registerManager.freeRegister(temp);
                }
            }
        }

        private String captureReturnValue() {
            String resultReg = registerManager.allocateRegister("function_result");
            emitter.move(resultReg, "a0");
            return resultReg;
        }
    }

    /**
     * Save live temporary registers before function call
     */
    private void saveLiveTemporaries() {
        var liveTemps = registerManager.getLiveTemporaries();
        if (!liveTemps.isEmpty()) {
            emitter.comment("Saving " + liveTemps.size() + " live temporaries");
            liveTemps.stream()
                    .sorted((a, b) -> Integer.compare(extractRegisterIndex(b), extractRegisterIndex(a)))
                    .forEach(reg -> {
                        emitter.push(reg);
                        ctx.pushTemporary(reg);
                    });
        }
    }

    /**
     * Restore live temporary registers after function call
     */
    private void restoreLiveTemporaries() {
        var savedTemps = ctx.getSavedTemporaries();
        if (!savedTemps.isEmpty()) {
            emitter.comment("Restoring " + savedTemps.size() + " saved temporaries");
            while (!savedTemps.isEmpty()) {
                String reg = savedTemps.pop();
                emitter.pop(reg);
            }
        }
    }

    /**
     * Extract numeric index from register name (e.g., "t5" -> 5)
     */
    private int extractRegisterIndex(String register) {
        return Integer.parseInt(register.substring(1));
    }
}