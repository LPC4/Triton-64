package org.lpc.compiler;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class TriCCompiler {
    private final Linker linker;
    private final Lexer lexer;
    private final Parser parser;
    private final CodeGenerator codeGenerator;

    public TriCCompiler(String sourceCode) {
        this.linker = new Linker(sourceCode);           // Concat source files into one string
        this.lexer = new Lexer(linker);                 // Tokenize the source code
        this.parser = new Parser(lexer);                // Parse the tokens into an AST
        this.codeGenerator = new CodeGenerator(parser); // Generate code from the AST
    }

    public List<String> compile() {
        return codeGenerator.generate();
    }
}