package org.lpc.compiler;

import org.lpc.compiler.ast.parent.Program;

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

/*
Language Syntax Overview:

Program:
    program       := { function_definition }

Function Definition:
    function_definition := "func" identifier "(" [ parameter_list ] ")" "{" block "}"
    parameter_list      := identifier { "," identifier }

Block:
    block := { statement }

Statements:
    statement :=
          if_statement
        | while_statement
        | return_statement
        | variable_declaration
        | asm_statement
        | assignment_or_expression_statement
        | ";"   // empty statement

If Statement:
    if_statement :=
        "if" "(" expression ")" "{" block "}" [ "else" "{" block "}" ]

While Statement:
    while_statement :=
        "while" "(" expression ")" "{" block "}"

Return Statement:
    return_statement :=
        "return" [ expression ] ";"

Variable Declaration:
    variable_declaration :=
        "var" identifier "=" expression ";"

Asm Statement:
    asm_statement :=
        "asm" "{" { asm_code_line } "}"

Assignment or Expression Statement:
    assignment_or_expression_statement :=
        (primary_expression "=" expression ";")
        | (expression ";")

Expressions (precedence from low to high):
    expression := logical_or

    logical_or := logical_and { "||" logical_and }
    logical_and := equality { "&&" equality }
    equality := comparison { ("==" | "!=") comparison }
    comparison := additive { ("<" | "<=" | ">" | ">=") additive }
    additive := multiplicative { ("+" | "-") multiplicative }
    multiplicative := unary { ("*" | "/" | "%") unary }
    unary := ( "-" | "!" ) unary | primary_expression

Primary Expressions:
    primary_expression :=
          integer_literal
        | hex_literal
        | variable
        | function_call
        | dereference_expression
        | "(" expression ")"

Function Call:
    function_call :=
        identifier "(" [ argument_list ] ")"
    argument_list :=
        expression { "," expression }

Dereference Expression:
    dereference_expression :=
        "@" primary_expression

Literals:
    integer_literal := [0-9]+
    hex_literal := "0x" [0-9a-fA-F]+

Identifiers:
    identifier := [a-zA-Z_][a-zA-Z0-9_]*
*/

