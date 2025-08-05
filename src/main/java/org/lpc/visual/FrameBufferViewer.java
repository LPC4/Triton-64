package org.lpc.visual;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.lpc.cpu.Cpu;
import org.lpc.memory.Memory;
import org.lpc.memory.MemoryMap;
import org.lpc.visual.style.Colors;
import org.lpc.visual.style.Fonts;
import org.lpc.visual.style.Styles;

public class FrameBufferViewer {
    // 320x240 resolution with RGBA (4 bytes per pixel)
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int BYTES_PER_PIXEL = 4; // RGBA
    private static final int FB_SIZE = WIDTH * HEIGHT * BYTES_PER_PIXEL; // 320*240*4 = 307,200 bytes

    private final Cpu cpu;
    private final Memory memory;

    private long lastUpdate = 0;
    private WritableImage image = new WritableImage(WIDTH, HEIGHT);


    public FrameBufferViewer(Cpu cpu) {
        this.cpu = cpu;
        this.memory = cpu.getMemory();
    }

    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("🖥️ FrameBuffer Viewer (RGBA)");
        title.setFont(Fonts.TITLE);
        title.setTextFill(Color.web(Colors.TEXT));
        title.setAlignment(Pos.CENTER);
        BorderPane.setMargin(title, new Insets(15, 20, 10, 20));

        // Display at 2x size (640x480) for better visibility
        Canvas canvas = new Canvas(WIDTH * 2, HEIGHT * 2);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Label infoLabel = new Label(String.format(
                "Resolution: %d×%d (RGBA) | Address: 0x%016X - 0x%016X | Size: %.2f KB",
                WIDTH, HEIGHT,
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

        Scene scene = new Scene(root, WIDTH * 2 + 100, HEIGHT * 2 + 150);
        stage.setScene(scene);
        stage.setTitle("FrameBuffer Viewer - Debug Monitor");
        stage.setResizable(false);
        stage.show();

        startAutoRefresh(gc);
    }

    private void startAutoRefresh(GraphicsContext gc) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100_000_000) { // ~100ms -> ~10 FPS
                    renderFrameBuffer(gc);
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private void renderFrameBuffer(GraphicsContext gc) {
        try {
            byte[] bgra = new byte[FB_SIZE];
            for (int i = 0; i < FB_SIZE; i++) {
                bgra[i] = memory.readByte(MemoryMap.FB_BASE + i);
            }

            PixelWriter pw = image.getPixelWriter();
            pw.setPixels(
                    0, 0,
                    WIDTH, HEIGHT,
                    PixelFormat.getByteBgraInstance(),
                    bgra, 0, WIDTH * BYTES_PER_PIXEL
            );

            gc.clearRect(0, 0, WIDTH * 2, HEIGHT * 2);
            gc.drawImage(image, 0, 0, WIDTH * 2, HEIGHT * 2);

        } catch (Exception e) {
            gc.clearRect(0, 0, WIDTH * 2, HEIGHT * 2);
            gc.setFill(Color.RED);
            gc.fillText("FrameBuffer Access Error", 10, 20);
        }
    }


}