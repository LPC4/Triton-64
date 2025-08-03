package org.lpc.compiler;

import org.lpc.compiler.ast.parent.Program;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class TriCCompiler {
    private final Lexer lexer;
    private final Parser parser;
    private final CodeGenerator codeGenerator;

    public TriCCompiler(String sourceCode) {
        this.lexer = new Lexer(sourceCode);             // Tokenize the source code
        this.parser = new Parser(lexer);                // Parse the tokens into an AST
        this.codeGenerator = new CodeGenerator(parser); // Generate code from the AST
    }

    public List<String> compile() {
        return codeGenerator.generate();
    }
}
