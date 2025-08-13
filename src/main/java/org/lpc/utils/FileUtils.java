// FileUtils.java
package org.lpc.utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public class FileUtils {
    private static final String COMPILED_OUTPUT = "src/main/resources/out/compiled.tasm";

    public static String loadResource(String path) {
        try (InputStream is = Objects.requireNonNull(
                FileUtils.class.getResourceAsStream(path))) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }

    public static void saveCompiledCode(List<String> compiledCode) {
        try {
            Path filePath = Path.of(COMPILED_OUTPUT);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, String.join("\n", compiledCode),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            Logger.log("Warning: Could not save debug file: %s", e.getMessage());
        }
    }
}