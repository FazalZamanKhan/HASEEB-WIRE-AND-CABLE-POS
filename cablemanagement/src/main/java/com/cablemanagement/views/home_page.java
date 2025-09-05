package com.cablemanagement.views;

import java.net.URL;
import com.cablemanagement.views.pages.HomeContent;
import com.cablemanagement.views.pages.SettingsContent;
import com.cablemanagement.views.pages.ProfileContent;
import com.cablemanagement.views.pages.RegisterContent;
import com.cablemanagement.views.pages.ReportsContent;
import com.cablemanagement.views.pages.RawStock;
import com.cablemanagement.views.pages.ProductionStock;
import com.cablemanagement.views.pages.BooksContent;
import com.cablemanagement.views.pages.BankManagementContent;
import com.cablemanagement.views.pages.EmployeeManagementContent;
import com.cablemanagement.views.pages.SalesmanContent;
import com.cablemanagement.views.pages.AccountsContent;
import com.cablemanagement.config;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;

public class home_page {

    private static boolean isCollapsed = false;

    public static Scene getHomeScene() {
        BorderPane mainLayout = new BorderPane();

        VBox sidebarContent = new VBox(2);  // Further reduced spacing to 2
        sidebarContent.setPadding(new Insets(5)); // Reduced padding to give more space to buttons
        sidebarContent.getStyleClass().add("sidebar");
        sidebarContent.setFillWidth(true); // Ensure buttons fill the width;
        
        // Make the VBox fill all available space
        VBox.setVgrow(sidebarContent, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(sidebarContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefWidth(200);
        scrollPane.getStyleClass().add("custom-scroll");

        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().add(HomeContent.get());

        // Standard Emoji + Arial Font
        Button homeBtn = createSidebarButton("🏠 Home");
        Button accountsBtn = createSidebarButton("💰 Accounts");
        Button registerBtn = createSidebarButton("✎ Register");
        Button rawStockBtn = createSidebarButton("📦 Raw Stock");
        Button productionStockBtn = createSidebarButton("🏭 Production");

        Button booksBtn = createSidebarButton("📚 Books");
        Button bankMgmtBtn = createSidebarButton("Ⓑ Bank Mgmt");
        Button salesmanBtn = createSidebarButton("☺ Salesman");
        Button employeeMgmtBtn = createSidebarButton("☺ Employees");
        Button reportsBtn = createSidebarButton("📊 Reports");
        Button settingsBtn = createSidebarButton("⚙️ Settings");

        Button collapseBtn = new Button("⏪");
        collapseBtn.setFont(Font.font("Arial", 14));
        collapseBtn.setMaxWidth(Double.MAX_VALUE);
        collapseBtn.setPrefHeight(65);  // Increased height to match other buttons
        collapseBtn.setMaxHeight(Double.MAX_VALUE); // Allow unlimited height growth
        collapseBtn.getStyleClass().add("collapse-button");
        VBox.setVgrow(collapseBtn, Priority.ALWAYS); // Allow button to grow vertically

        sidebarContent.getChildren().addAll(
            homeBtn, accountsBtn, registerBtn,
            rawStockBtn, productionStockBtn,
            booksBtn, bankMgmtBtn, salesmanBtn,
            employeeMgmtBtn, reportsBtn, settingsBtn,
            collapseBtn
        );

        mainLayout.setLeft(scrollPane);
        mainLayout.setCenter(contentArea);

        // Button actions with rights checking
        homeBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Home")) {
                contentArea.getChildren().setAll(HomeContent.get());
            } else {
                showAccessDeniedAlert("Home");
            }
        });
        
        accountsBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Accounts")) {
                contentArea.getChildren().setAll(AccountsContent.get());
            } else {
                showAccessDeniedAlert("Accounts");
            }
        });
        
        settingsBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Settings")) {
                contentArea.getChildren().setAll(SettingsContent.get());
            } else {
                showAccessDeniedAlert("Settings");
            }
        });
        
        registerBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Register")) {
                contentArea.getChildren().setAll(RegisterContent.get());
            } else {
                showAccessDeniedAlert("Register");
            }
        });
        
        rawStockBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Raw Stock")) {
                contentArea.getChildren().setAll(RawStock.get());
            } else {
                showAccessDeniedAlert("Raw Stock");
            }
        });
        
        productionStockBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Production")) {
                contentArea.getChildren().setAll(ProductionStock.get());
            } else {
                showAccessDeniedAlert("Production");
            }
        });
        
        booksBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Books")) {
                contentArea.getChildren().setAll(BooksContent.get());
            } else {
                showAccessDeniedAlert("Books");
            }
        });
        
        bankMgmtBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Bank Mgmt")) {
                contentArea.getChildren().setAll(BankManagementContent.get());
            } else {
                showAccessDeniedAlert("Bank Mgmt");
            }
        });
        
        salesmanBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Salesman")) {
                contentArea.getChildren().setAll(SalesmanContent.get());
            } else {
                showAccessDeniedAlert("Salesman");
            }
        });
        
        employeeMgmtBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Employees")) {
                contentArea.getChildren().setAll(EmployeeManagementContent.get());
            } else {
                showAccessDeniedAlert("Employees");
            }
        });
        
        reportsBtn.setOnAction(e -> {
            if (com.cablemanagement.config.hasCurrentUserRight("Reports")) {
                contentArea.getChildren().setAll(ReportsContent.get());
            } else {
                showAccessDeniedAlert("Reports");
            }
        });

        collapseBtn.setOnAction(e -> {
            isCollapsed = !isCollapsed;
            sidebarContent.getChildren().clear();

            if (isCollapsed) {
                scrollPane.setPrefWidth(72);
                sidebarContent.getChildren().addAll(
                    createIconOnlyButtonWithRights("🏠", contentArea, "Home", HomeContent.get()),
                    createIconOnlyButtonWithRights("💰", contentArea, "Accounts", AccountsContent.get()),
                    createIconOnlyButtonWithRights("☺", contentArea, "Profile", ProfileContent.get()),
                    createIconOnlyButtonWithRights("✎", contentArea, "Register", RegisterContent.get()),
                    createIconOnlyButtonWithRights("📦", contentArea, "Raw Stock", RawStock.get()),
                    createIconOnlyButtonWithRights("🏭", contentArea, "Production", ProductionStock.get()),
                    createIconOnlyButtonWithRights("📚", contentArea, "Books", BooksContent.get()),
                    createIconOnlyButtonWithRights("Ⓑ", contentArea, "Bank Mgmt", BankManagementContent.get()),
                    createIconOnlyButtonWithRights("☺", contentArea, "Salesman", SalesmanContent.get()),
                    createIconOnlyButtonWithRights("☺", contentArea, "Employees", EmployeeManagementContent.get()),
                    createIconOnlyButtonWithRights("📊", contentArea, "Reports", ReportsContent.get()),
                    createIconOnlyButtonWithRights("⚙️", contentArea, "Settings", SettingsContent.get())
                );
                collapseBtn.setText("⏩");
            } else {
                scrollPane.setPrefWidth(200);
                homeBtn.setText("🏠 Home");
                accountsBtn.setText("💰 Accounts");
                settingsBtn.setText("⚙️ Settings");
                registerBtn.setText("✎ Register");
                rawStockBtn.setText("📦 Raw Stock");
                productionStockBtn.setText("🏭 Production");
                booksBtn.setText("📚 Books");
                bankMgmtBtn.setText("Ⓑ Bank Mgmt");
                salesmanBtn.setText("☺ Salesman");
                employeeMgmtBtn.setText("☺ Employees");
                reportsBtn.setText("📊 Reports");

                sidebarContent.getChildren().addAll(
                    homeBtn, accountsBtn, registerBtn,
                    rawStockBtn, productionStockBtn,
                    booksBtn, bankMgmtBtn, salesmanBtn,
                    employeeMgmtBtn, reportsBtn, settingsBtn
                );
                collapseBtn.setText("⏪");
            }

            sidebarContent.getChildren().add(collapseBtn);
        });

        Scene scene = new Scene(mainLayout, 800, 500);
        String cssPath = home_page.class.getResource("/com/cablemanagement/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        return scene;
    }

    private static Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 14));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(65);  // Further increased height
        btn.setMaxHeight(Double.MAX_VALUE); // Allow unlimited height growth
        btn.getStyleClass().add("sidebar-button");
        
        // Make each button grow to fill available space
        VBox.setVgrow(btn, Priority.ALWAYS);
        
        return btn;
    }

    private static Button createIconOnlyButton(String icon, StackPane contentArea, Node targetPage) {
        Button btn = new Button(icon);
        btn.setFont(Font.font("Arial", 16));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(65);  // Increased height to match sidebar buttons
        btn.setMaxHeight(Double.MAX_VALUE); // Allow unlimited height growth
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> contentArea.getChildren().setAll(targetPage));
        btn.setAlignment(javafx.geometry.Pos.CENTER);
        btn.setStyle("-fx-text-alignment: center; -fx-alignment: center;");
        
        // Make each button grow to fill available space
        VBox.setVgrow(btn, Priority.ALWAYS);
        
        return btn;
    }
    
    private static Button createIconOnlyButtonWithRights(String icon, StackPane contentArea, String pageName, Node targetPage) {
        Button btn = new Button(icon);
        btn.setFont(Font.font("Arial", 16));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(65);  // Increased height to match sidebar buttons
        btn.setMaxHeight(Double.MAX_VALUE); // Allow unlimited height growth
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> {
            if (config.hasCurrentUserRight(pageName)) {
                contentArea.getChildren().setAll(targetPage);
            } else {
                showAccessDeniedAlert(pageName);
            }
        });
        btn.setAlignment(javafx.geometry.Pos.CENTER);
        btn.setStyle("-fx-text-alignment: center; -fx-alignment: center;");
        
        // Make each button grow to fill available space
        VBox.setVgrow(btn, Priority.ALWAYS);
        
        return btn;
    }
    
    private static void showAccessDeniedAlert(String pageName) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Insufficient Permissions");
        alert.setContentText("You don't have permission to access the " + pageName + " page.\n\nPlease contact your administrator to request access rights.");
        alert.showAndWait();
    }
}
