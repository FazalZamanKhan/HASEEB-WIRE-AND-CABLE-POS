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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.RawStockPurchaseItem;
import com.cablemanagement.model.RawStockUseItem;
import com.cablemanagement.invoice.Item;
import com.cablemanagement.invoice.InvoiceData;
import com.cablemanagement.invoice.PrintManager;

public class RawStock {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final db database = new SQLiteDatabase();

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createRawStockForm());

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
            "Register Raw Stock",
            "Create Raw Stock Purchase Invoice",
            "Create Raw Stock Return Purchase Invoice",
            "Create Raw Stock Use Invoice",
            "View Raw Stock Usage Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createRawStockForm()),
            () -> formArea.getChildren().setAll(createRawStockPurchaseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockReturnPurchaseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockUseInvoiceForm()),
            () -> formArea.getChildren().setAll(createRawStockUsageReportForm())
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

    // Create form field references accessible to other methods
    private static TextField rawStockNameField;
    private static TextField rawStockQuantityField;
    private static TextField rawStockUnitPriceField;
    private static ComboBox<String> rawStockBrandCombo;
    private static ComboBox<String> rawStockCategoryCombo;
    private static ComboBox<String> rawStockUnitCombo;
    private static ComboBox<String> rawStockSupplierCombo;
    private static TableView<RawStockRecord> rawStockTable;
    
    private static VBox createRawStockForm() {
        VBox form = new VBox();
        // form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");


        Label heading = createHeading("Register Raw Stock");

        // Initialize form fields
        rawStockNameField = createTextField("Stock Name");
        rawStockQuantityField = createTextField("0", "Quantity");
        rawStockUnitPriceField = createTextField("Unit Price");
        
        // Brand ComboBox for better database integration
        rawStockBrandCombo = new ComboBox<>();
        rawStockBrandCombo.setPromptText("Select Brand");
        for (Brand b : database.getAllBrands()) {
            rawStockBrandCombo.getItems().add(b.nameProperty().get());
        }
        rawStockBrandCombo.setPrefWidth(200);
        
        // Category ComboBox for selecting category
        rawStockCategoryCombo = new ComboBox<>();
        rawStockCategoryCombo.setPromptText("Select Category");
        rawStockCategoryCombo.getItems().addAll(database.getAllCategories());
        rawStockCategoryCombo.setPrefWidth(200);
        
        // Unit ComboBox for selecting units
        rawStockUnitCombo = new ComboBox<>();
        rawStockUnitCombo.setPromptText("Select Unit");
        rawStockUnitCombo.getItems().addAll(database.getAllUnits());
        rawStockUnitCombo.setPrefWidth(200);
        
        // Supplier ComboBox (optional)
        rawStockSupplierCombo = new ComboBox<>();
        rawStockSupplierCombo.setPromptText("Select Supplier");
        rawStockSupplierCombo.getItems().addAll(database.getAllSupplierNames());
        rawStockSupplierCombo.setPrefWidth(200);

        Button submitBtn = createSubmitButton("Submit Raw Stock");

        // Raw Stock Table
        Label tableHeading = createSubheading("Registered Raw Stock:");
        rawStockTable = createRawStockTable();
        refreshRawStockTable(rawStockTable);
        
        // Add update functionality (double-click)
        rawStockTable.setRowFactory(tv -> {
            TableRow<RawStockRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RawStockRecord selectedStock = row.getItem();
                    showUpdateStockDialog(selectedStock, rawStockNameField, rawStockBrandCombo, rawStockUnitCombo, 
                                          rawStockQuantityField, rawStockUnitPriceField, rawStockTable);
                }
            });
            return row;
        });

        submitBtn.setOnAction(e -> handleRawStockSubmit(
            rawStockNameField, rawStockBrandCombo, rawStockCategoryCombo, rawStockUnitCombo, rawStockSupplierCombo,
            rawStockQuantityField, rawStockUnitPriceField,
            rawStockTable
        ));

        GridPane registrationOptions = new GridPane();
        registrationOptions.setHgap(15);
        registrationOptions.setVgap(12);
        registrationOptions.setPadding(new Insets(10));
        registrationOptions.setAlignment(Pos.CENTER_LEFT);

        // Add fields in two columns for responsiveness
        registrationOptions.add(createFormRow("Stock Name:", rawStockNameField), 0, 0);
        registrationOptions.add(createFormRow("Brand:", rawStockBrandCombo), 1, 0);
        registrationOptions.add(createFormRow("Category:", rawStockCategoryCombo), 0, 1);
        registrationOptions.add(createFormRow("Unit:", rawStockUnitCombo), 1, 1);
        registrationOptions.add(createFormRow("Supplier:", rawStockSupplierCombo), 0, 2);
        registrationOptions.add(createFormRow("Quantity:", rawStockQuantityField), 1, 2);
        registrationOptions.add(createFormRow("Unit Price:", rawStockUnitPriceField), 0, 3);

        // Place submit button spanning both columns
        HBox submitBox = new HBox(submitBtn);
        submitBox.setAlignment(Pos.CENTER_RIGHT);
        registrationOptions.add(submitBox, 1, 3);

        // Make columns grow with window size
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        registrationOptions.getColumnConstraints().addAll(col1, col2);

        // Create form content in a compact layout
        VBox formContent = new VBox();
        formContent.setStyle("-fx-text-fill: black;");
        formContent.getChildren().addAll(
            heading,registrationOptions,
             tableHeading, rawStockTable
        );

        // Wrap form in ScrollPane for responsiveness
        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        form.getChildren().add(scrollPane);
        
        return form;
    }

    private static TableView<RawStockRecord> createRawStockTable() {
        TableView<RawStockRecord> table = new TableView<>();
        
        TableColumn<RawStockRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setMaxWidth(50);
        
        TableColumn<RawStockRecord, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<RawStockRecord, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<RawStockRecord, String> manufacturerCol = new TableColumn<>("Manufacturer");
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        manufacturerCol.setPrefWidth(120);

        TableColumn<RawStockRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(120);
        
        TableColumn<RawStockRecord, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(80);
        
        TableColumn<RawStockRecord, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);
        qtyCol.setCellFactory(column -> new TableCell<RawStockRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f", item));
                }
            }
        });
        
        TableColumn<RawStockRecord, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(column -> new TableCell<RawStockRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        TableColumn<RawStockRecord, Double> totalCol = new TableColumn<>("Total Cost");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(column -> new TableCell<RawStockRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        // Add action column with update button
        TableColumn<RawStockRecord, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(column -> new TableCell<RawStockRecord, Void>() {
            private final Button updateButton = new Button("Update");
            {
                updateButton.getStyleClass().add("action-button");
                updateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px;");
                updateButton.setOnAction(event -> {
                    RawStockRecord record = getTableView().getItems().get(getIndex());
                    // Use the static form fields
                    showUpdateStockDialog(
                        record, 
                        rawStockNameField, 
                        rawStockBrandCombo, 
                        rawStockUnitCombo, 
                        rawStockQuantityField, 
                        rawStockUnitPriceField, 
                        rawStockTable
                    );
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(updateButton);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, categoryCol, manufacturerCol, brandCol, unitCol, qtyCol, priceCol, totalCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Apply CSS class for proper header styling (defined in style.css)
        table.getStyleClass().add("table-view");
        
        return table;
    }

    private static void refreshRawStockTable(TableView<RawStockRecord> table) {
        ObservableList<RawStockRecord> data = FXCollections.observableArrayList();
        List<Object[]> rawStocks = database.getAllRawStocks();

        
        for (Object[] row : rawStocks) {
            System.out.println("Raw stock data: " + row[0] + ", " + row[1] + ", " + row[2] + ", " + row[3] + ", " + row[4] + ", " + row[5] + ", " + row[6] + ", " + row[7] + ", " + row[8]);
            data.add(new RawStockRecord(
                (Integer) row[0],  // stock_id
                (String) row[1],   // item_name
                (String) row[3],   // category_name
                (String) row[4],   // manufacturer_name
                (String) row[2],   // brand_name
                (String) row[5],   // unit_name
                (Double) row[6],   // quantity
                (Double) row[7],   // unit_price
                (Double) row[8]    // total_cost
            ));
        }
        
        table.setItems(data);
    }

    private static VBox createRawStockPurchaseInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Purchase Invoice");
        heading.setAlignment(Pos.CENTER);
        
        // Center the heading in a container
        HBox headingContainer = new HBox();
        headingContainer.setAlignment(Pos.CENTER);
        headingContainer.getChildren().add(heading);

        // Auto-generated invoice number (readonly)
        TextField invoiceNumberField = createTextField(database.generateNextInvoiceNumber("RPI"), "Auto-generated Invoice Number");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.setStyle("-fx-background-color: #f0f0f0;");

        // Supplier dropdown
        ComboBox<String> supplierCombo = new ComboBox<>();
        supplierCombo.setPromptText("Select Supplier");
        supplierCombo.getItems().addAll(database.getAllSupplierNames());
        supplierCombo.setPrefWidth(300);
        // Print selected supplier name when selected
        supplierCombo.setOnAction(e -> {
            String selectedSupplier = supplierCombo.getValue();
            if (selectedSupplier != null) {
            System.out.println("Selected supplier: " + selectedSupplier);
            }
        });

        DatePicker invoiceDatePicker = new DatePicker();
        invoiceDatePicker.setValue(LocalDate.now());

        // Invoice items section
        VBox itemsSection = new VBox(15);
        itemsSection.getStyleClass().add("section-container");

        Label itemsHeading = createSubheading("Invoice Items:");

        // Add item controls
        HBox addItemControls = new HBox(10);
        addItemControls.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> rawStockCombo = new ComboBox<>();
        rawStockCombo.setPromptText("Select Raw Stock");
        rawStockCombo.setPrefWidth(200);

        TextField quantityField = createTextField("Quantity");
        quantityField.setPrefWidth(100);

        TextField unitPriceField = createTextField("Unit Price");
        unitPriceField.setPrefWidth(100);
        unitPriceField.setEditable(true);
        unitPriceField.setStyle("-fx-border-color: #ddd;");

        Button addItemBtn = createActionButton("Add Item");

        // Populate raw stock dropdown
        List<Object[]> rawStocks = database.getAllRawStocksForDropdown();
        for (Object[] stock : rawStocks) {
            String displayName = String.format("%s (%s - %s)", stock[1], stock[2], stock[3]); // name (category - brand)
            rawStockCombo.getItems().add(displayName);
        }
        
        // Auto-fill price when raw stock is selected
        rawStockCombo.setOnAction(e -> {
            String selectedDisplay = rawStockCombo.getValue();
            if (selectedDisplay != null && !selectedDisplay.isEmpty()) {
                // Extract raw stock name from display (before the first parenthesis)
                String stockName = selectedDisplay.split(" \\(")[0];
                
                // Find the matching stock and get its unit price
                for (Object[] stock : rawStocks) {
                    if (stockName.equals(stock[1])) { // stock[1] is the item_name
                        double unitPrice = ((Number) stock[5]).doubleValue(); // stock[5] is unit_price
                        unitPriceField.setText(String.format("%.2f", unitPrice));
                        break;
                    }
                }
            } else {
                unitPriceField.clear();
            }
        });

        addItemControls.getChildren().addAll(
            new Label("Raw Stock:"), rawStockCombo,
            new Label("Qty:"), quantityField,
            new Label("Price:"), unitPriceField,
            addItemBtn
        );

        // Invoice items table
        TableView<RawStockPurchaseItem> itemsTable = createInvoiceItemsTable();

        // Total section
        HBox totalsSection = new HBox(20);
        totalsSection.setAlignment(Pos.CENTER_RIGHT);

        TextField discountField = createTextField("0", "Discount");
        discountField.setPrefWidth(100);

        TextField paidAmountField = createTextField("0", "Paid Amount");
        paidAmountField.setPrefWidth(100);

        Label totalLabel = new Label("Total: 0.00");
        totalLabel.getStyleClass().add("total-label");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        totalsSection.getChildren().addAll(
            new Label("Discount:"), discountField,
            new Label("Paid:"), paidAmountField,
            totalLabel
        );

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        Button submitBtn = createSubmitButton("Print Purchase Invoice");
        Button clearAllBtn = createActionButton("Clear All");
        actionButtons.getChildren().addAll(submitBtn, clearAllBtn);

        itemsSection.getChildren().addAll(itemsHeading, addItemControls, itemsTable, totalsSection);

        HBox optionsContainer = new HBox(10);
        optionsContainer.setAlignment(Pos.CENTER);
        optionsContainer.getChildren().addAll(createFormRow("Invoice Number:", invoiceNumberField),
            createFormRow("Supplier:", supplierCombo), createFormRow("Invoice Date:", invoiceDatePicker));

        form.getChildren().addAll(
            headingContainer,
            optionsContainer,
            
            itemsSection,
            actionButtons
        );

        // Event handlers
        addItemBtn.setOnAction(e -> handleAddInvoiceItem(
            rawStockCombo, quantityField, unitPriceField, 
            itemsTable, totalLabel, rawStocks
        ));

        // Auto-update total when discount or paid amount changes
        discountField.textProperty().addListener((obs, old, newVal) -> updateTotalLabel(itemsTable, discountField, totalLabel));
        paidAmountField.textProperty().addListener((obs, old, newVal) -> updateTotalLabel(itemsTable, discountField, totalLabel));

        clearAllBtn.setOnAction(e -> {
            if (!itemsTable.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all invoice items?");
                alert.setContentText("This will remove all items from the invoice.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    itemsTable.getItems().clear();
                    updateTotalLabel(itemsTable, discountField, totalLabel);
                }
            }
        });

        submitBtn.setOnAction(e -> {
            // First capture the supplier name before any database operations to ensure we have it
            String capturedSupplierName = supplierCombo.getValue();
            
            // Validate supplier name is not empty
            if (capturedSupplierName == null || capturedSupplierName.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please select a supplier before continuing");
                return;
            }
            
            // Validate that we have at least one item in the table
            if (itemsTable.getItems().isEmpty()) {
                // For testing purposes, add a sample item if there are none
                // RawStockPurchaseItem sampleItem = new RawStockPurchaseItem(
                //     1, // Sample ID
                //     "Sample Raw Stock", 
                //     "Sample Brand", 
                //     1.0, // quantity 
                //     100.0 // unit price
                // );
                // sampleItem.setUnitName("Unit");
                // itemsTable.getItems().add(sampleItem);
                // Update the total
                updateTotalLabel(itemsTable, discountField, totalLabel);
            }
            
            // Get supplier details early to ensure we have them
            Object[] supplierDetailsBeforeSubmit = database.getSupplierDetails(supplierCombo.getValue());
            // Debug print supplier details before any database operations
            System.out.println("DEBUG: Supplier details before submit:");
            if (supplierDetailsBeforeSubmit != null) {
                System.out.println("  Array length: " + supplierDetailsBeforeSubmit.length);
                for (int i = 0; i < supplierDetailsBeforeSubmit.length; i++) {
                    Object value = supplierDetailsBeforeSubmit[i];
                    System.out.println("  Index " + i + ": " + (value != null ? value.toString() + " (Type: " + value.getClass().getSimpleName() + ")" : "null"));
                }
            } else {
                System.out.println("  Supplier details array is null");
            }
            // Capture item data before any clearing happens
            List<RawStockPurchaseItem> capturedItems = new ArrayList<>(itemsTable.getItems());
            
            // Also capture form field values to prevent them from being lost when form is cleared
            double capturedDiscount = discountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText().trim());
            double capturedPaidAmount = paidAmountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(paidAmountField.getText().trim());
            
            // CRITICAL: Capture the invoice number BEFORE calling submit method to prevent using the new number generated after form clear
            String capturedInvoiceNumber = invoiceNumberField.getText();
            
            if (handleEnhancedPurchaseInvoiceSubmit(
                invoiceNumberField, supplierCombo, invoiceDatePicker,
                itemsTable, discountField, paidAmountField, totalLabel)) {
                    
                // Prepare invoice data for printing using the captured items
                List<Item> printItems = new ArrayList<>();
                double totalBeforeDiscount = 0.0;
                
                // Use the captured items instead of the current table items which might be cleared
                for (RawStockPurchaseItem item : capturedItems) {
                    double itemTotal = item.getQuantity() * item.getUnitPrice();
                    totalBeforeDiscount += itemTotal;
                    
                    printItems.add(new Item(
                        String.format("%s - %s", item.getRawStockName(), item.getUnitName()),
                        item.getQuantity().intValue(),
                        item.getUnitPrice(),
                        0.0  // No item-level discount - use invoice-level discount instead
                    ));
                }

                // Use the supplier details we captured earlier to prevent null values
                // Debug supplier details
                System.out.println("Supplier name: " + capturedSupplierName);
                if (supplierDetailsBeforeSubmit == null) {
                    System.out.println("WARNING: Supplier details are null for: " + capturedSupplierName);
                } else {
                    System.out.println("Supplier details array length: " + supplierDetailsBeforeSubmit.length);
                    for (int i = 0; i < supplierDetailsBeforeSubmit.length; i++) {
                        System.out.println("  details[" + i + "]: " + 
                            (supplierDetailsBeforeSubmit[i] != null ? supplierDetailsBeforeSubmit[i].toString() : "null"));
                    }
                }
                
                // Safely get supplier details, add fallbacks for missing information
                // Extract supplier details safely - updated with new array structure
                // supplierDetails array format: [id, name, address, tehsil, contact]
                String supplierAddress = (supplierDetailsBeforeSubmit != null && supplierDetailsBeforeSubmit.length > 2 && supplierDetailsBeforeSubmit[2] != null) ? 
                    supplierDetailsBeforeSubmit[2].toString() : "Khalil Abad, Amangarh, Nowshera";
                String supplierTehsil = (supplierDetailsBeforeSubmit != null && supplierDetailsBeforeSubmit.length > 3 && supplierDetailsBeforeSubmit[3] != null) ? 
                    supplierDetailsBeforeSubmit[3].toString() : "";
                String supplierContact = (supplierDetailsBeforeSubmit != null && supplierDetailsBeforeSubmit.length > 4 && supplierDetailsBeforeSubmit[4] != null) ? 
                    supplierDetailsBeforeSubmit[4].toString() : "";
                
                System.out.println("Using supplier details: Address=" + supplierAddress + 
                                  ", Tehsil=" + supplierTehsil + ", Contact=" + supplierContact);
                
                // Calculate amounts using captured values
                double invoiceDiscount = capturedDiscount;
                double invoicePaidAmount = capturedPaidAmount;
                double totalAfterDiscount = totalBeforeDiscount - invoiceDiscount;
                double balance = totalAfterDiscount - invoicePaidAmount;
                
                // Get supplier balance details for PDF
                Object[] balanceDetails = database.getSupplierInvoiceBalanceDetails(
                    capturedSupplierName, capturedInvoiceNumber, totalAfterDiscount, invoicePaidAmount
                );
                double previousBalance = (Double) balanceDetails[0];
                double totalBalance = (Double) balanceDetails[1];
                double netBalance = (Double) balanceDetails[2];

                // Create invoice data object using factory method
                InvoiceData invoiceData = InvoiceData.createPurchaseInvoice(
                    capturedInvoiceNumber,
                    invoiceDatePicker.getValue().format(DATE_FORMATTER),
                    capturedSupplierName,
                    "",  // Empty address as we're using metadata instead
                    printItems,
                    previousBalance  // Use calculated previous balance
                );

                // Set all balance details
                invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                invoiceData.setDiscountAmount(invoiceDiscount);
                invoiceData.setPaidAmount(invoicePaidAmount);
                
                // Set additional details including tehsil and contact as metadata
                invoiceData.setMetadata("tehsil", supplierTehsil);
                invoiceData.setMetadata("contact", supplierContact);
                // Since getCurrentOperator is not available, we can set as "System" or leave it null
                invoiceData.setOperator("System");
                // Since getCurrentOperator is not available, we can set as "System" or leave it null
                invoiceData.setOperator("System");

                // Use PrintManager for preview and print handling
                boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Raw Purchase");

                if (previewSuccess) {
                    showAlert("Success", "Invoice created and opened for preview.");
                } else {
                    // If preview fails, try printer selection dialog
                    boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Raw Purchase");
                    if (printSuccess) {
                        showAlert("Success", "Invoice created and sent to printer.");
                    } else {
                        showAlert("Warning", "Invoice created but printing failed. You can print it later from the records.");
                    }
                }
            }
        });

        return form;
    }

private static ScrollPane createRawStockReturnPurchaseInvoiceForm() {
    VBox mainContainer = new VBox(25);
    mainContainer.setPadding(new Insets(30));
    mainContainer.setAlignment(Pos.TOP_CENTER);
    mainContainer.getStyleClass().add("form-container");

    // Heading
    Label heading = createHeading("Create Raw Stock Return Purchase Invoice");

    // === Form Fields ===
    TextField returnInvoiceNumberField = createTextField("Return Invoice Number");
    returnInvoiceNumberField.setEditable(false);
    returnInvoiceNumberField.setText(database.generateReturnInvoiceNumber());

    ComboBox<String> originalInvoiceComboBox = new ComboBox<>();
    originalInvoiceComboBox.setPromptText("Select Original Invoice");
    originalInvoiceComboBox.setPrefWidth(300);

    ComboBox<String> supplierComboBox = new ComboBox<>();
    supplierComboBox.setPromptText("Auto-selected Supplier");
    supplierComboBox.setPrefWidth(300);
    supplierComboBox.setDisable(true);

    DatePicker returnDatePicker = new DatePicker(LocalDate.now());

    TextField totalReturnAmountField = createTextField("Total Return Amount");
    totalReturnAmountField.setEditable(false);
    totalReturnAmountField.setText("0.00");

    // === Tables & Buttons ===
    TableView<RawStockPurchaseItem> availableItemsTable = createAvailableItemsTable();
    TableView<RawStockPurchaseItem> selectedItemsTable = createSelectedReturnItemsTable();

    Button addItemsBtn = createSubmitButton("Add Selected Items →");
    Button removeItemsBtn = createSubmitButton("← Remove Selected Items");
    Button submitReturnInvoiceBtn = createSubmitButton("Submit Return Invoice");

    HBox actionButtons = new HBox(10, addItemsBtn, removeItemsBtn);
    actionButtons.setAlignment(Pos.CENTER);

    // === Grid Form Layout ===
    GridPane formGrid = new GridPane();
    formGrid.setHgap(15);
    formGrid.setVgap(15);
    formGrid.setPadding(new Insets(10));

    // First row
    formGrid.add(new Label("Return Invoice Number:"), 0, 0);
    formGrid.add(returnInvoiceNumberField, 1, 0);
    formGrid.add(new Label("Original Invoice:"), 2, 0);
    formGrid.add(originalInvoiceComboBox, 3, 0);
    formGrid.add(new Label("Supplier:"), 4, 0);
    formGrid.add(supplierComboBox, 5, 0);

    // Second row
    formGrid.add(new Label("Return Date:"), 0, 1);
    formGrid.add(returnDatePicker, 1, 1);
    formGrid.add(new Label("Total Return Amount:"), 2, 1);
    formGrid.add(totalReturnAmountField, 3, 1);

    // === Tables Section ===
    VBox tablesSection = new VBox(15,
        createSubheading("Available Items from Original Invoice:"),
        availableItemsTable,
        actionButtons,
        createSubheading("Selected Return Items:"),
        selectedItemsTable
    );

    // === Layout Composition ===
    mainContainer.getChildren().addAll(
        heading,
        formGrid,
        tablesSection,
        submitReturnInvoiceBtn
    );

    // === Load Dropdowns and Handlers ===
    loadOriginalInvoicesIntoDropdown(originalInvoiceComboBox);

    originalInvoiceComboBox.setOnAction(e -> {
        String selected = originalInvoiceComboBox.getValue();
        if (selected == null) return;

        for (Object[] invoice : database.getAllRawPurchaseInvoicesForDropdown()) {
            String displayText = String.format("%s - %s (%.2f)", invoice[1], invoice[2], (Double) invoice[4]);
            if (displayText.equals(selected)) {
                supplierComboBox.setValue((String) invoice[2]);
                int invoiceId = (Integer) invoice[0];
                System.out.println("DEBUG: Loading items for invoice ID: " + invoiceId);
                List<Object[]> items = database.getRawStockItemsByInvoiceId(invoiceId);
                ObservableList<RawStockPurchaseItem> itemsList = FXCollections.observableArrayList();
                for (Object[] item : items) {
                    System.out.println("DEBUG: Processing item with " + item.length + " elements");
                    RawStockPurchaseItem purchaseItem = new RawStockPurchaseItem(
                        (Integer) item[0],    // rawStockId
                        (String) item[1],     // rawStockName (item_name)
                        (String) item[2],     // brandName
                        (Double) item[3],     // quantity
                        (Double) item[4],     // unitPrice
                        (String) item[6],     // manufacturerName
                        (String) item[7],     // categoryName
                        (String) item[5]      // unitName
                    );
                    itemsList.add(purchaseItem);
                    System.out.println("DEBUG: Added item to list: " + purchaseItem.getRawStockName());
                }
                System.out.println("DEBUG: Setting " + itemsList.size() + " items to availableItemsTable");
                availableItemsTable.setItems(itemsList);
                break;
            }
        }
    });

    addItemsBtn.setOnAction(e -> {
        for (RawStockPurchaseItem item : new ArrayList<>(availableItemsTable.getSelectionModel().getSelectedItems())) {
            RawStockPurchaseItem returnItem = showReturnItemDialog(item);
            if (returnItem != null) {
                selectedItemsTable.getItems().add(returnItem);
                updateTotalAmount(selectedItemsTable, totalReturnAmountField);
            }
        }
    });

    removeItemsBtn.setOnAction(e -> {
        selectedItemsTable.getItems().removeAll(new ArrayList<>(selectedItemsTable.getSelectionModel().getSelectedItems()));
        updateTotalAmount(selectedItemsTable, totalReturnAmountField);
    });

    submitReturnInvoiceBtn.setOnAction(e -> {
        handleReturnInvoiceSubmit(returnInvoiceNumberField, originalInvoiceComboBox, supplierComboBox,
            returnDatePicker, selectedItemsTable, totalReturnAmountField);
    });

    // === Final Scrollable Container ===
    ScrollPane scrollPane = new ScrollPane(mainContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    scrollPane.setPadding(new Insets(20));
    scrollPane.setStyle("-fx-background-color:transparent;");
    return scrollPane;
}

    private static void loadOriginalInvoicesIntoDropdown(ComboBox<String> comboBox) {
        try {
            List<Object[]> invoices = database.getAllRawPurchaseInvoicesForDropdown();
            ObservableList<String> invoiceList = FXCollections.observableArrayList();
            
            for (Object[] invoice : invoices) {
                String displayText = String.format("%s - %s (%.2f)", 
                    invoice[1], invoice[2], (Double) invoice[4]); // invoice_number - supplier (total_amount)
                invoiceList.add(displayText);
            }
            
            comboBox.setItems(invoiceList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load original invoices");
        }
    }

private static TableView<RawStockPurchaseItem> createAvailableItemsTable() {
    TableView<RawStockPurchaseItem> table = new TableView<>();
    // table.setPrefHeight(200);
    
    TableColumn<RawStockPurchaseItem, String> nameCol = new TableColumn<>("Item Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
    nameCol.setPrefWidth(200);
    nameCol.setMinWidth(150);
    
    TableColumn<RawStockPurchaseItem, String> brandCol = new TableColumn<>("Brand");
    brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
    brandCol.setPrefWidth(120);
    brandCol.setMinWidth(100);
    
    TableColumn<RawStockPurchaseItem, Double> quantityCol = new TableColumn<>("Orig. Qty");
    quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    quantityCol.setPrefWidth(100);
    quantityCol.setMinWidth(80);
    quantityCol.setCellFactory(column -> new TableCell<RawStockPurchaseItem, Double>() {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%.0f", item));
            }
        }
    });
    
    TableColumn<RawStockPurchaseItem, Double> unitPriceCol = new TableColumn<>("Unit Price");
    unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
    unitPriceCol.setPrefWidth(120);
    unitPriceCol.setMinWidth(100);
    unitPriceCol.setCellFactory(column -> new TableCell<RawStockPurchaseItem, Double>() {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%.2f", item));
            }
        }
    });
    
    table.getColumns().addAll(nameCol, brandCol, quantityCol, unitPriceCol);
    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
    return table;
}
    private static TableView<RawStockPurchaseItem> createSelectedReturnItemsTable() {
        TableView<RawStockPurchaseItem> table = new TableView<>();
        // table.setPrefHeight(200);
        
        TableColumn<RawStockPurchaseItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(150);
        
        TableColumn<RawStockPurchaseItem, Double> quantityCol = new TableColumn<>("Return Qty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(120);
        quantityCol.setMinWidth(100);
        quantityCol.setCellFactory(column -> new TableCell<RawStockPurchaseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f", item));
                }
            }
        });
        
        TableColumn<RawStockPurchaseItem, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitPriceCol.setPrefWidth(120);
        unitPriceCol.setMinWidth(100);
        unitPriceCol.setCellFactory(column -> new TableCell<RawStockPurchaseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        TableColumn<RawStockPurchaseItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setPrefWidth(120);
        totalCol.setMinWidth(100);
        totalCol.setCellFactory(column -> new TableCell<RawStockPurchaseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        table.getColumns().addAll(nameCol, quantityCol, unitPriceCol, totalCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private static RawStockPurchaseItem showReturnItemDialog(RawStockPurchaseItem originalItem) {
        Dialog<RawStockPurchaseItem> dialog = new Dialog<>();
        dialog.setTitle("Return Item Details");
        
        // Get current stock quantity to display to user
        double currentStock = database.getCurrentRawStockQuantity(originalItem.getRawStockId());
        
        dialog.setHeaderText("Enter return details for: " + originalItem.getRawStockName() + 
                           "\nCurrent Stock Available: " + String.format("%.0f", currentStock));
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField quantityField = new TextField();
        double maxReturnQty = Math.min(originalItem.getQuantity(), currentStock);
        quantityField.setPromptText("Return Quantity (Max: " + String.format("%.0f", maxReturnQty) + ")");
        
        TextField unitPriceField = new TextField();
        unitPriceField.setText(String.valueOf(originalItem.getUnitPrice()));
        unitPriceField.setEditable(false);
        unitPriceField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        // Add info label about stock limitation
        Label infoLabel = new Label("Note: Return quantity limited by current stock availability");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        
        grid.add(new Label("Return Quantity:"), 0, 0);
        grid.add(quantityField, 1, 0);
        grid.add(new Label("Unit Price:"), 0, 1);
        grid.add(unitPriceField, 1, 1);
        grid.add(infoLabel, 0, 2, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    double returnQty = Double.parseDouble(quantityField.getText());
                    double unitPrice = Double.parseDouble(unitPriceField.getText());
                    
                    if (returnQty <= 0) {
                        showAlert("Invalid Input", "Return quantity must be greater than 0");
                        return null;
                    }
                    
                    if (returnQty > maxReturnQty) {
                        showAlert("Invalid Input", "Return quantity cannot exceed " + String.format("%.0f", maxReturnQty) + 
                                 "\n(Limited by current stock availability: " + String.format("%.0f", currentStock) + ")");
                        return null;
                    }
                    
                    return new RawStockPurchaseItem(
                        originalItem.getRawStockId(),
                        originalItem.getRawStockName(),
                        originalItem.getBrandName(),
                        returnQty,
                        unitPrice,
                        originalItem.getManufacturerName(),
                        originalItem.getCategoryName(),
                        originalItem.getUnitName()
                    );
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers");
                    return null;
                }
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }

    private static void updateTotalAmount(TableView<RawStockPurchaseItem> table, TextField totalField) {
        double total = table.getItems().stream()
                .mapToDouble(RawStockPurchaseItem::getTotalPrice)
                .sum();
        totalField.setText(String.format("%.2f", total));
    }

    private static void handleReturnInvoiceSubmit(TextField returnInvoiceNumberField,
                                                ComboBox<String> originalInvoiceComboBox,
                                                ComboBox<String> supplierComboBox,
                                                DatePicker returnDatePicker,
                                                TableView<RawStockPurchaseItem> selectedItemsTable,
                                                TextField totalReturnAmountField) {
        // Declare variables outside try block for use in catch block
        int originalInvoiceId = -1;
        int supplierId = -1;
        
        try {
            // Validate inputs
            if (originalInvoiceComboBox.getValue() == null || originalInvoiceComboBox.getValue().isEmpty()) {
                showAlert("Validation Error", "Please select an original invoice");
                return;
            }
            
            if (selectedItemsTable.getItems().isEmpty()) {
                showAlert("Validation Error", "Please select at least one item to return");
                return;
            }
            
            // Get original invoice ID
            List<Object[]> invoices = database.getAllRawPurchaseInvoicesForDropdown();
            
            for (Object[] invoice : invoices) {
                String displayText = String.format("%s - %s (%.2f)", 
                    invoice[1], invoice[2], (Double) invoice[4]);
                if (displayText.equals(originalInvoiceComboBox.getValue())) {
                    originalInvoiceId = (Integer) invoice[0];
                    supplierId = database.getSupplierIdByName((String) invoice[2]);
                    break;
                }
            }
            
            if (originalInvoiceId == -1) {
                showAlert("Error", "Could not find original invoice");
                return;
            }
            
            // Insert return invoice
            String returnInvoiceNumber = database.generateReturnInvoiceNumber();
            returnInvoiceNumberField.setText(returnInvoiceNumber); // Update field to show new number
            String returnDate = returnDatePicker.getValue().format(DATE_FORMATTER);
            double totalAmount = Double.parseDouble(totalReturnAmountField.getText());
            
            int returnInvoiceId = database.insertRawPurchaseReturnInvoiceAndGetId(
                returnInvoiceNumber, originalInvoiceId, supplierId, returnDate, totalAmount);
            
            if (returnInvoiceId > 0) {
                // Insert return invoice items and update stock quantities
                List<RawStockPurchaseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                boolean itemsInserted = database.insertRawPurchaseReturnInvoiceItems(returnInvoiceId, items);
                
                if (itemsInserted) {
                    // Prepare invoice data for printing
                    List<Item> printItems = new ArrayList<>();
                    for (RawStockPurchaseItem item : selectedItemsTable.getItems()) {
                        printItems.add(new Item(
                            item.getRawStockName() + " - " + item.getUnitName(),
                            (int)Math.floor(item.getQuantity()),
                            item.getUnitPrice(),
                            0.0  // No discount for return items
                        ));
                    }

                    String supplierName = supplierComboBox.getValue();
                    
                    // Get supplier details from database
                    Object[] supplierDetails = database.getSupplierDetails(supplierName);
                    
                    // Extract supplier details safely (using same structure as purchase invoice)
                    // supplierDetails array format: [id, name, address, tehsil, contact]
                    String supplierTehsil = (supplierDetails != null && supplierDetails.length > 3 && supplierDetails[3] != null) ? 
                        supplierDetails[3].toString() : "";
                    String supplierContact = (supplierDetails != null && supplierDetails.length > 4 && supplierDetails[4] != null) ? 
                        supplierDetails[4].toString() : "";
                    
                    // For return invoices, we need to manually calculate the correct previous balance
                    // This should be the balance that would exist if this return invoice didn't exist
                    // We'll use the current balance and add back this return amount to get the "previous" balance
                    double currentBalanceWithReturns = database.getSupplierCurrentBalance(supplierName);
                    
                    // Calculate return impact amount from print items
                    double returnImpactAmount = 0.0;
                    for (Item item : printItems) {
                        returnImpactAmount += item.getUnitPrice() * item.getQuantity();
                    }
                    
                    // Previous balance = current balance + return amount (since return reduces balance)
                    double previousBalance = currentBalanceWithReturns + returnImpactAmount;
                    
                    // For purchase return invoices: Total Balance = Previous Balance - Return Amount (we owe less to supplier)
                    double totalBalance = previousBalance - returnImpactAmount;
                    double netBalance = totalBalance; // No payment involved in returns, net balance equals total balance
                    
                    // Create invoice data with proper title
                    InvoiceData invoiceData = InvoiceData.createReturnPurchaseInvoice(
                        returnInvoiceNumber,
                        returnDate,
                        supplierName,
                        "", // Empty address as we'll use metadata
                        printItems,
                        previousBalance // Use calculated previous balance
                    );
                    
                    // Set all balance details for return
                    invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                    invoiceData.setPaidAmount(0.0); // No payment in returns
                    invoiceData.setDiscountAmount(0.0); // No discount in returns
                    
                    // Add supplier details as metadata
                    invoiceData.setMetadata("tehsil", supplierTehsil);
                    invoiceData.setMetadata("contact", supplierContact);

                    // Try to open invoice for print preview first
                    boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Purchase Return");

                    if (previewSuccess) {
                        showAlert("Success", "Return invoice created successfully and opened for preview!\n\nReturn Invoice Number: " + returnInvoiceNumber);
                    } else {
                        // Fallback to printer selection if preview fails
                        boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Purchase Return");
                        if (printSuccess) {
                            showAlert("Success", "Return invoice created and printed successfully!\n\nReturn Invoice Number: " + returnInvoiceNumber);
                        } else {
                            showAlert("Partial Success", "Return invoice created successfully but printing failed.\n\nReturn Invoice Number: " + returnInvoiceNumber);
                        }
                    }
                    
                    // Clear form
                    returnInvoiceNumberField.setText(database.generateReturnInvoiceNumber());
                    originalInvoiceComboBox.setValue(null);
                    supplierComboBox.setValue(null);
                    returnDatePicker.setValue(LocalDate.now());
                    selectedItemsTable.getItems().clear();
                    totalReturnAmountField.setText("0.00");
                    
                    // Refresh return invoices table
                    // This would need to be passed as a parameter or accessed differently
                } else {
                    showAlert("Error", "Failed to save return invoice items.\nThis could be due to insufficient stock quantities or database error.");
                }
            } else {
                showAlert("Error", "Failed to create return invoice");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                // If there's a unique constraint error, try once more with a new invoice number
                try {
                    String retryReturnInvoiceNumber = database.generateReturnInvoiceNumber();
                    returnInvoiceNumberField.setText(retryReturnInvoiceNumber);
                    
                    String returnDate = returnDatePicker.getValue().format(DATE_FORMATTER);
                    double totalAmount = Double.parseDouble(totalReturnAmountField.getText());
                    
                    int returnInvoiceId = database.insertRawPurchaseReturnInvoiceAndGetId(
                        retryReturnInvoiceNumber, originalInvoiceId, supplierId, returnDate, totalAmount);
                    
                    if (returnInvoiceId > 0) {
                        List<RawStockPurchaseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                        boolean itemsInserted = database.insertRawPurchaseReturnInvoiceItems(returnInvoiceId, items);
                        
                        if (itemsInserted) {
                            showAlert("Success", "Return invoice created successfully!\nStock quantities have been updated.");
                            
                            // Clear form
                            returnInvoiceNumberField.setText(database.generateReturnInvoiceNumber());
                            originalInvoiceComboBox.setValue(null);
                            supplierComboBox.setValue(null);
                            returnDatePicker.setValue(LocalDate.now());
                            selectedItemsTable.getItems().clear();
                            totalReturnAmountField.setText("0.00");
                        } else {
                            showAlert("Error", "Failed to save return invoice items on retry.\nThis could be due to insufficient stock quantities.");
                        }
                    } else {
                        showAlert("Error", "Failed to create return invoice even after retry");
                    }
                } catch (Exception retryException) {
                    retryException.printStackTrace();
                    showAlert("Error", "Return invoice number conflict occurred. Please try again. Error: " + retryException.getMessage());
                }
            } else {
                showAlert("Error", "An error occurred while creating the return invoice: " + e.getMessage());
            }
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void showUpdateStockDialog(
        RawStockRecord selectedStock, 
        TextField nameField, 
        ComboBox<String> brandCombo, 
        ComboBox<String> unitCombo,
        TextField quantityField, 
        TextField unitPriceField, 
        TableView<RawStockRecord> stockTable
    ) {
        // Create a dialog for updating the selected raw stock
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Raw Stock");
        dialog.setHeaderText("Update Raw Stock: " + selectedStock.getName());
        
        // Set the button types
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField updateNameField = createTextField(selectedStock.getName(), "Stock Name");
        
        ComboBox<String> updateBrandCombo = new ComboBox<>();
        updateBrandCombo.setPromptText("Select Brand");
        for (Brand b : database.getAllBrands()) {
            updateBrandCombo.getItems().add(b.nameProperty().get());
        }
        updateBrandCombo.setValue(selectedStock.getBrand());
        
        ComboBox<String> updateUnitCombo = new ComboBox<>();
        updateUnitCombo.setPromptText("Select Unit");
        updateUnitCombo.getItems().addAll(database.getAllUnits());
        updateUnitCombo.setValue(selectedStock.getUnit());
        
        TextField updateQuantityField = createTextField(String.format("%.0f", selectedStock.getQuantity()), "Quantity");
        TextField updateUnitPriceField = createTextField(String.format("%.2f", selectedStock.getUnitPrice()), "Unit Price");
        
        // Add fields to grid
        grid.add(new Label("Stock Name:"), 0, 0);
        grid.add(updateNameField, 1, 0);
        grid.add(new Label("Brand:"), 0, 1);
        grid.add(updateBrandCombo, 1, 1);
        grid.add(new Label("Unit:"), 0, 2);
        grid.add(updateUnitCombo, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(updateQuantityField, 1, 3);
        grid.add(new Label("Unit Price:"), 0, 4);
        grid.add(updateUnitPriceField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        updateNameField.requestFocus();
        
        // Handle dialog result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == updateButtonType) {
            // Validate inputs
            String name = updateNameField.getText().trim();
            String brand = updateBrandCombo.getValue();
            String unit = updateUnitCombo.getValue();
            String quantityText = updateQuantityField.getText().trim();
            String unitPriceText = updateUnitPriceField.getText().trim();
            
            if (name.isEmpty() || brand == null || unit == null || quantityText.isEmpty() || unitPriceText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "All fields are required");
                return;
            }
            
            try {
                double quantity = Double.parseDouble(quantityText);
                double unitPrice = Double.parseDouble(unitPriceText);
                
                // Update raw stock in database
                boolean success = database.updateRawStock(
                    selectedStock.getId(), name, brand, unit, quantity, unitPrice
                );
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Raw stock updated successfully!");
                    
                    // Populate the form fields with the updated values
                    nameField.setText(name);
                    brandCombo.setValue(brand);
                    unitCombo.setValue(unit);
                    quantityField.setText(String.format("%.0f", quantity));
                    unitPriceField.setText(String.format("%.2f", unitPrice));
                    
                    // Refresh the table
                    refreshRawStockTable(stockTable);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update raw stock");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for quantity and unit price");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
            }
        }
    }

    private static ScrollPane createRawStockUseInvoiceForm() {
        VBox form = new VBox(15); // Reduced spacing to save vertical space
        form.setPadding(new Insets(20)); // Reduced padding
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Raw Stock Use Invoice");

        // Auto-generated Invoice Number (Read-only)
        TextField invoiceNumberField = createTextField("Use Invoice Number");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.setStyle("-fx-background-color: #f0f0f0;");
        
        // Load auto-generated invoice number
        String autoInvoiceNumber = database.generateUseInvoiceNumber();
        invoiceNumberField.setText(autoInvoiceNumber);

        // Usage Date
        DatePicker usageDatePicker = new DatePicker();
        usageDatePicker.setValue(LocalDate.now());

        // Reference/Purpose field
        TextField referencePurposeField = createTextField("Reference/Purpose");

        // Compact form section for basic info
        VBox basicInfoSection = new VBox(10);
        basicInfoSection.getChildren().addAll(
            heading,
            createFormRow("Use Invoice Number:", invoiceNumberField),
            createFormRow("Usage Date:", usageDatePicker),
            createFormRow("Reference/Purpose:", referencePurposeField)
        );

        // Tables for Available and Selected Items (reduced heights)
        Label availableItemsLabel = createSubheading("Available Raw Stock Items:");
        TableView<RawStockUseItem> availableItemsTable = createRawStockItemsTable();
        // availableItemsTable.setPrefHeight(240); // Increased from 150
        // availableItemsTable.setMinHeight(240);
        // availableItemsTable.setMaxHeight(240);
 


        
        Label selectedItemsLabel = createSubheading("Selected Items for Use:");
        TableView<RawStockUseItem> selectedItemsTable = createSelectedUsageItemsTable();
        // selectedItemsTable.setPrefHeight(150); // Reduced from 200
        // selectedItemsTable.setMaxHeight(150);

        // Buttons for item selection
        HBox itemButtonsBox = new HBox(10);
        itemButtonsBox.setAlignment(Pos.CENTER);
        itemButtonsBox.setPadding(new Insets(10, 0, 10, 0));
        Button addItemBtn = new Button("Add Selected Items");
        Button removeItemBtn = new Button("Remove Selected Items");
        addItemBtn.getStyleClass().add("form-submit");
        removeItemBtn.getStyleClass().add("form-submit");

        // Total amount display
        Label totalLabel = createSubheading("Total Usage Amount: 0.00");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Load available items initially
        loadAvailableRawStockItems(availableItemsTable);

        // Add item button action
        addItemBtn.setOnAction(e -> {
            RawStockUseItem selectedItem = availableItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                showUsageQuantityDialog(selectedItem, selectedItemsTable, totalLabel);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from the available items table.");
            }
        });

        // Remove item button action
        removeItemBtn.setOnAction(e -> {
            RawStockUseItem selectedItem = selectedItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                selectedItemsTable.getItems().remove(selectedItem);
                updateTotalUsageAmount(selectedItemsTable, totalLabel);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from the selected items table.");
            }
        });

        itemButtonsBox.getChildren().addAll(addItemBtn, removeItemBtn);

        // Submit button
        Button submitBtn = createSubmitButton("Print Use Invoice");
        submitBtn.setOnAction(e -> {
            try {
                // Validate inputs
                if (selectedItemsTable.getItems().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select at least one item to use");
                    return;
                }

                String invoiceNumber = invoiceNumberField.getText();
                String usageDate = usageDatePicker.getValue().format(DATE_FORMATTER);
                String referencePurpose = referencePurposeField.getText().trim();
                
                // Calculate total amount
                double totalAmount = selectedItemsTable.getItems().stream()
                        .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                        .sum();

                // First, save the invoice to database
                int invoiceId = database.insertRawStockUseInvoiceAndGetId(invoiceNumber, usageDate, totalAmount, referencePurpose);
                
                if (invoiceId > 0) {
                    List<RawStockUseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                    boolean itemsInserted = database.insertRawStockUseInvoiceItems(invoiceId, items);
                    
                    if (itemsInserted) {
                        // Prepare items for printing
                        List<Item> printItems = new ArrayList<>();
                        for (RawStockUseItem item : items) {
                            printItems.add(new Item(
                                item.getRawStockName() + " - " + item.getUnitName(),
                                (int)Math.floor(item.getQuantityUsed()),
                                item.getUnitCost(),
                                0.0  // No discount for use items
                            ));
                        }

                        // Create invoice data for printing with proper type and metadata
                        InvoiceData invoiceData = new InvoiceData(
                            InvoiceData.TYPE_RAW_STOCK,
                            invoiceNumber,  // Invoice number
                            usageDate,      // Date
                            "STOCK USAGE INVOICE", // Title instead of supplier name
                            "",             // Empty address field
                            printItems,
                            0.0             // No previous balance for usage
                        );
                        
                        // Add reference/purpose as metadata
                        invoiceData.setMetadata("tehsil", "");
                        invoiceData.setMetadata("contact", "");
                        invoiceData.setMetadata("reference", referencePurpose);
                        invoiceData.setMetadata("totalAmount", totalAmount);

                        // Try to open invoice for print preview first
                        boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Stock Usage");
                        
                        if (previewSuccess) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                String.format("Raw Stock Use Invoice %s created successfully!\nTotal Amount: %.2f\n\nThe invoice has been opened for preview.", 
                                invoiceNumber, totalAmount));
                            clearUseInvoiceForm(invoiceNumberField, usageDatePicker, referencePurposeField, 
                                              selectedItemsTable, totalLabel);
                        } else {
                            // Fallback to printer selection if preview fails
                            boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Stock Usage");
                            if (printSuccess) {
                                showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    String.format("Raw Stock Use Invoice %s created and printed successfully!\nTotal Amount: %.2f", 
                                    invoiceNumber, totalAmount));
                                clearUseInvoiceForm(invoiceNumberField, usageDatePicker, referencePurposeField, 
                                                  selectedItemsTable, totalLabel);
                            } else {
                                showAlert(Alert.AlertType.WARNING, "Partial Success", 
                                    String.format("Raw Stock Use Invoice %s created but printing failed.\nTotal Amount: %.2f\nYou can print it later from the records.", 
                                    invoiceNumber, totalAmount));
                            }
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save invoice items.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create invoice.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
            }
        });

        // Add all components to form
        form.getChildren().addAll(
            basicInfoSection,
            availableItemsLabel,
            availableItemsTable,
            itemButtonsBox,
            selectedItemsLabel,
            selectedItemsTable,
            totalLabel,
            submitBtn
        );
        
        // Wrap in ScrollPane for responsiveness
        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // scrollPane.setPrefViewportHeight(600); // Set a reasonable height
        scrollPane.getStyleClass().add("scroll-pane");
        
        return scrollPane;
    }

    private static VBox createRawStockUsageReportForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Raw Stock Usage Report");

        // Date range selection
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(30)); // Default to last 30 days
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now());

        // Report type selection
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Summary Report", "Detailed Report", "Item Usage Report");
        reportTypeCombo.setValue("Summary Report");

        Button generateBtn = new Button("Generate Report");
        generateBtn.getStyleClass().add("form-submit");

        // Summary statistics area
        VBox summaryBox = new VBox(10);
        summaryBox.setPadding(new Insets(10));
        summaryBox.getStyleClass().add("summary-box");
        Label summaryHeading = createSubheading("Summary Statistics:");
        Label totalInvoicesLabel = new Label("Total Invoices: -");
        Label uniqueItemsLabel = new Label("Unique Items Used: -");
        Label totalValueLabel = new Label("Total Value Used: -");
        summaryBox.getChildren().addAll(summaryHeading, totalInvoicesLabel, uniqueItemsLabel, totalValueLabel);

        generateBtn.setOnAction(e -> {
            try {
                String startDate = startDatePicker.getValue().format(DATE_FORMATTER);
                String endDate = endDatePicker.getValue().format(DATE_FORMATTER);
                String reportType = reportTypeCombo.getValue();
                
                // Generate summary statistics for the main form
                Object[] summaryStats = database.getUsageSummaryStatistics(startDate, endDate);
                if (summaryStats != null && summaryStats.length > 0) {
                    totalInvoicesLabel.setText("Total Invoices: " + summaryStats[0]);
                    uniqueItemsLabel.setText("Unique Items Used: " + summaryStats[1]);
                    totalValueLabel.setText("Total Value Used: " + String.format("%.2f", Double.parseDouble(summaryStats[2].toString())));
                } else {
                    totalInvoicesLabel.setText("Total Invoices: 0");
                    uniqueItemsLabel.setText("Unique Items Used: 0");
                    totalValueLabel.setText("Total Value Used: 0.00");
                }
                
                // Show report in a new window
                showReportInNewWindow(reportType, startDate, endDate);
                
            } catch (Exception ex) {
                showAlert("Error", "Failed to generate report: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(
            heading,
            createFormRow("Start Date:", startDatePicker),
            createFormRow("End Date:", endDatePicker),
            createFormRow("Report Type:", reportTypeCombo),
            generateBtn,
            summaryBox
        );
        
        return form;
    }

    private static void generateSummaryReport(VBox reportContent, String startDate, String endDate) {
        try {
            List<Object[]> usageData = database.getRawStockUsageReportByDateRange(startDate, endDate);
            
            if (usageData.isEmpty()) {
                reportContent.getChildren().add(new Label("No usage data found for the selected date range."));
                return;
            }
            
            // Create table for summary data
            TableView<Object[]> table = new TableView<>();
            table.getStyleClass().add("table-view");
            // table.setPrefHeight(300);
            
            TableColumn<Object[], String> itemCol = new TableColumn<>("Item Name");
            itemCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0]));
            
            TableColumn<Object[], String> brandCol = new TableColumn<>("Brand");
            brandCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
            
            TableColumn<Object[], String> quantityCol = new TableColumn<>("Total Quantity");
            quantityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[2])));
            
            TableColumn<Object[], String> unitCostCol = new TableColumn<>("Unit Cost");
            unitCostCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[3])));
            
            TableColumn<Object[], String> valueCol = new TableColumn<>("Total Value");
            valueCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[4])));
            
            TableColumn<Object[], String> usageCountCol = new TableColumn<>("Usage Count");
            usageCountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[5].toString()));
            
            table.getColumns().addAll(itemCol, brandCol, quantityCol, unitCostCol, valueCol, usageCountCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            ObservableList<Object[]> data = FXCollections.observableArrayList(usageData);
            table.setItems(data);
            
            reportContent.getChildren().addAll(
                new Label("Item Usage Summary from " + startDate + " to " + endDate) {{
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                    setStyle("-fx-text-fill: #1a1a1a;");
                }},
                table
            );
            
        } catch (Exception e) {
            reportContent.getChildren().add(new Label("Error generating summary report: " + e.getMessage()));
        }
    }

    private static void generateDetailedReport(VBox reportContent, String startDate, String endDate) {
        try {
            List<Object[]> detailData = database.getRawStockUsageDetails(startDate, endDate);
            
            if (detailData.isEmpty()) {
                reportContent.getChildren().add(new Label("No detailed usage data found for the selected date range."));
                return;
            }
            
            // Create table for detailed data
            TableView<Object[]> table = new TableView<>();
            table.getStyleClass().add("table-view");
            // table.setPrefHeight(350);
            
            TableColumn<Object[], String> invoiceCol = new TableColumn<>("Invoice #");
            invoiceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0]));
            
            TableColumn<Object[], String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
            
            TableColumn<Object[], String> referenceCol = new TableColumn<>("Reference");
            referenceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[2]));
            
            TableColumn<Object[], String> itemCol = new TableColumn<>("Item");
            itemCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]));
            
            TableColumn<Object[], String> brandCol = new TableColumn<>("Brand");
            brandCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[4]));
            
            TableColumn<Object[], String> quantityCol = new TableColumn<>("Quantity");
            quantityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[5])));
            
            TableColumn<Object[], String> unitCostCol = new TableColumn<>("Unit Cost");
            unitCostCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[6])));
            
            TableColumn<Object[], String> totalCol = new TableColumn<>("Total");
            totalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
            
            table.getColumns().addAll(invoiceCol, dateCol, referenceCol, itemCol, brandCol, quantityCol, unitCostCol, totalCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            ObservableList<Object[]> data = FXCollections.observableArrayList(detailData);
            table.setItems(data);
            
            reportContent.getChildren().addAll(
                new Label("Detailed Usage Report from " + startDate + " to " + endDate) {{
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                    setStyle("-fx-text-fill: #1a1a1a;");
                }},
                table
            );
            
        } catch (Exception e) {
            reportContent.getChildren().add(new Label("Error generating detailed report: " + e.getMessage()));
        }
    }

    private static void generateItemUsageReport(VBox reportContent, String startDate, String endDate) {
        try {
            List<Object[]> usageData = database.getRawStockUsageReportByDateRange(startDate, endDate);
            
            if (usageData.isEmpty()) {
                reportContent.getChildren().add(new Label("No item usage data found for the selected date range."));
                return;
            }
            
            // Group by item and create charts/statistics
            reportContent.getChildren().add(new Label("Item Usage Analysis from " + startDate + " to " + endDate) {{
                setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                setStyle("-fx-text-fill: #1a1a1a;");
            }});
            
            for (Object[] item : usageData) {
                VBox itemBox = new VBox(5);
                itemBox.setPadding(new Insets(10));
                itemBox.getStyleClass().add("item-usage-box");
                
                Label itemLabel = new Label(item[0] + " (" + item[1] + ")");
                itemLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                itemLabel.getStyleClass().add("item-name");
                
                Label quantityLabel = new Label("Quantity Used: " + item[2]);
                quantityLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                quantityLabel.setStyle("-fx-text-fill: #1a1a1a;");
                
                Label valueLabel = new Label("Total Value: " + String.format("%.2f", Double.parseDouble(item[3].toString())));
                valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                valueLabel.setStyle("-fx-text-fill: #1a1a1a;");
                
                itemBox.getChildren().addAll(itemLabel, quantityLabel, valueLabel);
                reportContent.getChildren().add(itemBox);
            }
            
        } catch (Exception e) {
            reportContent.getChildren().add(new Label("Error generating item usage report: " + e.getMessage()));
        }
    }

    // Method to show reports in a new window
    private static void showReportInNewWindow(String reportType, String startDate, String endDate) {
        Stage reportStage = new Stage();
        reportStage.setTitle("Raw Stock Usage Report - " + reportType + " (" + startDate + " to " + endDate + ")");
        reportStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Raw Stock Usage Report - " + reportType);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        
        // Date range info
        Label dateRangeLabel = new Label("Date Range: " + startDate + " to " + endDate);
        dateRangeLabel.setFont(Font.font("Segoe UI", 14));
        dateRangeLabel.setStyle("-fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
        
        // Report content area
        ScrollPane reportScrollPane = new ScrollPane();
        reportScrollPane.setFitToWidth(true);
        reportScrollPane.setPrefHeight(500);
        VBox reportContent = new VBox(10);
        reportScrollPane.setContent(reportContent);
        
        // Generate the selected report type
        try {
            switch (reportType) {
                case "Summary Report":
                    generateSummaryReport(reportContent, startDate, endDate);
                    break;
                case "Detailed Report":
                    generateDetailedReport(reportContent, startDate, endDate);
                    break;
                case "Item Usage Report":
                    generateItemUsageReport(reportContent, startDate, endDate);
                    break;
            }
        } catch (Exception ex) {
            reportContent.getChildren().add(new Label("Error generating report: " + ex.getMessage()));
        }
        
        // Control buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        
        closeBtn.setOnAction(e -> reportStage.close());
        
        buttonBox.getChildren().add(closeBtn);
        
        mainLayout.getChildren().addAll(titleLabel, dateRangeLabel, reportScrollPane, buttonBox);
        
        Scene scene = new Scene(mainLayout, 1000, 700);
        reportStage.setScene(scene);
        reportStage.showAndWait();
    }

    // Export functionality for reports
    private static void exportReportToCSV(String reportType, String startDate, String endDate) {
        try {
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Raw Stock Usage Report");
            fileChooser.setInitialFileName("RawStockUsageReport_" + reportType.replace(" ", "_") + "_" + 
                                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            
            // Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                // Generate CSV content based on report type
                StringBuilder csvContent = new StringBuilder();
                
                switch (reportType) {
                    case "Summary Report":
                        generateCSVSummaryReport(csvContent, startDate, endDate);
                        break;
                    case "Detailed Report":
                        generateCSVDetailedReport(csvContent, startDate, endDate);
                        break;
                    case "Item Usage Report":
                        generateCSVItemUsageReport(csvContent, startDate, endDate);
                        break;
                }

                // Write to file
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(csvContent.toString());
                }

                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Export Successful");
                successAlert.setHeaderText("Report Exported Successfully");
                successAlert.setContentText("The " + reportType + " has been exported to:\n" + file.getAbsolutePath() + 
                                          "\n\nTotal size: " + csvContent.length() + " characters");
                successAlert.showAndWait();
            }

        } catch (Exception ex) {
            showAlert("Export Error", "Failed to export report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Generate printable summary report content
    private static void generatePrintableSummaryReport(StringBuilder content, String startDate, String endDate) {
        try {
            List<Object[]> usageData = database.getRawStockUsageReportByDateRange(startDate, endDate);
            
            if (usageData.isEmpty()) {
                content.append("No usage data found for the selected date range.\n");
                return;
            }

            content.append("ITEM USAGE SUMMARY\n");
            content.append(String.format("%-30s %-20s %-15s %-15s %-15s %-10s\n", 
                          "Item Name", "Brand", "Total Qty", "Unit Cost", "Total Value", "Usage Count"));
            content.append("-".repeat(110)).append("\n");

            for (Object[] item : usageData) {
                content.append(String.format("%-30s %-20s %-15.2f %-15.2f %-15.2f %-10s\n",
                    truncateString((String) item[0], 28),
                    truncateString((String) item[1], 18),
                    (Double) item[2],
                    (Double) item[3],
                    (Double) item[4],
                    item[5].toString()));
            }

        } catch (Exception e) {
            content.append("Error generating summary report: ").append(e.getMessage()).append("\n");
        }
    }

    // Generate printable detailed report content
    private static void generatePrintableDetailedReport(StringBuilder content, String startDate, String endDate) {
        try {
            List<Object[]> detailData = database.getRawStockUsageDetails(startDate, endDate);
            
            if (detailData.isEmpty()) {
                content.append("No detailed usage data found for the selected date range.\n");
                return;
            }

            content.append("DETAILED USAGE REPORT\n");
            content.append(String.format("%-15s %-12s %-20s %-25s %-15s %-10s %-12s %-12s\n",
                          "Invoice #", "Date", "Reference", "Item", "Brand", "Quantity", "Unit Cost", "Total"));
            content.append("-".repeat(130)).append("\n");

            for (Object[] item : detailData) {
                content.append(String.format("%-15s %-12s %-20s %-25s %-15s %-10.2f %-12.2f %-12.2f\n",
                    truncateString((String) item[0], 13),
                    truncateString((String) item[1], 10),
                    truncateString((String) item[2], 18),
                    truncateString((String) item[3], 23),
                    truncateString((String) item[4], 13),
                    (Double) item[5],
                    (Double) item[6],
                    (Double) item[7]));
            }

        } catch (Exception e) {
            content.append("Error generating detailed report: ").append(e.getMessage()).append("\n");
        }
    }

    // Generate printable item usage report content
    private static void generatePrintableItemUsageReport(StringBuilder content, String startDate, String endDate) {
        try {
            List<Object[]> usageData = database.getRawStockUsageReportByDateRange(startDate, endDate);
            
            if (usageData.isEmpty()) {
                content.append("No item usage data found for the selected date range.\n");
                return;
            }

            content.append("ITEM USAGE ANALYSIS\n");
            content.append("-".repeat(60)).append("\n");

            for (Object[] item : usageData) {
                content.append("Item: ").append(item[0]).append(" (").append(item[1]).append(")\n");
                content.append("Quantity Used: ").append(item[2]).append("\n");
                content.append("Total Value: ").append(String.format("%.2f", Double.parseDouble(item[3].toString()))).append("\n");
                content.append("-".repeat(40)).append("\n");
            }

        } catch (Exception e) {
            content.append("Error generating item usage report: ").append(e.getMessage()).append("\n");
        }
    }

    // Generate CSV summary report content
    private static void generateCSVSummaryReport(StringBuilder csvContent, String startDate, String endDate) {
        try {
            csvContent.append("Raw Stock Usage Summary Report\n");
            csvContent.append("Date Range:,").append(startDate).append(" to ").append(endDate).append("\n");
            csvContent.append("Generated:,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            csvContent.append("Item Name,Brand,Total Quantity,Unit Cost,Total Value,Usage Count\n");
            
            List<Object[]> usageData = database.getRawStockUsageReportByDateRange(startDate, endDate);
            for (Object[] item : usageData) {
                csvContent.append(String.format("\"%s\",\"%s\",%.2f,%.2f,%.2f,%s\n",
                    escapeCSV((String) item[0]),
                    escapeCSV((String) item[1]),
                    (Double) item[2],
                    (Double) item[3],
                    (Double) item[4],
                    item[5].toString()));
            }

        } catch (Exception e) {
            csvContent.append("Error generating CSV summary report: ").append(e.getMessage()).append("\n");
        }
    }

    // Generate CSV detailed report content
    private static void generateCSVDetailedReport(StringBuilder csvContent, String startDate, String endDate) {
        try {
            csvContent.append("Raw Stock Usage Detailed Report\n");
            csvContent.append("Date Range:,").append(startDate).append(" to ").append(endDate).append("\n");
            csvContent.append("Generated:,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            csvContent.append("Invoice Number,Date,Reference,Item,Brand,Quantity,Unit Cost,Total\n");
            
            List<Object[]> detailData = database.getRawStockUsageDetails(startDate, endDate);
            for (Object[] item : detailData) {
                csvContent.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f\n",
                    escapeCSV((String) item[0]),
                    escapeCSV((String) item[1]),
                    escapeCSV((String) item[2]),
                    escapeCSV((String) item[3]),
                    escapeCSV((String) item[4]),
                    (Double) item[5],
                    (Double) item[6],
                    (Double) item[7]));
            }

        } catch (Exception e) {
            csvContent.append("Error generating CSV detailed report: ").append(e.getMessage()).append("\n");
        }
    }

    // Generate CSV item usage report content
    private static void generateCSVItemUsageReport(StringBuilder csvContent, String startDate, String endDate) {
        try {
            csvContent.append("Raw Stock Item Usage Analysis Report\n");
            csvContent.append("Date Range:,").append(startDate).append(" to ").append(endDate).append("\n");
            csvContent.append("Generated:,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            csvContent.append("Item Name,Brand,Quantity Used,Total Value\n");
            
            List<Object[]> usageData = database.getRawStockUsageReportByDateRange(startDate, endDate);
            for (Object[] item : usageData) {
                csvContent.append(String.format("\"%s\",\"%s\",%.2f,%.2f\n",
                    escapeCSV((String) item[0]),
                    escapeCSV((String) item[1]),
                    (Double) item[2],
                    Double.parseDouble(item[3].toString())));
            }

        } catch (Exception e) {
            csvContent.append("Error generating CSV item usage report: ").append(e.getMessage()).append("\n");
        }
    }

    // Helper method to truncate strings for printing
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 2) + "..";
    }

    // Helper method to escape CSV values
    private static String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    // Helper methods for UI components
    private static Label createHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-heading");
        label.setFont(Font.font(18));
        return label;
    }

    private static Label createSubheading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-subheading");
        return label;
    }

    private static TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private static TextField createTextField(String text, String prompt) {
        TextField field = new TextField(text);
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private static ListView<String> createListView() {
        ListView<String> listView = new ListView<>();
        // listView.setPrefHeight(200);
        listView.getStyleClass().add("category-list");
        return listView;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        return button;
    }

    private static HBox createFormRow(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        HBox row = new HBox(10, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");
        return row;
    }

    // Enhanced methods for new invoice functionality
    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        return button;
    }

    private static TableView<RawStockPurchaseItem> createInvoiceItemsTable() {
        TableView<RawStockPurchaseItem> table = new TableView<>();
        table.getStyleClass().add("form-table");

        TableColumn<RawStockPurchaseItem, String> nameCol = new TableColumn<>("Raw Stock");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("rawStockName"));
        nameCol.setPrefWidth(150);

        TableColumn<RawStockPurchaseItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(100);

        TableColumn<RawStockPurchaseItem, String> manufacturerCol = new TableColumn<>("Manufacturer");
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturerName"));
        manufacturerCol.setPrefWidth(120);

        TableColumn<RawStockPurchaseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandCol.setPrefWidth(100);

        TableColumn<RawStockPurchaseItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        unitCol.setPrefWidth(80);

        TableColumn<RawStockPurchaseItem, Double> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<RawStockPurchaseItem, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(80);

        TableColumn<RawStockPurchaseItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setPrefWidth(80);

        // Add delete column
        TableColumn<RawStockPurchaseItem, Void> deleteCol = new TableColumn<>("Action");
        deleteCol.setPrefWidth(60);
        deleteCol.setCellFactory(param -> new TableCell<RawStockPurchaseItem, Void>() {
            private final Button deleteBtn = new Button("✖");

            {
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    RawStockPurchaseItem item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        table.getColumns().addAll(nameCol, categoryCol, manufacturerCol, brandCol, unitCol, qtyCol, priceCol, totalCol, deleteCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private static void handleAddInvoiceItem(ComboBox<String> rawStockCombo, TextField quantityField, 
                                           TextField unitPriceField, TableView<RawStockPurchaseItem> itemsTable, 
                                           Label totalLabel, List<Object[]> rawStocks) {
        String selectedRawStock = rawStockCombo.getValue();
        String quantityText = quantityField.getText().trim();
        String unitPriceText = unitPriceField.getText().trim();

        if (selectedRawStock == null || quantityText.isEmpty() || unitPriceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select raw stock and enter quantity and unit price");
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityText);
            double unitPrice = Double.parseDouble(unitPriceText);

            if (quantity <= 0 || unitPrice <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Quantity and unit price must be positive numbers");
                return;
            }

            // Find the selected raw stock details
            Object[] selectedStock = null;
            for (Object[] stock : rawStocks) {
                String displayName = String.format("%s (%s - %s)", stock[1], stock[2], stock[3]);
                if (displayName.equals(selectedRawStock)) {
                    selectedStock = stock;
                    break;
                }
            }

            if (selectedStock != null) {
                // Check if item already exists in table
                for (RawStockPurchaseItem existingItem : itemsTable.getItems()) {
                    if (existingItem.getRawStockId() == (Integer) selectedStock[0]) {
                        showAlert(Alert.AlertType.ERROR, "Error", "This raw stock is already in the invoice. Please edit the existing item or remove it first.");
                        return;
                    }
                }

                RawStockPurchaseItem item = new RawStockPurchaseItem(
                    (Integer) selectedStock[0],  // rawStockId
                    (String) selectedStock[1],   // rawStockName
                    (String) selectedStock[3],   // brandName
                    quantity,                    // quantity
                    unitPrice,                   // unitPrice
                    (String) selectedStock[6],   // manufacturerName
                    (String) selectedStock[2],   // categoryName
                    (String) selectedStock[4]    // unitName
                );

                itemsTable.getItems().add(item);
                updateTotalLabel(itemsTable, null, totalLabel);

                // Clear fields
                rawStockCombo.setValue(null);
                quantityField.clear();
                unitPriceField.clear();
            }

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for quantity and unit price");
        }
    }

    private static void updateTotalLabel(TableView<RawStockPurchaseItem> itemsTable, TextField discountField, Label totalLabel) {
        double subtotal = itemsTable.getItems().stream()
            .mapToDouble(RawStockPurchaseItem::getTotalPrice)
            .sum();

        double discount = 0.0;
        if (discountField != null && !discountField.getText().trim().isEmpty()) {
            try {
                discount = Double.parseDouble(discountField.getText().trim());
            } catch (NumberFormatException e) {
                // Ignore invalid discount
            }
        }

        double total = subtotal - discount;
        totalLabel.setText(String.format("Total: %.2f", total));
    }

    private static boolean handleEnhancedPurchaseInvoiceSubmit(
        TextField invoiceNumberField, ComboBox<String> supplierCombo, DatePicker invoiceDatePicker,
        TableView<RawStockPurchaseItem> itemsTable, TextField discountField, 
        TextField paidAmountField, Label totalLabel) {
            
        // Validate required fields
        if (supplierCombo.getValue() == null || supplierCombo.getValue().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a supplier");
            return false;
        }
        
        if (itemsTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please add at least one item to the invoice");
            return false;
        }

        // Use the invoice number already displayed in the field to maintain consistency
        String invoiceNumber = invoiceNumberField.getText();
        
        String selectedSupplier = supplierCombo.getValue();
        String invoiceDate = invoiceDatePicker.getValue().format(DATE_FORMATTER);

        if (selectedSupplier == null || itemsTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a supplier and add at least one item");
            return false;
        }

        try {
            double discount = discountField.getText().trim().isEmpty() ? 0.0 : 
                             Double.parseDouble(discountField.getText().trim());
            double paidAmount = paidAmountField.getText().trim().isEmpty() ? 0.0 : 
                               Double.parseDouble(paidAmountField.getText().trim());

            // Calculate total
            double subtotal = itemsTable.getItems().stream()
                .mapToDouble(RawStockPurchaseItem::getTotalPrice)
                .sum();
            double totalAmount = subtotal; // This should be the gross amount before discount

            if (discount < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Discount cannot be negative");
                return false;
            }

            if (paidAmount < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Paid amount cannot be negative");
                return false;
            }

            // Convert table items to list and validate them
            List<RawStockPurchaseItem> items = new ArrayList<>(itemsTable.getItems());
            for (RawStockPurchaseItem item : items) {
                if (item.getQuantity() <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                             "Invalid quantity for item " + item.getRawStockName() + ". Must be greater than 0.");
                    return false;
                }
                if (item.getUnitPrice() <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                             "Invalid unit price for item " + item.getRawStockName() + ". Must be greater than 0.");
                    return false;
                }
            }

            // Use simplified invoice insertion with validated data
            // Pass: grossAmount, discountAmount, paidAmount
            boolean success = database.insertSimpleRawPurchaseInvoice(
                invoiceNumber, selectedSupplier, invoiceDate, totalAmount, discount, paidAmount, items
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    String.format("Purchase Invoice %s created successfully!\nTotal Amount: %.2f", 
                    invoiceNumber, totalAmount));

                // Clear form
                clearPurchaseInvoiceForm(invoiceNumberField, supplierCombo, itemsTable, 
                                       discountField, paidAmountField, totalLabel);
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create purchase invoice");
                return false;
            }

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for discount and paid amount");
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE constraint failed")) {
                // If there's a unique constraint error, try once more with a new invoice number
                try {
                    String retryInvoiceNumber = database.generateNextInvoiceNumber("RPI");
                    invoiceNumberField.setText(retryInvoiceNumber);
                    
                    // Calculate total again
                    double discount = discountField.getText().trim().isEmpty() ? 0.0 : 
                                     Double.parseDouble(discountField.getText().trim());
                    double paidAmount = paidAmountField.getText().trim().isEmpty() ? 0.0 : 
                                       Double.parseDouble(paidAmountField.getText().trim());
                    double subtotal = itemsTable.getItems().stream()
                        .mapToDouble(RawStockPurchaseItem::getTotalPrice)
                        .sum();
                    double totalAmount = subtotal; // Keep gross amount before discount
                    
                    List<RawStockPurchaseItem> items = new ArrayList<>(itemsTable.getItems());
                    boolean success = database.insertSimpleRawPurchaseInvoice(
                        retryInvoiceNumber, selectedSupplier, invoiceDate, totalAmount, discount, paidAmount, items
                    );
                    
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                            String.format("Purchase Invoice %s created successfully!\nTotal Amount: %.2f", 
                            retryInvoiceNumber, totalAmount));
                        clearPurchaseInvoiceForm(invoiceNumberField, supplierCombo, itemsTable, 
                                               discountField, paidAmountField, totalLabel);
                        return true;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to create purchase invoice even after retry");
                        return false;
                    }
                } catch (Exception retryException) {
                    retryException.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Invoice number conflict occurred. Please try again. Error: " + retryException.getMessage());
                    return false;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving to database: " + ex.getMessage());
                return false;
            }
        }
        return false;
    }

    private static void clearPurchaseInvoiceForm(TextField invoiceNumberField, ComboBox<String> supplierCombo,
                                               TableView<RawStockPurchaseItem> itemsTable, TextField discountField,
                                               TextField paidAmountField, Label totalLabel) {
        // Generate new invoice number
        invoiceNumberField.setText(database.generateNextInvoiceNumber("RPI"));
        supplierCombo.setValue(null);
        itemsTable.getItems().clear();
        discountField.setText("0");
        paidAmountField.setText("0");
        updateTotalLabel(itemsTable, discountField, totalLabel);
    }

    // Form submission handlers
    private static void handleRawStockSubmit(
        TextField nameField, ComboBox<String> brandCombo, ComboBox<String> categoryCombo, ComboBox<String> unitCombo, ComboBox<String> supplierCombo,
        TextField quantityField, TextField unitPriceField,
        TableView<RawStockRecord> stockTable
    ) {
        String name = nameField.getText().trim();
        String brand = brandCombo.getValue();
        String category = categoryCombo.getValue();
        String unit = unitCombo.getValue();
        String supplier = supplierCombo.getValue(); // Optional
        String quantityText = quantityField.getText().trim();
        String unitPriceText = unitPriceField.getText().trim();

        if (name.isEmpty() || brand == null || category == null || unit == null || unitPriceText.isEmpty() || quantityText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name, Brand, Category, Unit, Quantity, and Unit Price are required");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            double unitPrice = Double.parseDouble(unitPriceText);
            
            // Insert into RawStock table using the updated database method with category
            boolean success = database.insertRawStock(name, category, brand, unit, quantity, unitPrice, 0.0);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Raw stock registered successfully!");
                
                // Clear fields
                nameField.clear();
                brandCombo.setValue(null);
                categoryCombo.setValue(null);
                unitCombo.setValue(null);
                supplierCombo.setValue(null);
                quantityField.clear();
                unitPriceField.clear();
                
                // Refresh table
                refreshRawStockTable(stockTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register raw stock. Please check your entries.");
            }
            
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity must be a whole number and Unit Price must be a valid number");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
        }
    }

    private static void handlePurchaseInvoiceSubmit(
        TextField invoiceNumberField, TextField supplierField, DatePicker invoiceDatePicker,
        TextField stockField, TextField quantityField, TextField unitPriceField,
        TextField discountField, TextField paidAmountField, ListView<String> invoiceView
    ) {
        if (invoiceNumberField.getText().trim().isEmpty() ||
            supplierField.getText().trim().isEmpty() ||
            stockField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Required fields are missing");
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityField.getText().trim());
            double unitPrice = Double.parseDouble(unitPriceField.getText().trim());
            double discount = Double.parseDouble(discountField.getText().trim());
            double paid = Double.parseDouble(paidAmountField.getText().trim());
            double total = quantity * unitPrice - discount;

            String invoiceEntry = String.format("%s | %s | %s | %s x %s = %s | Paid: %s",
                invoiceNumberField.getText().trim(),
                invoiceDatePicker.getValue().format(DATE_FORMATTER),
                supplierField.getText().trim(),
                quantity,
                unitPrice,
                total,
                paid);
            
            invoiceView.getItems().add(invoiceEntry);
            
            // Clear fields
            invoiceNumberField.clear();
            supplierField.clear();
            stockField.clear();
            quantityField.clear();
            unitPriceField.clear();
            discountField.setText("0");
            paidAmountField.setText("0");
            invoiceDatePicker.setValue(LocalDate.now());

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers");
        }
    }

    // --------------------------
    // Raw Stock Use Invoice Helper Methods
    // --------------------------
    
    /**
     * Load raw stocks into the combo box for selection
     */
    private static void loadRawStocksIntoComboBox(ComboBox<RawStockUseItem> comboBox) {
        try {
            List<Object[]> rawStocks = database.getAllRawStocksWithUnitsForDropdown();
            ObservableList<RawStockUseItem> items = FXCollections.observableArrayList();
            
            for (Object[] row : rawStocks) {
                int rawStockId = (Integer) row[0];
                String rawStockName = (String) row[1];
                String categoryName = (String) row[2];
                String brandName = (String) row[3];
                String unitName = (String) row[4];
                double availableQuantity = (Double) row[5];
                double unitCost = (Double) row[6];
                
                RawStockUseItem item = new RawStockUseItem(
                    rawStockId, rawStockName, categoryName, brandName, 
                    unitName, 0.0, unitCost, availableQuantity
                );
                items.add(item);
            }
            
            comboBox.setItems(items);
            
            // Custom cell factory to display formatted text
            comboBox.setCellFactory(listView -> new ListCell<RawStockUseItem>() {
                @Override
                protected void updateItem(RawStockUseItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDisplayString());
                    }
                }
            });
            
            comboBox.setButtonCell(new ListCell<RawStockUseItem>() {
                @Override
                protected void updateItem(RawStockUseItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Raw Stock Item");
                    } else {
                        setText(item.getDisplayString());
                    }
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load raw stock items: " + e.getMessage());
        }
    }
    
    /**
     * Create table for displaying available raw stock items
     */
    private static TableView<RawStockUseItem> createRawStockItemsTable() {
        TableView<RawStockUseItem> table = new TableView<>();
        
        TableColumn<RawStockUseItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(cellData -> {
            RawStockUseItem item = cellData.getValue();
            String itemName = item.getRawStockName();
            String unitName = item.getUnitName();
            String displayText = itemName + " - " + unitName;
            return new SimpleStringProperty(displayText);
        });
        nameCol.setPrefWidth(200); // Increased to accommodate "Name - Unit" format
        
        TableColumn<RawStockUseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandCol.setPrefWidth(120); // Reduced from 150
        
        TableColumn<RawStockUseItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(100); // Reduced from 120
        
        TableColumn<RawStockUseItem, Double> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        availableCol.setPrefWidth(80); // Reduced from 100
        
        TableColumn<RawStockUseItem, Double> costCol = new TableColumn<>("Unit Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        costCol.setPrefWidth(80); // Reduced from 100
        
        // Format currency columns
        availableCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        costCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        table.getColumns().addAll(nameCol, brandCol, categoryCol, availableCol, costCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Create table for displaying selected usage items
     */
    private static TableView<RawStockUseItem> createSelectedUsageItemsTable() {
        TableView<RawStockUseItem> table = new TableView<>();
        
        TableColumn<RawStockUseItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(cellData -> {
            RawStockUseItem item = cellData.getValue();
            String itemName = item.getRawStockName();
            String unitName = item.getUnitName();
            String displayText = itemName + " - " + unitName;
            return new SimpleStringProperty(displayText);
        });
        nameCol.setPrefWidth(200); // Increased to accommodate "Name - Unit" format
        
        TableColumn<RawStockUseItem, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        brandCol.setPrefWidth(120); // Reduced from 150
        
        TableColumn<RawStockUseItem, Double> quantityCol = new TableColumn<>("Qty Used");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        quantityCol.setPrefWidth(80); // Reduced from 100
        
        TableColumn<RawStockUseItem, Double> costCol = new TableColumn<>("Unit Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        costCol.setPrefWidth(80); // Reduced from 100
        
        TableColumn<RawStockUseItem, Double> totalCol = new TableColumn<>("Total Cost");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        totalCol.setPrefWidth(80); // Reduced from 100
        
        // Format numeric columns
        quantityCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        costCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        totalCol.setCellFactory(col -> new TableCell<RawStockUseItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        table.getColumns().addAll(nameCol, brandCol, quantityCol, costCol, totalCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Load available raw stock items into the table
     */
    private static void loadAvailableRawStockItems(TableView<RawStockUseItem> table) {
        try {
            List<Object[]> rawStocks = database.getAllRawStocksWithUnitsForDropdown();
            ObservableList<RawStockUseItem> items = FXCollections.observableArrayList();
            
            for (Object[] row : rawStocks) {
                int rawStockId = (Integer) row[0];
                String rawStockName = (String) row[1];
                String categoryName = (String) row[2];
                String brandName = (String) row[3];
                String unitName = (String) row[4];
                double availableQuantity = (Double) row[5];
                double unitCost = (Double) row[6];
                
                RawStockUseItem item = new RawStockUseItem(
                    rawStockId, rawStockName, categoryName, brandName, 
                    unitName, 0.0, unitCost, availableQuantity
                );
                items.add(item);
            }
            
            table.setItems(items);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load raw stock items: " + e.getMessage());
        }
    }
    
    /**
     * Show dialog to enter usage quantity and confirm item addition
     */
    private static void showUsageQuantityDialog(RawStockUseItem item, TableView<RawStockUseItem> selectedTable, Label totalLabel) {
        Dialog<RawStockUseItem> dialog = new Dialog<>();
        dialog.setTitle("Enter Usage Quantity");
        dialog.setHeaderText("Add item to usage invoice");
        
        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label itemLabel = new Label("Item: " + item.getRawStockName() + " (" + item.getBrandName() + ")");
        Label availableLabel = new Label("Available Quantity: " + item.getAvailableQuantity() + " " + item.getUnitName());
        Label costLabel = new Label("Unit Cost: " + String.format("%.2f", item.getUnitCost()));
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity to use");
        
        Label totalCostLabel = new Label("Total Cost: 0.00");
        totalCostLabel.setStyle("-fx-font-weight: bold;");
        
        // Update total cost as user types
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    double qty = Double.parseDouble(newVal);
                    double total = qty * item.getUnitCost();
                    totalCostLabel.setText("Total Cost: " + String.format("%.2f", total));
                } else {
                    totalCostLabel.setText("Total Cost: 0.00");
                }
            } catch (NumberFormatException e) {
                totalCostLabel.setText("Total Cost: Invalid");
            }
        });
        
        content.getChildren().addAll(itemLabel, availableLabel, costLabel, 
            new Label("Quantity to Use:"), quantityField, totalCostLabel);
        
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType addButtonType = new ButtonType("Add Item", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Enable/disable add button based on input
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.trim().isEmpty()) {
                    double qty = Double.parseDouble(newVal.trim());
                    addButton.setDisable(qty <= 0 || qty > item.getAvailableQuantity());
                } else {
                    addButton.setDisable(true);
                }
            } catch (NumberFormatException e) {
                addButton.setDisable(true);
            }
        });
        
        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double quantity = Double.parseDouble(quantityField.getText().trim());
                    if (quantity > 0 && quantity <= item.getAvailableQuantity()) {
                        return item.createUseItem(quantity);
                    }
                } catch (NumberFormatException e) {
                    // Invalid input
                }
            }
            return null;
        });
        
        Optional<RawStockUseItem> result = dialog.showAndWait();
        result.ifPresent(useItem -> {
            // Check if item already exists in selected table
            boolean found = false;
            for (RawStockUseItem existingItem : selectedTable.getItems()) {
                if (existingItem.getRawStockId() == useItem.getRawStockId()) {
                    // Update quantity instead of adding duplicate
                    double newQuantity = existingItem.getQuantityUsed() + useItem.getQuantityUsed();
                    if (newQuantity <= item.getAvailableQuantity()) {
                        existingItem.setQuantityUsed(newQuantity);
                        existingItem.recalculateTotalCost(); // Ensure total cost is updated
                        found = true;
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Quantity Exceeded", 
                            "Total quantity would exceed available stock.");
                        return;
                    }
                    break;
                }
            }
            
            if (!found) {
                useItem.recalculateTotalCost(); // Ensure total cost is calculated
                selectedTable.getItems().add(useItem);
            }
            
            selectedTable.refresh();
            updateTotalUsageAmount(selectedTable, totalLabel);
        });
    }
    
    /**
     * Update the total usage amount label
     */
    private static void updateTotalUsageAmount(TableView<RawStockUseItem> selectedTable, Label totalLabel) {
        double total = selectedTable.getItems().stream()
                .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                .sum();
        totalLabel.setText("Total Usage Amount: " + String.format("%.2f", total));
    }
    
    /**
     * Handle the submission of raw stock use invoice
     */
    private static void handleRawStockUseInvoiceSubmit(TextField invoiceNumberField, DatePicker usageDatePicker,
                                                      TextField referencePurposeField, TableView<RawStockUseItem> selectedItemsTable,
                                                      Label totalLabel) {
        // Validate inputs
        if (invoiceNumberField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invoice number is required.");
            return;
        }
        
        if (usageDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Usage date is required.");
            return;
        }
        
        if (selectedItemsTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "At least one item must be selected.");
            return;
        }
        
        try {
            // Generate a fresh invoice number to avoid UNIQUE constraint violations
            String invoiceNumber = database.generateUseInvoiceNumber();
            invoiceNumberField.setText(invoiceNumber); // Update the field to show the new number
            
            String usageDate = usageDatePicker.getValue().format(DATE_FORMATTER);
            String referencePurpose = referencePurposeField.getText().trim();
            
            // Calculate total amount
            double totalAmount = selectedItemsTable.getItems().stream()
                    .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                    .sum();
            
            // Insert invoice and get ID
            int invoiceId = database.insertRawStockUseInvoiceAndGetId(invoiceNumber, usageDate, totalAmount, referencePurpose);
            
            if (invoiceId > 0) {
                // Insert invoice items
                List<RawStockUseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                
                // Ensure all items have their total cost properly calculated and validate
                for (RawStockUseItem item : items) {
                    item.recalculateTotalCost();
                    
                    // Validate that all required fields are properly set
                    if (item.getQuantityUsed() <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", 
                            "Invalid quantity for item: " + item.getRawStockName());
                        return;
                    }
                    if (item.getUnitCost() < 0) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", 
                            "Invalid unit cost for item: " + item.getRawStockName());
                        return;
                    }
                    if (Double.isNaN(item.getTotalCost()) || Double.isInfinite(item.getTotalCost())) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", 
                            "Invalid total cost calculated for item: " + item.getRawStockName());
                        return;
                    }
                }
                
                boolean itemsInserted = database.insertRawStockUseInvoiceItems(invoiceId, items);
                
                if (itemsInserted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Raw Stock Use Invoice created successfully!\nInvoice Number: " + invoiceNumber);
                    
                    // Clear the form
                    clearUseInvoiceForm(invoiceNumberField, usageDatePicker, referencePurposeField, 
                                      selectedItemsTable, totalLabel);
                    
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save invoice items.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create invoice.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                // If there's a unique constraint error, try once more with a new invoice number
                try {
                    String retryInvoiceNumber = database.generateUseInvoiceNumber();
                    invoiceNumberField.setText(retryInvoiceNumber);
                    
                    String usageDate = usageDatePicker.getValue().format(DATE_FORMATTER);
                    String referencePurpose = referencePurposeField.getText().trim();
                    
                    double totalAmount = selectedItemsTable.getItems().stream()
                            .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                            .sum();
                    
                    int invoiceId = database.insertRawStockUseInvoiceAndGetId(retryInvoiceNumber, usageDate, totalAmount, referencePurpose);
                    
                    if (invoiceId > 0) {
                        List<RawStockUseItem> items = new ArrayList<>(selectedItemsTable.getItems());
                        
                        // Ensure all items have their total cost properly calculated and validate
                        for (RawStockUseItem item : items) {
                            item.recalculateTotalCost();
                            
                            // Validate that all required fields are properly set
                            if (item.getQuantityUsed() <= 0 || item.getUnitCost() < 0 || 
                                Double.isNaN(item.getTotalCost()) || Double.isInfinite(item.getTotalCost())) {
                                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                                    "Invalid item data detected during retry for: " + item.getRawStockName());
                                return;
                            }
                        }
                        
                        boolean itemsInserted = database.insertRawStockUseInvoiceItems(invoiceId, items);
                        
                        if (itemsInserted) {
                            // Prepare items for printing
                            List<Item> printItems = new ArrayList<>();
                            for (RawStockUseItem item : items) {
                                printItems.add(new Item(
                                    item.getRawStockName() + " - " + item.getUnitName(),
                                    (int)Math.floor(item.getQuantityUsed()),
                                    item.getUnitCost(),
                                    0.0  // No discount for use items
                                ));
                            }

                            // Create invoice data for printing
                            InvoiceData invoiceData = new InvoiceData(
                                retryInvoiceNumber,
                                usageDate,
                                "Raw Stock Usage",
                                "Reference/Purpose: " + referencePurpose,
                                0.0,
                                printItems
                            );

                            // Try to open invoice for print preview first
                            boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Stock Usage");
                            
                            if (previewSuccess) {
                                showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    String.format("Raw Stock Use Invoice %s created successfully!\nTotal Amount: %.2f\n\nThe invoice has been opened for preview.", 
                                    retryInvoiceNumber, totalAmount));
                            } else {
                                // If preview fails, try direct printing
                                boolean printSuccess = PrintManager.printInvoice(invoiceData, "Stock Usage", true);
                                if (printSuccess) {
                                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                                        String.format("Raw Stock Use Invoice %s created and printed successfully!\nTotal Amount: %.2f", 
                                        retryInvoiceNumber, totalAmount));
                                } else {
                                    showAlert(Alert.AlertType.WARNING, "Partial Success", 
                                        String.format("Raw Stock Use Invoice %s created but printing failed.\nTotal Amount: %.2f\nYou can print it later from the records.", 
                                        retryInvoiceNumber, totalAmount));
                                }
                            }

                            // Clear form after successful creation and print attempt
                            clearUseInvoiceForm(invoiceNumberField, usageDatePicker, referencePurposeField, 
                                              selectedItemsTable, totalLabel);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save invoice items on retry.");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create invoice even after retry.");
                    }
                } catch (Exception retryException) {
                    retryException.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Invoice number conflict occurred. Please try again. Error: " + retryException.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Database error occurred: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clear the use invoice form after successful submission
     */
    private static void clearUseInvoiceForm(TextField invoiceNumberField, DatePicker usageDatePicker,
                                          TextField referencePurposeField, TableView<RawStockUseItem> selectedItemsTable,
                                          Label totalLabel) {
        // Generate new invoice number
        String newInvoiceNumber = database.generateUseInvoiceNumber();
        invoiceNumberField.setText(newInvoiceNumber);
        
        // Reset date
        usageDatePicker.setValue(LocalDate.now());
        
        // Clear reference
        referencePurposeField.clear();
        
        // Clear selected items
        selectedItemsTable.getItems().clear();
        
        // Reset total
        totalLabel.setText("Total Usage Amount: 0.00");
    }

    private static void handleUsageInvoiceSubmit(
        TextField stockField, DatePicker usageDatePicker, TextField quantityField,
        TextField referenceField, ListView<String> usageView
    ) {
        if (stockField.getText().trim().isEmpty() ||
            quantityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Stock and Quantity are required");
            return;
        }

        try {
            String usageEntry = String.format("%s | %s | Qty: %s | Ref: %s",
                stockField.getText().trim(),
                usageDatePicker.getValue().format(DATE_FORMATTER),
                quantityField.getText().trim(),
                referenceField.getText().trim());
            
            usageView.getItems().add(usageEntry);
            
            // Clear fields
            stockField.clear();
            quantityField.clear();
            referenceField.clear();
            usageDatePicker.setValue(LocalDate.now());

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please check your entries");
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Model class for Raw Stock records
    public static class RawStockRecord {
        private final Integer id;
        private final String name;
        private final String category;
        private final String manufacturer;
        private final String brand;
        private final String unit;
        private final Double quantity;
        private final Double unitPrice;
        private final Double totalCost;

        public RawStockRecord(Integer id, String name, String category, String manufacturer, String brand, String unit,
                             Double quantity, Double unitPrice, Double totalCost) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.manufacturer = manufacturer;
            this.brand = brand;
            this.unit = unit;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalCost = totalCost;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public String getUnit() { return unit; }
        public Double getQuantity() { return quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public Double getTotalCost() { return totalCost; }
        public String getCategory() { return category; }
        public String getManufacturer() { return manufacturer; }
    }
}


