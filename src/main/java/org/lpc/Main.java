package org.lpc;

import org.lpc.assembler.Assembler;
import org.lpc.cpu.Cpu;
import org.lpc.cpu.InstructionSet;
import org.lpc.memory.Memory;
import org.lpc.rom.ROMData;
import org.lpc.visual.MemoryPrinter;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static org.lpc.memory.MemoryMap.*;

public class Main {
    public Main () {
        System.out.println("Triton-64 Virtual Machine");
        Memory memory = new Memory();
        Cpu cpu = new Cpu(memory);

        loadProgramToRAM(memory, new Assembler());
        System.out.println("Loaded program into RAM");
        new MemoryPrinter(memory).printPages(0, 2);
        System.out.println("Running program...");
        cpu.run();

        cpu.printRegisters();
    }

    private void loadProgramToRAM(Memory memory, Assembler assembler) {
        String sourceCode = getSourceFromFile("/test.asm");
        int[] program = assembler.assemble(sourceCode);

        if (program.length >= RAM_SIZE / 4) throw new IllegalArgumentException("Program too large for RAM");

        for (int i = 0; i < program.length; i++) {
            memory.writeInt(RAM_BASE + i * 4L, program[i]);
        }
    }


    @SuppressWarnings("SameParameterValue")
    private String getSourceFromFile(String filename) {
        URL resource = getClass().getResource(filename);
        if (resource == null) {
            throw new IllegalArgumentException("Source file not found: " + filename);
        }
        File file = new File(resource.getFile());
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid source file: " + filename);
        }
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error reading source file: " + filename, e);
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}