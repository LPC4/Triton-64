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
        List<FunctionDef> functions = new ArrayList<>();
        while (!isAtEnd()) {
            functions.add(parseFunction());
        }
        Program program = new Program(functions);

        // Print the AST after parsing
        System.out.println("\nParsed AST:");
        System.out.println(program);
        return program;
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
            // Skip semicolons (comments)
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
        if (checkTokenIsIdentifier() && check(1, "=")) {
            return parseAssignment();
        }
        Expression expr = parseExpression();
        if (expr instanceof FunctionCall) {
            return new ExpressionStatement(expr);
        }
        throw new RuntimeException("Invalid statement: " + expr);
    }

    private Statement parseIfStatement() {
        consume("(");
        Expression condition = parseExpression();
        consume(")");
        consume("{");
        List<Statement> thenBranch = parseBlock();
        List<Statement> elseBranch = null;
        if (match("else")) {
            consume("{");
            elseBranch = parseBlock();
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
        consume("=");
        Expression initializer = parseExpression();
        return new Declaration(name, initializer);
    }

    private Statement parseReturnStatement() {
        if (check(";") || check("}")) {
            // Skip semicolon if present
            if (check(";")) consume();
            return new ReturnStatement(null);
        }
        Expression value = parseExpression();
        // Skip semicolon if present
        if (check(";")) consume();
        return new ReturnStatement(value);
    }

    private Statement parseAssignment() {
        String name = consume();
        consume("=");
        Expression value = parseExpression();
        // Skip semicolon if present
        if (check(";")) consume();
        return new AssignmentStatement(name, value);
    }

    private Expression parseExpression() {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() {
        Expression left = parseLogicalAnd();
        while (match("||")) {
            String operator = tokens.get(position - 1);
            Expression right = parseLogicalAnd();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseLogicalAnd() {
        Expression left = parseEquality();
        while (match("&&")) {
            String operator = tokens.get(position - 1);
            Expression right = parseEquality();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseEquality() {
        Expression left = parseComparison();
        while (match("==", "!=")) {
            String operator = tokens.get(position - 1);
            Expression right = parseComparison();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseComparison() {
        Expression left = parseAdditive();
        while (match("<=", ">=", "<", ">")) {
            String operator = tokens.get(position - 1);
            Expression right = parseAdditive();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseAdditive() {
        Expression left = parseMultiplicative();
        while (match("+", "-")) {
            String operator = tokens.get(position - 1);
            Expression right = parseMultiplicative();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseMultiplicative() {
        Expression left = parseUnary();
        while (match("*", "/", "%")) {
            String operator = tokens.get(position - 1);
            Expression right = parseUnary();
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseUnary() {
        if (match("-", "!")) {
            String operator = tokens.get(position - 1);
            Expression right = parseUnary();
            return new UnaryOp(operator, right);
        }
        return parsePrimary();
    }

    private Expression parsePrimary() {
        if (match("(")) {
            Expression expr = parseExpression();
            consume(")");
            return expr;
        }
        if (checkTokenIsInteger()) {
            return new IntegerLiteral(Integer.parseInt(consume()));
        }
        if (checkTokenIsIdentifier()) {
            String name = consume();
            if (match("(")) {
                return parseFunctionCall(name);
            }
            return new Variable(name);
        }
        throw new RuntimeException("Unexpected token: " + peek());
    }

    private FunctionCall parseFunctionCall(String name) {
        List<Expression> args = new ArrayList<>();
        if (!check(")")) {
            do {
                args.add(parseExpression());
            } while (match(","));
        }
        consume(")");
        return new FunctionCall(name, args);
    }

    // Helper methods
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

    private boolean check(int offset, String token) {
        return position + offset < tokens.size() &&
                tokens.get(position + offset).equals(token);
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
        return !isAtEnd() && peek().matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
}