package org.lpc.compiler.context_managers;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Context for code generation, holding the assembly code and temporary variables.
 */
@Getter
public class ContextManager {
    private final List<String> assembly;
    private final Deque<String> savedTemporaries; // temps to be restored later
    @Setter
    private String currentFunctionEndLabel;

    // Label generation
    private int labelCounter = 0;
    private String lastGeneratedLabel;

    public ContextManager() {
        this.assembly = new ArrayList<>();
        this.savedTemporaries = new ArrayDeque<>();
    }

    public void addAssembly(String code) {
        assembly.add(code);
    }

    public void pushTemporary(String temp) {
        savedTemporaries.push(temp);
    }

    public String generateLabel(String prefix) {
        lastGeneratedLabel = String.format("%s_%d", prefix, labelCounter++);
        return lastGeneratedLabel;
    }
}