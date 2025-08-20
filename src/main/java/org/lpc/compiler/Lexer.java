package org.lpc.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lexer {
    private static final String SPECIAL_CHARS = "~@(){}[]=-,+-*/%<>!&:|.?";
    private final String input;
    private int position = 0;

    private enum Mode {
        NORMAL,
        SINGLE_QUOTE,
        DOUBLE_QUOTE,
        ASM_BLOCK
    }

    public Lexer(Linker linker) {
        Objects.requireNonNull(linker, "Linker cannot be null");
        this.input = linker.link();
    }

    public List<String> tokenize() {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        Mode mode = Mode.NORMAL;

        while (position < input.length()) {
            if (mode == Mode.ASM_BLOCK) {
                StringBuilder line = new StringBuilder();
                while (position < input.length() && input.charAt(position) != '\n') {
                    line.append(input.charAt(position));
                    position++;
                }
                if (position < input.length()) {
                    position++; // skip newline
                }
                String token = line.toString().trim();
                if (!token.isEmpty()) {
                    tokens.add(token);
                    if (token.equals("}")) {
                        mode = Mode.NORMAL;
                    }
                }
                continue;
            }

            // Handle quote modes: collect content until closing quote
            if (mode == Mode.SINGLE_QUOTE || mode == Mode.DOUBLE_QUOTE) {
                char c = input.charAt(position);
                // Check for closing quote
                if ((mode == Mode.SINGLE_QUOTE && c == '\'') ||
                        (mode == Mode.DOUBLE_QUOTE && c == '"')) {
                    // Add the collected content as a token (even if empty)
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                    // Add the closing quote as a separate token
                    tokens.add(String.valueOf(c));
                    mode = Mode.NORMAL;
                    position++;
                    continue;
                } else {
                    // Collect content (including newlines)
                    currentToken.append(c);
                    position++;
                    continue;
                }
            }

            // Normal mode processing
            char c = input.charAt(position);

            // Handle opening single quote
            if (c == '\'') {
                if (!currentToken.isEmpty()) {
                    addToken(tokens, currentToken);
                }
                tokens.add("'");
                mode = Mode.SINGLE_QUOTE;
                position++;
                continue;
            }

            // Handle opening double quote
            if (c == '"') {
                if (!currentToken.isEmpty()) {
                    addToken(tokens, currentToken);
                }
                tokens.add("\"");
                mode = Mode.DOUBLE_QUOTE;
                position++;
                continue;
            }

            // Skip single-line comments
            if (c == ';') {
                while (position < input.length() && input.charAt(position) != '\n') {
                    position++;
                }
                continue;
            }

            // Handle whitespace
            if (Character.isWhitespace(c)) {
                if (!currentToken.isEmpty()) {
                    addToken(tokens, currentToken);
                    String lastToken = tokens.getLast();
                    if (lastToken.equals("asm")) {
                        int pos = skipWhitespace(position);
                        if (pos < input.length() && input.charAt(pos) == '{') {
                            tokens.add("{");
                            position = skipWhitespace(pos + 1);
                            mode = Mode.ASM_BLOCK;
                        }
                    }
                }
                position++;
                continue;
            }

            // Handle special characters/operators
            if (isSpecialChar(c)) {
                if (!currentToken.isEmpty()) {
                    addToken(tokens, currentToken);
                }
                String op = handleSpecialOperator();
                tokens.add(op);
                continue;
            }

            // Build normal tokens (identifiers, numbers, etc.)
            currentToken.append(c);
            position++;
        }

        // Add any remaining token
        if (!currentToken.isEmpty()) {
            addToken(tokens, currentToken);
        }

        return tokens;
    }

    private void addToken(List<String> tokens, StringBuilder currentToken) {
        tokens.add(currentToken.toString());
        currentToken.setLength(0);
    }

    private String handleSpecialOperator() {
        char c = input.charAt(position);
        char next = peekNext();
        if ((c == '<' || c == '>' || c == '=' || c == '!' || c == '&' || c == '|') && next == '=') {
            position += 2;
            return "" + c + '=';
        } else if (c == '&' && next == '&') {
            position += 2;
            return "&&";
        } else if (c == '|' && next == '|') {
            position += 2;
            return "||";
        } else if (c == '>' && next == '>') {
            position += 2;
            return ">>";
        } else if (c == '<' && next == '<') {
            position += 2;
            return "<<";
        } else if (c == '+' && next == '+') {
            position += 2;
            return "++";
        } else if (c == '-' && next == '-') {
            position += 2;
            return "--";
        } else {
            position++;
            return String.valueOf(c);
        }
    }

    private char peekNext() {
        if (position + 1 < input.length()) {
            return input.charAt(position + 1);
        }
        return '\0';
    }

    private int skipWhitespace(int pos) {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private boolean isSpecialChar(char c) {
        return SPECIAL_CHARS.indexOf(c) != -1;
    }
}