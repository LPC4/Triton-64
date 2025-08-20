package org.lpc.memory.io.devices;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.lpc.memory.io.IODevice;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Keyboard device with a queue of key events, including repeats and proper shift handling.
 *
 * Memory Map:
 * +0x00 CURRENT_CHAR    [RO] - ASCII code of any currently pressed key (0 if none)
 * +0x01 QUEUE_HEAD      [RO] - ASCII of first queued key (0xFF if empty)
 * +0x02 QUEUE_CONTROL   [WO] - Write 1 to pop QUEUE_HEAD
 */
public class KeyboardDevice implements IODevice {
    public static final int SIZE = 8;
    public static final int OFFSET_CURRENT_CHAR = 0;
    public static final int OFFSET_QUEUE_HEAD = 1;
    public static final int OFFSET_QUEUE_CONTROL = 2;
    public static final int MAX_QUEUE_SIZE = 64;

    private final long baseAddress;

    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Queue<Integer> keyQueue = new ArrayDeque<>();
    private final Set<KeyCode> currentlyPressed = new HashSet<>();

    public KeyboardDevice(long baseAddress) {
        this.baseAddress = baseAddress;
    }

    public void setScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyRelease);
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
    }

    private void handleKeyPress(KeyEvent event) {
        currentlyPressed.add(event.getCode());

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
        currentlyPressed.remove(event.getCode());

        int c = mapKeyEventToChar(event);
        if (c == 0) return;

        pressedKeys.remove(c);
        event.consume();
    }

    private int mapKeyEventToChar(KeyEvent event) {
        // Handle special keys first
        switch (event.getCode()) {
            case ENTER:
                return '\n';
            case BACK_SPACE:
                return '\b';
            case TAB:
                return '\t';
            case SPACE:
                return ' ';
            case ESCAPE:
                return 27;
        }

        // Check if shift is pressed
        boolean shiftPressed = event.isShiftDown() ||
                currentlyPressed.contains(KeyCode.SHIFT) ||
                currentlyPressed.contains(KeyCode.SHORTCUT);

        // Handle number row with shift
        if (shiftPressed) {
            switch (event.getCode()) {
                case DIGIT1: return '!';
                case DIGIT2: return '@';
                case DIGIT3: return '#';
                case DIGIT4: return '$';
                case DIGIT5: return '%';
                case DIGIT6: return '^';
                case DIGIT7: return '&';
                case DIGIT8: return '*';
                case DIGIT9: return '(';
                case DIGIT0: return ')';
                case MINUS: return '_';
                case EQUALS: return '+';
                case OPEN_BRACKET: return '{';
                case CLOSE_BRACKET: return '}';
                case BACK_SLASH: return '|';
                case SEMICOLON: return ':';
                case QUOTE: return '"';
                case COMMA: return '<';
                case PERIOD: return '>';
                case SLASH: return '?';
                case BACK_QUOTE: return '~';
            }
        } else {
            // Handle unshifted special characters
            switch (event.getCode()) {
                case DIGIT1: return '1';
                case DIGIT2: return '2';
                case DIGIT3: return '3';
                case DIGIT4: return '4';
                case DIGIT5: return '5';
                case DIGIT6: return '6';
                case DIGIT7: return '7';
                case DIGIT8: return '8';
                case DIGIT9: return '9';
                case DIGIT0: return '0';
                case MINUS: return '-';
                case EQUALS: return '=';
                case OPEN_BRACKET: return '[';
                case CLOSE_BRACKET: return ']';
                case BACK_SLASH: return '\\';
                case SEMICOLON: return ';';
                case QUOTE: return '\'';
                case COMMA: return ',';
                case PERIOD: return '.';
                case SLASH: return '/';
                case BACK_QUOTE: return '`';
            }
        }

        // Handle letters
        if (event.getCode().isLetterKey()) {
            char baseChar = event.getCode().getChar().charAt(0);
            if (shiftPressed) {
                return Character.toUpperCase(baseChar);
            } else {
                return Character.toLowerCase(baseChar);
            }
        }

        // Fallback to getText() for any remaining cases
        String text = event.getText();
        if (text != null && !text.isEmpty()) {
            char c = text.charAt(0);
            // Filter out control characters except the ones we want
            if (c >= 32 && c <= 126) { // Printable ASCII range
                return c;
            }
        }

        return 0;
    }

    @Override
    public long getBaseAdress() {
        return baseAddress;
    }

    @Override
    public long getSize() {
        return SIZE;
    }

    @Override
    public String getName() {
        return "EnhancedKeyboard";
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
    public long handleRead(long relativeAddress, int size) {
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