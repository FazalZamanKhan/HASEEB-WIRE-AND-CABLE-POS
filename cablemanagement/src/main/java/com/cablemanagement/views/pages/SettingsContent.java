package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Optional;

public class SettingsContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(new Label("⚙️ Select a settings option"));

        VBox buttonColumn = new VBox(10);
        buttonColumn.setPadding(new Insets(10));
        buttonColumn.setAlignment(Pos.TOP_LEFT);

        // Responsive resizing
        buttonColumn.setPrefWidth(250);
        ScrollPane buttonScroll = new ScrollPane(buttonColumn);
        buttonScroll.setFitToWidth(true);
        buttonScroll.setPrefWidth(260);

        addButton(buttonColumn, "Change Password", () -> formArea.getChildren().setAll(createChangePasswordForm()));
        addButton(buttonColumn, "Logout", () -> formArea.getChildren().setAll(createLogoutPrompt()));
        addButton(buttonColumn, "Signup", () -> formArea.getChildren().setAll(createSignupForm()));
        addButton(buttonColumn, "Assign Rights", () -> formArea.getChildren().setAll(createAssignRightsForm()));

        mainLayout.setLeft(buttonScroll);
        mainLayout.setCenter(formArea);

        return mainLayout;
    }

    private static void addButton(VBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static VBox createChangePasswordForm() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Change Password");
        heading.setStyle("-fx-font-size: 18px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Old Password");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");

        Button submit = new Button("Update Password");

        submit.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String oldPassword = oldPass.getText();
            String newPassword = newPass.getText();
            String confirmPassword = confirmPass.getText();

            if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Error", "New passwords do not match!");
                return;
            }

            if (config.database != null && config.database.isConnected()) {
                if (config.database.changePassword(username, oldPassword, newPassword)) {
                    showAlert("Success", "Password changed successfully!");
                    usernameField.clear();
                    oldPass.clear();
                    newPass.clear();
                    confirmPass.clear();
                } else {
                    showAlert("Error", "Failed to change password. Please check your current password.");
                }
            } else {
                showAlert("Error", "Database not connected!");
            }
        });

        box.getChildren().addAll(heading, usernameField, oldPass, newPass, confirmPass, submit);
        return box;
    }

    private static VBox createLogoutPrompt() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Are you sure you want to logout?");
        Button logout = new Button("Confirm Logout");

        logout.setOnAction(e -> {
            // Show confirmation dialog before logout
            Alert logoutConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
            logoutConfirmation.setTitle("Logout Confirmation");
            logoutConfirmation.setHeaderText("Are you sure you want to logout?");
            logoutConfirmation.setContentText("You will be returned to the login screen.");
            
            logoutConfirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            Optional<ButtonType> result = logoutConfirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                // Clear current user session
                com.cablemanagement.config.clearCurrentUser();
                
                // Get the current stage and close it
                Stage currentStage = (Stage) logout.getScene().getWindow();
                currentStage.close();
                
                // Create a new stage for the login page
                Stage loginStage = new Stage();
                com.cablemanagement.views.signin_page loginPage = new com.cablemanagement.views.signin_page();
                try {
                    loginPage.start(loginStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Fallback: show error message and exit if login page fails to load
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to return to login page");
                    errorAlert.setContentText("The application will now exit.");
                    errorAlert.showAndWait();
                    Platform.exit();
                }
                
                System.out.println("User logged out successfully - returned to login page");
            }
            // If NO is selected, do nothing (stay logged in)
        });

        box.getChildren().addAll(label, logout);
        return box;
    }

    private static VBox createSignupForm() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Signup New User");
        heading.setStyle("-fx-font-size: 18px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.setPromptText("Select Role");
        roleCombo.getItems().addAll("user", "admin");
        roleCombo.setValue("user"); // Default role

        Button signup = new Button("Create Account");

        signup.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText();
            String confirmPass = confirmPassword.getText();
            String userRole = roleCombo.getValue();

            if (user.isEmpty() || pass.isEmpty() || userRole.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }

            if (!pass.equals(confirmPass)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }

            if (config.database != null && config.database.isConnected()) {
                if (config.database.userExists(user)) {
                    showAlert("Error", "Username already exists!");
                } else if (config.database.insertUser(user, pass, userRole)) {
                    showAlert("Success", "User created successfully!");
                    username.clear();
                    password.clear();
                    confirmPassword.clear();
                    roleCombo.setValue("user");
                } else {
                    showAlert("Error", "Failed to create user!");
                }
            } else {
                showAlert("Error", "Database not connected!");
            }
        });

        box.getChildren().addAll(heading, username, password, confirmPassword, roleCombo, signup);
        return box;
    }

    private static Node createAssignRightsForm() {
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(30));
        mainBox.setAlignment(Pos.TOP_LEFT);

        // Main heading
        Label mainHeading = new Label("Assign Rights");
        mainHeading.setStyle("-fx-font-size: 20px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        // Rights assignment box
        VBox rightsBox = new VBox(15);
        rightsBox.setPadding(new Insets(20));
        rightsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-color: #f9f9f9; -fx-background-radius: 8;");

        Label rightsHeading = new Label("Assign Rights");
        rightsHeading.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-font-weight: bold;");

        // Username field for target user
        TextField targetUsername = new TextField();
        targetUsername.setPromptText("Target Username (user to assign rights to)");
        
        // Admin credentials for confirmation
        TextField adminUsername = new TextField();
        adminUsername.setPromptText("Admin Username (for confirmation)");

        PasswordField adminPassword = new PasswordField();
        adminPassword.setPromptText("Admin Password (for confirmation)");

        // Available Rights section
        Label availableRightsLabel = new Label("Available Rights (Click to add):");
        availableRightsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");
        
        // Create buttons for each available right
        VBox availableRightsBox = new VBox(8);
        availableRightsBox.setMaxHeight(200);
        availableRightsBox.setPrefHeight(200);
        
        // Wrap available rights in scroll pane
        ScrollPane availableRightsScrollPane = new ScrollPane(availableRightsBox);
        availableRightsScrollPane.setFitToWidth(true);
        availableRightsScrollPane.setMaxHeight(200);
        availableRightsScrollPane.setPrefHeight(200);
        availableRightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        availableRightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
            String[] mainRights = {
                "Home", "Accounts", "Register", "Raw Stock", "Production", "Books", "Bank Mgmt", "Salesman", "Employees", "Reports", "Settings"
            };
            String[] accountSubRights = {
                "Update Customer", "View Customer Ledger", "Add Customer Payment", "Delete Customer",
                "Update Supplier", "View Supplier Ledger", "Add Supplier Payment", "Delete Supplier"
            };
        
        // Selected Rights section
        Label selectedRightsLabel = new Label("Selected Rights (Click to remove):");
        selectedRightsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");
        
        VBox selectedRightsBox = new VBox(5);
        selectedRightsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: #f9f9f9;");
        selectedRightsBox.setPrefHeight(120);
        selectedRightsBox.setMaxHeight(120);
        selectedRightsBox.setMaxWidth(Double.MAX_VALUE);
        
        // Wrap selected rights in a scroll pane for overflow protection
        ScrollPane selectedRightsScrollPane = new ScrollPane(selectedRightsBox);
        selectedRightsScrollPane.setFitToWidth(true);
        selectedRightsScrollPane.setMaxHeight(120);
        selectedRightsScrollPane.setPrefHeight(120);
        selectedRightsScrollPane.setStyle("-fx-background: #f9f9f9; -fx-background-color: #f9f9f9;");
        selectedRightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        selectedRightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        Label noRightsLabel = new Label("No rights selected");
        noRightsLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
        selectedRightsBox.getChildren().add(noRightsLabel);

        // Add listener to target username field to load existing rights
        targetUsername.textProperty().addListener((observable, oldValue, newValue) -> {
            String username = newValue.trim();
            if (!username.isEmpty() && config.database != null && config.database.isConnected()) {
                // Check if user exists and load their existing rights
                if (config.database.userExists(username)) {
                    loadExistingRights(username, selectedRightsBox, noRightsLabel);
                } else {
                    // Clear rights if user doesn't exist
                    selectedRightsBox.getChildren().clear();
                    selectedRightsBox.getChildren().add(noRightsLabel);
                }
            } else {
                // Clear rights if username is empty
                selectedRightsBox.getChildren().clear();
                selectedRightsBox.getChildren().add(noRightsLabel);
            }
        });
        
    // Create available rights buttons
    for (String right : mainRights) {
            Button rightButton = new Button(right);
            rightButton.setMaxWidth(Double.MAX_VALUE);
            rightButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
            
            // Add hover effects
            rightButton.setOnMouseEntered(e -> {
                rightButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-color: #0056b3; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,123,255,0.3), 4, 0, 0, 2);");
            });
            
            rightButton.setOnMouseExited(e -> {
                rightButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
            });
            
            // Add click effect
            rightButton.setOnMousePressed(e -> {
                rightButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-border-color: #004085; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-scale-x: 0.98; -fx-scale-y: 0.98;");
            });
            
            rightButton.setOnMouseReleased(e -> {
                rightButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-color: #0056b3; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,123,255,0.3), 4, 0, 0, 2);");
            });
            
            // Store the right name for the lambda
            final String rightName = right;
            
            rightButton.setOnAction(event -> {
                System.out.println("Clicked right: " + rightName); // Debug line
                
                // Add a brief visual feedback for successful click
                rightButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-color: #1e7e34; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(40,167,69,0.4), 6, 0, 0, 2);");
                
                // Reset style after a brief moment
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), e -> {
                        rightButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                    })
                );
                timeline.play();
                
                // Check if this right is already selected
                boolean alreadySelected = selectedRightsBox.getChildren().stream()
                    .filter(node -> node instanceof Button)
                    .map(node -> (Button) node)
                    .anyMatch(btn -> btn.getText().replace(" ✕", "").equals(rightName));
                
                if (!alreadySelected) {
                    // Remove "No rights selected" label if present
                    selectedRightsBox.getChildren().removeIf(node -> node == noRightsLabel);
                    
                    // Create selected right button (with remove functionality)
                    Button selectedButton = new Button(rightName + " ✕");
                    selectedButton.setMaxWidth(Double.MAX_VALUE);
                    selectedButton.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                    
                    // Add hover effects for selected buttons
                    selectedButton.setOnMouseEntered(e -> {
                        selectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #c82333; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(220,53,69,0.3), 4, 0, 0, 2);");
                    });
                    
                    selectedButton.setOnMouseExited(e -> {
                        selectedButton.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                    });
                    
                    selectedButton.setOnMousePressed(e -> {
                        selectedButton.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; -fx-border-color: #bd2130; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-scale-x: 0.98; -fx-scale-y: 0.98;");
                    });
                    
                    selectedButton.setOnMouseReleased(e -> {
                        selectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #c82333; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(220,53,69,0.3), 4, 0, 0, 2);");
                    });
                    
                    selectedButton.setOnAction(removeEvent -> {
                        System.out.println("Removing right: " + rightName); // Debug line
                        selectedRightsBox.getChildren().remove(selectedButton);
                        // If no rights left, show "No rights selected"
                        if (selectedRightsBox.getChildren().stream().noneMatch(node -> node instanceof Button)) {
                            selectedRightsBox.getChildren().add(noRightsLabel);
                        }
                    });
                    
                    selectedRightsBox.getChildren().add(selectedButton);
                    System.out.println("Added right: " + rightName); // Debug line
                } else {
                    System.out.println("Right already selected: " + rightName); // Debug line
                }
            });
            
            availableRightsBox.getChildren().add(rightButton);
        }

        Button assignRights = new Button("Add Selected Rights");
        assignRights.getStyleClass().add("register-button");
        assignRights.setMaxWidth(Double.MAX_VALUE);
        assignRights.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 10; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Add some spacing before the button
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(15);

        assignRights.setOnAction(e -> {
            String targetUser = targetUsername.getText().trim();
            String adminUser = adminUsername.getText().trim();
            String adminPass = adminPassword.getText();
            
            // Get newly selected rights from the selected rights box (only green buttons, not red existing ones)
            java.util.List<String> newlySelectedRights = selectedRightsBox.getChildren().stream()
                .filter(node -> node instanceof Button && !node.equals(noRightsLabel))
                .map(node -> (Button) node)
                .filter(btn -> btn.getStyle().contains("#d4edda")) // Only green buttons (newly selected)
                .map(btn -> btn.getText().replace(" ✕", "")) // Remove the X symbol
                .collect(java.util.stream.Collectors.toList());
                
            System.out.println("DEBUG: Newly selected rights to add: " + newlySelectedRights); // Debug line

            if (targetUser.isEmpty() || adminUser.isEmpty() || adminPass.isEmpty() || newlySelectedRights.isEmpty()) {
                showAlert("Error", "All fields are required! Please select at least one new right to add.");
                return;
            }

            if (config.database != null && config.database.isConnected()) {
                // First verify admin credentials
                if (config.database.SignIn(adminUser, adminPass)) {
                    // Create available rights buttons
                    for (String right : mainRights) {
                        Button rightButton = new Button(right);
                        rightButton.setMaxWidth(Double.MAX_VALUE);
                        rightButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                        final String rightName = right;
                        if ("Accounts".equals(rightName)) {
                            rightButton.setOnAction(event -> {
                                Stage subStage = new Stage();
                                subStage.initModality(Modality.APPLICATION_MODAL);
                                subStage.setTitle("Accounts Rights");
                                VBox subBox = new VBox(10);
                                subBox.setPadding(new Insets(20));
                                subBox.setPrefWidth(350);
                                subBox.setPrefHeight(400);
                                subBox.getChildren().add(new Label("Select specific rights for Accounts:"));
                                for (String subRight : accountSubRights) {
                                    Button subBtn = new Button(subRight);
                                    subBtn.setMaxWidth(Double.MAX_VALUE);
                                    subBtn.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                                    subBtn.setOnAction(subEvent -> {
                                        boolean alreadySelected = selectedRightsBox.getChildren().stream()
                                            .filter(node -> node instanceof Button)
                                            .map(node -> (Button) node)
                                            .anyMatch(btn -> btn.getText().replace(" ✕", "").equals(subRight));
                                        if (!alreadySelected) {
                                            selectedRightsBox.getChildren().removeIf(node -> node == noRightsLabel);
                                            Button selectedButton = new Button(subRight + " ✕");
                                            selectedButton.setMaxWidth(Double.MAX_VALUE);
                                            selectedButton.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                                            selectedButton.setOnAction(removeEvent -> {
                                                selectedRightsBox.getChildren().remove(selectedButton);
                                                if (selectedRightsBox.getChildren().stream().noneMatch(node -> node instanceof Button)) {
                                                    selectedRightsBox.getChildren().add(noRightsLabel);
                                                }
                                            });
                                            selectedRightsBox.getChildren().add(selectedButton);
                                        }
                                    });
                                    subBox.getChildren().add(subBtn);
                                }
                                Scene subScene = new Scene(subBox, 350, 400);
                                subStage.setScene(subScene);
                                subStage.show();
                            });
                        } else {
                            rightButton.setOnAction(event -> {
                                boolean alreadySelected = selectedRightsBox.getChildren().stream()
                                    .filter(node -> node instanceof Button)
                                    .map(node -> (Button) node)
                                    .anyMatch(btn -> btn.getText().replace(" ✕", "").equals(rightName));
                                if (!alreadySelected) {
                                    selectedRightsBox.getChildren().removeIf(node -> node == noRightsLabel);
                                    Button selectedButton = new Button(rightName + " ✕");
                                    selectedButton.setMaxWidth(Double.MAX_VALUE);
                                    selectedButton.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                                    selectedButton.setOnAction(removeEvent -> {
                                        selectedRightsBox.getChildren().remove(selectedButton);
                                        if (selectedRightsBox.getChildren().stream().noneMatch(node -> node instanceof Button)) {
                                            selectedRightsBox.getChildren().add(noRightsLabel);
                                        }
                                    });
                                    selectedRightsBox.getChildren().add(selectedButton);
                                }
                            });
                        }
                        availableRightsBox.getChildren().add(rightButton);
                    }
                    });
                    
                    selectedButton.setOnMouseExited(e -> {
                        selectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #c82333; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(220,53,69,0.3), 4, 0, 0, 2);");
                    });
                    
                    selectedButton.setOnMousePressed(e -> {
                        selectedButton.setStyle("-fx-background-color: #bd2130; -fx-text-fill: white; -fx-border-color: #a71e2a; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-scale-x: 0.98; -fx-scale-y: 0.98;");
                    });
                    
                    selectedButton.setOnMouseReleased(e -> {
                        selectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #c82333; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(220,53,69,0.3), 4, 0, 0, 2);");
                    });
                    
                    // Add click handler to remove existing rights
                    selectedButton.setOnAction(removeEvent -> {
                        System.out.println("Removing existing right: " + right);
                        // Remove this right from the database immediately
                        java.util.List<String> rightsToRemove = java.util.Arrays.asList(right);
                        if (config.database.removeUserRights(username, rightsToRemove)) {
                            selectedRightsBox.getChildren().remove(selectedButton);
                            // If no rights left, show "No rights selected"
                            if (selectedRightsBox.getChildren().stream().noneMatch(node -> node instanceof Button)) {
                                selectedRightsBox.getChildren().add(noRightsLabel);
                            }
                            System.out.println("Successfully removed right: " + right);
                        } else {
                            System.err.println("Failed to remove right: " + right);
                        }
                    });
                    
                    selectedRightsBox.getChildren().add(selectedButton);
                }
                System.out.println("Loaded " + existingRights.size() + " existing rights for user: " + username);
            }
        } catch (Exception e) {
            System.err.println("Error loading existing rights for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            // On error, show no rights
            selectedRightsBox.getChildren().clear();
            selectedRightsBox.getChildren().add(noRightsLabel);
        }
    }
}
