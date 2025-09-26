package com.cablemanagement.views.pages;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class ProfileContent {
    public static Node get() {
        Label label = new Label("👤 Profile Page");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #007bff; -fx-font-weight: bold;");
        return new StackPane(label);
    }
}
