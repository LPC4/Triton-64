package org.lpc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.lpc.assembler.Assembler;
import org.lpc.cpu.Cpu;
import org.lpc.memory.Memory;
import org.lpc.visual.CpuViewer;
import org.lpc.visual.MemoryViewer;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import static org.lpc.memory.MemoryMap.*;

public class Main extends Application {

    private Memory memory;
    private Cpu cpu;
    private MemoryViewer memoryViewer;
    private CpuViewer cpuViewer;

    public Main() {
        System.out.println("Triton-64 Virtual Machine");
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize the system
        memory = new Memory();
        cpu = new Cpu(memory);
        memoryViewer = new MemoryViewer(cpu);
        cpuViewer = new CpuViewer(cpu);

        // Load program into RAM
        loadProgramToRAM(memory, new Assembler());

        // Start the memory viewer GUI
        memoryViewer.start(new Stage());
        cpuViewer.start(new Stage());

        // Run CPU in background thread to avoid blocking JavaFX
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Starting CPU execution...");
                cpu.run();

                // Print final state after execution
                Platform.runLater(() -> {
                    System.out.println("\n=== CPU Execution Complete ===");
                    cpu.printRegisters();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("CPU execution error: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void loadProgramToRAM(Memory memory, Assembler assembler) {
        String sourceCode = getSourceFromFile("/test.asm");
        int[] program = assembler.assemble(sourceCode);

        if (program.length >= RAM_SIZE / 4) {
            throw new IllegalArgumentException("Program too large for RAM");
        }

        System.out.println("Loading program to RAM (" + program.length + " instructions)");
        for (int i = 0; i < program.length; i++) {
            memory.writeInt(RAM_BASE + i * 4L, program[i]);
        }

        System.out.println("Program loaded at address: 0x" + Long.toHexString(RAM_BASE));
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
        // Check if JavaFX is available and launch GUI
        try {
            System.out.println("Launching Memory Viewer GUI...");
            launch(args);
        } catch (Exception e) {
            // Fallback to console-only mode if JavaFX is not available
            System.err.println("JavaFX not available, running in console mode: " + e.getMessage());
            runConsoleMode();
        }
    }

    /**
     * Fallback method to run without GUI if JavaFX is not available
     */
    private static void runConsoleMode() {
        System.out.println("Running in console mode...");
        Memory memory = new Memory();
        Cpu cpu = new Cpu(memory);

        Main main = new Main();
        main.loadProgramToRAM(memory, new Assembler());

        cpu.run();
        cpu.printRegisters();
    }
}