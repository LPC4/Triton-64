package org.lpc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.lpc.assembler.Assembler;
import org.lpc.compiler.TriCCompiler;
import org.lpc.cpu.Cpu;
import org.lpc.io.IODeviceManager;
import org.lpc.io.devices.KeyboardDevice;
import org.lpc.memory.Memory;
import org.lpc.memory.MemoryMap;
import org.lpc.visual.CpuViewer;
import org.lpc.visual.MemoryViewer;
import org.lpc.visual.TextModeViewer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static org.lpc.memory.MemoryMap.RAM_BASE;
import static org.lpc.memory.MemoryMap.RAM_SIZE;

public final class Main extends Application {
    private static final String TRIC_FILE = "/test/test_textmode.tc";
    private static final String APP_NAME = "Triton-64 VM";
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "VM-Worker");
        t.setDaemon(true);
        return t;
    });

    private IODeviceManager ioDeviceManager;
    private Memory memory;
    private Cpu cpu;
    private TriCCompiler compiler;
    private final CompletableFuture<Void> initializationComplete = new CompletableFuture<>();
    private KeyboardDevice keyboardDevice; // Reference to keyboard device

    @Override
    public void init() {
        log("Initializing virtual machine components...");
        initializeComponents();
    }

    @SneakyThrows
    private void initializeComponents() {
        try {
            ioDeviceManager = new IODeviceManager();
            memory = new Memory(ioDeviceManager);
            cpu = new Cpu(memory);
            compiler = new TriCCompiler(loadResource(TRIC_FILE));
            log("VM components initialized successfully");
            initializationComplete.complete(null);
        } catch (Exception e) {
            initializationComplete.completeExceptionally(e);
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        log("Starting application...");

        // Create keyboard device without scene initially
        keyboardDevice = new KeyboardDevice(MemoryMap.MMIO_BASE);
        ioDeviceManager.addDevice(keyboardDevice);

        // Wait for initialization to complete before starting pipeline
        initializationComplete
                .thenRunAsync(this::executeCompilationPipeline, executor)
                .exceptionally(this::handleError);
    }

    private void executeCompilationPipeline() {
        try {
            log("=== Starting Compilation Pipeline ===");

            // Step 1: Compile TriC code
            log("Step 1: Compiling TriC code...");
            List<String> compiledCode = compileTricCode();
            log("TriC compilation completed (%d lines)", compiledCode.size());

            // Step 2: Assemble the compiled code directly
            log("Step 2: Assembling program...");
            int[] program = assembleProgram(compiledCode);
            log("Assembly completed (%d instructions)", program.length);

            // Step 3: Execute the program
            log("Step 3: Executing program...");
            executeProgram(program);

        } catch (Exception e) {
            throw new RuntimeException("Pipeline execution failed", e);
        }
    }

    private List<String> compileTricCode() {
        try {
            return compiler.compile();
        } catch (Exception e) {
            throw new RuntimeException("TriC compilation failed: " + e.getMessage(), e);
        }
    }

    private int[] assembleProgram(List<String> compiledCode) {
        try {
            String assemblyCode = String.join("\n", compiledCode);
            saveCompiledCodeForDebugging(compiledCode);
            return new Assembler().assemble(assemblyCode);
        } catch (Exception e) {
            throw new RuntimeException("Assembly failed: " + e.getMessage(), e);
        }
    }

    private void saveCompiledCodeForDebugging(List<String> compiledCode) {
        try {
            Path filePath = Path.of("src/main/resources/compiled.asm");
            Files.createDirectories(filePath.getParent());
            String content = String.join("\n", compiledCode);
            Files.writeString(filePath, content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            log("Debug: Compiled code saved to %s", filePath);
        } catch (Exception e) {
            log("Warning: Could not save debug file: %s", e.getMessage());
        }
    }

    private void executeProgram(int[] program) {
        validateProgramSize(program);
        loadToRam(program);
        Platform.runLater(this::launchDebugViews);
        executor.submit(() -> runCpuAndShutdown(program));
    }

    private void validateProgramSize(int[] program) {
        long maxInstructions = RAM_SIZE / Integer.BYTES;
        if (program.length > maxInstructions) {
            throw new IllegalArgumentException(
                    String.format("Program too large for RAM (max %d instructions, got %d)",
                            maxInstructions, program.length));
        }
        log("Program size validation passed: %d/%d instructions", program.length, maxInstructions);
    }

    private void loadToRam(int[] program) {
        log("Loading %d instructions to RAM at 0x%016X", program.length, RAM_BASE);
        for (int i = 0; i < program.length; i++) {
            long address = RAM_BASE + (long) i * Integer.BYTES;
            memory.writeInt(address, program[i]);
        }
        log("Program loaded to RAM successfully");
    }

    private void runCpuAndShutdown(int[] program) {
        log("Starting CPU execution...");
        try {
            long startTime = System.nanoTime();
            cpu.run();
            long endTime = System.nanoTime();
            double executionTimeMs = (endTime - startTime) / 1_000_000.0;
            log("\n=== Execution Complete ===");
            log("Execution time: %.2f ms", executionTimeMs);
            log("Instructions executed: %d", program.length);
            cpu.printRegisters();
        } catch (Exception e) {
            log("CPU execution failed: %s", e.getMessage());
            e.printStackTrace();
        } finally {
            executor.submit(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
                Platform.runLater(Platform::exit);
            });
        }
    }

    private Void handleError(Throwable throwable) {
        logError(throwable);
        Platform.runLater(Platform::exit);
        return null;
    }

    private void launchDebugViews() {
        log("Launching debug views...");

        // Create and show text mode viewer first
        Stage textModeStage = new Stage();
        TextModeViewer textModeViewer = new TextModeViewer(cpu);
        textModeViewer.start(textModeStage);

        // Get the scene from the text mode viewer
        Scene textModeScene = textModeStage.getScene();

        // Connect keyboard to the text mode scene
        keyboardDevice.setScene(textModeScene);

        // Focus the text mode window
        textModeStage.requestFocus();

        // Launch other debug views
        launchMemoryViewer();
        launchCpuViewer();

        log("Debug view launch requests sent");
    }

    private void launchMemoryViewer() {
        try {
            Stage memoryStage = new Stage();
            new MemoryViewer(cpu).start(memoryStage);
        } catch (Exception e) {
            log("Warning: Could not launch MemoryViewer: %s", e.getMessage());
        }
    }

    private void launchCpuViewer() {
        try {
            Stage cpuStage = new Stage();
            new CpuViewer(cpu).start(cpuStage);
        } catch (Exception e) {
            log("Warning: Could not launch CpuViewer: %s", e.getMessage());
        }
    }

    @SneakyThrows
    private String loadResource(String resourcePath) {
        try (InputStream is = Objects.requireNonNull(
                getClass().getResourceAsStream(resourcePath),
                "Missing resource: " + resourcePath)) {
            return new String(is.readAllBytes());
        }
    }

    @Override
    public void stop() {
        log("Initiating graceful shutdown...");
        initializationComplete.cancel(true);
        shutdownExecutor(executor, "Main executor");
        log("Shutdown complete");
    }

    private void shutdownExecutor(ExecutorService executor, String executorName) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log("%s did not terminate gracefully, forcing shutdown...", executorName);
                List<Runnable> pendingTasks = executor.shutdownNow();
                log("Cancelled %d pending tasks", pendingTasks.size());
            } else {
                log("%s terminated gracefully", executorName);
            }
        } catch (InterruptedException e) {
            log("%s interrupted during shutdown", executorName);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void log(String format, Object... args) {
        String timestamp = String.format("%tT", System.currentTimeMillis());
        System.out.printf("[%s] [%s] %s%n", timestamp, APP_NAME, String.format(format, args));
    }

    private static void logError(Throwable t) {
        String timestamp = String.format("%tT", System.currentTimeMillis());
        System.err.printf("[%s] [%s] ERROR: %s%n", timestamp, APP_NAME, "Pipeline error");
        t.printStackTrace(System.err);
    }

    public static void main(String[] args) {
        log("Launching %s", APP_NAME);
        launch(args);
    }
}