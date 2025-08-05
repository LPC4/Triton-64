package org.lpc.compiler;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position = 0;

    public Lexer(String input) {
        this.input = input.trim();
    }

    public List<String> tokenize() {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inAsmBlock = false;

        while (position < input.length()) {
            if (inAsmBlock) {
                StringBuilder line = new StringBuilder();
                while (position < input.length() && input.charAt(position) != '\n') {
                    line.append(input.charAt(position++));
                }
                position++; // skip newline
                String token = line.toString().trim();
                if (!token.isEmpty()) {
                    tokens.add(token);
                    if (token.equals("}")) {
                        inAsmBlock = false;
                    }
                }
                continue;
            }

            char c = input.charAt(position);

            // Skip comments
            if (c == ';') {
                while (position < input.length() && input.charAt(position) != '\n') {
                    position++;
                }
                continue;
            }

            if (Character.isWhitespace(c)) {
                if (!currentToken.isEmpty()) {
                    String token = currentToken.toString();
                    tokens.add(token);
                    if (token.equals("asm") && peekNextNonWhitespace() == '{') {
                        tokens.add("{");
                        position = skipWhitespace(position + 1);
                        inAsmBlock = true;
                    }
                    currentToken.setLength(0);
                }
                position++;
            } else if (isSpecialChar(c)) {
                if (!currentToken.isEmpty()) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                if ((c == '<' || c == '>' || c == '=' || c == '!' || c == '&' || c == '|') && peekNext() == '=') {
                    tokens.add("" + c + '=');
                    position += 2;
                } else if (c == '&' && peekNext() == '&') {
                    tokens.add("&&");
                    position += 2;
                } else if (c == '|' && peekNext() == '|') {
                    tokens.add("||");
                    position += 2;
                } else {
                    tokens.add(String.valueOf(c));
                    position++;
                }
            } else {
                currentToken.append(c);
                position++;
            }
        }

        if (!currentToken.isEmpty()) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private char peekNext() {
        if (position + 1 < input.length()) {
            return input.charAt(position + 1);
        }
        return '\0';
    }

    private char peekNextNonWhitespace() {
        int i = position + 1;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return i < input.length() ? input.charAt(i) : '\0';
    }

    private int skipWhitespace(int i) {
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return i;
    }

    private boolean isSpecialChar(char c) {
        return "(){}=,+-*/%<>!&|".indexOf(c) != -1;
    }
}
