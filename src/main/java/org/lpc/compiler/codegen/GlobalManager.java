package org.lpc.compiler.codegen;

import org.lpc.compiler.VariableType;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.statements.GlobalDeclaration;

import java.util.*;

/**
 * Manages global variable allocation and access with type-aware alignment.
 * Uses natural alignment (1 for byte, 4 for int, 8 for long) to minimize memory usage.
 */
public class GlobalManager {
    private final InstructionEmitter emitter;
    private final RegisterManager registerManager;
    private final Map<String, GlobalVariableInfo> globals = new LinkedHashMap<>();
    private int currentOffset = 0;
    private int totalSize = 0;

    public GlobalManager(InstructionEmitter emitter, RegisterManager registerManager) {
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
            int alignment = getAlignmentForType(decl.getType());

            // Align the current offset to the required boundary
            currentOffset = alignOffset(currentOffset, alignment);

            // Record the variable with its actual offset and size
            globals.put(decl.getName(), new GlobalVariableInfo(currentOffset, size, decl.getType()));

            // Move to the next position (no padding after the variable)
            currentOffset += size;
        }

        // Ensure the total size is aligned for proper memory management
        totalSize = alignOffset(currentOffset, 8);

        allocateDataSection(totalSize);

        emitter.comment("Total global data size: " + totalSize + " bytes");
    }

    private void allocateDataSection(int totalSize) {
        emitter.add("hp", "gp", String.valueOf(totalSize));
        emitter.comment("Allocated " + totalSize + " bytes for global variables");
    }

    /**
     * Initializes global variables with their values
     */
    public void initializeGlobals(List<GlobalDeclaration> globalDeclarations, AstVisitor<String> visitor) {
        for (GlobalDeclaration decl : globalDeclarations) {
            String valueReg = decl.getInitializer().accept(visitor);
            storeToGlobal(decl.getName(), valueReg, decl.getType());
            registerManager.freeRegister(valueReg);
        }
    }

    /**
     * Checks if a variable is a global
     */
    public boolean isGlobal(String name) {
        return globals.containsKey(name);
    }

    /**
     * Gets the address of a global variable
     */
    private String getGlobalAddress(String name) {
        GlobalVariableInfo info = globals.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Global variable not found: " + name);
        }

        String addrReg = registerManager.allocateRegister("global_addr");
        emitter.loadImmediate(addrReg, info.offset);
        emitter.add(addrReg, "gp", addrReg);
        return addrReg;
    }

    /**
     * Stores a value to a global variable
     */
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

    /**
     * Loads a value from a global variable
     */
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

    /**
     * Returns the size in bytes for a given type
     */
    private int getSizeForType(VariableType type) {
        return switch (type) {
            case BYTE -> 1;
            case INT -> 4;
            case LONG -> 8;
            default -> throw new IllegalArgumentException("Unsupported variable type: " + type);
        };
    }

    /**
     * Returns the required alignment for a given type
     * (natural alignment: same as size for most architectures)
     */
    private int getAlignmentForType(VariableType type) {
        return switch (type) {
            case BYTE -> 1;
            case INT -> 4;
            case LONG -> 8;
            default -> throw new IllegalArgumentException("Unsupported variable type: " + type);
        };
    }

    /**
     * Aligns an offset to the specified boundary
     */
    private int alignOffset(int offset, int alignment) {
        return (offset + alignment - 1) & ~(alignment - 1);
    }

    private record GlobalVariableInfo(int offset, int size, VariableType type) {
    }
}