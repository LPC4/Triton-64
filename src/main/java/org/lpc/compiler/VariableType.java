package org.lpc.compiler;

import lombok.Getter;

@Getter
public enum VariableType {
    BYTE(1),
    INT(4),
    LONG(8);

    private final int size;

    VariableType(int size) {
        this.size = size;
    }
}
