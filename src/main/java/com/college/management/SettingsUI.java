package com.college.management;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SettingsUI {
    private int userId;
    private UserSettings settings;
    private Runnable onSaveCallback;

    public SettingsUI(int userId, Runnable onSaveCallback) {
        this.userId = userId;
        this.settings = UserSettings.getSettings(userId);
        this.onSaveCallback = onSaveCallback;
    }

    public Pane getView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));

        Label title = new Label("User Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Display Name
        HBox nameBox = new HBox(10);
        Label nameLbl = new Label("Display Name:");
        TextField txtName = new TextField(settings.getDisplayName() == null ? "" : settings.getDisplayName());
        nameBox.getChildren().addAll(nameLbl, txtName);

        // Theme Toggle
        HBox themeBox = new HBox(10);
        Label themeLbl = new Label("Theme:");
        ToggleButton themeBtn = new ToggleButton(settings.getTheme().equals("DARK") ? "Dark Mode" : "Light Mode");
        themeBtn.setSelected(settings.getTheme().equals("DARK"));
        themeBtn.setOnAction(e -> {
            themeBtn.setText(themeBtn.isSelected() ? "Dark Mode" : "Light Mode");
        });
        themeBox.getChildren().addAll(themeLbl, themeBtn);

        // Bubble Color
        HBox colorBox = new HBox(10);
        Label colorLbl = new Label("My Chat Bubble Color:");
        ColorPicker colorPicker = new ColorPicker(Color.web(settings.getBubbleColor()));
        colorBox.getChildren().addAll(colorLbl, colorPicker);

        Button btnSave = new Button("Save Settings");
        btnSave.setStyle("-fx-background-color: #128c7e; -fx-text-fill: white; -fx-font-weight: bold;");

        btnSave.setOnAction(e -> {
            settings.setDisplayName(txtName.getText().isEmpty() ? null : txtName.getText());
            settings.setTheme(themeBtn.isSelected() ? "DARK" : "LIGHT");
            settings.setBubbleColor(toHexString(colorPicker.getValue()));
            settings.save();
            if (onSaveCallback != null) onSaveCallback.run();
            new Alert(Alert.AlertType.INFORMATION, "Settings Saved!").show();
        });

        layout.getChildren().addAll(title, nameBox, themeBox, colorBox, btnSave);
        return layout;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
