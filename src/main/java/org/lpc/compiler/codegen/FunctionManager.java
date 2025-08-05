package org.lpc.compiler.codegen;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.Declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages function generation including prologue, epilogue, and calling conventions.
 * Handles the complexity of function setup and teardown.
 */
public class FunctionManager {
    private final CodeGenContext ctx;
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final StackManager stackManager;

    public FunctionManager(CodeGenContext ctx, InstructionEmitter emitter,
                           RegisterManager registerManager, StackManager stackManager) {
        this.ctx = ctx;
        this.emitter = emitter;
        this.registerManager = registerManager;
        this.stackManager = stackManager;
    }

    /**
     * Generate a complete function with prologue and epilogue
     */
    public void generateFunction(String name, List<String> parameters, List<Statement> body,
                                 CodeGenerator.StatementBlockGenerator bodyGenerator, String endLabel) {
        emitter.sectionHeader("Function: " + name);
        emitter.label(name);

        // Reset register state for this function
        registerManager.reset();
        stackManager.startFunctionFrame(parameters);

        try {
            // Pre-analyze the function body to count local variables
            int localVarCount = countLocalVariables(body, parameters);

            // Generate prologue with correct stack allocation
            generateFunctionPrologue(localVarCount);

            // Process declarations first (this will assign offsets but won't allocate more space)
            generateDeclarations(body, parameters, bodyGenerator);

            // Generate function body (non-declaration statements)
            generateNonDeclarationStatements(body, bodyGenerator);

            generateFunctionEpilogue(endLabel, name);
        } finally {
            stackManager.endFunctionFrame();

            // Validate all registers were properly freed
            try {
                registerManager.validateAllFreed();
            } catch (Exception e) {
                emitter.comment("WARNING: Register leak detected in function " + name);
                emitter.comment("Debug info: " + registerManager.getDebugInfo());
            }
        }
    }

    /**
     * Generate function prologue with pre-calculated stack space
     */
    private void generateFunctionPrologue(int localVarCount) {
        emitter.comment("Function prologue");
        emitter.push("ra");           // Save return address
        emitter.push("fp");           // Save frame pointer
        emitter.move("fp", "sp");     // Set new frame pointer

        // Allocate space for local variables
        if (localVarCount > 0) {
            int frameSize = localVarCount * 8; // 8 bytes per variable
            int alignedSize = (frameSize + 15) & ~15; // Align to 16 bytes
            String temp = registerManager.allocateRegister("frame_alloc");
            emitter.loadImmediate(temp, alignedSize);
            emitter.subtract("sp", "sp", temp);
            registerManager.freeRegister(temp);
            emitter.comment("Allocated " + alignedSize + " bytes for " + localVarCount + " local variables");
        }
    }

    /**
     * Generate function epilogue
     */
    private void generateFunctionEpilogue(String endLabel, String functionName) {
        emitter.comment("Function epilogue");
        emitter.label(endLabel);

        // Deallocate local variables - use the actual frame size
        int frameSize = stackManager.getFrameSize() * 8; // 8 bytes per variable
        if (frameSize > 0) {
            int alignedSize = (frameSize + 15) & ~15; // Must match prologue alignment
            String temp = registerManager.allocateRegister("frame_dealloc");
            emitter.loadImmediate(temp, alignedSize);
            emitter.add("sp", "sp", temp);
            registerManager.freeRegister(temp);
            emitter.comment("Deallocated " + alignedSize + " bytes for frame");
        }

        emitter.pop("fp");            // Restore frame pointer
        emitter.pop("ra");            // Restore return address
        emitter.instructionWithComment("Return from " + functionName, "JMP", "ra");
    }

    /**
     * Generate a function call with arguments pushed onto the stack
     */
    public String generateFunctionCall(String functionName, List<Expression> arguments, CodeGenerator visitor) {
        emitter.comment("Preparing function call: " + functionName);

        // Save live temporaries before function call
        saveLiveTemporaries();

        try {
            // Evaluate all arguments and push onto the stack in reverse order
            List<String> argRegs = evaluateArguments(arguments, visitor);
            pushArgumentsToStack(argRegs);

            // Make the function call
            emitter.jumpAndLink(functionName, "ra");

            // Clean up the stack after the call
            if (!argRegs.isEmpty()) {
                String temp = registerManager.allocateRegister("stack_cleanup");
                emitter.loadImmediate(temp, (long) argRegs.size() * 8);
                emitter.add("sp", "sp", temp);
                registerManager.freeRegister(temp);
                emitter.comment("Cleaned up " + argRegs.size() + " arguments from stack");
            }

            // Move return value to a temporary register
            String resultReg = registerManager.allocateRegister("function_result");
            emitter.move(resultReg, "a0");

            return resultReg;
        } finally {
            // Restore live temporaries after function call
            restoreLiveTemporaries();
        }
    }

