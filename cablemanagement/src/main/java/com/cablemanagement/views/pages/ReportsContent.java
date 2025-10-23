package com.cablemanagement.views.pages;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cablemanagement.config;
import com.cablemanagement.invoice.PrintManager;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Supplier;

public class ReportsContent {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane reportArea = new StackPane();
        reportArea.getChildren().add(createReportsList());

        HBox buttonBar = createButtonBar(reportArea);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        configureScrollPane(scrollPane);

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(reportArea);

        return mainLayout;
    }

    private static HBox createButtonBar(StackPane reportArea) {
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        String[] buttonLabels = {
            "Purchase Report",
            "Sales Report",
            "Return Purchase Report",
            "Return Sales Report",
            "Bank Transfer Report",
            "Profit Report",
            // "Summary Report",
            "Balance Sheet",
            "Area-Wise Report",
            "Brand Sales Report",
            // "Brand Profit Report",
            // "Customer Sales Report",
            // "Supplier Sales Report",
            // "Attendance Report"
        };

        Runnable[] actions = {
            () -> reportArea.getChildren().setAll(createPurchaseReport()),
            () -> reportArea.getChildren().setAll(createSalesReport()),
            () -> reportArea.getChildren().setAll(createReturnPurchaseReport()),
            () -> reportArea.getChildren().setAll(createReturnSalesReport()),
            () -> reportArea.getChildren().setAll(createBankTransferReport()),
            () -> reportArea.getChildren().setAll(createProfitReport()),
            // () -> reportArea.getChildren().setAll(createSummaryReport()),
            () -> reportArea.getChildren().setAll(createBalanceSheet()),
            () -> reportArea.getChildren().setAll(createAreaWiseReport()),
            () -> reportArea.getChildren().setAll(createBrandSalesReport()),
            // () -> reportArea.getChildren().setAll(createBrandProfitReport()),
            // () -> reportArea.getChildren().setAll(createCustomerSalesReport()),
            // () -> reportArea.getChildren().setAll(createSupplierSalesReport()),
            // () -> reportArea.getChildren().setAll(createAttendanceReport())
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            // Only show button if user has the specific report right
            if (com.cablemanagement.config.hasCurrentUserRight(buttonLabels[i])) {
                addButton(buttonBar, buttonLabels[i], actions[i]);
            }
        }

        return buttonBar;
    }

    private static void configureScrollPane(ScrollPane scrollPane) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(72);
        scrollPane.setMinHeight(72);
        scrollPane.setMaxHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static VBox createReportsList() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Reports Dashboard");
        Label info = new Label("Select a report from the toolbar above to view detailed information");
        info.setStyle("-fx-font-size: 14; -fx-text-fill: #007bff;");

        form.getChildren().addAll(heading, info);
        return form;
    }

    private static VBox createPurchaseReport() {
        
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Purchase Report");

        // Report type dropdown
        HBox reportTypeBox = new HBox(10);
        Label reportLabel = new Label("Select Report:");
        ComboBox<String> reportComboBox = new ComboBox<>();
        reportComboBox.getItems().addAll(
            "All Reports",
            "Product-wise Report",
            "Category-wise Report",
            "Brand-wise Report",
            "Manufacturer-wise Report"
        );
        reportComboBox.setValue("All Reports");
        reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
        reportTypeBox.setAlignment(Pos.CENTER_LEFT);

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Purchase report table (declare before buttons for lambda reference)
        TableView<Map<String, String>> table = new TableView<>();

        TableColumn<Map<String, String>, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Invoice #", "")));

        TableColumn<Map<String, String>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Date", "")));

        TableColumn<Map<String, String>, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Supplier", "")));

        TableColumn<Map<String, String>, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Amount", "")));

        TableColumn<Map<String, String>, String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Discount", "")));

        TableColumn<Map<String, String>, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("Paid", "")));

        table.getColumns().add(invCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(supplierCol);
        table.getColumns().add(amountCol);
        table.getColumns().add(discountCol);
        table.getColumns().add(paidCol);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons with custom "Show Report" action
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = String.format("Purchase Report - %s | From: %s | To: %s", 
                reportComboBox.getValue(), 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Purchase Report", table, filterInfo);
        });

        // Load data on filter
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            errorLabel.setText("");

            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    String selectedReport = reportComboBox.getValue();

                    ResultSet rs = config.database.getPurchaseReport(from, to, selectedReport);
                    if (rs != null && rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // Create columns dynamically
                        for (int i = 1; i <= columnCount; i++) {
                            final String colName = metaData.getColumnLabel(i);
                            TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);
                            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(colName, "")));
                            table.getColumns().add(col);
                        }

                        // Remove rs.beforeFirst(); because SQLite ResultSet is TYPE_FORWARD_ONLY

                        // Fill table with data
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        // The first rs.next() above already moved to the first row, so process that row first
                        do {
                            Map<String, String> row = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnLabel(i), rs.getString(i));
                            }
                            data.add(row);
                        } while (rs.next());

                        table.setItems(data);
                    } else {
                        errorLabel.setText("No data found for selected filters.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading data: " + ex.getMessage());
            }
        });

        // Trigger filter once on load
        filterBtn.fire();

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                exportPurchaseReportToPDF(table, reportComboBox.getValue(), fromDatePicker.getValue(), toDatePicker.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                
                // Add headers - dynamically from table columns
                for (int i = 0; i < table.getColumns().size(); i++) {
                    csvContent.append(table.getColumns().get(i).getText());
                    if (i < table.getColumns().size() - 1) csvContent.append(",");
                }
                csvContent.append("\n");
                
                // Add data rows
                for (Map<String, String> row : table.getItems()) {
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        String columnName = table.getColumns().get(i).getText();
                        String value = row.getOrDefault(columnName, "");
                        // Escape quotes and commas in CSV
                        if (value.contains(",") || value.contains("\"")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        csvContent.append(value);
                        if (i < table.getColumns().size() - 1) csvContent.append(",");
                    }
                    csvContent.append("\n");
                }
                
                // Show export dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Purchase Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Sales Report");

        HBox reportTypeBox = new HBox(10);
        Label reportLabel = new Label("Select Report:");
        ComboBox<String> reportComboBox = new ComboBox<>();
        reportComboBox.getItems().addAll(
            "All Reports",
            "Product-wise Report",
            "Category-wise Report",
            "Brand-wise Report",
            "Manufacturer-wise Report"
        );
        reportComboBox.setValue("All Reports"); // default
        reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
        reportTypeBox.setAlignment(Pos.CENTER_LEFT);

        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        TableView<ObservableList<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = String.format("Sales Report - %s | From: %s | To: %s", 
                reportComboBox.getValue(), 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Sales Report", table, filterInfo);
        });

        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            errorLabel.setText("");

            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    String selectedReport = reportComboBox.getValue();

                    ResultSet rs = config.database.getSalesReport(from, to, selectedReport);

                    if (rs != null) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int columnCount = meta.getColumnCount();

                        // Auto-create columns
                        for (int i = 1; i <= columnCount; i++) {
                            final int colIndex = i;
                            TableColumn<ObservableList<String>, String> col =
                                new TableColumn<>(meta.getColumnLabel(i));
                            col.setCellValueFactory(data ->
                                new SimpleStringProperty(data.getValue().get(colIndex - 1))
                            );
                            table.getColumns().add(col);
                        }

                        // Add rows
                        while (rs.next()) {
                            ObservableList<String> row = FXCollections.observableArrayList();
                            for (int i = 1; i <= columnCount; i++) {
                                row.add(rs.getString(i));
                            }
                            table.getItems().add(row);
                        }

                        if (table.getItems().isEmpty()) {
                            errorLabel.setText("No data found for selected filters.");
                        }
                    } else {
                        errorLabel.setText("No data returned from query.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading sales data: " + ex.getMessage());
            }
        });

        filterBtn.fire();

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                exportSalesReportToPDF(table, reportComboBox.getValue(), fromDatePicker.getValue(), toDatePicker.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                
                // Add headers
                for (int i = 0; i < table.getColumns().size(); i++) {
                    csvContent.append(table.getColumns().get(i).getText());
                    if (i < table.getColumns().size() - 1) csvContent.append(",");
                }
                csvContent.append("\n");
                
                // Add data rows
                for (ObservableList<String> row : table.getItems()) {
                    for (int i = 0; i < row.size(); i++) {
                        String value = row.get(i);
                        // Escape quotes and commas in CSV
                        if (value.contains(",") || value.contains("\"")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        csvContent.append(value);
                        if (i < row.size() - 1) csvContent.append(",");
                    }
                    csvContent.append("\n");
                }
                
                // For now, just show export dialog (actual file saving implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Sales Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createReturnPurchaseReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Purchase Report");

        // Report type dropdown
        HBox reportTypeBox = new HBox(10);
        Label reportLabel = new Label("Select Report:");
        ComboBox<String> reportComboBox = new ComboBox<>();
        reportComboBox.getItems().addAll(
            "All Reports",
            "Product-wise Report",
            "Category-wise Report",
            "Brand-wise Report",
            "Manufacturer-wise Report"
        );
        reportComboBox.setValue("All Reports");
        reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
        reportTypeBox.setAlignment(Pos.CENTER_LEFT);

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Table (dynamic)
        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = String.format("Return Purchase Report - %s | From: %s | To: %s", 
                reportComboBox.getValue(), 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Return Purchase Report", table, filterInfo);
        });

        // Load data
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            errorLabel.setText("");

            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    String selectedReport = reportComboBox.getValue();

                    ResultSet rs = config.database.getReturnPurchaseReport(from, to, selectedReport); // Make sure backend handles report type

                    if (rs != null && rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // Create columns dynamically
                        for (int i = 1; i <= columnCount; i++) {
                            final String colName = metaData.getColumnLabel(i);
                            TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);
                            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(colName, "")));
                            table.getColumns().add(col);
                        }

                        // Fill data
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        
                        // The first rs.next() above already moved to the first row, so process that row first
                        do {
                            Map<String, String> row = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnLabel(i), rs.getString(i));
                            }
                            data.add(row);
                        } while (rs.next());

                        table.setItems(data);
                    } else {
                        errorLabel.setText("No data found for selected filters.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading data: " + ex.getMessage());
            }
        });

        // Trigger load on start
        filterBtn.fire();

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                exportReturnPurchaseReportToPDF(table, reportComboBox.getValue(), fromDatePicker.getValue(), toDatePicker.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                
                // Add headers - dynamically from table columns
                for (int i = 0; i < table.getColumns().size(); i++) {
                    csvContent.append(table.getColumns().get(i).getText());
                    if (i < table.getColumns().size() - 1) csvContent.append(",");
                }
                csvContent.append("\n");
                
                // Add data rows
                for (Map<String, String> row : table.getItems()) {
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        String columnName = table.getColumns().get(i).getText();
                        String value = row.getOrDefault(columnName, "");
                        // Escape quotes and commas in CSV
                        if (value.contains(",") || value.contains("\"")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        csvContent.append(value);
                        if (i < table.getColumns().size() - 1) csvContent.append(",");
                    }
                    csvContent.append("\n");
                }
                
                // Show export dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Return Purchase Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createReturnSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Return Sales Report");

        // Report type dropdown
        HBox reportTypeBox = new HBox(10);
        Label reportLabel = new Label("Select Report:");
        ComboBox<String> reportComboBox = new ComboBox<>();
        reportComboBox.getItems().addAll(
            "All Reports",
            "Product-wise Report",
            "Category-wise Report",
            "Brand-wise Report",
            "Manufacturer-wise Report"
        );
        reportComboBox.setValue("All Reports");
        reportTypeBox.getChildren().addAll(reportLabel, reportComboBox);
        reportTypeBox.setAlignment(Pos.CENTER_LEFT);

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Return sales report table (dynamic)
        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Error label for feedback
    Label errorLabel = new Label("");
    errorLabel.setStyle("-fx-text-fill: red;");

    // Total label (shows sum of total_return_amount) - declared here so filter handler can update it
    Label totalLabel = new Label("Total: 0.00");
    totalLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");
    HBox totalBox = new HBox(totalLabel);
    totalBox.setAlignment(Pos.CENTER_RIGHT);

        // Action buttons
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = String.format("Return Sales Report - %s | From: %s | To: %s", 
                reportComboBox.getValue(), 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Return Sales Report", table, filterInfo);
        });
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            table.getColumns().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    String selectedReport = reportComboBox.getValue();
                    
                    java.sql.ResultSet rs = config.database.getReturnSalesReport(from, to, selectedReport);
                    
                    if (rs != null && rs.next()) {
                        java.sql.ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // Create columns dynamically
                        for (int i = 1; i <= columnCount; i++) {
                            final String colName = metaData.getColumnLabel(i);
                            TableColumn<Map<String, String>, String> col = new TableColumn<>(colName);
                            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(colName, "")));
                            table.getColumns().add(col);
                        }

                        // Fill data
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        
                        // The first rs.next() above already moved to the first row, so process that row first
                        do {
                            Map<String, String> row = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnLabel(i), rs.getString(i));
                            }
                            data.add(row);
                        } while (rs.next());

                        table.setItems(data);

                        // Compute total for the return amount column (attempt exact match first, then fuzzy)
                        try {
                            String totalColumnName = null;
                            // try exact match
                            for (int i = 1; i <= columnCount; i++) {
                                String col = metaData.getColumnLabel(i);
                                if (col != null && col.equalsIgnoreCase("total_return_amount")) {
                                    totalColumnName = col;
                                    break;
                                }
                            }
                            // try fuzzy match: contains both 'total' and 'return'
                            if (totalColumnName == null) {
                                for (int i = 1; i <= columnCount; i++) {
                                    String col = metaData.getColumnLabel(i);
                                    if (col != null) {
                                        String low = col.toLowerCase();
                                        if (low.contains("total") && low.contains("return")) {
                                            totalColumnName = col;
                                            break;
                                        }
                                    }
                                }
                            }

                            double total = 0.0;
                            if (totalColumnName != null) {
                                for (Map<String, String> rowItem : data) {
                                    String v = rowItem.get(totalColumnName);
                                    if (v != null && !v.trim().isEmpty()) {
                                        // remove any non-numeric characters except dot and minus
                                        String cleaned = v.replaceAll("[^0-9.\\-]", "");
                                        try {
                                            total += Double.parseDouble(cleaned);
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }
                            }
                            totalLabel.setText("Total: " + String.format("%.2f", total));
                        } catch (Exception exTot) {
                            // don't break loading if total calculation fails
                            System.out.println("DEBUG: Error calculating total: " + exTot.getMessage());
                        }
                    } else {
                        errorLabel.setText("No data found for selected filters.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading return sales data: " + ex.getMessage());
            }
        });

    // Optionally, trigger filter on load
    filterBtn.fire();

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                exportReturnSalesReportToPDF(table, reportComboBox.getValue(), fromDatePicker.getValue(), toDatePicker.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                
                // Add headers - dynamically from table columns
                for (int i = 0; i < table.getColumns().size(); i++) {
                    csvContent.append(table.getColumns().get(i).getText());
                    if (i < table.getColumns().size() - 1) csvContent.append(",");
                }
                csvContent.append("\n");
                
                // Add data rows
                for (Map<String, String> row : table.getItems()) {
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        String columnName = table.getColumns().get(i).getText();
                        String value = row.getOrDefault(columnName, "");
                        // Escape quotes and commas in CSV
                        if (value.contains(",") || value.contains("\"")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        csvContent.append(value);
                        if (i < table.getColumns().size() - 1) csvContent.append(",");
                    }
                    csvContent.append("\n");
                }
                
                // Show export dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Return Sales Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, reportTypeBox, dateRangeBox, buttons, errorLabel, table, totalBox);
        return form;
    }

    private static VBox createBankTransferReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Bank Transfer Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Bank transfer report table - maps to View_Bank_Transfer_Report
        TableView<BankTransferReport> table = new TableView<>();
        
        TableColumn<BankTransferReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        
        TableColumn<BankTransferReport, String> fromCol = new TableColumn<>("From Bank");
        fromCol.setCellValueFactory(new PropertyValueFactory<>("fromBank"));
        
        TableColumn<BankTransferReport, String> toCol = new TableColumn<>("To Bank");
        toCol.setCellValueFactory(new PropertyValueFactory<>("toBank"));
        
        TableColumn<BankTransferReport, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        table.getColumns().addAll(dateCol, fromCol, toCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = String.format("Bank Transfer Report | From: %s | To: %s", 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Bank Transfer Report", table, filterInfo);
        });

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    java.sql.ResultSet rs = config.database.getBankTransferReport(from, to);
                    int count = 0;
                    if (rs != null) {
                        try {
                            while (rs.next()) {
                                String fromBank = rs.getString("from_bank");
                                String toBank = rs.getString("to_bank");
                                String amount = String.format("%.2f", rs.getDouble("amount"));
                                String date = rs.getString("transaction_date");
                                
                                // Handle null values
                                if (fromBank == null) fromBank = "Unknown";
                                if (toBank == null) toBank = "Unknown";
                                if (date == null) date = "Unknown";
                                
                                table.getItems().add(new BankTransferReport(date, fromBank, toBank, amount));
                                count++;
                            }
                        } finally {
                            rs.close();
                        }
                    }
                    System.out.println("BankTransferReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No bank transfer data found for selected date range.\n" +
                                         "Bank transfers are created when you use 'Transfer Bank to Bank' in Bank Management.\n" +
                                         "Only transactions with types 'transfer_in' and 'transfer_out' appear in this report.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading bank transfer data: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> filterBtn.fire());

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            try {
                exportBankTransferReportToPDF(table, fromDatePicker.getValue(), toDatePicker.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Date,From Bank,To Bank,Amount\n");
                
                for (BankTransferReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s,%s\n",
                        item.getTransactionDate(),
                        item.getFromBank(),
                        item.getToBank(),
                        item.getAmount()
                    ));
                }
                
                // Show export dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Bank Transfer Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel, table);
        return form;
    }

    private static VBox createProfitReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Profit Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Profit report table - maps to Sales_Invoice joined with Sales_Invoice_Item and ProductionStock
        TableView<ProfitReport> table = new TableView<>();
        
        TableColumn<ProfitReport, String> invCol = new TableColumn<>("Invoice #");
        invCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        
        TableColumn<ProfitReport, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        
        TableColumn<ProfitReport, String> saleCol = new TableColumn<>("Sale Amount");
        saleCol.setCellValueFactory(new PropertyValueFactory<>("saleAmount"));
        
        TableColumn<ProfitReport, String> costCol = new TableColumn<>("Cost Amount");
        costCol.setCellValueFactory(new PropertyValueFactory<>("costAmount"));
        
        TableColumn<ProfitReport, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
        
        table.getColumns().addAll(invCol, dateCol, saleCol, costCol, profitCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = String.format("Profit Report | From: %s | To: %s", 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Profit Report", table, filterInfo);
        });

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                System.out.println("DEBUG: Profit Report - Loading data...");
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    
                    System.out.println("DEBUG: Date range: " + from + " to " + to);
                    
                    java.sql.ResultSet rs = config.database.getProfitReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String invoiceNumber = rs.getString("sales_invoice_number");
                        String invoiceDate = rs.getString("sales_date");
                        double saleAmount = rs.getDouble("sale_amount");
                        double costAmount = rs.getDouble("cost_amount");
                        double profitAmount = rs.getDouble("profit");
                        
                        // Format amounts as currency strings
                        String formattedSaleAmount = String.format("%.2f", saleAmount);
                        String formattedCostAmount = String.format("%.2f", costAmount);
                        String formattedProfit = String.format("%.2f", profitAmount);
                        
                        System.out.println("DEBUG: Processing profit record - Invoice: " + invoiceNumber + 
                                         ", Date: " + invoiceDate + 
                                         ", Sale: " + formattedSaleAmount + 
                                         ", Cost: " + formattedCostAmount + 
                                         ", Profit: " + formattedProfit);
                        
                        table.getItems().add(new ProfitReport(
                            invoiceNumber,
                            invoiceDate,
                            formattedSaleAmount,
                            formattedCostAmount,
                            formattedProfit
                        ));
                        count++;
                    }
                    
                    System.out.println("ProfitReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No profit data found for selected date range.\n" +
                                         "Profit is calculated as (Sale Amount - Cost Amount) per invoice.\n" +
                                         "Data comes from Sales_Invoice, Sales_Invoice_Item, and ProductionStock tables.");
                    }
                } else {
                    System.out.println("DEBUG: Database is null or not connected");
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading profit data: " + ex.getMessage());
            }
        });

        // Print button action
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create a print-friendly representation
                StringBuilder printContent = new StringBuilder();
                printContent.append("Profit Report\n");
                printContent.append("Date Range: ").append(fromDatePicker.getValue()).append(" to ").append(toDatePicker.getValue()).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n\n");
                printContent.append(String.format("%-15s %-12s %-15s %-15s %-15s\n", "Invoice #", "Date", "Sale Amount", "Cost Amount", "Profit"));
                printContent.append("=".repeat(75)).append("\n");
                
                double totalSales = 0, totalCosts = 0, totalProfit = 0;
                for (ProfitReport item : table.getItems()) {
                    printContent.append(String.format("%-15s %-12s %-15s %-15s %-15s\n",
                        item.getInvoiceNumber(),
                        item.getInvoiceDate(),
                        item.getSaleAmount(),
                        item.getCostAmount(),
                        item.getProfit()
                    ));
                    
                    // Calculate totals
                    try {
                        totalSales += Double.parseDouble(item.getSaleAmount());
                        totalCosts += Double.parseDouble(item.getCostAmount());
                        totalProfit += Double.parseDouble(item.getProfit());
                    } catch (NumberFormatException ignored) {}
                }
                
                printContent.append("=".repeat(75)).append("\n");
                printContent.append(String.format("%-28s %-15s %-15s %-15s\n", "TOTALS:", 
                    String.format("%.2f", totalSales), 
                    String.format("%.2f", totalCosts), 
                    String.format("%.2f", totalProfit)
                ));
                
                // For now, just show print dialog (actual printing implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Report");
                alert.setHeaderText("Profit Report");
                alert.setContentText("Print functionality would be implemented here.\nReport contains " + table.getItems().size() + " records.");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for printing: " + ex.getMessage());
            }
        });

        // Export button action
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Invoice Number,Date,Sale Amount,Cost Amount,Profit\n");
                
                for (ProfitReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s,%s,%s\n",
                        item.getInvoiceNumber(),
                        item.getInvoiceDate(),
                        item.getSaleAmount(),
                        item.getCostAmount(),
                        item.getProfit()
                    ));
                }
                
                // For now, just show export dialog (actual file saving implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Profit Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Data would be saved as CSV with " + table.getItems().size() + " records.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createSummaryReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Summary Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Summary report - maps to getSummaryReport database method
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(10);
        summaryGrid.setPadding(new Insets(15));
        summaryGrid.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-padding: 15;");

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons with Show Report functionality
        HBox buttons = createReportActionButtonsWithShowReport(() -> showGridInNewWindow("Summary Report", summaryGrid, ""));

        // Initialize with default values
        updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);

        // Load data from database
        filterBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date fromDate = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date toDate = java.sql.Date.valueOf(toDatePicker.getValue());
                    
                    System.out.println("DEBUG: Summary Report - Loading data from " + fromDate + " to " + toDate);
                    
                    java.sql.ResultSet rs = config.database.getSummaryReport(fromDate, toDate);
                    if (rs != null && rs.next()) {
                        double totalPurchases = rs.getDouble("total_purchases");
                        double totalSales = rs.getDouble("total_sales");
                        double totalPurchaseReturns = rs.getDouble("total_purchase_returns");
                        double totalSalesReturns = rs.getDouble("total_sales_returns");
                        double totalBankBalance = rs.getDouble("total_bank_balance");
                        int totalCustomers = rs.getInt("total_customers");
                        int totalSuppliers = rs.getInt("total_suppliers");
                        double totalInventoryValue = rs.getDouble("total_inventory_value");
                        
                        System.out.println("DEBUG: Summary data loaded - Purchases: " + totalPurchases + 
                                         ", Sales: " + totalSales + ", Bank Balance: " + totalBankBalance);
                        
                        // Update the grid with real data
                        updateSummaryGrid(summaryGrid, totalPurchases, totalSales, totalPurchaseReturns, 
                                        totalSalesReturns, totalBankBalance, totalCustomers, totalSuppliers, totalInventoryValue);
                        
                        try {
                            rs.close();
                        } catch (SQLException closeEx) {
                            System.out.println("DEBUG: Error closing ResultSet: " + closeEx.getMessage());
                        }
                    } else {
                        System.out.println("DEBUG: No summary data found");
                        errorLabel.setText("No data found for selected date range.");
                        updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);
                        
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (SQLException closeEx) {
                                System.out.println("DEBUG: Error closing empty ResultSet: " + closeEx.getMessage());
                            }
                        }
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                    updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading summary data: " + ex.getMessage());
                updateSummaryGrid(summaryGrid, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);
            }
        });

        // Print button action
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (summaryGrid.getChildren().isEmpty()) {
                errorLabel.setText("No data available to print. Please load data first.");
                return;
            }
            try {
                // Create a print-friendly representation
                StringBuilder printContent = new StringBuilder();
                printContent.append("BUSINESS SUMMARY REPORT\n");
                printContent.append("Date Range: ").append(fromDatePicker.getValue()).append(" to ").append(toDatePicker.getValue()).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n");
                printContent.append("=".repeat(50)).append("\n\n");
                
                // Extract current values from the grid
                printContent.append("FINANCIAL SUMMARY:\n");
                printContent.append("-".repeat(30)).append("\n");
                for (int i = 0; i < summaryGrid.getChildren().size(); i += 2) {
                    if (i + 1 < summaryGrid.getChildren().size()) {
                        Label labelNode = (Label) summaryGrid.getChildren().get(i);
                        Label valueNode = (Label) summaryGrid.getChildren().get(i + 1);
                        printContent.append(String.format("%-25s %s\n", labelNode.getText(), valueNode.getText()));
                    }
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Summary Report");
                alert.setHeaderText("Business Summary Report");
                alert.setContentText("Print functionality would be implemented here.\n\n" +
                                   "Sample Print Preview:\n" + 
                                   printContent.toString().substring(0, Math.min(300, printContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for printing: " + ex.getMessage());
            }
        });

        // Export button action
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (summaryGrid.getChildren().isEmpty()) {
                errorLabel.setText("No data available to export. Please load data first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Summary Item,Value\n");
                
                // Extract current values from the grid
                for (int i = 0; i < summaryGrid.getChildren().size(); i += 2) {
                    if (i + 1 < summaryGrid.getChildren().size()) {
                        Label labelNode = (Label) summaryGrid.getChildren().get(i);
                        Label valueNode = (Label) summaryGrid.getChildren().get(i + 1);
                        String label = labelNode.getText().replace(":", "").replace(",", ";");
                        String value = valueNode.getText().replace(",", "");
                        csvContent.append(String.format("%s,%s\n", label, value));
                    }
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Summary Report");
                alert.setHeaderText("Business Summary Report Export");
                alert.setContentText("Export functionality would be implemented here.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        // Load initial data
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel);
        return form;
    }

    // Helper method to update the summary grid with data
    private static void updateSummaryGrid(GridPane summaryGrid, double totalPurchases, double totalSales, 
                                        double totalPurchaseReturns, double totalSalesReturns, 
                                        double totalBankBalance, int totalCustomers, int totalSuppliers, 
                                        double totalInventoryValue) {
        summaryGrid.getChildren().clear();
        
        // Calculate derived values
        double netPurchases = totalPurchases - totalPurchaseReturns;
        double netSales = totalSales - totalSalesReturns;
        double grossProfit = netSales - netPurchases;
        
        // Add summary items with formatted values
        addSummaryItem(summaryGrid, 0, "Total Purchases:", String.format("%.2f", totalPurchases));
        addSummaryItem(summaryGrid, 1, "Total Sales:", String.format("%.2f", totalSales));
        addSummaryItem(summaryGrid, 2, "Total Purchase Returns:", String.format("%.2f", totalPurchaseReturns));
        addSummaryItem(summaryGrid, 3, "Total Sales Returns:", String.format("%.2f", totalSalesReturns));
        addSummaryItem(summaryGrid, 4, "Net Purchases:", String.format("%.2f", netPurchases));
        addSummaryItem(summaryGrid, 5, "Net Sales:", String.format("%.2f", netSales));
        addSummaryItem(summaryGrid, 6, "Gross Profit:", String.format("%.2f", grossProfit));
        addSummaryItem(summaryGrid, 7, "Total Bank Balance:", String.format("%.2f", totalBankBalance));
        addSummaryItem(summaryGrid, 8, "Total Customers:", String.valueOf(totalCustomers));
        addSummaryItem(summaryGrid, 9, "Total Suppliers:", String.valueOf(totalSuppliers));
        addSummaryItem(summaryGrid, 10, "Inventory Value:", String.format("%.2f", totalInventoryValue));
    }

    private static VBox createBalanceSheet() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Balance Sheet");

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        actionButtons.setPadding(new Insets(10));
        
        Button refreshBtn = createActionButton("Refresh");
        Button viewDetailedBtn = createActionButton("View Detailed Balance Sheet");
        Button printBtn = createActionButton("Print");
        actionButtons.getChildren().addAll(refreshBtn, viewDetailedBtn, printBtn);

        // Balance sheet - gets data from database
        GridPane balanceGrid = new GridPane();
        balanceGrid.setHgap(20);
        balanceGrid.setVgap(10);
        balanceGrid.setPadding(new Insets(15));
        balanceGrid.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Load balance sheet data from database
        Runnable loadBalanceSheet = () -> {
            balanceGrid.getChildren().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    Object[] balanceData = config.database.getBalanceSheetData();
                    
                    // Defensive programming - check for null data
                    if (balanceData == null || balanceData.length < 8) {
                        errorLabel.setText("Error: Invalid balance sheet data returned from database");
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
                    
                    int row = 0;
                    
                    // Assets Header
                    Label assetsLabel = new Label("ASSETS");
                    assetsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #27ae60;");
                    balanceGrid.add(assetsLabel, 0, row++);
                    
                    // Current Assets
                    addBalanceSheetItem(balanceGrid, row++, "Cash in Hand (All Banks):", String.format("Rs. %.2f", totalBankBalance));
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Receivable (Customers):", String.format("Rs. %.2f", customersOweUs));
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Receivable (Suppliers):", String.format("Rs. %.2f", suppliersOweUs));
                    
                    // Total Assets
                    Label totalAssetsLabel = new Label("TOTAL ASSETS:");
                    totalAssetsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    balanceGrid.add(totalAssetsLabel, 0, row);
                    
                    Label totalAssetsValue = new Label(String.format("Rs. %.2f", totalBankBalance + totalReceivables));
                    totalAssetsValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;");
                    balanceGrid.add(totalAssetsValue, 1, row++);
                    
                    // Spacer
                    balanceGrid.add(new Label(" "), 0, row++);
                    
                    // Liabilities Header
                    Label liabilitiesLabel = new Label("LIABILITIES");
                    liabilitiesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
                    balanceGrid.add(liabilitiesLabel, 0, row++);
                    
                    // Current Liabilities
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Payable (Customers):", String.format("Rs. %.2f", weOweCustomers));
                    addBalanceSheetItem(balanceGrid, row++, "Accounts Payable (Suppliers):", String.format("Rs. %.2f", weOweSuppliers));
                    
                    // Total Liabilities
                    Label totalLiabilitiesLabel = new Label("TOTAL LIABILITIES:");
                    totalLiabilitiesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    balanceGrid.add(totalLiabilitiesLabel, 0, row);
                    
                    Label totalLiabilitiesValue = new Label(String.format("Rs. %.2f", totalPayables));
                    totalLiabilitiesValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    balanceGrid.add(totalLiabilitiesValue, 1, row++);
                    
                    // Spacer
                    balanceGrid.add(new Label(" "), 0, row++);
                    
                    // Net Worth Header
                    Label netWorthLabel = new Label("NET WORTH");
                    netWorthLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3498db;");
                    balanceGrid.add(netWorthLabel, 0, row++);
                    
                    // Net Worth Value
                    Label netWorthDescLabel = new Label("NET WORTH (Assets - Liabilities):");
                    netWorthDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    balanceGrid.add(netWorthDescLabel, 0, row);
                    
                    String netWorthColor = netWorth >= 0 ? "#27ae60" : "#e74c3c";
                    Label netWorthValue = new Label(String.format("Rs. %.2f", netWorth));
                    netWorthValue.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + netWorthColor + ";");
                    balanceGrid.add(netWorthValue, 1, row++);
                    
                    // Footer
                    balanceGrid.add(new Label(" "), 0, row++);
                    Label footerLabel = new Label("As of: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    footerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                    balanceGrid.add(footerLabel, 0, row);
                    
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading balance sheet data: " + ex.getMessage());
            }
        };

        // Refresh button action
        refreshBtn.setOnAction(e -> loadBalanceSheet.run());

        // View detailed balance sheet button action
        viewDetailedBtn.setOnAction(e -> {
            try {
                com.cablemanagement.views.BalanceSheetView.showBalanceSheet();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error opening detailed balance sheet: " + ex.getMessage());
            }
        });

        // Print button action
        printBtn.setOnAction(e -> {
            try {
                // Generate temporary filename for balance sheet PDF
                String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                                 "BalanceSheet_Summary_" + timestamp + ".pdf";
                
                // Use the BalanceSheetGenerator to create and open PDF for printing
                com.cablemanagement.invoice.BalanceSheetGenerator.generateAndPreviewBalanceSheet(filename);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing balance sheet for printing: " + ex.getMessage());
            }
        });

        // Load initial data
        loadBalanceSheet.run();

        form.getChildren().addAll(heading, actionButtons, errorLabel, balanceGrid);
        return form;
    }

    private static VBox createCustomersReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Customers General Report");

        // Customers report table - maps to Customer table
        TableView<CustomerReport> table = new TableView<>();
        
        TableColumn<CustomerReport, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<CustomerReport, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<CustomerReport, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = "All Customers Report";
            showTableInNewWindow("Customers Report", table, filterInfo);
        });

        // Load data from backend
        try {
            System.out.println("DEBUG: Starting to load customer data...");
            if (config.database != null && config.database.isConnected()) {
                System.out.println("DEBUG: Database is connected, executing query...");
                java.sql.ResultSet rs = config.database.getCustomersReport();
                int count = 0;
                if (rs != null) {
                    System.out.println("DEBUG: ResultSet is not null, processing results...");
                    while (rs.next()) {
                        String customerName = rs.getString("customer_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        System.out.println("DEBUG: Processing customer: " + customerName + ", phone: " + phoneNumber);
                        
                        // Handle null values
                        if (customerName == null) customerName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new CustomerReport(customerName, phoneNumber, address));
                        count++;
                    }
                } else {
                    System.out.println("DEBUG: ResultSet is null!");
                }
                System.out.println("CustomersReport rows loaded: " + count);
                if (count == 0) {
                    errorLabel.setText("No customer data found.");
                }
            } else {
                errorLabel.setText("Database not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading customer data: " + ex.getMessage());
        }

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.ResultSet rs = config.database.getCustomersReport();
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String customerName = rs.getString("customer_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        // Handle null values
                        if (customerName == null) customerName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new CustomerReport(customerName, phoneNumber, address));
                        count++;
                    }
                    System.out.println("CustomersReport rows refreshed: " + count);
                    if (count == 0) {
                        errorLabel.setText("No customer data found.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error refreshing customer data: " + ex.getMessage());
            }
        });

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                exportCustomersReportToPDF(table);
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Customer Name,Phone Number,Address\n");
                
                for (CustomerReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s\n",
                        item.getCustomerName(),
                        item.getPhoneNumber(),
                        item.getAddress()
                    ));
                }
                
                // Show export dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Customers Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, buttons, errorLabel);
        return form;
    }

    private static VBox createSuppliersReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Suppliers General Report");

        // Suppliers report table - maps to Supplier table
        TableView<SupplierReport> table = new TableView<>();
        
        TableColumn<SupplierReport, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<SupplierReport, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<SupplierReport, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to show. Please apply filters first.");
                return;
            }
            
            String filterInfo = "All Suppliers Report";
            showTableInNewWindow("Suppliers Report", table, filterInfo);
        });
        
        table.getColumns().addAll(nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load data from backend
        try {
            if (config.database != null && config.database.isConnected()) {
                java.sql.ResultSet rs = config.database.getSuppliersReport();
                int count = 0;
                while (rs != null && rs.next()) {
                    String supplierName = rs.getString("supplier_name");
                    String phoneNumber = rs.getString("contact_number");
                    String address = rs.getString("address");
                    
                    // Handle null values
                    if (supplierName == null) supplierName = "Unknown";
                    if (phoneNumber == null) phoneNumber = "N/A";
                    if (address == null) address = "N/A";
                    
                    table.getItems().add(new SupplierReport(supplierName, phoneNumber, address));
                    count++;
                }
                System.out.println("SuppliersReport rows loaded: " + count);
                if (count == 0) {
                    errorLabel.setText("No supplier data found.");
                }
            } else {
                errorLabel.setText("Database not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading supplier data: " + ex.getMessage());
        }

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.ResultSet rs = config.database.getSuppliersReport();
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String supplierName = rs.getString("supplier_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        // Handle null values
                        if (supplierName == null) supplierName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new SupplierReport(supplierName, phoneNumber, address));
                        count++;
                    }
                    System.out.println("SuppliersReport rows refreshed: " + count);
                    if (count == 0) {
                        errorLabel.setText("No supplier data found.");
                    }
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error refreshing supplier data: " + ex.getMessage());
            }
        });

        // Refresh button action
        ((Button) buttons.getChildren().get(0)).setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                if (config.database != null && config.database.isConnected()) {
                    java.sql.ResultSet rs = config.database.getSuppliersReport();
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String supplierName = rs.getString("supplier_name");
                        String phoneNumber = rs.getString("contact_number");
                        String address = rs.getString("address");
                        
                        // Handle null values
                        if (supplierName == null) supplierName = "Unknown";
                        if (phoneNumber == null) phoneNumber = "N/A";
                        if (address == null) address = "N/A";
                        
                        table.getItems().add(new SupplierReport(supplierName, phoneNumber, address));
                        count++;
                    }
                    errorLabel.setText("Loaded " + count + " suppliers");
                } else {
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error refreshing supplier data: " + ex.getMessage());
            }
        });

        // Print button action - Export to PDF
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            try {
                exportSuppliersReportToPDF(table);
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error exporting to PDF: " + ex.getMessage());
            }
        });

        // Export button action (CSV export)
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to export. Please apply filters first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Supplier Name,Phone Number,Address\n");
                
                for (SupplierReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s\n",
                        item.getSupplierName(),
                        item.getPhoneNumber(),
                        item.getAddress()
                    ));
                }
                
                // Show export dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Suppliers Report Export");
                alert.setContentText("Export functionality would be implemented here.\n" +
                                   "Report contains " + table.getItems().size() + " records ready for CSV export.\n\n" +
                                   "Sample CSV format:\n" + 
                                   csvContent.toString().substring(0, Math.min(200, csvContent.length())) + "...");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(heading, buttons, errorLabel);
        return form;
    }

    private static VBox createAreaWiseReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Area-Wise Customer/Supplier Report");

        // Filter controls section
        VBox filterSection = new VBox(10);
        filterSection.setStyle("-fx-padding: 15; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: #f8f9fa;");
        
        Label filterLabel = new Label("Report Filters");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Party Type Selection
        HBox partyTypeBox = new HBox(10);
        partyTypeBox.setAlignment(Pos.CENTER_LEFT);
        Label partyTypeLabel = new Label("Party Type:");
        partyTypeLabel.setMinWidth(100);
        ComboBox<String> partyTypeCombo = new ComboBox<>();
        partyTypeCombo.getItems().addAll("Both", "Customer", "Supplier");
        partyTypeCombo.setValue("Both");
        partyTypeCombo.setPrefWidth(150);
        partyTypeBox.getChildren().addAll(partyTypeLabel, partyTypeCombo);
        
        // Area Type Selection
        HBox areaTypeBox = new HBox(10);
        areaTypeBox.setAlignment(Pos.CENTER_LEFT);
        Label areaTypeLabel = new Label("Area Type:");
        areaTypeLabel.setMinWidth(100);
        ComboBox<String> areaTypeCombo = new ComboBox<>();
        areaTypeCombo.getItems().addAll("All", "Province", "District", "Tehsil");
        areaTypeCombo.setValue("All");
        areaTypeCombo.setPrefWidth(150);
        areaTypeBox.getChildren().addAll(areaTypeLabel, areaTypeCombo);
        
        // Area Value Selection (depends on area type)
        HBox areaValueBox = new HBox(10);
        areaValueBox.setAlignment(Pos.CENTER_LEFT);
        Label areaValueLabel = new Label("Select Area:");
        areaValueLabel.setMinWidth(100);
        ComboBox<String> areaValueCombo = new ComboBox<>();
        areaValueCombo.setValue("All");
        areaValueCombo.setPrefWidth(200);
        areaValueBox.getChildren().addAll(areaValueLabel, areaValueCombo);
        
        // Date Range Selection
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        Label fromLabel = new Label("From:");
        fromLabel.setMinWidth(100);
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        fromDatePicker.setPrefWidth(150);
        Label toLabel = new Label("To:");
        toLabel.setMinWidth(50);
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        toDatePicker.setPrefWidth(150);
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker);
        
        // Filter and Reset buttons
        HBox filterButtonsBox = new HBox(10);
        filterButtonsBox.setAlignment(Pos.CENTER_LEFT);
        filterButtonsBox.setPadding(new Insets(10, 0, 0, 0));
        Button filterBtn = createActionButton("Apply Filter");
        Button resetBtn = createActionButton("Reset");
        resetBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        filterButtonsBox.getChildren().addAll(filterBtn, resetBtn);
        
        filterSection.getChildren().addAll(filterLabel, partyTypeBox, areaTypeBox, areaValueBox, dateRangeBox, filterButtonsBox);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Method to populate area value combo based on area type selection
        Runnable populateAreaValues = () -> {
            String areaType = areaTypeCombo.getValue();
            areaValueCombo.getItems().clear();
            areaValueCombo.getItems().add("All");
            
            try {
                switch (areaType) {
                    case "Province":
                        List<String> provinces = config.database.getAllProvinces();
                        areaValueCombo.getItems().addAll(provinces);
                        break;
                    case "District":
                        List<String> districts = config.database.getAllDistricts();
                        areaValueCombo.getItems().addAll(districts);
                        break;
                    case "Tehsil":
                        List<String> tehsils = config.database.getAllTehsils();
                        areaValueCombo.getItems().addAll(tehsils);
                        break;
                }
                areaValueCombo.setValue("All");
            } catch (Exception e) {
                errorLabel.setText("Error loading area values: " + e.getMessage());
            }
        };

        // Event handlers
        areaTypeCombo.setOnAction(e -> populateAreaValues.run());
        
        // Apply Filter button action - opens new window with report data
        filterBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                showAreaWiseReportInNewWindow(partyTypeCombo, areaTypeCombo, areaValueCombo, fromDatePicker, toDatePicker);
            } catch (Exception ex) {
                errorLabel.setText("Error opening report window: " + ex.getMessage());
            }
        });
        
        resetBtn.setOnAction(e -> {
            partyTypeCombo.setValue("Both");
            areaTypeCombo.setValue("All");
            areaValueCombo.getItems().clear();
            areaValueCombo.getItems().add("All");
            areaValueCombo.setValue("All");
            fromDatePicker.setValue(LocalDate.now().minusDays(30));
            toDatePicker.setValue(LocalDate.now());
            errorLabel.setText("");
        });

        // Initial population of area values
        populateAreaValues.run();

        // Area Summary Section - Separate filter for simple area totals
        VBox areaSummarySection = new VBox(10);
        areaSummarySection.setStyle("-fx-padding: 15; -fx-border-color: #4CAF50; -fx-border-radius: 5; -fx-background-color: #f1f8e9;");
        
        Label summaryLabel = new Label("Area Summary Report");
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2E7D32;");
        
        // Summary Type Selection
        HBox summaryTypeBox = new HBox(10);
        summaryTypeBox.setAlignment(Pos.CENTER_LEFT);
        Label summaryTypeLabel = new Label("Summary By:");
        summaryTypeLabel.setMinWidth(100);
        ComboBox<String> summaryTypeCombo = new ComboBox<>();
        summaryTypeCombo.getItems().addAll("Tehsil", "District", "Province");
        summaryTypeCombo.setValue("Tehsil");
        summaryTypeCombo.setPrefWidth(150);
        summaryTypeBox.getChildren().addAll(summaryTypeLabel, summaryTypeCombo);
        
        // Summary Party Type Selection
        HBox summaryPartyTypeBox = new HBox(10);
        summaryPartyTypeBox.setAlignment(Pos.CENTER_LEFT);
        Label summaryPartyTypeLabel = new Label("Party Type:");
        summaryPartyTypeLabel.setMinWidth(100);
        ComboBox<String> summaryPartyTypeCombo = new ComboBox<>();
        summaryPartyTypeCombo.getItems().addAll("Customer", "Supplier");
        summaryPartyTypeCombo.setValue("Customer");
        summaryPartyTypeCombo.setPrefWidth(150);
        summaryPartyTypeBox.getChildren().addAll(summaryPartyTypeLabel, summaryPartyTypeCombo);
        
        // Summary Date Range Selection
        HBox summaryDateRangeBox = new HBox(10);
        summaryDateRangeBox.setAlignment(Pos.CENTER_LEFT);
        Label summaryFromLabel = new Label("From:");
        summaryFromLabel.setMinWidth(100);
        DatePicker summaryFromDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        summaryFromDatePicker.setPrefWidth(150);
        Label summaryToLabel = new Label("To:");
        summaryToLabel.setMinWidth(50);
        DatePicker summaryToDatePicker = new DatePicker(LocalDate.now());
        summaryToDatePicker.setPrefWidth(150);
        summaryDateRangeBox.getChildren().addAll(summaryFromLabel, summaryFromDatePicker, summaryToLabel, summaryToDatePicker);
        
        // Summary Filter and Reset buttons
        HBox summaryButtonsBox = new HBox(10);
        summaryButtonsBox.setAlignment(Pos.CENTER_LEFT);
        summaryButtonsBox.setPadding(new Insets(10, 0, 0, 0));
        Button summaryFilterBtn = createActionButton("Generate Summary");
        summaryFilterBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        Button summaryResetBtn = createActionButton("Reset");
        summaryResetBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        summaryButtonsBox.getChildren().addAll(summaryFilterBtn, summaryResetBtn);
        
        areaSummarySection.getChildren().addAll(summaryLabel, summaryTypeBox, summaryPartyTypeBox, summaryDateRangeBox, summaryButtonsBox);
        
        // Error label for summary feedback
        Label summaryErrorLabel = new Label("");
        summaryErrorLabel.setStyle("-fx-text-fill: red;");
        
        // Summary Filter button action - opens new window with simple area summary
        summaryFilterBtn.setOnAction(e -> {
            summaryErrorLabel.setText("");
            try {
                showSimpleAreaSummaryInNewWindow(summaryTypeCombo, summaryPartyTypeCombo, summaryFromDatePicker, summaryToDatePicker);
            } catch (Exception ex) {
                summaryErrorLabel.setText("Error opening summary window: " + ex.getMessage());
            }
        });
        
        summaryResetBtn.setOnAction(e -> {
            summaryTypeCombo.setValue("Tehsil");
            summaryPartyTypeCombo.setValue("Customer");
            summaryFromDatePicker.setValue(LocalDate.now().minusDays(30));
            summaryToDatePicker.setValue(LocalDate.now());
            summaryErrorLabel.setText("");
        });

        form.getChildren().addAll(heading, filterSection, errorLabel, areaSummarySection, summaryErrorLabel);
        return form;
    }

    private static VBox createBrandSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Brand-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Brand sales report table - maps to Sales_Invoice_Item joined with Brand and ProductionStock
        TableView<BrandSalesReport> table = new TableView<>();
        
        TableColumn<BrandSalesReport, String> salesmanCol = new TableColumn<>("Salesman");
        salesmanCol.setCellValueFactory(new PropertyValueFactory<>("salesmanName"));
        
        TableColumn<BrandSalesReport, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        
        TableColumn<BrandSalesReport, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        
        TableColumn<BrandSalesReport, String> salesCol = new TableColumn<>("Total Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSale"));
        
        table.getColumns().addAll(salesmanCol, brandCol, quantityCol, salesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons with Show Report functionality
        HBox buttons = createReportActionButtonsWithShowReport(() -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data to display. Please run the filter first.");
                return;
            }
            String filterInfo = String.format("Brand-Wise Sales Report | From: %s | To: %s", 
                fromDatePicker.getValue().toString(), 
                toDatePicker.getValue().toString());
            showTableInNewWindow("Brand-Wise Sales Report", table, filterInfo);
        });

        // Load data from backend
        filterBtn.setOnAction(e -> {
            table.getItems().clear();
            errorLabel.setText("");
            try {
                System.out.println("DEBUG: Brand Sales Report - Loading data...");
                if (config.database != null && config.database.isConnected()) {
                    java.sql.Date from = java.sql.Date.valueOf(fromDatePicker.getValue());
                    java.sql.Date to = java.sql.Date.valueOf(toDatePicker.getValue());
                    
                    System.out.println("DEBUG: Date range: " + from + " to " + to);
                    
                    java.sql.ResultSet rs = config.database.getBrandSalesReport(from, to);
                    int count = 0;
                    while (rs != null && rs.next()) {
                        String brandName = rs.getString("brand_name");
                        String totalQuantity = String.format("%.2f", rs.getDouble("total_quantity"));
                        String totalSales = String.format("%.2f", rs.getDouble("total_sales"));
                        String salesmanName = rs.getString("salesman_name");
                        
                        System.out.println("DEBUG: Processing record - Brand: " + brandName + ", Quantity: " + totalQuantity + ", Sales: " + totalSales);
                        
                        // Handle null values
                        if (brandName == null) brandName = "Unknown Brand";
                        if (salesmanName == null) salesmanName = "N/A";
                        
                        table.getItems().add(new BrandSalesReport(salesmanName, brandName, totalQuantity, totalSales));
                        count++;
                    }
                    
                    System.out.println("BrandSalesReport rows loaded: " + count);
                    if (count == 0) {
                        errorLabel.setText("No brand sales data found for selected date range.");
                    }
                } else {
                    System.out.println("DEBUG: Database is null or not connected");
                    errorLabel.setText("Database not connected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error loading brand sales data: " + ex.getMessage());
            }
        });

        // Print button action
        ((Button) buttons.getChildren().get(1)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data available to print. Please load data first.");
                return;
            }
            try {
                // Create a print-friendly representation
                StringBuilder printContent = new StringBuilder();
                printContent.append("Brand-Wise Sales Report\n");
                printContent.append("Date Range: ").append(fromDatePicker.getValue()).append(" to ").append(toDatePicker.getValue()).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n\n");
                printContent.append(String.format("%-20s %-20s %-15s %-15s\n", "Salesman", "Brand", "Quantity", "Total Sales"));
                printContent.append("=".repeat(70)).append("\n");
                
                for (BrandSalesReport item : table.getItems()) {
                    printContent.append(String.format("%-20s %-20s %-15s %-15s\n", 
                        item.getSalesmanName(), 
                        item.getBrandName(), 
                        item.getTotalQuantity(), 
                        item.getTotalSale()));
                }
                
                // For now, just show print dialog (actual printing implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Report");
                alert.setHeaderText("Brand Sales Report");
                alert.setContentText("Print functionality would be implemented here.\nReport contains " + table.getItems().size() + " records.");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for printing: " + ex.getMessage());
            }
        });

        // Export button action
        ((Button) buttons.getChildren().get(2)).setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                errorLabel.setText("No data available to export. Please load data first.");
                return;
            }
            try {
                // Create CSV export content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Salesman,Brand,Quantity,Total Sales\n");
                
                for (BrandSalesReport item : table.getItems()) {
                    csvContent.append(String.format("%s,%s,%s,%s\n", 
                        item.getSalesmanName(), 
                        item.getBrandName(), 
                        item.getTotalQuantity(), 
                        item.getTotalSale()));
                }
                
                // For now, just show export dialog (actual file saving implementation depends on requirements)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Report");
                alert.setHeaderText("Brand Sales Report Export");
                alert.setContentText("Export functionality would be implemented here.\nReport contains " + table.getItems().size() + " records ready for CSV export.");
                alert.showAndWait();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error preparing report for export: " + ex.getMessage());
            }
        });

        // Optionally, trigger filter on load
        filterBtn.fire();

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createBrandProfitReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Brand-Wise Profit Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Brand profit report table - maps to View_Brand_Wise_Profit_Report
        TableView<BrandProfitReport> table = new TableView<>();
        
        TableColumn<BrandProfitReport, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        
        TableColumn<BrandProfitReport, String> salesCol = new TableColumn<>("Total Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        
        TableColumn<BrandProfitReport, String> costCol = new TableColumn<>("Total Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        TableColumn<BrandProfitReport, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
        
        table.getColumns().addAll(brandCol, salesCol, costCol, profitCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons with Show Report functionality
        HBox buttons = createReportActionButtonsWithShowReport(() -> showTableInNewWindow("Brand-Wise Profit Report", table, ""));
        
        // Sample data - in real app, fetch from View_Brand_Wise_Profit_Report
        ObservableList<BrandProfitReport> data = FXCollections.observableArrayList(
            new BrandProfitReport("PowerFlex", "37500.00", "25000.00", "12500.00"),
            new BrandProfitReport("SafeWire", "36000.00", "24000.00", "12000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createCustomerSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Customer-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Customer sales report table - maps to View_Customer_Wise_Sales_Report
        TableView<CustomerSalesReport> table = new TableView<>();
        
        TableColumn<CustomerSalesReport, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<CustomerSalesReport, String> invoicesCol = new TableColumn<>("Invoices");
        invoicesCol.setCellValueFactory(new PropertyValueFactory<>("totalInvoices"));
        
        TableColumn<CustomerSalesReport, String> salesCol = new TableColumn<>("Total Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        
        table.getColumns().addAll(customerCol, invoicesCol, salesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons with Show Report functionality
        HBox buttons = createReportActionButtonsWithShowReport(() -> showTableInNewWindow("Customer-Wise Sales Report", table, ""));
        
        // Sample data - in real app, fetch from View_Customer_Wise_Sales_Report
        ObservableList<CustomerSalesReport> data = FXCollections.observableArrayList(
            new CustomerSalesReport("Ali Traders", "5", "75000.00"),
            new CustomerSalesReport("Pak Electric House", "3", "50000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel);
        return form;
    }

    private static VBox createSupplierSalesReport() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Supplier-Wise Sales Report");

        // Date range filters
        HBox dateRangeBox = createDateRangeFilter();

        // Supplier sales report table - maps to View_Supplier_Wise_Sales_Report
        TableView<SupplierSalesReport> table = new TableView<>();
        
        TableColumn<SupplierSalesReport, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<SupplierSalesReport, String> invoicesCol = new TableColumn<>("Invoices");
        invoicesCol.setCellValueFactory(new PropertyValueFactory<>("totalInvoices"));
        
        TableColumn<SupplierSalesReport, String> suppliedCol = new TableColumn<>("Total Supplied");
        suppliedCol.setCellValueFactory(new PropertyValueFactory<>("totalSupplied"));
        
        table.getColumns().addAll(supplierCol, invoicesCol, suppliedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Error label for feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        // Action buttons with Show Report functionality
        HBox buttons = createReportActionButtonsWithShowReport(() -> showTableInNewWindow("Supplier-Wise Sales Report", table, ""));
        
        // Sample data - in real app, fetch from View_Supplier_Wise_Sales_Report
        ObservableList<SupplierSalesReport> data = FXCollections.observableArrayList(
            new SupplierSalesReport("RawMetals Pvt Ltd", "10", "250000.00"),
            new SupplierSalesReport("Insulation Depot", "8", "180000.00")
        );
        table.setItems(data);

        form.getChildren().addAll(heading, dateRangeBox, buttons, errorLabel);
        return form;
    }

    // private static VBox createAttendanceReport() {
    //     VBox form = new VBox(15);
    //     form.setPadding(new Insets(20));
    //     form.getStyleClass().add("form-container");

    //     Label heading = createHeading("Attendance Report");

    //     // Date range filters
    //     HBox dateRangeBox = createDateRangeFilter();

    //     // Action buttons
    //     HBox buttons = createReportActionButtons();

    //     // Attendance report table - maps to View_Attendance_Report
    //     TableView<AttendanceReport> table = new TableView<>();
        
    //     TableColumn<AttendanceReport, String> nameCol = new TableColumn<>("Employee");
    //     nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
    //     TableColumn<AttendanceReport, String> dateCol = new TableColumn<>("Date");
    //     dateCol.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        
    //     TableColumn<AttendanceReport, String> statusCol = new TableColumn<>("Status");
    //     statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
    //     TableColumn<AttendanceReport, String> hoursCol = new TableColumn<>("Hours");
    //     hoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        
    //     table.getColumns().addAll(nameCol, dateCol, statusCol, hoursCol);
    //     table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
    //     // Sample data - in real app, fetch from View_Attendance_Report
    //     ObservableList<AttendanceReport> data = FXCollections.observableArrayList(
    //         new AttendanceReport("Zahid Khan", "2025-07-01", "present", "8"),
    //         new AttendanceReport("Zahid Khan", "2025-07-02", "present", "8"),
    //         new AttendanceReport("Faisal Mehmood", "2025-07-01", "absent", "0")
    //     );
    //     table.setItems(data);

    //     form.getChildren().addAll(heading, dateRangeBox, buttons, table);
    //     return form;
    // }

    // Helper methods
    private static HBox createDateRangeFilter() {
        HBox dateRangeBox = new HBox(10);
        Label fromLabel = new Label("From:");
        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        Label toLabel = new Label("To:");
        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setValue(LocalDate.now());
        Button filterBtn = createActionButton("Filter");
        dateRangeBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterBtn);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        return dateRangeBox;
    }

    // Method to show area-wise report in a new window
    private static void showAreaWiseReportInNewWindow(ComboBox<String> partyTypeCombo, 
                                                     ComboBox<String> areaTypeCombo, 
                                                     ComboBox<String> areaValueCombo,
                                                     DatePicker fromDatePicker,
                                                     DatePicker toDatePicker) {
        // Validate inputs
        if (partyTypeCombo.getValue() == null || areaTypeCombo.getValue() == null || areaValueCombo.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Selection");
            alert.setHeaderText("Please select all filter options");
            alert.setContentText("Make sure to select Party Type, Area Type, and Area values before applying the filter.");
            alert.showAndWait();
            return;
        }
        
        // Validate date range
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Date Range");
            alert.setHeaderText("Please select valid dates");
            alert.setContentText("Both From and To dates are required.");
            alert.showAndWait();
            return;
        }
        
        if (fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Date Range");
            alert.setHeaderText("Invalid date range");
            alert.setContentText("From date cannot be after To date.");
            alert.showAndWait();
            return;
        }
        
        Stage reportStage = new Stage();
        reportStage.setTitle("Area-Wise Customer/Supplier Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true); // Allow maximizing
        reportStage.setMaximized(true); // Start maximized
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");
        
        // Title
        Label titleLabel = new Label("Area-Wise Customer/Supplier Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        // Filter info
        String partyType = partyTypeCombo.getValue();
        String areaType = areaTypeCombo.getValue();
        String areaValue = areaValueCombo.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        Label filterInfoLabel = new Label("Filters: Party Type: " + partyType + 
                                        ", Area Type: " + areaType + 
                                        ", Selected Area: " + areaValue +
                                        ", Date Range: " + fromDate + " to " + toDate);
        filterInfoLabel.setFont(Font.font("Segoe UI", 14));
        filterInfoLabel.setStyle("-fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
        
        // Report content area
        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        // reportScrollPane.setPrefHeight(500);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);
        
        // Generate the area-wise report table
        try {
            generateAreaWiseReportTable(reportContent, partyType, areaType, areaValue, fromDate, toDate);
        } catch (Exception ex) {
            reportContent.getChildren().add(new Label("Error generating report: " + ex.getMessage()));
        }
        
        // Control buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            try {
                generateAreaWiseReportTable(reportContent, partyType, areaType, areaValue, fromDate, toDate);
            } catch (Exception ex) {
                reportContent.getChildren().add(new Label("Error refreshing report: " + ex.getMessage()));
            }
        });
        
        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());
        
        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        
        mainLayout.getChildren().addAll(titleLabel, filterInfoLabel, reportScrollPane, buttonBox);
        
        Scene scene = new Scene(mainLayout, 1200, 700);
        
        // Apply the same stylesheet as the main application
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }
        
        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to generate area-wise report table for the new window
    private static void generateAreaWiseReportTable(VBox reportContent, String partyType, String areaType, String areaValue, LocalDate fromDate, LocalDate toDate) {
        try {
            // Show loading message
            Label loadingLabel = new Label("Loading report data...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            reportContent.getChildren().add(loadingLabel);
            
            // Create table for area-wise data
            TableView<AreaWiseReport> table = new TableView<>();
            table.getStyleClass().add("table-view");
            
            TableColumn<AreaWiseReport, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(new PropertyValueFactory<>("partyType"));
            typeCol.setPrefWidth(100);
            
            TableColumn<AreaWiseReport, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(200);
            
            TableColumn<AreaWiseReport, String> totalSaleCol = new TableColumn<>("Total Purchase");
            totalSaleCol.setCellValueFactory(new PropertyValueFactory<>("totalSale"));
            totalSaleCol.setPrefWidth(120);
            
            TableColumn<AreaWiseReport, String> totalDiscountCol = new TableColumn<>("Total Discount");
            totalDiscountCol.setCellValueFactory(new PropertyValueFactory<>("totalDiscount"));
            totalDiscountCol.setPrefWidth(120);
            
            TableColumn<AreaWiseReport, String> totalPaidCol = new TableColumn<>("Total Payment");
            totalPaidCol.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
            totalPaidCol.setPrefWidth(120);
            
            TableColumn<AreaWiseReport, String> totalReturnCol = new TableColumn<>("Total Return");
            totalReturnCol.setCellValueFactory(new PropertyValueFactory<>("totalReturn"));
            totalReturnCol.setPrefWidth(120);
            
            TableColumn<AreaWiseReport, String> totalBalanceCol = new TableColumn<>("Current Balance");
            totalBalanceCol.setCellValueFactory(new PropertyValueFactory<>("totalBalance"));
            totalBalanceCol.setPrefWidth(120);
            
            table.getColumns().addAll(typeCol, nameCol, totalSaleCol, totalDiscountCol, totalPaidCol, totalReturnCol, totalBalanceCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Load data
            if (config.database != null && config.database.isConnected()) {
                String lowerAreaType = areaType.toLowerCase();
                
                // Convert LocalDate to java.sql.Date for database queries
                java.sql.Date sqlFromDate = java.sql.Date.valueOf(fromDate);
                java.sql.Date sqlToDate = java.sql.Date.valueOf(toDate);
                
                System.out.println("DEBUG: ReportsContent - Date range: " + fromDate + " to " + toDate);
                System.out.println("DEBUG: ReportsContent - SQL Date range: " + sqlFromDate + " to " + sqlToDate);
                
                // Get data and totals using the same method as individual ledgers to ensure consistency
                AreaWiseReportResult result = generateAreaWiseDataFromLedgers(table, partyType, fromDate, toDate, lowerAreaType, areaValue);
                int count = result.count;
                
                // Remove loading message
                reportContent.getChildren().clear();
                
                if (count == 0) {
                    Label noDataLabel = new Label("No transactions found for the selected date range and criteria.");
                    noDataLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                    noDataLabel.setStyle("-fx-text-fill: #e74c3c;");
                    reportContent.getChildren().add(noDataLabel);
                    return;
                }
                
                Label resultLabel = new Label("Area-Wise Report (Date Filtered) - Records with Transactions: " + count);
                resultLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                resultLabel.setStyle("-fx-text-fill: #1a1a1a;");
                
                Label noteLabel = new Label("Note: Only showing customers/suppliers with transactions in the selected date range. Current Balance column shows overall balance, not date-filtered balance.");
                noteLabel.setFont(Font.font("Segoe UI", 12));
                noteLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
                
                // Create action buttons for the report
                HBox actionButtonsBox = new HBox(10);
                actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);
                actionButtonsBox.setPadding(new Insets(10, 0, 10, 0));
                
                Button exportPdfBtn = createActionButton("Export to PDF");
                exportPdfBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16;");
                
                actionButtonsBox.getChildren().add(exportPdfBtn);
                
                // Export PDF button action
                exportPdfBtn.setOnAction(e -> {
                    try {
                        exportAreaWiseReportToPDF(table, partyType, areaType, areaValue, fromDate, toDate);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Error");
                        alert.setHeaderText("Error exporting report");
                        alert.setContentText("Failed to export the report to PDF: " + ex.getMessage());
                        alert.showAndWait();
                    }
                });
                
                // Create grand totals display below the table
                HBox totalsBox = new HBox();
                totalsBox.setAlignment(Pos.CENTER);
                totalsBox.setPadding(new Insets(15, 0, 10, 0));
                totalsBox.setStyle("-fx-background-color: #d4edda; -fx-border-color: #28a745; -fx-border-width: 2; -fx-border-radius: 5;");
                
                VBox totalsContent = new VBox(5);
                totalsContent.setAlignment(Pos.CENTER);
                
                Label totalsTitle = new Label("GRAND TOTALS");
                totalsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                totalsTitle.setStyle("-fx-text-fill: #155724;");
                
                HBox totalsValues = new HBox(40);
                totalsValues.setAlignment(Pos.CENTER);
                
                Label purchaseTotal = new Label("Purchase: " + result.formattedTotalPurchase);
                purchaseTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                purchaseTotal.setStyle("-fx-text-fill: #155724;");
                
                Label discountTotal = new Label("Discount: " + result.formattedTotalDiscount);
                discountTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                discountTotal.setStyle("-fx-text-fill: #155724;");
                
                Label paymentTotal = new Label("Payment: " + result.formattedTotalPayment);
                paymentTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                paymentTotal.setStyle("-fx-text-fill: #155724;");
                
                Label returnTotal = new Label("Return: " + result.formattedTotalReturn);
                returnTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                returnTotal.setStyle("-fx-text-fill: #155724;");
                
                Label balanceTotal = new Label("Balance: " + result.formattedTotalBalance);
                balanceTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                balanceTotal.setStyle("-fx-text-fill: #155724;");
                
                totalsValues.getChildren().addAll(purchaseTotal, discountTotal, paymentTotal, returnTotal, balanceTotal);
                totalsContent.getChildren().addAll(totalsTitle, totalsValues);
                totalsBox.getChildren().add(totalsContent);
                
                reportContent.getChildren().addAll(resultLabel, noteLabel, actionButtonsBox, table, totalsBox);
                
            } else {
                reportContent.getChildren().clear();
                Label errorLabel = new Label("Database not connected.");
                errorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                reportContent.getChildren().add(errorLabel);
            }
            
        } catch (Exception e) {
            reportContent.getChildren().clear();
            Label errorLabel = new Label("Error generating area-wise report: " + e.getMessage());
            errorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            reportContent.getChildren().add(errorLabel);
            e.printStackTrace();
        }
    }

    // =================================================================
    // NEW WINDOW METHODS FOR ALL REPORTS
    // =================================================================

    // Method to show Purchase Report in a new window
    private static void showPurchaseReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Purchase Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Purchase Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        // Generate the purchase report content
        reportContent.getChildren().add(createPurchaseReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createPurchaseReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Sales Report in a new window
    private static void showSalesReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Sales Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Sales Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createSalesReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createSalesReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Return Purchase Report in a new window
    private static void showReturnPurchaseReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Return Purchase Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Return Purchase Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createReturnPurchaseReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createReturnPurchaseReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Return Sales Report in a new window
    private static void showReturnSalesReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Return Sales Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Return Sales Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createReturnSalesReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createReturnSalesReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Bank Transfer Report in a new window
    private static void showBankTransferReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Bank Transfer Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Bank Transfer Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createBankTransferReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createBankTransferReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Profit Report in a new window
    private static void showProfitReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Profit Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Profit Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createProfitReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createProfitReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Balance Sheet in a new window
    private static void showBalanceSheetInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Balance Sheet");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Balance Sheet");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createBalanceSheet());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createBalanceSheet());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Customers Report in a new window
    private static void showCustomersReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Customers Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Customers Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createCustomersReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createCustomersReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Suppliers Report in a new window
    private static void showSuppliersReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Suppliers Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Suppliers Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createSuppliersReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createSuppliersReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // Method to show Brand Sales Report in a new window
    private static void showBrandSalesReportInNewWindow() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Brand Sales Report");
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label("Brand Sales Report");
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);

        reportContent.getChildren().add(createBrandSalesReport());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setOnAction(e -> {
            reportContent.getChildren().clear();
            reportContent.getChildren().add(createBrandSalesReport());
        });

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        mainLayout.getChildren().addAll(titleLabel, reportScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    // =================================================================
    // END OF NEW WINDOW METHODS
    // =================================================================

    private static HBox createReportActionButtons() {
        HBox buttons = new HBox(10);
        Button refreshBtn = createActionButton("Refresh");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        Button exportBtn = createActionButton("Export");
        exportBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Red for export buttons
        buttons.getChildren().addAll(refreshBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        return buttons;
    }

    private static HBox createReportActionButtonsWithShowReport(Runnable showReportAction) {
        HBox buttons = new HBox(10);
        Button showReportBtn = createActionButton("Show Report");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        Button exportBtn = createActionButton("Export");
        exportBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Red for export buttons
        
        // Set the custom action for the show report button
        showReportBtn.setOnAction(e -> showReportAction.run());
        
        buttons.getChildren().addAll(showReportBtn, printBtn, exportBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        return buttons;
    }

    // Generic method to show a table in a new window
    private static void showTableInNewWindow(String reportTitle, TableView<?> table, String filterInfo) {
        Stage reportStage = new Stage();
        reportStage.setTitle(reportTitle);
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);
        reportStage.setMaximized(true);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("form-container");

        Label titleLabel = new Label(reportTitle);
        titleLabel.getStyleClass().add("form-heading");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        // Show filter information if provided
        if (filterInfo != null && !filterInfo.isEmpty()) {
            Label filterInfoLabel = new Label(filterInfo);
            filterInfoLabel.setFont(Font.font("Segoe UI", 14));
            filterInfoLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
            mainLayout.getChildren().add(filterInfoLabel);
        }

        // Create a new table with the same structure but copy the data
        // Create a new table with raw types to avoid type conflicts
        @SuppressWarnings({"unchecked", "rawtypes"})
        TableView newTable = new TableView();
        
        // Copy columns
        for (TableColumn column : table.getColumns()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            TableColumn newColumn = new TableColumn(column.getText());
            newColumn.setPrefWidth(column.getPrefWidth());
            newColumn.setCellValueFactory(column.getCellValueFactory());
            newTable.getColumns().add(newColumn);
        }
        
        // Copy data
        @SuppressWarnings({"unchecked", "rawtypes"})
        ObservableList items = table.getItems();
        newTable.setItems(items);
        newTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ScrollPane tableScrollPane = new ScrollPane(newTable);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setFitToHeight(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button closeBtn = createActionButton("Close");
        closeBtn.setOnAction(e -> reportStage.close());

        buttonBox.getChildren().add(closeBtn);
        mainLayout.getChildren().addAll(titleLabel, tableScrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    private static void addSummaryItem(GridPane grid, int row, String label, String value) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nameLabel, 0, row);
        
        Label valueLabel = new Label(value);
        grid.add(valueLabel, 1, row);
    }

    private static void addBalanceSheetItem(GridPane grid, int row, String label, String value) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nameLabel, 0, row);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-weight: bold;");
        grid.add(valueLabel, 1, row);
    }

    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
    }

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    // Model classes for reports
    public static class PurchaseReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String supplierName;
        private final String totalAmount;
        private final String discountAmount;
        private final String paidAmount;

        public PurchaseReport(String invoiceNumber, String invoiceDate, String supplierName, 
                            String totalAmount, String discountAmount, String paidAmount) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.supplierName = supplierName;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.paidAmount = paidAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getSupplierName() { return supplierName; }
        public String getTotalAmount() { return totalAmount; }
        public String getDiscountAmount() { return discountAmount; }
        public String getPaidAmount() { return paidAmount; }
    }

    public static class SalesReport {
        private final String invoiceNumber;
        private final String salesDate;
        private final String customerName;
        private final String totalAmount;
        private final String discountAmount;
        private final String paidAmount;

        public SalesReport(String invoiceNumber, String salesDate, String customerName, 
                         String totalAmount, String discountAmount, String paidAmount) {
            this.invoiceNumber = invoiceNumber;
            this.salesDate = salesDate;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.paidAmount = paidAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getSalesDate() { return salesDate; }
        public String getCustomerName() { return customerName; }
        public String getTotalAmount() { return totalAmount; }
        public String getDiscountAmount() { return discountAmount; }
        public String getPaidAmount() { return paidAmount; }
    }

    public static class ReturnPurchaseReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String supplierName;
        private final String totalAmount;
        private final String discountAmount;
        private final String paidAmount;

        public ReturnPurchaseReport(String invoiceNumber, String invoiceDate, String supplierName, 
                                  String totalAmount, String discountAmount, String paidAmount) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.supplierName = supplierName;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.paidAmount = paidAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getSupplierName() { return supplierName; }
        public String getTotalAmount() { return totalAmount; }
        public String getDiscountAmount() { return discountAmount; }
        public String getPaidAmount() { return paidAmount; }
    }

    public static class ReturnSalesReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String customerName;
        private final String totalAmount;

        public ReturnSalesReport(String invoiceNumber, String invoiceDate, 
                               String customerName, String totalAmount) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getCustomerName() { return customerName; }
        public String getTotalAmount() { return totalAmount; }
    }

    public static class BankTransferReport {
        private final String transactionDate;
        private final String fromBank;
        private final String toBank;
        private final String amount;

        public BankTransferReport(String transactionDate, String fromBank, 
                                String toBank, String amount) {
            this.transactionDate = transactionDate;
            this.fromBank = fromBank;
            this.toBank = toBank;
            this.amount = amount;
        }

        public String getTransactionDate() { return transactionDate; }
        public String getFromBank() { return fromBank; }
        public String getToBank() { return toBank; }
        public String getAmount() { return amount; }
    }

    public static class ProfitReport {
        private final String invoiceNumber;
        private final String invoiceDate;
        private final String saleAmount;
        private final String costAmount;
        private final String profit;

        public ProfitReport(String invoiceNumber, String invoiceDate, 
                           String saleAmount, String costAmount, String profit) {
            this.invoiceNumber = invoiceNumber;
            this.invoiceDate = invoiceDate;
            this.saleAmount = saleAmount;
            this.costAmount = costAmount;
            this.profit = profit;
        }

        public String getInvoiceNumber() { return invoiceNumber; }
        public String getInvoiceDate() { return invoiceDate; }
        public String getSaleAmount() { return saleAmount; }
        public String getCostAmount() { return costAmount; }
        public String getProfit() { return profit; }
    }

    public static class CustomerReport {
        private final String customerName;
        private final String phoneNumber;
        private final String address;

        public CustomerReport(String customerName, String phoneNumber, String address) {
            this.customerName = customerName;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        public String getCustomerName() { return customerName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getAddress() { return address; }
    }

    public static class SupplierReport {
        private final String supplierName;
        private final String phoneNumber;
        private final String address;

        public SupplierReport(String supplierName, String phoneNumber, String address) {
            this.supplierName = supplierName;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        public String getSupplierName() { return supplierName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getAddress() { return address; }
    }

    // Helper class to hold area-wise report result with totals
    private static class AreaWiseReportResult {
        int count;
        String formattedTotalPurchase;
        String formattedTotalDiscount;
        String formattedTotalPayment;
        String formattedTotalReturn;
        String formattedTotalBalance;
        
        public AreaWiseReportResult(int count, String formattedTotalPurchase, String formattedTotalDiscount,
                                   String formattedTotalPayment, String formattedTotalReturn, String formattedTotalBalance) {
            this.count = count;
            this.formattedTotalPurchase = formattedTotalPurchase;
            this.formattedTotalDiscount = formattedTotalDiscount;
            this.formattedTotalPayment = formattedTotalPayment;
            this.formattedTotalReturn = formattedTotalReturn;
            this.formattedTotalBalance = formattedTotalBalance;
        }
    }

    // Method to generate area-wise data using the same ledger methods as individual customer/supplier ledgers
    private static AreaWiseReportResult generateAreaWiseDataFromLedgers(TableView<AreaWiseReport> table, String partyType, 
                                                      LocalDate fromDate, LocalDate toDate, String areaType, String areaValue) {
        int count = 0;
        
        // Variables to track grand totals
        double grandTotalPurchase = 0.0;
        double grandTotalDiscount = 0.0;
        double grandTotalPayment = 0.0;
        double grandTotalReturn = 0.0;
        double grandTotalBalance = 0.0;
        
        try {
            System.out.println("DEBUG: Generating area-wise data from ledgers for " + partyType);
            
            // Get all customers and suppliers based on area filter
            String partiesQuery = "";
            
            if (partyType.equals("Customer") || partyType.equals("Both")) {
                if (areaType.equals("all") || areaValue.equals("All")) {
                    partiesQuery = "SELECT customer_name FROM Customer";
                } else {
                    switch (areaType.toLowerCase()) {
                        case "province":
                            partiesQuery = "SELECT c.customer_name FROM Customer c " +
                                         "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                                         "LEFT JOIN District d ON t.district_id = d.district_id " +
                                         "LEFT JOIN Province p ON d.province_id = p.province_id " +
                                         "WHERE p.province_name = ?";
                            break;
                        case "district":
                            partiesQuery = "SELECT c.customer_name FROM Customer c " +
                                         "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                                         "LEFT JOIN District d ON t.district_id = d.district_id " +
                                         "WHERE d.district_name = ?";
                            break;
                        case "tehsil":
                            partiesQuery = "SELECT c.customer_name FROM Customer c " +
                                         "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                                         "WHERE t.tehsil_name = ?";
                            break;
                    }
                }
                
                java.sql.Connection conn = config.database.getConnection();
                java.sql.PreparedStatement stmt;
                if (areaType.equals("all") || areaValue.equals("All")) {
                    stmt = conn.prepareStatement(partiesQuery);
                } else {
                    stmt = conn.prepareStatement(partiesQuery);
                    stmt.setString(1, areaValue);
                }
                
                java.sql.ResultSet customersResult = stmt.executeQuery();
                while (customersResult.next()) {
                    String customerName = customersResult.getString("customer_name");
                    
                    // Get customer ledger data using the same method as AccountsContent
                    List<Object[]> ledgerData;
                    if (fromDate != null && toDate != null) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String startDate = fromDate.format(formatter);
                        String endDate = toDate.format(formatter);
                        ledgerData = config.database.getCustomerLedgerByDateRange(customerName, startDate, endDate);
                    } else {
                        ledgerData = config.database.getCustomerLedger(customerName);
                    }
                    
                    // Calculate totals using the same logic as AccountsContent
                    double totalSale = 0.0;
                    double totalDiscount = 0.0;
                    double totalPayment = 0.0;
                    double totalReturn = 0.0;
                    double currentBalance = 0.0;
                    
                    for (Object[] row : ledgerData) {
                        if (row.length >= 12) {
                            totalSale += (Double) row[5];      // Total Bill column
                            totalDiscount += (Double) row[6] + (Double) row[7]; // Discount + Other Discount columns
                            totalPayment += (Double) row[9];   // Payment column
                            totalReturn += (Double) row[10];    // Return Amount column
                        }
                    }
                    
                    // Get actual current balance using the same method as customer ledger
                    // (Customer.balance field is not updated, so we need to calculate from transactions)
                    currentBalance = config.database.getCustomerCurrentBalance(customerName);
                    
                    // Only add customers with transactions in the date range
                    if (totalSale > 0 || totalPayment > 0 || totalReturn > 0) {
                        // Add to grand totals
                        grandTotalPurchase += totalSale;
                        grandTotalDiscount += totalDiscount;
                        grandTotalPayment += totalPayment;
                        grandTotalReturn += totalReturn;
                        grandTotalBalance += currentBalance;
                        
                        String formattedSale = String.format("%.2f", totalSale);
                        String formattedDiscount = String.format("%.2f", totalDiscount);
                        String formattedPayment = String.format("%.2f", totalPayment);
                        String formattedReturn = String.format("%.2f", totalReturn);
                        String formattedBalance = String.format("%.2f", currentBalance);
                        
                        table.getItems().add(new AreaWiseReport("Customer", customerName, formattedSale,
                                                              formattedDiscount, formattedPayment, formattedReturn, formattedBalance));
                        count++;
                    }
                }
                customersResult.close();
                stmt.close();
            }
            
            if (partyType.equals("Supplier") || partyType.equals("Both")) {
                if (areaType.equals("all") || areaValue.equals("All")) {
                    partiesQuery = "SELECT supplier_name FROM Supplier";
                } else {
                    switch (areaType.toLowerCase()) {
                        case "province":
                            partiesQuery = "SELECT s.supplier_name FROM Supplier s " +
                                         "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                                         "LEFT JOIN District d ON t.district_id = d.district_id " +
                                         "LEFT JOIN Province p ON d.province_id = p.province_id " +
                                         "WHERE p.province_name = ?";
                            break;
                        case "district":
                            partiesQuery = "SELECT s.supplier_name FROM Supplier s " +
                                         "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                                         "LEFT JOIN District d ON t.district_id = d.district_id " +
                                         "WHERE d.district_name = ?";
                            break;
                        case "tehsil":
                            partiesQuery = "SELECT s.supplier_name FROM Supplier s " +
                                         "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                                         "WHERE t.tehsil_name = ?";
                            break;
                    }
                }
                
                java.sql.Connection conn = config.database.getConnection();
                java.sql.PreparedStatement stmt;
                if (areaType.equals("all") || areaValue.equals("All")) {
                    stmt = conn.prepareStatement(partiesQuery);
                } else {
                    stmt = conn.prepareStatement(partiesQuery);
                    stmt.setString(1, areaValue);
                }
                
                java.sql.ResultSet suppliersResult = stmt.executeQuery();
                while (suppliersResult.next()) {
                    String supplierName = suppliersResult.getString("supplier_name");
                    
                    // Get supplier ledger data using the same method as AccountsContent
                    List<Object[]> ledgerData;
                    if (fromDate != null && toDate != null) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String startDate = fromDate.format(formatter);
                        String endDate = toDate.format(formatter);
                        ledgerData = config.database.getSupplierLedgerByDateRange(supplierName, startDate, endDate);
                    } else {
                        ledgerData = config.database.getSupplierLedger(supplierName);
                    }
                    
                    // Calculate totals using the same logic as AccountsContent
                    double totalPurchase = 0.0;
                    double totalDiscount = 0.0;
                    double totalPayment = 0.0;
                    double totalReturn = 0.0;
                    double currentBalance = 0.0;
                    
                    for (Object[] row : ledgerData) {
                        if (row.length >= 11) {
                            totalPurchase += (Double) row[5];   // Total Bill column
                            totalDiscount += (Double) row[6];   // Discount column
                            totalPayment += (Double) row[8];    // Payment column
                            totalReturn += (Double) row[9];     // Return Amount column
                        }
                    }
                    
                    // Get actual current balance from Supplier table, not filtered ledger data
                    java.sql.PreparedStatement balanceStmt = conn.prepareStatement("SELECT balance FROM Supplier WHERE supplier_name = ?");
                    balanceStmt.setString(1, supplierName);
                    java.sql.ResultSet balanceResult = balanceStmt.executeQuery();
                    if (balanceResult.next()) {
                        currentBalance = balanceResult.getDouble("balance");
                    }
                    balanceResult.close();
                    balanceStmt.close();
                    
                    // Only add suppliers with transactions in the date range
                    if (totalPurchase > 0 || totalPayment > 0 || totalReturn > 0) {
                        // Add to grand totals
                        grandTotalPurchase += totalPurchase;
                        grandTotalDiscount += totalDiscount;
                        grandTotalPayment += totalPayment;
                        grandTotalReturn += totalReturn;
                        grandTotalBalance += currentBalance;
                        
                        String formattedPurchase = String.format("%.2f", totalPurchase);
                        String formattedDiscount = String.format("%.2f", totalDiscount);
                        String formattedPayment = String.format("%.2f", totalPayment);
                        String formattedReturn = String.format("%.2f", totalReturn);
                        String formattedBalance = String.format("%.2f", currentBalance);
                        
                        table.getItems().add(new AreaWiseReport("Supplier", supplierName, formattedPurchase,
                                                              formattedDiscount, formattedPayment, formattedReturn, formattedBalance));
                        count++;
                    }
                }
                suppliersResult.close();
                stmt.close();
            }
            
            // Format totals for return
            String formattedGrandPurchase = String.format("%.2f", grandTotalPurchase);
            String formattedGrandDiscount = String.format("%.2f", grandTotalDiscount);
            String formattedGrandPayment = String.format("%.2f", grandTotalPayment);
            String formattedGrandReturn = String.format("%.2f", grandTotalReturn);
            String formattedGrandBalance = String.format("%.2f", grandTotalBalance);
            
            System.out.println("DEBUG: Generated " + count + " records using ledger data");
            
            return new AreaWiseReportResult(count, formattedGrandPurchase, formattedGrandDiscount, 
                                          formattedGrandPayment, formattedGrandReturn, formattedGrandBalance);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating area-wise data from ledgers: " + e.getMessage());
            return new AreaWiseReportResult(0, "0.00", "0.00", "0.00", "0.00", "0.00");
        }
    }

    public static class AreaWiseReport {
        private final String partyType;
        private final String name;
        private final String totalSale;
        private final String totalDiscount;
        private final String totalPaid;
        private final String totalReturn;
        private final String totalBalance;

        public AreaWiseReport(String partyType, String name, String totalSale,
                            String totalDiscount, String totalPaid, String totalReturn, String totalBalance) {
            this.partyType = partyType;
            this.name = name;
            this.totalSale = totalSale;
            this.totalDiscount = totalDiscount;
            this.totalPaid = totalPaid;
            this.totalReturn = totalReturn;
            this.totalBalance = totalBalance;
        }

        public String getPartyType() { return partyType; }
        public String getName() { return name; }
        public String getTotalSale() { return totalSale; }
        public String getTotalDiscount() { return totalDiscount; }
        public String getTotalPaid() { return totalPaid; }
        public String getTotalReturn() { return totalReturn; }
        public String getTotalBalance() { return totalBalance; }
    }

    public static class BrandSalesReport {
        private final String salesmanName;
        private final String brandName;
        private final String totalQuantity;
        private final String totalSale;

        public BrandSalesReport(String salesmanName, String brandName, 
                              String totalQuantity, String totalSale) {
            this.salesmanName = salesmanName;
            this.brandName = brandName;
            this.totalQuantity = totalQuantity;
            this.totalSale = totalSale;
        }

        public String getSalesmanName() { return salesmanName; }
        public String getBrandName() { return brandName; }
        public String getTotalQuantity() { return totalQuantity; }
        public String getTotalSale() { return totalSale; }
    }

    public static class BrandProfitReport {
        private final String brandName;
        private final String totalSales;
        private final String totalCost;
        private final String profit;

        public BrandProfitReport(String brandName, String totalSales, 
                               String totalCost, String profit) {
            this.brandName = brandName;
            this.totalSales = totalSales;
            this.totalCost = totalCost;
            this.profit = profit;
        }

        public String getBrandName() { return brandName; }
        public String getTotalSales() { return totalSales; }
        public String getTotalCost() { return totalCost; }
        public String getProfit() { return profit; }
    }

    public static class CustomerSalesReport {
        private final String customerName;
        private final String totalInvoices;
        private final String totalSales;

        public CustomerSalesReport(String customerName, String totalInvoices, 
                                 String totalSales) {
            this.customerName = customerName;
            this.totalInvoices = totalInvoices;
            this.totalSales = totalSales;
        }

        public String getCustomerName() { return customerName; }
        public String getTotalInvoices() { return totalInvoices; }
        public String getTotalSales() { return totalSales; }
    }

    public static class SupplierSalesReport {
        private final String supplierName;
        private final String totalInvoices;
        private final String totalSupplied;

        public SupplierSalesReport(String supplierName, String totalInvoices, 
                                 String totalSupplied) {
            this.supplierName = supplierName;
            this.totalInvoices = totalInvoices;
            this.totalSupplied = totalSupplied;
        }

        public String getSupplierName() { return supplierName; }
        public String getTotalInvoices() { return totalInvoices; }
        public String getTotalSupplied() { return totalSupplied; }
    }

    public static class AttendanceReport {
        private final String employeeName;
        private final String attendanceDate;
        private final String status;
        private final String workingHours;

        public AttendanceReport(String employeeName, String attendanceDate, 
                              String status, String workingHours) {
            this.employeeName = employeeName;
            this.attendanceDate = attendanceDate;
            this.status = status;
            this.workingHours = workingHours;
        }

        public String getEmployeeName() { return employeeName; }
        public String getAttendanceDate() { return attendanceDate; }
        public String getStatus() { return status; }
        public String getWorkingHours() { return workingHours; }
    }

    /**
     * Export area-wise report to PDF using similar pattern as BalanceSheetGenerator
     */
    private static void exportAreaWiseReportToPDF(TableView<AreaWiseReport> table, String partyType, 
                                                 String areaType, String areaValue, LocalDate fromDate, LocalDate toDate) {
        try {
            // Generate timestamp for filename
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                             "AreaWiseReport_" + timestamp + ".pdf";
            
            // Generate the PDF using a simple text-based approach
            generateAreaWiseReportPDF(table, partyType, areaType, areaValue, fromDate, toDate, filename);
            
            // Use PrintManager to open the PDF for preview/printing
            boolean success = com.cablemanagement.invoice.PrintManager.openPDFForPreview(filename, "Area-Wise Report");
            
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("PDF Export");
                alert.setHeaderText("PDF Generated but Failed to Open");
                alert.setContentText("The PDF has been generated successfully but could not be opened automatically.\n\n" +
                                   "File saved at: " + filename + "\n\n" +
                                   "You can manually open this file with a PDF viewer.");
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            System.err.println("Error exporting area-wise report to PDF: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PDF Export Error");
            alert.setHeaderText("Failed to Generate PDF");
            alert.setContentText("An error occurred while generating the PDF report:\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Generate the actual PDF content for area-wise report
     */
    private static void generateAreaWiseReportPDF(TableView<AreaWiseReport> table, String partyType, 
                                                 String areaType, String areaValue, LocalDate fromDate, LocalDate toDate, 
                                                 String filename) throws Exception {
        
        // Use a simple PDF generation approach similar to existing generators
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        
        document.open();
        
        // Title
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("AREA-WISE CUSTOMER/SUPPLIER REPORT", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        
        // Filter information
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        document.add(new com.itextpdf.text.Paragraph(" "));
        document.add(new com.itextpdf.text.Paragraph("Report Filters:", normalFont));
        document.add(new com.itextpdf.text.Paragraph("Party Type: " + partyType, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Area Type: " + areaType, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Selected Area: " + areaValue, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        // Create table
        com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(7);
        pdfTable.setWidthPercentage(100);
        pdfTable.setWidths(new float[]{1.5f, 3f, 2f, 2f, 2f, 2f, 2f});
        
        // Table headers
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
        String[] headers = {"Type", "Name", "Total Purchase", "Total Discount", "Total Payment", "Total Return", "Current Balance"};
        for (String header : headers) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            pdfTable.addCell(cell);
        }
        
        // Table data
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        for (AreaWiseReport report : table.getItems()) {
            pdfTable.addCell(new com.itextpdf.text.Phrase(report.getPartyType(), cellFont));
            pdfTable.addCell(new com.itextpdf.text.Phrase(report.getName(), cellFont));
            // Right-align numerical values
            com.itextpdf.text.pdf.PdfPCell purchaseCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(report.getTotalSale(), cellFont));
            purchaseCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(purchaseCell);
            com.itextpdf.text.pdf.PdfPCell discountCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(report.getTotalDiscount(), cellFont));
            discountCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(discountCell);
            com.itextpdf.text.pdf.PdfPCell paymentCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(report.getTotalPaid(), cellFont));
            paymentCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(paymentCell);
            com.itextpdf.text.pdf.PdfPCell returnCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(report.getTotalReturn(), cellFont));
            returnCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(returnCell);
            com.itextpdf.text.pdf.PdfPCell balanceCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(report.getTotalBalance(), cellFont));
            balanceCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(balanceCell);
        }

        // Add grand totals row
        AreaWiseReportResult grandTotals = null;
        try {
            // Recalculate grand totals using the same method as UI
            grandTotals = generateAreaWiseDataFromLedgers(table, partyType, fromDate, toDate, areaType, areaValue);
        } catch (Exception e) {
            // Fallback: leave grandTotals null
        }
        com.itextpdf.text.Font grandTotalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.GREEN);
        com.itextpdf.text.pdf.PdfPCell grandLabelCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("GRAND TOTALS", grandTotalFont));
        grandLabelCell.setColspan(2);
        grandLabelCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        pdfTable.addCell(grandLabelCell);
    com.itextpdf.text.pdf.PdfPCell purchaseTotalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(grandTotals != null ? grandTotals.formattedTotalPurchase : "-", grandTotalFont));
    purchaseTotalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
    pdfTable.addCell(purchaseTotalCell);

    com.itextpdf.text.pdf.PdfPCell discountTotalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(grandTotals != null ? grandTotals.formattedTotalDiscount : "-", grandTotalFont));
    discountTotalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
    pdfTable.addCell(discountTotalCell);

    com.itextpdf.text.pdf.PdfPCell paymentTotalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(grandTotals != null ? grandTotals.formattedTotalPayment : "-", grandTotalFont));
    paymentTotalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
    pdfTable.addCell(paymentTotalCell);

    com.itextpdf.text.pdf.PdfPCell returnTotalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(grandTotals != null ? grandTotals.formattedTotalReturn : "-", grandTotalFont));
    returnTotalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
    pdfTable.addCell(returnTotalCell);

    com.itextpdf.text.pdf.PdfPCell balanceTotalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(grandTotals != null ? grandTotals.formattedTotalBalance : "-", grandTotalFont));
    balanceTotalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
    pdfTable.addCell(balanceTotalCell);

        document.add(pdfTable);

        // Footer note
        document.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.Font noteFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC);
        document.add(new com.itextpdf.text.Paragraph("Note: Only showing customers/suppliers with transactions in the selected date range. Financial breakdown shows Total Purchase (gross amount before discount), Total Discount, Total Payment, Total Return, and Current Balance to match supplier ledger format.", noteFont));

        // CODOC Footer
        com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY);
        document.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.Paragraph codocLine1 = new com.itextpdf.text.Paragraph("⚡ Software Developed by CODOC", footerFont);
        codocLine1.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        com.itextpdf.text.Paragraph codocLine2 = new com.itextpdf.text.Paragraph("Reach out for your own custom solution", footerFont);
        codocLine2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        com.itextpdf.text.Paragraph codocLine3 = new com.itextpdf.text.Paragraph("📧 info@codoc.it.com | 📞 0312-0854678", footerFont);
        codocLine3.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(codocLine1);
        document.add(codocLine2);
        document.add(codocLine3);

        document.close();
        writer.close();

        System.out.println("Area-wise report PDF generated successfully: " + filename);
    }

    /**
     * Export sales report to PDF using similar pattern as AreaWise report
     */
    private static void exportSalesReportToPDF(TableView<ObservableList<String>> table, String reportType, 
                                              LocalDate fromDate, LocalDate toDate) {
        try {
            // Generate timestamp for filename
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                             "SalesReport_" + timestamp + ".pdf";
            
            // Generate the PDF
            generateSalesReportPDF(table, reportType, fromDate, toDate, filename);
            
            // Use PrintManager to open the PDF for preview/printing
            boolean success = com.cablemanagement.invoice.PrintManager.openPDFForPreview(filename, "Sales Report");
            
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("PDF Export");
                alert.setHeaderText("PDF Generated but Failed to Open");
                alert.setContentText("The PDF has been generated successfully but could not be opened automatically.\n\n" +
                                   "File saved at: " + filename + "\n\n" +
                                   "You can manually open this file with a PDF viewer.");
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to Export PDF");
            alert.setContentText("An error occurred while generating the PDF report:\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Generate the actual PDF content for sales report
     */
    private static void generateSalesReportPDF(TableView<ObservableList<String>> table, String reportType, 
                                              LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        
        // Use iText PDF generation approach similar to existing generators
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        
        document.open();
        
        // Title
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("SALES REPORT", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        
        // Filter information
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        document.add(new com.itextpdf.text.Paragraph(" "));
        document.add(new com.itextpdf.text.Paragraph("Report Filters:", normalFont));
        document.add(new com.itextpdf.text.Paragraph("Report Type: " + reportType, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        // Check if table has data
        if (table.getItems().isEmpty() || table.getColumns().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected filters.", normalFont));
            document.close();
            writer.close();
            return;
        }
        
        // Create table with dynamic columns
        int columnCount = table.getColumns().size();
        com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(columnCount);
        pdfTable.setWidthPercentage(100);
        
        // Set column widths based on column count (distribute evenly)
        float[] columnWidths = new float[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = 1.0f;
        }
        pdfTable.setWidths(columnWidths);
        
        // Table headers
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
        for (int i = 0; i < table.getColumns().size(); i++) {
            String headerText = table.getColumns().get(i).getText();
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(headerText, headerFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            pdfTable.addCell(cell);
        }
        
        // Table data
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        for (ObservableList<String> row : table.getItems()) {
            for (int i = 0; i < row.size(); i++) {
                String cellValue = row.get(i);
                if (cellValue == null) cellValue = "";
                
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(cellValue, cellFont));
                
                // Right-align numerical columns (typically amounts)
                String headerText = table.getColumns().get(i).getText().toLowerCase();
                if (headerText.contains("amount") || headerText.contains("price") || 
                    headerText.contains("total") || headerText.contains("discount") || 
                    headerText.contains("paid") || headerText.contains("balance")) {
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                }
                
                pdfTable.addCell(cell);
            }
        }
        
        document.add(pdfTable);
        
        // Summary information
        document.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.Font summaryFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        document.add(new com.itextpdf.text.Paragraph("Total Records: " + table.getItems().size(), summaryFont));
        
        // Footer note
        document.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.Font noteFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC);
        document.add(new com.itextpdf.text.Paragraph("This report shows sales data for the selected date range and report type.", noteFont));
        
        // Add CODOC footer
        addCodocFooterToPdf(document);
        
        document.close();
        writer.close();
        
        System.out.println("Sales report PDF generated successfully: " + filename);
    }

    /**
     * Export purchase report to PDF
     */
    private static void exportPurchaseReportToPDF(TableView<Map<String, String>> table, String reportType, 
                                                 LocalDate fromDate, LocalDate toDate) {
        try {
            // Generate timestamp for filename
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                             "PurchaseReport_" + timestamp + ".pdf";
            
            // Generate the PDF
            generatePurchaseReportPDF(table, reportType, fromDate, toDate, filename);
            
            // Use PrintManager to open the PDF for preview/printing
            boolean success = com.cablemanagement.invoice.PrintManager.openPDFForPreview(filename, "Purchase Report");
            
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("PDF Export");
                alert.setHeaderText("PDF Generated but Failed to Open");
                alert.setContentText("The PDF has been generated successfully but could not be opened automatically.\n\n" +
                                   "File saved at: " + filename + "\n\n" +
                                   "You can manually open this file with a PDF viewer.");
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to Export PDF");
            alert.setContentText("An error occurred while generating the PDF report:\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Generate the actual PDF content for purchase report
     */
    private static void generatePurchaseReportPDF(TableView<Map<String, String>> table, String reportType, 
                                                 LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        
        // Use iText PDF generation approach similar to existing generators
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        
        document.open();
        
        // Title
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("PURCHASE REPORT", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        
        // Filter information
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        document.add(new com.itextpdf.text.Paragraph(" "));
        document.add(new com.itextpdf.text.Paragraph("Report Filters:", normalFont));
        document.add(new com.itextpdf.text.Paragraph("Report Type: " + reportType, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        // Check if table has data
        if (table.getItems().isEmpty() || table.getColumns().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected filters.", normalFont));
            document.close();
            writer.close();
            return;
        }
        
        // Create table with dynamic columns
        int columnCount = table.getColumns().size();
        com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(columnCount);
        pdfTable.setWidthPercentage(100);
        
        // Set column widths based on column count (distribute evenly)
        float[] columnWidths = new float[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = 1.0f;
        }
        pdfTable.setWidths(columnWidths);
        
        // Table headers
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
        for (int i = 0; i < table.getColumns().size(); i++) {
            String headerText = table.getColumns().get(i).getText();
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(headerText, headerFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            pdfTable.addCell(cell);
        }
        
        // Table data
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        for (Map<String, String> row : table.getItems()) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                String columnName = table.getColumns().get(i).getText();
                String cellValue = row.getOrDefault(columnName, "");
                if (cellValue == null) cellValue = "";
                
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(cellValue, cellFont));
                
                // Right-align numerical columns
                String columnNameLower = columnName.toLowerCase();
                if (columnNameLower.contains("amount") || columnNameLower.contains("price") || 
                    columnNameLower.contains("total") || columnNameLower.contains("discount") || 
                    columnNameLower.contains("paid") || columnNameLower.contains("balance")) {
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                }
                
                pdfTable.addCell(cell);
            }
        }
        
        document.add(pdfTable);
        
        // Summary information
        document.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.Font summaryFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        document.add(new com.itextpdf.text.Paragraph("Total Records: " + table.getItems().size(), summaryFont));
        
        // Footer note
        document.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.Font noteFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC);
        document.add(new com.itextpdf.text.Paragraph("This report shows purchase data for the selected date range and report type.", noteFont));
        
        document.close();
        writer.close();
        
        System.out.println("Purchase report PDF generated successfully: " + filename);
    }

    /**
     * Export return purchase report to PDF
     */
    private static void exportReturnPurchaseReportToPDF(TableView<Map<String, String>> table, String reportType, 
                                                       LocalDate fromDate, LocalDate toDate) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                             "ReturnPurchaseReport_" + timestamp + ".pdf";
            
            generateReturnPurchaseReportPDF(table, reportType, fromDate, toDate, filename);
            
            boolean success = com.cablemanagement.invoice.PrintManager.openPDFForPreview(filename, "Return Purchase Report");
            
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("PDF Export");
                alert.setHeaderText("PDF Generated but Failed to Open");
                alert.setContentText("The PDF has been generated successfully but could not be opened automatically.\n\n" +
                                   "File saved at: " + filename);
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to Export PDF");
            alert.setContentText("An error occurred while generating the PDF report:\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private static void generateReturnPurchaseReportPDF(TableView<Map<String, String>> table, String reportType, 
                                                       LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("RETURN PURCHASE REPORT", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        document.add(new com.itextpdf.text.Paragraph(" "));
        document.add(new com.itextpdf.text.Paragraph("Report Type: " + reportType, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty() || table.getColumns().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected filters.", normalFont));
        } else {
            int columnCount = table.getColumns().size();
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(columnCount);
            pdfTable.setWidthPercentage(100);
            
            // Headers
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
            for (int i = 0; i < table.getColumns().size(); i++) {
                String headerText = table.getColumns().get(i).getText();
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(headerText, headerFont));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }
            
            // Data
            com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
            for (Map<String, String> row : table.getItems()) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    String columnName = table.getColumns().get(i).getText();
                    String cellValue = row.getOrDefault(columnName, "");
                    if (cellValue == null) cellValue = "";
                    
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(cellValue, cellFont));
                    
                    String columnNameLower = columnName.toLowerCase();
                    if (columnNameLower.contains("amount") || columnNameLower.contains("total") || 
                        columnNameLower.contains("discount") || columnNameLower.contains("paid")) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    }
                    
                    pdfTable.addCell(cell);
                }
            }
            
            document.add(pdfTable);
            document.add(new com.itextpdf.text.Paragraph(" "));
            com.itextpdf.text.Font summaryFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            document.add(new com.itextpdf.text.Paragraph("Total Records: " + table.getItems().size(), summaryFont));
        }
        
        document.close();
        writer.close();
        
        System.out.println("Return purchase report PDF generated successfully: " + filename);
    }

    /**
     * Export return sales report to PDF
     */
    private static void exportReturnSalesReportToPDF(TableView<Map<String, String>> table, String reportType, 
                                                    LocalDate fromDate, LocalDate toDate) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = System.getProperty("java.io.tmpdir") + java.io.File.separator + 
                             "ReturnSalesReport_" + timestamp + ".pdf";
            
            generateReturnSalesReportPDF(table, reportType, fromDate, toDate, filename);
            
            boolean success = com.cablemanagement.invoice.PrintManager.openPDFForPreview(filename, "Return Sales Report");
            
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("PDF Export");
                alert.setHeaderText("PDF Generated but Failed to Open");
                alert.setContentText("The PDF has been generated successfully but could not be opened automatically.\n\n" +
                                   "File saved at: " + filename);
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to Export PDF");
            alert.setContentText("An error occurred while generating the PDF report:\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private static void generateReturnSalesReportPDF(TableView<Map<String, String>> table, String reportType, 
                                                    LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("RETURN SALES REPORT", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        document.add(new com.itextpdf.text.Paragraph(" "));
        document.add(new com.itextpdf.text.Paragraph("Report Type: " + reportType, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty() || table.getColumns().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected filters.", normalFont));
        } else {
            int columnCount = table.getColumns().size();
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(columnCount);
            pdfTable.setWidthPercentage(100);
            
            // Headers
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
            for (int i = 0; i < table.getColumns().size(); i++) {
                String headerText = table.getColumns().get(i).getText();
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(headerText, headerFont));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }
            
            // Data
            com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
            for (Map<String, String> row : table.getItems()) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    String columnName = table.getColumns().get(i).getText();
                    String cellValue = row.getOrDefault(columnName, "");
                    if (cellValue == null) cellValue = "";
                    
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(cellValue, cellFont));
                    
                    String columnNameLower = columnName.toLowerCase();
                    if (columnNameLower.contains("amount") || columnNameLower.contains("total") || 
                        columnNameLower.contains("discount") || columnNameLower.contains("paid")) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    }
                    
                    pdfTable.addCell(cell);
                }
            }
            
            document.add(pdfTable);
            document.add(new com.itextpdf.text.Paragraph(" "));
            com.itextpdf.text.Font summaryFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            document.add(new com.itextpdf.text.Paragraph("Total Records: " + table.getItems().size(), summaryFont));
        }
        
        document.close();
        writer.close();
        
        System.out.println("Return sales report PDF generated successfully: " + filename);
    }

    private static void exportBankTransferReportToPDF(TableView<BankTransferReport> table, LocalDate fromDate, LocalDate toDate) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "bank_transfer_report_" + timestamp + ".pdf";
            String filepath = "exports/" + filename;
            
            // Create exports directory if it doesn't exist
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }
            
            generateBankTransferReportPDF(table, fromDate, toDate, filepath);
            
            // Open PDF for preview
            PrintManager.openPDFForPreview(filepath, "Bank Transfer Report");
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to export Bank Transfer Report");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static void generateBankTransferReportPDF(TableView<BankTransferReport> table, LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        
        document.add(new com.itextpdf.text.Paragraph("Bank Transfer Report", titleFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected date range.", normalFont));
        } else {
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(4);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{2, 3, 3, 2});
            
            // Header
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Date", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("From Bank", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("To Bank", headerFont)));
            
            com.itextpdf.text.pdf.PdfPCell amountHeaderCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Amount", headerFont));
            amountHeaderCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(amountHeaderCell);
            
            // Data rows
            double totalAmount = 0;
            for (BankTransferReport item : table.getItems()) {
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getTransactionDate(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getFromBank(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getToBank(), normalFont)));
                
                com.itextpdf.text.pdf.PdfPCell amountCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getAmount(), normalFont));
                amountCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                pdfTable.addCell(amountCell);
                
                try {
                    totalAmount += Double.parseDouble(item.getAmount().replace(",", ""));
                } catch (NumberFormatException e) {
                    // Ignore non-numeric amounts
                }
            }
            
            // Total row
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total:", headerFont)));
            
            com.itextpdf.text.pdf.PdfPCell totalAmountCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.2f", totalAmount), headerFont));
            totalAmountCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(totalAmountCell);
            
            document.add(pdfTable);
        }
        
        document.close();
        writer.close();
        
        System.out.println("Bank transfer report PDF generated successfully: " + filename);
    }

    private static void exportProfitReportToPDF(TableView<ProfitReport> table, LocalDate fromDate, LocalDate toDate) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "profit_report_" + timestamp + ".pdf";
            String filepath = "exports/" + filename;
            
            // Create exports directory if it doesn't exist
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }
            
            generateProfitReportPDF(table, fromDate, toDate, filepath);
            
            // Open PDF for preview
            PrintManager.openPDFForPreview(filepath, "Profit Report");
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to export Profit Report");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static void generateProfitReportPDF(TableView<ProfitReport> table, LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        
        document.add(new com.itextpdf.text.Paragraph("Profit Report", titleFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected date range.", normalFont));
        } else {
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(4);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{2, 2, 2, 2});
            
            // Header
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Date", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Sales Amount", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cost Amount", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Profit", headerFont)));
            
            // Data rows
            double totalSales = 0, totalCost = 0, totalProfit = 0;
            
            for (ProfitReport item : table.getItems()) {
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getInvoiceDate(), normalFont)));
                
                com.itextpdf.text.pdf.PdfPCell saleAmountCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getSaleAmount(), normalFont));
                saleAmountCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                pdfTable.addCell(saleAmountCell);
                
                com.itextpdf.text.pdf.PdfPCell costAmountCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getCostAmount(), normalFont));
                costAmountCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                pdfTable.addCell(costAmountCell);
                
                com.itextpdf.text.pdf.PdfPCell profitCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getProfit(), normalFont));
                profitCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                pdfTable.addCell(profitCell);
                
                // Add to totals (parse safely)
                try {
                    totalSales += Double.parseDouble(item.getSaleAmount().replace(",", ""));
                    totalCost += Double.parseDouble(item.getCostAmount().replace(",", ""));
                    totalProfit += Double.parseDouble(item.getProfit().replace(",", ""));
                } catch (NumberFormatException e) {
                    // Ignore non-numeric values
                }
            }
            
            // Total row
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total:", headerFont)));
            
            com.itextpdf.text.pdf.PdfPCell totalSalesCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.2f", totalSales), headerFont));
            totalSalesCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(totalSalesCell);
            
            com.itextpdf.text.pdf.PdfPCell totalCostCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.2f", totalCost), headerFont));
            totalCostCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(totalCostCell);
            
            com.itextpdf.text.pdf.PdfPCell totalProfitCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.2f", totalProfit), headerFont));
            totalProfitCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(totalProfitCell);
            
            document.add(pdfTable);
        }
        
        document.close();
        writer.close();
        
        System.out.println("Profit report PDF generated successfully: " + filename);
    }

    private static void exportCustomersReportToPDF(TableView<CustomerReport> table) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "customers_report_" + timestamp + ".pdf";
            String filepath = "exports/" + filename;
            
            // Create exports directory if it doesn't exist
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }
            
            generateCustomersReportPDF(table, filepath);
            
            // Open PDF for preview
            PrintManager.openPDFForPreview(filepath, "Customers Report");
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to export Customers Report");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static void generateCustomersReportPDF(TableView<CustomerReport> table, String filename) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        
        document.add(new com.itextpdf.text.Paragraph("Customers General Report", titleFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No customer data available.", normalFont));
        } else {
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(3);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{3, 2, 4});
            
            // Header
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Customer Name", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Phone Number", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Address", headerFont)));
            
            // Data rows
            for (CustomerReport item : table.getItems()) {
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getCustomerName(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getPhoneNumber(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getAddress(), normalFont)));
            }
            
            document.add(pdfTable);
        }
        
        document.close();
        writer.close();
        
        System.out.println("Customers report PDF generated successfully: " + filename);
    }

    private static void exportSuppliersReportToPDF(TableView<SupplierReport> table) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "suppliers_report_" + timestamp + ".pdf";
            String filepath = "exports/" + filename;
            
            // Create exports directory if it doesn't exist
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }
            
            generateSuppliersReportPDF(table, filepath);
            
            // Open PDF for preview
            PrintManager.openPDFForPreview(filepath, "Suppliers Report");
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to export Suppliers Report");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static void generateSuppliersReportPDF(TableView<SupplierReport> table, String filename) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        
        document.add(new com.itextpdf.text.Paragraph("Suppliers General Report", titleFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No supplier data available.", normalFont));
        } else {
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(3);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{3, 2, 4});
            
            // Header
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Supplier Name", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Phone Number", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Address", headerFont)));
            
            // Data rows
            for (SupplierReport item : table.getItems()) {
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getSupplierName(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getPhoneNumber(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getAddress(), normalFont)));
            }
            
            document.add(pdfTable);
        }
        
        document.close();
        writer.close();
        
        System.out.println("Suppliers report PDF generated successfully: " + filename);
    }

    private static void exportBrandSalesReportToPDF(TableView<BrandSalesReport> table, LocalDate fromDate, LocalDate toDate) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "brand_sales_report_" + timestamp + ".pdf";
            String filepath = "exports/" + filename;
            
            // Create exports directory if it doesn't exist
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }
            
            generateBrandSalesReportPDF(table, fromDate, toDate, filepath);
            
            // Open PDF for preview
            PrintManager.openPDFForPreview(filepath, "Brand Sales Report");
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to export Brand Sales Report");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static void generateBrandSalesReportPDF(TableView<BrandSalesReport> table, LocalDate fromDate, LocalDate toDate, String filename) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filename));
        document.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        
        document.add(new com.itextpdf.text.Paragraph("Brand-Wise Sales Report", titleFont));
        document.add(new com.itextpdf.text.Paragraph("Date Range: " + fromDate + " to " + toDate, normalFont));
        document.add(new com.itextpdf.text.Paragraph("Generated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
        document.add(new com.itextpdf.text.Paragraph(" "));
        
        if (table.getItems().isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("No data available for the selected date range.", normalFont));
        } else {
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(4);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{3, 3, 2, 2});
            
            // Header
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Salesman", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Brand", headerFont)));
            
            com.itextpdf.text.pdf.PdfPCell quantityHeaderCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Quantity", headerFont));
            quantityHeaderCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(quantityHeaderCell);
            
            com.itextpdf.text.pdf.PdfPCell salesHeaderCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total Sales", headerFont));
            salesHeaderCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(salesHeaderCell);
            
            // Data rows
            double totalQuantity = 0, totalSales = 0;
            for (BrandSalesReport item : table.getItems()) {
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getSalesmanName(), normalFont)));
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getBrandName(), normalFont)));
                
                com.itextpdf.text.pdf.PdfPCell quantityCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getTotalQuantity(), normalFont));
                quantityCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                pdfTable.addCell(quantityCell);
                
                com.itextpdf.text.pdf.PdfPCell salesCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getTotalSale(), normalFont));
                salesCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                pdfTable.addCell(salesCell);
                
                try {
                    totalQuantity += Double.parseDouble(item.getTotalQuantity().replace(",", ""));
                    totalSales += Double.parseDouble(item.getTotalSale().replace(",", ""));
                } catch (NumberFormatException e) {
                    // Ignore non-numeric values
                }
            }
            
            // Total row
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("", headerFont)));
            pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total:", headerFont)));
            
            com.itextpdf.text.pdf.PdfPCell totalQuantityCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.0f", totalQuantity), headerFont));
            totalQuantityCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(totalQuantityCell);
            
            com.itextpdf.text.pdf.PdfPCell totalSalesCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.2f", totalSales), headerFont));
            totalSalesCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfTable.addCell(totalSalesCell);
            
            document.add(pdfTable);
        }
        
        document.close();
        writer.close();
        
        System.out.println("Brand sales report PDF generated successfully: " + filename);
    }

    // Overloaded method for showing GridPane in a new window (for Summary/Balance Sheet reports)
    private static void showGridInNewWindow(String reportTitle, GridPane grid, String filterInfo) {
        Stage reportStage = new Stage();
        reportStage.setTitle(reportTitle);
        reportStage.initModality(Modality.NONE); // Allow minimizing
        reportStage.setResizable(true);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label(reportTitle);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Filter info
        if (filterInfo != null && !filterInfo.trim().isEmpty()) {
            Label filterLabel = new Label(filterInfo);
            filterLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            mainLayout.getChildren().addAll(titleLabel, filterLabel);
        } else {
            mainLayout.getChildren().add(titleLabel);
        }

        // Clone the GridPane for the new window
        GridPane newGrid = new GridPane();
        newGrid.setHgap(grid.getHgap());
        newGrid.setVgap(grid.getVgap());
        newGrid.setPadding(grid.getPadding());
        newGrid.setStyle(grid.getStyle());

        // Copy all children from original grid
        for (Node child : grid.getChildren()) {
            if (child instanceof Label) {
                Label originalLabel = (Label) child;
                Label newLabel = new Label(originalLabel.getText());
                newLabel.setStyle(originalLabel.getStyle());
                
                // Preserve grid position
                Integer rowIndex = GridPane.getRowIndex(originalLabel);
                Integer colIndex = GridPane.getColumnIndex(originalLabel);
                newGrid.add(newLabel, 
                           colIndex != null ? colIndex : 0, 
                           rowIndex != null ? rowIndex : 0);
            }
        }

        ScrollPane gridScrollPane = new ScrollPane(newGrid);
        gridScrollPane.setFitToWidth(true);
        gridScrollPane.setFitToHeight(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> reportStage.close());
        buttonBox.getChildren().add(closeBtn);

        mainLayout.getChildren().addAll(gridScrollPane, buttonBox);
        VBox.setVgrow(gridScrollPane, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 800, 600);
        try {
            String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load stylesheet for report window: " + e.getMessage());
        }

        reportStage.setScene(scene);
        reportStage.show();
    }

    /**
     * Helper method to add CODOC footer to PDF documents
     */
    private static void addCodocFooterToPdf(com.itextpdf.text.Document document) {
        try {
            com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY);
            document.add(new com.itextpdf.text.Paragraph(" "));
            com.itextpdf.text.Paragraph codocLine1 = new com.itextpdf.text.Paragraph("⚡ Software Developed by CODOC", footerFont);
            codocLine1.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            com.itextpdf.text.Paragraph codocLine2 = new com.itextpdf.text.Paragraph("Reach out for your own custom solution", footerFont);
            codocLine2.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            com.itextpdf.text.Paragraph codocLine3 = new com.itextpdf.text.Paragraph("📧 info@codoc.it.com | 📞 0312-0854678", footerFont);
            codocLine3.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(codocLine1);
            document.add(codocLine2);
            document.add(codocLine3);
        } catch (Exception e) {
            System.err.println("Error adding CODOC footer: " + e.getMessage());
        }
    }

    private static void showSimpleAreaSummaryInNewWindow(ComboBox<String> summaryTypeCombo,
                                                        ComboBox<String> summaryPartyTypeCombo,
                                                        DatePicker summaryFromDatePicker,
                                                        DatePicker summaryToDatePicker) {
        try {
            String summaryType = summaryTypeCombo.getValue();
            String partyType = summaryPartyTypeCombo.getValue();
            LocalDate fromDate = summaryFromDatePicker.getValue();
            LocalDate toDate = summaryToDatePicker.getValue();
            
            // Validate date range
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Date Range");
                alert.setHeaderText("Invalid date range");
                alert.setContentText("From date cannot be after To date.");
                alert.showAndWait();
                return;
            }
            
            Stage reportStage = new Stage();
            reportStage.setTitle("Area Summary Report - " + summaryType + " wise " + partyType + " Summary");
            reportStage.initModality(Modality.NONE);
            reportStage.setResizable(true);
            reportStage.setMaximized(true);
            
            VBox mainLayout = new VBox(15);
            mainLayout.setPadding(new Insets(20));
            
            // Title with filter info
            String filterInfo = String.format("Summary Type: %s | Party Type: %s", summaryType, partyType);
            if (fromDate != null || toDate != null) {
                filterInfo += String.format(" | Period: %s to %s", 
                    fromDate != null ? fromDate.toString() : "Start", 
                    toDate != null ? toDate.toString() : "End");
            }
            
            Label titleLabel = new Label("Area Summary Report - " + summaryType + " wise " + partyType + " Summary");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            
            Label filterInfoLabel = new Label(filterInfo);
            filterInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 10 0;");
            
            // Create table for the summary
            TableView<AreaSummary> summaryTable = new TableView<>();
            summaryTable.setPrefSize(800, 500);
            
            TableColumn<AreaSummary, String> areaCol = new TableColumn<>(summaryType + " Name");
            areaCol.setCellValueFactory(new PropertyValueFactory<>("areaName"));
            areaCol.setMaxWidth(1f * Integer.MAX_VALUE * 60); // 60% of width
            
            TableColumn<AreaSummary, String> totalCol = new TableColumn<>("Total " + (partyType.equals("Customer") ? "Sales" : "Purchase") + " Amount");
            totalCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
            totalCol.setMaxWidth(1f * Integer.MAX_VALUE * 40); // 40% of width
            totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");
            
            summaryTable.getColumns().add(areaCol);
            summaryTable.getColumns().add(totalCol);
            
            // Set column resize policy to fill the table width
            summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Generate summary data
            ObservableList<AreaSummary> summaryData = generateSimpleAreaSummaryTable(summaryType, partyType, fromDate, toDate);
            summaryTable.setItems(summaryData);
            
            // Style the table
            summaryTable.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2;");
            
            ScrollPane reportScrollPane = new ScrollPane(summaryTable);
            reportScrollPane.setFitToWidth(true);
            reportScrollPane.setFitToHeight(true);
            
            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            
            Button refreshBtn = new Button("Refresh");
            refreshBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 16;");
            refreshBtn.setOnAction(e -> {
                ObservableList<AreaSummary> newData = generateSimpleAreaSummaryTable(summaryType, partyType, fromDate, toDate);
                summaryTable.setItems(newData);
            });
            
            Button printBtn = new Button("Generate PDF");
            printBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8 16;");
            printBtn.setOnAction(e -> {
                try {
                    PrintManager.printAreaSummaryReport(summaryTable, summaryType, partyType, fromDate, toDate);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("PDF Error");
                    alert.setHeaderText("Failed to generate PDF");
                    alert.setContentText("Error: " + ex.getMessage());
                    alert.showAndWait();
                }
            });
            
            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 16;");
            closeBtn.setOnAction(e -> reportStage.close());
            
            buttonBox.getChildren().addAll(refreshBtn, printBtn, closeBtn);
            
            mainLayout.getChildren().addAll(titleLabel, filterInfoLabel, reportScrollPane, buttonBox);
            
            Scene reportScene = new Scene(mainLayout, 1000, 600);
            
            // Apply stylesheet
            try {
                String cssPath = ReportsContent.class.getResource("/com/cablemanagement/style.css").toExternalForm();
                reportScene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.out.println("Warning: Could not load stylesheet: " + e.getMessage());
            }
            
            reportStage.setScene(reportScene);
            reportStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to generate area summary");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private static ObservableList<AreaSummary> generateSimpleAreaSummaryTable(String summaryType, String partyType, LocalDate fromDate, LocalDate toDate) {
        ObservableList<AreaSummary> summaryData = FXCollections.observableArrayList();
        Map<String, Double> areaMap = new HashMap<>();
        
        System.out.println("DEBUG: Generating area summary - Type: " + summaryType + ", Party: " + partyType);
        
        try {
            if ("Customer".equals(partyType)) {
                List<Customer> customers = config.database.getAllCustomers();
                System.out.println("DEBUG: Found " + customers.size() + " customers");
                
                for (Customer customer : customers) {
                    try {
                        String areaName = getAreaNameForCustomer(customer, summaryType);
                        System.out.println("DEBUG: Processing customer: " + customer.getName() + ", area: " + areaName);
                        
                        // Calculate customer totals using the same method as ledger
                        Map<String, Object> totals = calculateCustomerTotals(customer.getName(), fromDate, toDate);
                        double totalSales = (Double) totals.get("totalSales");
                        
                        System.out.println("DEBUG: Customer " + customer.getName() + " total sales: " + totalSales);
                        
                        areaMap.put(areaName, areaMap.getOrDefault(areaName, 0.0) + totalSales);
                    } catch (Exception e) {
                        System.err.println("Error processing customer " + customer.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else if ("Supplier".equals(partyType)) {
                List<Supplier> suppliers = config.database.getAllSuppliers();
                System.out.println("DEBUG: Found " + suppliers.size() + " suppliers");
                
                for (Supplier supplier : suppliers) {
                    try {
                        String areaName = getAreaNameForSupplier(supplier, summaryType);
                        System.out.println("DEBUG: Processing supplier: " + supplier.nameProperty().get() + ", area: " + areaName);
                        
                        // Calculate supplier totals using the same method as ledger
                        Map<String, Object> totals = calculateSupplierTotals(supplier.nameProperty().get(), fromDate, toDate);
                        double totalPurchase = (Double) totals.get("totalPurchase");
                        
                        System.out.println("DEBUG: Supplier " + supplier.nameProperty().get() + " total purchase: " + totalPurchase);
                        
                        areaMap.put(areaName, areaMap.getOrDefault(areaName, 0.0) + totalPurchase);
                    } catch (Exception e) {
                        System.err.println("Error processing supplier " + supplier.nameProperty().get() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            System.out.println("DEBUG: Area map contents: " + areaMap);
            
            // Convert map to observable list and sort
            double grandTotal = 0.0;
            for (Map.Entry<String, Double> entry : areaMap.entrySet()) {
                summaryData.add(new AreaSummary(entry.getKey(), String.format("%.2f", entry.getValue())));
                grandTotal += entry.getValue();
            }
            
            // Sort by area name
            summaryData.sort((a, b) -> a.getAreaName().compareToIgnoreCase(b.getAreaName()));
            
            // Add grand total row
            summaryData.add(new AreaSummary("=== GRAND TOTAL ===", String.format("%.2f", grandTotal)));
            
            System.out.println("DEBUG: Final summary data size: " + summaryData.size());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return summaryData;
    }
    
    private static String getAreaNameForCustomer(Customer customer, String summaryType) {
        try {
            switch (summaryType) {
                case "Tehsil":
                    String tehsil = customer.getTehsil();
                    return (tehsil != null && !tehsil.trim().isEmpty()) ? tehsil : "Unknown Tehsil";
                case "District":
                    // For now, return tehsil as district placeholder since we don't have district mapping
                    String customerTehsil = customer.getTehsil();
                    return (customerTehsil != null && !customerTehsil.trim().isEmpty()) ? customerTehsil + " (District)" : "Unknown District";
                case "Province":
                    // For now, return a generic province since we don't have province mapping
                    return "General Province";
                default:
                    return "Unknown";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private static String getAreaNameForSupplier(Supplier supplier, String summaryType) {
        try {
            switch (summaryType) {
                case "Tehsil":
                    String tehsil = supplier.tehsilProperty().get();
                    return (tehsil != null && !tehsil.trim().isEmpty()) ? tehsil : "Unknown Tehsil";
                case "District":
                    // For now, return tehsil as district placeholder since we don't have district mapping
                    String supplierTehsil = supplier.tehsilProperty().get();
                    return (supplierTehsil != null && !supplierTehsil.trim().isEmpty()) ? supplierTehsil + " (District)" : "Unknown District";
                case "Province":
                    // For now, return a generic province since we don't have province mapping
                    return "General Province";
                default:
                    return "Unknown";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private static Map<String, Object> calculateCustomerTotals(String customerName, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> totals = new HashMap<>();
        double totalSales = 0.0;
        double totalReceived = 0.0;
        
        System.out.println("DEBUG: Calculating totals for customer: " + customerName);
        
        try {
            if (config.database != null && config.database.isConnected()) {
                List<Object[]> ledgerData;
                
                if (fromDate != null && toDate != null) {
                    String fromStr = fromDate.toString();
                    String toStr = toDate.toString();
                    System.out.println("DEBUG: Using date range: " + fromStr + " to " + toStr);
                    ledgerData = config.database.getCustomerLedgerByDateRange(customerName, fromStr, toStr);
                } else {
                    System.out.println("DEBUG: Using all dates");
                    ledgerData = config.database.getCustomerLedger(customerName);
                }
                
                System.out.println("DEBUG: Ledger data size: " + (ledgerData != null ? ledgerData.size() : "null"));
                
                if (ledgerData != null) {
                    for (Object[] row : ledgerData) {
                        System.out.println("DEBUG: Row length: " + row.length);
                        if (row.length >= 12) {
                            // Based on the database structure:
                            // row[5] = totalBillAmount (total sales before discount)
                            // row[8] = netAmount (after discount), row[9] = paymentAmount, row[10] = returnAmount
                            try {
                                double totalBillAmount = row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0;
                                double paymentAmount = row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0;
                                double returnAmount = row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0;
                                
                                System.out.println("DEBUG: totalBillAmount=" + totalBillAmount + ", paymentAmount=" + paymentAmount + ", returnAmount=" + returnAmount);
                                
                                // Use total bill amount (before discount) for total sales
                                if (totalBillAmount > 0) {
                                    totalSales += totalBillAmount;
                                    System.out.println("DEBUG: Added totalBillAmount, new totalSales=" + totalSales);
                                }
                                if (paymentAmount > 0) {
                                    totalReceived += paymentAmount;
                                }
                                // Note: Not subtracting returns to match ledger gross total behavior
                            } catch (NumberFormatException e) {
                                System.out.println("DEBUG: Number format error: " + e.getMessage());
                                // Skip invalid number formats
                                continue;
                            }
                        }
                    }
                }
                
                System.out.println("DEBUG: Final totals - totalSales=" + totalSales + ", totalReceived=" + totalReceived);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        totals.put("totalSales", totalSales);
        totals.put("totalReceived", totalReceived);
        return totals;
    }
    
    private static Map<String, Object> calculateSupplierTotals(String supplierName, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> totals = new HashMap<>();
        double totalPurchase = 0.0;
        double totalPaid = 0.0;
        
        System.out.println("DEBUG: Calculating totals for supplier: " + supplierName);
        
        try {
            if (config.database != null && config.database.isConnected()) {
                List<Object[]> ledgerData;
                
                if (fromDate != null && toDate != null) {
                    String fromStr = fromDate.toString();
                    String toStr = toDate.toString();
                    System.out.println("DEBUG: Using date range: " + fromStr + " to " + toStr);
                    ledgerData = config.database.getSupplierLedgerByDateRange(supplierName, fromStr, toStr);
                } else {
                    System.out.println("DEBUG: Using all dates");
                    ledgerData = config.database.getSupplierLedger(supplierName);
                }
                
                System.out.println("DEBUG: Ledger data size: " + (ledgerData != null ? ledgerData.size() : "null"));
                
                if (ledgerData != null) {
                    for (Object[] row : ledgerData) {
                        System.out.println("DEBUG: Row length: " + row.length);
                        if (row.length >= 11) {
                            // Based on the database structure:
                            // row[5] = totalBillAmount (total purchase before discount)
                            // row[7] = netAmount (after discount), row[8] = paymentAmount, row[9] = returnAmount
                            try {
                                double totalBillAmount = row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0;
                                double paymentAmount = row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0;
                                double returnAmount = row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0;
                                
                                System.out.println("DEBUG: totalBillAmount=" + totalBillAmount + ", paymentAmount=" + paymentAmount + ", returnAmount=" + returnAmount);
                                
                                // Use total bill amount (before discount) for total purchases
                                if (totalBillAmount > 0) {
                                    totalPurchase += totalBillAmount;
                                    System.out.println("DEBUG: Added totalBillAmount, new totalPurchase=" + totalPurchase);
                                }
                                if (paymentAmount > 0) {
                                    totalPaid += paymentAmount;
                                }
                                // Note: Not subtracting returns to match ledger gross total behavior
                            } catch (NumberFormatException e) {
                                System.out.println("DEBUG: Number format error: " + e.getMessage());
                                // Skip invalid number formats
                                continue;
                            }
                        }
                    }
                }
                
                System.out.println("DEBUG: Final totals - totalPurchase=" + totalPurchase + ", totalPaid=" + totalPaid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        totals.put("totalPurchase", totalPurchase);
        totals.put("totalPaid", totalPaid);
        return totals;
    }

    // Inner class for Area Summary table data
    public static class AreaSummary {
        private final String areaName;
        private final String totalSales;
        
        public AreaSummary(String areaName, String totalSales) {
            this.areaName = areaName;
            this.totalSales = totalSales;
        }
        
        public String getAreaName() {
            return areaName;
        }
        
        public String getTotalSales() {
            return totalSales;
        }
    }

}