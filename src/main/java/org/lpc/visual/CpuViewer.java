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
import org.lpc.cpu.RegisterInfo;
import org.lpc.visual.style.Colors;
import org.lpc.visual.style.Fonts;
import org.lpc.visual.style.Styles;

public class CpuViewer {
    private final Cpu cpu;
    private final Label[] registerLabels = new Label[32];
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
                Styles.title("CPU State Monitor"),
                createRegistersSection(),
                createControlRegistersSection()
        );

        startAutoRefresh();

        stage.setTitle("CPU Viewer - Debug Monitor");
        stage.setScene(new Scene(root, 850, 700));  // Increased window width
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
        grid.setHgap(25);  // Increased horizontal gap
        grid.setVgap(10);  // Increased vertical gap
        grid.setPadding(new Insets(15));
        grid.setStyle(Styles.cardStyle());

        // Set column constraints to ensure proper spacing
        ColumnConstraints nameCol = new ColumnConstraints();
        nameCol.setMinWidth(10);  // Wider for register names
        ColumnConstraints valueCol = new ColumnConstraints();
        valueCol.setMinWidth(150); // Much wider for 64-bit values

        // Apply column constraints to all columns (4 pairs of name+value columns)
        for (int i = 0; i < 8; i++) {
            grid.getColumnConstraints().addAll(nameCol, valueCol);
        }

        for (int i = 0; i < 32; i++) {
            Label name = Styles.monoLabel(RegisterInfo.REG_NAMES[i]);
            Label value = Styles.valueLabel();
            registerLabels[i] = value;

            int row = i / 4;
            int col = (i % 4) * 2;  // Each register takes 2 columns (name + value)

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
        grid.setHgap(5);  // Increased gap
        grid.setVgap(15);   // Increased gap
        grid.setPadding(new Insets(15));
        grid.setStyle(Styles.cardStyle());

        addControlRow(grid, "Program Counter (PC):", programCounterLabel, 0);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(180);  // Wider label column
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setMinWidth(200);  // Wider value column for 64-bit addresses
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
            for (int i = 0; i < 32; i++) {
                // Display as 64-bit hex value
                registerLabels[i].setText(String.format("0x%016X", cpu.getRegister(i)));
            }

            // Display PC as 64-bit hex value
            programCounterLabel.setText(String.format("0x%016X", cpu.getProgramCounter()));
        });
    }
}