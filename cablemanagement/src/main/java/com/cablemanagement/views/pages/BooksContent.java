package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.invoice.*;
import com.cablemanagement.model.*;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksContent {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String EXPORT_PATH = "exports"; // Configure as needed

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createPurchaseBookForm());

        HBox buttonBar = createButtonBar(formArea);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        configureScrollPane(scrollPane);

        mainLayout.setTop(scrollPane);
            mainLayout.setCenter(formArea);
    
            return mainLayout;
        }

    private static HBox createButtonBar(StackPane formArea) {
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        String[] buttonLabels = {
            "Purchase Book",
            "Return Purchase Book",
            "Raw Stock Usage Book",
            "Production Book",
            "Return Production Book",
            "Sales Book",
            "Return Sales Book"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createPurchaseBookForm()),
            () -> formArea.getChildren().setAll(createReturnPurchaseBookForm()),
            () -> formArea.getChildren().setAll(createRawStockUsageBookForm()),
            () -> formArea.getChildren().setAll(createProductionBookForm()),
            () -> formArea.getChildren().setAll(createReturnProductionBookForm()),
            () -> formArea.getChildren().setAll(createSalesBookForm()),
            () -> formArea.getChildren().setAll(createReturnSalesBookForm())
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            addButton(buttonBar, buttonLabels[i], actions[i]);
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

    private static VBox createSection(String title, String description) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);
        box.getStyleClass().add("form-container");

        Label heading = new Label(title);
        heading.getStyleClass().add("form-heading");

        Label note = new Label(description);
        note.getStyleClass().add("form-subheading");

        box.getChildren().addAll(heading, note);
        return box;
    }

    private static HBox createFormRow(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        HBox row = new HBox(10, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");
        return row;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        return button;
    }

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static VBox createPurchaseBookForm() {
        VBox form = createSection("Purchase Book", "View and manage purchase records.");
        ObservableList<PurchaseRecord> data = FXCollections.observableArrayList();
        TableView<PurchaseRecord> table = createPurchaseTable(data);

        ComboBox<String> supplierFilter = createSupplierComboBox();
        HBox filters = createFilterControls(supplierFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
        
        // Enhanced print functionality for purchase invoices
        printBtn.setOnAction(e -> {
            PurchaseRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select an invoice to print");
                return;
            }

            try {
                // Get invoice details
                String invoiceNumber = selectedRecord.getInvoiceNumber();
                String supplier = selectedRecord.getSupplierName();
                String date = selectedRecord.getInvoiceDate();

                // Fetch all relevant fields from Raw_Purchase_Invoice for this invoice
                double discountAmount = 0.0;
                double paidAmount = 0.0;
                // Fetch discount and paid amount from Raw_Purchase_Invoice
                try {
                    String invoiceQuery = "SELECT discount_amount, paid_amount FROM Raw_Purchase_Invoice WHERE invoice_number = ?";
                    java.sql.PreparedStatement invStmt = config.database.getConnection().prepareStatement(invoiceQuery);
                    invStmt.setString(1, invoiceNumber);
                    java.sql.ResultSet invRs = invStmt.executeQuery();
                    if (invRs.next()) {
                        discountAmount = invRs.getDouble("discount_amount");
                        paidAmount = invRs.getDouble("paid_amount");
                    }
                    invRs.close();
                    invStmt.close();
                } catch (Exception ex) {
                    System.err.println("Error fetching invoice details: " + ex.getMessage());
                }

                // Calculate balance properly using the same method as original invoice creation
                double previousBalance = 0.0;
                double totalBalance = 0.0;
                double netBalance = 0.0;
                try {
                    // Get the invoice total after discount (net amount)
                    double invoiceTotalBeforeDiscount = selectedRecord.getAmount(); // Total before discount from DB
                    double invoiceTotalAfterDiscount = invoiceTotalBeforeDiscount - discountAmount; // Net amount
                    
                    // Use the same balance calculation method as the original invoice creation
                    Object[] balanceDetails = config.database.getSupplierInvoiceBalanceDetails(
                        supplier, invoiceNumber, invoiceTotalAfterDiscount, paidAmount
                    );
                    previousBalance = (Double) balanceDetails[0];
                    totalBalance = (Double) balanceDetails[1];
                    netBalance = (Double) balanceDetails[2];
                    
                    System.out.println("DEBUG Purchase Book Balance Calculation:");
                    System.out.println("  Invoice number: " + invoiceNumber);
                    System.out.println("  Supplier: " + supplier);
                    System.out.println("  Invoice total (before discount): " + invoiceTotalBeforeDiscount);
                    System.out.println("  Discount amount: " + discountAmount);
                    System.out.println("  Invoice total (after discount): " + invoiceTotalAfterDiscount);
                    System.out.println("  Paid amount: " + paidAmount);
                    System.out.println("  Previous balance: " + previousBalance);
                    System.out.println("  Total balance: " + totalBalance);
                    System.out.println("  Net balance: " + netBalance);
                } catch (Exception ex) {
                    System.err.println("Error calculating balance: " + ex.getMessage());
                    ex.printStackTrace();
                }

                // Operator field: not available, set to empty or fetch from elsewhere if needed
                String operator = "";

                // Create custom SQL query to get invoice items - fixed table references
                String query = "SELECT rs.item_name, rpii.quantity, rpii.unit_price, b.brand_name " +
                              "FROM Raw_Purchase_Invoice_Item rpii " +
                              "JOIN Raw_Purchase_Invoice rpi ON rpii.raw_purchase_invoice_id = rpi.raw_purchase_invoice_id " +
                              "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                              "LEFT JOIN Brand b ON rs.brand_id = b.brand_id " +
                              "WHERE rpi.invoice_number = ?";

                List<Item> printItems = new ArrayList<>();

                try {
                    // Execute the query to get invoice items
                    java.sql.PreparedStatement stmt = config.database.getConnection().prepareStatement(query);
                    stmt.setString(1, invoiceNumber);
                    java.sql.ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        String itemName = rs.getString("item_name");
                        String brandName = rs.getString("brand_name");
                        double quantity = rs.getDouble("quantity");
                        double unitPrice = rs.getDouble("unit_price");

                        // Include brand name with item name if available
                        String displayName = itemName;
                        if (brandName != null && !brandName.isEmpty()) {
                            displayName += " - " + brandName;
                        }

                        printItems.add(new Item(displayName, (int)quantity, unitPrice, 0.0));
                    }

                    rs.close();
                    stmt.close();

                } catch (Exception ex) {
                    System.err.println("Error fetching invoice items: " + ex.getMessage());
                    ex.printStackTrace();

                    // If we couldn't get detailed items, create a generic item based on invoice data
                    printItems.add(new Item("Purchase Items", 1, selectedRecord.getAmount(), 0.0));
                }

                // Get supplier contact and tehsil
                String contactNumber = "";
                String tehsil = "";

                try {
                    String supplierQuery = "SELECT s.contact_number, t.tehsil_name " +
                                          "FROM Supplier s " +
                                          "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                                          "WHERE s.supplier_name = ?";

                    java.sql.PreparedStatement supplierStmt = config.database.getConnection().prepareStatement(supplierQuery);
                    supplierStmt.setString(1, supplier);
                    java.sql.ResultSet supplierRs = supplierStmt.executeQuery();

                    if (supplierRs.next()) {
                        contactNumber = supplierRs.getString("contact_number");
                        tehsil = supplierRs.getString("tehsil_name");

                        if (contactNumber == null) contactNumber = "";
                        if (tehsil == null) tehsil = "";
                    }

                    supplierRs.close();
                    supplierStmt.close();

                } catch (Exception ex) {
                    System.err.println("Error fetching supplier details: " + ex.getMessage());
                }

                // Create invoice data for printing with all fields
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_PURCHASE,
                    invoiceNumber,
                    date,
                    supplier,
                    "", // Empty address as requested
                    printItems,
                    previousBalance // Set previous balance from calculation
                );

                // Set all balance details to match original invoice format
                invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                invoiceData.setDiscountAmount(discountAmount);
                invoiceData.setPaidAmount(paidAmount);
                invoiceData.setMetadata("contact", contactNumber);
                invoiceData.setMetadata("tehsil", tehsil);
                invoiceData.setMetadata("operator", operator);

                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Purchase");

                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Purchase");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print invoice " + invoiceNumber);
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare invoice for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter);
        return form;
    }

    private static VBox createReturnPurchaseBookForm() {
        VBox form = createSection("Return Purchase Book", "View and manage return purchase records.");
        ObservableList<ReturnPurchaseRecord> data = FXCollections.observableArrayList();
        TableView<ReturnPurchaseRecord> table = createReturnPurchaseTable(data);

        ComboBox<String> supplierFilter = createSupplierComboBox();
        HBox filters = createFilterControls(supplierFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadReturnPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter));
                
        // Enhanced print functionality for return purchase invoices
        printBtn.setOnAction(e -> {
            ReturnPurchaseRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select a return invoice to print");
                return;
            }

            try {
                // Get invoice details
                String returnInvoiceNumber = selectedRecord.getReturnInvoice();
                String supplier = selectedRecord.getSupplier();
                String date = selectedRecord.getDate();
                
                // Create custom SQL query to get return invoice items - fixed table references
                String query = "SELECT rs.item_name, rprii.quantity, rprii.unit_price, b.brand_name " +
                              "FROM Raw_Purchase_Return_Invoice_Item rprii " +
                              "JOIN Raw_Purchase_Return_Invoice rpri ON rprii.raw_purchase_return_invoice_id = rpri.raw_purchase_return_invoice_id " +
                              "JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                              "LEFT JOIN Brand b ON rs.brand_id = b.brand_id " +
                              "WHERE rpri.return_invoice_number = ?";
                
                List<Item> printItems = new ArrayList<>();
                
                try {
                    // Execute the query to get invoice items
                    java.sql.PreparedStatement stmt = config.database.getConnection().prepareStatement(query);
                    stmt.setString(1, returnInvoiceNumber);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String itemName = rs.getString("item_name");
                        String brandName = rs.getString("brand_name");
                        double quantity = rs.getDouble("quantity");
                        double unitPrice = rs.getDouble("unit_price");
                        
                        // Include brand name with item name if available
                        String displayName = itemName;
                        if (brandName != null && !brandName.isEmpty()) {
                            displayName += " - " + brandName;
                        }
                        
                        printItems.add(new Item(displayName, (int)quantity, unitPrice, 0.0));
                    }
                    
                    rs.close();
                    stmt.close();
                    
                } catch (Exception ex) {
                    System.err.println("Error fetching return invoice items: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    // If we couldn't get detailed items, create a generic item based on invoice data
                    printItems.add(new Item(selectedRecord.getItemName() + " - " + selectedRecord.getBrandName(), 
                                          (int)selectedRecord.getQuantity(), 
                                          selectedRecord.getUnitPrice(), 
                                          0.0));
                }
                
                // Get supplier contact and tehsil
                String contactNumber = "";
                String tehsil = "";
                
                try {
                    String supplierQuery = "SELECT s.contact_number, t.tehsil_name " +
                                          "FROM Supplier s " +
                                          "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                                          "WHERE s.supplier_name = ?";
                    
                    java.sql.PreparedStatement supplierStmt = config.database.getConnection().prepareStatement(supplierQuery);
                    supplierStmt.setString(1, supplier);
                    java.sql.ResultSet supplierRs = supplierStmt.executeQuery();
                    
                    if (supplierRs.next()) {
                        contactNumber = supplierRs.getString("contact_number");
                        tehsil = supplierRs.getString("tehsil_name");
                        
                        if (contactNumber == null) contactNumber = "";
                        if (tehsil == null) tehsil = "";
                    }
                    
                    supplierRs.close();
                    supplierStmt.close();
                    
                } catch (Exception ex) {
                    System.err.println("Error fetching supplier details: " + ex.getMessage());
                }
                
                // Calculate balance manually to match original return invoice behavior
                double previousBalance = 0.0;
                double totalBalance = 0.0;
                double netBalance = 0.0;
                double discountAmount = 0.0;
                double paidAmount = 0.0;
                try {
                    // Get current balance with all transactions included
                    double currentBalance = config.database.getSupplierCurrentBalance(supplier);
                    
                    // Calculate return impact amount from print items
                    double returnImpactAmount = 0.0;
                    for (Item item : printItems) {
                        returnImpactAmount += item.getUnitPrice() * item.getQuantity();
                    }
                    
                    // Previous balance = current balance + return amount (since return reduces balance)
                    previousBalance = currentBalance + returnImpactAmount;
                    
                    // For purchase return invoices: Total Balance = Previous Balance - Return Amount (we owe less to supplier)
                    totalBalance = previousBalance - returnImpactAmount;
                    
                    // Net balance = total balance (no payment involved in returns)
                    netBalance = totalBalance;
                    
                    System.out.println("DEBUG Return Purchase Book Balance Calculation:");
                    System.out.println("  Current balance: " + currentBalance);
                    System.out.println("  Return impact amount: " + returnImpactAmount);
                    System.out.println("  Previous balance: " + previousBalance);
                    System.out.println("  Total balance: " + totalBalance);
                    System.out.println("  Net balance: " + netBalance);
                    
                    // Note: Return invoices typically don't have discount or payment amounts
                    discountAmount = 0.0;
                    paidAmount = 0.0;
                } catch (Exception ex) {
                    System.err.println("Error calculating return balance: " + ex.getMessage());
                }
                
                // Create invoice data for printing with proper type and metadata
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_PURCHASE_RETURN,
                    returnInvoiceNumber,
                    date,
                    supplier,
                    "", // Empty address as requested
                    printItems,
                    previousBalance // Previous balance from calculation
                );
                
                // Set all balance details to match original return invoice format
                invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                // Set discount and payment amounts
                invoiceData.setDiscountAmount(discountAmount);
                invoiceData.setPaidAmount(paidAmount);
                
                // Add metadata
                invoiceData.setMetadata("contact", contactNumber);
                invoiceData.setMetadata("tehsil", tehsil);

                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Purchase Return");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Purchase Return");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print return invoice " + returnInvoiceNumber);
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare return invoice for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadReturnPurchaseData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), supplierFilter);
        return form;
    }

    private static VBox createRawStockUsageBookForm() {
        VBox form = createSection("Raw Stock Usage Book", "View and manage raw stock usage records.");
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        TableView<RawStockRecord> table = createRawStockTable(data);

        ComboBox<String> itemFilter = createItemComboBox();
        HBox filters = createFilterControls(itemFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), itemFilter));
        
        // Enhanced print functionality for raw stock usage
        printBtn.setOnAction(e -> {
            RawStockRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select a record to print");
                return;
            }

            try {
                // Get record details
                String reference = selectedRecord.getReference(); // This should be the invoice number
                String date = selectedRecord.getDate();
                
                // Try to get detailed invoice items from the database
                List<Item> printItems = new ArrayList<>();
                
                try {
                    // Query to get raw stock use invoice items with unit information
                    String query = "SELECT rs.item_name, u.unit_name, rsuii.quantity_used, rsuii.unit_cost " +
                                  "FROM Raw_Stock_Use_Invoice_Item rsuii " +
                                  "JOIN Raw_Stock_Use_Invoice rsui ON rsuii.raw_stock_use_invoice_id = rsui.raw_stock_use_invoice_id " +
                                  "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                                  "LEFT JOIN Unit u ON rs.unit_id = u.unit_id " +
                                  "WHERE rsui.use_invoice_number = ?";
                    
                    java.sql.PreparedStatement stmt = config.database.getConnection().prepareStatement(query);
                    stmt.setString(1, reference);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String itemName = rs.getString("item_name");
                        String unitName = rs.getString("unit_name");
                        double quantity = rs.getDouble("quantity_used");
                        double unitCost = rs.getDouble("unit_cost");
                        
                        // Create display name in "Name - Unit" format
                        String displayName = itemName;
                        if (unitName != null && !unitName.isEmpty()) {
                            displayName += " - " + unitName;
                        }
                        
                        printItems.add(new Item(displayName, (int)Math.floor(quantity), unitCost, 0.0));
                    }
                    
                    rs.close();
                    stmt.close();
                    
                } catch (Exception ex) {
                    System.err.println("Error fetching raw stock use invoice items: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    // If we couldn't get detailed items, create a generic item
                    String itemName = selectedRecord.getItem();
                    double quantity = selectedRecord.getQuantity();
                    printItems.add(new Item(itemName, (int)quantity, 0.0, 0.0));
                }
                
                // If no items were found, create a fallback item
                if (printItems.isEmpty()) {
                    String itemName = selectedRecord.getItem();
                    double quantity = selectedRecord.getQuantity();
                    printItems.add(new Item(itemName, (int)quantity, 0.0, 0.0));
                }
                
                // Create invoice data for printing
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_RAW_STOCK,
                    reference,
                    date,
                    "Raw Stock Usage",
                    "", 
                    printItems,
                    0.0
                );
                
                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Raw Stock Usage");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Raw Stock Usage");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print raw stock usage record");
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare record for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadRawStockData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), itemFilter);
        return form;
    }

    private static VBox createProductionBookForm() {
        VBox form = createSection("Production Book", "View and manage production records.");
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList();
        TableView<ProductionRecord> table = createProductionTable(data);

        ComboBox<String> productFilter = createProductComboBox();
        HBox filters = createFilterControls(productFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), productFilter));
        
        // Enhanced print functionality for production book
        printBtn.setOnAction(e -> {
            ProductionRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select a production record to print");
                return;
            }

            try {
                // Get record details
                String productName = selectedRecord.getProduct();
                String date = selectedRecord.getDate();
                double quantity = selectedRecord.getQuantity();
                String notes = selectedRecord.getNotes();
                
                // Get production stock ID to retrieve unit information
                int productionStockId = getProductionStockIdByName(productName);
                String unit = "N/A";
                if (productionStockId != -1) {
                    unit = getProductionStockUnit(productionStockId);
                }
                
                // Format the item name as "name - unit"
                String itemNameWithUnit = productName + " - " + unit;
                
                // Create items list for invoice
                List<Item> printItems = new ArrayList<>();
                printItems.add(new Item(itemNameWithUnit, (int)quantity, 0.0, 0.0)); // Unit price not available
                
                // Create invoice data for printing
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_PRODUCTION,
                    "PROD-" + date.replace("-", ""),
                    date,
                    "Production Record",
                    "", 
                    printItems,
                    0.0
                );
                
                // Add metadata
                invoiceData.setMetadata("notes", notes);

                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Production");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Production");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print production record");
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare production record for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), productFilter);
        return form;
    }

    private static VBox createReturnProductionBookForm() {
        VBox form = createSection("Return Production Book", "View and manage return production records.");
        ObservableList<ReturnProductionRecord> data = FXCollections.observableArrayList();
        TableView<ReturnProductionRecord> table = createReturnProductionTable(data);

        HBox filters = createFilterControls(null);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadReturnProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker")));
        
        // Enhanced print functionality for return production book
        printBtn.setOnAction(e -> {
            ReturnProductionRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select a return production record to print");
                return;
            }

            try {
                // Get record details
                String returnInvoiceNumber = selectedRecord.getReference();
                String date = selectedRecord.getDate();
                
                // Get return invoice ID from return invoice number
                int returnInvoiceId = -1;
                try {
                    List<Object[]> returnInvoices = config.database.getReturnProductionBookData(new HashMap<>());
                    for (Object[] invoice : returnInvoices) {
                        if (returnInvoiceNumber.equals(invoice[2])) { // return_invoice_number is at index 2
                            returnInvoiceId = (Integer) invoice[1]; // production_return_invoice_id is at index 1
                            break;
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Could not find return invoice ID: " + ex.getMessage());
                }
                
                // Create items list for invoice with detailed product information
                List<Item> printItems = new ArrayList<>();
                
                if (returnInvoiceId > 0) {
                    // Get detailed return invoice items using SQLiteDatabase directly
                    SQLiteDatabase sqliteDb = (SQLiteDatabase) config.database;
                    List<Object[]> returnItems = sqliteDb.getProductionReturnInvoiceItems(returnInvoiceId);
                    for (Object[] item : returnItems) {
                        int productionId = (Integer) item[1];
                        String productName = (String) item[2];
                        double quantity = (Double) item[4];
                        double unitCost = (Double) item[5];
                        
                        // Get unit information using production stock ID
                        String unit = getProductionStockUnit(productionId);
                        
                        // Format the item name as "name - unit"
                        String itemNameWithUnit = productName + " - " + unit;
                        
                        printItems.add(new Item(itemNameWithUnit, (int)quantity, unitCost, 0.0));
                    }
                } else {
                    // Fallback if we can't get detailed info
                    printItems.add(new Item(returnInvoiceNumber, (int)selectedRecord.getQuantity(), 0.0, 0.0));
                }
                
                // Create invoice data for printing with proper type
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_PRODUCTION_RETURN,
                    "RETPROD-" + date.replace("-", ""),
                    date,
                    "Return Production Record",
                    "", 
                    printItems,
                    0.0
                );

                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Return Production");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Return Production");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print return production record");
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare return production record for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadReturnProductionData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"));
        return form;
    }

    private static VBox createSalesBookForm() {
        VBox form = createSection("Sales Book", "View and manage sales records.");
        ObservableList<SalesRecord> data = FXCollections.observableArrayList();
        TableView<SalesRecord> table = createSalesTable(data);

        ComboBox<String> customerFilter = createCustomerComboBox();
        HBox filters = createFilterControls(customerFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter));
        
        // Enhanced print functionality
        printBtn.setOnAction(e -> {
            SalesRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select an invoice to print");
                return;
            }

            try {
                // Get detailed invoice data from database
                String invoiceNumber = selectedRecord.getInvoiceNumber();
                // Get sales invoice ID first
                int salesInvoiceId = config.database.getSalesInvoiceIdByNumber(invoiceNumber);
                if (salesInvoiceId == -1) {
                    showAlert("Error", "Invoice " + invoiceNumber + " not found");
                    return;
                }
                
                List<Object[]> invoiceItems = config.database.getSalesInvoiceItemsByInvoiceId(salesInvoiceId);
                if (invoiceItems.isEmpty()) {
                    showAlert("Error", "No items found for invoice " + invoiceNumber);
                    return;
                }

                // Convert to Item objects for printing
                List<Item> printItems = new ArrayList<>();
                double itemsSubtotal = 0.0; // Calculate the raw subtotal from items
                
                for (Object[] item : invoiceItems) {
                    String productName = item[1].toString();
                    double quantity = Double.parseDouble(item[2].toString());
                    double unitPrice = Double.parseDouble(item[3].toString());
                    double discountPercentage = item[4] != null ? Double.parseDouble(item[4].toString()) : 0.0;
                    double discountAmount = item[5] != null ? Double.parseDouble(item[5].toString()) : 0.0;
                    
                    // Get production stock ID to retrieve unit information
                    int productionStockId = Integer.parseInt(item[0].toString());
                    String unit = getProductionStockUnit(productionStockId);
                    
                    // Format the item name as "name - unit"
                    String itemNameWithUnit = productName + " - " + unit;
                    
                    printItems.add(new Item(itemNameWithUnit, (int)quantity, unitPrice, discountPercentage));
                    
                    // Calculate raw item total (before discount)
                    itemsSubtotal += quantity * unitPrice;
                }

                // Get customer details from database
                String contactNumber = "";
                String tehsil = "";
                
                try {
                    String customerQuery = "SELECT c.contact_number, t.tehsil_name " +
                                          "FROM Customer c " +
                                          "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                                          "WHERE c.customer_name = ?";
                    
                    java.sql.PreparedStatement customerStmt = config.database.getConnection().prepareStatement(customerQuery);
                    customerStmt.setString(1, selectedRecord.getCustomer());
                    java.sql.ResultSet customerRs = customerStmt.executeQuery();
                    
                    if (customerRs.next()) {
                        contactNumber = customerRs.getString("contact_number");
                        tehsil = customerRs.getString("tehsil_name");
                        
                        if (contactNumber == null) contactNumber = "";
                        if (tehsil == null) tehsil = "";
                    }
                    
                    customerRs.close();
                    customerStmt.close();
                    
                } catch (Exception ex) {
                    System.err.println("Error fetching customer details: " + ex.getMessage());
                }
                
                // Get the exact balance information stored in Sales_Book table when the invoice was created
                double previousBalance = 0.0;
                double totalBalance = 0.0;
                double netBalance = 0.0;
                double storedItemLevelDiscount = 0.0;
                double storedOtherDiscount = 0.0;
                double storedPaidAmount = 0.0;
                
                try {
                    // Query Sales_Book table to get the exact balance data that was stored when invoice was created
                    String balanceQuery = "SELECT previous_balance, total_balance, net_balance, " +
                                         "other_discount, paid_amount " +
                                         "FROM Sales_Book WHERE sales_invoice_number = ? LIMIT 1";
                    
                    java.sql.PreparedStatement balanceStmt = config.database.getConnection().prepareStatement(balanceQuery);
                    balanceStmt.setString(1, invoiceNumber);
                    java.sql.ResultSet balanceRs = balanceStmt.executeQuery();
                    
                    if (balanceRs.next()) {
                        previousBalance = balanceRs.getDouble("previous_balance");
                        totalBalance = balanceRs.getDouble("total_balance");
                        netBalance = balanceRs.getDouble("net_balance");
                        storedOtherDiscount = balanceRs.getDouble("other_discount");
                        storedPaidAmount = balanceRs.getDouble("paid_amount");
                        
                        System.out.println("DEBUG: Using EXACT stored Sales_Book balance data for " + invoiceNumber + ":");
                        System.out.println("  Previous Balance: " + previousBalance);
                        System.out.println("  Total Balance: " + totalBalance);
                        System.out.println("  Net Balance: " + netBalance);
                        System.out.println("  Stored Other Discount: " + storedOtherDiscount);
                        System.out.println("  Stored Paid Amount: " + storedPaidAmount);
                    } else {
                        System.out.println("WARNING: No balance data found in Sales_Book for " + invoiceNumber + ", falling back to calculation");
                        // Fallback to old calculation method
                        previousBalance = config.database.getCustomerPreviousBalance(selectedRecord.getCustomer(), invoiceNumber);
                        totalBalance = previousBalance + (itemsSubtotal - selectedRecord.getDiscount() - selectedRecord.getOtherDiscount());
                        netBalance = totalBalance - selectedRecord.getPaid();
                        storedOtherDiscount = selectedRecord.getOtherDiscount();
                        storedPaidAmount = selectedRecord.getPaid();
                    }
                    
                    balanceRs.close();
                    balanceStmt.close();
                    
                } catch (Exception ex) {
                    System.err.println("Error getting stored balance data: " + ex.getMessage());
                    // Fallback to old calculation method
                    previousBalance = config.database.getCustomerPreviousBalance(selectedRecord.getCustomer(), invoiceNumber);
                    totalBalance = previousBalance + (itemsSubtotal - selectedRecord.getDiscount() - selectedRecord.getOtherDiscount());
                    netBalance = totalBalance - selectedRecord.getPaid();
                    storedOtherDiscount = selectedRecord.getOtherDiscount();
                    storedPaidAmount = selectedRecord.getPaid();
                }
                
                // Use the exact stored values from Sales_Book table
                double billAmount = itemsSubtotal; // Raw total from items
                double itemLevelDiscount = selectedRecord.getDiscount(); // Item-level discount from database
                double otherDiscountAmount = storedOtherDiscount; // Use stored other discount
                double paidAmount = storedPaidAmount; // Use stored paid amount
                
                System.out.println("DEBUG: Sales Book printing FINAL values for " + invoiceNumber + ":");
                System.out.println("  itemsSubtotal: " + itemsSubtotal);
                System.out.println("  itemLevelDiscount: " + itemLevelDiscount);
                System.out.println("  otherDiscountAmount: " + otherDiscountAmount);
                System.out.println("  paidAmount: " + paidAmount);
                System.out.println("  previousBalance: " + previousBalance);
                System.out.println("  totalBalance: " + totalBalance);
                System.out.println("  netBalance: " + netBalance);
                
                // Create invoice data object with the exact stored balance details
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_SALE,
                    invoiceNumber,
                    selectedRecord.getDate(),
                    selectedRecord.getCustomer(),
                    "", // Empty address as requested
                    printItems,
                    previousBalance  // Use the exact stored previous balance
                );
                
                // Set the financial details using the exact stored values from Sales_Book
                invoiceData.setDiscountAmount(itemLevelDiscount);
                invoiceData.setOtherDiscountAmount(otherDiscountAmount);
                invoiceData.setPaidAmount(paidAmount);
                
                // Set the exact balance details that were calculated when the invoice was originally created
                invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                
                // Add metadata
                invoiceData.setMetadata("contact", contactNumber);
                invoiceData.setMetadata("tehsil", tehsil);

                // Open invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Sales");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Sales");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print invoice " + invoiceNumber);
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare invoice for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter);
        return form;
    }

    private static VBox createReturnSalesBookForm() {
        VBox form = createSection("Return Sales Book", "View and manage return sales records.");
        ObservableList<ReturnSalesRecord> data = FXCollections.observableArrayList();
        TableView<ReturnSalesRecord> table = createReturnSalesTable(data);

        ComboBox<String> customerFilter = createCustomerComboBox();
        HBox filters = createFilterControls(customerFilter);
        Button loadBtn = createSubmitButton("Load");
        Button printBtn = createActionButton("Print");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;"); // Green for print buttons
        HBox buttons = new HBox(10, loadBtn, printBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        loadBtn.setOnAction(e -> loadReturnSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter));
        
        // Enhanced print functionality for return sales
        printBtn.setOnAction(e -> {
            ReturnSalesRecord selectedRecord = table.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert("No Selection", "Please select a return invoice to print");
                return;
            }

            try {
                String returnInvoiceNumber = selectedRecord.getReturnInvoice();
                
                // Get comprehensive return invoice data directly from Return_Sales_Book table
                String query = "SELECT customer_contact, customer_tehsil, product_name, brand_name, " +
                              "manufacturer_name, unit_name, quantity, unit_price, item_discount_percentage, " +
                              "item_discount_amount, item_total, invoice_discount, other_discount, " +
                              "paid_amount, calculated_balance, original_sales_invoice_number " +
                              "FROM Return_Sales_Book WHERE return_invoice_number = ? ORDER BY return_sales_book_id";
                
                List<Item> printItems = new ArrayList<>();
                String contactNumber = "";
                String tehsil = "";
                String originalInvoiceNumber = "";
                double invoiceDiscount = 0.0;
                double otherDiscount = 0.0;
                double paidAmount = 0.0;
                double calculatedBalance = 0.0;
                
                try (java.sql.PreparedStatement stmt = config.database.getConnection().prepareStatement(query)) {
                    stmt.setString(1, returnInvoiceNumber);
                    java.sql.ResultSet rs = stmt.executeQuery();
                    
                    boolean hasData = false;
                    
                    while (rs.next()) {
                        hasData = true;
                        
                        // Get customer details from first row (same for all items)
                        if (contactNumber.isEmpty()) {
                            contactNumber = rs.getString("customer_contact") != null ? rs.getString("customer_contact") : "";
                            tehsil = rs.getString("customer_tehsil") != null ? rs.getString("customer_tehsil") : "";
                            originalInvoiceNumber = rs.getString("original_sales_invoice_number") != null ? rs.getString("original_sales_invoice_number") : "";
                            invoiceDiscount = rs.getDouble("invoice_discount");
                            otherDiscount = rs.getDouble("other_discount");
                            paidAmount = rs.getDouble("paid_amount");
                            calculatedBalance = rs.getDouble("calculated_balance");
                        }
                        
                        // Build comprehensive item information
                        String productName = rs.getString("product_name");
                        String brandName = rs.getString("brand_name") != null ? rs.getString("brand_name") : "";
                        String manufacturerName = rs.getString("manufacturer_name") != null ? rs.getString("manufacturer_name") : "";
                        String unitName = rs.getString("unit_name") != null ? rs.getString("unit_name") : "N/A";
                        
                        double quantity = rs.getDouble("quantity");
                        double unitPrice = rs.getDouble("unit_price");
                        double itemDiscountPercentage = rs.getDouble("item_discount_percentage");
                        
                        // Format comprehensive item display name
                        StringBuilder itemDisplayName = new StringBuilder(productName);
                        if (!brandName.isEmpty()) {
                            itemDisplayName.append(" (").append(brandName);
                            if (!manufacturerName.isEmpty()) {
                                itemDisplayName.append(" - ").append(manufacturerName);
                            }
                            itemDisplayName.append(")");
                        } else if (!manufacturerName.isEmpty()) {
                            itemDisplayName.append(" (").append(manufacturerName).append(")");
                        }
                        itemDisplayName.append(" - ").append(unitName);
                        
                        printItems.add(new Item(
                            itemDisplayName.toString(), 
                            (int)quantity, 
                            unitPrice, 
                            itemDiscountPercentage
                        ));
                    }
                    
                    if (!hasData) {
                        showAlert("Error", "No comprehensive data found for return invoice " + returnInvoiceNumber + " in Return_Sales_Book table");
                        return;
                    }
                } catch (Exception ex) {
                    System.err.println("Error fetching comprehensive return invoice data: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("Error", "Failed to retrieve return invoice data from Return_Sales_Book table: " + ex.getMessage());
                    return;
                }
                
                // Use the stored previous balance and calculated balance from the Return_Sales_Book table
                double previousBalance = selectedRecord.getPreviousBalance();
                
                // Create invoice data object for return invoice with comprehensive data
                InvoiceData invoiceData = new InvoiceData(
                    InvoiceData.TYPE_SALE_RETURN,
                    returnInvoiceNumber,
                    selectedRecord.getDate(),
                    selectedRecord.getCustomer(),
                    "", // Empty address as requested
                    printItems,
                    previousBalance // Use stored previous balance from the database
                );
                
                // Set comprehensive balance and payment details from stored data
                invoiceData.setBalanceDetails(previousBalance, previousBalance - selectedRecord.getAmount(), calculatedBalance);
                invoiceData.setPaidAmount(paidAmount);
                invoiceData.setDiscountAmount(invoiceDiscount);
                invoiceData.setOtherDiscountAmount(otherDiscount);
                
                // Add metadata
                invoiceData.setMetadata("contact", contactNumber);
                invoiceData.setMetadata("tehsil", tehsil);
                invoiceData.setMetadata("originalInvoiceNumber", originalInvoiceNumber);

                // Open return invoice for print preview
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Return Sales");
                
                if (!previewSuccess) {
                    // Fallback to printer selection if preview fails
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Return Sales");
                    if (!printSuccess) {
                        showAlert("Error", "Failed to print return invoice " + returnInvoiceNumber);
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to prepare return invoice for printing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        form.getChildren().addAll(filters, buttons, table);
        loadReturnSalesData(table, (DatePicker) filters.getChildren().get(0).lookup(".date-picker"),
                (DatePicker) filters.getChildren().get(1).lookup(".date-picker"), customerFilter);
        return form;
    }

    // Table creation methods
@SuppressWarnings("unchecked")
private static TableView<PurchaseRecord> createPurchaseTable(ObservableList<PurchaseRecord> data) {
    TableView<PurchaseRecord> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Invoice Number Column
    TableColumn<PurchaseRecord, String> invCol = new TableColumn<>("Invoice No");
    invCol.setCellValueFactory(cellData -> cellData.getValue().invoiceNumberProperty());
    
    // Date Column
    TableColumn<PurchaseRecord, String> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
    
    // Supplier Column
    TableColumn<PurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
    supplierCol.setCellValueFactory(cellData -> cellData.getValue().supplierProperty());
    
    // Amount Column
    TableColumn<PurchaseRecord, Double> amountCol = new TableColumn<>("Amount");
    amountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getAmount()));
    
    // Discount Column
    TableColumn<PurchaseRecord, Double> discountCol = new TableColumn<>("Discount");
    discountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getDiscount()));
    
    // Paid Column
    TableColumn<PurchaseRecord, Double> paidCol = new TableColumn<>("Paid");
    paidCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getPaid()));

    table.getColumns().addAll(invCol, dateCol, supplierCol, amountCol, discountCol, paidCol);
    table.setItems(data);
    return table;
}
    @SuppressWarnings("unchecked")
    private static TableView<ReturnPurchaseRecord> createReturnPurchaseTable(ObservableList<ReturnPurchaseRecord> data) {
        TableView<ReturnPurchaseRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Return Invoice Number Column
        TableColumn<ReturnPurchaseRecord, String> returnInvCol = new TableColumn<>("Return Invoice");
        returnInvCol.setCellValueFactory(cellData -> cellData.getValue().returnInvoiceProperty());

        // Date Column
        TableColumn<ReturnPurchaseRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        // Supplier Column
        TableColumn<ReturnPurchaseRecord, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(cellData -> cellData.getValue().supplierProperty());

        // Item Name Column
        TableColumn<ReturnPurchaseRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());

        // Brand Column
        TableColumn<ReturnPurchaseRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandNameProperty());

        // Quantity Column
        TableColumn<ReturnPurchaseRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        // Unit Price Column
        TableColumn<ReturnPurchaseRecord, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getUnitPrice()));

        // Total Amount Column
        TableColumn<ReturnPurchaseRecord, Double> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getTotalAmount()));

        table.getColumns().addAll(returnInvCol, dateCol, supplierCol, itemCol, brandCol, qtyCol, unitPriceCol, amountCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<RawStockRecord> createRawStockTable(ObservableList<RawStockRecord> data) {
        TableView<RawStockRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RawStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<RawStockRecord, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(cellData -> cellData.getValue().itemProperty());

        TableColumn<RawStockRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        TableColumn<RawStockRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(cellData -> cellData.getValue().referenceProperty());

        table.getColumns().addAll(dateCol, itemCol, qtyCol, refCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ProductionRecord> createProductionTable(ObservableList<ProductionRecord> data) {
        TableView<ProductionRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<ProductionRecord, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(cellData -> cellData.getValue().productProperty());

        TableColumn<ProductionRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductionRecord, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(cellData -> cellData.getValue().notesProperty());

        table.getColumns().addAll(dateCol, productCol, qtyCol, notesCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnProductionRecord> createReturnProductionTable(ObservableList<ReturnProductionRecord> data) {
        TableView<ReturnProductionRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<ReturnProductionRecord, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(cellData -> cellData.getValue().referenceProperty());

        TableColumn<ReturnProductionRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        table.getColumns().addAll(dateCol, refCol, qtyCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<SalesRecord> createSalesTable(ObservableList<SalesRecord> data) {
        TableView<SalesRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SalesRecord, String> invoiceCol = new TableColumn<>("Invoice No");
        invoiceCol.setCellValueFactory(cellData -> cellData.getValue().invoiceNumberProperty());

        TableColumn<SalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<SalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> cellData.getValue().customerProperty());

        TableColumn<SalesRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        TableColumn<SalesRecord, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(cellData -> cellData.getValue().discountProperty().asObject());

        TableColumn<SalesRecord, Double> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(cellData -> cellData.getValue().paidProperty().asObject());

        table.getColumns().addAll(invoiceCol, dateCol, customerCol, amountCol, discountCol, paidCol);
        table.setItems(data);
        return table;
    }

    @SuppressWarnings("unchecked")
    private static TableView<ReturnSalesRecord> createReturnSalesTable(ObservableList<ReturnSalesRecord> data) {
        TableView<ReturnSalesRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReturnSalesRecord, String> returnInvoiceCol = new TableColumn<>("Return Invoice");
        returnInvoiceCol.setCellValueFactory(cellData -> cellData.getValue().returnInvoiceProperty());

        TableColumn<ReturnSalesRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<ReturnSalesRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> cellData.getValue().customerProperty());

        TableColumn<ReturnSalesRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getAmount()));

        TableColumn<ReturnSalesRecord, String> originalInvoiceCol = new TableColumn<>("Original Invoice");
        originalInvoiceCol.setCellValueFactory(cellData -> cellData.getValue().originalInvoiceProperty());

        table.getColumns().addAll(returnInvoiceCol, dateCol, customerCol, amountCol, originalInvoiceCol);
        table.setItems(data);
        return table;
    }

    // Data loading methods
private static void loadPurchaseData(TableView<PurchaseRecord> table, DatePicker fromDatePicker, 
                                    DatePicker toDatePicker, ComboBox<String> supplierFilter) {
    ObservableList<PurchaseRecord> data = FXCollections.observableArrayList();
    try {
        Map<String, String> filters = new HashMap<>();
        if (fromDatePicker.getValue() != null) {
            filters.put("fromDate", fromDatePicker.getValue().format(DATE_FORMATTER));
        }
        if (toDatePicker.getValue() != null) {
            filters.put("toDate", toDatePicker.getValue().format(DATE_FORMATTER));
        }
        if (supplierFilter.getValue() != null && !supplierFilter.getValue().isEmpty()) {
            filters.put("supplier_name", supplierFilter.getValue());
        }

        List<Object[]> result = config.database.getPurchaseBookData(filters);
        System.out.println("Purchase_Book results: " + result.size() + " rows");

        // Use a set to track seen invoice numbers and skip duplicates
        java.util.HashSet<String> seenInvoiceIds = new java.util.HashSet<>();
        for (Object[] row : result) {
            // Purchase_Book table structure: purchase_book_id, raw_purchase_invoice_id, invoice_number, supplier_name, 
            // invoice_date, item_name, brand_name, manufacturer_name, quantity, unit_price, item_total, 
            // total_amount, discount_amount, paid_amount, balance, created_at
            String invoiceId = row[1] != null ? row[1].toString() : ""; // raw_purchase_invoice_id
            if (seenInvoiceIds.contains(invoiceId)) {
            continue; // Skip duplicate invoice ids
            }
            seenInvoiceIds.add(invoiceId);

            data.add(new PurchaseRecord(
            invoiceId, // raw_purchase_invoice_id
            row[2] != null ? row[2].toString() : "", // invoice_number
            row[3] != null ? row[3].toString() : "", // supplier_name
            row[4] != null ? row[4].toString() : "", // invoice_date
            row[5] != null ? row[5].toString() : "", // item_name
            row[6] != null ? row[6].toString() : "", // brand_name
            row[7] != null ? row[7].toString() : "", // manufacturer_name
            row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0, // quantity
            row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0, // unit_price
            row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0, // item_total
            row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0, // total_amount
            row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0, // discount_amount
            row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0, // paid_amount
            row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0 // balance
            ));
        }
    } catch (Exception e) {
        showAlert("Database Error", "Failed to load purchase data: " + e.getMessage());
        e.printStackTrace();
    }

    table.setItems(data);

    // Debug output
    System.out.println("Table items count: " + table.getItems().size());
    if (!table.getItems().isEmpty()) {
        System.out.println("First record: " + table.getItems().get(0).getInvoiceNumber());
    }
}
private static void loadReturnPurchaseData(TableView<ReturnPurchaseRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> supplierFilter) {
    ObservableList<ReturnPurchaseRecord> data = FXCollections.observableArrayList();
    try {
        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) {
            filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
        }
        if (toDate.getValue() != null) {
            filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
        }
        if (supplierFilter.getValue() != null && !supplierFilter.getValue().isEmpty() && !supplierFilter.getValue().equals("All Suppliers")) {
            filters.put("supplier_name", supplierFilter.getValue());
        }

        List<Object[]> result = config.database.getReturnPurchaseBookData(filters);
        System.out.println("Return_Purchase_Book results: " + result.size() + " rows");

        // Use a set to track seen invoice numbers and skip duplicates
        java.util.HashSet<String> seenInvoiceIds = new java.util.HashSet<>();
        for (Object[] row : result) {
            // Return_Purchase_Book table structure: return_purchase_book_id, raw_purchase_return_invoice_id, 
            // return_invoice_number, supplier_name, return_date, item_name, brand_name, manufacturer_name, 
            // quantity, unit_price, item_total, total_return_amount, created_at
            String invoiceId = row[1] != null ? row[1].toString() : ""; // raw_purchase_return_invoice_id
            if (seenInvoiceIds.contains(invoiceId)) {
                continue; // Skip duplicate invoice ids
            }
            seenInvoiceIds.add(invoiceId);

            data.add(new ReturnPurchaseRecord(
                invoiceId, // raw_purchase_return_invoice_id
                row[2] != null ? row[2].toString() : "", // return_invoice_number
                row[3] != null ? row[3].toString() : "", // supplier_name
                row[4] != null ? row[4].toString() : "", // return_date
                row[5] != null ? row[5].toString() : "", // item_name
                row[6] != null ? row[6].toString() : "", // brand_name
                row[7] != null ? row[7].toString() : "", // manufacturer_name
                row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0, // quantity
                row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0, // unit_price
                row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0, // item_total
                row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0, // total_return_amount
                0.0, // discount_amount (not available in return invoices)
                row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0, // paid_amount (same as total)
                0.0 // balance (no balance in returns)
            ));
        }
    } catch (Exception e) {
        showAlert("Database Error", "Failed to load return purchase data: " + e.getMessage());
        e.printStackTrace();
    }

    table.setItems(data);

    // Debug output
    System.out.println("Return Purchase Table items count: " + table.getItems().size());
    if (!table.getItems().isEmpty()) {
        System.out.println("First return record: " + table.getItems().get(0).getReturnInvoice());
    }
}

    private static void loadRawStockData(TableView<RawStockRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> itemFilter) {
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        try {
            Map<String, String> filters = new HashMap<>();
            if (fromDate.getValue() != null) {
                filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            }
            if (toDate.getValue() != null) {
                filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            }
            if (itemFilter.getValue() != null && !itemFilter.getValue().isEmpty() && !itemFilter.getValue().equals("All Items")) {
                filters.put("item_name", itemFilter.getValue());
            }

            List<Object[]> result = config.database.getRawStockUseBookData(filters);
            System.out.println("Raw_Stock_Use_Book results: " + result.size() + " rows");

            // Use a set to track seen invoice numbers and skip duplicates
            java.util.HashSet<String> seenInvoiceIds = new java.util.HashSet<>();
            for (Object[] row : result) {
                // Raw_Stock_Use_Book table structure: raw_stock_use_book_id, raw_stock_use_invoice_id, 
                // use_invoice_number, usage_date, item_name, brand_name, manufacturer_name, 
                // quantity_used, unit_cost, total_cost, total_usage_amount, reference_purpose, created_at
                String invoiceId = row[1] != null ? row[1].toString() : ""; // raw_stock_use_invoice_id
                if (seenInvoiceIds.contains(invoiceId)) {
                    continue; // Skip duplicate invoice ids
                }
                seenInvoiceIds.add(invoiceId);

                data.add(new RawStockRecord(
                    row[3] != null ? row[3].toString() : "", // usage_date
                    row[4] != null ? row[4].toString() : "", // item_name
                    row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0, // quantity_used
                    row[2] != null ? row[2].toString() : "" // use_invoice_number as reference
                ));
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load raw stock usage data: " + e.getMessage());
            e.printStackTrace();
        }

        table.setItems(data);

        // Debug output
        System.out.println("Raw Stock Usage Table items count: " + table.getItems().size());
        if (!table.getItems().isEmpty()) {
            System.out.println("First usage record: " + table.getItems().get(0).getReference());
        }
    }

    private static void loadProductionData(TableView<ProductionRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> productFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) {
            filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            System.out.println("From date filter: " + fromDate.getValue().format(DATE_FORMATTER));
        }
        if (toDate.getValue() != null) {
            filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            System.out.println("To date filter: " + toDate.getValue().format(DATE_FORMATTER));
        }
        if (productFilter.getValue() != null && !productFilter.getValue().equals("All Products")) {
            filters.put("product_name", productFilter.getValue());
            System.out.println("Product filter: " + productFilter.getValue());
        }

        System.out.println("Executing query for Production_Book");
        List<Object[]> rows = config.database.getProductionBookData(filters);
        System.out.println("Retrieved " + (rows != null ? rows.size() : 0) + " rows from Production_Book");
        
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null) {
                    System.out.println("Processing row: " + java.util.Arrays.toString(row));
                    // Production_Book table structure: production_book_id, production_invoice_id, production_date, 
                    // product_name, brand_name, manufacturer_name, quantity_produced, unit_cost, total_cost, notes, created_at
                    data.add(new ProductionRecord(
                        row[2] != null ? row[2].toString() : "", // production_date
                        row[3] != null ? row[3].toString() : "", // product_name
                        row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0, // quantity_produced
                        row[9] != null ? row[9].toString() : "" // notes
                    ));
                }
            }
        }
        table.setItems(data);
    }

    private static void loadReturnProductionData(TableView<ReturnProductionRecord> table, DatePicker fromDate, DatePicker toDate) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        if (fromDate.getValue() != null) {
            filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            System.out.println("From date filter: " + fromDate.getValue().format(DATE_FORMATTER));
        }
        if (toDate.getValue() != null) {
            filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            System.out.println("To date filter: " + toDate.getValue().format(DATE_FORMATTER));
        }

        System.out.println("Loading return production data with filters: " + filters);
        System.out.println("Executing query for Return_Production_Book");
        List<Object[]> rows = config.database.getReturnProductionBookData(filters);
        System.out.println("Retrieved " + (rows != null ? rows.size() : 0) + " rows from Return_Production_Book");

        ObservableList<ReturnProductionRecord> data = FXCollections.observableArrayList();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null) {
                    System.out.println("Processing row: " + java.util.Arrays.toString(row));
                    // Return_Production_Book table structure: return_production_book_id, production_return_invoice_id, 
                    // return_invoice_number, return_date, product_name, brand_name, manufacturer_name, 
                    // quantity_returned, unit_cost, total_cost, notes, created_at
                    data.add(new ReturnProductionRecord(
                        row[3] != null ? row[3].toString() : "", // return_date
                        row[2] != null ? row[2].toString() : "", // return_invoice_number
                        row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0 // quantity_returned
                    ));
                }
            }
        }
        table.setItems(data);
    }

    private static void loadSalesData(TableView<SalesRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> customerFilter) {
        ObservableList<SalesRecord> data = FXCollections.observableArrayList();
        try {
            Map<String, String> filters = new HashMap<>();
            if (fromDate.getValue() != null) {
                filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            }
            if (toDate.getValue() != null) {
                filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            }
            if (customerFilter.getValue() != null && !customerFilter.getValue().isEmpty() && !customerFilter.getValue().equals("All Customers")) {
                filters.put("customer_name", customerFilter.getValue());
            }

            // Use the new sales book data method
            List<Object[]> result = config.database.getSalesBookData(filters);
            System.out.println("Sales_Book results: " + result.size() + " rows");

            // Use a set to track seen sales invoice numbers and skip duplicates
            java.util.HashSet<String> seenSalesInvoiceNumbers = new java.util.HashSet<>();
            for (Object[] row : result) {
                // Sales_Book table structure: sales_book_id, sales_invoice_id, sales_invoice_number, customer_name, 
                // sales_date, product_name, brand_name, manufacturer_name, quantity, unit_price, 
                // discount_percentage, discount_amount, item_total, total_amount, other_discount, paid_amount, balance, created_at
                String salesInvoiceNumber = row[2] != null ? row[2].toString() : ""; // sales_invoice_number
                if (seenSalesInvoiceNumbers.contains(salesInvoiceNumber)) {
                    continue; // Skip duplicate sales invoice numbers
                }
                seenSalesInvoiceNumbers.add(salesInvoiceNumber);

                String customerName = row[3] != null ? row[3].toString() : ""; // customer_name
                String salesDate = row[4] != null ? row[4].toString() : ""; // sales_date
                double totalAmount = row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0; // total_amount
                double discountAmount = row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0; // discount_amount
                double otherDiscountAmount = row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0; // other_discount
                double paidAmount = row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0; // paid_amount

                System.out.println("DEBUG: Sales_Book row data for " + salesInvoiceNumber + ":");
                System.out.println("  totalAmount (row[13]): " + totalAmount);
                System.out.println("  discountAmount (row[11]): " + discountAmount); 
                System.out.println("  otherDiscountAmount (row[14]): " + otherDiscountAmount);
                System.out.println("  paidAmount (row[15]): " + paidAmount);

                // Data is already filtered, so just add to the results
                data.add(new SalesRecord(
                    salesInvoiceNumber,
                    salesDate,
                    customerName,
                    totalAmount,
                    discountAmount,
                    otherDiscountAmount,
                    paidAmount
                ));
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load sales data: " + e.getMessage());
            e.printStackTrace();
        }

        table.setItems(data);

        // Debug output
        System.out.println("Sales Table items count: " + table.getItems().size());
        if (!table.getItems().isEmpty()) {
            System.out.println("First sales record: " + table.getItems().get(0).getInvoiceNumber());
        }
    }

    private static void loadReturnSalesData(TableView<ReturnSalesRecord> table, DatePicker fromDate, DatePicker toDate, ComboBox<String> customerFilter) {
        if (config.database == null || !config.database.isConnected()) {
            showAlert("Error", "Database not connected!");
            return;
        }

        ObservableList<ReturnSalesRecord> data = FXCollections.observableArrayList();
        try {
            System.out.println("Loading return sales data...");

            Map<String, String> filters = new HashMap<>();
            if (fromDate.getValue() != null) {
                filters.put("fromDate", fromDate.getValue().format(DATE_FORMATTER));
            }
            if (toDate.getValue() != null) {
                filters.put("toDate", toDate.getValue().format(DATE_FORMATTER));
            }
            if (customerFilter.getValue() != null && !customerFilter.getValue().isEmpty() && !customerFilter.getValue().equals("All Customers")) {
                filters.put("customer_name", customerFilter.getValue());
            }

            // Use the new return sales book data method
            List<Object[]> result = config.database.getReturnSalesBookData(filters);
            System.out.println("Return_Sales_Book results: " + result.size() + " rows");

            // Use a set to track seen return invoice numbers and skip duplicates
            java.util.HashSet<String> seenReturnInvoiceNumbers = new java.util.HashSet<>();
            for (Object[] row : result) {
                // Return_Sales_Book table structure: return_sales_book_id(0), sales_return_invoice_id(1), return_invoice_number(2), 
                // customer_name(3), customer_contact(4), customer_tehsil(5), return_date(6), product_name(7), brand_name(8), manufacturer_name(9),
                // unit_name(10), quantity(11), unit_price(12), item_discount_percentage(13), item_discount_amount(14), item_total(15), 
                // total_return_amount(16), previous_balance(17), invoice_discount(18), other_discount(19), paid_amount(20), calculated_balance(21), 
                // original_sales_invoice_number(22), created_at(23)
                String returnInvoiceNumber = row[2] != null ? row[2].toString() : ""; // return_invoice_number
                if (seenReturnInvoiceNumbers.contains(returnInvoiceNumber)) {
                    continue; // Skip duplicate return invoice numbers
                }
                seenReturnInvoiceNumbers.add(returnInvoiceNumber);

                String customerName = row[3] != null ? row[3].toString() : ""; // customer_name
                String returnDate = row[6] != null ? row[6].toString() : ""; // return_date (corrected index)
                double totalReturnAmount = row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0; // total_return_amount (corrected index)
                
                // Safe parsing of previous_balance with error handling - correct index is 17
                double previousBalance = 0.0;
                try {
                    if (row[17] != null && !row[17].toString().trim().isEmpty()) {
                        previousBalance = Double.parseDouble(row[17].toString());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing previous_balance for return invoice " + row[2] + ": " + e.getMessage());
                    previousBalance = 0.0;
                }
                
                String originalInvoiceNumber = row[22] != null ? row[22].toString() : ""; // original_sales_invoice_number (corrected index)

                // Data is already filtered, so just add to the results
                data.add(new ReturnSalesRecord(
                    returnInvoiceNumber,
                    returnDate,
                    customerName,
                    totalReturnAmount,
                    previousBalance,
                    originalInvoiceNumber
                ));
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load return sales data: " + e.getMessage());
            e.printStackTrace();
        }

        table.setItems(data);
        System.out.println("Return Sales Table items count: " + data.size());
        if (!data.isEmpty()) {
            System.out.println("First return sales record: " + data.get(0).getReturnInvoice());
        }
    }

    private static ComboBox<String> createSupplierComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Suppliers");
        if (config.database != null && config.database.isConnected()) {
            List<Supplier> suppliers = config.database.getAllSuppliers();
            ObservableList<String> items = FXCollections.observableArrayList("All Suppliers");
            for (Supplier supplier : suppliers) {
                items.add(supplier.nameProperty().get());
            }
            comboBox.setItems(items);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static ComboBox<String> createCustomerComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Customers");
        if (config.database != null && config.database.isConnected()) {
            List<Customer> customers = config.database.getAllCustomers();
            ObservableList<String> items = FXCollections.observableArrayList("All Customers");
            for (Customer customer : customers) {
                items.add(customer.nameProperty().get());
            }
            comboBox.setItems(items);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static ComboBox<String> createItemComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Items");
        if (config.database != null && config.database.isConnected()) {
            List<Object[]> items = config.database.getAllRawStock();
            ObservableList<String> names = FXCollections.observableArrayList("All Items");
            for (Object[] item : items) {
                names.add(item[1].toString());
            }
            comboBox.setItems(names);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static ComboBox<String> createProductComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPromptText("All Products");
        if (config.database != null && config.database.isConnected()) {
            List<Object[]> products = config.database.getAllProductionStock();
            ObservableList<String> names = FXCollections.observableArrayList("All Products");
            for (Object[] product : products) {
                names.add(product[1].toString());
            }
            comboBox.setItems(names);
        }
        comboBox.getStyleClass().add("combo-box");
        return comboBox;
    }

    private static HBox createFilterControls(ComboBox<String> filterCombo) {
        HBox filters = new HBox(20);
        filters.setAlignment(Pos.CENTER_LEFT);

        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        fromDate.getStyleClass().add("date-picker");
        DatePicker toDate = new DatePicker(LocalDate.now());
        toDate.getStyleClass().add("date-picker");

        List<Node> children = new ArrayList<>();
        children.add(createFormRow("From:", fromDate));
        children.add(createFormRow("To:", toDate));
        if (filterCombo != null) {
            children.add(createFormRow(filterCombo.getPromptText().replace("All ", "") + ":", filterCombo));
        }

        filters.getChildren().addAll(children);
        return filters;
    }

    // Assumed PurchaseRecord class
    static class PurchaseRecord {
    private final SimpleStringProperty rawPurchaseInvoiceId;
    private final SimpleStringProperty invoiceNumber;
    private final SimpleStringProperty supplierName;
    private final SimpleStringProperty invoiceDate;
    private final SimpleStringProperty itemName;
    private final SimpleStringProperty brandName;
    private final SimpleStringProperty manufacturerName;
    private final SimpleDoubleProperty quantity;
    private final SimpleDoubleProperty unitPrice;
    private final SimpleDoubleProperty itemTotal;
    private final SimpleDoubleProperty totalAmount;
    private final SimpleDoubleProperty discountAmount;
    private final SimpleDoubleProperty paidAmount;
    private final SimpleDoubleProperty balanceDue;

    public PurchaseRecord(String rawPurchaseInvoiceId, String invoiceNumber, String supplierName, 
                         String invoiceDate, String itemName, String brandName, String manufacturerName,
                         double quantity, double unitPrice, double itemTotal, double totalAmount,
                         double discountAmount, double paidAmount, double balanceDue) {
        this.rawPurchaseInvoiceId = new SimpleStringProperty(rawPurchaseInvoiceId);
        this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
        this.supplierName = new SimpleStringProperty(supplierName);
        this.invoiceDate = new SimpleStringProperty(invoiceDate);
        this.itemName = new SimpleStringProperty(itemName);
        this.brandName = new SimpleStringProperty(brandName);
        this.manufacturerName = new SimpleStringProperty(manufacturerName);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.itemTotal = new SimpleDoubleProperty(itemTotal);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.discountAmount = new SimpleDoubleProperty(discountAmount);
        this.paidAmount = new SimpleDoubleProperty(paidAmount);
        this.balanceDue = new SimpleDoubleProperty(balanceDue);
    }

    // Property methods for TableView columns
    public SimpleStringProperty invoiceNumberProperty() { return invoiceNumber; }
    public SimpleStringProperty dateProperty() { return invoiceDate; }
    public SimpleStringProperty supplierProperty() { return supplierName; }

    // Getters (used by TableView columns)
    public String getRawPurchaseInvoiceId() { return rawPurchaseInvoiceId.get(); }
    public String getInvoiceNumber() { return invoiceNumber.get(); }
    public String getSupplierName() { return supplierName.get(); }
    public String getInvoiceDate() { return invoiceDate.get(); }
    public String getItemName() { return itemName.get(); }
    public String getBrandName() { return brandName.get(); }
    public String getManufacturerName() { return manufacturerName.get(); }
    public Double getQuantity() { return quantity.get(); }
    public Double getUnitPrice() { return unitPrice.get(); }
    public Double getItemTotal() { return itemTotal.get(); }
    public Double getTotalAmount() { return totalAmount.get(); }
    public Double getDiscountAmount() { return discountAmount.get(); }
    public Double getPaidAmount() { return paidAmount.get(); }
    public Double getBalanceDue() { return balanceDue.get(); }

    // Additional getters for TableView columns
    public Double getAmount() { return getTotalAmount(); }
    public Double getDiscount() { return getDiscountAmount(); }
    public Double getPaid() { return getPaidAmount(); }
}



    static class ReturnPurchaseRecord {
        private final StringProperty returnInvoiceId = new SimpleStringProperty();
        private final StringProperty returnInvoice = new SimpleStringProperty();
        private final StringProperty supplier = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty itemName = new SimpleStringProperty();
        private final StringProperty brandName = new SimpleStringProperty();
        private final StringProperty manufacturerName = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final SimpleDoubleProperty unitPrice = new SimpleDoubleProperty();
        private final SimpleDoubleProperty itemTotal = new SimpleDoubleProperty();
        private final SimpleDoubleProperty totalAmount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty discountAmount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty paidAmount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty balance = new SimpleDoubleProperty();

        // Constructor for backwards compatibility (5 parameters)
        ReturnPurchaseRecord(String returnInvoice, String date, String supplier, String originalInvoice, double amount) {
            this.returnInvoiceId.set("");
            this.returnInvoice.set(returnInvoice);
            this.supplier.set(supplier);
            this.date.set(date);
            this.itemName.set(originalInvoice); // Using as original invoice for backward compatibility
            this.brandName.set("");
            this.manufacturerName.set("");
            this.quantity.set(1.0);
            this.unitPrice.set(amount);
            this.itemTotal.set(amount);
            this.totalAmount.set(amount);
            this.discountAmount.set(0.0);
            this.paidAmount.set(amount);
            this.balance.set(0.0);
        }

        // Full constructor (14 parameters)
        ReturnPurchaseRecord(String returnInvoiceId, String returnInvoice, String supplier, String date, 
                           String itemName, String brandName, String manufacturerName, 
                           double quantity, double unitPrice, double itemTotal, 
                           double totalAmount, double discountAmount, double paidAmount, double balance) {
            this.returnInvoiceId.set(returnInvoiceId);
            this.returnInvoice.set(returnInvoice);
            this.supplier.set(supplier);
            this.date.set(date);
            this.itemName.set(itemName);
            this.brandName.set(brandName);
            this.manufacturerName.set(manufacturerName);
            this.quantity.set(quantity);
            this.unitPrice.set(unitPrice);
            this.itemTotal.set(itemTotal);
            this.totalAmount.set(totalAmount);
            this.discountAmount.set(discountAmount);
            this.paidAmount.set(paidAmount);
            this.balance.set(balance);
        }

        // Property getters
        StringProperty returnInvoiceIdProperty() { return returnInvoiceId; }
        StringProperty returnInvoiceProperty() { return returnInvoice; }
        StringProperty supplierProperty() { return supplier; }
        StringProperty dateProperty() { return date; }
        StringProperty itemNameProperty() { return itemName; }
        StringProperty brandNameProperty() { return brandName; }
        StringProperty manufacturerNameProperty() { return manufacturerName; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        SimpleDoubleProperty itemTotalProperty() { return itemTotal; }
        SimpleDoubleProperty totalAmountProperty() { return totalAmount; }
        SimpleDoubleProperty discountAmountProperty() { return discountAmount; }
        SimpleDoubleProperty paidAmountProperty() { return paidAmount; }
        SimpleDoubleProperty balanceProperty() { return balance; }

        // Backward compatibility properties
        StringProperty originalInvoiceProperty() { return itemName; } // For backward compatibility
        SimpleDoubleProperty amountProperty() { return totalAmount; } // For backward compatibility

        // Value getters
        String getReturnInvoiceId() { return returnInvoiceId.get(); }
        String getReturnInvoice() { return returnInvoice.get(); }
        String getSupplier() { return supplier.get(); }
        String getDate() { return date.get(); }
        String getItemName() { return itemName.get(); }
        String getBrandName() { return brandName.get(); }
        String getManufacturerName() { return manufacturerName.get(); }
        double getQuantity() { return quantity.get(); }
        double getUnitPrice() { return unitPrice.get(); }
        double getItemTotal() { return itemTotal.get(); }
        double getTotalAmount() { return totalAmount.get(); }
        double getDiscountAmount() { return discountAmount.get(); }
        double getPaidAmount() { return paidAmount.get(); }
        double getBalance() { return balance.get(); }

        // Backward compatibility getters
        String getOriginalInvoice() { return itemName.get(); } // For backward compatibility
        double getAmount() { return totalAmount.get(); } // For backward compatibility
    }

    static class RawStockRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty item = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final StringProperty reference = new SimpleStringProperty();

        RawStockRecord(String date, String item, double quantity, String reference) {
            this.date.set(date);
            this.item.set(item);
            this.quantity.set(quantity);
            this.reference.set(reference);
        }

        StringProperty dateProperty() { return date; }
        StringProperty itemProperty() { return item; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        StringProperty referenceProperty() { return reference; }
        String getDate() { return date.get(); }
        String getItem() { return item.get(); }
        double getQuantity() { return quantity.get(); }
        String getReference() { return reference.get(); }
    }

    static class ProductionRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty product = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();
        private final StringProperty notes = new SimpleStringProperty();

        ProductionRecord(String date, String product, double quantity, String notes) {
            this.date.set(date);
            this.product.set(product);
            this.quantity.set(quantity);
            this.notes.set(notes);
        }

        StringProperty dateProperty() { return date; }
        StringProperty productProperty() { return product; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        StringProperty notesProperty() { return notes; }
        String getDate() { return date.get(); }
        String getProduct() { return product.get(); }
        double getQuantity() { return quantity.get(); }
        String getNotes() { return notes.get(); }
    }

    static class ReturnProductionRecord {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty reference = new SimpleStringProperty();
        private final SimpleDoubleProperty quantity = new SimpleDoubleProperty();

        ReturnProductionRecord(String date, String reference, double quantity) {
            this.date.set(date);
            this.reference.set(reference);
            this.quantity.set(quantity);
        }

        StringProperty dateProperty() { return date; }
        StringProperty referenceProperty() { return reference; }
        SimpleDoubleProperty quantityProperty() { return quantity; }
        String getDate() { return date.get(); }
        String getReference() { return reference.get(); }
        double getQuantity() { return quantity.get(); }
    }

    static class SalesRecord {
        private final StringProperty invoiceNumber = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty customer = new SimpleStringProperty();
        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty discount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty otherDiscount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty paid = new SimpleDoubleProperty();

        SalesRecord(String invoiceNumber, String date, String customer, double amount, double discount, double otherDiscount, double paid) {
            this.invoiceNumber.set(invoiceNumber);
            this.date.set(date);
            this.customer.set(customer);
            this.amount.set(amount);
            this.discount.set(discount);
            this.otherDiscount.set(otherDiscount);
            this.paid.set(paid);
        }

        StringProperty invoiceNumberProperty() { return invoiceNumber; }
        StringProperty dateProperty() { return date; }
        StringProperty customerProperty() { return customer; }
        SimpleDoubleProperty amountProperty() { return amount; }
        SimpleDoubleProperty discountProperty() { return discount; }
        SimpleDoubleProperty otherDiscountProperty() { return otherDiscount; }
        SimpleDoubleProperty paidProperty() { return paid; }
        String getInvoiceNumber() { return invoiceNumber.get(); }
        String getDate() { return date.get(); }
        String getCustomer() { return customer.get(); }
        double getAmount() { return amount.get(); }
        double getDiscount() { return discount.get(); }
        double getOtherDiscount() { return otherDiscount.get(); }
        double getPaid() { return paid.get(); }
    }

    static class ReturnSalesRecord {
        private final StringProperty returnInvoice = new SimpleStringProperty();
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty customer = new SimpleStringProperty();
        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();
        private final SimpleDoubleProperty previousBalance = new SimpleDoubleProperty();
        private final StringProperty originalInvoice = new SimpleStringProperty();

        ReturnSalesRecord(String returnInvoice, String date, String customer, double amount, double previousBalance, String originalInvoice) {
            this.returnInvoice.set(returnInvoice);
            this.date.set(date);
            this.customer.set(customer);
            this.amount.set(amount);
            this.previousBalance.set(previousBalance);
            this.originalInvoice.set(originalInvoice);
        }

        StringProperty returnInvoiceProperty() { return returnInvoice; }
        StringProperty dateProperty() { return date; }
        StringProperty customerProperty() { return customer; }
        SimpleDoubleProperty amountProperty() { return amount; }
        SimpleDoubleProperty previousBalanceProperty() { return previousBalance; }
        StringProperty originalInvoiceProperty() { return originalInvoice; }
        String getReturnInvoice() { return returnInvoice.get(); }
        String getDate() { return date.get(); }
        String getCustomer() { return customer.get(); }
        double getAmount() { return amount.get(); }
        double getPreviousBalance() { return previousBalance.get(); }
        String getOriginalInvoice() { return originalInvoice.get(); }
    }
    
    // Helper method to get production stock ID by name
    private static int getProductionStockIdByName(String productName) {
        try {
            List<Object[]> productionStocks = config.database.getAllProductionStocksForDropdown();
            for (Object[] stock : productionStocks) {
                if (stock[1].toString().equals(productName)) {
                    return (Integer) stock[0]; // production_id
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // Helper method to get production stock unit by production ID
    private static String getProductionStockUnit(int productionId) {
        try {
            List<Object[]> productionStocks = config.database.getAllProductionStocksForDropdown();
            for (Object[] stock : productionStocks) {
                if (((Integer) stock[0]).equals(productionId)) {
                    // stock[4] contains unit_name based on getAllProductionStocksForDropdown structure
                    String unit = (String) stock[4]; // unit_name
                    return unit != null ? unit : "N/A";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}