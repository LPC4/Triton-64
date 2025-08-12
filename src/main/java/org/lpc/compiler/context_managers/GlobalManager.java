package org.lpc.compiler.context_managers;

import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.statements.GlobalDeclaration;
import org.lpc.compiler.generators.InstructionGenerator;

import java.util.*;

/**
 * Manages global variable allocation and access with type-aware alignment.
 * Uses natural alignment (1 for byte, 4 for int, 8 for long) to minimize memory usage.
 */
public class GlobalManager {
    private final InstructionGenerator emitter;
    private final RegisterManager registerManager;
    private final Map<String, GlobalVariableInfo> globals = new LinkedHashMap<>();
    private int currentOffset = 0;

    public GlobalManager(InstructionGenerator emitter, RegisterManager registerManager) {
        this.emitter = emitter;
        this.registerManager = registerManager;
    }

    /**
     * Allocates space for all global variables in the data section with proper alignment
     */
    public void allocateGlobals(List<GlobalDeclaration> globalDeclarations) {
        emitter.sectionHeader("Global variables");

        for (GlobalDeclaration decl : globalDeclarations) {
            int size = getSizeForType(decl.getType());

            globals.put(decl.getName(), new GlobalVariableInfo(currentOffset, size, decl.getType()));

            currentOffset += size;
        }

        // Ensure the total size is aligned for proper memory management
        int totalSize = alignOffset(currentOffset, 8);

        allocateDataSection(totalSize);

        emitter.comment("Total global data size: " + totalSize + " bytes");
    }

    private void allocateDataSection(int totalSize) {
        emitter.add("hp", "gp", String.valueOf(totalSize));
        emitter.comment("Allocated " + totalSize + " bytes for global variables");
    }

    public void initializeGlobals(List<GlobalDeclaration> globalDeclarations, AstVisitor<String> visitor) {
        for (GlobalDeclaration decl : globalDeclarations) {
            String valueReg = decl.getInitializer().accept(visitor);
            storeToGlobal(decl.getName(), valueReg, decl.getType());
            registerManager.freeRegister(valueReg);
        }
    }

    public boolean isGlobal(String name) {
        return globals.containsKey(name);
    }

    private String getGlobalAddress(String name) {
        GlobalVariableInfo info = globals.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Global variable not found: " + name);
        }

        String addrReg = registerManager.allocateRegister("global_addr");
        emitter.loadImmediate(addrReg, String.valueOf(info.offset));
        emitter.add(addrReg, "gp", addrReg);
        return addrReg;
    }

    public void storeToGlobal(String name, String valueReg, VariableType type) {
        String addrReg = getGlobalAddress(name);
        try {
            switch (type) {
                case BYTE -> emitter.storeByte(addrReg, valueReg);
                case INT -> emitter.storeInt(addrReg, valueReg);
                case LONG -> emitter.store(addrReg, valueReg);
                default -> throw new IllegalArgumentException("Unknown variable type: " + type);
            }
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    public String loadFromGlobal(String name, VariableType type) {
        String addrReg = getGlobalAddress(name);
        String valueReg = registerManager.allocateRegister("global_load_value");
        try {
            switch (type) {
                case BYTE -> emitter.loadByte(valueReg, addrReg);
                case INT -> emitter.loadInt(valueReg, addrReg);
                case LONG -> emitter.load(valueReg, addrReg);
                default -> throw new IllegalArgumentException("Unknown variable type: " + type);
            }
            return valueReg;
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private int getSizeForType(VariableType type) {
        return switch (type) {
            case BYTE -> 1;
            case INT -> 4;
            case LONG -> 8;
        };
    }

    private int alignOffset(int offset, int alignment) {
        return (offset + alignment - 1) & -alignment;
    }

    private record GlobalVariableInfo(int offset, int size, VariableType type) {
    }
}