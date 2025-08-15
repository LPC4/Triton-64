package org.lpc.compiler.types;

import lombok.Getter;

import java.util.List;

public record StructType(@Getter String name, @Getter List<Field> fields) implements Type {
    @Override
    public int getSize() {
        return fields.stream()
                .mapToInt(field -> field.type().getSize())
                .sum();
    }

    @Override
    public boolean isStruct() {
        return true;
    }

    @Override
    public StructType asStruct() {
        return this;
    }

    public int getFieldOffset(String fieldName) {
        int offset = 0;
        for (Field field : fields) {
            if (field.name().equals(fieldName)) {
                return offset;
            }
            offset += field.type().getSize();
        }
        throw new IllegalArgumentException("Field not found: " + fieldName);
    }

    public record Field(String name, Type type) {}
}
