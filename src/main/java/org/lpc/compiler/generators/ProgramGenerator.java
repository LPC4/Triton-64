package org.lpc.compiler.generators;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.parent.*;
import org.lpc.compiler.ast.statements.*;
import org.lpc.compiler.context_managers.*;

import java.util.List;

/**
 * Handles code generation at the program level.
 */
public class ProgramGenerator {
    private final InstructionGenerator emitter;
    private final FunctionGenerator functionGenerator;
    private final GlobalManager globalManager;
    private final ContextManager context;

    // Reference to the main generator for recursive calls
    private final AstVisitor<String> astVisitor;

    public ProgramGenerator(
            InstructionGenerator emitter,
            FunctionGenerator functionGenerator,
            GlobalManager globalManager,
            ContextManager context,
            CodeGenerator astVisitor) {
        this.emitter = emitter;
        this.functionGenerator = functionGenerator;
        this.globalManager = globalManager;
        this.context = context;
        this.astVisitor = astVisitor;
    }

    public String visitProgram(Program program) {
        emitter.comment("Program entry point");
        emitter.label("_start");

        // Initialize global state
        globalManager.allocateGlobals(program.getGlobals());
        globalManager.initializeGlobals(program.getGlobals(), astVisitor);

        // Setup main execution
        emitter.jumpAndLink("main", "ra");
        emitter.halt();

        // Generate all function definitions
        program.getFunctions().forEach(func -> {
            try {
                func.accept(astVisitor);
            } catch (Exception e) {
                throw new CodeGenerator.CodeGenerationException("Failed to generate function: " + func.getName(), e);
            }
        });

        return null;
    }

    public String visitFunctionDef(FunctionDef functionDef) {
        final String functionName = functionDef.getName();

        try {
            String currentFunctionEndLabel = context.generateLabel(functionName + "_end");
            context.setCurrentFunctionEndLabel(currentFunctionEndLabel);

            functionGenerator.generateFunction(
                    functionName,
                    functionDef.getParameters(),
                    functionDef.getParameterTypes(),
                    functionDef.getBody(),
                    currentFunctionEndLabel,
                    this
            );

            return null;
        } finally {
            context.setCurrentFunctionEndLabel(null);
        }
    }

    public String visitGlobalDeclaration(GlobalDeclaration ignoredGlobalDeclaration) {
        // Global declarations are handled in Program.visit()
        return null;
    }

    public String visitStructDef(StructDef ignoredStructDef) {
        // Struct definitions are not directly translated to assembly
        // but can be used for type checking and validation
        return null;
    }

    public void generateStatements(List<Statement> statements) {
        statements.forEach(stmt -> stmt.accept(astVisitor));
    }
}