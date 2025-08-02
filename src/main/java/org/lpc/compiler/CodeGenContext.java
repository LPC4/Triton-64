package org.lpc.compiler;

import lombok.Getter;
import org.lpc.cpu.InstructionSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
class CodeGenContext {
    private int nextTemp = 0; // Tracks next available temp register (t0-t8)
    private int stackOffset = 0; // Current stack offset
    private int labelCount = 0; // For generating unique labels
    private final List<String> output = new ArrayList<>(); // Generated instructions

    int allocateTemp() {
        if (nextTemp > 8) throw new RuntimeException("Out of temp registers");
        return 22 + nextTemp++; // t0 (r22) to t8 (r30)
    }

    void freeTemp() {
        nextTemp--; // Free last allocated temp
    }

    String newLabel() {
        return "L" + labelCount++;
    }
}