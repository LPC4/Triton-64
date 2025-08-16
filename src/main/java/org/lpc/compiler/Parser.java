package org.lpc.compiler;

import org.lpc.compiler.ast.parent.StructDef;
import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.StructType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.types.PointerType;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.expressions.Expression;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.statements.Statement;
import org.lpc.compiler.ast.statements.*;

import java.text.ParseException;
import java.util.*;
import java.util.Objects;

public class Parser {
    private final List<String> tokens;
    private int position = 0;
    private final SymbolTable symbolTable = new SymbolTable();
    private final Map<String, Type> typeTable = new HashMap<>();
    private final Map<Type, Long> sizeTable = new HashMap<>();
    private Type currentFunctionReturnType = PrimitiveType.LONG;

    public Parser(Lexer lexer) {
        Objects.requireNonNull(lexer, "Lexer cannot be null");
        this.tokens = lexer.tokenize();
        // Initialize primitive strides
        sizeTable.put(PrimitiveType.BYTE, 1L);
        sizeTable.put(PrimitiveType.INT, 4L);
        sizeTable.put(PrimitiveType.LONG, 8L);
    }

    public Program parse() {
        // Pass 1: Collect type definitions and function signatures
        List<StructDef> structs = new ArrayList<>();
        List<FunctionDef> functionSignatures = new ArrayList<>();
        List<Integer> bodyStartPositions = new ArrayList<>();

        symbolTable.enterScope();

        parsePass1(structs, functionSignatures, bodyStartPositions);

        // Reset position for second pass
        position = 0;

        // Pass 2: Parse actual code with full type information
        try {
            return parsePass2(structs, functionSignatures, bodyStartPositions);
        } finally { symbolTable.exitScope();}
    }

    private void parsePass1(List<StructDef> structs, List<FunctionDef> functionSignatures, List<Integer> bodyStartPositions) {
        while (!isAtEnd()) {
            if (match("struct")) {
                StructDef structDef = parseStruct();
                structs.add(structDef);
                typeTable.put(structDef.getName(), new StructType(structDef.getName(), structDef.getFields()));
            } else if (match("func")) {
                FunctionDef sig = parseFunctionSignature();
                functionSignatures.add(sig);
                // Add function signature to symbol table
                symbolTable.define(sig.getName(), sig.getReturnType());
                if (!check("{")) {
                    throw new RuntimeException("Expected '{' after function signature");
                }
                bodyStartPositions.add(position);
                skipFunctionBody();
            } else {
                // Skip non-type elements in pass 1
                consume();
            }
        }

    }

    private Program parsePass2(List<StructDef> structs, List<FunctionDef> functionSignatures, List<Integer> bodyStartPositions) {
        List<GlobalDeclaration> globals = new ArrayList<>();
        List<FunctionDef> functions = new ArrayList<>();

        // Parse all top-level items (skipping types already processed)
        while (!isAtEnd()) {
            if (match("struct")) {
                skipStruct();
            } else if (match("func")) {
                skipFunctionDefinition();
            } else if (match("global")) {
                GlobalDeclaration global = parseGlobalDeclaration();
                symbolTable.define(global.getName(), global.getType());
                globals.add(global);
            } else {
                throw new RuntimeException("Expected 'global', found: " + peek());
            }
        }

        // Parse function bodies using saved positions
        for (int i = 0; i < functionSignatures.size(); i++) {
            int savedPos = position;
            position = bodyStartPositions.get(i);
            consume("{");

            // Set the current function return type before parsing the body
            Type originalReturnType = currentFunctionReturnType;
            currentFunctionReturnType = functionSignatures.get(i).getReturnType();

            // Enter function scope and add parameters
            symbolTable.enterScope();
            FunctionDef sig = functionSignatures.get(i);
            for (int j = 0; j < sig.getParameters().size(); j++) {
                symbolTable.define(sig.getParameters().get(j), sig.getParameterTypes().get(j));
            }

            // Parse the body with the correct return type
            List<Statement> body = parseBlock();

            // Exit function scope and restore original return type
            symbolTable.exitScope();
            currentFunctionReturnType = originalReturnType;

            functions.add(new FunctionDef(
                    sig.getName(),
                    sig.getParameters(),
                    body,
                    sig.getParameterTypes(),
                    sig.getReturnType()
            ));
            position = savedPos;
        }
        Program program = new Program(globals, functions, structs);
        System.out.println("\nParsed AST:");
        System.out.println(program);
        return program;
    }

