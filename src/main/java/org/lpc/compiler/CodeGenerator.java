package org.lpc.compiler;

import org.lpc.compiler.ast.AstVisitor;
import org.lpc.compiler.ast.expressions.*;
import org.lpc.compiler.ast.parent.FunctionDef;
import org.lpc.compiler.ast.parent.Program;
import org.lpc.compiler.ast.parent.Statement;
import org.lpc.compiler.ast.statements.*;

import java.util.List;

public class CodeGenerator implements AstVisitor<String> {
    private final CodeGenContext ctx;
    private final Program program;
    private String currentFunctionEndLabel;

    public CodeGenerator(Parser parser) {
        this.ctx = new CodeGenContext();
        this.program = parser.parse();
    }

    public List<String> generate() {
        program.accept(this);
        return ctx.getAssembly();
    }

    @Override
    public String visit(Program program) {
        // Add jump to main at start
        ctx.addInstruction("JMP", "main");

        // First generate all non-main functions
        for (FunctionDef functionDef : program.functions) {
            if (!"main".equals(functionDef.name)) {
                functionDef.accept(this);
            }
        }

        // Then generate main function
        for (FunctionDef functionDef : program.functions) {
            if ("main".equals(functionDef.name)) {
                functionDef.accept(this);
            }
        }
        return null;
    }

    @Override
    public String visit(FunctionDef functionDef) {
        List<Statement> body = functionDef.body;
        List<String> parameters = functionDef.parameters;
        String name = functionDef.name;

        // Add function label
        ctx.addLabel(name);

        ctx.resetRegisterAllocator();
        ctx.startFunctionFrame(parameters);
        String endLabel = ctx.generateLabel(name + "_end");
        currentFunctionEndLabel = endLabel;

        // Function prologue
        if (!name.equals("main")) {
            ctx.addInstruction("PUSH", "ra");
            ctx.addInstruction("PUSH", "fp");
        }
        ctx.addInstruction("MOV", "fp", "sp");

        // Save parameters to stack
        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            int offset = ctx.getVariableOffset(param);
            String temp = ctx.allocateTempRegister();
            ctx.addInstruction("LDI", temp, String.valueOf(offset));
            ctx.addInstruction("ADD", temp, "fp", temp);
            ctx.addInstruction("ST", temp, "a" + i);
            ctx.freeRegister(temp);
        }

        // Allocate space for local variables
        int localSpace = 0;
        for (Statement stmt : body) {
            if (stmt instanceof Declaration) {
                localSpace++;
            }
        }
        if (localSpace > 0) {
            String temp = ctx.allocateTempRegister();
            ctx.addInstruction("LDI", temp, String.valueOf(localSpace));
            ctx.addInstruction("SUB", "sp", "sp", temp);
            ctx.freeRegister(temp);
        }

        // Process declarations first for stack allocation
        for (Statement stmt : body) {
            if (stmt instanceof Declaration) {
                stmt.accept(this);
            }
        }

        // Generate function body
        for (Statement stmt : body) {
            if (!(stmt instanceof Declaration)) {
                stmt.accept(this);
            }
        }

        // Function epilogue
        ctx.addLabel(endLabel);
        if (name.equals("main")) {
            ctx.addInstruction("HLT", "; End of main");
        } else {
            ctx.addInstruction("MOV", "sp", "fp");
            ctx.addInstruction("POP", "fp");
            ctx.addInstruction("POP", "ra");
            ctx.addInstruction("JMP", "ra", "; Return to caller");
        }

