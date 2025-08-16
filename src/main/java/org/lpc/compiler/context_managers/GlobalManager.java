package org.lpc.compiler.context_managers;

import org.lpc.compiler.types.PrimitiveType;
import org.lpc.compiler.types.Type;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.statements.GlobalDeclaration;
import org.lpc.compiler.generators.InstructionGenerator;

import java.util.*;

/**
 * Manages global variable allocation and access with type-aware alignment.
 * Uses natural alignment (1 for byte, 4 for int, 8 for long) to minimize memory usage.
 */
public class GlobalManager {
    private static final int GLOBAL_ALIGNMENT = 8;
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
            int size = decl.getType().getSize();

            globals.put(decl.getName(), new GlobalVariableInfo(currentOffset, size, decl.getType()));

            currentOffset += size;
        }

        // Ensure the total size is aligned for proper memory management
        int totalSize = alignOffset(currentOffset, GLOBAL_ALIGNMENT);

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

    public void storeToGlobal(String name, String valueReg, Type type) {
        String addrReg = getGlobalAddress(name);
        try {
            if (type.isPrimitive()) {
                storePrimitive(addrReg, valueReg, type);
            } else if (type.isPtr()) {
                emitter.store(addrReg, valueReg); // pointers are stored as long
            } else {
                throw new IllegalArgumentException("Unsupported type for global variable: " + type);
            }
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    public String loadFromGlobal(String name, Type type) {
        String addrReg = getGlobalAddress(name);
        String valueReg = registerManager.allocateRegister("global_load_value");
        try {
            if (type.isPrimitive()) {
                loadPrimitive(valueReg, addrReg, type);
            } else if (type.isPtr()) {
                emitter.load(valueReg, addrReg); // points is long
            } else {
                throw new IllegalArgumentException("Unsupported type for global variable: " + type);
            }
            return valueReg;
        } finally {
            registerManager.freeRegister(addrReg);
        }
    }

    private void storePrimitive(String addrReg, String valueReg, Type type) {
        switch (type) {
            case PrimitiveType.BYTE -> emitter.storeByte(addrReg, valueReg);
            case PrimitiveType.INT -> emitter.storeInt(addrReg, valueReg);
            case PrimitiveType.LONG -> emitter.store(addrReg, valueReg);
            default -> throw new IllegalArgumentException("Unknown variable type: " + type);
        }
    }

    private void loadPrimitive(String valueReg, String addrReg, Type type) {
        switch (type) {
            case PrimitiveType.BYTE -> emitter.loadByte(valueReg, addrReg);
            case PrimitiveType.INT -> emitter.loadInt(valueReg, addrReg);
            case PrimitiveType.LONG -> emitter.load(valueReg, addrReg);
            default -> throw new IllegalArgumentException("Unknown variable type: " + type);
        }
    }

    private int alignOffset(int offset, int alignment) {
        return (offset + alignment - 1) & -alignment;
    }

    public Type getVariableType(String name) {
        GlobalVariableInfo info = globals.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Global variable not found: " + name);
        }
        return info.type;
    }

    private record GlobalVariableInfo(int offset, int size, Type type) {
    }
}