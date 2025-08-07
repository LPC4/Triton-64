package org.lpc.visual;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.lpc.cpu.Cpu;
import org.lpc.memory.Memory;
import org.lpc.memory.MemoryMap;
import org.lpc.visual.style.Colors;
import org.lpc.visual.style.Fonts;

public class TextModeViewer {
    private static final int COLS = 80;
    private static final int ROWS = 30;
    private static final int CELL_SIZE = 4; // char + fg + bg + 1 unused
    private static final int CHAR_WIDTH = 10;  // larger monospace character width
    private static final int CHAR_HEIGHT = 20; // larger monospace character height
    private static final int FB_SIZE = COLS * ROWS * CELL_SIZE;

    private final Memory memory;
    private long lastUpdate = 0;
    private final Font textFont;

    public TextModeViewer(Cpu cpu) {
        this.memory = cpu.getMemory();
        this.textFont = Font.font("Courier New", FontWeight.NORMAL, 14);
    }

    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("📝 Text FrameBuffer Viewer (80×30)");
        title.setFont(Fonts.TITLE);
        title.setTextFill(Color.web(Colors.TEXT));
        title.setAlignment(Pos.CENTER);
        BorderPane.setMargin(title, new Insets(15, 20, 10, 20));

        // Canvas sized for text display
        Canvas canvas = new Canvas(COLS * CHAR_WIDTH, ROWS * CHAR_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Set text rendering properties
        gc.setFont(textFont);

        Label infoLabel = new Label(String.format(
                "Grid: %d×%d (4-byte cells) | Address: 0x%016X - 0x%016X | Size: %.2f KB",
                COLS, ROWS,
                MemoryMap.FB_BASE,
                MemoryMap.FB_BASE + FB_SIZE - 1,
                FB_SIZE / 1024.0
        ));
        infoLabel.setFont(Fonts.LABEL);
        infoLabel.setTextFill(Color.web(Colors.MUTED));

        VBox container = new VBox(10, title, canvas, infoLabel);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(10));

        root.setCenter(container);

        Scene scene = new Scene(root, COLS * CHAR_WIDTH + 100, ROWS * CHAR_HEIGHT + 150);
        stage.setScene(scene);
        stage.setTitle("Text FrameBuffer Viewer - Debug Monitor");
        stage.setResizable(false);
        stage.show();

        startAutoRefresh(gc);
    }

    private void startAutoRefresh(GraphicsContext gc) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100_000_000) { // ~100ms -> ~10 FPS (matches PixelModeViewer)
                    renderTextBuffer(gc);
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private void renderTextBuffer(GraphicsContext gc) {
        try {
            byte[] buffer = new byte[FB_SIZE];
            memory.readBytes(MemoryMap.FB_BASE, buffer, 0, FB_SIZE);

            // Clear canvas with black background
            gc.clearRect(0, 0, COLS * CHAR_WIDTH, ROWS * CHAR_HEIGHT);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, COLS * CHAR_WIDTH, ROWS * CHAR_HEIGHT);

            // Render each character cell
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int cellIndex = (row * COLS + col) * CELL_SIZE;

                    // Extract cell data
                    int charCode = buffer[cellIndex] & 0xFF;
                    int fgColor = buffer[cellIndex + 1] & 0xFF;
                    int bgColor = buffer[cellIndex + 2] & 0xFF;
                    // buffer[cellIndex + 3] is unused

                    // Calculate screen position
                    double x = col * CHAR_WIDTH;
                    double y = row * CHAR_HEIGHT;

                    // Draw background
                    gc.setFill(getColor(bgColor));
                    gc.fillRect(x, y, CHAR_WIDTH, CHAR_HEIGHT);

                    // Draw character if it's printable
                    if (charCode >= 32 && charCode <= 126) { // Printable ASCII range
                        gc.setFill(getColor(fgColor));

                        // Position text within cell (baseline adjustment for proper alignment)
                        double textX = x + 2;
                        double textY = y + CHAR_HEIGHT - 4;

                        gc.fillText(String.valueOf((char) charCode), textX, textY);
                    }
                }
            }

        } catch (Exception e) {
            // Error handling - display error message
            gc.clearRect(0, 0, COLS * CHAR_WIDTH, ROWS * CHAR_HEIGHT);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, COLS * CHAR_WIDTH, ROWS * CHAR_HEIGHT);
            gc.setFill(Color.RED);
            gc.fillText("FrameBuffer Access Error: " + e.getMessage(), 10, 20);
        }
    }

    /**
     * Convert 8-bit color code to JavaFX Color using standard VGA palette
     */
    private Color getColor(int colorCode) {
        return switch (colorCode & 0xF) {
            case 0x0 -> Color.BLACK;
            case 0x1 -> Color.web("#0000AA"); // Dark Blue
            case 0x2 -> Color.web("#00AA00"); // Dark Green
            case 0x3 -> Color.web("#00AAAA"); // Dark Cyan
            case 0x4 -> Color.web("#AA0000"); // Dark Red
            case 0x5 -> Color.web("#AA00AA"); // Dark Magenta
            case 0x6 -> Color.web("#AA5500"); // Brown
            case 0x7 -> Color.web("#AAAAAA"); // Light Gray
            case 0x8 -> Color.web("#555555"); // Dark Gray
            case 0x9 -> Color.web("#5555FF"); // Bright Blue
            case 0xA -> Color.web("#55FF55"); // Bright Green
            case 0xB -> Color.web("#55FFFF"); // Bright Cyan
            case 0xC -> Color.web("#FF5555"); // Bright Red
            case 0xD -> Color.web("#FF55FF"); // Bright Magenta
            case 0xE -> Color.web("#FFFF55"); // Yellow
            case 0xF -> Color.WHITE;
            default -> Color.BLACK;
        };
    }
}