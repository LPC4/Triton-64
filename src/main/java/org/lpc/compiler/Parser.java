package org.lpc.compiler;

import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<String> tokens;
    private int position = 0;

    public Parser(Lexer lexer) {
        this.tokens = lexer.tokenize();
    }

    public Program parse() {
        List<GlobalDeclaration> globals = new ArrayList<>();
        List<FunctionDef> functions = new ArrayList<>();

        while (!isAtEnd()) {
            if (check("global")) {
                globals.add(parseGlobalDeclaration());
            } else if (check("func")) {
                functions.add(parseFunction());
            } else {
                throw new RuntimeException("Expected 'global' or 'func', found: " + peek());
            }
        }

        Program program = new Program(globals, functions);
        System.out.println("\nParsed AST:");
        System.out.println(program);
        return program;
    }

    private GlobalDeclaration parseGlobalDeclaration() {
        consume("global");
        String name = consume();
        Expression initializer;

        if (match("=")) {
            initializer = parseExpression();
        } else {
            initializer = new LongLiteral(0); // Default to 0
        }

        if (check(";")) consume(); // Optional semicolon
        return new GlobalDeclaration(name, initializer);
    }

    private FunctionDef parseFunction() {
        consume("func");
        String name = consume();
        consume("(");
        List<String> parameters = new ArrayList<>();
        if (!check(")")) {
            do {
                parameters.add(consume());
            } while (match(","));
        }
        consume(")");
        consume("{");
        List<Statement> body = parseBlock();
        return new FunctionDef(name, parameters, body);
    }

    private List<Statement> parseBlock() {
        List<Statement> statements = new ArrayList<>();
        while (!check("}") && !isAtEnd()) {
            if (check(";")) {
                consume();
                continue;
            }
            statements.add(parseStatement());
        }
        consume("}");
        return statements;
    }

    private Statement parseStatement() {
        if (match("if")) return parseIfStatement();
        if (match("return")) return parseReturnStatement();
        if (match("while")) return parseWhileStatement();
        if (match("var")) return parseDeclaration();
        if (match("asm")) return parseAsmStatement();

        return parseAssignmentOrExpressionStatement();
    }

    private Statement parseAssignmentOrExpressionStatement() {
        // Parse the left side, which could include array indexing
        Expression left = parsePostfix();

        if (check("=")) {
            consume("=");
            Expression right = parseExpression();
            if (check(";")) consume();
            return new AssignmentStatement(left, right);
        }

        // Continue parsing the expression with the left side
        Expression expr = parseExpressionRest(left);

        if (expr instanceof FunctionCall) {
            if (check(";")) consume();
            return new ExpressionStatement(expr);
        }

        if (check(";")) consume();
        return new ExpressionStatement(expr);
    }

    private Expression parseExpression() {
        return parseLogicalOr();
    }

    private Expression parseExpressionRest(Expression left) {
        return parseLogicalOrRest(left);
    }

    private Expression parseLogicalOr() {
        Expression left = parseLogicalAnd();
        return parseLogicalOrRest(left);
    }

    private Expression parseLogicalOrRest(Expression left) {
        while (match("||")) {
            String operator = tokens.get(position - 1);
            Expression right = parseLogicalAnd();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseLogicalOr(Expression left) {
        while (match("||")) {
            String operator = tokens.get(position - 1);
            Expression right = parseLogicalAnd();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseLogicalAnd() {
        Expression left = parseBitwiseOr();
        return parseLogicalAndRest(left);
    }

    private Expression parseLogicalAndRest(Expression left) {
        while (match("&&")) {
            String operator = tokens.get(position - 1);
            Expression right = parseBitwiseOr();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseBitwiseOr() {
        Expression left = parseBitwiseXor();
        return parseBitwiseOrRest(left);
    }

    private Expression parseBitwiseOrRest(Expression left) {
        while (match("|")) {
            String operator = tokens.get(position - 1);
            Expression right = parseBitwiseXor();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseBitwiseXor() {
        Expression left = parseBitwiseAnd();
        return parseBitwiseXorRest(left);
    }

    private Expression parseBitwiseXorRest(Expression left) {
        while (match("^")) {
            String operator = tokens.get(position - 1);
            Expression right = parseBitwiseAnd();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseBitwiseAnd() {
        Expression left = parseEquality();
        return parseBitwiseAndRest(left);
    }

    private Expression parseBitwiseAndRest(Expression left) {
        while (match("&")) {
            String operator = tokens.get(position - 1);
            Expression right = parseEquality();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseEquality() {
        Expression left = parseComparison();
        return parseEqualityRest(left);
    }

    private Expression parseEqualityRest(Expression left) {
        while (match("==", "!=")) {
            String operator = tokens.get(position - 1);
            Expression right = parseComparison();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseComparison() {
        Expression left = parseShift();
        return parseComparisonRest(left);
    }

    private Expression parseComparisonRest(Expression left) {
        while (match("<=", ">=", "<", ">")) {
            String operator = tokens.get(position - 1);
            Expression right = parseShift();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseShift() {
        Expression left = parseAdditive();
        return parseShiftRest(left);
    }

    private Expression parseShiftRest(Expression left) {
        while (match(">>", "<<", ">>>")) {
            String operator = tokens.get(position - 1);
            Expression right = parseAdditive();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseAdditive() {
        Expression left = parseMultiplicative();
        return parseAdditiveRest(left);
    }

    private Expression parseAdditiveRest(Expression left) {
        while (match("+", "-")) {
            String operator = tokens.get(position - 1);
            Expression right = parseMultiplicative();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseMultiplicative() {
        Expression left = parseUnary();
        return parseMultiplicativeRest(left);
    }

    private Expression parseMultiplicativeRest(Expression left) {
        while (match("*", "/", "%")) {
            String operator = tokens.get(position - 1);
            Expression right = parseUnary();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseUnary() {
        if (match("-", "!", "~")) {
            String operator = tokens.get(position - 1);
            Expression right = parseUnary();
            return new UnaryOp(operator, right);
        }
        return parsePostfix();
    }

    // NEW: parsePostfix handles array indexing and function calls
    private Expression parsePostfix() {
        Expression expr = parsePrimary();

        while (true) {
            if (match("[")) {
                // Array indexing
                Expression index = parseExpression();
                consume("]");
                expr = new ArrayIndex(expr, index);
            } else if (check("(") && expr instanceof Variable var) {
                return parseFunctionCall(var.name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expression parsePrimary() {
        if (match("(")) {
            Expression expr = parseExpression();
            consume(")");
            return expr;
        }
        if (match("[")) {
            return parseArrayLiteral();
        }
        if (match("@")) {
            Expression address = parsePostfix();
            return new Dereference(address);
        }
        if (checkTokenIsInteger()) {
            return new LongLiteral(Long.parseLong(consume()));
        }
        if (checkTokenIsHex()) {
            String hexValue = consume();
            if (hexValue.startsWith("0x") || hexValue.startsWith("0X")) {
                return new LongLiteral(Long.parseLong(hexValue.substring(2), 16));
            }
            throw new RuntimeException("Invalid hex literal: " + hexValue);
        }
        if (checkTokenIsCharLiteral()) {
            String charToken = consume();
            long charValue = parseCharLiteral(charToken);
            return new LongLiteral(charValue);
        }
        if (checkTokenIsIdentifier()) {
            String name = consume();
            return new Variable(name);
        }
        throw new RuntimeException("Unexpected token: " + peek());
    }

    private Statement parseIfStatement() {
        consume("(");
        Expression condition = parseExpression();
        consume(")");
        consume("{");
        List<Statement> thenBranch = parseBlock();
        List<Statement> elseBranch = null;

        if (match("else")) {
            if (match("if")) {
                elseBranch = new ArrayList<>();
                elseBranch.add(parseIfStatement());
            } else {
                consume("{");
                elseBranch = parseBlock();
            }
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement parseWhileStatement() {
        consume("(");
        Expression condition = parseExpression();
        consume(")");
        consume("{");
        List<Statement> body = parseBlock();
        return new WhileStatement(condition, body);
    }

    private Statement parseDeclaration() {
        String name = consume();
        if (match("=")) {
            Expression initializer = parseExpression();
            return new Declaration(name, initializer);
        } else {
            return new Declaration(name, new LongLiteral(0));
        }
    }

    private Statement parseReturnStatement() {
        if (check(";") || check("}")) {
            if (check(";")) consume();
            return new ReturnStatement(null);
        }
        Expression value = parseExpression();
        if (check(";")) consume();
        return new ReturnStatement(value);
    }

    private Statement parseAsmStatement() {
        consume("{");
        StringBuilder asmCode = new StringBuilder();
        while (!check("}")) {
            String token = consume();
            asmCode.append(token).append("\n");
        }
        consume("}");
        return new AsmStatement(asmCode.toString());
    }

    private FunctionCall parseFunctionCall(String name) {
        consume("(");
        List<Expression> args = new ArrayList<>();
        if (!check(")")) {
            do {
                args.add(parseExpression());
            } while (match(","));
        }
        consume(")");
        return new FunctionCall(name, args);
    }

    /**
     * Parses a character literal and returns its numeric value.
     * Supports:
     * - Regular characters: 'a', 'B', '5'
     * - Escape sequences: '\n', '\t', '\r', '\\', '\'', '\"'
     * - Octal sequences: '\123' (up to 3 digits)
     * - Hex sequences: '\x41', '\X41' (exactly 2 hex digits)
     */
    private long parseCharLiteral(String charToken) {
        if (charToken.length() < 3 || !charToken.startsWith("'") || !charToken.endsWith("'")) {
            throw new RuntimeException("Invalid character literal: " + charToken);
        }

        String content = charToken.substring(1, charToken.length() - 1);

        // Handle escape sequences
        if (content.startsWith("\\")) {
            if (content.length() == 1) {
                throw new RuntimeException("Incomplete escape sequence in character literal: " + charToken);
            }

            char escapeChar = content.charAt(1);
            switch (escapeChar) {
                case 'n': return '\n';
                case 't': return '\t';
                case 'r': return '\r';
                case 'b': return '\b';
                case 'f': return '\f';
                case '\\': return '\\';
                case '\'': return '\'';
                case '\"': return '\"';
                case '0': return '\0';
                case 'x': case 'X':
                    // Hexadecimal escape sequence
                    if (content.length() != 4) {
                        throw new RuntimeException("Invalid hex escape sequence in character literal: " + charToken);
                    }
                    try {
                        return Integer.parseInt(content.substring(2), 16);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid hex digits in character literal: " + charToken);
                    }
                default:
                    // Octal escape sequence
                    if (Character.isDigit(escapeChar)) {
                        StringBuilder octalStr = new StringBuilder();
                        for (int i = 1; i < content.length() && i < 4; i++) {
                            char c = content.charAt(i);
                            if (c >= '0' && c <= '7') {
                                octalStr.append(c);
                            } else {
                                break;
                            }
                        }
                        if (octalStr.isEmpty()) {
                            throw new RuntimeException("Invalid octal escape sequence in character literal: " + charToken);
                        }
                        try {
                            return Integer.parseInt(octalStr.toString(), 8);
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid octal digits in character literal: " + charToken);
                        }
                    }
                    throw new RuntimeException("Unknown escape sequence in character literal: " + charToken);
            }
        } else {
            // Regular character
            if (content.length() != 1) {
                throw new RuntimeException("Character literal must contain exactly one character: " + charToken);
            }
            return content.charAt(0);
        }
    }

    private Expression parseArrayLiteral() {
        List<Expression> elements = new ArrayList<>();

        if (!check("]")) {
            do {
                elements.add(parseExpression());
            } while (match(","));
        }

        consume("]");
        return new ArrayLiteral(elements);
    }

    private String consume() {
        return tokens.get(position++);
    }

    private void consume(String expected) {
        String actual = consume();
        if (!actual.equals(expected)) {
            throw new RuntimeException("Expected '" + expected + "' but found '" + actual + "'");
        }
    }

    private boolean match(String... options) {
        for (String option : options) {
            if (check(option)) {
                consume();
                return true;
            }
        }
        return false;
    }

    private boolean check(String token) {
        return !isAtEnd() && tokens.get(position).equals(token);
    }

    private String peek() {
        return tokens.get(position);
    }

    private boolean isAtEnd() {
        return position >= tokens.size();
    }

    private boolean checkTokenIsInteger() {
        return !isAtEnd() && peek().matches("\\d+");
    }

    private boolean checkTokenIsIdentifier() {
        return !isAtEnd() && peek().matches("@?[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private boolean checkTokenIsHex() {
        return (!isAtEnd() && (peek().matches("0x[0-9a-fA-F]+") || peek().matches("0X[0-9a-fA-F]+")));
    }

    private boolean checkTokenIsCharLiteral() {
        if (isAtEnd()) {
            return false;
        }
        String token = peek();
        return token.startsWith("'") && token.endsWith("'") && token.length() >= 3;
    }
}