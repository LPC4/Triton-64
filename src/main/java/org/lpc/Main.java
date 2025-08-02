package org.lpc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.lpc.assembler.Assembler;
import org.lpc.compiler.TriCCompiler;
import org.lpc.cpu.Cpu;
import org.lpc.memory.Memory;
import org.lpc.visual.CpuViewer;
import org.lpc.visual.MemoryViewer;

import java.io.InputStream;
import java.util.concurrent.*;

import static org.lpc.memory.MemoryMap.RAM_BASE;
import static org.lpc.memory.MemoryMap.RAM_SIZE;

public final class Main extends Application {
    private static final String PROGRAM_FILE = "/test.asm";
    private static final String TRIC_FILE = "/example.tric";
    private static final String APP_NAME = "Triton-64 Virtual Machine";
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Memory memory;
    private Cpu cpu;
    private TriCCompiler compiler;

    @SneakyThrows
    @Override
    public void init() {
        log("Initializing...");
        memory = new Memory();
        cpu = new Cpu(memory);
        compiler = new TriCCompiler(loadTritonCCode());
        //launchDebugViews();
    }

    @SneakyThrows
    @Override
    public void start(Stage primaryStage) {
        log("Loading program...");

        compiler.compile();

        /*
        int[] program = new Assembler().assemble(loadSourceCode());
        if (program.length > RAM_SIZE / Integer.BYTES)
            throw new IllegalArgumentException("Program too large for RAM");
        loadToRam(program);

        log("Starting CPU...");
        CompletableFuture.runAsync(() -> {
            try {
                cpu.run();
                log("\n=== Execution Complete ===");
                cpu.printRegisters();
            } catch (Exception e) {
                logError("CPU execution error", e);
                Platform.exit();
            }
        }, executor);
        */
    }

    @Override
    public void stop() {
        log("Shutting down...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void launchDebugViews() {
        Platform.runLater(() -> {
            new MemoryViewer(cpu).start(new Stage());
            new CpuViewer(cpu).start(new Stage());
        });
    }

    private String loadSourceCode() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(PROGRAM_FILE)) {
            if (is == null) throw new IllegalStateException("Missing file: " + PROGRAM_FILE);
            return new String(is.readAllBytes());
        }
    }

    private String loadTritonCCode() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(TRIC_FILE)) {
            if (is == null) throw new IllegalStateException("Missing file: " + TRIC_FILE);
            return new String(is.readAllBytes());
        }
    }

    private void loadToRam(int[] program) {
        log("Writing %d instructions to RAM at 0x%016X", program.length, RAM_BASE);
        for (int i = 0; i < program.length; i++) {
            memory.writeInt(RAM_BASE + (long) i * Integer.BYTES, program[i]);
        }
    }

    private static void log(String format, Object... args) {
        System.out.printf(APP_NAME + " - " + format + "%n", args);
    }

    private static void logError(String message, Throwable t) {
        System.err.println(APP_NAME + " - " + message + ":");
        t.printStackTrace();
    }

    public static void main(String[] args) {
        log("Launching %s", APP_NAME);
        launch(args);
    }
}
