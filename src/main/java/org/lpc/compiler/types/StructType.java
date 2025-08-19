package org.lpc.compiler.types;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@Getter
public final class StructType implements Type {
    private final String name;
    private List<Field> fields;
    private boolean complete = false;

    public StructType(String name) {
        this.name = name;
    }

    public StructType(String name, List<Field> fields) {
        this.name = name;
        setFields(fields);
    }

    public void setFields(List<Field> fields) {
        if (this.fields != null) {
            throw new IllegalStateException("Struct " + name + " is already defined");
        }
        this.fields = fields;
        this.complete = true;
    }

    public List<Field> getFields() {
        if (!complete) {
            throw new IllegalStateException("Cannot access fields of incomplete struct: " + name);
        }
        return fields;
    }

    @Override
    public int getSize() {
        if (!complete) {
            throw new IllegalStateException("Cannot calculate size of incomplete struct: " + name);
        }
        int total = 0;
        for (Field field : fields) {
            total += field.type().getSize();
        }
        return total;
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
        if (!complete) {
            throw new IllegalStateException("Cannot calculate offsets for incomplete struct: " + name);
        }

        int offset = 0;
        for (Field field : fields) {
            if (field.name().equals(fieldName)) {
                return offset;
            }
            offset += field.type().getSize();
        }
        throw new IllegalArgumentException("Field not found in struct " + name + ": " + fieldName);
    }

    public record Field(String name, Type type) {}

    @Override
    public String toString() {
        return "StructType{" +
                "name='" + name + '\'' +
                ", complete=" + complete +
                '}';
    }
}