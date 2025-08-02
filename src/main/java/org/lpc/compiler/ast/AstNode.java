package org.lpc.compiler.ast;

public abstract class AstNode {
    public abstract <T> T accept(AstVisitor<T> visitor);
}
