package org.lpc.compiler.generators;

import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.ast.expressions.Expression;
import org.lpc.compiler.ast.statements.Statement;
import org.lpc.compiler.ast.statements.Declaration;
import org.lpc.compiler.ast.statements.IfStatement;
import org.lpc.compiler.ast.statements.WhileStatement;
import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.context_managers.ContextManager;
import org.lpc.compiler.context_managers.RegisterManager;
import org.lpc.compiler.context_managers.StackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manages function generation with proper stack frame allocation based on actual variable sizes.
 * Uses natural alignment for variables to minimize memory usage.
 */
public class FunctionGenerator {
    private static final int WORD_SIZE = 8; // For register-based arguments

    private final ContextManager ctx;
    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;

    public FunctionGenerator(ContextManager ctx, InstructionGenerator emitter,
                             RegisterManager registerManager, StackManager stackManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
    }

    public void generateFunction(String name, List<String> parameters, List<Type> parameterTypes, List<Statement> body,
                                 String endLabel, ProgramGenerator programGenerator) {
        emitter.sectionHeader("Function: " + name);
        emitter.label(name);

        registerManager.reset();
        stackManager.startFunctionFrame(parameters, parameterTypes);

        // Pre-analyze and allocate all local variables
        FunctionAnalyzer analyzer = new FunctionAnalyzer();
        FunctionInfo info = analyzer.analyze(body, parameters);
        preAllocateLocalVariables(info.declarations());

        generateFunctionPrologue();
        programGenerator.generateStatements(body);
        generateFunctionEpilogue(endLabel);

        stackManager.endFunctionFrame();
        validateRegisterState(name);
    }

    private void preAllocateLocalVariables(List<Statement> declarations) {
        emitter.comment("Pre-allocating " + declarations.size() + " local variables");

        for (Statement stmt : declarations) {
            if (stmt instanceof Declaration decl) {
                Type type = decl.getType() != null ? decl.getType() : PrimitiveType.LONG;
                stackManager.allocateVariable(decl.getName(), type);
                emitter.comment("Pre-allocated variable: " + decl.getName() + " (" + type + ")");
            }
        }
    }

    private void generateFunctionPrologue() {
        emitter.comment("Function prologue");
        emitter.push("ra");
        emitter.push("fp");
        emitter.move("fp", "sp");

        int frameSize = stackManager.getTotalFrameSize();
        emitter.comment("Total frame size: " + frameSize + " bytes");
        if (frameSize > 0) {
            allocateStackSpace(frameSize);
        }
    }

    private void generateFunctionEpilogue(String endLabel) {
        emitter.comment("Function epilogue");
        emitter.label(endLabel);

        int frameSize = stackManager.getTotalFrameSize();
        if (frameSize > 0) {
            deallocateStackSpace(frameSize);
        }

        emitter.pop("fp");
        emitter.pop("ra");
        emitter.instructionWithComment("Return from function", "JMP", "ra");
    }

    private void allocateStackSpace(int frameSize) {
        String temp = registerManager.allocateRegister("frame_alloc");
        try {
            emitter.loadImmediate(temp, String.valueOf(frameSize));
            emitter.subtract("sp", "sp", temp);
            emitter.comment("Allocated " + frameSize + " bytes for stack frame");
        } finally {
            registerManager.freeRegister(temp);
        }
    }

    private void deallocateStackSpace(int frameSize) {
        String temp = registerManager.allocateRegister("frame_dealloc");
        try {
            emitter.loadImmediate(temp, String.valueOf(frameSize));
            emitter.add("sp", "sp", temp);
            emitter.comment("Deallocated " + frameSize + " bytes for frame");
        } finally {
            registerManager.freeRegister(temp);
        }
    }

    private void validateRegisterState(String functionName) {
        try {
            registerManager.validateAllFreed();
        } catch (Exception e) {
            emitter.comment("WARNING: Register leak detected in function " + functionName);
            emitter.comment("Debug info: " + registerManager.getDebugInfo());
            throw new RuntimeException("Register leak detected in function " + functionName, e);
        }
    }

    public String generateFunctionCall(String functionName, List<Expression> arguments, CodeGenerator visitor) {
        emitter.comment("Preparing function call: " + functionName);

        if (arguments.size() > 7) {
            throw new IllegalArgumentException("Too many arguments for function: " + functionName);
        }

        return new FunctionCallGenerator().generateCall(functionName, arguments, visitor);
    }

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
            // Push arguments in reverse order so first argument ends up at lowest address
            for (int i = argRegs.size() - 1; i >= 0; i--) {
                String argReg = argRegs.get(i);
                emitter.push(argReg);
                registerManager.freeRegister(argReg);
            }
        }

        private void cleanupArgumentStack(int argCount) {
            if (argCount > 0) {
                String temp = registerManager.allocateRegister("stack_cleanup");
                try {
                    emitter.loadImmediate(temp, String.valueOf(argCount * WORD_SIZE));
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

    private int extractRegisterIndex(String register) {
        return Integer.parseInt(register.substring(1));
    }

    private record FunctionInfo(int localVariableCount, List<Statement> declarations, List<Statement> otherStatements) {}

    private static class FunctionAnalyzer {
        public FunctionInfo analyze(List<Statement> body, List<String> parameters) {
            List<Statement> declarations = extractDeclarations(body, parameters);
            List<Statement> otherStatements = extractNonDeclarations(body);
            int localVarCount = declarations.size();

            return new FunctionInfo(localVarCount, declarations, otherStatements);
        }

        private List<Statement> extractDeclarations(List<Statement> body, List<String> parameters) {
            Set<String> paramSet = Set.copyOf(parameters);
            List<Statement> allDeclarations = new ArrayList<>();

            // Recursively find all declarations in the function body
            findDeclarations(body, paramSet, allDeclarations);

            return allDeclarations;
        }

        private void findDeclarations(List<Statement> statements, Set<String> paramSet, List<Statement> allDeclarations) {
            for (Statement stmt : statements) {
                switch (stmt) {
                    case Declaration decl when !paramSet.contains(decl.getName()) -> {
                        allDeclarations.add(stmt);
                    }
                    case IfStatement ifStmt -> {
                        findDeclarations(ifStmt.getThenBranch(), paramSet, allDeclarations);
                        if (ifStmt.getElseBranch() != null) {
                            findDeclarations(ifStmt.getElseBranch(), paramSet, allDeclarations);
                        }
                    }
                    case WhileStatement whileStmt -> {
                        findDeclarations(whileStmt.getBody(), paramSet, allDeclarations);
                    }
                    default -> {
                        // Other statement types don't contain declarations
                    }
                }
            }
        }

        private List<Statement> extractNonDeclarations(List<Statement> body) {
            return body.stream()
                    .filter(stmt -> !(stmt instanceof Declaration))
                    .toList();
        }
    }
}