        ctx.endFunctionFrame();
        currentFunctionEndLabel = null;
        return null;
    }

    @Override
    public String visit(ReturnStatement returnStatement) {
        ctx.addComment("return");
        if (returnStatement.value != null) {
            String valueReg = returnStatement.value.accept(this);
            ctx.addInstruction("MOV", "a0", valueReg);
            ctx.freeRegister(valueReg);
        }
        ctx.addInstruction("JMP", currentFunctionEndLabel);
        return null;
    }

    @Override
    public String visit(IfStatement ifStatement) {
        ctx.addComment("If statement");
        String elseLabel = ctx.generateLabel("else");
        String endLabel = ctx.generateLabel("endif");

        if (ifStatement.condition instanceof BinaryOp &&
                isComparisonOp(((BinaryOp) ifStatement.condition).op)) {
            generateComparisonJump((BinaryOp) ifStatement.condition, elseLabel, true);
        } else {
            String conditionReg = ifStatement.condition.accept(this);
            ctx.addInstruction("JZ", elseLabel, conditionReg);
            ctx.freeRegister(conditionReg);
        }

        for (Statement stmt : ifStatement.thenBranch) {
            stmt.accept(this);
        }

        if (ifStatement.elseBranch != null && !ifStatement.elseBranch.isEmpty()) {
            ctx.addInstruction("JMP", endLabel);
            ctx.addLabel(elseLabel);
            for (Statement stmt : ifStatement.elseBranch) {
                stmt.accept(this);
            }
        } else {
            ctx.addLabel(elseLabel);
        }

        ctx.addLabel(endLabel);
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        ctx.addComment("While loop");
        String loopLabel = ctx.generateLabel("loop");
        String endLabel = ctx.generateLabel("endloop");

        ctx.addLabel(loopLabel);

        if (whileStatement.condition instanceof BinaryOp &&
                isComparisonOp(((BinaryOp) whileStatement.condition).op)) {
            generateComparisonJump((BinaryOp) whileStatement.condition, endLabel, true);
        } else {
            String conditionReg = whileStatement.condition.accept(this);
            ctx.addInstruction("JZ", endLabel, conditionReg);
            ctx.freeRegister(conditionReg);
        }

        for (Statement stmt : whileStatement.body) {
            stmt.accept(this);
        }

        ctx.addInstruction("JMP", loopLabel);
        ctx.addLabel(endLabel);
        return null;
    }

    private boolean isComparisonOp(BinaryOp.Op op) {
        return op == BinaryOp.Op.GT ||
                op == BinaryOp.Op.LT ||
                op == BinaryOp.Op.GE ||
                op == BinaryOp.Op.LE ||
                op == BinaryOp.Op.EQ ||
                op == BinaryOp.Op.NE;
    }

    private void generateComparisonJump(BinaryOp comparison, String jumpLabel, boolean jumpOnFalse) {
        String leftReg = comparison.left.accept(this);
        String rightReg = comparison.right.accept(this);

        BinaryOp.Op op = comparison.op;
        if (jumpOnFalse) {
            op = invertComparison(op);
        }

        // Use a more direct approach that doesn't require a temp register for subtraction
        switch (op) {
            case GT:
                // Use SUB and check result inline
                String tempReg1 = ctx.allocateTempRegister();
                ctx.addInstruction("SUB", tempReg1, leftReg, rightReg);
                ctx.addInstruction("JPP", jumpLabel, tempReg1);
                ctx.freeRegister(tempReg1);
                break;
            case LT:
                String tempReg2 = ctx.allocateTempRegister();
                ctx.addInstruction("SUB", tempReg2, leftReg, rightReg);
                ctx.addInstruction("JPN", jumpLabel, tempReg2);
                ctx.freeRegister(tempReg2);
                break;
            case EQ:
                String tempReg3 = ctx.allocateTempRegister();
                ctx.addInstruction("SUB", tempReg3, leftReg, rightReg);
                ctx.addInstruction("JZ", jumpLabel, tempReg3);
                ctx.freeRegister(tempReg3);
                break;
            case NE:
                String tempReg4 = ctx.allocateTempRegister();
                ctx.addInstruction("SUB", tempReg4, leftReg, rightReg);
                ctx.addInstruction("JNZ", jumpLabel, tempReg4);
                ctx.freeRegister(tempReg4);
                break;
            // Simplify GE and LE for now
            case GE:
                // a >= b is equivalent to !(a < b)
                String tempReg5 = ctx.allocateTempRegister();
                ctx.addInstruction("SUB", tempReg5, leftReg, rightReg);
                String skipLabel = ctx.generateLabel("skip_ge");
                ctx.addInstruction("JPN", skipLabel, tempReg5);
                ctx.addInstruction("JMP", jumpLabel);
                ctx.addLabel(skipLabel);
                ctx.freeRegister(tempReg5);
                break;
            case LE:
                // a <= b is equivalent to !(a > b)
                String tempReg6 = ctx.allocateTempRegister();
                ctx.addInstruction("SUB", tempReg6, leftReg, rightReg);
                String skipLabel2 = ctx.generateLabel("skip_le");
                ctx.addInstruction("JPP", skipLabel2, tempReg6);
                ctx.addInstruction("JMP", jumpLabel);
                ctx.addLabel(skipLabel2);
                ctx.freeRegister(tempReg6);
                break;
            default:
                throw new IllegalArgumentException("Unsupported comparison operator: " + op);
        }

        // Free the operand registers
        ctx.freeRegister(leftReg);
        ctx.freeRegister(rightReg);
    }

    private BinaryOp.Op invertComparison(BinaryOp.Op op) {
        return switch (op) {
            case GT -> BinaryOp.Op.LE;
            case LT -> BinaryOp.Op.GE;
            case GE -> BinaryOp.Op.LT;
            case LE -> BinaryOp.Op.GT;
            case EQ -> BinaryOp.Op.NE;
            case NE -> BinaryOp.Op.EQ;
            default -> op;
        };
    }

    @Override
    public String visit(Declaration declaration) {
        ctx.addComment("Declaration: " + declaration.name);
        int offset = ctx.allocateLocalVariable(declaration.name);

        if (declaration.initializer != null) {
            String valueReg = declaration.initializer.accept(this);
            String addrReg = ctx.allocateTempRegister();

            // Calculate address: fp + offset
            ctx.addInstruction("LDI", addrReg, String.valueOf(offset));
            ctx.addInstruction("ADD", addrReg, "fp", addrReg);

            // Store value to stack
            ctx.addInstruction("ST", addrReg, valueReg);
            ctx.freeRegister(addrReg);
            ctx.freeRegister(valueReg);
        }
        return null;
    }

    @Override
    public String visit(AssignmentStatement assignment) {
        ctx.addComment("Assignment: " + assignment.name);
        String valueReg = assignment.initialValue.accept(this);
        int offset = ctx.getVariableOffset(assignment.name);
        String addrReg = ctx.allocateTempRegister();

        // Calculate address: fp + offset
        ctx.addInstruction("LDI", addrReg, String.valueOf(offset));
        ctx.addInstruction("ADD", addrReg, "fp", addrReg);

        // Store value to stack
        ctx.addInstruction("ST", addrReg, valueReg);
        ctx.freeRegister(addrReg);
        ctx.freeRegister(valueReg);
        return null;
    }

    @Override
    public String visit(Variable variable) {
        int offset = ctx.getVariableOffset(variable.name);
        String addrReg = ctx.allocateTempRegister();
        String valueReg = ctx.allocateTempRegister();

        // Calculate address: fp + offset
        ctx.addInstruction("LDI", addrReg, String.valueOf(offset));
        ctx.addInstruction("ADD", addrReg, "fp", addrReg);

        // Load value from stack
        ctx.addInstruction("LD", valueReg, addrReg);
        ctx.freeRegister(addrReg);
        return valueReg;
    }

    @Override
    public String visit(ExpressionStatement expressionStatement) {
        ctx.addComment("Expression Statement: " + expressionStatement.expression);
        String resultReg = expressionStatement.expression.accept(this);
        if (resultReg != null) {
            ctx.freeRegister(resultReg);
        }
        return null;
    }

    @Override
    public String visit(BinaryOp binaryOp) {
        ctx.addComment("Binary Operation: " + binaryOp.op + " between " + binaryOp.left + " and " + binaryOp.right);
        String leftReg = binaryOp.left.accept(this);
        String rightReg = binaryOp.right.accept(this);
        String resultReg = ctx.allocateTempRegister();

        // Remove comparison operators from here since they're handled specially
        if (isComparisonOp(binaryOp.op)) {
            throw new IllegalStateException("Comparison operators should be handled in conditional contexts, not as standalone expressions");
        }

        switch (binaryOp.op) {
            case ADD:
                ctx.addInstruction("ADD", resultReg, leftReg, rightReg);
                break;
            case SUB:
                ctx.addInstruction("SUB", resultReg, leftReg, rightReg);
                break;
            case MUL:
                ctx.addInstruction("MUL", resultReg, leftReg, rightReg);
                break;
            case DIV:
                ctx.addInstruction("DIV", resultReg, leftReg, rightReg);
                break;
            case AND:
                ctx.addInstruction("AND", resultReg, leftReg, rightReg);
                break;
            case OR:
                ctx.addInstruction("OR", resultReg, leftReg, rightReg);
                break;
            case XOR:
                ctx.addInstruction("XOR", resultReg, leftReg, rightReg);
                break;
            case SHL:
                ctx.addInstruction("SHL", resultReg, leftReg, rightReg);
                break;
            case SHR:
                ctx.addInstruction("SHR", resultReg, leftReg, rightReg);
                break;
            case SAR:
                ctx.addInstruction("SAR", resultReg, leftReg, rightReg);
                break;
            default:
                throw new IllegalArgumentException("Unsupported binary.op: " + binaryOp.op);
        }

        ctx.freeRegister(leftReg);
        ctx.freeRegister(rightReg);
        return resultReg;
    }

    @Override
    public String visit(UnaryOp unaryOp) {
        ctx.addComment("Unary Operation: " + unaryOp.op + " on " + unaryOp.operand);
        String operandReg = unaryOp.operand.accept(this);
        String resultReg = ctx.allocateTempRegister();

        switch (unaryOp.op) {
            case NEG:
                ctx.addInstruction("NEG", resultReg, operandReg);
                break;
            case NOT:
                ctx.addInstruction("NOT", resultReg, operandReg);
                break;
            default:
                throw new IllegalArgumentException("Unsupported unary.op: " + unaryOp.op);
        }

        ctx.freeRegister(operandReg);
        return resultReg;
    }

    @Override
    public String visit(FunctionCall functionCall) {
        ctx.addComment("Function Call: " + functionCall.name);
        // Check argument count
        if (functionCall.arguments.size() > 7) {
            throw new IllegalArgumentException("Too many arguments for function: " + functionCall.name +
                    " (max 7, got " + functionCall.arguments.size() + ")");
        }

        ctx.saveLiveTemporaries();

        // Load arguments into a0-a6
        for (int i = 0; i < functionCall.arguments.size(); i++) {
            String argReg = functionCall.arguments.get(i).accept(this);
            ctx.addInstruction("MOV", "a" + i, argReg);
            ctx.freeRegister(argReg);
        }

        ctx.addInstruction("JAL", functionCall.name, "ra"); // Jump to function, save return address in ra

        ctx.restoreLiveTemporaries();

        // Result is in a0
        String resultReg = ctx.allocateTempRegister();
        ctx.addInstruction("MOV", resultReg, "a0");
        return resultReg;
    }

    @Override
    public String visit(IntegerLiteral integerLiteral) {
        String reg = ctx.allocateTempRegister();

        ctx.addInstruction("LDI", reg, String.valueOf(integerLiteral.value));

        return reg;
    }
}