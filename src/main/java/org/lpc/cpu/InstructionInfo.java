package org.lpc.cpu;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains instruction metadata and operand type definitions
 */
public class InstructionInfo {
    public enum OperandType {
        REGISTER,
        IMMEDIATE
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
            Map.entry("OR" , InstructionSet.OP_OR),
            Map.entry("XOR", InstructionSet.OP_XOR),
            Map.entry("SHL", InstructionSet.OP_SHL),
            Map.entry("SHR", InstructionSet.OP_SHR),
            Map.entry("SAR", InstructionSet.OP_SAR),
            Map.entry("MOD", InstructionSet.OP_MOD),
            Map.entry("JMP", InstructionSet.OP_JMP),
            Map.entry("JZ" , InstructionSet.OP_JZ),
            Map.entry("JNZ", InstructionSet.OP_JNZ),
            Map.entry("JPP", InstructionSet.OP_JPP),
            Map.entry("JPN", InstructionSet.OP_JPN),
            Map.entry("JAL", InstructionSet.OP_JAL),
            Map.entry("LD" , InstructionSet.OP_LD),
            Map.entry("ST" , InstructionSet.OP_ST),
            Map.entry("SB" , InstructionSet.OP_SB),
            Map.entry("LB" , InstructionSet.OP_LB),
            Map.entry("SI" , InstructionSet.OP_SI),
            Map.entry("LI" , InstructionSet.OP_LI),
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
            Map.entry("OR" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("XOR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SHL", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SHR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SAR", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("MOD", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JMP", new OperandType[]{OperandType.REGISTER}),
            Map.entry("JZ" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JNZ", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JPP", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JPN", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("JAL", new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("LD" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("ST" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SB" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("LB" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("SI" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("LI" , new OperandType[]{OperandType.REGISTER, OperandType.REGISTER}),
            Map.entry("LDI", new OperandType[]{OperandType.REGISTER, OperandType.IMMEDIATE})
    );

    public static final Map<Integer, String> OPCODE_NAMES = new HashMap<>() {
        {
            put(InstructionSet.OP_NOP, "NOP");
            put(InstructionSet.OP_HLT, "HLT");
            put(InstructionSet.OP_MOV, "MOV");
            put(InstructionSet.OP_NOT, "NOT");
            put(InstructionSet.OP_NEG, "NEG");
            put(InstructionSet.OP_ADD, "ADD");
            put(InstructionSet.OP_SUB, "SUB");
            put(InstructionSet.OP_MUL, "MUL");
            put(InstructionSet.OP_DIV, "DIV");
            put(InstructionSet.OP_AND, "AND");
            put(InstructionSet.OP_OR , "OR");
            put(InstructionSet.OP_XOR, "XOR");
            put(InstructionSet.OP_SHL, "SHL");
            put(InstructionSet.OP_SHR, "SHR");
            put(InstructionSet.OP_SAR, "SAR");
            put(InstructionSet.OP_MOD, "MOD");
            put(InstructionSet.OP_JMP, "JMP");
            put(InstructionSet.OP_JZ , "JZ");
            put(InstructionSet.OP_JNZ, "JNZ");
            put(InstructionSet.OP_JPP, "JPP");
            put(InstructionSet.OP_JPN, "JPN");
            put(InstructionSet.OP_JAL, "JAL");
            put(InstructionSet.OP_LD , "LD");
            put(InstructionSet.OP_ST , "ST");
            put(InstructionSet.OP_SB , "SB");
            put(InstructionSet.OP_LB , "LB");
            put(InstructionSet.OP_SI , "SI");
            put(InstructionSet.OP_LI , "LI");
            put(InstructionSet.OP_LDI, "LDI");
        }
    };
}