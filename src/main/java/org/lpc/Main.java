package org.lpc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.lpc.assembler.Assembler;
import org.lpc.cpu.Cpu;
import org.lpc.memory.Memory;
import org.lpc.visual.CpuViewer;
import org.lpc.visual.MemoryViewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lpc.memory.MemoryMap.RAM_BASE;
import static org.lpc.memory.MemoryMap.RAM_SIZE;

public class Main extends Application {
    private static final String PROGRAM_FILE = "/test.asm";
    private static final String APP_NAME = "Triton-64 Virtual Machine";
    private static final int CPU_THREAD_POOL_SIZE = 1;

    private final ExecutorService cpuExecutor = Executors.newFixedThreadPool(CPU_THREAD_POOL_SIZE);
    private Memory memory;
    private Cpu cpu;
    private MemoryViewer memoryViewer;
    private CpuViewer cpuViewer;

    @Override
    public void init() {
        System.out.println(APP_NAME + " - Initializing...");
        this.memory = new Memory();
        this.cpu = new Cpu(memory);
        initializeDebugViews();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            loadProgram();
            launchDebugViews();
            startCpuExecution();
        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    @Override
    public void stop() {
        System.out.println(APP_NAME + " - Shutting down...");
        cpuExecutor.shutdownNow();
        Platform.exit();
    }

    private void initializeDebugViews() {
        this.memoryViewer = new MemoryViewer(cpu);
        this.cpuViewer = new CpuViewer(cpu);
    }

    private void launchDebugViews() {
        Platform.runLater(() -> {
            memoryViewer.start(new Stage());
            cpuViewer.start(new Stage());
        });
    }

    private void loadProgram() throws IOException {
        String sourceCode = readProgramFile();
        int[] program = new Assembler().assemble(sourceCode);
        validateProgramSize(program.length);
        writeProgramToMemory(program);
    }

    private String readProgramFile() throws IOException {
        try {
            return Files.readString(Path.of(getClass().getResource(PROGRAM_FILE).toURI()));
        } catch (Exception e) {
            throw new IOException("Failed to read program file: " + PROGRAM_FILE, e);
        }
    }

    private void validateProgramSize(int programLength) {
        if (programLength >= RAM_SIZE / 4) {
            throw new IllegalArgumentException(
                    String.format("Program too large for RAM (max %d instructions)", RAM_SIZE / 4)
            );
        }
    }

    private void writeProgramToMemory(int[] program) {
        System.out.printf("Loading %d instructions to RAM at 0x%016X%n",
                program.length, RAM_BASE);

        for (int i = 0; i < program.length; i++) {
            memory.writeInt(RAM_BASE + i * 4L, program[i]);
        }
    }

    private void startCpuExecution() {
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("CPU execution started");
                cpu.run();
                Platform.runLater(this::printFinalState);
            } catch (Exception e) {
                Platform.runLater(() -> handleExecutionError(e));
            }
        }, cpuExecutor);
    }

    private void printFinalState() {
        System.out.println("\n=== Execution Complete ===");
        cpu.printRegisters();
    }

    private void handleStartupError(Throwable t) {
        System.err.println("Fatal startup error:");
        t.printStackTrace();
        Platform.exit();
    }

    private void handleExecutionError(Throwable t) {
        System.err.println("CPU execution error:");
        t.printStackTrace();
    }

    public static void main(String[] args) {
        try {
            System.out.println("Launching " + APP_NAME);
            launch(args);
        } catch (Exception e) {
            System.err.println("GUI launch failed, falling back to console mode");
            new ConsoleRunner().run();
        }
    }

    private static class ConsoleRunner {
        public void run() {
            try {
                Memory memory = new Memory();
                Cpu cpu = new Cpu(memory);
                Main main = new Main();

                main.loadProgram();
                cpu.run();
                cpu.printRegisters();
            } catch (Exception e) {
                System.err.println("Console mode failed:");
                e.printStackTrace();
            }
        }
    }
}