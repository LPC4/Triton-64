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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.lpc.memory.MemoryMap.RAM_BASE;
import static org.lpc.memory.MemoryMap.RAM_SIZE;

/**
 * Triton-64 Virtual Machine main application class.
 * Manages the virtual machine lifecycle including initialization,
 * program loading, execution, and debugging views.
 */
public final class Main extends Application {

    private static final String PROGRAM_FILE = "/test.asm";
    private static final String APP_NAME = "Triton-64 Virtual Machine";
    private static final int CPU_THREAD_POOL_SIZE = 1;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ExecutorService cpuExecutor = Executors.newFixedThreadPool(CPU_THREAD_POOL_SIZE);
    private Memory memory;
    private Cpu cpu;
    private MemoryViewer memoryViewer;
    private CpuViewer cpuViewer;

    /**
     * Initializes the virtual machine components.
     */
    @Override
    public void init() {
        log("Initializing...");
        initializeVirtualMachine();
        initializeDebugViews();
    }

    /**
     * Starts the application and launches the debug views.
     * @param primaryStage The primary stage (unused in this application)
     */
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

    /**
     * Cleans up resources when the application stops.
     */
    @Override
    public void stop() {
        log("Shutting down...");
        shutdownExecutorService();
        Platform.exit();
    }

    private void initializeVirtualMachine() {
        this.memory = new Memory();
        this.cpu = new Cpu(memory);
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
        int[] program = assembleProgram(sourceCode);
        validateProgramSize(program.length);
        writeProgramToMemory(program);
    }

    private String readProgramFile() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(PROGRAM_FILE)) {
            if (is == null) {
                throw new IOException("Program file not found: " + PROGRAM_FILE);
            }
            return new String(is.readAllBytes());
        }
    }

    private int[] assembleProgram(String sourceCode) {
        return new Assembler().assemble(sourceCode);
    }

    private void validateProgramSize(int programLength) {
        if (programLength >= RAM_SIZE / Integer.BYTES) {
            throw new IllegalArgumentException(
                    String.format("Program too large for RAM (max %d instructions)", RAM_SIZE / Integer.BYTES)
            );
        }
    }

    private void writeProgramToMemory(int[] program) {
        log("Loading %d instructions to RAM at 0x%016X%n", program.length, RAM_BASE);

        for (int i = 0; i < program.length; i++) {
            memory.writeInt(RAM_BASE + (long) i * Integer.BYTES, program[i]);
        }
    }

    private void startCpuExecution() {
        CompletableFuture.runAsync(() -> {
            try {
                log("CPU execution started");
                cpu.run();
                Platform.runLater(this::printFinalState);
            } catch (Exception e) {
                Platform.runLater(() -> handleExecutionError(e));
            }
        }, cpuExecutor);
    }

    private void printFinalState() {
        log("\n=== Execution Complete ===");
        cpu.printRegisters();
    }

    private void shutdownExecutorService() {
        cpuExecutor.shutdown();
        try {
            if (!cpuExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                cpuExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cpuExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void handleStartupError(Throwable t) {
        logError("Fatal startup error", t);
        Platform.exit();
    }

    private void handleExecutionError(Throwable t) {
        logError("CPU execution error", t);
    }

    private static void log(String format, Object... args) {
        System.out.printf(APP_NAME + " - " + format + "\n", args);
    }

    private static void logError(String message, Throwable t) {
        System.err.println(APP_NAME + " - " + message + ":");
        t.printStackTrace();
    }

    /**
     * Entry point for the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            log("Launching %s%n", APP_NAME);
            launch(args);
        } catch (Exception e) {
            logError("GUI launch failed, falling back to console mode", e);
            new ConsoleRunner().run();
        }
    }

    /**
     * Console-only runner for environments where JavaFX is not available.
     */
    private static final class ConsoleRunner {
        public void run() {
            try {
                Memory memory = new Memory();
                Cpu cpu = new Cpu(memory);
                Main main = new Main();

                main.loadProgram();
                cpu.run();
                cpu.printRegisters();
            } catch (Exception e) {
                logError("Console mode failed", e);
                System.exit(1);
            }
        }
    }
}