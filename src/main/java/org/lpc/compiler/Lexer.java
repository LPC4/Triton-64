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

        while (position < input.length()) {
            char c = input.charAt(position);

            if (Character.isWhitespace(c)) {
                if (!currentToken.isEmpty()) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                position++;
            }
            else if (isSpecialChar(c)) {
                if (!currentToken.isEmpty()) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(String.valueOf(c));
                position++;
            }
            else {
                currentToken.append(c);
                position++;
            }
        }

        if (!currentToken.isEmpty()) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private boolean isSpecialChar(char c) {
        return "(){}=;,*&+-<>".indexOf(c) != -1;
    }
}
