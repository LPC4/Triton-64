package org.lpc.compiler;

import org.lpc.compiler.ast.parent.Program;

@SuppressWarnings("FieldCanBeLocal")
public class TriCCompiler {
    private final Lexer lexer;
    private final Parser parser;

    public TriCCompiler(String sourceCode) {
        this.lexer = new Lexer(sourceCode); // Tokenize the source code
        this.parser = new Parser(lexer);    // Parse the tokens into an AST

    }

    public void compile() {
        Program program = parser.parse();
        System.out.println("Compilation complete. AST generated.");
    }
}
