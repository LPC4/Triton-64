package org.lpc.visual.style;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;

public class Styles {
    public static String cardStyle() {
        return "-fx-background-color: " + Colors.CARD + "; " +
                "-fx-border-color: " + Colors.BORDER + "; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px;";
    }

    public static String highlightRow() {
        return "-fx-background-color: #fff3cd; -fx-font-family: 'Consolas'; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;";
    }

    public static String faintHighlightRow() {
        return "-fx-background-color: #f8f9fa; -fx-font-family: 'Consolas'; " +
                "-fx-font-size: 11px;";
    }

    public static String monoRow() {
        return "-fx-font-family: 'Consolas'; -fx-font-size: 11px;";
    }

    public static Button button(String text) {
        Button btn = new Button(text);
        btn.setFont(Fonts.LABEL);
        setButtonStyle(btn, Colors.ACCENT);
        return btn;
    }

    public static ToggleButton toggleButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setFont(Fonts.LABEL);
        setButtonStyle(btn, Colors.ACCENT);
        return btn;
    }

    public static void styleCombo(ComboBox<?> combo) {
        combo.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: " + Colors.BORDER + "; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 4px; " +
                        "-fx-background-radius: 4px;"
        );
    }

    public static void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: " + Colors.BORDER + "; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 4px; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 6px;"
        );
        field.setFont(Fonts.MONO);
    }

    private static void setButtonStyle(ButtonBase btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4px; " +
                "-fx-background-radius: 4px; " +
                "-fx-padding: 4px 8px; " +
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #0069d9; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 4px; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 4px 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 4px; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 4px 8px; " +
                        "-fx-cursor: hand;"));
    }

    public static Label title(String text) {
        var label = new Label(text);
        label.setFont(Fonts.TITLE);
        label.setTextFill(Paint.valueOf(Colors.TEXT));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    public static Label sectionHeader(String text) {
        var label = new Label(text);
        label.setFont(Fonts.SECTION);
        label.setTextFill(Paint.valueOf(Colors.ACCENT));
        return label;
    }

    public static Label monoLabel(String text) {
        var label = new Label(text);
        label.setFont(Fonts.MONO);
        label.setTextFill(Paint.valueOf(Colors.TEXT));
        return label;
    }

    public static Label valueLabel() {
        var label = new Label();
        label.setFont(Fonts.MONO);
        label.setTextFill(Paint.valueOf(Colors.VALUE));
        label.setStyle(valueLabelStyle());
        return label;
    }

    public static Label flagLabel() {
        var label = new Label();
        label.setFont(Fonts.MONO);
        label.setTextFill(Paint.valueOf(Colors.VALUE));
        label.setStyle(flagDefaultStyle());
        return label;
    }

    public static String valueLabelStyle() {
        return "-fx-background-color: " + Colors.BACKGROUND + "; " +
                "-fx-padding: 6px 12px; " +
                "-fx-border-radius: 4px; " +
                "-fx-background-radius: 4px; " +
                "-fx-min-width: 100px;";
    }

    public static String flagDefaultStyle() {
        return "-fx-background-color: #e8f5e8; " +
                "-fx-padding: 6px 12px; " +
                "-fx-border-color: " + Colors.VALUE + "; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px;";
    }

    public static String flagActiveStyle() {
        return "-fx-background-color: #fff3cd; " +
                "-fx-padding: 6px 12px; " +
                "-fx-border-color: #ffc107; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px;";
    }

}

