package org.lpc.assembler;

import org.lpc.cpu.InstructionSet;
import java.util.*;
import static org.lpc.memory.MemoryMap.RAM_BASE;

/**
 * Assembler class that processes source code, expands pseudo-instructions,
 * and generates machine code for the Triton-64 architecture.
 * t9 gets used as a scratch register for immediate values, don't use it in your code!
 */
public class Assembler {
    private final Preprocessor preprocessor;
    private final Expander expander;
    private final SymbolTable symbolTable;

    public Assembler() {
        this.preprocessor = new Preprocessor();
        this.symbolTable = new SymbolTable();
        this.expander = new Expander();
    }

    public int[] assemble(String sourceCode) {
        // Preprocess and split into lines
        String cleaned = preprocessor.preprocess(sourceCode);
        List<String> lines = Arrays.asList(cleaned.split("\n"));

        // First pass: Build symbol table with pseudo-instruction sizes
        firstPassWithPseudo(lines);

        // Expand pseudo-instructions using resolved symbols
        List<String> expanded = expander.expand(lines, symbolTable);

        // Second pass: Assemble expanded instructions
        return assembleExpanded(expanded);
    }

    private void firstPassWithPseudo(List<String> lines) {
        long currentAddress = RAM_BASE;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.endsWith(":")) {
                symbolTable.addSymbol(extractLabel(trimmed), currentAddress);
            } else {
                String[] parts = trimmed.split("\\s+", 2);
                String mnemonic = parts[0].toUpperCase();
                String[] operands = parts.length > 1 ?
                        parts[1].split(",\\s*") : new String[0];

                currentAddress += 4L * expander.getExpansionSize(mnemonic, operands);
            }
        }
    }

    private int[] assembleExpanded(List<String> expandedLines) {
        List<Integer> program = new ArrayList<>();
        for (String line : expandedLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.endsWith(":")) continue;

            String[] parts = trimmed.split("\\s+", 2);
            String mnemonic = parts[0].toUpperCase();
            String[] operands = parts.length > 1 ?
                    parts[1].split(",\\s*") : new String[0];

            Integer opcode = InstructionInfo.OPCODES.get(mnemonic);
            if (opcode == null) throw new IllegalArgumentException("Unknown instruction: " + mnemonic);

            InstructionInfo.OperandType[] types = InstructionInfo.OPERAND_TYPES.get(mnemonic);
            Parser.validateOperandCount(operands, types.length, mnemonic);

            int dest = 0, src = 0, src2 = 0;
            int imm = 0;
            for (int i = 0; i < types.length; i++) {
                String operand = operands[i].trim();
                switch (types[i]) {
                    case REGISTER:
                        int reg = Parser.parseRegister(operand);
                        if (i == 0) dest = reg;
                        else if (i == 1) src = reg;
                        else src2 = reg;
                        break;
                    case IMMEDIATE:
                        imm = Math.toIntExact(Parser.parseLongImmediate(operand));
                        if ("LDI".equals(mnemonic)) validateLDI(imm);
                        break;
                }
            }
            program.add(InstructionSet.encodeInstruction(opcode, dest, src, src2, imm));
        }
        return program.stream().mapToInt(Integer::intValue).toArray();
    }

    private void validateLDI(int imm) {
        if (imm < -512 || imm > 511) {
            throw new IllegalArgumentException("LDI immediate must be 10-bit signed: " + imm);
        }
    }

    private String extractLabel(String line) {
        return line.substring(0, line.length() - 1).trim();
    }
}