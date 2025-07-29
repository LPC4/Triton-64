package org.lpc.assembler;

import org.lpc.Globals;
import org.lpc.cpu.InstructionSet;
import java.util.ArrayList;
import java.util.List;

import static org.lpc.memory.MemoryMap.RAM_BASE;

public class Assembler {
    private final Preprocessor preprocessor;
    private final Expander expander;

    public Assembler() {
        this.preprocessor = new Preprocessor();
        this.expander = new Expander();
    }

    public int[] assemble(String sourceCode) {
        // First preprocess to handle includes and macros
        String cleaned = preprocessor.preprocess(sourceCode);

        // Create symbol table and do first pass to collect labels
        SymbolTable symbolTable = new SymbolTable();
        firstPass(cleaned, symbolTable);

        // Expand pseudo-instructions (this will use symbol addresses)
        List<String> expanded = expander.expand(cleaned, symbolTable);

        // Final assembly with resolved symbols
        return assembleExpanded(expanded, symbolTable);
    }

    private void firstPass(String source, SymbolTable symbolTable) {
        String[] lines = source.split("\n");
        long currentAddress = RAM_BASE;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.endsWith(":")) {
                // Label definition - add to symbol table
                String label = extractLabel(trimmed);
                symbolTable.addSymbol(label, currentAddress);
            } else {
                // Regular instruction - calculate size
                String[] parts = trimmed.split("\\s+", 2);
                String mnemonic = parts[0].toUpperCase();
                String[] operands = parts.length > 1 ? parts[1].split(",\\s*") : new String[0];

                // Calculate instruction size (in bytes)
                int size = calculateInstructionSize(mnemonic, operands);
                currentAddress += size;
            }
        }
    }

    private int calculateInstructionSize(String mnemonic, String[] operands) {
        switch (mnemonic) {
            case "LDI64":
                return 39 * 4;  // 39 instructions * 4 bytes each
            case "JMP":
            case "JZ":
            case "JNZ":
                if (operands.length > 0 && !Parser.isRegister(operands[0])) {
                    return 40 * 4;  // Expanded jump with immediate
                }
                return 4;  // Direct register jump
            default:
                return 4;  // Regular instruction
        }
    }

    private int[] assembleExpanded(List<String> expandedLines, SymbolTable symbolTable) {
        List<Integer> program = new ArrayList<>();

        for (String line : expandedLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.endsWith(":")) {
                continue;  // Skip labels and empty lines in expanded code
            }

            String[] parts = trimmed.split("\\s+", 2);
            String mnemonic = parts[0].toUpperCase();
            String[] operands = parts.length > 1 ? parts[1].split(",\\s*") : new String[0];

            // Get instruction info
            Integer opcode = InstructionInfo.OPCODES.get(mnemonic);
            if (opcode == null) {
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
            }

            InstructionInfo.OperandType[] types = InstructionInfo.OPERAND_TYPES.get(mnemonic);
            Parser.validateOperandCount(operands, types.length, mnemonic);

            // Parse operands
            int dest = 0, src = 0, src2 = 0, imm = 0;
            for (int i = 0; i < types.length; i++) {
                String operand = operands[i].trim();

                switch (types[i]) {
                    case REGISTER:
                        int reg = Parser.parseRegister(operand);
                        if (i == 0) dest = reg;
                        else if (i == 1) src = reg;
                        else if (i == 2) src2 = reg;
                        break;

                    case IMMEDIATE:
                        imm = Parser.parseImmediate(operand);

                        // Validate immediate size
                        if ("LDI".equals(mnemonic) && (imm < -512 || imm > 511)) {
                            throw new IllegalArgumentException(
                                    "Immediate must be 10-bit signed for LDI: " + imm);
                        }
                        break;
                }
            }

            // Encode and add to program
            program.add(InstructionSet.encodeInstruction(opcode, dest, src, src2, imm));
        }

        return program.stream().mapToInt(Integer::intValue).toArray();
    }

    private String extractLabel(String line) {
        return line.substring(0, line.length() - 1).trim();
    }
}