    private Statement parseReturnStatement() {
        if (check("}")) {
            return new ReturnStatement(null); // No return value
        }

        // Parse the expression with type information from the function return type
        Expression value = parseExpression(currentFunctionReturnType);

        // Check if we need to convert types
        Type valueType = Type.getExpressionType(value);
        if (valueType != null && !valueType.equals(currentFunctionReturnType)) {
            // Create a type conversion node
            value = new TypeConversion(value, currentFunctionReturnType);
        }
        return new ReturnStatement(value);
    }

    private FunctionCall parseFunctionCall(String name) {
        consume("(");
        List<Expression> args = new ArrayList<>();
        if (!check(")")) {
            do {
                args.add(parseExpression(null));
            } while (match(","));
        }
        consume(")");

        // Look up return type from symbol table
        Type returnType = symbolTable.lookup(name);
        if (returnType == null) {
            throw new RuntimeException("Function not defined: " + name);
        }
        return new FunctionCall(name, args, returnType);
    }

    private void skipStruct() {
        consume(); // consume name
        consume("{");
        int braceCount = 1;
        while (braceCount > 0 && !isAtEnd()) {
            String token = consume();
            if (token.equals("{")) braceCount++;
            else if (token.equals("}")) braceCount--;
        }
        if (braceCount != 0) {
            throw new RuntimeException("Unmatched braces in struct");
        }
    }

    private void skipFunctionDefinition() {
        // Skip the rest
        while (!isAtEnd() && !check("{")) {
            consume();
        }
        if (isAtEnd()) {
            throw new RuntimeException("Unexpected end of file in function definition");
        }
        skipFunctionBody();
    }

    private void skipFunctionBody() {
        consume("{");
        int braceCount = 1;
        while (braceCount > 0 && !isAtEnd()) {
            String token = consume();
            if (token.equals("{")) braceCount++;
            else if (token.equals("}")) braceCount--;
        }
        if (braceCount != 0) {
            throw new RuntimeException("Unmatched braces in function body");
        }
    }

    private StructDef parseStruct() {
        symbolTable.enterScope();
        String name = consume();
        consume("{");
        List<StructType.Field> fields = new ArrayList<>();

        while (!check("}")) {
            consume("var");
            String fieldName = consume();
            Type fieldType;

            if (match(":")) {
                fieldType = parseVariableType();
            } else {
                fieldType = PrimitiveType.LONG;
            }

            // Record field in symbol table for access resolution
            symbolTable.define(fieldName, fieldType);
            fields.add(new StructType.Field(fieldName, fieldType));
        }
        consume("}");

        // Calculate struct size
        long totalSize = fields.stream()
                .mapToLong(field -> field.type().getSize())
                .sum();
        sizeTable.put(new StructType(name, fields), totalSize);

        symbolTable.exitScope();
        return new StructDef(name, fields);
    }

    private GlobalDeclaration parseGlobalDeclaration() {
        String name = consume();
        // typed
        Type type;
        if (match(":")) {
            type = parseVariableType();
        } else { // default to long
            type = PrimitiveType.LONG;
        }
        Expression initializer;
        if (match("=")) {
            initializer = parseExpression(type);
        } else {
            initializer = createLiteral(0, type);
        }
        return new GlobalDeclaration(name, initializer, type);
    }

    private Statement parseDeclaration() {
        String name = consume();

        // Typed
        Type type = null;
        if (match(":")) {
            type = parseVariableType();
        }

        Expression initializer;
        if (match("=")) {
            initializer = parseExpression(type);
        } else {
            initializer = createLiteral(0, type);
        }

        // If still null, determine type from initializer
        if (type == null) {
            type = Type.getExpressionType(initializer) != null
                    ? Type.getExpressionType(initializer)
                    : PrimitiveType.LONG; // Default to LONG if no type specified in initializer
        }

        // Add to symbol table
        symbolTable.define(name, type);
        return new Declaration(name, initializer, type);
    }

    private FunctionDef parseFunctionSignature() {
        String name = consume();
        consume("(");
        List<String> parameters = new ArrayList<>();
        List<Type> parameterTypes = new ArrayList<>();
        if (!check(")")) {
            do {
                String paramName = consume();
                Type paramType;
                if (match(":")) {
                    paramType = parseVariableType();
                } else {
                    paramType = PrimitiveType.LONG;
                }
                parameters.add(paramName);
                parameterTypes.add(paramType);
            } while (match(","));
        }
        consume(")");
        Type returnType = PrimitiveType.LONG;
        if (match(":")) {
            returnType = parseVariableType();
        }
        return new FunctionDef(name, parameters, null, parameterTypes, returnType);
    }

