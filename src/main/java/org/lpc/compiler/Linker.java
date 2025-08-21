package org.lpc.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very simple linker class that just concats the code of all files
 * not really a linker in the traditional sense, but serves the purpose
 * adding a real one would require .object files and a lot more complexity
 */
public class Linker {
    private final String sourceCode;

    public Linker(String sourceCode) {
        Objects.requireNonNull(sourceCode, "Source code cannot be null");
        this.sourceCode = sourceCode;
    }

    public String link() {
        List<String> imports = findImports(sourceCode);
        StringBuilder linkedCode = new StringBuilder();

        // Add stdlib
        imports.add("memory");
        imports.add("console");
        imports.add("string");

        // Add library code first
        for (String importName : imports) {
            String libraryCode = loadLibrary(importName);
            if (libraryCode != null) {
                linkedCode.append(libraryCode).append("\n");
            }
        }

        // Add original source code (without import statements)
        String sourceWithoutImports = removeImportStatements(sourceCode);
        linkedCode.append(sourceWithoutImports);

        return linkedCode.toString();
    }

    private List<String> findImports(String code) {
        List<String> imports = new ArrayList<>();
        Pattern importPattern = Pattern.compile("^\\s*import\\s+(\\w+)\\s*(?:;.*)?$", Pattern.MULTILINE);
        Matcher matcher = importPattern.matcher(code);

        while (matcher.find()) {
            imports.add(matcher.group(1));
        }

        return imports;
    }

    private String removeImportStatements(String code) {
        return code.replaceAll("(?m)^\\s*import\\s+\\w+\\s*(?:;.*)?$\\n?", "");
    }

    private String loadLibrary(String name) {
        try {
            Path libPath = Paths.get("src/main/resources/kernel/" + name + ".tlib");
            return Files.readString(libPath);
        } catch (IOException e) {
            System.err.println("Warning: Could not load library " + name + ".tlib");
            return null;
        }
    }
}