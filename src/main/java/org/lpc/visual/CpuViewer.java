package org.lpc.visual;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.lpc.cpu.Cpu;
import org.lpc.visual.style.Colors;
import org.lpc.visual.style.Fonts;
import org.lpc.visual.style.Styles;

public class CpuViewer {
    private final Cpu cpu;
    private final Label[] registerLabels = new Label[32]; // Adjusted for 32 registers
    private final Label programCounterLabel = Styles.valueLabel();
    private long lastUpdate = 0;

    public CpuViewer(Cpu cpu) {
        this.cpu = cpu;
    }

    public void start(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Colors.BACKGROUND + ";");
        root.getChildren().addAll(
                Styles.title("Cpu State Monitor"),
                createRegistersSection(),
                createControlRegistersSection()
        );

        startAutoRefresh();

        stage.setTitle("Cpu Viewer - Debug Monitor");
        stage.setScene(new Scene(root, 700, 650));
        stage.show();
    }

    private void startAutoRefresh() {
        AnimationTimer refreshTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 20_000_000) {
                    refresh();
                    lastUpdate = now;
                }
            }
        };
        refreshTimer.start();
    }

    private VBox createRegistersSection() {
        VBox section = new VBox(10);
        section.getChildren().addAll(Styles.sectionHeader("📊 General Purpose Registers"));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));
        grid.setStyle(Styles.cardStyle());

        for (int i = 0; i < 32; i++) { // Adjusted for 32 registers
            Label name = Styles.monoLabel(String.format("R%02d", i));
            Label value = Styles.valueLabel();
            registerLabels[i] = value;

            int row = i / 4;
            int col = (i % 4) * 2;

            grid.add(name, col, row);
            grid.add(value, col + 1, row);
        }

        section.getChildren().add(grid);
        return section;
    }

    private VBox createControlRegistersSection() {
        VBox section = new VBox(10);
        section.getChildren().addAll(Styles.sectionHeader("🎛 Control Registers"));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        grid.setStyle(Styles.cardStyle());

        addControlRow(grid, "Program Counter (PC):", programCounterLabel, 0);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(150);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        section.getChildren().add(grid);
        return section;
    }

    private void addControlRow(GridPane grid, String name, Label valueLabel, int row) {
        Label nameLabel = Styles.monoLabel(name);
        grid.add(nameLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    public void refresh() {
        Platform.runLater(() -> {
            for (int i = 0; i < 32; i++) { // Adjusted for 32 registers
                registerLabels[i].setText(String.format("0x%08X", cpu.getRegister(i)));
            }

            programCounterLabel.setText(String.format("0x%08X", cpu.getProgramCounter()));
        });
    }
}
