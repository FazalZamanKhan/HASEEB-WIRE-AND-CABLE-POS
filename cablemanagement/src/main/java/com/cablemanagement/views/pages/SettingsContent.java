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
    // Helper to check if current user has a specific Settings subright
    private static boolean hasCurrentUserSubright(String subright) {
   
        String currentUser = com.cablemanagement.config.getCurrentUsername();
        if (currentUser == null || currentUser.isEmpty()) return false;
        // Admins always have access
        if (com.cablemanagement.config.isCurrentUserAdmin()) return true;
        java.util.List<String> rights = config.database.getUserRights(currentUser);
        return rights != null && rights.contains(subright);
    }

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

        addButton(buttonColumn, "Change Password", () -> {
            if (hasCurrentUserSubright("Change Password")) {
                formArea.getChildren().setAll(createChangePasswordForm());
            } else {
                showAlert("Access Denied", "You do not have permission to access Change Password.");
            }
        });
        addButton(buttonColumn, "Logout", () -> {
            if (hasCurrentUserSubright("Logout")) {
                formArea.getChildren().setAll(createLogoutPrompt());
            } else {
                showAlert("Access Denied", "You do not have permission to access Logout.");
            }
        });
        addButton(buttonColumn, "Signup", () -> {
            if (hasCurrentUserSubright("Signup")) {
                formArea.getChildren().setAll(createSignupForm());
            } else {
                showAlert("Access Denied", "You do not have permission to access Signup.");
            }
        });
        addButton(buttonColumn, "Assign Rights", () -> {
            if (hasCurrentUserSubright("Assign Rights")) {
                formArea.getChildren().setAll(createAssignRightsForm());
            } else {
                showAlert("Access Denied", "You do not have permission to access Assign Rights.");
            }
        });

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
                // Reports subrights (from ReportsContent.java)
                String[] reportsSubRights = {
                    "Purchase Report",
                    "Sales Report",
                    "Return Purchase Report",
                    "Return Sales Report",
                    "Bank Transfer Report",
                    "Profit Report",
                    "Balance Sheet",
                    "Area-Wise Report",
                    "Brand Sales Report"
                };

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
                        // ...existing code...
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
        // Settings sub-rights (from attached image)
        String[] settingsSubRights = {
            "Change Password",
            "Logout",
            "Signup",
            "Assign Rights"
        };
        // Bank Mgmt sub-rights (from attached image)
        String[] bankMgmtSubRights = {
            "Manage Banks",
            "Bank Transactions",
            "View Cash In Hand",
            "View Cash Ledger"
        };
        // Production sub-rights
        String[] productionSubRights = {
            "Register Production Stock",
            "Create Production Invoice",
            "Create Return Production Invoice",
            "Create Sales Invoice",
            "Create Return Sales Invoice",
            "View Production Stock Usage Report"
        };
        // Register sub-rights
        String[] registerSubRights = {
            "Register Category", "Register Manufacturer", "Register Brand", "Register Province",
            "Register District", "Register Tehsil", "Register Units", "Register Customer", "Register Supplier"
        };
        // Raw Stock sub-rights
        String[] rawStockSubRights = {
            "Raw Stock - Register",
            "Raw Stock - Purchase Invoice",
            "Raw Stock - Return Purchase Invoice",
            "Raw Stock - Use Invoice",
            "Raw Stock - Usage Report"
        };
        // Books sub-rights (updated to match latest attached image)
        String[] booksSubRights = {
            "Purchase Book",
            "Return Purchase Book",
            "Raw Stock Usage Book",
            "Production Book",
            "Return Production Book",
            "Sales Book",
            "Return Sales Book"
        };
        // Employees sub-rights (from attached images)
        String[] employeesSubRights = {
            "Manage Designations",
            "Register New Employee",
            "Register Contract Employee",
            "Contract-Based Employee",
            "Manage All Employees",
            "View Salary Reports",
            "Mark Employee Attendance",
            "View Attendance Report",
            "Grant Advance Salary",
            "Register New Loan",
            "View Employee Loan Report"
        };
        // Reports sub-rights (from ReportsContent.java)
        String[] reportsSubRights = {
            "Purchase Report",
            "Sales Report",
            "Return Purchase Report",
            "Return Sales Report",
            "Bank Transfer Report",
            "Profit Report",
            "Balance Sheet",
            "Area-Wise Report",
            "Brand Sales Report"
        };
    VBox mainBox = new VBox(20);
    mainBox.setPadding(new Insets(30));
    mainBox.setAlignment(Pos.TOP_LEFT);
    mainBox.setPrefWidth(1600);

        Label mainHeading = new Label("Assign Rights");
        mainHeading.setStyle("-fx-font-size: 20px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        // Username and admin fields
        TextField targetUsername = new TextField();
        targetUsername.setPromptText("Target Username (user to assign rights to)");
        TextField adminUsername = new TextField();
        adminUsername.setPromptText("Admin Username (for confirmation)");
        PasswordField adminPassword = new PasswordField();
        adminPassword.setPromptText("Admin Password (for confirmation)");

        // Rights arrays
        String[] mainRights = {
            "Home", "Accounts", "Register", "Raw Stock", "Production", "Books", "Bank Mgmt", "Salesman", "Employees", "Reports", "Settings"
        };
        String[] accountSubRights = {
            "Update Customer", "View Customer Ledger", "Add Customer Payment", "Delete Customer",
            "Update Supplier", "View Supplier Ledger", "Add Supplier Payment", "Delete Supplier"
        };

        // Available Rights (left)
        Label availableRightsLabel = new Label("Available Rights (Click to add):");
        availableRightsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");
        VBox availableRightsBox = new VBox(8);
    availableRightsBox.setMaxWidth(500);
    availableRightsBox.setPrefWidth(500);
        ScrollPane availableRightsScrollPane = new ScrollPane(availableRightsBox);
    availableRightsScrollPane.setFitToWidth(true);
    availableRightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    availableRightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Specific Rights (right, hidden until Accounts is clicked)
    Label specificRightsLabel = new Label("Specific Rights (Click to add/remove):");
    specificRightsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");
    VBox specificRightsBox = new VBox(8);
    specificRightsBox.setMaxWidth(500);
    specificRightsBox.setPrefWidth(500);
    specificRightsBox.setVisible(false);
    ScrollPane specificRightsScrollPane = new ScrollPane(specificRightsBox);
    specificRightsScrollPane.setFitToWidth(true);
    specificRightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    specificRightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    specificRightsScrollPane.setVisible(false);

        // Selected Rights section
        Label selectedRightsLabel = new Label("Selected Rights (Click to remove):");
        selectedRightsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");
        VBox selectedRightsBox = new VBox(5);
    selectedRightsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: #f9f9f9;");
    selectedRightsBox.setPrefHeight(900);
    selectedRightsBox.setMaxWidth(Double.MAX_VALUE);
        ScrollPane selectedRightsScrollPane = new ScrollPane(selectedRightsBox);
    selectedRightsScrollPane.setFitToWidth(true);
    selectedRightsScrollPane.setStyle("-fx-background: #f9f9f9; -fx-background-color: #f9f9f9;");
    selectedRightsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    selectedRightsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Label noRightsLabel = new Label("No rights selected");
        noRightsLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
        selectedRightsBox.getChildren().add(noRightsLabel);

        // Add listener to target username field to load existing rights
        targetUsername.textProperty().addListener((observable, oldValue, newValue) -> {
            String username = newValue.trim();
            if (!username.isEmpty() && config.database != null && config.database.isConnected()) {
                if (config.database.userExists(username)) {
                    loadExistingRights(username, selectedRightsBox, noRightsLabel);
                } else {
                    selectedRightsBox.getChildren().clear();
                    selectedRightsBox.getChildren().add(noRightsLabel);
                }
            } else {
                selectedRightsBox.getChildren().clear();
                selectedRightsBox.getChildren().add(noRightsLabel);
            }
        });

        // Create available rights buttons
        for (String right : mainRights) {
            Button rightButton = new Button(right);
            rightButton.setMaxWidth(Double.MAX_VALUE);
            rightButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
            final String rightName = right;
            rightButton.setOnAction(event -> {
                specificRightsBox.getChildren().clear();
                if ("Accounts".equals(rightName)) {
                    for (String subRight : accountSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Employees".equals(rightName)) {
                    for (String subRight : employeesSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Reports".equals(rightName)) {
                    for (String subRight : reportsSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Settings".equals(rightName)) {
                    for (String subRight : settingsSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Register".equals(rightName)) {
                    for (String subRight : registerSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Raw Stock".equals(rightName)) {
                    for (String subRight : rawStockSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Production".equals(rightName)) {
                    for (String subRight : productionSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Books".equals(rightName)) {
                    for (String subRight : booksSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Bank Mgmt".equals(rightName)) {
                    for (String subRight : bankMgmtSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else if ("Settings".equals(rightName)) {
                    for (String subRight : settingsSubRights) {
                        Button subBtn = new Button(subRight);
                        subBtn.setMaxWidth(Double.MAX_VALUE);
                        subBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #007bff; -fx-border-color: #007bff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
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
                        specificRightsBox.getChildren().add(subBtn);
                    }
                    specificRightsBox.setVisible(true);
                    specificRightsScrollPane.setVisible(true);
                } else {
                    // Hide specific rights box for other rights
                    specificRightsBox.setVisible(false);
                    specificRightsScrollPane.setVisible(false);
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
                }
            });
            availableRightsBox.getChildren().add(rightButton);
        }

        Button assignRights = new Button("Add Selected Rights");
        assignRights.getStyleClass().add("register-button");
        assignRights.setMaxWidth(Double.MAX_VALUE);
        assignRights.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 10; -fx-font-size: 14px; -fx-font-weight: bold;");
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(15);
        assignRights.setOnAction(e -> {
            java.util.List<String> newlySelectedRights = selectedRightsBox.getChildren().stream()
                .filter(node -> node instanceof Button && !node.equals(noRightsLabel))
                .map(node -> (Button) node)
                .filter(btn -> btn.getStyle().contains("#d4edda"))
                .map(btn -> btn.getText().replace(" ✕", "").trim())
                .collect(java.util.stream.Collectors.toList());

            // If any bank mgmt sub-right is selected, also add 'Bank Mgmt' right
            boolean hasBankMgmtSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(bankMgmtSubRights).contains(r));
            if (hasBankMgmtSubRight && !newlySelectedRights.contains("Bank Mgmt")) {
                newlySelectedRights.add("Bank Mgmt");
            }

            // No removal needed; just add 'Bank Mgmt' if any sub-right is present
            String targetUser = targetUsername.getText().trim();
            String adminUser = adminUsername.getText().trim();
            String adminPass = adminPassword.getText();
            // If any account sub-right is selected, also add 'Accounts' right
            boolean hasAccountSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(accountSubRights).contains(r));
            if (hasAccountSubRight && !newlySelectedRights.contains("Accounts")) {
                newlySelectedRights.add("Accounts");
            }
            // If any register sub-right is selected, also add 'Register' right
            boolean hasRegisterSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(registerSubRights).contains(r));
            if (hasRegisterSubRight && !newlySelectedRights.contains("Register")) {
                newlySelectedRights.add("Register");
            }
            // If any raw stock sub-right is selected, also add 'Raw Stock' right
            boolean hasRawStockSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(rawStockSubRights).contains(r));
            if (hasRawStockSubRight && !newlySelectedRights.contains("Raw Stock")) {
                newlySelectedRights.add("Raw Stock");
            }
            // If any production sub-right is selected, also add 'Production' right
            boolean hasProductionSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(productionSubRights).contains(r));
            if (hasProductionSubRight && !newlySelectedRights.contains("Production")) {
                newlySelectedRights.add("Production");
            }
            // If any books sub-right is selected, also add 'Books' right
            boolean hasBooksSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(booksSubRights).contains(r));
            if (hasBooksSubRight && !newlySelectedRights.contains("Books")) {
                newlySelectedRights.add("Books");
            }
            // If any Employees sub-right is selected, also add 'Employees' right
            boolean hasEmployeesSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(employeesSubRights).contains(r));
            if (hasEmployeesSubRight && !newlySelectedRights.contains("Employees")) {
                newlySelectedRights.add("Employees");
            }
            // If any Reports sub-right is selected, also add 'Reports' right
            boolean hasReportsSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(reportsSubRights).contains(r));
            if (hasReportsSubRight && !newlySelectedRights.contains("Reports")) {
                newlySelectedRights.add("Reports");
            }
            // If any Settings sub-right is selected, also add 'Settings' right
            boolean hasSettingsSubRight = newlySelectedRights.stream().anyMatch(r -> java.util.Arrays.asList(settingsSubRights).contains(r));
            if (hasSettingsSubRight && !newlySelectedRights.contains("Settings")) {
                newlySelectedRights.add("Settings");
            }
            System.out.println("DEBUG: Newly selected rights to add: " + newlySelectedRights);
            if (targetUser.isEmpty() || adminUser.isEmpty() || adminPass.isEmpty() || newlySelectedRights.isEmpty()) {
                showAlert("Error", "All fields are required! Please select at least one new right to add.");
                return;
            }
            if (config.database != null && config.database.isConnected()) {
                if (config.database.SignIn(adminUser, adminPass)) {
                    if (config.database.userExists(targetUser)) {
                        if (config.database.addUserRights(targetUser, newlySelectedRights, adminUser)) {
                            String rightsString = String.join(", ", newlySelectedRights);
                            showAlert("Success", "Rights [" + rightsString + "] added to user '" + targetUser + "' successfully!");
                            // Refresh rights in UI
                            loadExistingRights(targetUser, selectedRightsBox, noRightsLabel);
                            // Optionally reload session (uncomment if needed)
                            // com.cablemanagement.config.setCurrentUser(targetUser, com.cablemanagement.config.getCurrentUserRole());
                            adminUsername.clear();
                            adminPassword.clear();
                        } else {
                            showAlert("Error", "Failed to assign rights to user. Please try again.");
                        }
                    } else {
                        showAlert("Error", "Target user '" + targetUser + "' does not exist!");
                    }
                } else {
                    showAlert("Error", "Invalid admin credentials!");
                }
            } else {
                showAlert("Error", "Database not connected!");
            }
        });

        // Layout: HBox for left/right, VBox for form and selected rights
        HBox rightsHBox = new HBox(30);
        rightsHBox.setAlignment(Pos.TOP_LEFT);
        rightsHBox.getChildren().addAll(
            new VBox(availableRightsLabel, availableRightsScrollPane),
            new VBox(specificRightsLabel, specificRightsScrollPane)
        );

        VBox formBox = new VBox(15, targetUsername, adminUsername, adminPassword, spacer, assignRights);
        formBox.setAlignment(Pos.TOP_LEFT);

        mainBox.getChildren().addAll(mainHeading, rightsHBox, selectedRightsLabel, selectedRightsScrollPane, formBox);
        ScrollPane scrollPane = new ScrollPane(mainBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setPadding(new Insets(10));
    scrollPane.setPrefWidth(1600);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    return scrollPane;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void loadExistingRights(String username, VBox selectedRightsBox, Label noRightsLabel) {
        try {
            // Get existing rights for the user
            java.util.List<String> existingRights = config.database.getUserRights(username);
            
            // Clear the selected rights box
            selectedRightsBox.getChildren().clear();
            
            if (existingRights.isEmpty()) {
                // No existing rights, show the "no rights" label
                selectedRightsBox.getChildren().add(noRightsLabel);
            } else {
                // Add existing rights as removable buttons
                    // For Bank Mgmt, only show assigned sub-rights
                    String[] bankMgmtSubRights = {
                        "Manage Banks",
                        "Bank Transactions",
                        "View Cash In Hand",
                        "View Cash Ledger"
                    };
                    boolean hasBankMgmt = existingRights.contains("Bank Mgmt");
                    java.util.List<String> assignedBankMgmtSubRights = new java.util.ArrayList<>();
                    for (String sub : bankMgmtSubRights) {
                        if (existingRights.contains(sub)) {
                            assignedBankMgmtSubRights.add(sub);
                        }
                    }
                    for (String right : existingRights) {
                        // If main right and sub-rights exist, skip showing main right
                        if (right.equals("Bank Mgmt") && !assignedBankMgmtSubRights.isEmpty()) continue;
                        Button selectedButton = new Button(right + " ✕");
                        selectedButton.setMaxWidth(Double.MAX_VALUE);
                        selectedButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #c82333; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                    
                        // Add hover effects for existing rights
                        selectedButton.setOnMouseEntered(e -> {
                            selectedButton.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; -fx-border-color: #bd2130; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(220,53,69,0.5), 4, 0, 0, 2);");
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
