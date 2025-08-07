package org.lpc.visual;

import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.lpc.cpu.Cpu;
import org.lpc.cpu.InstructionSet;
import org.lpc.memory.Memory;
import org.lpc.memory.MemoryMap;
import org.lpc.visual.style.Colors;
import org.lpc.visual.style.Fonts;
import org.lpc.visual.style.Styles;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryViewer {
    private static final int ROW_SIZE = 8;
    private static final int ROWS_TO_SHOW = 16;

    private final Cpu cpu;
    private final Memory memory;

    private final Map<String, Long> memorySections = new LinkedHashMap<>();

    private long currentAddress;
    private ViewMode viewMode = ViewMode.HEX;

    private TableSection tableSection;
    private ControlPanel controlPanel;
    private StatusBar statusBar;

    private long lastUpdate = 0;

    public MemoryViewer(final Cpu cpu) {
        this.cpu = cpu;
        this.memory = cpu.getMemory();

        initMemorySections();
        currentAddress = MemoryMap.ROM_BASE;
    }

    private void initMemorySections() {
        memorySections.put("🔨 ROM", MemoryMap.ROM_BASE);
        memorySections.put("💾 RAM", MemoryMap.RAM_BASE);
        memorySections.put("🏗️ Heap", MemoryMap.HEAP_BASE);
        memorySections.put("📚 Stack", MemoryMap.STACK_BASE - 1024); // Show some stack data
        memorySections.put("🔌 MMIO", MemoryMap.MMIO_BASE);
        memorySections.put("🖥️ Framebuffer", MemoryMap.FB_BASE);
    }

    public void start(final Stage stage) {
        tableSection = new TableSection();
        controlPanel = new ControlPanel();
        statusBar = new StatusBar();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("Memory Viewer");
        title.setFont(Fonts.TITLE);
        title.setTextFill(Color.web(Colors.TEXT));
        title.setAlignment(Pos.CENTER);
        BorderPane.setMargin(title, new Insets(15, 20, 10, 20));

        root.setTop(new VBox(5, title, controlPanel.get()));
        root.setCenter(tableSection.get());
        root.setBottom(statusBar.get());

        BorderPane.setMargin(controlPanel.get(), new Insets(0, 20, 10, 20));
        BorderPane.setMargin(tableSection.get(), new Insets(0, 20, 10, 20));
        BorderPane.setMargin(statusBar.get(), new Insets(0, 20, 15, 20));

        refresh();

        Scene scene = new Scene(root, 900, 700); // Slightly wider for long addresses
        stage.setScene(scene);
        stage.setTitle("Memory Viewer - Debug Monitor");
        stage.setResizable(false);
        stage.show();

        startAutoRefresh();
    }

    private void refresh() {
        tableSection.refresh();
        statusBar.refresh();
    }

    private void startAutoRefresh() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 25_000_000) {
                    refresh();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private long clampAddress(final long addr) {
        long totalMemorySize = MemoryMap.ROM_SIZE + MemoryMap.RAM_SIZE + MemoryMap.MMIO_SIZE + MemoryMap.FB_SIZE;
        long maxStart = Math.max(0, totalMemorySize - ROW_SIZE * ROWS_TO_SHOW);
        if (addr < 0) return 0;
        if (addr > maxStart) return maxStart;
        return addr & ~(ROW_SIZE - 1); // Align to row boundary
    }

    private String getMemoryRegionName(long address) {
        if (address >= MemoryMap.ROM_BASE && address < MemoryMap.ROM_BASE + MemoryMap.ROM_SIZE) {
            return "ROM";
        } else if (address >= MemoryMap.RAM_BASE && address < MemoryMap.RAM_BASE + MemoryMap.RAM_SIZE) {
            return "RAM"; // Treat heap and stack as regular RAM
        } else if (address >= MemoryMap.MMIO_BASE && address < MemoryMap.MMIO_BASE + MemoryMap.MMIO_SIZE) {
            return "MMIO";
        } else if (address >= MemoryMap.FB_BASE && address < MemoryMap.FB_BASE + MemoryMap.FB_SIZE) {
            return "FB";
        }
        return "UNKNOWN";
    }

    private enum ViewMode { HEX, BITS, NUM }

    private static class MemoryRow {
        private final SimpleStringProperty address;
        private final SimpleStringProperty bytes;
        private final SimpleStringProperty ascii;
        private final SimpleStringProperty region;

        public MemoryRow(String address, String bytes, String ascii, String region) {
            this.address = new SimpleStringProperty(address);
            this.bytes = new SimpleStringProperty(bytes);
            this.ascii = new SimpleStringProperty(ascii);
            this.region = new SimpleStringProperty(region);
        }

        public String getAddress() { return address.get(); }
        public String getBytes() { return bytes.get(); }
        public String getAscii() { return ascii.get(); }
        public String getRegion() { return region.get(); }
    }

    private class TableSection {
        private final TableView<MemoryRow> table;
        private final VBox container;

        public TableSection() {
            table = new TableView<>();
            container = new VBox(8);
            container.setFillWidth(true);

            Label label = new Label("📋 Memory Contents");
            label.setFont(Fonts.SECTION);
            label.setTextFill(Color.web(Colors.ACCENT));

            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setStyle(Styles.cardStyle());

            TableColumn<MemoryRow, String> addrCol = new TableColumn<>("📍 Address");
            addrCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
            addrCol.setPrefWidth(150);
            addrCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            TableColumn<MemoryRow, String> regionCol = new TableColumn<>("🏷️ Region");
            regionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegion()));
            regionCol.setPrefWidth(80);
            regionCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            TableColumn<MemoryRow, String> bytesCol = new TableColumn<>("📊 Bytes");
            bytesCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBytes()));
            bytesCol.setMinWidth(200);
            bytesCol.setStyle("-fx-font-weight: bold; -fx-font-family: 'Consolas, Monaco, monospace';");

            TableColumn<MemoryRow, String> asciiCol = new TableColumn<>("📝 ASCII");
            asciiCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAscii()));
            asciiCol.setMinWidth(100);
            asciiCol.setStyle("-fx-font-weight: bold; -fx-font-family: 'Consolas, Monaco, monospace';");

            table.getColumns().addAll(addrCol, regionCol, bytesCol, asciiCol);

            table.setRowFactory(tv -> {
                TableRow<MemoryRow> row = new TableRow<>();
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        long pc = cpu.getProgramCounter();
                        long memAddr = Long.parseUnsignedLong(newItem.getAddress().replace("0x", ""), 16);

                        // Clear previous styles first
                        row.setStyle("");

                        if (memAddr == pc) {
                            row.setStyle(Styles.highlightRow());
                        } else if (memAddr >= pc && memAddr < pc + 32) {
                            row.setStyle(Styles.faintHighlightRow());
                        } else {
                            row.setStyle(Styles.monoRow());
                        }
                    }
                });
                return row;
            });

            container.getChildren().addAll(label, table);
            VBox.setVgrow(table, Priority.ALWAYS);
        }

        public Node get() {
            return container;
        }

        public void refresh() {
            table.setItems(new MemoryRenderer().render());
        }
    }

    private class StatusBar {
        private final Label addressLabel = new Label();
        private final Label memoryLabel = new Label();
        private final Label pcLabel = new Label();
        private final HBox bar;

        public StatusBar() {
            addressLabel.setFont(Fonts.MONO);
            addressLabel.setTextFill(Color.web(Colors.SUCCESS));

            memoryLabel.setFont(Fonts.LABEL);
            memoryLabel.setTextFill(Color.web(Colors.MUTED));

            pcLabel.setFont(Fonts.MONO);
            pcLabel.setTextFill(Color.web(Colors.ACCENT));

            bar = new HBox(15,
                    new Label("📍 Current View:"), addressLabel,
                    createSeparator(),
                    new Label("💾 Memory:"), memoryLabel,
                    createSeparator(),
                    pcLabel
            );
            bar.setAlignment(Pos.CENTER_LEFT);
            bar.setPadding(new Insets(10));
            bar.setStyle(Styles.cardStyle());
        }

        public Node get() {
            return bar;
        }

        public void refresh() {
            addressLabel.setText(String.format("0x%016X - 0x%016X",
                    currentAddress, currentAddress + (ROWS_TO_SHOW * ROW_SIZE) - 1));

            long totalSize = MemoryMap.ROM_SIZE + MemoryMap.RAM_SIZE + MemoryMap.MMIO_SIZE + MemoryMap.FB_SIZE;
            memoryLabel.setText(String.format("Total: %s | Showing: %d rows",
                    formatBytes(totalSize), ROWS_TO_SHOW));

            pcLabel.setText("PC: 0x" + String.format("%016X", cpu.getProgramCounter()));
        }

        private String formatBytes(long bytes) {
            if (bytes >= 1024 * 1024 * 1024) {
                return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
            } else if (bytes >= 1024 * 1024) {
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            } else if (bytes >= 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            }
            return bytes + " B";
        }

        private Node createSeparator() {
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setPrefHeight(20);
            return sep;
        }
    }

    private class ControlPanel {
        private final VBox panel;
        private final ComboBox<String> sectionSelector;
        private final TextField addressInput;

        public ControlPanel() {
            panel = new VBox(8);
            Label label = new Label("Navigation Controls");
            label.setFont(Fonts.SECTION);
            label.setTextFill(Color.web(Colors.ACCENT));

            sectionSelector = new ComboBox<>(FXCollections.observableArrayList(memorySections.keySet()));
            sectionSelector.setPrefWidth(150);
            sectionSelector.getSelectionModel().select(0); // Start with ROM
            Styles.styleCombo(sectionSelector);
            sectionSelector.setOnAction(e -> {
                currentAddress = clampAddress(memorySections.get(sectionSelector.getValue()));
                refresh();
            });

            ToggleButton viewToggle = Styles.toggleButton("Hex");
            viewToggle.setOnAction(e -> {
                switch (viewMode) {
                    case HEX -> { viewMode = ViewMode.BITS; viewToggle.setText("Bits"); }
                    case BITS -> { viewMode = ViewMode.NUM; viewToggle.setText("Num"); }
                    case NUM -> { viewMode = ViewMode.HEX; viewToggle.setText("Hex"); }
                }
                refresh();
            });

            Button prev = Styles.button("⬅ Prev");
            Button next = Styles.button("Next ➡");

            prev.setOnAction(e -> {
                currentAddress = clampAddress(currentAddress - ROW_SIZE * ROWS_TO_SHOW);
                refresh();
            });
            next.setOnAction(e -> {
                currentAddress = clampAddress(currentAddress + ROW_SIZE * ROWS_TO_SHOW);
                refresh();
            });

            addressInput = new TextField();
            addressInput.setPromptText("0x0000000000000000");
            addressInput.setPrefWidth(150);
            Styles.styleTextField(addressInput);

            Button go = Styles.button("🎯 Go");
            go.setOnAction(e -> {
                try {
                    long addr = Long.parseUnsignedLong(addressInput.getText().trim().replace("0x", ""), 16);
                    currentAddress = clampAddress(addr);
                    refresh();
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Invalid address format");
                    alert.showAndWait();
                }
            });

            HBox line = new HBox(12,
                    new Label("Section:"), sectionSelector,
                    createSeparator(),
                    viewToggle,
                    createSeparator(),
                    prev, next,
                    createSeparator(),
                    new Label("Address:"), addressInput, go
            );
            line.setAlignment(Pos.CENTER_LEFT);
            line.setPadding(new Insets(10));
            line.setStyle(Styles.cardStyle());

            panel.getChildren().addAll(label, line);
        }

        public Node get() {
            return panel;
        }

        private Node createSeparator() {
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setPrefHeight(20);
            return sep;
        }
    }

    private class MemoryRenderer {
        public ObservableList<MemoryRow> render() {
            ObservableList<MemoryRow> rows = FXCollections.observableArrayList();

            for (int row = 0; row < ROWS_TO_SHOW; row++) {
                final long addr = currentAddress + (long) row * ROW_SIZE;
                long totalMemorySize = MemoryMap.ROM_SIZE + MemoryMap.RAM_SIZE + MemoryMap.MMIO_SIZE + MemoryMap.FB_SIZE;

                if (addr + ROW_SIZE > totalMemorySize) break;

                StringBuilder bytesRep = new StringBuilder();
                StringBuilder asciiRep = new StringBuilder();

                for (int i = 0; i < ROW_SIZE; i++) {
                    try {
                        byte b = memory.readByte(addr + i);
                        int unsignedByte = Byte.toUnsignedInt(b);

                        bytesRep.append(switch (viewMode) {
                            case HEX -> String.format("%02X", unsignedByte);
                            case BITS -> String.format("%8s", Integer.toBinaryString(unsignedByte)).replace(' ', '0');
                            case NUM -> String.format("%3d", unsignedByte);
                        });

                        // ASCII representation
                        if (unsignedByte >= 32 && unsignedByte <= 126) {
                            asciiRep.append((char) unsignedByte);
                        } else {
                            asciiRep.append('.');
                        }

                    } catch (IllegalArgumentException ex) {
                        bytesRep.append(switch (viewMode) {
                            case HEX -> "--";
                            case BITS -> "--------";
                            case NUM -> "---";
                        });
                        asciiRep.append('?');
                    }
                    if (i < ROW_SIZE - 1) bytesRep.append(" ");
                }

                rows.add(new MemoryRow(
                        String.format("0x%016X", addr),
                        bytesRep.toString(),
                        asciiRep.toString(),
                        getMemoryRegionName(addr)
                ));
            }
            return rows;
        }
    }
}