    private List<Statement> parseBlock() {
        symbolTable.enterScope();
        List<Statement> statements = new ArrayList<>();
        while (!check("}") && !isAtEnd()) {
            statements.add(parseStatement());
        }
        consume("}");
        symbolTable.exitScope();
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
        // Parse the left side, which could include array indexing or dereference
        Expression left = parsePostfix(null);
        if (check("=")) {
            consume("=");
            Expression right = parseExpression(null);
            return new AssignmentStatement(left, right);
        }
        // Continue parsing the expression with the left side
        Expression expr = parseExpressionRest(left);
        if (expr instanceof FunctionCall) {
            return new ExpressionStatement(expr);
        }
        return new ExpressionStatement(expr);
    }

    private Expression parseExpression(Type expectedType) {
        return parseLogicalOr(expectedType);
    }

    private Expression parseExpressionRest(Expression left) {
        return parseLogicalOrRest(left, null);
    }

    private Expression parseLogicalOr(Type expectedType) {
        Expression left = parseLogicalAnd(expectedType);
        return parseLogicalOrRest(left, expectedType);
    }

    private Expression parseLogicalOrRest(Expression left, Type expectedType) {
        while (match("||")) {
            String operator = tokens.get(position - 1);
            Expression right = parseLogicalAnd(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseLogicalAnd(Type expectedType) {
        Expression left = parseBitwiseOr(expectedType);
        return parseLogicalAndRest(left, expectedType);
    }

    private Expression parseLogicalAndRest(Expression left, Type expectedType) {
        while (match("&&")) {
            String operator = tokens.get(position - 1);
            Expression right = parseBitwiseOr(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseBitwiseOr(Type expectedType) {
        Expression left = parseBitwiseXor(expectedType);
        return parseBitwiseOrRest(left, expectedType);
    }

    private Expression parseBitwiseOrRest(Expression left, Type expectedType) {
        while (match("|")) {
            String operator = tokens.get(position - 1);
            Expression right = parseBitwiseXor(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseBitwiseXor(Type expectedType) {
        Expression left = parseBitwiseAnd(expectedType);
        return parseBitwiseXorRest(left, expectedType);
    }

    private Expression parseBitwiseXorRest(Expression left, Type expectedType) {
        while (match("^")) {
            String operator = tokens.get(position - 1);
            Expression right = parseBitwiseAnd(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseBitwiseAnd(Type expectedType) {
        Expression left = parseEquality(expectedType);
        return parseBitwiseAndRest(left, expectedType);
    }

    private Expression parseBitwiseAndRest(Expression left, Type expectedType) {
        while (match("&")) {
            String operator = tokens.get(position - 1);
            Expression right = parseEquality(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseEquality(Type expectedType) {
        Expression left = parseComparison(expectedType);
        return parseEqualityRest(left, expectedType);
    }

    private Expression parseEqualityRest(Expression left, Type expectedType) {
        while (match("==", "!=")) {
            String operator = tokens.get(position - 1);
            Expression right = parseComparison(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseComparison(Type expectedType) {
        Expression left = parseShift(expectedType);
        return parseComparisonRest(left, expectedType);
    }

    private Expression parseComparisonRest(Expression left, Type expectedType) {
        while (match("<=", ">=", "<", ">")) {
            String operator = tokens.get(position - 1);
            Expression right = parseShift(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseShift(Type expectedType) {
        Expression left = parseAdditive(expectedType);
        return parseShiftRest(left, expectedType);
    }

    private Expression parseShiftRest(Expression left, Type expectedType) {
        while (match(">>", "<<", ">>>")) {
            String operator = tokens.get(position - 1);
            Expression right = parseAdditive(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseAdditive(Type expectedType) {
        Expression left = parseMultiplicative(expectedType);
        return parseAdditiveRest(left, expectedType);
    }

    private Expression parseAdditiveRest(Expression left, Type expectedType) {
        while (match("+", "-")) {
            String operator = tokens.get(position - 1);
            Expression right = parseMultiplicative(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseMultiplicative(Type expectedType) {
        Expression left = parseUnary(expectedType);
        return parseMultiplicativeRest(left, expectedType);
    }

    private Expression parseMultiplicativeRest(Expression left, Type expectedType) {
        while (match("*", "/", "%")) {
            String operator = tokens.get(position - 1);
            Expression right = parseUnary(expectedType);
            left = new BinaryOp(operator, left, right);
        }
        return left;
    }

    private Expression parseUnary(Type expectedType) {
        if (match("-", "!", "~")) {
            String operator = tokens.get(position - 1);
            Expression right = parseUnary(expectedType);
            return new UnaryOp(operator, right);
        }
        // Handle type casts (byte 5, int x) ONLY when NOT followed by @
        // (Typed dereference is handled in parsePrimary)
        if (check("byte") || check("int") || check("long")) {
            // Check if this is actually a typed dereference (handled in primary)
            if (position + 1 < tokens.size() && tokens.get(position + 1).equals("@")) {
                // This will be handled in parsePrimary, so we don't consume here
                // Let parsePrimary handle it
                return parsePostfix(expectedType);
            }
            // It's a type cast
            Type type = parseVariableType();
            Expression operand = parseUnary(expectedType);
            return new TypeConversion(operand, type);
        }
        return parsePostfix(expectedType);
    }

    private Expression parsePostfix(Type expectedType) {
        Expression expr = parsePrimary(expectedType);

        while (true) {
            // Handle array indexing
            if (match("[")) {
                Expression index = parseExpression(null);
                consume("]");
                expr = new ArrayIndex(expr, index);
            }
            // Handle struct field access
            else if (match(".")) {
                String fieldName = consume();
                // Resolve field type and offset during parsing
                expr = createFieldAccess(expr, fieldName);
            }
            // Handle function calls
            else if (check("(") && expr instanceof Variable var) {
                return parseFunctionCall(var.getName());
            } else {
                break;
            }
        }
        return expr;
    }

    private StructFieldAccess createFieldAccess(Expression baseExpr, String fieldName) {
        Type baseType = Type.getExpressionType(baseExpr);
        if (baseType == null) {
            throw new RuntimeException("Cannot resolve type for base expression in field access: " + baseExpr);
        }

        // Dereference if pointer
        Type structType = baseType.isPtr() ? baseType.asPointer().getElementType() : baseType;

        if (!structType.isStruct()) {
            throw new RuntimeException("Cannot access field '" + fieldName + "' on non-struct type: " + structType);
        }

        StructType structDef = structType.asStruct();

        // Look up field
        for (StructType.Field field : structDef.getFields()) {
            if (field.name().equals(fieldName)) {
                int offset = structDef.getFieldOffset(fieldName);
                return new StructFieldAccess(baseExpr, fieldName, field.type(), offset);
            }
        }

        throw new RuntimeException("Struct '" + structDef.getName() + "' has no field '" + fieldName + "'");
    }

    private Expression parsePrimary(Type expectedType) {
        if (match("strideOf")) {
            return parseStrideOf();
        }

        // Handle typed dereference FIRST (byte@, int@, long@)
        if ((check("byte") || check("int") || check("long")) &&
                position + 1 < tokens.size() &&
                tokens.get(position + 1).equals("@")) {
            Type type = parseVariableType(); // consumes the type token
            consume("@");
            Expression address = parsePostfix(expectedType);
            return new Dereference(address, type);
        }
        // Handle default dereference (@ptr) as long@ptr
        if (match("@")) {
            Expression address = parsePostfix(expectedType);
            return new Dereference(address, PrimitiveType.LONG);
        }
        if (match("(")) {
            Expression expr = parseExpression(expectedType);
            consume(")");
            return expr;
        }
        if (match("[")) {
            return parseArrayLiteral(expectedType);
        }
        if (checkTokenIsInteger()) {
            return createLiteral(Long.parseLong(consume()), expectedType);
        }
        if (checkTokenIsHex()) {
            String hexValue = consume();
            if (hexValue.startsWith("0x") || hexValue.startsWith("0X")) {
                return createLiteral(Long.parseLong(hexValue.substring(2), 16), expectedType);
            }
            throw new RuntimeException("Invalid hex literal: " + hexValue);
        }
        if (checkTokenIsCharLiteral()) {
            String charToken = consume();
            long charValue = parseCharLiteral(charToken);
            return createLiteral(charValue, expectedType);
        }
        if (checkTokenIsIdentifier()) {
            String name = consume();
            // Look up the type in the symbol table
            Type actualType = symbolTable.lookup(name);
            if (actualType == null) {
                // If not found, use expectedType or default to LONG
                actualType = Objects.requireNonNullElse(expectedType, PrimitiveType.LONG);
            }
            return new Variable(name, actualType);
        }
        throw new RuntimeException("Unexpected token: " + peek());
    }

    private Expression parseStrideOf() {
        consume("(");
        Type type = parseVariableType();
        consume(")");
        long stride = computeStride(type);
        return Literal.ofLong(stride);
    }

    private long computeStride(Type type) {
        // Check if already computed (for recursive cases)
        if (sizeTable.containsKey(type)) {
            return sizeTable.get(type);
        }

        if (type instanceof PrimitiveType) {
            Long stride = sizeTable.get(type);
            if (stride == null) {
                throw new RuntimeException("Unknown primitive type for stride: " + type);
            }
            return stride;
        } else if (type instanceof PointerType) {
            sizeTable.put(type, 8L); // Pointers are always 8 bytes on 64-bit systems
            return 8L;
        } else {
            throw new RuntimeException("Cannot compute stride for type: " + type);
        }
    }

    private Statement parseIfStatement() {
        consume("(");
        Expression condition = parseExpression(null);
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
        Expression condition = parseExpression(null);
        consume(")");
        consume("{");
        List<Statement> body = parseBlock();
        return new WhileStatement(condition, body);
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

    private Expression parseArrayLiteral(Type expectedType) {
        List<Expression> elements = new ArrayList<>();

        // Determine element type from expected type
        Type elementType = PrimitiveType.LONG;
        if (expectedType != null && expectedType.isPtr()) {
            elementType = expectedType.asPointer().getElementType();
        }

        if (!check("]")) {
            do {
                elements.add(parseExpression(elementType));
            } while (match(","));
        }
        consume("]");
        return new ArrayLiteral(elements, expectedType);
    }

    private Type parseVariableType() {
        String typeToken = consume();
        Type baseType;

        // Check if it's a struct name
        if (typeTable.containsKey(typeToken)) {
            baseType = typeTable.get(typeToken);
        }
        // Check primitive types
        else {
            baseType = switch (typeToken.toLowerCase()) {
                case "byte" -> PrimitiveType.BYTE;
                case "int" -> PrimitiveType.INT;
                case "long" -> PrimitiveType.LONG;
                default -> throw new RuntimeException("Unknown type: " + typeToken);
            };
        }

        Type currentType = baseType;
        // Handle pointers/arrays
        while (true) {
            if (match("*")) {
                currentType = new PointerType(currentType);
            } else if (match("[")) {
                if (match("]")) {
                    currentType = new PointerType(currentType);
                } else {
                    throw new RuntimeException("Expected ']' after '['");
                }
            } else {
                break;
            }
        }
        return currentType;
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

    /**
     * Creates a Literal expression of the appropriate type based on the expected type
     */
    private Expression createLiteral(long value, Type expectedType) {
        if (expectedType == null) {
            return Literal.ofLong(value);
        }
        switch (expectedType) {
            case PrimitiveType.BYTE:
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    return Literal.ofByte((byte) value);
                }
                break;
            case PrimitiveType.INT:
                if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                    return Literal.ofInt((int) value);
                }
                break;
            case PrimitiveType.LONG:
                return Literal.ofLong(value);
            default:
                if (expectedType.isPtr()) return Literal.ofLong(value);
                throw new IllegalStateException("Unexpected value: " + expectedType);
        }
        throw new RuntimeException("Value " + value + " does not fit in " + expectedType);
    }

    /**
     * Symbol table to track variable types across different scopes
     */
    private static class SymbolTable {
        private final Deque<Map<String, Type>> scopes = new LinkedList<>();
        public void enterScope() {
            scopes.push(new HashMap<>());
        }
        public void exitScope() {
            if (scopes.isEmpty()) {
                throw new RuntimeException("Cannot exit global scope");
            }
            scopes.pop();
        }
        public void define(String name, Type type) {
            if (scopes.isEmpty()) {
                throw new RuntimeException("No scope to define variable in");
            }
            scopes.peek().put(name, type);
        }
        public Type lookup(String name) {
            for (Map<String, Type> scope : scopes) {
                if (scope.containsKey(name)) {
                    return scope.get(name);
                }
            }
            return null; // Not found
        }
    }
}