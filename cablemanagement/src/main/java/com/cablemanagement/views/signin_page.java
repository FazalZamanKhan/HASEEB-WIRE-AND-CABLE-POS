package com.cablemanagement.views;

import com.cablemanagement.config;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class signin_page extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Haseeb Wires & Cables - Sign In");

        // Set up close confirmation for login window
        primaryStage.setOnCloseRequest(e -> {
            e.consume(); // Prevent the window from closing immediately
            showExitConfirmation(primaryStage);
        });

        // Create logo (try to load from multiple possible locations)
        ImageView logoView = null;
        try {
            String[] logoPaths = {
                "src/main/resources/LOGO.jpg",
                "cablemanagement/src/main/resources/LOGO.jpg",
                "cablemanagement/LOGO.jpg",
                "LOGO.jpg"
            };
            
            for (String logoPath : logoPaths) {
                try {
                    File logoFile = new File(logoPath);
                    if (logoFile.exists()) {
                        Image logo = new Image(logoFile.toURI().toString());
                        logoView = new ImageView(logo);
                        logoView.setFitWidth(100);
                        logoView.setFitHeight(100);
                        logoView.setPreserveRatio(true);
                        System.out.println("Logo loaded successfully from: " + logoPath);
                        break;
                    }
                } catch (Exception e) {
                    // Continue to next path if this one fails
                    continue;
                }
            }
            
            // Try loading from resources if file paths didn't work
            if (logoView == null) {
                try {
                    Image logo = new Image(getClass().getResourceAsStream("/LOGO.jpg"));
                    logoView = new ImageView(logo);
                    logoView.setFitWidth(100);
                    logoView.setFitHeight(100);
                    logoView.setPreserveRatio(true);
                    System.out.println("Logo loaded from resources");
                } catch (Exception e) {
                    System.out.println("Could not load logo from resources: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load logo: " + e.getMessage());
        }

        // Create title label
        Label titleLabel = new Label("HASEEB WIRES & CABLES");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");

        Label subtitleLabel = new Label("Please sign in to continue");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // Create UI components with better styling
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-weight: bold;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(250);
        usernameField.setStyle("-fx-padding: 8px;");

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(250);
        passwordField.setStyle("-fx-padding: 8px;");

        Button loginButton = new Button("Sign In");
        loginButton.setPrefWidth(250);
        loginButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        // Add hover effect
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px;"));

        // Allow Enter key to login
        passwordField.setOnAction(e -> loginButton.fire());

        // Action on login button
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Login Failed", "Please enter both username and password!");
                return;
            }

            if(config.database.isConnected())
            {
                if(config.database.SignIn(username, password)){
                    // Successfully logged in, switch to home page
                    primaryStage.setScene(home_page.getHomeScene());
                    primaryStage.setTitle("Haseeb Wires & Cables - Home");
                    
                    // Re-enable window resizing and maximization for the main application
                    primaryStage.setResizable(true);
                    primaryStage.setMaximized(false); // Start in normal window mode
                    
                    // Keep the close confirmation for the home page
                    primaryStage.setOnCloseRequest(closeEvent -> {
                        closeEvent.consume();
                        showExitConfirmation(primaryStage);
                    });
                }
                else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials!");
                    passwordField.clear(); // Clear password field on failed login
                }
            }
            else {
                showAlert(Alert.AlertType.ERROR, "Connection Error", "Unable to connect to database!");
            }
        });

        VBox layout = new VBox(15);
        
        // Add logo if it was successfully loaded
        if (logoView != null) {
            layout.getChildren().add(logoView);
        }
        
        layout.getChildren().addAll(
            titleLabel, 
            subtitleLabel,
            new VBox(5, userLabel, usernameField),
            new VBox(5, passLabel, passwordField),
            loginButton
        );
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 400, logoView != null ? 450 : 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Focus on username field initially
        usernameField.requestFocus();
    }
    
    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmation(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("The application will be closed.");
        
        // Add custom buttons
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Close database connection before exiting
            try {
                if (config.database != null && config.database.isConnected()) {
                    config.database.disconnect();
                }
            } catch (Exception ex) {
                System.err.println("Error closing database: " + ex.getMessage());
            }
            
            // Close the application
            Platform.exit();
            System.exit(0);
        }
        // If NO is selected, do nothing (window stays open)
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Allow launching from another class
    public static void main(String[] args) {
        launch(args);
    }
}
