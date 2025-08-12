package org.lpc.compiler.ast;

public interface AstNode {
    <T> T accept(AstVisitor<T> visitor);
}
