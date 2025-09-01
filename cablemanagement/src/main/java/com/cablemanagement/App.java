package com.cablemanagement;

import com.cablemanagement.views.signin_page;
import com.cablemanagement.views.home_page;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Optional;

public class App extends Application {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        // initialize database
        config.database.connect(null, null, null);

        // Launch JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set up close confirmation for the primary stage
        primaryStage.setOnCloseRequest(e -> {
            e.consume(); // Prevent the window from closing immediately
            showExitConfirmation(primaryStage);
        });
        
        // Start with login page
        signin_page loginPage = new signin_page();
        loginPage.start(primaryStage);
        
        // Skip signin and go directly to home page for testing (COMMENTED OUT)
        // primaryStage.setTitle("Haseeb Wires & Cables - Home");
        // primaryStage.setScene(home_page.getHomeScene());
        // primaryStage.setResizable(true);
        // primaryStage.show();
    }
    
    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmation(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");
        
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
}