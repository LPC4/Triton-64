package org.lpc.compiler;

import org.lpc.compiler.ast.parent.Program;

public class TypeChecker {
    private final Program program;

    public TypeChecker(Parser parser) {
        this.program = parser.parse(); // Parse the source code into an AST
    }

    public Program checkTypes() {
        return program; // TODO: implement type checking logic
    }
}
