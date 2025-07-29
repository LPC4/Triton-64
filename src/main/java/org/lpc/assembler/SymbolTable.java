package org.lpc.assembler;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages symbols (labels) and their addresses during assembly
 */
public class SymbolTable {
    private final Map<String, Long> symbols;

    public SymbolTable() {
        this.symbols = new HashMap<>();
    }

    public void addSymbol(String name, long address) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException("Symbol already defined: " + name);
        }
        symbols.put(name, address);
    }

    public long getSymbolAddress(String name) {
        if (!symbols.containsKey(name)) {
            throw new IllegalArgumentException("Undefined symbol: " + name);
        }
        return symbols.get(name);
    }

    public boolean hasSymbol(String name) {
        return symbols.containsKey(name);
    }

    public Map<String, Long> getAllSymbols() {
        return new HashMap<>(symbols);
    }
}