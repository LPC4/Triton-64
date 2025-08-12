package org.lpc.rom;

import org.lpc.assembler.Assembler;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Objects;

public class RomDumper {
    public static void main(String[] args) throws Exception {
        File input = new File(Objects.requireNonNull(RomDumper.class.getResource("/rom/rom.tasm")).toURI());
        File output = new File("src/main/java/org/lpc/rom/ROMData.java");

        if (!output.getParentFile().exists()) {
            if (output.getParentFile().mkdirs()) {
                System.out.println("Created directory: " + output.getParentFile().getAbsolutePath());
            } else {
                throw new RuntimeException("Failed to create directory: " + output.getParentFile().getAbsolutePath());
            }
        }

        String source = new String(Files.readAllBytes(input.toPath()));
        int[] program = new Assembler().assemble(source);

        try (FileWriter writer = new FileWriter(output)) {
            writer.write("package org.lpc.rom;\n\n");

            writer.write("// Generated on " + java.time.LocalDateTime.now() + "\n");
            writer.write("// This file contains the ROM data for the Triton-64 virtual machine.\n");
            writer.write("// Do not modify this file manually. It is generated from rom.asm.\n");
            writer.write("// To regenerate, run the RomDumper class.\n\n");

            writer.write("public class ROMData {\n");
            writer.write("    public static final byte[] ROM = new byte[] {\n");

            for (int i = 0; i < program.length; i++) {
                int value = program[i];
                writer.write(String.format("        (byte)0x%02X, (byte)0x%02X, (byte)0x%02X, (byte)0x%02X",
                        (value) & 0xFF,
                        (value >> 8) & 0xFF,
                        (value >> 16) & 0xFF,
                        (value >> 24) & 0xFF));

                if (i < program.length - 1)
                    writer.write(",\n");
                else
                    writer.write("\n");
            }

            writer.write("    };\n");
            writer.write("}\n");
        }

        System.out.println("ROMData.java generated at: " + output.getAbsolutePath());
    }
}

