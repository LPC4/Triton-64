package org.lpc.compiler.codegen;

import org.lpc.compiler.CodeGenerator;
import org.lpc.compiler.ast.parent.Expression;
import org.lpc.compiler.ast.statements.GlobalDeclaration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalManager {
    private static final int WORD_SIZE = 8;

    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;

    private final Map<String, Integer> globalOffsets = new HashMap<>();
    private int nextOffset = 0;

    public GlobalManager(InstructionEmitter emitter, RegisterManager registerManager) {
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    /**
     * Allocate space for all globals (called before main)
     */
    public void allocateGlobals(List<GlobalDeclaration> globals) {
        if (globals.isEmpty()) {
            return;
        }

        emitter.comment("Allocating space for " + globals.size() + " global variables");

        // Reserve space for all globals
        for (GlobalDeclaration global : globals) {
            allocateGlobal(global.name);
        }

        // Update heap pointer
        if (nextOffset > 0) {
            String temp = registerManager.allocateRegister("heap_init");
            try {
                emitter.loadImmediate(temp, nextOffset);
                emitter.add("hp", "gp", temp); // hp = gp + size of globals
                emitter.comment("Heap pointer set to SP + " + nextOffset + " for global variables");
            } finally {
                registerManager.freeRegister(temp);
            }
        }
    }

    /**
     * Initialize all global variables with their values (called before main)
     */
    public void initializeGlobals(List<GlobalDeclaration> globals, CodeGenerator visitor) {
        if (globals.isEmpty()) {
            return;
        }

        emitter.comment("Initializing global variables");

        for (GlobalDeclaration global : globals) {
            initializeGlobal(global, visitor);
        }
    }

    private void allocateGlobal(String name) {
        if (globalOffsets.containsKey(name)) {
            return; // Already allocated
        }

        int offset = nextOffset;
        globalOffsets.put(name, offset);
        nextOffset += WORD_SIZE;

        emitter.comment("Allocated global variable '" + name + "' at GP+" + offset);
    }

    public int getGlobalOffset(String name) {
        Integer offset = globalOffsets.get(name);
        if (offset == null) {
            throw new IllegalArgumentException("Global variable not found: " + name);
        }
        return offset;
    }

    public boolean isGlobal(String name) {
        return globalOffsets.containsKey(name);
    }

    public void storeToGlobal(String name, String valueReg) {
        int offset = getGlobalOffset(name);

        if (offset == 0) {
            // Direct store to GP (first global)
            emitter.store("gp", valueReg);
        } else {
            // Calculate address: GP + offset
            String addrReg = registerManager.allocateRegister("global_addr");
            try {
                emitter.loadImmediate(addrReg, offset);
                emitter.add(addrReg, "gp", addrReg);
                emitter.store(addrReg, valueReg);
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }
    }

    public String loadFromGlobal(String name) {
        int offset = getGlobalOffset(name);
        String resultReg = registerManager.allocateRegister("global_load");

        if (offset == 0) {
            // Direct load from GP (first global)
            emitter.load(resultReg, "gp");
        } else {
            // Calculate address: GP + offset
            String addrReg = registerManager.allocateRegister("global_addr");
            try {
                emitter.loadImmediate(addrReg, offset);
                emitter.add(addrReg, "gp", addrReg);
                emitter.load(resultReg, addrReg);
            } finally {
                registerManager.freeRegister(addrReg);
            }
        }

        return resultReg;
    }

    public int getTotalGlobalsSize() {
        return nextOffset;
    }

    private void initializeGlobal(GlobalDeclaration global, CodeGenerator visitor) {
        emitter.comment("Initializing global '" + global.name + "'");

        // Evaluate the initializer expression
        String valueReg = global.initializer.accept(visitor);
        try {
            // Store to global location
            storeToGlobal(global.name, valueReg);
        } finally {
            registerManager.freeRegister(valueReg);
        }
    }
}