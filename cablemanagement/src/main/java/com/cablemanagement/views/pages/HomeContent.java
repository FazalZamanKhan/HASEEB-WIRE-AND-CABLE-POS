package com.cablemanagement.views.pages;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class HomeContent {
    public static Node get() {
        // Create main container that fills all available space
        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        // Company name label at the top with increased font size
        Label companyName = new Label("HASEEB WIRE AND CABLES");
        companyName.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 36)); // Changed from 600 to reasonable size
        companyName.setStyle("-fx-text-fill: #007bff; -fx-text-alignment: center; -fx-font-weight: 900;");
        
        // Position text at the top
        StackPane.setAlignment(companyName, Pos.TOP_CENTER);
        companyName.setTranslateY(30); // Add some top margin
        
        try {
            // Load and display the company logo to cover most of the available space
            Image logo = new Image(HomeContent.class.getResourceAsStream("/LOGO.jpg"));
            ImageView logoView = new ImageView(logo);
            
            // Make the logo much larger and fill most of the area
            logoView.setFitWidth(400);  // Reduced from 600 to reasonable size
            logoView.setFitHeight(300); // Reduced from 500 to reasonable size
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            
            // Center the logo but move it down to leave space for text at top
            StackPane.setAlignment(logoView, Pos.CENTER);
            logoView.setTranslateY(50); // Move down to account for text at top
            
            mainContainer.getChildren().addAll(logoView, companyName);
        } catch (Exception e) {
            // If logo can't be loaded, show a larger placeholder
            Label logoPlaceholder = new Label("📷");
            logoPlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 100)); // Reduced from 200 to reasonable size
            logoPlaceholder.setStyle("-fx-text-fill: #1a252f;");
            
            // Center the placeholder but move it down to leave space for text at top
            StackPane.setAlignment(logoPlaceholder, Pos.CENTER);
            logoPlaceholder.setTranslateY(50); // Move down to account for text at top
            
            mainContainer.getChildren().addAll(logoPlaceholder, companyName);
        }
        
        return mainContainer;
    }
}
