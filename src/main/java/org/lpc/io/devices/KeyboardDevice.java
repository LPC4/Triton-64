package org.lpc.io.devices;

import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.lpc.io.IODevice;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Keyboard device with a queue of key events, including repeats.
 *
 * Memory Map:
 * +0x00 CURRENT_CHAR    [RO] - ASCII code of any currently pressed key (0 if none)
 * +0x08 QUEUE_HEAD      [RO] - ASCII of first queued key (0xFF if empty)
 * +0x0C QUEUE_CONTROL   [WO] - Write 1 to pop QUEUE_HEAD
 */
public class KeyboardDevice implements IODevice {
    public static final int SIZE = 16;
    public static final int OFFSET_CURRENT_CHAR = 0;
    public static final int OFFSET_QUEUE_HEAD = 8;
    public static final int OFFSET_QUEUE_CONTROL = 12;
    public static final int MAX_QUEUE_SIZE = 64;

    private final long baseAddress;

    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Queue<Integer> keyQueue = new ArrayDeque<>();

    public KeyboardDevice(long baseAddress) {
        this.baseAddress = baseAddress;
    }

    public void setScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyRelease);
        scene.getRoot().setFocusTraversable(true);
    }

    private void handleKeyPress(KeyEvent event) {
        int c = mapKeyEventToChar(event);
        if (c == 0) return;

        // Only enqueue if under max queue size
        if (keyQueue.size() < MAX_QUEUE_SIZE) {
            keyQueue.add(c);
        }

        // Track pressed keys set
        pressedKeys.add(c);

        event.consume();
    }

    private void handleKeyRelease(KeyEvent event) {
        int c = mapKeyEventToChar(event);
        if (c == 0) return;

        pressedKeys.remove(c);
    }

    private int mapKeyEventToChar(KeyEvent event) {
        return switch (event.getCode()) {
            case ENTER -> '\n';
            case BACK_SPACE -> '\b';
            case TAB -> '\t';
            case SPACE -> ' ';
            case ESCAPE -> 27;
            default -> {
                String text = event.getText();
                yield (text != null && !text.isEmpty()) ? text.charAt(0) : 0;
            }
        };
    }

    @Override
    public long getAddress() {
        return baseAddress;
    }

    @Override
    public long getSize() {
        return SIZE;
    }

    @Override
    public String getName() {
        return "SimpleKeyboard";
    }

    @Override
    public boolean handleWrite(long relativeAddress, long value) {
        if (relativeAddress == OFFSET_QUEUE_CONTROL && value == 1) {
            if (!keyQueue.isEmpty()) keyQueue.poll();
            return true;
        }
        return false;
    }

    @Override
    public long handleRead(long relativeAddress) {
        if (relativeAddress == OFFSET_CURRENT_CHAR) {
            // Return any pressed key, or 0 if none pressed
            return pressedKeys.stream().findFirst().orElse(0) & 0xFF;
        }
        if (relativeAddress == OFFSET_QUEUE_HEAD) {
            return keyQueue.isEmpty() ? 0xFF : keyQueue.peek();
        }
        return 0;
    }
}
