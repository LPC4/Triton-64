package org.lpc.assembler;

import org.lpc.cpu.InstructionSet;
import java.util.Map;

/**
 * Contains instruction metadata and operand type definitions
 */
public class InstructionInfo {
    public enum OperandType {
        REGISTER,
        IMMEDIATE,
        LABEL  // For jump instructions that can accept labels
    }

    public static final Map<String, Integer> OPCODES = Map.ofEntries(
            Map.entry("NOP", InstructionSet.OP_NOP),
            Map.entry("HLT", InstructionSet.OP_HLT),
            Map.entry("MOV", InstructionSet.OP_MOV),
            Map.entry("NOT", InstructionSet.OP_NOT),
            Map.entry("NEG", InstructionSet.OP_NEG),
            Map.entry("ADD", InstructionSet.OP_ADD),
            Map.entry("SUB", InstructionSet.OP_SUB),
            Map.entry("MUL", InstructionSet.OP_MUL),
            Map.entry("DIV", InstructionSet.OP_DIV),
            Map.entry("AND", InstructionSet.OP_AND),
            Map.entry("OR", InstructionSet.OP_OR),
            Map.entry("XOR", InstructionSet.OP_XOR),
            Map.entry("SHL", InstructionSet.OP_SHL),
            Map.entry("SHR", InstructionSet.OP_SHR),
            Map.entry("SAR", InstructionSet.OP_SAR),
            Map.entry("JMP", InstructionSet.OP_JMP),
            Map.entry("JZ", InstructionSet.OP_JZ),
            Map.entry("JNZ", InstructionSet.OP_JNZ),
            Map.entry("LD", InstructionSet.OP_LD),
            Map.entry("ST", InstructionSet.OP_ST),
            Map.entry("LDI", InstructionSet.OP_LDI)
    );

    public static final Map<String, OperandType[]> OPERAND_TYPES = Map.ofEntries(
            Map.entry("NOP", new OperandType[0]),
            Map.entry("HLT", new OperandType[0]),
            Map.entry("MOV", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("NOT", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("NEG", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("ADD", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SUB", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("MUL", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("DIV", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("AND", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("OR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("XOR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SHL", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SHR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SAR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JMP", new OperandType[]{OperandType.REGISTER}),
            Map.entry("JZ", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JNZ", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("LD", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("ST", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("LDI", new OperandType[]{OperandType.REGISTER, OperandType.IMMEDIATE})
    );
}