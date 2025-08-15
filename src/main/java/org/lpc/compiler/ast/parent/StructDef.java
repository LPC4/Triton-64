package org.lpc.compiler.ast.parent;

import lombok.Getter;
import lombok.ToString;
import org.lpc.compiler.ast.AstNode;
import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.types.StructType;

import java.util.List;

@Getter
@ToString
public class StructDef implements AstNode {
    private final String name;
    private final List<StructType.Field> fields;

    public StructDef(String name, List<StructType.Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
