// PipelineExecutor.java
package org.lpc;

import javafx.application.Platform;
import org.lpc.assembler.Assembler;
import org.lpc.compiler.TriCCompiler;
import org.lpc.memory.Memory;
import org.lpc.memory.MemoryMap;
import org.lpc.utils.FileUtils;
import org.lpc.utils.Logger;
import org.lpc.utils.StageUtils;
import org.lpc.visual.DebugViewManager;

import java.util.List;
import java.util.concurrent.*;

public class PipelineExecutor {
    private VirtualMachine vm;
    private final ExecutorService executor = Executors.newCachedThreadPool(r ->
            new Thread(r, "VM-Worker") {{ setDaemon(true); }}
    );

    public void initializeVM() {
        vm = new VirtualMachine();
        Logger.log("VM components initialized successfully");
    }

    public void executePipeline() {
        CompletableFuture
                .supplyAsync(this::compileStage, executor)
                .thenCompose(this::assembleStage)
                .thenCompose(this::loadStage)
                .thenCompose(this::launchDebugViewsStage)
                .thenCompose(this::executeStage)
                .exceptionally(this::handleError);
    }

    private List<String> compileStage() {
        return StageUtils.timeStage("Compilation", () -> {
            String sourceCode = FileUtils.loadResource(Main.EXECUTED_FILE);
            List<String> compiledCode = new TriCCompiler(sourceCode).compile();
            FileUtils.saveCompiledCode(compiledCode);
            Logger.log("Compiled %d lines of TriC code", compiledCode.size());
            return compiledCode;
        });
    }

    private CompletableFuture<int[]> assembleStage(List<String> compiledCode) {
        return CompletableFuture.supplyAsync(() ->
                StageUtils.timeStage("Assembly", () -> {
                    String assemblyCode = String.join("\n", compiledCode);
                    int[] program = new Assembler().assemble(assemblyCode);
                    Logger.log("Assembled %d instructions", program.length);
                    return program;
                }), executor
        );
    }

    private CompletableFuture<int[]> loadStage(int[] program) {
        return CompletableFuture.supplyAsync(() ->
                StageUtils.timeStage("Loading", () -> {
                    StageUtils.validateProgramSize(program);
                    Memory memory = vm.getMemory();
                    for (int i = 0; i < program.length; i++) {
                        long address = MemoryMap.RAM_BASE + (long) i * Integer.BYTES;
                        memory.writeInt(address, program[i]);
                    }
                    long maxInstructions = (MemoryMap.RAM_SIZE - MemoryMap.STACK_HEAP_SIZE) / 4;
                    float percentageFilled = (float) ((program.length / (double) maxInstructions) * 100);
                    Logger.log("Loaded %d out of max %d instructions to RAM", program.length, maxInstructions);
                    Logger.log("Memory usage: %.3f%%", percentageFilled);
                    return program;
                }), executor
        );
    }

    private CompletableFuture<int[]> launchDebugViewsStage(int[] program) {
        CompletableFuture<Void> uiFuture = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                new DebugViewManager(vm).launchDebugViews();
                uiFuture.complete(null);
            } catch (Exception e) {
                uiFuture.completeExceptionally(e);
            }
        });
        return uiFuture.thenApply(ignored -> program);
    }

    private CompletableFuture<Void> executeStage(int[] program) {
        return CompletableFuture.runAsync(() -> {
            StageUtils.timeStage("Execution", () -> {
                vm.getCpu().run();
                Logger.log("\n=== Execution Complete ===");
                vm.getCpu().printRegisters();
                scheduleShutdown();
                return null;
            });
        }, executor);
    }

    private Void handleError(Throwable throwable) {
        Logger.logError(throwable);
        Platform.runLater(Platform::exit);
        return null;
    }

    private void scheduleShutdown() {
        executor.submit(() -> {
            try { Thread.sleep(2000); }
            catch (InterruptedException ignored) {}
            Platform.runLater(Platform::exit);
        });
    }

    public void shutdown() {
        Logger.log("Initiating graceful shutdown...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Logger.log("Shutdown complete");
    }
}