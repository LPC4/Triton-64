package org.lpc.compiler.codegen;

import lombok.Getter;

import java.util.*;

/**
 * Updated CodeGenContext that works with the refactored architecture.
 * Simplified to focus on core responsibilities.
 */
@Getter
public class CodeGenContext {
    private final List<String> assembly;
    private final Deque<String> savedTemporaries;

    // Label generation
    private int labelCounter = 0;
    private String lastGeneratedLabel;

    public CodeGenContext() {
        this.assembly = new ArrayList<>();
        this.savedTemporaries = new ArrayDeque<>();
    }

    public void addAssembly(String code) {
        assembly.add(code);
    }

    public String generateLabel(String prefix) {
        lastGeneratedLabel = String.format("%s_%d", prefix, labelCounter++);
        return lastGeneratedLabel;
    }

    // Reset methods
    public void reset() {
        assembly.clear();
        savedTemporaries.clear();
        labelCounter = 0;
        lastGeneratedLabel = null;
    }
}