    /**
     * Evaluate function arguments into temporary registers
     */
    private List<String> evaluateArguments(List<Expression> arguments, CodeGenerator visitor) {
        List<String> argRegs = new ArrayList<>();

        for (int i = 0; i < arguments.size(); i++) {
            emitter.comment("Evaluating argument " + i);
            String reg = arguments.get(i).accept(visitor);
            argRegs.add(reg);
        }

        return argRegs;
    }

    /**
     * Push evaluated arguments onto the stack in reverse order
     */
    private void pushArgumentsToStack(List<String> argRegs) {
        for (String argReg : argRegs) {
            emitter.push(argReg);
            registerManager.freeRegister(argReg);
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
                        ctx.getSavedTemporaries().push(reg);
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

    /**
     * Count local variables in function body (excluding parameters)
     * This includes ALL declarations at any nesting level
     */
    private int countLocalVariables(List<Statement> body, List<String> parameters) {
        return countDeclarationsRecursively(body, parameters);
    }

    /**
     * Recursively count all declarations in statements, including nested blocks
     */
    private int countDeclarationsRecursively(List<Statement> statements, List<String> parameters) {
        int count = 0;

        for (Statement stmt : statements) {
            if (stmt instanceof Declaration) {
                Declaration decl = (Declaration) stmt;
                if (!parameters.contains(decl.name)) {
                    count++;
                }
            } else {
                // Handle nested statements (if, while, etc.)
                count += countNestedDeclarations(stmt, parameters);
            }
        }

        return count;
    }

    /**
     * Count declarations in nested statement structures
     */
    private int countNestedDeclarations(Statement stmt, List<String> parameters) {
        // You'll need to add these methods based on your AST structure
        // This is a placeholder - implement based on your actual AST nodes

        if (stmt instanceof org.lpc.compiler.ast.statements.IfStatement) {
            org.lpc.compiler.ast.statements.IfStatement ifStmt =
                    (org.lpc.compiler.ast.statements.IfStatement) stmt;
            int count = countDeclarationsRecursively(ifStmt.thenBranch, parameters);
            if (ifStmt.elseBranch != null) {
                count += countDeclarationsRecursively(ifStmt.elseBranch, parameters);
            }
            return count;
        }

        if (stmt instanceof org.lpc.compiler.ast.statements.WhileStatement) {
            org.lpc.compiler.ast.statements.WhileStatement whileStmt =
                    (org.lpc.compiler.ast.statements.WhileStatement) stmt;
            return countDeclarationsRecursively(whileStmt.body, parameters);
        }

        // Add other nested statement types as needed

        return 0;
    }

    /**
     * Generate only declaration statements
     */
    private void generateDeclarations(List<Statement> body, List<String> parameters,
                                      CodeGenerator.StatementBlockGenerator bodyGenerator) {
        List<Statement> declarations = body.stream()
                .filter(Declaration.class::isInstance)
                .filter(stmt -> {
                    Declaration decl = (Declaration) stmt;
                    return !parameters.contains(decl.name);
                })
                .toList();

        if (!declarations.isEmpty()) {
            emitter.comment("Processing local variable declarations");
            bodyGenerator.generate(declarations);
        }
    }

    /**
     * Generate non-declaration statements
     */
    private void generateNonDeclarationStatements(List<Statement> body,
                                                  CodeGenerator.StatementBlockGenerator bodyGenerator) {
        List<Statement> nonDeclarations = body.stream()
                .filter(stmt -> !(stmt instanceof Declaration))
                .toList();

        if (!nonDeclarations.isEmpty()) {
            emitter.comment("Processing function body");
            bodyGenerator.generate(nonDeclarations);
        }
    }
}