// StageUtils.java
package org.lpc.utils;

import org.lpc.memory.MemoryMap;

import java.util.function.Supplier;

public class StageUtils {
    public static void validateProgramSize(int[] program) {
        long maxInstructions = MemoryMap.RAM_SIZE / Integer.BYTES;
        if (program.length > maxInstructions) {
            throw new IllegalArgumentException(
                    String.format("Program too large (max %d instructions, got %d)",
                            maxInstructions, program.length));
        }
    }

    public static <T> T timeStage(String name, Supplier<T> operation) {
        Logger.log("Starting %s stage...", name);
        long start = System.nanoTime();
        T result = operation.get();
        double duration = (System.nanoTime() - start) / 1_000_000.0;
        Logger.log("%s completed in %.2f ms", name, duration);
        return result;
    }
}