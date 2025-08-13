package org.lpc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.lpc.assembler.Assembler;
import org.lpc.compiler.TriCCompiler;
import org.lpc.cpu.Cpu;
import org.lpc.io.IODeviceManager;
import org.lpc.io.devices.KeyboardDevice;
import org.lpc.io.devices.TimerDevice;
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
import java.util.function.Supplier;

import static org.lpc.memory.MemoryMap.RAM_BASE;
import static org.lpc.memory.MemoryMap.RAM_SIZE;

@SuppressWarnings("FieldCanBeLocal")
public final class Main extends Application {
    // Application components
    private static final String MAIN_FILE = "/test/test_typing3.tc";
    private static final String APP_NAME = "Triton-64 VM";
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> 
            new Thread(r, "VM-Worker") {{setDaemon(true);}}
    );
    
    // VM components
    private Memory memory;
    private Cpu cpu;

    // I/O devices
    private KeyboardDevice keyboardDevice;
    private TimerDevice timerDevice;

    /**
     * TODO:
     *   - make ptr's longs, no specific ptr type
     */

    @Override
    public void init() {
        log("Initializing virtual machine components...");
        IODeviceManager ioDeviceManager = new IODeviceManager();

        memory = new Memory(ioDeviceManager);
        cpu = new Cpu(memory);

        keyboardDevice = new KeyboardDevice(MemoryMap.MMIO_BASE);
        timerDevice = new TimerDevice(MemoryMap.MMIO_BASE + KeyboardDevice.SIZE);
        ioDeviceManager.addDevices(keyboardDevice, timerDevice);

        log("VM components initialized successfully");
    }

    @Override
    public void start(Stage primaryStage) {
        executePipeline()
                .exceptionally(this::handleError);
    }

    private CompletableFuture<Void> executePipeline() {
        return CompletableFuture
                .supplyAsync(this::compileStage, executor)
                .thenCompose(this::assembleStage)
                .thenCompose(this::loadStage)
                .thenCompose(this::launchDebugViewsStage)
                .thenCompose(this::executeStage);
    }

    private List<String> compileStage() {
        return timeStage("Compilation", () -> {
            TriCCompiler compiler = new TriCCompiler(loadMainFile());
            List<String> compiledCode = compiler.compile();
            saveCompiledCode(compiledCode);
            log("Compiled %d lines of TriC code", compiledCode.size());
            return compiledCode;
        });
    }

    private CompletableFuture<int[]> assembleStage(List<String> compiledCode) {
        return CompletableFuture.supplyAsync(() ->
                timeStage("Assembly", () -> {
                    String assemblyCode = String.join("\n", compiledCode);
                    int[] program = new Assembler().assemble(assemblyCode);
                    log("Assembled %d instructions", program.length);
                    return program;
                }), executor
        );
    }

    private CompletableFuture<int[]> loadStage(int[] program) {
        return CompletableFuture.supplyAsync(() ->
                timeStage("Loading", () -> {
                    validateProgramSize(program);
                    for (int i = 0; i < program.length; i++) {
                        long address = RAM_BASE + (long) i * Integer.BYTES;
                        memory.writeInt(address, program[i]);
                    }
                    log("Loaded %d instructions to RAM", program.length);
                    return program;
                }), executor
        );
    }

    private CompletableFuture<int[]> launchDebugViewsStage(int[] program) {
        CompletableFuture<Void> uiSetup = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                // Launch debug views
                createTextModeViewer();
                createMemoryViewer();
                createCpuViewer();

                log("Debug views launched successfully");
                uiSetup.complete(null);
            } catch (Exception e) {
                uiSetup.completeExceptionally(e);
            }
        });

        return uiSetup.thenApply(ignored -> program);
    }

    private CompletableFuture<Void> executeStage(int[] program) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Small delay to ensure UI is fully initialized
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            timeStage("Execution", () -> {
                cpu.run();
                logExecutionMetrics();
                scheduleShutdown();
                return null;
            });
        }, executor);
    }

    private void createTextModeViewer() {
        Stage textModeStage = new Stage();
        TextModeViewer textModeViewer = new TextModeViewer(cpu);
        textModeViewer.start(textModeStage);
        keyboardDevice.setScene(textModeStage.getScene());
        textModeStage.requestFocus();
    }

    private void createMemoryViewer() {
        Stage memoryStage = new Stage();
        new MemoryViewer(cpu).start(memoryStage);
    }

    private void createCpuViewer() {
        Stage cpuStage = new Stage();
        new CpuViewer(cpu).start(cpuStage);
    }

    private <T> T timeStage(String name, Supplier<T> operation) {
        log("Starting %s stage...", name);
        long start = System.nanoTime();
        T result = operation.get();
        double duration = (System.nanoTime() - start) / 1_000_000.0;
        log("%s completed in %.2f ms", name, duration);
        return result;
    }

    private void validateProgramSize(int[] program) {
        long maxInstructions = RAM_SIZE / Integer.BYTES;
        if (program.length > maxInstructions) {
            throw new IllegalArgumentException(
                    String.format("Program too large (max %d instructions, got %d)",
                            maxInstructions, program.length));
        }
    }

    private void saveCompiledCode(List<String> compiledCode) {
        try {
            Path filePath = Path.of("src/main/resources/out/compiled.tasm");
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, String.join("\n", compiledCode),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            log("Warning: Could not save debug file: %s", e.getMessage());
        }
    }

    private void logExecutionMetrics() {
        log("\n=== Execution Complete ===");
        cpu.printRegisters();
    }

    private void scheduleShutdown() {
        executor.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(Platform::exit);
        });
    }

    @SneakyThrows
    private String loadMainFile() {
        try (InputStream is = Objects.requireNonNull(
                getClass().getResourceAsStream(Main.MAIN_FILE))) {
            return new String(is.readAllBytes());
        }
    }

    private Void handleError(Throwable throwable) {
        logError(throwable);
        Platform.runLater(Platform::exit);
        return null;
    }

    @Override
    public void stop() {
        log("Initiating graceful shutdown...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log("Shutdown complete");
    }

    private static void log(String format, Object... args) {
        String timestamp = String.format("%tT", System.currentTimeMillis());
        System.out.printf("[%s] [%s] %s%n", timestamp, APP_NAME, String.format(format, args));
    }

    private static void logError(Throwable t) {
        String timestamp = String.format("%tT", System.currentTimeMillis());
        System.err.printf("[%s] [%s] ERROR: %s%n", timestamp, APP_NAME, t.getMessage());
        t.printStackTrace(System.err);
    }

    public static void main(String[] args) {
        log("Launching %s", APP_NAME);
        launch(args);
    }
}