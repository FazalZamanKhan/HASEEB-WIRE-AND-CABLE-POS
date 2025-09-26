package com.cablemanagement.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.cablemanagement.config;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BalanceSheetView {
    
    public static void showBalanceSheet() {
        // Get balance sheet data
        Object[] balanceData = config.database.getBalanceSheetData();
        
        // Defensive programming - check for null data
        if (balanceData == null || balanceData.length < 8) {
            System.err.println("Error: Invalid balance sheet data returned from database");
            // Show error dialog and return
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Balance Sheet Error");
            alert.setHeaderText("Data Error");
            alert.setContentText("Unable to retrieve balance sheet data from database. Please check your database connection.");
            alert.showAndWait();
            return;
        }
        
        // Safely extract data with null checks
        double totalBankBalance = (balanceData[0] != null) ? (Double) balanceData[0] : 0.0;
        double customersOweUs = (balanceData[1] != null) ? (Double) balanceData[1] : 0.0;
        double weOweCustomers = (balanceData[2] != null) ? (Double) balanceData[2] : 0.0;
        double suppliersOweUs = (balanceData[3] != null) ? (Double) balanceData[3] : 0.0;
        double weOweSuppliers = (balanceData[4] != null) ? (Double) balanceData[4] : 0.0;
        double totalReceivables = (balanceData[5] != null) ? (Double) balanceData[5] : 0.0;
        double totalPayables = (balanceData[6] != null) ? (Double) balanceData[6] : 0.0;
        double netWorth = (balanceData[7] != null) ? (Double) balanceData[7] : 0.0;
        
        // Create the balance sheet view
        VBox balanceSheetView = createBalanceSheetView(
            totalBankBalance, customersOweUs, weOweCustomers, 
            suppliersOweUs, weOweSuppliers, totalReceivables, 
            totalPayables, netWorth
        );
        
        // Create buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        
        Button printBtn = new Button("Print Balance Sheet");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        printBtn.setOnAction(e -> printBalanceSheet());
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        
        buttonBox.getChildren().addAll(printBtn, closeBtn);
        
        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(balanceSheetView, buttonBox);
        
        Scene scene = new Scene(mainLayout, 600, 700);
        Stage stage = new Stage();
        stage.setTitle("Balance Sheet - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        stage.setScene(scene);
        stage.show();
    }
    
    private static VBox createBalanceSheetView(double totalBankBalance, double customersOweUs, 
                                             double weOweCustomers, double suppliersOweUs, 
                                             double weOweSuppliers, double totalReceivables, 
                                             double totalPayables, double netWorth) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        // Header
        Label title = new Label("BALANCE SHEET");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label companyName = new Label("Cable Management System");
        companyName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        Label dateLabel = new Label("As of " + currentDate);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(title, companyName, dateLabel);
        
        // Separator line
        Label separator1 = new Label("═".repeat(60));
        separator1.setStyle("-fx-font-family: monospace; -fx-text-fill: #bdc3c7;");
        separator1.setAlignment(Pos.CENTER);
        
        // Assets Section
        Label assetsHeader = new Label("ASSETS");
        assetsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        VBox assetsSection = new VBox(5);
        assetsSection.getChildren().addAll(
            createBalanceRow("Cash in Hand (All Banks)", totalBankBalance, false),
            createBalanceRow("Customers Owe Us", customersOweUs, false),
            createBalanceRow("Suppliers Owe Us", suppliersOweUs, false),
            createSeparatorLine(),
            createBalanceRow("TOTAL ASSETS", totalBankBalance + totalReceivables, true)
        );
        
        // Liabilities Section
        Label liabilitiesHeader = new Label("LIABILITIES");
        liabilitiesHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        VBox liabilitiesSection = new VBox(5);
        liabilitiesSection.getChildren().addAll(
            createBalanceRow("We Owe Customers", weOweCustomers, false),
            createBalanceRow("We Owe Suppliers", weOweSuppliers, false),
            createSeparatorLine(),
            createBalanceRow("TOTAL LIABILITIES", totalPayables, true)
        );
        
        // Net Worth Section
        Label netWorthHeader = new Label("NET WORTH");
        netWorthHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        VBox netWorthSection = new VBox(5);
        String netWorthColor = netWorth >= 0 ? "#27ae60" : "#e74c3c";
        Label netWorthRow = createBalanceRow("NET WORTH (Assets - Liabilities)", netWorth, true);
        netWorthRow.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + netWorthColor + ";");
        netWorthSection.getChildren().add(netWorthRow);
        
        // Footer separator
        Label separator2 = new Label("═".repeat(60));
        separator2.setStyle("-fx-font-family: monospace; -fx-text-fill: #bdc3c7;");
        separator2.setAlignment(Pos.CENTER);
        
        // Summary
        VBox summary = new VBox(3);
        summary.setAlignment(Pos.CENTER);
        summary.getChildren().addAll(
            new Label("Generated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
            new Label("Cable Management System - Financial Report")
        );
        summary.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
        
        container.getChildren().addAll(
            header,
            separator1,
            assetsHeader,
            assetsSection,
            new Label(" "), // Spacer
            liabilitiesHeader,
            liabilitiesSection,
            new Label(" "), // Spacer
            netWorthHeader,
            netWorthSection,
            separator2,
            summary
        );
        
        return container;
    }
    
    private static Label createBalanceRow(String description, double amount, boolean isBold) {
        String formattedAmount = String.format("Rs. %.2f", Math.abs(amount));
        String text = String.format("%-40s %15s", description, formattedAmount);
        
        Label row = new Label(text);
        row.setStyle("-fx-font-family: monospace; -fx-font-size: " + (isBold ? "14px" : "12px") + 
                    "; -fx-font-weight: " + (isBold ? "bold" : "normal") + ";");
        
        if (amount < 0 && !description.contains("Owe")) {
            row.setStyle(row.getStyle() + " -fx-text-fill: #e74c3c;");
        }
        
        return row;
    }
    
    private static Label createSeparatorLine() {
        Label line = new Label("─".repeat(55));
        line.setStyle("-fx-font-family: monospace; -fx-text-fill: #bdc3c7; -fx-font-size: 10px;");
        return line;
    }
    
    private static void printBalanceSheet() {
        try {
            // Generate temporary filename
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                             "BalanceSheet_" + timestamp + ".pdf";
            
            // Use the BalanceSheetGenerator to create and open PDF for printing
            com.cablemanagement.invoice.BalanceSheetGenerator.generateAndPreviewBalanceSheet(filename);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Print Error");
            alert.setHeaderText("Balance Sheet Print Error");
            alert.setContentText("Failed to generate balance sheet for printing: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}
