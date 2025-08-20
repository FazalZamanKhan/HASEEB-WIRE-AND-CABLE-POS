package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;
import com.cablemanagement.model.ProductionStockRecord;
import com.cablemanagement.model.ProductionStockItem;
import com.cablemanagement.invoice.PrintManager;
import com.cablemanagement.invoice.InvoiceData;
import com.cablemanagement.invoice.Item;

public class ProductionStock {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final db database = new SQLiteDatabase();
    private static final SQLiteDatabase sqliteDatabase = new SQLiteDatabase();

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        formArea.getChildren().add(createProductionStockForm());

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
            "Register Production Stock",
            "Create Production Invoice",
            "Create Return Production Invoice",
            "Create Sales Invoice", 
            "Create Return Sales Invoice",
            "View Production Stock Usage Report"
        };

        Runnable[] actions = {
            () -> formArea.getChildren().setAll(createProductionStockForm()),
            () -> formArea.getChildren().setAll(createProductionInvoiceForm()),
            () -> formArea.getChildren().setAll(createReturnProductionInvoiceForm()),
            () -> formArea.getChildren().setAll(createSalesInvoiceForm()),
            () -> formArea.getChildren().setAll(createReturnSalesInvoiceForm()),
            () -> formArea.getChildren().setAll(createProductionStockUsageReportForm())
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

    private static VBox createProductionStockForm() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.getStyleClass().add("form-container");

        Label heading = createHeading("Register Production Stock");

        // === TWO-COLUMN LAYOUT CONTAINER ===
        HBox twoColumnLayout = new HBox(30);
        twoColumnLayout.setAlignment(Pos.TOP_LEFT);
        twoColumnLayout.setFillHeight(true);

        // === LEFT COLUMN - INPUT FORM ===
        VBox leftColumn = new VBox(20);
        leftColumn.setPrefWidth(400);
        leftColumn.setMinWidth(350);
        leftColumn.setMaxWidth(450);
        leftColumn.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20; -fx-background-color: #fafafa;");

        Label formTitle = createSubheading("Product Registration");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        // Input fields in a clean grid layout
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(15);
        inputGrid.setVgap(20);
        inputGrid.setAlignment(Pos.TOP_LEFT);

        // Product Name Field
        TextField nameField = createTextField("Enter Product Name");
        nameField.setPrefWidth(300);
        nameField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Category ComboBox
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("-- Select Category --");
        categoryCombo.setEditable(false);
        categoryCombo.setPrefWidth(300);
        categoryCombo.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        try {
            List<String> categories = database.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }

        // Manufacturer ComboBox
        ComboBox<String> manufacturerCombo = new ComboBox<>();
        manufacturerCombo.setPromptText("-- Select Manufacturer --");
        manufacturerCombo.setEditable(false);
        manufacturerCombo.setPrefWidth(300);
        manufacturerCombo.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        try {
            List<Manufacturer> manufacturers = database.getAllManufacturers();
            ObservableList<String> manufacturerNames = FXCollections.observableArrayList();
            for (Manufacturer manufacturer : manufacturers) {
                manufacturerNames.add(manufacturer.nameProperty().get());
            }
            manufacturerCombo.setItems(manufacturerNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load manufacturers: " + e.getMessage());
        }

        // Brand ComboBox 
        ComboBox<String> brandCombo = new ComboBox<>();
        brandCombo.setPromptText("-- Select Brand --");
        brandCombo.setEditable(false);
        brandCombo.setPrefWidth(300);
        brandCombo.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        try {
            List<Brand> brands = database.getAllBrands();
            ObservableList<String> brandNames = FXCollections.observableArrayList();
            for (Brand brand : brands) {
                brandNames.add(brand.nameProperty().get());
            }
            brandCombo.setItems(brandNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load brands: " + e.getMessage());
        }

        // Unit ComboBox
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.setPromptText("-- Select Unit --");
        unitCombo.setEditable(false);
        unitCombo.setPrefWidth(300);
        unitCombo.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
        
        try {
            List<String> units = database.getAllUnits();
            unitCombo.setItems(FXCollections.observableArrayList(units));
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load units: " + e.getMessage());
        }

        // Quantity Field
        TextField quantityField = createTextField("Enter Quantity");
        quantityField.setPrefWidth(300);
        quantityField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Unit Cost Field
        TextField unitCostField = createTextField("Enter Unit Cost");
        unitCostField.setPrefWidth(300);
        unitCostField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Sale Price Field (manual entry)
        TextField salePriceField = createTextField("Enter Sale Price");
        salePriceField.setPrefWidth(300);
        salePriceField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        // Add fields to grid with labels
        inputGrid.add(createFormRow("Product Name:", nameField), 0, 0);
        inputGrid.add(createFormRow("Category:", categoryCombo), 0, 1);
        inputGrid.add(createFormRow("Manufacturer:", manufacturerCombo), 0, 2);
        inputGrid.add(createFormRow("Brand:", brandCombo), 0, 3);
        inputGrid.add(createFormRow("Unit:", unitCombo), 0, 4);
        inputGrid.add(createFormRow("Quantity:", quantityField), 0, 5);
        inputGrid.add(createFormRow("Unit Cost:", unitCostField), 0, 6);
        inputGrid.add(createFormRow("Sale Price:", salePriceField), 0, 7);

        // Action buttons for the form
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button submitBtn = createSubmitButton("Register Product");
        submitBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20;");
        submitBtn.setPrefWidth(150);

        Button clearBtn = createActionButton("Clear Form");
        clearBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        clearBtn.setPrefWidth(120);

        buttonBox.getChildren().addAll(submitBtn, clearBtn);

        // Add components to left column
        leftColumn.getChildren().addAll(formTitle, inputGrid, buttonBox);

        // === RIGHT COLUMN - PRODUCTION STOCK TABLE ===
        VBox rightColumn = new VBox(15);
        // rightColumn.setMinWidth(600);
        rightColumn.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20; -fx-background-color: #ffffff;");

        Label tableTitle = createSubheading("Registered Production Stock");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        // Search and filter controls
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 15, 0));

        TextField searchField = createTextField("Search products...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 8; -fx-font-size: 14px;");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.setPromptText("Filter by Brand");
        filterCombo.setPrefWidth(150);
        filterCombo.getItems().add("All Brands");
        
        // Load brands for filter
        try {
            List<Brand> brands = database.getAllBrands();
            for (Brand brand : brands) {
                filterCombo.getItems().add(brand.nameProperty().get());
            }
            filterCombo.getSelectionModel().selectFirst(); // Select "All Brands"
        } catch (Exception e) {
            System.err.println("Failed to load brands for filter: " + e.getMessage());
        }

        Button refreshBtn = createActionButton("Refresh");
        refreshBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 8 15;");

        searchBox.getChildren().addAll(searchField, filterCombo, refreshBtn);

        // Production Stock Table with enhanced styling
        TableView<ProductionStockRecord> stockTable = createProductionStockTable();
        // stockTable.setPrefHeight(400);
        stockTable.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1;");

        // Stock summary labels
        HBox summaryBox = new HBox(30);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(15, 0, 0, 0));
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-radius: 5;");

        Label totalItemsLabel = new Label("Total Items: 0");
        totalItemsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        Label totalValueLabel = new Label("Total Value: 0.00");
        totalValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label lowStockLabel = new Label("Low Stock Items: 0");
        lowStockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");

        summaryBox.getChildren().addAll(totalItemsLabel, totalValueLabel, lowStockLabel);

        // Add components to right column
        rightColumn.getChildren().addAll(tableTitle, searchBox, stockTable, summaryBox);

        // Add columns to the two-column layout
        twoColumnLayout.getChildren().addAll(leftColumn, rightColumn);

        // === MAIN CONTAINER WITH SCROLLING ===
        ScrollPane mainScrollPane = new ScrollPane(twoColumnLayout);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // mainScrollPane.setPrefViewportHeight(600);
        mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Add heading and scrollable content to main container
        mainContainer.getChildren().addAll(heading, mainScrollPane);

        // === EVENT HANDLERS ===

        // Clear form button
        clearBtn.setOnAction(e -> {
            nameField.clear();
            categoryCombo.setValue(null);
            manufacturerCombo.setValue(null);
            brandCombo.setValue(null);
            unitCombo.setValue(null);
            quantityField.clear();
            unitCostField.clear();
            salePriceField.clear();
            nameField.requestFocus();
        });

        // Submit button
        submitBtn.setOnAction(e -> handleProductionStockSubmit(
            nameField, categoryCombo, manufacturerCombo, brandCombo, unitCombo, quantityField, unitCostField, salePriceField, stockTable,
            totalItemsLabel, totalValueLabel, lowStockLabel
        ));

        // Refresh button
        refreshBtn.setOnAction(e -> {
            refreshProductionStockTable(stockTable);
            updateStockSummary(stockTable, totalItemsLabel, totalValueLabel, lowStockLabel);
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterProductionStockTable(stockTable, newVal, filterCombo.getValue());
        });

        // Filter functionality
        filterCombo.setOnAction(e -> {
            filterProductionStockTable(stockTable, searchField.getText(), filterCombo.getValue());
        });

        // Load initial data
        refreshProductionStockTable(stockTable);
        updateStockSummary(stockTable, totalItemsLabel, totalValueLabel, lowStockLabel);

        return mainContainer;
    }

    private static VBox createProductionInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        // Header Section
        Label heading = createHeading("Create Production Invoice");
        
        // Production Info Section
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setAlignment(Pos.CENTER_LEFT);
        
        // Auto-generated invoice number
        TextField invoiceNumberField = createTextField("Production Invoice Number");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.getStyleClass().add("readonly-field");
        
        try {
            String autoGeneratedNumber = sqliteDatabase.generateProductionInvoiceNumber();
            invoiceNumberField.setText(autoGeneratedNumber);
        } catch (Exception e) {
            invoiceNumberField.setText("Error generating number");
            e.printStackTrace();
        }
        
        DatePicker productionDatePicker = new DatePicker();
        productionDatePicker.setValue(LocalDate.now());
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(400);
        
        infoGrid.add(createFormRow("Invoice Number:", invoiceNumberField), 0, 0);
        infoGrid.add(createFormRow("Production Date:", productionDatePicker), 0, 1);
        infoGrid.add(createFormRow("Notes:", notesArea), 0, 2);

        // Items and Materials Sections
        HBox itemsMaterialsSection = new HBox(20);
        itemsMaterialsSection.setAlignment(Pos.TOP_LEFT);

        // Production Items Section
        VBox itemsSection = new VBox(10);
        itemsSection.setMinWidth(400);
        
        // Load production stocks for dropdown
        ComboBox<String> productComboBox = createProductionStockComboBox();
        TextField quantityField = createTextField("Quantity");
        
        HBox itemButtonBox = new HBox(10);
        Button addItemBtn = createActionButton("Add Item");
        Button clearItemsBtn = createActionButton("Clear All");
        
        itemButtonBox.getChildren().addAll(addItemBtn, clearItemsBtn);
        
        ListView<String> itemsList = createEnhancedListView();
        
        itemsSection.getChildren().addAll(
            createSubheading("Production Items:"),
            createFormRow("Product:", productComboBox),
            createFormRow("Quantity:", quantityField),
            itemButtonBox,
            itemsList
        );

        // Raw Materials Section
        VBox materialsSection = new VBox(10);
        materialsSection.setMinWidth(400);
        
        // Load raw stocks for dropdown
        ComboBox<String> rawMaterialComboBox = createRawStockComboBox();
        TextField rawQuantityField = createTextField("Quantity Used");
        
        HBox materialButtonBox = new HBox(10);
        Button addMaterialBtn = createActionButton("Add Material");
        Button clearMaterialsBtn = createActionButton("Clear All");
        
        materialButtonBox.getChildren().addAll(addMaterialBtn, clearMaterialsBtn);
        
        ListView<String> materialsList = createEnhancedListView();
        
        materialsSection.getChildren().addAll(
            createSubheading("Raw Materials Used:"),
            createFormRow("Raw Material:", rawMaterialComboBox),
            createFormRow("Quantity Used:", rawQuantityField),
            materialButtonBox,
            materialsList
        );

        // Add sections to layout
        itemsMaterialsSection.getChildren().addAll(itemsSection, materialsSection);
        
        // Submit & Print Button
        Button submitBtn = createSubmitButton("Submit & Print Production Invoice");
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        // Add all components to main form
        form.getChildren().addAll(
            heading,
            infoGrid,
            itemsMaterialsSection,
            submitBtn
        );

        // Event Handlers
        addItemBtn.setOnAction(e -> handleAddProductionItem(productComboBox, quantityField, itemsList));
        quantityField.setOnAction(e -> handleAddProductionItem(productComboBox, quantityField, itemsList));
        
        clearItemsBtn.setOnAction(e -> {
            if (!itemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all production items?");
                alert.setContentText("This will remove all items from the list.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    itemsList.getItems().clear();
                }
            }
        });
        
        addMaterialBtn.setOnAction(e -> handleAddRawMaterial(rawMaterialComboBox, rawQuantityField, materialsList));
        rawQuantityField.setOnAction(e -> handleAddRawMaterial(rawMaterialComboBox, rawQuantityField, materialsList));
        
        clearMaterialsBtn.setOnAction(e -> {
            if (!materialsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all raw materials?");
                alert.setContentText("This will remove all materials from the list.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    materialsList.getItems().clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> handleSubmitProductionInvoice(
            invoiceNumberField,
            productionDatePicker, 
            notesArea, 
            itemsList, 
            materialsList
        ));

        return form;
    }

    private static VBox createReturnProductionInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Return Production Invoice");

        // Auto-generate return invoice number
        TextField returnInvoiceNumberField = createTextField("Return Invoice Number");
        returnInvoiceNumberField.setEditable(false);
        
        try {
            String autoGeneratedNumber = sqliteDatabase.generateProductionReturnInvoiceNumber();
            returnInvoiceNumberField.setText(autoGeneratedNumber);
        } catch (Exception e) {
            returnInvoiceNumberField.setText("Error generating number");
            e.printStackTrace();
        }

        // Return date
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());

        // Production invoice dropdown
        ComboBox<String> productionInvoiceCombo = new ComboBox<>();
        productionInvoiceCombo.setPromptText("Select Production Invoice");
        productionInvoiceCombo.setPrefWidth(400);
        
        // Load production invoices
        try {
            List<Object[]> productionInvoiceData = sqliteDatabase.getAllProductionInvoicesForDropdown();
            List<String> productionInvoices = new ArrayList<>();
            for (Object[] invoice : productionInvoiceData) {
                int invoiceId = (Integer) invoice[0];
                String date = (String) invoice[1];
                String notes = (String) invoice[2];
                String displayText = "Invoice #" + invoiceId + " - " + date + (notes != null && !notes.isEmpty() ? " (" + notes + ")" : "");
                productionInvoices.add(displayText);
            }
            productionInvoiceCombo.getItems().addAll(productionInvoices);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load production invoices: " + e.getMessage());
        }

        // Available items list (from selected production invoice)
        Label availableItemsLabel = createSubheading("Available Items from Selected Invoice:");
        ListView<String> availableItemsList = new ListView<>();
        availableItemsList.setPrefHeight(150);

        // Return items list with quantities
        Label returnItemsLabel = createSubheading("Items to Return:");
        ListView<String> returnItemsList = createEnhancedListView();
        
        // Add return item controls
        HBox addReturnItemBox = new HBox(10);
        ComboBox<String> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Select Item");
        itemCombo.setPrefWidth(200);
        TextField returnQuantityField = createTextField("Return Quantity");
        returnQuantityField.setPrefWidth(150);
        Button addReturnItemBtn = createActionButton("Add to Return");
        addReturnItemBox.getChildren().addAll(itemCombo, returnQuantityField, addReturnItemBtn);

        // Total return quantity
        TextField totalReturnQuantityField = createTextField("Total Return Quantity");
        totalReturnQuantityField.setEditable(false);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        Button submitBtn = createSubmitButton("Submit & Print Return Invoice");
        Button clearBtn = createActionButton("Clear All");
        
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        clearBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
        actionButtons.getChildren().addAll(submitBtn, clearBtn);
        
        form.getChildren().addAll(
            heading,
            createFormRow("Return Invoice Number:", returnInvoiceNumberField),
            createFormRow("Return Date:", returnDatePicker),
            createFormRow("Select Production Invoice:", productionInvoiceCombo),
            availableItemsLabel,
            availableItemsList,
            returnItemsLabel,
            addReturnItemBox,
            returnItemsList,
            createFormRow("Total Return Quantity:", totalReturnQuantityField),
            actionButtons
        );

        // Event handlers
        productionInvoiceCombo.setOnAction(e -> {
            String selectedInvoice = productionInvoiceCombo.getValue();
            if (selectedInvoice != null && !selectedInvoice.isEmpty()) {
                try {
                    // Extract production invoice ID from the dropdown text
                    String productionInvoiceId = selectedInvoice.split(" - ")[0].replace("Invoice #", "");
                    
                    // Load items from selected production invoice
                    List<Object[]> itemsData = sqliteDatabase.getProductionItemsByInvoiceId(Integer.parseInt(productionInvoiceId));
                    List<String> items = new ArrayList<>();
                    for (Object[] item : itemsData) {
                        int productionId = (Integer) item[0];
                        String productName = (String) item[1];
                        String brandName = (String) item[2];
                        double quantity = (Double) item[3];
                        String displayText = productName + " - " + brandName + " - Quantity: " + quantity;
                        items.add(displayText);
                    }
                    availableItemsList.getItems().clear();
                    availableItemsList.getItems().addAll(items);
                    
                    // Update item combo box
                    itemCombo.getItems().clear();
                    itemCombo.getItems().addAll(items);
                    
                    // Clear previous return items
                    returnItemsList.getItems().clear();
                    totalReturnQuantityField.setText("0");
                    
                } catch (Exception ex) {
                    showAlert("Database Error", "Failed to load items: " + ex.getMessage());
                }
            }
        });

        addReturnItemBtn.setOnAction(e -> {
            String selectedItem = itemCombo.getValue();
            String returnQuantityText = returnQuantityField.getText().trim();
            
            if (selectedItem == null || selectedItem.isEmpty()) {
                showAlert("Missing Information", "Please select an item");
                return;
            }
            
            if (returnQuantityText.isEmpty()) {
                showAlert("Missing Information", "Please enter return quantity");
                return;
            }
            
            try {
                int returnQuantity = Integer.parseInt(returnQuantityText);
                if (returnQuantity <= 0) {
                    showAlert("Invalid Quantity", "Return quantity must be greater than 0");
                    return;
                }
                
                // Add to return items list
                String returnItemText = selectedItem + " (Return Qty: " + returnQuantity + ")";
                returnItemsList.getItems().add(returnItemText);
                
                // Update total return quantity
                int currentTotal = Integer.parseInt(totalReturnQuantityField.getText().isEmpty() ? "0" : totalReturnQuantityField.getText());
                totalReturnQuantityField.setText(String.valueOf(currentTotal + returnQuantity));
                
                // Clear fields
                itemCombo.setValue(null);
                returnQuantityField.clear();
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Return quantity must be a valid number");
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!returnItemsList.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all return items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    returnItemsList.getItems().clear();
                    totalReturnQuantityField.setText("0");
                    itemCombo.setValue(null);
                    returnQuantityField.clear();
                }
            }
        });
        
        submitBtn.setOnAction(e -> {
            // Call submit handler
            boolean success = handleSubmitReturnProductionInvoice(
                returnInvoiceNumberField.getText(),
                returnDatePicker.getValue(),
                productionInvoiceCombo.getValue(),
                returnItemsList.getItems(),
                totalReturnQuantityField.getText(),
                returnInvoiceNumberField,
                returnDatePicker,
                productionInvoiceCombo,
                availableItemsList,
                returnItemsList,
                itemCombo,
                returnQuantityField,
                totalReturnQuantityField
            );
            
            // Print logic is now handled inside the method
        });

        return form;
    }

    private static VBox createSalesInvoiceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Sales Invoice");
        
        // Create a VBox for scrollable content
        VBox scrollableContent = new VBox(20);
        scrollableContent.setPadding(new Insets(0, 20, 20, 20));

        // === INVOICE HEADER SECTION ===
        VBox headerSection = new VBox(15);
        headerSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label headerTitle = createSubheading("Invoice Information");
        headerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Invoice header fields in a grid layout
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(20);
        headerGrid.setVgap(15);
        headerGrid.setAlignment(Pos.TOP_LEFT);
        
        TextField invoiceNumberField = createTextField("Auto-generated");
        invoiceNumberField.setEditable(false);
        invoiceNumberField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        // Auto-generate invoice number
        String autoInvoiceNumber = database.generateSalesInvoiceNumber();
        invoiceNumberField.setText(autoInvoiceNumber);
        
        DatePicker salesDatePicker = new DatePicker();
        salesDatePicker.setValue(LocalDate.now());
        salesDatePicker.setPrefWidth(200);
        
        // Customer dropdown with improved styling
        ComboBox<String> customerComboBox = new ComboBox<>();
        customerComboBox.setPromptText("-- Select Customer --");
        customerComboBox.setEditable(false);
        customerComboBox.setPrefWidth(250);
        
        // Load customers with error handling
        try {
            List<Object[]> customers = database.getAllCustomersForDropdown();
            ObservableList<String> customerNames = FXCollections.observableArrayList();
            for (Object[] customer : customers) {
                customerNames.add((String) customer[1]); // customer_name
            }
            customerComboBox.setItems(customerNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load customers: " + e.getMessage());
        }
        
        // Add fields to grid
        headerGrid.add(createFormRow("Invoice Number:", invoiceNumberField), 0, 0);
        headerGrid.add(createFormRow("Sales Date:", salesDatePicker), 1, 0);  
        headerGrid.add(createFormRow("Customer:", customerComboBox), 0, 1, 2, 1);
        
        headerSection.getChildren().addAll(headerTitle, headerGrid);

        // === PRODUCT SELECTION SECTION ===
        VBox productSection = new VBox(15);
        productSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label productTitle = createSubheading("Add Products to Invoice");
        productTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Product selection grid
        GridPane productGrid = new GridPane();
        productGrid.setHgap(15);
        productGrid.setVgap(15);
        productGrid.setAlignment(Pos.TOP_LEFT);
        
        // Product dropdown with better styling
        ComboBox<String> productComboBox = new ComboBox<>();
        productComboBox.setPromptText("-- Select Product --");
        productComboBox.setEditable(false);
        productComboBox.setPrefWidth(250);
        
        // Load production stock items with error handling
        final List<Object[]> products = new ArrayList<>();
        try {
            products.addAll(database.getAllProductionStocksWithPriceForDropdown());
            ObservableList<String> productNames = FXCollections.observableArrayList();
            for (Object[] product : products) {
                String displayName = String.format("%s (Stock: %d)", 
                    product[1], // product_name
                    ((Number) product[3]).intValue() // quantity available
                );
                productNames.add(displayName);
            }
            productComboBox.setItems(productNames);
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage());
        }
        
        TextField quantityField = createTextField("");
        quantityField.setPromptText("Enter Quantity");
        quantityField.setPrefWidth(120);
        
        TextField priceField = createTextField("0.00");
        priceField.setPromptText("Unit Price");
        priceField.setPrefWidth(120);
        priceField.setEditable(false);
        priceField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        TextField stockAvailableField = createTextField("0");
        stockAvailableField.setPromptText("Available Stock");
        stockAvailableField.setPrefWidth(120);
        stockAvailableField.setEditable(false);
        stockAvailableField.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #28a745;");
        
        // Discount fields
        TextField discountPercentageField = createTextField("0.0");
        discountPercentageField.setPromptText("Discount %");
        discountPercentageField.setPrefWidth(120);
        discountPercentageField.setEditable(true); // Make this editable for percentage input
        discountPercentageField.setStyle("-fx-border-color: #ddd;");
        
        TextField discountPerUnitField = createTextField("");
        discountPerUnitField.setPromptText("Discount Per Unit (Auto)");
        discountPerUnitField.setPrefWidth(120);
        discountPerUnitField.setEditable(false); // Make this read-only as it will be calculated
        discountPerUnitField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        TextField totalDiscountField = createTextField("");
        totalDiscountField.setPromptText("Total Discount (Auto)");
        totalDiscountField.setPrefWidth(120);
        totalDiscountField.setEditable(false); // Read-only, calculated automatically
        totalDiscountField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        
        // Helper method to update discount calculations
        Runnable updateDiscountCalculations = () -> {
            try {
                String discountPercentageText = discountPercentageField.getText().trim();
                String quantityText = quantityField.getText().trim();
                String priceText = priceField.getText().trim();
                
                if (!discountPercentageText.isEmpty() && !quantityText.isEmpty() && !priceText.isEmpty()) {
                    double discountPercentage = Double.parseDouble(discountPercentageText);
                    double quantity = Double.parseDouble(quantityText);
                    double unitPrice = Double.parseDouble(priceText);
                    
                    // Validate discount percentage is not greater than 100%
                    if (discountPercentage > 100.0) {
                        showAlert("Invalid Discount", "Discount percentage cannot exceed 100%");
                        discountPercentage = 100.0;
                        discountPercentageField.setText("100.0");
                    }
                    
                    if (discountPercentage < 0.0) {
                        showAlert("Invalid Discount", "Discount percentage cannot be negative");
                        discountPercentage = 0.0;
                        discountPercentageField.setText("0.0");
                    }
                    
                    // Calculate discount per unit from percentage
                    double discountPerUnit = (discountPercentage / 100.0) * unitPrice;
                    discountPerUnitField.setText(formatNumber(discountPerUnit));
                    
                    // Calculate total discount amount
                    double totalDiscount = discountPerUnit * quantity;
                    totalDiscountField.setText(formatNumber(totalDiscount));
                } else {
                    discountPerUnitField.setText("");
                    totalDiscountField.setText("");
                }
            } catch (NumberFormatException e) {
                discountPerUnitField.setText("");
                totalDiscountField.setText("");
            }
        };
        
        // Add listeners to recalculate when values change
        discountPercentageField.textProperty().addListener((obs, oldVal, newVal) -> updateDiscountCalculations.run());
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> updateDiscountCalculations.run());
        priceField.textProperty().addListener((obs, oldVal, newVal) -> updateDiscountCalculations.run());
        
        // Auto-fill price and stock when product is selected
        productComboBox.setOnAction(e -> {
            String selectedDisplay = productComboBox.getValue();
            if (selectedDisplay != null) {
                // Extract product name from display (remove stock info)
                String productName = selectedDisplay.split(" \\(Stock:")[0];
                
                for (Object[] product : products) {
                    if (productName.equals(product[1])) {
                        double salePrice = ((Number) product[2]).doubleValue();
                        int availableStock = ((Number) product[3]).intValue();
                        
                        priceField.setText(formatNumber(salePrice));
                        stockAvailableField.setText(String.valueOf(availableStock));
                        
                        // Update stock field color based on availability
                        if (availableStock > 10) {
                            stockAvailableField.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #28a745;");
                        } else if (availableStock > 0) {
                            stockAvailableField.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffc107;");
                        } else {
                            stockAvailableField.setStyle("-fx-background-color: #f8d7da; -fx-border-color: #dc3545;");
                        }
                        break;
                    }
                }
            } else {
                priceField.clear();
                stockAvailableField.clear();
                stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
            }
        });
        
        // Action buttons for adding items
        Button addItemBtn = createActionButton("Add to Invoice");
        addItemBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button clearSelectionBtn = createActionButton("Clear Selection");
        clearSelectionBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
        HBox itemActionButtons = new HBox(10);
        itemActionButtons.getChildren().addAll(addItemBtn, clearSelectionBtn);
        
        // Add to product grid
        productGrid.add(createFormRow("Product:", productComboBox), 0, 0);
        productGrid.add(createFormRow("Quantity:", quantityField), 1, 0);
        productGrid.add(createFormRow("Unit Price:", priceField), 0, 1);
        productGrid.add(createFormRow("Available Stock:", stockAvailableField), 1, 1);
        productGrid.add(createFormRow("Discount %:", discountPercentageField), 0, 2);
        productGrid.add(createFormRow("Discount Per Unit (Auto):", discountPerUnitField), 1, 2);
        productGrid.add(createFormRow("Total Discount (Auto):", totalDiscountField), 0, 3);
        productGrid.add(itemActionButtons, 0, 4, 2, 1);
        
        productSection.getChildren().addAll(productTitle, productGrid);

        // === INVOICE ITEMS TABLE SECTION ===
        VBox tableSection = new VBox(15);
        tableSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label tableTitle = createSubheading("Invoice Items");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Invoice items table with improved columns
        TableView<SalesInvoiceItemUI> itemsTable = new TableView<>();
        itemsTable.setPrefHeight(250);
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<SalesInvoiceItemUI, String> productCol = new TableColumn<>("Product Name");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.setPrefWidth(250);
        
        TableColumn<SalesInvoiceItemUI, Double> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        quantityCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatNumber(item));
                }
            }
        });
        
        TableColumn<SalesInvoiceItemUI, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatNumber(item));
                }
            }
        });
        
        TableColumn<SalesInvoiceItemUI, Double> discountPercentageCol = new TableColumn<>("Discount % (Auto)");
        discountPercentageCol.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        discountPercentageCol.setPrefWidth(90);
        discountPercentageCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatNumber(item) + "%");
                    if (item > 0) {
                        setStyle("-fx-text-fill: #17a2b8; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<SalesInvoiceItemUI, Double> discountAmountCol = new TableColumn<>("Total Discount");
        discountAmountCol.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        discountAmountCol.setPrefWidth(100);
        discountAmountCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatNumber(item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<SalesInvoiceItemUI, Double> totalCol = new TableColumn<>("Line Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setPrefWidth(120);
        totalCol.setCellFactory(col -> new TableCell<SalesInvoiceItemUI, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatNumber(item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
                }
            }
        });
        
        itemsTable.getColumns().addAll(productCol, quantityCol, priceCol, discountPercentageCol, discountAmountCol, totalCol);
        
        ObservableList<SalesInvoiceItemUI> invoiceItems = FXCollections.observableArrayList();
        itemsTable.setItems(invoiceItems);
        
        // Table action buttons
        Button removeItemBtn = createActionButton("Remove Selected Item");
        removeItemBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        
        Button clearAllItemsBtn = createActionButton("Clear All Items");
        clearAllItemsBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
        HBox tableActionButtons = new HBox(10);
        tableActionButtons.getChildren().addAll(removeItemBtn, clearAllItemsBtn);
        
        tableSection.getChildren().addAll(tableTitle, itemsTable, tableActionButtons);

        // === PAYMENT SECTION ===
        VBox paymentSection = new VBox(15);
        paymentSection.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label paymentTitle = createSubheading("Payment Information");
        paymentTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        GridPane paymentGrid = new GridPane();
        paymentGrid.setHgap(20);
        paymentGrid.setVgap(15);
        paymentGrid.setAlignment(Pos.TOP_LEFT);
        
        // Discount percentage field
        TextField invoiceDiscountPercentageField = createTextField("0.0");
        invoiceDiscountPercentageField.setPromptText("Discount %");
        invoiceDiscountPercentageField.setPrefWidth(150);
        
        // Discount amount field
        TextField discountField = createTextField("0.00");
        discountField.setPromptText("Discount Amount");
        discountField.setPrefWidth(150);
        
        TextField paidAmountField = createTextField("0.00");
        paidAmountField.setPromptText("Amount Paid");
        paidAmountField.setPrefWidth(150);
        
        // Summary labels
        Label subtotalLabel = new Label("Subtotal: 0.00");
        subtotalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label discountLabel = new Label("Discount: 0.00");
        discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545;");
        
        Label totalAmountLabel = new Label("Total Amount: 0.00");
        totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        Label balanceLabel = new Label("Balance Due: 0.00");
        balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        
        // Synchronization logic for discount fields
        final boolean[] isUpdatingDiscount = {false}; // Flag to prevent infinite loops
        final boolean[] isPercentageUpdate = {false}; // Track which field initiated the update
        final boolean[] isAmountUpdate = {false};
        
        // Helper method to update discount calculations
        Runnable updateDiscountSync = () -> {
            if (isUpdatingDiscount[0]) return; // Prevent infinite loops
            
            try {
                // Calculate subtotal from invoice items
                double subtotal = invoiceItems.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
                
                if (subtotal <= 0) {
                    return; // No calculation needed if subtotal is 0
                }
                
                isUpdatingDiscount[0] = true;
                
                String discountPercentageText = invoiceDiscountPercentageField.getText().trim();
                String discountAmountText = discountField.getText().trim();
                
                if (isPercentageUpdate[0] && !discountPercentageText.isEmpty()) {
                    // Percentage field was modified - update amount field
                    double percentage = Double.parseDouble(discountPercentageText);
                    
                    // Validate percentage
                    if (percentage < 0) {
                        percentage = 0.0;
                        invoiceDiscountPercentageField.setText("0.0");
                    } else if (percentage > 100) {
                        percentage = 100.0;
                        invoiceDiscountPercentageField.setText("100.0");
                    }
                    
                    double discountAmount = (percentage / 100.0) * subtotal;
                    discountField.setText(formatNumber(discountAmount));
                    
                } else if (isAmountUpdate[0] && !discountAmountText.isEmpty()) {
                    // Amount field was modified - update percentage field
                    double discountAmount = Double.parseDouble(discountAmountText);
                    
                    // Validate amount
                    if (discountAmount < 0) {
                        discountAmount = 0.0;
                        discountField.setText("0.00");
                    } else if (discountAmount > subtotal) {
                        discountAmount = subtotal;
                        discountField.setText(formatNumber(subtotal));
                    }
                    
                    double percentage = subtotal > 0 ? (discountAmount / subtotal) * 100.0 : 0.0;
                    invoiceDiscountPercentageField.setText(formatNumber(percentage));
                }
                
                isUpdatingDiscount[0] = false;
                isPercentageUpdate[0] = false;
                isAmountUpdate[0] = false;
                
            } catch (NumberFormatException e) {
                isUpdatingDiscount[0] = false;
                isPercentageUpdate[0] = false;
                isAmountUpdate[0] = false;
            }
        };
        
        // Add listeners for discount field synchronization
        invoiceDiscountPercentageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingDiscount[0]) {
                isPercentageUpdate[0] = true;
                updateDiscountSync.run();
            }
            updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
        });
        
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingDiscount[0]) {
                isAmountUpdate[0] = true;
                updateDiscountSync.run();
            }
            updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
        });
        
        // Update balance when discount or paid amount changes
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
        });
        
        paidAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
        });
        
        paymentGrid.add(createFormRow("Discount %:", invoiceDiscountPercentageField), 0, 0);
        paymentGrid.add(createFormRow("Discount Amount:", discountField), 1, 0);
        paymentGrid.add(createFormRow("Paid Amount:", paidAmountField), 0, 1);
        paymentGrid.add(subtotalLabel, 0, 2);
        paymentGrid.add(discountLabel, 1, 2);
        paymentGrid.add(totalAmountLabel, 0, 3, 2, 1);
        paymentGrid.add(balanceLabel, 0, 4, 2, 1);
        
        paymentSection.getChildren().addAll(paymentTitle, paymentGrid);

        // === ACTION BUTTONS ===
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));
        
        Button submitBtn = createSubmitButton("Submit & Print Invoice");
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 24;");
        submitBtn.setPrefWidth(200);
        
        Button resetFormBtn = createActionButton("Reset Form");
        resetFormBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        resetFormBtn.setPrefWidth(150);
        
        actionButtons.getChildren().addAll(submitBtn, resetFormBtn);

        // Add all sections to scrollable content
        scrollableContent.getChildren().addAll(
            headerSection,
            productSection,  
            tableSection,
            paymentSection,
            actionButtons
        );
        
        // Create ScrollPane for the form content
        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setMaxHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Add heading and scrollable content to main form
        form.getChildren().addAll(heading, scrollPane);

        // === EVENT HANDLERS ===
        
        // Clear selection button
        clearSelectionBtn.setOnAction(e -> {
            productComboBox.setValue(null);
            quantityField.clear();
            priceField.clear();
            stockAvailableField.clear();
            discountPercentageField.setText("0.0");
            discountPerUnitField.clear();
            totalDiscountField.clear();
            stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
        });
        
        // Add item to invoice button
        addItemBtn.setOnAction(e -> {
            String selectedDisplay = productComboBox.getValue();
            String quantityText = quantityField.getText().trim();
            String priceText = priceField.getText().trim();
            String stockText = stockAvailableField.getText().trim();
            String discountPercentageText = discountPercentageField.getText().trim();
            
            // Validation
            if (selectedDisplay == null) {
                showAlert("Missing Information", "Please select a product");
                return;
            }
            
            if (quantityText.isEmpty()) {
                showAlert("Missing Information", "Please enter quantity");
                return;
            }
            
            if (priceText.isEmpty()) {
                showAlert("Missing Information", "Price not loaded. Please select product again");
                return;
            }
            
            try {
                double qty = Double.parseDouble(quantityText);
                double price = Double.parseDouble(priceText);
                int availableStock = stockText.isEmpty() ? 0 : Integer.parseInt(stockText);
                double discountPercentage = discountPercentageText.isEmpty() ? 0.0 : Double.parseDouble(discountPercentageText);
                
                if (qty <= 0) {
                    showAlert("Invalid Input", "Quantity must be greater than 0");
                    return;
                }
                
                if (price <= 0) {
                    showAlert("Invalid Input", "Price must be greater than 0");
                    return;
                }
                
                if (discountPercentage < 0) {
                    showAlert("Invalid Input", "Discount percentage cannot be negative");
                    return;
                }
                
                if (discountPercentage > 100) {
                    showAlert("Invalid Input", "Discount percentage cannot exceed 100%");
                    return;
                }
                
                if (qty > availableStock) {
                    showAlert("Insufficient Stock", 
                        String.format("Requested quantity (%.1f) exceeds available stock (%d)", qty, availableStock));
                    return;
                }
                
                // Extract product name from display
                String productName = selectedDisplay.split(" \\(Stock:")[0];
                
                // Check if product already exists in table
                boolean productExists = false;
                for (SalesInvoiceItemUI item : invoiceItems) {
                    if (item.getProductName().equals(productName)) {
                        double newQty = item.getQuantity() + qty;
                        if (newQty > availableStock) {
                            showAlert("Insufficient Stock", 
                                String.format("Total quantity (%.1f) would exceed available stock (%d)", newQty, availableStock));
                            return;
                        }
                        
                        // Update quantity and keep the same discount percentage
                        item.setQuantity(newQty);
                        
                        productExists = true;
                        break;
                    }
                }
                
                if (!productExists) {
                    // Create new item with the discount percentage directly
                    SalesInvoiceItemUI newItem = new SalesInvoiceItemUI(productName, qty, price, discountPercentage, 0.0);
                    invoiceItems.add(newItem);
                }
                
                itemsTable.refresh();
                updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                    subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
                
                // Clear selection after adding
                productComboBox.setValue(null);
                quantityField.clear();
                priceField.clear();
                stockAvailableField.clear();
                discountPercentageField.setText("0.0");
                discountPerUnitField.clear();
                totalDiscountField.clear();
                stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for quantity, price, and discount percentage");
            }
        });
        
        // Remove selected item button
        removeItemBtn.setOnAction(e -> {
            SalesInvoiceItemUI selectedItem = itemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                invoiceItems.remove(selectedItem);
                updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                    subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
            } else {
                showAlert("No Selection", "Please select an item to remove from the table");
            }
        });
        
        // Clear all items button
        clearAllItemsBtn.setOnAction(e -> {
            if (!invoiceItems.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all invoice items?");
                alert.setContentText("This will remove all products from the invoice.");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    invoiceItems.clear();
                    updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                        subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
                }
            }
        });
        
        // Reset form button
        resetFormBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Reset");
            alert.setHeaderText("Reset entire form?");
            alert.setContentText("This will clear all data and generate a new invoice number.");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                // Generate new invoice number
                String newInvoiceNumber = database.generateSalesInvoiceNumber();
                invoiceNumberField.setText(newInvoiceNumber);
                
                // Reset all fields
                salesDatePicker.setValue(LocalDate.now());
                customerComboBox.setValue(null);
                productComboBox.setValue(null);
                quantityField.setText("1");
                priceField.clear();
                stockAvailableField.clear();
                stockAvailableField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd;");
                discountPercentageField.setText("0.0");
                discountPerUnitField.clear();
                totalDiscountField.clear();
                invoiceDiscountPercentageField.setText("0.0"); // Reset invoice discount percentage
                discountField.setText("0.00");
                paidAmountField.setText("0.00");
                
                // Clear items
                invoiceItems.clear();
                updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                    subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
            }
        });
        
        // Submit invoice button
        submitBtn.setOnAction(e -> {
            String invoiceNumber = invoiceNumberField.getText().trim();
            String customer = customerComboBox.getValue();
            String date = salesDatePicker.getValue().format(DATE_FORMATTER);
            String discountText = discountField.getText().trim();
            String paidAmountText = paidAmountField.getText().trim();
            
            // Validation
            if (customer == null || customer.isEmpty()) {
                showAlert("Missing Information", "Please select a customer");
                return;
            }
            
            if (invoiceItems.isEmpty()) {
                showAlert("Missing Information", "Please add at least one item to the invoice");
                return;
            }
            
            try {
                double discount = discountText.isEmpty() ? 0.0 : Double.parseDouble(discountText);
                double paidAmount = paidAmountText.isEmpty() ? 0.0 : Double.parseDouble(paidAmountText);
                
                if (discount < 0 || paidAmount < 0) {
                    showAlert("Invalid Input", "Discount and paid amount cannot be negative");
                    return;
                }
                
                // Calculate total amount
                double subtotal = invoiceItems.stream()
                    .mapToDouble(SalesInvoiceItemUI::getTotalPrice)
                    .sum();
                double totalAmount = subtotal - discount;
                
                if (totalAmount < 0) {
                    showAlert("Invalid Input", "Discount cannot exceed subtotal");
                    return;
                }
                
                // Get customer ID
                int customerId = database.getCustomerIdByName(customer);
                if (customerId == -1) {
                    showAlert("Database Error", "Customer not found in database");
                    return;
                }
                
                // Prepare invoice items for database
                List<Object[]> items = new ArrayList<>();
                for (SalesInvoiceItemUI item : invoiceItems) {
                    int productId = database.getProductionStockIdByName(item.getProductName());
                    if (productId == -1) {
                        showAlert("Database Error", "Product '" + item.getProductName() + "' not found in database");
                        return;
                    }
                    items.add(new Object[]{productId, item.getQuantity(), item.getUnitPrice(), 
                                          item.getDiscountPercentage(), item.getDiscountAmount()});
                }
                
                // Show confirmation with invoice summary
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Invoice Creation");
                confirmAlert.setHeaderText("Create Sales Invoice?");
                confirmAlert.setContentText(String.format(
                    "Invoice: %s\nCustomer: %s\nSubtotal: %.2f\nDiscount: %.2f\nTotal: %.2f\nPaid: %.2f\nBalance: %.2f",
                    invoiceNumber, customer, subtotal, discount, totalAmount, paidAmount, totalAmount - paidAmount
                ));
                
                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    // Save to database
                    boolean success = database.insertSalesInvoice(invoiceNumber, customerId, date, 
                        totalAmount, discount, paidAmount, items);
                    
                    if (success) {
                        // Prepare invoice data for printing
                        List<Item> printItems = new ArrayList<>();
                        for (SalesInvoiceItemUI item : invoiceItems) {
                            // Get production stock ID to retrieve unit information using helper method
                            int productionStockId = getProductionStockIdByName(item.getProductName());
                            String unit = "N/A";
                            if (productionStockId != -1) {
                                unit = getProductionStockUnit(productionStockId);
                            }
                            
                            // Format the item name as "name - unit"
                            String itemNameWithUnit = item.getProductName() + " - " + unit;
                            
                            printItems.add(new Item(
                                itemNameWithUnit,
                                (int) item.getQuantity(),
                                item.getUnitPrice(),
                                item.getDiscountPercentage() // individual item discount percentage
                            ));
                        }
                        
                        // Get customer details from database
                        String contactNumber = "";
                        String tehsil = "";
                        
                        try {
                            // Get all customers and find the matching one to extract details
                            List<Customer> customers = sqliteDatabase.getAllCustomers();
                            for (Customer c : customers) {
                                if (c.nameProperty().get().equals(customer)) {
                                    contactNumber = c.contactProperty().get();
                                    tehsil = c.tehsilProperty().get();
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Could not retrieve customer details: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                        
                        // Get customer balance details for PDF
                        Object[] balanceDetails = database.getCustomerInvoiceBalanceDetails(
                            customer, invoiceNumber, totalAmount, paidAmount
                        );
                        double previousBalance = (Double) balanceDetails[0];
                        double totalBalance = (Double) balanceDetails[1];
                        double netBalance = (Double) balanceDetails[2];
                        
                        // Create invoice data with proper type and metadata
                        InvoiceData invoiceData = new InvoiceData(
                            InvoiceData.TYPE_SALE,
                            invoiceNumber,
                            date,
                            customer,
                            "", // Empty address field as requested
                            printItems,
                            previousBalance // Use calculated previous balance
                        );
                        
                        // Set all balance details
                        invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                        invoiceData.setPaidAmount(paidAmount);
                        invoiceData.setDiscountAmount(discount);
                        
                        // Add metadata for contact and tehsil
                        invoiceData.setMetadata("contact", contactNumber);
                        invoiceData.setMetadata("tehsil", tehsil);
                        
                        // Open invoice for print preview (like Ctrl+P behavior)
                        boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Sales");
                        
                        if (previewSuccess) {
                            showAlert("Success", "Sales invoice created successfully!\n\nInvoice Number: " + invoiceNumber + "\n\nThe invoice has been opened for preview and printing.");
                        } else {
                            // Fallback to printer selection if preview fails
                            boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Sales");
                            if (printSuccess) {
                                showAlert("Success", "Sales invoice created and printed successfully!\n\nInvoice Number: " + invoiceNumber);
                            } else {
                                showAlert("Partial Success", "Sales invoice created successfully but printing failed.\n\nInvoice Number: " + invoiceNumber);
                            }
                        }
                        
                        // Reset form for next invoice
                        String newInvoiceNumber = database.generateSalesInvoiceNumber();
                        invoiceNumberField.setText(newInvoiceNumber);
                        salesDatePicker.setValue(LocalDate.now());
                        customerComboBox.setValue(null);
                        invoiceDiscountPercentageField.setText("0.0");
                        discountField.setText("0.00");
                        paidAmountField.setText("0.00");
                        invoiceItems.clear();
                        updatePaymentSummary(invoiceItems, discountField, paidAmountField, 
                            subtotalLabel, discountLabel, totalAmountLabel, balanceLabel);
                    } else {
                        showAlert("Database Error", "Failed to create sales invoice. Please check the database connection and try again.");
                    }
                }
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for discount and paid amount");
            } catch (Exception ex) {
                showAlert("Unexpected Error", "An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Print Last Invoice button
        // Print Last Invoice button removed

        return form;
    }

    private static ScrollPane createReturnSalesInvoiceForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Create Return Sales Invoice");

        // Return invoice header fields
        TextField returnInvoiceNumberField = createTextField("Auto-generated");
        returnInvoiceNumberField.setEditable(false);
        returnInvoiceNumberField.setStyle("-fx-background-color: #f0f0f0;");
        
        // Auto-generate return invoice number
        String autoReturnInvoiceNumber = database.generateSalesReturnInvoiceNumber();
        returnInvoiceNumberField.setText(autoReturnInvoiceNumber);
        
        // Original invoice dropdown
        ComboBox<String> originalInvoiceComboBox = new ComboBox<>();
        originalInvoiceComboBox.setPromptText("Select Original Invoice");
        originalInvoiceComboBox.setEditable(false);
        originalInvoiceComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load sales invoices
        List<Object[]> salesInvoices = database.getAllSalesInvoicesForDropdown();
        ObservableList<String> invoiceNumbers = FXCollections.observableArrayList();
        for (Object[] invoice : salesInvoices) {
            String displayText = String.format("%s - %s (%s)", 
                invoice[1], // invoice_number
                invoice[2], // customer_name
                invoice[3]  // sales_date
            );
            invoiceNumbers.add(displayText);
        }
        originalInvoiceComboBox.setItems(invoiceNumbers);
        
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now());
        
        TextField customerField = createTextField("Customer");
        customerField.setEditable(false);
        customerField.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField returnAmountField = createTextField("Return Amount");
        returnAmountField.setEditable(false);
        returnAmountField.setStyle("-fx-background-color: #f0f0f0;");
        
        // Refund method ComboBox
        ComboBox<String> refundMethodComboBox = new ComboBox<>();
        refundMethodComboBox.getItems().addAll("Refund to Balance", "Cash Refund");
        refundMethodComboBox.setValue("Refund to Balance"); // Default selection
        refundMethodComboBox.setPrefWidth(300);
        refundMethodComboBox.getStyleClass().add("combo-box");
        
        // Return items table
        TableView<SalesInvoiceItemUI> returnItemsTable = new TableView<>();
        returnItemsTable.setPrefHeight(250);
        returnItemsTable.getStyleClass().add("table-view");
        returnItemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<SalesInvoiceItemUI, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> originalQtyCol = new TableColumn<>("Original Qty");
        originalQtyCol.setCellValueFactory(new PropertyValueFactory<>("originalQuantity"));
        originalQtyCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> returnQtyCol = new TableColumn<>("Return Qty");
        returnQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        returnQtyCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        TableColumn<SalesInvoiceItemUI, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.prefWidthProperty().bind(returnItemsTable.widthProperty().multiply(0.20));
        
        returnItemsTable.getColumns().addAll(productCol, originalQtyCol, returnQtyCol, priceCol, totalCol);
        
        ObservableList<SalesInvoiceItemUI> returnItems = FXCollections.observableArrayList();
        returnItemsTable.setItems(returnItems);
        
        // Wrap return items table in ScrollPane
        ScrollPane returnItemsScrollPane = new ScrollPane(returnItemsTable);
        returnItemsScrollPane.setFitToWidth(true);
        returnItemsScrollPane.setFitToHeight(true);
        returnItemsScrollPane.setPrefHeight(250);
        returnItemsScrollPane.setMaxHeight(250);
        returnItemsScrollPane.getStyleClass().addAll("scroll-pane", "custom-scroll");
        
        // Available items table (from original invoice)
        TableView<SalesInvoiceItemUI> availableItemsTable = new TableView<>();
        availableItemsTable.setPrefHeight(200);
        availableItemsTable.getStyleClass().add("table-view");
        
        TableColumn<SalesInvoiceItemUI, String> availableProductCol = new TableColumn<>("Product Name");
        availableProductCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        availableProductCol.setPrefWidth(250);
        
        TableColumn<SalesInvoiceItemUI, Double> availableQtyCol = new TableColumn<>("Available Quantity");
        availableQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        availableQtyCol.setPrefWidth(140);
        
        TableColumn<SalesInvoiceItemUI, Double> availablePriceCol = new TableColumn<>("Unit Price");
        availablePriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        availablePriceCol.setPrefWidth(120);
        
        availableItemsTable.getColumns().addAll(availableProductCol, availableQtyCol, availablePriceCol);
        
        ObservableList<SalesInvoiceItemUI> availableItems = FXCollections.observableArrayList();
        availableItemsTable.setItems(availableItems);
        
        // Wrap available items table in ScrollPane
        ScrollPane availableItemsScrollPane = new ScrollPane(availableItemsTable);
        availableItemsScrollPane.setFitToWidth(true);
        availableItemsScrollPane.setFitToHeight(true);
        availableItemsScrollPane.setPrefHeight(200);
        availableItemsScrollPane.setMaxHeight(200);
        availableItemsScrollPane.getStyleClass().addAll("scroll-pane", "custom-scroll");
        
        // Add return item controls
        VBox addReturnSection = new VBox(10);
        addReturnSection.setPadding(new Insets(15, 0, 15, 0));
        addReturnSection.getStyleClass().add("form-container");
        
        HBox addReturnItemBox = new HBox(15);
        addReturnItemBox.setAlignment(Pos.CENTER_LEFT);
        addReturnItemBox.setPadding(new Insets(10));
        addReturnItemBox.getStyleClass().add("form-row");
        
        Label returnQtyLabel = new Label("Return Quantity:");
        returnQtyLabel.getStyleClass().add("form-label");
        
        TextField returnQuantityField = createTextField("Return Quantity");
        returnQuantityField.setPrefWidth(150);
        
        Button addReturnItemBtn = createActionButton("Add to Return");
        addReturnItemBtn.setPrefWidth(120);
        
        Button removeReturnItemBtn = createActionButton("Remove Item");
        removeReturnItemBtn.setPrefWidth(120);
        
        addReturnItemBox.getChildren().addAll(
            returnQtyLabel, returnQuantityField,
            addReturnItemBtn, removeReturnItemBtn
        );
        
        addReturnSection.getChildren().add(addReturnItemBox);
        
        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 10, 0));
        actionButtons.getStyleClass().add("form-row");
        
        Button submitBtn = createSubmitButton("Submit & Print Return");
        submitBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        
        Button clearBtn = createActionButton("Clear All");
        clearBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        
        actionButtons.getChildren().addAll(submitBtn, clearBtn);

        // Form layout with proper spacing
        VBox formContent = new VBox(20);
        formContent.getChildren().addAll(
            heading,
            new VBox(10, 
                createFormRow("Return Invoice Number:", returnInvoiceNumberField),
                createFormRow("Original Invoice:", originalInvoiceComboBox),
                createFormRow("Return Date:", returnDatePicker),
                createFormRow("Customer:", customerField),
                createFormRow("Return Amount:", returnAmountField),
                createFormRow("Refund Method:", refundMethodComboBox)
            ),
            new VBox(15,
                createSubheading("Available Items from Original Invoice:"),
                availableItemsScrollPane,
                addReturnSection,
                createSubheading("Return Items:"),
                returnItemsScrollPane
            ),
            actionButtons
        );
        
        form.getChildren().add(formContent);
        
        // Create main ScrollPane for the entire form
        ScrollPane mainScrollPane = new ScrollPane(form);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(false);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.getStyleClass().addAll("scroll-pane", "custom-scroll");

        // Event handlers
        originalInvoiceComboBox.setOnAction(e -> {
            String selectedDisplay = originalInvoiceComboBox.getValue();
            if (selectedDisplay != null) {
                // Extract invoice number from display text
                String invoiceNumber = selectedDisplay.split(" - ")[0];
                
                // Find the selected invoice data
                for (Object[] invoice : salesInvoices) {
                    if (invoiceNumber.equals(invoice[1])) {
                        int salesInvoiceId = (Integer) invoice[0];
                        
                        // Get invoice details
                        Object[] invoiceData = database.getSalesInvoiceById(salesInvoiceId);
                        if (invoiceData != null) {
                            customerField.setText((String) invoiceData[3]); // customer_name
                        }
                        
                        // Load invoice items
                        List<Object[]> originalItems = database.getSalesInvoiceItemsByInvoiceId(salesInvoiceId);
                        availableItems.clear();
                        
                        for (Object[] item : originalItems) {
                            // Calculate net unit price (after discount)
                            double originalUnitPrice = (Double) item[3];
                            double discountAmount = (Double) item[5];
                            double quantity = (Double) item[2];
                            
                            // Net unit price = (Original amount - total discount) / quantity
                            double originalAmount = originalUnitPrice * quantity;
                            double netAmount = originalAmount - discountAmount;
                            double netUnitPrice = netAmount / quantity;
                            
                            SalesInvoiceItemUI itemUI = new SalesInvoiceItemUI(
                                (String) item[1], // product_name
                                quantity,         // quantity
                                netUnitPrice      // net unit price after discount
                            );
                            itemUI.setProductionStockId((Integer) item[0]);
                            availableItems.add(itemUI);
                        }
                        break;
                    }
                }
                
                // Clear return items when original invoice changes
                returnItems.clear();
                updateReturnAmount(returnItems, returnAmountField);
            }
        });
        
        addReturnItemBtn.setOnAction(e -> {
            SalesInvoiceItemUI selectedItem = availableItemsTable.getSelectionModel().getSelectedItem();
            String returnQtyText = returnQuantityField.getText().trim();
            
            if (selectedItem == null) {
                showAlert("No Selection", "Please select an item from the available items table");
                return;
            }
            
            if (returnQtyText.isEmpty()) {
                showAlert("Missing Information", "Please enter return quantity");
                return;
            }
            
            try {
                double returnQty = Double.parseDouble(returnQtyText);
                
                if (returnQty <= 0) {
                    showAlert("Invalid Input", "Return quantity must be positive");
                    return;
                }
                
                if (returnQty > selectedItem.getQuantity()) {
                    showAlert("Invalid Input", "Return quantity cannot exceed original quantity");
                    return;
                }
                
                // Check if item already exists in return items
                boolean itemExists = false;
                for (SalesInvoiceItemUI returnItem : returnItems) {
                    if (returnItem.getProductName().equals(selectedItem.getProductName())) {
                        double newReturnQty = returnItem.getQuantity() + returnQty;
                        if (newReturnQty > selectedItem.getQuantity()) {
                            showAlert("Invalid Input", 
                                String.format("Total return quantity (%.2f) cannot exceed original quantity (%.2f)", 
                                    newReturnQty, selectedItem.getQuantity()));
                            return;
                        }
                        returnItem.setQuantity(newReturnQty);
                        itemExists = true;
                        break;
                    }
                }
                
                if (!itemExists) {
                    SalesInvoiceItemUI returnItem = new SalesInvoiceItemUI(
                        selectedItem.getProductName(), returnQty, selectedItem.getUnitPrice());
                    returnItem.setProductionStockId(selectedItem.getProductionStockId());
                    returnItem.setOriginalQuantity(selectedItem.getQuantity());
                    returnItems.add(returnItem);
                }
                
                returnItemsTable.refresh();
                updateReturnAmount(returnItems, returnAmountField);
                returnQuantityField.clear();
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number for return quantity");
            }
        });
        
        removeReturnItemBtn.setOnAction(e -> {
            SalesInvoiceItemUI selectedItem = returnItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                returnItems.remove(selectedItem);
                updateReturnAmount(returnItems, returnAmountField);
            } else {
                showAlert("No Selection", "Please select an item to remove from return items");
            }
        });
        
        clearBtn.setOnAction(e -> {
            if (!returnItems.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Clear");
                alert.setHeaderText("Clear all return items?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    returnItems.clear();
                    updateReturnAmount(returnItems, returnAmountField);
                }
            }
        });
        
        // Print Return button removed
        
        submitBtn.setOnAction(e -> {
            String returnInvoiceNumber = returnInvoiceNumberField.getText().trim();
            String selectedDisplay = originalInvoiceComboBox.getValue();
            String customer = customerField.getText().trim();
            String date = returnDatePicker.getValue().format(DATE_FORMATTER);
            String returnAmountText = returnAmountField.getText().trim();
            String refundMethod = refundMethodComboBox.getValue();
            
            if (selectedDisplay == null || customer.isEmpty() || returnItems.isEmpty() || refundMethod == null) {
                showAlert("Missing Information", "Please select original invoice, refund method and add at least one return item");
                return;
            }
            
            try {
                double totalReturnAmount = Double.parseDouble(returnAmountText);
                
                // Get original invoice data
                String originalInvoiceNumber = selectedDisplay.split(" - ")[0];
                int originalSalesInvoiceId = -1;
                int customerId = -1;
                
                for (Object[] invoice : salesInvoices) {
                    if (originalInvoiceNumber.equals(invoice[1])) {
                        originalSalesInvoiceId = (Integer) invoice[0];
                        break;
                    }
                }
                
                customerId = database.getCustomerIdByName(customer);
                
                if (originalSalesInvoiceId == -1 || customerId == -1) {
                    showAlert("Error", "Original invoice or customer not found");
                    return;
                }
                
                // Prepare return items for database
                List<Object[]> items = new ArrayList<>();
                for (SalesInvoiceItemUI item : returnItems) {
                    items.add(new Object[]{item.getProductionStockId(), item.getQuantity(), item.getUnitPrice()});
                }
                
                // Save to database
                boolean updateBalance = "Refund to Balance".equals(refundMethod);
                boolean success = database.insertSalesReturnInvoice(returnInvoiceNumber, originalSalesInvoiceId, 
                    customerId, date, totalReturnAmount, items, updateBalance);
                
                if (success) {
                    // Prepare invoice data for printing
                    List<Item> printItems = new ArrayList<>();
                    for (SalesInvoiceItemUI item : returnItems) {
                        // Calculate the net unit price (what was actually paid after discount)
                        double originalAmount = item.getUnitPrice() * item.getQuantity();
                        double netAmount = originalAmount - item.getDiscountAmount();
                        double netUnitPrice = netAmount / item.getQuantity();
                        
                        // Get unit information using production stock ID
                        String unit = "N/A";
                        if (item.getProductionStockId() != 0) {
                            unit = getProductionStockUnit(item.getProductionStockId());
                        }
                        
                        // Format the item name as "name - unit"
                        String itemNameWithUnit = item.getProductName() + " - " + unit;
                        
                        printItems.add(new Item(
                            itemNameWithUnit,
                            (int) item.getQuantity(),
                            netUnitPrice, // Use net unit price instead of original price
                            0.0 // No discount percentage for return invoice display
                        ));
                    }
                    
                    // Get customer details from database
                    String contactNumber = "";
                    String tehsil = "";
                    
                    try {
                        // Get all customers and find the matching one to extract details
                        List<Customer> customers = sqliteDatabase.getAllCustomers();
                        for (Customer c : customers) {
                            if (c.nameProperty().get().equals(customer)) {
                                contactNumber = c.contactProperty().get();
                                tehsil = c.tehsilProperty().get();
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Could not retrieve customer details: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    
                    // Get customer balance details for PDF (for return, we need to calculate return impact)
                    Object[] balanceDetails = database.getCustomerInvoiceBalanceDetails(
                        customer, returnInvoiceNumber, 0.0, 0.0  // Return doesn't add to balance, so amounts are 0
                    );
                    double previousBalance = (Double) balanceDetails[0];
                    
                    // Calculate return impact on balance from print items using net prices
                    double returnImpactAmount = 0.0;
                    for (Item item : printItems) {
                        // Since printItems now contain net unit prices, just multiply by quantity
                        returnImpactAmount += item.getUnitPrice() * item.getQuantity();
                    }
                    
                    // For return invoices: Total Balance = Previous Balance - Return Amount
                    double totalBalance = previousBalance - returnImpactAmount;
                    double netBalance = totalBalance; // No payment involved in returns, net balance equals total balance
                    
                    // Create invoice data for printing with proper type and metadata
                    InvoiceData invoiceData = new InvoiceData(
                        InvoiceData.TYPE_SALE_RETURN,
                        returnInvoiceNumber,
                        date,
                        customer,
                        "", // Empty address field as requested
                        printItems,
                        previousBalance // Use calculated previous balance
                    );
                    
                    // Set all balance details for return
                    invoiceData.setBalanceDetails(previousBalance, totalBalance, netBalance);
                    invoiceData.setPaidAmount(0.0); // No payment in returns
                    invoiceData.setDiscountAmount(0.0); // No discount in returns
                    
                    // Add metadata
                    invoiceData.setMetadata("contact", contactNumber);
                    invoiceData.setMetadata("tehsil", tehsil);
                    invoiceData.setMetadata("originalInvoiceNumber", originalInvoiceNumber);
                    
                    // Open invoice for print preview
                    boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Sales Return");
                    
                    if (previewSuccess) {
                        String refundDetails = updateBalance ? "Amount refunded to customer balance." : "Cash refund - balance not updated.";
                        showAlert("Success", "Sales return invoice created successfully!\n\nReturn Invoice Number: " + returnInvoiceNumber + 
                                "\nRefund Method: " + refundMethod + "\n" + refundDetails + 
                                "\n\nThe return invoice has been opened for preview and printing.");
                    } else {
                        // Fallback to printer selection if preview fails
                        boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Sales Return");
                        if (printSuccess) {
                            String refundDetails = updateBalance ? "Amount refunded to customer balance." : "Cash refund - balance not updated.";
                            showAlert("Success", "Sales return invoice created and printed successfully!\n\nReturn Invoice Number: " + returnInvoiceNumber + 
                                    "\nRefund Method: " + refundMethod + "\n" + refundDetails);
                        } else {
                            String refundDetails = updateBalance ? "Amount refunded to customer balance." : "Cash refund - balance not updated.";
                            showAlert("Partial Success", "Sales return invoice created successfully but printing failed.\n\nReturn Invoice Number: " + returnInvoiceNumber + 
                                    "\nRefund Method: " + refundMethod + "\n" + refundDetails);
                        }
                    }
                    
                    // Clear form and generate new return invoice number
                    String newReturnInvoiceNumber = database.generateSalesReturnInvoiceNumber();
                    returnInvoiceNumberField.setText(newReturnInvoiceNumber);
                    originalInvoiceComboBox.setValue(null);
                    returnDatePicker.setValue(LocalDate.now());
                    customerField.clear();
                    returnAmountField.clear();
                    refundMethodComboBox.setValue("Refund to Balance"); // Reset to default
                    availableItems.clear();
                    returnItems.clear();
                } else {
                    showAlert("Error", "Failed to create sales return invoice.\nThis could be due to database error or stock update failure. Please try again.");
                }
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Invalid return amount");
            }
        });

        return mainScrollPane;
    }

    private static VBox createProductionStockUsageReportForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");

        Label heading = createHeading("Production Stock Usage Report");

        // Date range selection
        HBox dateRangeBox = new HBox(20);
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(30)); // Default to last 30 days
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now());
        dateRangeBox.getChildren().addAll(
            createFormRow("Start Date:", startDatePicker),
            createFormRow("End Date:", endDatePicker)
        );

        // Control buttons - removed Generate Report and Export to CSV
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        Button openReportBtn = createSubmitButton("Open Report Window");
        Button refreshBtn = createActionButton("Refresh");
        controlBox.getChildren().addAll(openReportBtn, refreshBtn);

        // Summary statistics
        HBox summaryBox = new HBox(30);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(15));
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label totalRecordsLabel = new Label("Total Records: 0");
        totalRecordsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        Label productionRecordsLabel = new Label("Production Purchase Records: 0");
        productionRecordsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        Label rawUsageRecordsLabel = new Label("Production Return Records: 0");
        rawUsageRecordsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        
        summaryBox.getChildren().addAll(totalRecordsLabel, productionRecordsLabel, rawUsageRecordsLabel);

        // Create the main usage report table
        TableView<UsageReportRecord> usageTable = createUsageReportTable();
        usageTable.setPrefHeight(450);
        usageTable.setMaxHeight(Double.MAX_VALUE);
        
        // Table container with scroll
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(
            createSubheading("Production Stock Purchase & Return Records"),
            usageTable
        );
        
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        VBox.setVgrow(usageTable, Priority.ALWAYS);

        form.getChildren().addAll(
            heading,
            dateRangeBox,
            controlBox,
            summaryBox,
            tableContainer
        );

        // Event handlers
        openReportBtn.setOnAction(e -> {
            String startDate = startDatePicker.getValue().format(DATE_FORMATTER);
            String endDate = endDatePicker.getValue().format(DATE_FORMATTER);
            openProductionStockUsageReportWindow(startDate, endDate);
        });
        
        refreshBtn.setOnAction(e -> {
            String startDate = startDatePicker.getValue().format(DATE_FORMATTER);
            String endDate = endDatePicker.getValue().format(DATE_FORMATTER);
            loadProductionStockUsageReportData(usageTable, startDate, endDate, totalRecordsLabel, productionRecordsLabel, rawUsageRecordsLabel);
        });

        // Load initial data
        String initialStartDate = startDatePicker.getValue().format(DATE_FORMATTER);
        String initialEndDate = endDatePicker.getValue().format(DATE_FORMATTER);
        loadProductionStockUsageReportData(usageTable, initialStartDate, initialEndDate, totalRecordsLabel, productionRecordsLabel, rawUsageRecordsLabel);

        return form;
    }

    // Create table for combined usage report
    private static TableView<UsageReportRecord> createUsageReportTable() {
        TableView<UsageReportRecord> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Date column
        TableColumn<UsageReportRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        dateCol.setPrefWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");
        
        // Type column (Production or Raw Usage)
        TableColumn<UsageReportRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(100);
        typeCol.setCellFactory(col -> new TableCell<UsageReportRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Production Purchase".equals(item)) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else if ("Production Return".equals(item)) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Invoice/Reference Number column
        TableColumn<UsageReportRecord, String> invoiceCol = new TableColumn<>("Invoice/Ref #");
        invoiceCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        invoiceCol.setPrefWidth(130);
        
        // Item Name column
        TableColumn<UsageReportRecord, String> itemCol = new TableColumn<>("Item Name");
        itemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        itemCol.setPrefWidth(200);
        
        // Brand column
        TableColumn<UsageReportRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        brandCol.setPrefWidth(120);
        
        // Quantity column
        TableColumn<UsageReportRecord, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> new SimpleStringProperty(formatNumber(data.getValue().getQuantity())));
        quantityCol.setPrefWidth(100);
        quantityCol.setStyle("-fx-alignment: CENTER;");
        
        // Purpose/Notes column
        TableColumn<UsageReportRecord, String> purposeCol = new TableColumn<>("Purpose/Notes");
        purposeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPurpose()));
        purposeCol.setPrefWidth(150);
        
        // Unit Cost column
        TableColumn<UsageReportRecord, String> unitCostCol = new TableColumn<>("Unit Cost");
        unitCostCol.setCellValueFactory(data -> new SimpleStringProperty(formatNumber(data.getValue().getUnitCost())));
        unitCostCol.setPrefWidth(100);
        unitCostCol.setStyle("-fx-alignment: CENTER;");
        
        // Total Value column
        TableColumn<UsageReportRecord, String> totalValueCol = new TableColumn<>("Total Value");
        totalValueCol.setCellValueFactory(data -> new SimpleStringProperty(formatNumber(data.getValue().getTotalValue())));
        totalValueCol.setPrefWidth(110);
        totalValueCol.setStyle("-fx-alignment: CENTER;");
        totalValueCol.setCellFactory(col -> new TableCell<UsageReportRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
                }
            }
        });
        
        table.getColumns().addAll(dateCol, typeCol, invoiceCol, itemCol, brandCol, quantityCol, purposeCol, unitCostCol, totalValueCol);
        
        // Set placeholder text
        table.setPlaceholder(new Label("No usage records found for the selected date range"));
        
        return table;
    }

    // Load production stock usage report data from database (only production purchase and return invoices)
    private static void loadProductionStockUsageReportData(TableView<UsageReportRecord> table, String startDate, String endDate,
                                          Label totalRecordsLabel, Label productionRecordsLabel, Label rawUsageRecordsLabel) {
        ObservableList<UsageReportRecord> records = FXCollections.observableArrayList();
        int productionPurchaseCount = 0;
        int productionReturnCount = 0;
        
        try {
            // Load Production Invoice records (Production Stock Purchase)
            List<Object[]> productionInvoices = database.getAllProductionInvoices();
            for (Object[] invoice : productionInvoices) {
                String invoiceDate = (String) invoice[1]; // production_date
                
                // Check if date falls within range
                if (isDateInRange(invoiceDate, startDate, endDate)) {
                    int invoiceId = (Integer) invoice[0];
                    String notes = (String) invoice[2];
                    String productName = (String) invoice[3];
                    double quantity = ((Number) invoice[4]).doubleValue();
                    
                    // Get additional details for production items
                    try {
                        List<Object[]> productionItems = sqliteDatabase.getProductionItemsByInvoiceId(invoiceId);
                        for (Object[] item : productionItems) {
                            String itemProductName = (String) item[1];
                            String brandName = (String) item[2];
                            double itemQuantity = ((Number) item[3]).doubleValue();
                            double unitCost = ((Number) item[4]).doubleValue();
                            
                            records.add(new UsageReportRecord(
                                invoiceDate,
                                "Production Purchase",
                                "PROD-" + invoiceId,
                                itemProductName,
                                brandName,
                                itemQuantity,
                                notes != null ? notes : "Production",
                                unitCost,
                                itemQuantity * unitCost
                            ));
                            productionPurchaseCount++;
                        }
                    } catch (Exception e) {
                        // Fallback - use invoice level data
                        records.add(new UsageReportRecord(
                            invoiceDate,
                            "Production Purchase",
                            "PROD-" + invoiceId,
                            productName,
                            "Unknown",
                            quantity,
                            notes != null ? notes : "Production",
                            0.0,
                            0.0
                        ));
                        productionPurchaseCount++;
                    }
                }
            }
            
            // Load Production Return Invoice records (Production Stock Return Purchase)
            List<Object[]> productionReturnInvoices = sqliteDatabase.getAllProductionReturnInvoices();
            for (Object[] invoice : productionReturnInvoices) {
                String returnDate = (String) invoice[2]; // return_date (corrected index)
                
                // Check if date falls within range
                if (isDateInRange(returnDate, startDate, endDate)) {
                    int returnInvoiceId = (Integer) invoice[0];
                    String returnInvoiceNumber = (String) invoice[1];
                    double totalReturnQuantity = ((Number) invoice[3]).doubleValue();
                    double totalReturnAmount = ((Number) invoice[4]).doubleValue();
                    String notes = (String) invoice[5];
                    
                    // Get return invoice items
                    try {
                        List<Object[]> returnItems = sqliteDatabase.getProductionReturnInvoiceItems(returnInvoiceId);
                        for (Object[] item : returnItems) {
                            int productionId = (Integer) item[1]; // production_id
                            double returnQuantity = ((Number) item[2]).doubleValue();
                            double unitCost = ((Number) item[3]).doubleValue();
                            double totalCost = ((Number) item[4]).doubleValue();
                            
                            // Get production stock details
                            String itemName = "Unknown Item";
                            String brandName = "Unknown Brand";
                            try {
                                itemName = getProductionStockNameById(productionId);
                                // For now, we'll use "Unknown Brand" as we need brand info from production stock
                                brandName = "Production Stock";
                            } catch (Exception e) {
                                System.err.println("Error getting production stock details: " + e.getMessage());
                            }
                            
                            records.add(new UsageReportRecord(
                                returnDate,
                                "Production Return",
                                returnInvoiceNumber,
                                itemName,
                                brandName,
                                returnQuantity,
                                notes != null ? notes : "Production Return",
                                unitCost,
                                totalCost
                            ));
                            productionReturnCount++;
                        }
                    } catch (Exception e) {
                        // Fallback - use invoice level data
                        records.add(new UsageReportRecord(
                            returnDate,
                            "Production Return",
                            returnInvoiceNumber,
                            "Mixed Production Items",
                            "Various",
                            totalReturnQuantity,
                            notes != null ? notes : "Production Return",
                            totalReturnAmount / totalReturnQuantity,
                            totalReturnAmount
                        ));
                        productionReturnCount++;
                    }
                }
            }
            
            // Sort records by date (most recent first)
            records.sort((r1, r2) -> r2.getDate().compareTo(r1.getDate()));
            
            // Update table
            table.setItems(records);
            
            // Update summary labels
            totalRecordsLabel.setText("Total Records: " + records.size());
            productionRecordsLabel.setText("Production Purchase Records: " + productionPurchaseCount);
            rawUsageRecordsLabel.setText("Production Return Records: " + productionReturnCount);
            
        } catch (Exception e) {
            System.err.println("Error loading production stock usage report data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to load production stock usage report data: " + e.getMessage());
            
            // Reset summary labels on error
            totalRecordsLabel.setText("Total Records: 0");
            productionRecordsLabel.setText("Production Purchase Records: 0");
            rawUsageRecordsLabel.setText("Production Return Records: 0");
        }
    }
    
    // Helper method to check if a date falls within the specified range
    private static boolean isDateInRange(String dateStr, String startDate, String endDate) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            
            return !date.isBefore(start) && !date.isAfter(end);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return false;
        }
    }

    // Method to open production stock usage report in a new window
    private static void openProductionStockUsageReportWindow(String startDate, String endDate) {
        Stage reportStage = new Stage();
        reportStage.setTitle("Production Stock Usage Report - " + startDate + " to " + endDate);
        reportStage.initModality(Modality.NONE); // Allow interaction with parent window
        
        // Create main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.getStyleClass().add("form-container");
        
        // Header with title and date range
        VBox headerBox = new VBox(10);
        Label titleLabel = new Label("Production Stock Usage Report");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label dateRangeLabel = new Label("Date Range: " + startDate + " to " + endDate);
        dateRangeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        
        headerBox.getChildren().addAll(titleLabel, dateRangeLabel);
        
        // Summary statistics
        HBox summaryBox = new HBox(30);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(15));
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Label totalRecordsLabel = new Label("Total Records: 0");
        totalRecordsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        Label productionPurchaseLabel = new Label("Production Purchase Records: 0");
        productionPurchaseLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        Label productionReturnLabel = new Label("Production Return Records: 0");
        productionReturnLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        
        summaryBox.getChildren().addAll(totalRecordsLabel, productionPurchaseLabel, productionReturnLabel);
        
        // Create the report table
        TableView<UsageReportRecord> reportTable = createUsageReportTable();
        reportTable.setPrefHeight(500);
        reportTable.setMaxHeight(Double.MAX_VALUE);
        
        // Refresh button
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button refreshBtn = createActionButton("Refresh Data");
        Button closeBtn = createActionButton("Close Window");
        buttonBox.getChildren().addAll(refreshBtn, closeBtn);
        
        // Event handlers
        refreshBtn.setOnAction(e -> {
            loadProductionStockUsageReportData(reportTable, startDate, endDate, 
                totalRecordsLabel, productionPurchaseLabel, productionReturnLabel);
        });
        
        closeBtn.setOnAction(e -> reportStage.close());
        
        // Load initial data
        loadProductionStockUsageReportData(reportTable, startDate, endDate, 
            totalRecordsLabel, productionPurchaseLabel, productionReturnLabel);
        
        // Add all components to main container
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        mainContainer.getChildren().addAll(headerBox, summaryBox, reportTable, buttonBox);
        
        // Create scene and show
        Scene scene = new Scene(mainContainer, 1000, 700);
        
        // Apply CSS if available
        try {
            scene.getStylesheets().add(ProductionStock.class.getResource("/com/cablemanagement/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        
        reportStage.setScene(scene);
        reportStage.show();
    }

    // Data model class for usage report records
    private static class UsageReportRecord {
        private final String date;
        private final String type;
        private final String invoiceNumber;
        private final String itemName;
        private final String brand;
        private final double quantity;
        private final String purpose;
        private final double unitCost;
        private final double totalValue;
        
        public UsageReportRecord(String date, String type, String invoiceNumber, String itemName, String brand,
                               double quantity, String purpose, double unitCost, double totalValue) {
            this.date = date;
            this.type = type;
            this.invoiceNumber = invoiceNumber;
            this.itemName = itemName;
            this.brand = brand;
            this.quantity = quantity;
            this.purpose = purpose;
            this.unitCost = unitCost;
            this.totalValue = totalValue;
        }
        
        // Getters
        public String getDate() { return date; }
        public String getType() { return type; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public String getItemName() { return itemName; }
        public String getBrand() { return brand; }
        public double getQuantity() { return quantity; }
        public String getPurpose() { return purpose; }
        public double getUnitCost() { return unitCost; }
        public double getTotalValue() { return totalValue; }
    }

    private static ListView<String> createEnhancedListView() {
        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(300);
        listView.setPlaceholder(new Label("No items added"));
        
        // Context menu for delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete Selected");
        deleteItem.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                listView.getItems().remove(selectedIndex);
            }
        });
        contextMenu.getItems().add(deleteItem);
        listView.setContextMenu(contextMenu);
        
        // Keyboard delete support
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    listView.getItems().remove(selectedIndex);
                }
            }
        });
        
        return listView;
    }

    // Production Stock specific methods
    private static void handleProductionStockSubmit(
            TextField nameField, ComboBox<String> categoryCombo, ComboBox<String> manufacturerCombo, 
            ComboBox<String> brandCombo, ComboBox<String> unitCombo,
            TextField quantityField, TextField unitCostField, TextField salePriceField,
            TableView<ProductionStockRecord> stockTable,
            Label totalItemsLabel, Label totalValueLabel, Label lowStockLabel) {
        
        String name = nameField.getText().trim();
        String category = categoryCombo.getSelectionModel().getSelectedItem();
        String manufacturer = manufacturerCombo.getSelectionModel().getSelectedItem();
        String brand = brandCombo.getSelectionModel().getSelectedItem();
        String unit = unitCombo.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText().trim();
        String unitCostText = unitCostField.getText().trim();
        String salePriceText = salePriceField.getText().trim();
        
        // Validation
        if (name.isEmpty()) {
            showAlert("Missing Information", "Please enter a product name.");
            nameField.requestFocus();
            return;
        }
        
        if (category == null || category.isEmpty()) {
            showAlert("Missing Information", "Please select a category.");
            categoryCombo.requestFocus();
            return;
        }
        
        if (manufacturer == null || manufacturer.isEmpty()) {
            showAlert("Missing Information", "Please select a manufacturer.");
            manufacturerCombo.requestFocus();
            return;
        }
        
        if (brand == null || brand.isEmpty()) {
            showAlert("Missing Information", "Please select a brand.");
            brandCombo.requestFocus();
            return;
        }
        
        if (unit == null || unit.isEmpty()) {
            showAlert("Missing Information", "Please select a unit.");
            unitCombo.requestFocus();
            return;
        }
        
        if (quantityText.isEmpty()) {
            showAlert("Missing Information", "Please enter quantity.");
            quantityField.requestFocus();
            return;
        }
        
        if (unitCostText.isEmpty()) {
            showAlert("Missing Information", "Please enter unit cost.");
            unitCostField.requestFocus();
            return;
        }
        
        if (salePriceText.isEmpty()) {
            showAlert("Missing Information", "Please enter sale price.");
            salePriceField.requestFocus();
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityText);
            double unitCost = Double.parseDouble(unitCostText);
            double salePrice = Double.parseDouble(salePriceText);
            
            if (quantity <= 0) {
                showAlert("Invalid Input", "Quantity must be greater than 0.");
                quantityField.requestFocus();
                return;
            }
            
            if (unitCost <= 0) {
                showAlert("Invalid Input", "Unit cost must be greater than 0.");
                unitCostField.requestFocus();
                return;
            }
            
            if (salePrice <= 0) {
                showAlert("Invalid Input", "Sale price must be greater than 0.");
                salePriceField.requestFocus();
                return;
            }
            
            if (salePrice <= unitCost) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Price Warning");
                alert.setHeaderText("Sale price is less than or equal to unit cost");
                alert.setContentText(String.format("Sale Price: %.2f\nUnit Cost: %.2f\n\nThis will result in no profit or a loss. Do you want to continue?", salePrice, unitCost));
                
                if (alert.showAndWait().get() != ButtonType.OK) {
                    salePriceField.requestFocus();
                    return;
                }
            }
            
            // Check if product with same name and brand already exists
            if (database.productionStockExists(name, brand)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Product Exists");
                alert.setHeaderText("Product already exists");
                alert.setContentText("A product with the name '" + name + "' and brand '" + brand + "' already exists.\n\nDo you want to add this quantity to existing stock?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    // Add to existing stock
                    boolean success = database.addToProductionStock(name, brand, quantity, unitCost, salePrice);
                    if (success) {
                        showAlert("Success", "Quantity added to existing production stock successfully!");
                    } else {
                        showAlert("Error", "Failed to add quantity to existing stock.");
                        return;
                    }
                } else {
                    return; // User cancelled
                }
            } else {
                // Insert new production stock - using the method with category, manufacturer, unitCost and salePrice parameters  
                boolean success = database.insertProductionStock(name, category, manufacturer, brand, unit, quantity, unitCost, salePrice);
                
                if (!success) {
                    showAlert("Error", "Failed to register production stock. Please check database connection.");
                    return;
                }
                
                showAlert("Success", String.format("Production Stock registered successfully!\n\nProduct: %s\nCategory: %s\nManufacturer: %s\nBrand: %s\nUnit: %s\nQuantity: %d\nUnit Cost: %.2f\nSale Price: %.2f\nProfit Margin: %.1f%%", 
                    name, category, manufacturer, brand, unit, quantity, unitCost, salePrice, ((salePrice - unitCost) / unitCost) * 100));
            }
            
            // Clear form after successful submission
            nameField.clear();
            categoryCombo.getSelectionModel().clearSelection();
            manufacturerCombo.getSelectionModel().clearSelection();
            brandCombo.getSelectionModel().clearSelection();
            unitCombo.getSelectionModel().clearSelection();
            quantityField.clear();
            unitCostField.clear();
            salePriceField.clear();
            nameField.requestFocus();
            
            // Refresh table and summary
            refreshProductionStockTable(stockTable);
            updateStockSummary(stockTable, totalItemsLabel, totalValueLabel, lowStockLabel);
            
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid numbers for Quantity, Unit Cost, and Sale Price.\n\nQuantity should be a whole number.\nUnit Cost and Sale Price should be decimal numbers.");
        } catch (Exception ex) {
            showAlert("Unexpected Error", "An error occurred while registering the product: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static TableView<ProductionStockRecord> createProductionStockTable() {
        TableView<ProductionStockRecord> table = new TableView<>();
        // table.setPrefHeight(300);
        // table.setMaxHeight(300);
        table.getStyleClass().add("table-view");
        
        // Use UNCONSTRAINED_RESIZE_POLICY for better column sizing
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        TableColumn<ProductionStockRecord, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(180);
        nameCol.setMinWidth(150);
        nameCol.setResizable(true);

        TableColumn<ProductionStockRecord, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription())); // Using description field for category for now
        categoryCol.setPrefWidth(120);
        categoryCol.setMinWidth(100);
        categoryCol.setResizable(true);

        TableColumn<ProductionStockRecord, String> manufacturerCol = new TableColumn<>("Manufacturer");
        manufacturerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription())); // Using brandDescription field for manufacturer for now
        manufacturerCol.setPrefWidth(140);
        manufacturerCol.setMinWidth(120);
        manufacturerCol.setResizable(true);
        
        TableColumn<ProductionStockRecord, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        brandCol.setPrefWidth(130);
        brandCol.setMinWidth(110);
        brandCol.setResizable(true);
        
        TableColumn<ProductionStockRecord, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));
        unitCol.setPrefWidth(80);
        unitCol.setMinWidth(70);
        unitCol.setResizable(true);
        
        TableColumn<ProductionStockRecord, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        quantityCol.setPrefWidth(100);
        quantityCol.setMinWidth(90);
        quantityCol.setResizable(true);
        
        TableColumn<ProductionStockRecord, String> unitCostCol = new TableColumn<>("Unit Cost");
        unitCostCol.setCellValueFactory(data -> new SimpleStringProperty(formatNumber(data.getValue().getUnitCost())));
        unitCostCol.setPrefWidth(110);
        unitCostCol.setMinWidth(100);
        unitCostCol.setResizable(true);
        
        TableColumn<ProductionStockRecord, String> totalCostCol = new TableColumn<>("Total Cost");
        totalCostCol.setCellValueFactory(data -> new SimpleStringProperty(formatNumber(data.getValue().getTotalCost())));
        totalCostCol.setPrefWidth(120);
        totalCostCol.setMinWidth(110);
        totalCostCol.setResizable(true);
        
        // Add Edit column with button
        TableColumn<ProductionStockRecord, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(90);
        actionsCol.setMinWidth(80);
        actionsCol.setResizable(false); // Keep action column fixed
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            
            {
                editButton.getStyleClass().add("edit-button");
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                editButton.setOnAction(event -> {
                    ProductionStockRecord record = getTableView().getItems().get(getIndex());
                    openEditProductionStockDialog(record, getTableView());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
        
        table.getColumns().addAll(nameCol, categoryCol, manufacturerCol, brandCol, unitCol, quantityCol, unitCostCol, totalCostCol, actionsCol);
        
        // Set table preferred width to sum of all column widths plus some padding
        table.setPrefWidth(1140); // Sum of all preferred widths + padding
        table.setMinWidth(950);    // Sum of all minimum widths + padding
        
        return table;
    }

    private static void refreshProductionStockTable(TableView<ProductionStockRecord> table) {
        ObservableList<ProductionStockRecord> data = FXCollections.observableArrayList();
        
        try {
            // Get all production stocks from database
            List<Object[]> stockList = database.getAllProductionStocks();
            for (Object[] stock : stockList) {
                data.add(new ProductionStockRecord(
                    (Integer) stock[0],   // production_id
                    (String) stock[1],    // product_name
                    (String) stock[2],    // product_description (empty string)
                    (String) stock[3],    // brand_name
                    (String) stock[4],    // brand_description (empty string)
                    (String) stock[5] != null ? (String) stock[5] : "N/A",   // unit_name
                    (Integer) stock[6],   // quantity
                    (Double) stock[7],    // unit_cost
                    (Double) stock[8]     // sale_price
                ));
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to refresh production stock table: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to load production stock data: " + e.getMessage());
        }
        
        table.setItems(data);
    }

    // Simple record class for table display

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

    private static Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("action-button");
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private static Button createSubmitButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("form-submit");
        button.setMaxWidth(Double.MAX_VALUE);
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

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Production Stock ComboBox
    private static ComboBox<String> createProductionStockComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("form-input");
        comboBox.setPromptText("Select Production Stock");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load production stocks from database
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
            ObservableList<String> items = FXCollections.observableArrayList();
            
            for (Object[] stock : productionStocks) {
                // Format: "Product Name - Brand - Available: X"
                String item = String.format("%s - %s - Available: %s", 
                    stock[1], // product_name
                    stock[3], // brand_name
                    formatNumber(((Number) stock[6]).doubleValue())  // quantity
                );
                items.add(item);
            }
            
            comboBox.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load production stocks: " + e.getMessage());
        }
        
        return comboBox;
    }

    // Raw Stock ComboBox
    private static ComboBox<String> createRawStockComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("form-input");
        comboBox.setPromptText("Select Raw Material");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load raw stocks from database
        try {
            List<Object[]> rawStocks = database.getAllRawStocksWithUnitsForDropdown();
            ObservableList<String> items = FXCollections.observableArrayList();
            
            for (Object[] stock : rawStocks) {
                // Format: "Raw Material Name - Brand - Available: X"
                String item = String.format("%s - %s - Available: %s", 
                    stock[1], // item_name
                    stock[3], // brand_name
                    formatNumber(((Number) stock[5]).doubleValue())  // quantity
                );
                items.add(item);
            }
            
            comboBox.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load raw stocks: " + e.getMessage());
        }
        
        return comboBox;
    }

    // Handle adding production item with ComboBox
    private static void handleAddProductionItem(ComboBox<String> productComboBox, TextField quantityField, ListView<String> itemsList) {
        String selectedProduct = productComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        
        if (selectedProduct != null && !quantityText.isEmpty()) {
            try {
                double quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    showAlert("Invalid Input", "Quantity must be greater than 0");
                    return;
                }
                
                // Extract product name from the ComboBox selection
                String productName = selectedProduct.split(" - ")[0];
                String displayText = String.format("%s - Quantity: %s", productName, formatNumber(quantity));
                
                itemsList.getItems().add(displayText);
                productComboBox.setValue(null);
                quantityField.clear();
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for quantity");
            }
        } else {
            showAlert("Missing Input", "Please select a product and enter quantity");
        }
    }

    // Handle adding raw material with ComboBox
    private static void handleAddRawMaterial(ComboBox<String> rawMaterialComboBox, TextField quantityField, ListView<String> materialsList) {
        String selectedMaterial = rawMaterialComboBox.getValue();
        String quantityText = quantityField.getText().trim();
        
        if (selectedMaterial != null && !quantityText.isEmpty()) {
            try {
                double quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    showAlert("Invalid Input", "Quantity must be greater than 0");
                    return;
                }
                
                // Extract material name from the ComboBox selection
                String materialName = selectedMaterial.split(" - ")[0];
                String displayText = String.format("%s - Quantity Used: %s", materialName, formatNumber(quantity));
                
                materialsList.getItems().add(displayText);
                rawMaterialComboBox.setValue(null);
                quantityField.clear();
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for quantity");
            }
        } else {
            showAlert("Missing Input", "Please select a raw material and enter quantity");
        }
    }

    // Handle production invoice submission
    private static void handleSubmitProductionInvoice(TextField invoiceNumberField, DatePicker productionDatePicker, TextArea notesArea, 
                                                    ListView<String> itemsList, ListView<String> materialsList) {
        try {
            String productionDate = productionDatePicker.getValue().format(DATE_FORMATTER);
            String notes = notesArea.getText().trim();
            
            if (itemsList.getItems().isEmpty()) {
                showAlert("Missing Items", "Please add at least one production item");
                return;
            }
            
            // Insert production invoice and get ID
            int invoiceId = database.insertProductionInvoiceAndGetId(productionDate, notes);
            if (invoiceId == -1) {
                showAlert("Error", "Failed to create production invoice");
                return;
            }
            
            // Prepare production items data
            List<Object[]> productionItems = new ArrayList<>();
            for (String item : itemsList.getItems()) {
                // Parse the display text to extract data
                String[] parts = item.split(" - Quantity: ");
                if (parts.length == 2) {
                    String productName = parts[0];
                    double quantity = Double.parseDouble(parts[1]);
                    
                    // Get production stock ID by name
                    int productionStockId = getProductionStockIdByName(productName);
                    if (productionStockId != -1) {
                        productionItems.add(new Object[]{productionStockId, quantity});
                    } else {
                        showAlert("Error", "Production stock not found for product: " + productName);
                        return;
                    }
                }
            }
            
            if (productionItems.isEmpty()) {
                showAlert("Error", "No valid production items found");
                return;
            }
            
            // Insert production invoice items and update stock quantities
            if (!database.insertProductionInvoiceItems(invoiceId, productionItems)) {
                showAlert("Error", "Failed to save production items and update stock quantities");
                return;
            }

            // Prepare items for printing
            List<Item> printItems = new ArrayList<>();
            for (String item : itemsList.getItems()) {
                String[] parts = item.split(" - Quantity: ");
                if (parts.length == 2) {
                    String productName = parts[0];
                    double quantity = Double.parseDouble(parts[1]);
                    
                    // Get production stock ID by name to retrieve unit information
                    int productionStockId = getProductionStockIdByName(productName);
                    String unit = "N/A";
                    if (productionStockId != -1) {
                        unit = getProductionStockUnit(productionStockId);
                    }
                    
                    // Format the item name as "name - unit"
                    String itemNameWithUnit = productName + " - " + unit;
                    
                    printItems.add(new Item(
                        itemNameWithUnit,
                        (int) quantity,
                        0.0, // unit price not applicable for production
                        0.0  // discount not applicable for production
                    ));
                }
            }

            // Create invoice data for printing with proper type and metadata
            InvoiceData invoiceData = new InvoiceData(
                InvoiceData.TYPE_PRODUCTION,
                invoiceNumberField.getText(),
                productionDate,
                "PRODUCTION INVOICE",
                "", // Empty address field
                printItems,
                0.0 // not applicable for production
            );
            
            // Add reference/notes as metadata
            invoiceData.setMetadata("tehsil", "");
            invoiceData.setMetadata("contact", "");
            invoiceData.setMetadata("notes", notes);
            
            // Open invoice for print preview
            boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Production");
            
            if (previewSuccess) {
                showAlert("Success", "Production invoice created and opened for preview!\n\nInvoice Number: " + 
                    invoiceNumberField.getText() + "\n\nProceeed with printing from the preview window.");
            } else {
                // Fallback to printer selection if preview fails
                boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Production");
                if (printSuccess) {
                    showAlert("Success", "Production invoice created and printed successfully!\n\nInvoice Number: " + 
                        invoiceNumberField.getText());
                } else {
                    showAlert("Partial Success", "Production invoice created but printing failed.\n\nInvoice Number: " + 
                        invoiceNumberField.getText() + "\n\nYou can print it later if needed.");
                }
            }
            
            // Prepare raw materials data (if any)
            if (!materialsList.getItems().isEmpty()) {
                List<Object[]> rawMaterialsUsed = new ArrayList<>();
                for (String material : materialsList.getItems()) {
                    // Parse the display text to extract data
                    String[] parts = material.split(" - Quantity Used: ");
                    if (parts.length == 2) {
                        String materialName = parts[0];
                        double quantity = Double.parseDouble(parts[1]);
                        
                        // Get raw stock ID by name
                        int rawStockId = database.getRawStockIdByName(materialName);
                        if (rawStockId != -1) {
                            rawMaterialsUsed.add(new Object[]{rawStockId, quantity});
                        }
                    }
                }
                
                // Insert raw material usage
                if (!database.insertProductionStockRawUsage(invoiceId, rawMaterialsUsed)) {
                    showAlert("Warning", "Production invoice created but failed to save raw material usage");
                }
            }
            
            showAlert("Success", "Production invoice created successfully!\nProduction stock quantities have been updated.");
            
            // Clear form
            invoiceNumberField.setText(sqliteDatabase.generateProductionInvoiceNumber());
            productionDatePicker.setValue(LocalDate.now());
            notesArea.clear();
            itemsList.getItems().clear();
            materialsList.getItems().clear();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit production invoice: " + e.getMessage());
        }
    }

    // Helper method to get production stock ID by name
    private static int getProductionStockIdByName(String productName) {
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
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

    // Helper method to get production stock name by ID
    private static String getProductionStockNameById(int productionId) {
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
            for (Object[] stock : productionStocks) {
                if (((Integer) stock[0]).equals(productionId)) {
                    return stock[1].toString(); // product_name
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Product";
    }

    // Helper method to get production stock unit cost by production ID
    private static double getProductionStockUnitCost(int productionId) {
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
            for (Object[] stock : productionStocks) {
                if (((Integer) stock[0]).equals(productionId)) {
                    // stock[5] contains unit_cost based on getAllProductionStocksForDropdown structure
                    return (Double) stock[5]; // unit_cost
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Helper method to get production stock unit by production ID
    private static String getProductionStockUnit(int productionId) {
        try {
            List<Object[]> productionStocks = database.getAllProductionStocksForDropdown();
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

    // Handle return production invoice submission
    private static boolean handleSubmitReturnProductionInvoice(String returnInvoiceNumber, LocalDate returnDate, 
                                                          String selectedProductionInvoice, ObservableList<String> returnItems,
                                                          String totalReturnQuantity, TextField returnInvoiceNumberField,
                                                          DatePicker returnDatePicker, ComboBox<String> productionInvoiceCombo,
                                                          ListView<String> availableItemsList, ListView<String> returnItemsList,
                                                          ComboBox<String> itemCombo, TextField returnQuantityField,
                                                          TextField totalReturnQuantityField) {
        try {
            // Validation
            if (selectedProductionInvoice == null || selectedProductionInvoice.isEmpty()) {
                showAlert("Missing Information", "Please select a production invoice");
                return false;
            }
            
            if (returnItems.isEmpty()) {
                showAlert("Missing Information", "Please add at least one item to return");
                return false;
            }
            
            if (totalReturnQuantity.isEmpty() || Integer.parseInt(totalReturnQuantity) <= 0) {
                showAlert("Missing Information", "Total return quantity must be greater than 0");
                return false;
            }

            // Extract production invoice ID
            String productionInvoiceId = selectedProductionInvoice.split(" - ")[0].replace("Invoice #", "");
            
            // Create return invoice record
            String formattedDate = returnDate.format(DATE_FORMATTER);
            String notes = "Return for Invoice #" + productionInvoiceId;
            double totalQuantity = Double.parseDouble(totalReturnQuantity);
            int originalProductionInvoiceId = Integer.parseInt(productionInvoiceId);
            
            // First, prepare return invoice items and calculate totals
            List<Object[]> returnInvoiceItems = new ArrayList<>();
            double totalAmount = 0.0;
            
            for (String returnItem : returnItems) {
                try {
                    // Parse item format: "ProductName - BrandName - Quantity: X (Return Qty: Y)"
                    String[] parts = returnItem.split(" \\(Return Qty: ");
                    if (parts.length == 2) {
                        String productPart = parts[0]; // "ProductName - BrandName - Quantity: X"
                        String returnQtyPart = parts[1].replace(")", ""); // "Y"
                        
                        // Extract product name and brand name (everything before " - Quantity:")
                        String[] productParts = productPart.split(" - Quantity:");
                        if (productParts.length == 2) {
                            String fullProductInfo = productParts[0]; // "ProductName - BrandName"
                            String[] productInfoParts = fullProductInfo.split(" - ");
                            if (productInfoParts.length >= 2) {
                                String productName = productInfoParts[0];
                                double returnQuantity = Double.parseDouble(returnQtyPart);
                                
                                // Get production stock ID and unit cost by name
                                int productionStockId = getProductionStockIdByName(productName);
                                if (productionStockId > 0) {
                                    double unitCost = getProductionStockUnitCost(productionStockId);
                                    double totalCost = returnQuantity * unitCost;
                                    totalAmount += totalCost;
                                    
                                    returnInvoiceItems.add(new Object[]{
                                        productionStockId,  // production_id
                                        returnQuantity,     // quantity_returned (Double)
                                        unitCost,          // unit_cost (Double)
                                        totalCost          // total_cost (Double)
                                    });
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing return item: " + returnItem + " - " + e.getMessage());
                }
            }
            
            // Insert return invoice and get ID
            int returnInvoiceId = sqliteDatabase.insertProductionReturnInvoiceAndGetId(
                returnInvoiceNumber, 
                originalProductionInvoiceId, 
                formattedDate, 
                totalQuantity, 
                totalAmount, 
                notes
            );
            
            if (returnInvoiceId > 0) {
                // Insert return invoice items
                if (!returnInvoiceItems.isEmpty()) {
                    sqliteDatabase.insertProductionReturnInvoiceItems(returnInvoiceId, returnInvoiceItems);
                    
                    // Wrap printing logic in try-catch like Purchase Return
                    try {
                        // Prepare items for printing
                        List<Item> printItems = new ArrayList<>();
                        for (Object[] returnItemData : returnInvoiceItems) {
                            int productionStockId = (Integer) returnItemData[0];
                            double returnQuantity = (Double) returnItemData[1];
                            double unitCost = (Double) returnItemData[2];
                            
                            // Get product name and unit for this production stock ID
                            String productName = getProductionStockNameById(productionStockId);
                            String unit = getProductionStockUnit(productionStockId);
                            
                            // Format the item name as "name - unit"
                            String itemNameWithUnit = productName + " - " + unit;
                            
                            printItems.add(new Item(
                                itemNameWithUnit,
                                (int) returnQuantity,
                                unitCost,
                                0.0 // no discount for returns
                            ));
                        }
                        
                        // Create invoice data for printing with proper type and metadata
                        InvoiceData invoiceData = new InvoiceData(
                            InvoiceData.TYPE_PRODUCTION_RETURN,
                            returnInvoiceNumber,
                            formattedDate,
                            "PRODUCTION RETURN INVOICE",
                            "", // Empty address field
                            printItems,
                            0.0 // no previous balance for returns
                        );
                        
                        // Add notes as metadata
                        invoiceData.setMetadata("tehsil", "");
                        invoiceData.setMetadata("contact", "");
                        invoiceData.setMetadata("notes", notes);
                        
                        // Open invoice for print preview
                        boolean previewSuccess = PrintManager.openInvoiceForPrintPreview(invoiceData, "Production Return");
                        
                        if (!previewSuccess) {
                            // Fallback to printer selection if preview fails
                            boolean printSuccess = PrintManager.printInvoiceWithPrinterSelection(invoiceData, "Production Return");
                            if (!printSuccess) {
                                showAlert("Error", "Failed to print return invoice " + returnInvoiceNumber);
                            }
                        }
                    } catch (Exception ex) {
                        showAlert("Error", "Failed to prepare return invoice for printing: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    
                    showAlert("Success", "Return production invoice saved successfully!\nReturn Invoice Number: " + returnInvoiceNumber);
                    
                    // Clear form
                    try {
                        String newReturnInvoiceNumber = sqliteDatabase.generateProductionReturnInvoiceNumber();
                        returnInvoiceNumberField.setText(newReturnInvoiceNumber);
                    } catch (Exception e) {
                        returnInvoiceNumberField.setText("Error generating number");
                    }
                    
                    returnDatePicker.setValue(LocalDate.now());
                    productionInvoiceCombo.setValue(null);
                    availableItemsList.getItems().clear();
                    returnItemsList.getItems().clear();
                    itemCombo.setValue(null);
                    returnQuantityField.clear();
                    totalReturnQuantityField.setText("0");
                    
                    return true;
                } else {
                    showAlert("Error", "No valid return items found to save");
                    return false;
                }
            } else {
                showAlert("Error", "Failed to create return production invoice");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit return production invoice: " + e.getMessage());
            return false;
        }
    }

    // Helper methods for sales invoice functionality
    private static void updatePaymentSummary(ObservableList<SalesInvoiceItemUI> items, 
                                           TextField discountField, TextField paidAmountField,
                                           Label subtotalLabel, Label discountLabel, 
                                           Label totalAmountLabel, Label balanceLabel) {
        try {
            // Calculate subtotal
            double subtotal = items.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
            
            // Get discount and paid amounts
            double discount = 0.0;
            double paidAmount = 0.0;
            
            try {
                String discountText = discountField.getText().trim();
                discount = discountText.isEmpty() ? 0.0 : Double.parseDouble(discountText);
            } catch (NumberFormatException e) {
                discount = 0.0;
            }
            
            try {
                String paidText = paidAmountField.getText().trim();
                paidAmount = paidText.isEmpty() ? 0.0 : Double.parseDouble(paidText);
            } catch (NumberFormatException e) {
                paidAmount = 0.0;
            }
            
            // Calculate total and balance
            double totalAmount = subtotal - discount;
            double balance = totalAmount - paidAmount;
            
            // Update labels
            subtotalLabel.setText(String.format("Subtotal: %.2f", subtotal));
            discountLabel.setText(String.format("Discount: %.2f", discount));
            totalAmountLabel.setText(String.format("Total Amount: %.2f", totalAmount));
            
            // Set balance color based on amount
            if (balance <= 0) {
                balanceLabel.setText("Balance Due: 0.00 (PAID)");
                balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
            } else {
                balanceLabel.setText(String.format("Balance Due: %.2f", balance));
                balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            }
            
        } catch (Exception e) {
            // Fallback in case of any errors
            subtotalLabel.setText("Subtotal: 0.00");
            discountLabel.setText("Discount: 0.00");
            totalAmountLabel.setText("Total Amount: 0.00");
            balanceLabel.setText("Balance Due: 0.00");
        }
    }
    
    private static void updateTotalAmount(ObservableList<SalesInvoiceItemUI> items, Label totalLabel) {
        double total = items.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
        totalLabel.setText(String.format("Total Amount: %.2f", total));
    }

    private static void updateReturnAmount(ObservableList<SalesInvoiceItemUI> items, TextField returnAmountField) {
        double total = items.stream().mapToDouble(SalesInvoiceItemUI::getTotalPrice).sum();
        returnAmountField.setText(formatNumber(total));
    }

    // Helper method to format numbers without unnecessary decimals
    private static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            // It's a whole number
            return String.valueOf((int) value);
        } else {
            // It has decimal places, format to 2 decimal places but remove trailing zeros
            String formatted = String.format("%.2f", value);
            // Remove trailing zeros and decimal point if not needed
            formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
            return formatted;
        }
    }

    // UI Model class for Sales Invoice Items
    public static class SalesInvoiceItemUI {
        private final SimpleStringProperty productName;
        private final SimpleDoubleProperty quantity;
        private final SimpleDoubleProperty unitPrice;
        private final SimpleDoubleProperty discountPercentage;
        private final SimpleDoubleProperty discountAmount;
        private final SimpleDoubleProperty totalPrice;
        private final SimpleDoubleProperty originalQuantity;
        private int productionStockId;

        public SalesInvoiceItemUI(String productName, double quantity, double unitPrice, 
                                 double discountPercentage, double discountAmount) {
            this.productName = new SimpleStringProperty(productName);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
            this.discountPercentage = new SimpleDoubleProperty(discountPercentage);
            this.discountAmount = new SimpleDoubleProperty(discountAmount);
            this.totalPrice = new SimpleDoubleProperty();
            this.originalQuantity = new SimpleDoubleProperty(quantity);
            updateTotalPrice();
        }
        
        // Constructor for backward compatibility
        public SalesInvoiceItemUI(String productName, double quantity, double unitPrice) {
            this(productName, quantity, unitPrice, 0.0, 0.0);
        }

        // Property getters
        public SimpleStringProperty productNameProperty() { return productName; }
        public SimpleDoubleProperty quantityProperty() { return quantity; }
        public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        public SimpleDoubleProperty discountPercentageProperty() { return discountPercentage; }
        public SimpleDoubleProperty discountAmountProperty() { return discountAmount; }
        public SimpleDoubleProperty totalPriceProperty() { return totalPrice; }
        public SimpleDoubleProperty originalQuantityProperty() { return originalQuantity; }

        // Value getters
        public String getProductName() { return productName.get(); }
        public double getQuantity() { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getDiscountPercentage() { return discountPercentage.get(); }
        public double getDiscountAmount() { return discountAmount.get(); }
        public double getTotalPrice() { return totalPrice.get(); }
        public double getOriginalQuantity() { return originalQuantity.get(); }
        public int getProductionStockId() { return productionStockId; }

        // Value setters
        public void setProductName(String productName) { this.productName.set(productName); }
        
        public void setQuantity(double quantity) { 
            this.quantity.set(quantity);
            updateTotalPrice();
        }
        
        public void setUnitPrice(double unitPrice) { 
            this.unitPrice.set(unitPrice);
            updateTotalPrice();
        }
        
        public void setDiscountPercentage(double discountPercentage) {
            this.discountPercentage.set(discountPercentage);
            updateTotalPrice();
        }
        
        public void setDiscountAmount(double discountAmount) {
            this.discountAmount.set(discountAmount);
            updateTotalPrice();
        }
        
        public void setOriginalQuantity(double originalQuantity) { 
            this.originalQuantity.set(originalQuantity); 
        }
        
        public void setProductionStockId(int productionStockId) { 
            this.productionStockId = productionStockId; 
        }

        private void updateTotalPrice() {
            double basePrice = this.quantity.get() * this.unitPrice.get();
            
            // Update discount amount based on percentage and quantity
            double discountPerUnit = (this.discountPercentage.get() / 100.0) * this.unitPrice.get();
            double totalDiscount = discountPerUnit * this.quantity.get();
            this.discountAmount.set(totalDiscount);
            
            // Calculate final price after discount
            double finalPrice = basePrice - totalDiscount;
            this.totalPrice.set(Math.max(0, finalPrice)); // Ensure price is not negative
        }
    }

    // Helper methods for the new two-column production stock form
    
    private static void updateStockSummary(TableView<ProductionStockRecord> stockTable, 
                                         Label totalItemsLabel, Label totalValueLabel, Label lowStockLabel) {
        try {
            ObservableList<ProductionStockRecord> items = stockTable.getItems();
            int totalItems = items.size();
            double totalValue = 0.0;
            int lowStockCount = 0;
            
            for (ProductionStockRecord record : items) {
                // Calculate total value (quantity * sale_price)
                totalValue += record.getQuantity() * record.getSalePrice();
                
                // Count low stock items (less than 10)
                if (record.getQuantity() < 10) {
                    lowStockCount++;
                }
            }
            
            totalItemsLabel.setText("Total Items: " + totalItems);
            totalValueLabel.setText(String.format("Total Value: %.2f", totalValue));
            
            if (lowStockCount > 0) {
                lowStockLabel.setText("Low Stock Items: " + lowStockCount);
                lowStockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            } else {
                lowStockLabel.setText("Low Stock Items: 0");
                lowStockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating stock summary: " + e.getMessage());
            totalItemsLabel.setText("Total Items: 0");
            totalValueLabel.setText("Total Value: 0.00");
            lowStockLabel.setText("Low Stock Items: 0");
        }
    }
    
    private static void filterProductionStockTable(TableView<ProductionStockRecord> stockTable, 
                                                 String searchText, String brandFilter) {
        try {
            // Get all production stock from database
            List<Object[]> allStock = database.getAllProductionStocks();
            ObservableList<ProductionStockRecord> filteredRecords = FXCollections.observableArrayList();
            
            for (Object[] stock : allStock) {
                int productionId = (Integer) stock[0];
                String productName = (String) stock[1];
                String productDesc = (String) stock[2];
                String brandName = (String) stock[3];
                String brandDesc = (String) stock[4];
                String unitName = (String) stock[5];
                int quantity = (Integer) stock[6];
                double unitCost = (Double) stock[7];
                double salePrice = (Double) stock[8];
                
                boolean matchesSearch = true;
                boolean matchesBrand = true;
                
                // Apply search filter
                if (searchText != null && !searchText.trim().isEmpty()) {
                    String search = searchText.toLowerCase();
                    matchesSearch = productName.toLowerCase().contains(search) || 
                                  brandName.toLowerCase().contains(search);
                }
                
                // Apply brand filter
                if (brandFilter != null && !brandFilter.equals("All Brands")) {
                    matchesBrand = brandName.equals(brandFilter);
                }
                
                if (matchesSearch && matchesBrand) {
                    filteredRecords.add(new ProductionStockRecord(
                        productionId, productName, productDesc, brandName, brandDesc, unitName, 
                        quantity, unitCost, salePrice
                    ));
                }
            }
            
            stockTable.setItems(filteredRecords);
            
        } catch (Exception e) {
            System.err.println("Error filtering production stock table: " + e.getMessage());
            showAlert("Error", "Failed to filter table: " + e.getMessage());
        }
    }
    
    /**
     * Opens a dialog to edit production stock item details
     * 
     * @param record The production stock record to edit
     * @param tableView The table view to refresh after editing
     */
    private static void openEditProductionStockDialog(ProductionStockRecord record, TableView<ProductionStockRecord> tableView) {
        // Create a modal dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Production Stock Item");
        dialog.setMinWidth(450);
        dialog.setMinHeight(500);
        
        // Create the form layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        
        // Create form fields
        Label titleLabel = new Label("Edit Product: " + record.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Create input fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(10));
        formGrid.setAlignment(Pos.CENTER);
        
        // Product ID (hidden)
        Label idLabel = new Label("Product ID:");
        TextField idField = new TextField(String.valueOf(record.getProductionId()));
        idField.setEditable(false);
        idField.setVisible(false);
        idLabel.setVisible(false);
        
        // Product Name
        Label nameLabel = new Label("Product Name:");
        TextField nameField = new TextField(record.getName());
        
        // Brand (ComboBox)
        Label brandLabel = new Label("Brand:");
        ComboBox<String> brandComboBox = new ComboBox<>();
        brandComboBox.setPromptText("-- Select Brand --");
        brandComboBox.setEditable(false);
        brandComboBox.setPrefWidth(200);
        
        try {
            List<Brand> brands = database.getAllBrands();
            ObservableList<String> brandNames = FXCollections.observableArrayList();
            for (Brand brand : brands) {
                brandNames.add(brand.nameProperty().get());
            }
            brandComboBox.setItems(brandNames);
            brandComboBox.setValue(record.getBrand());
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load brands: " + e.getMessage());
        }
        
        // Unit (ComboBox)
        Label unitLabel = new Label("Unit:");
        ComboBox<String> unitComboBox = new ComboBox<>();
        unitComboBox.setPromptText("-- Select Unit --");
        unitComboBox.setEditable(false);
        unitComboBox.setPrefWidth(200);
        
        try {
            List<String> units = database.getAllUnits();
            unitComboBox.setItems(FXCollections.observableArrayList(units));
            unitComboBox.setValue(record.getUnit());
        } catch (Exception e) {
            showAlert("Database Error", "Failed to load units: " + e.getMessage());
        }
        
        // Quantity
        Label quantityLabel = new Label("Quantity:");
        TextField quantityField = new TextField(String.valueOf(record.getQuantity()));
        
        // Unit Cost
        Label unitCostLabel = new Label("Unit Cost:");
        TextField unitCostField = new TextField(formatNumber(record.getUnitCost()));
        
        // Sale Price
        Label salePriceLabel = new Label("Sale Price:");
        TextField salePriceField = new TextField(formatNumber(record.getSalePrice()));
        
        // Add form fields to grid
        formGrid.add(idLabel, 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(nameLabel, 0, 1);
        formGrid.add(nameField, 1, 1);
        formGrid.add(brandLabel, 0, 2);
        formGrid.add(brandComboBox, 1, 2);
        formGrid.add(unitLabel, 0, 3);
        formGrid.add(unitComboBox, 1, 3);
        formGrid.add(quantityLabel, 0, 4);
        formGrid.add(quantityField, 1, 4);
        formGrid.add(unitCostLabel, 0, 5);
        formGrid.add(unitCostField, 1, 5);
        formGrid.add(salePriceLabel, 0, 6);
        formGrid.add(salePriceField, 1, 6);
        
        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        updateButton.setPrefWidth(120);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        cancelButton.setPrefWidth(120);
        
        buttonBox.getChildren().addAll(updateButton, cancelButton);
        
        // Add all components to layout
        layout.getChildren().addAll(titleLabel, formGrid, buttonBox);
        
        // Create scene and show dialog
        Scene scene = new Scene(layout);
        // Add your application CSS if needed
        // scene.getStylesheets().add(getClass().getResource("path/to/styles.css").toExternalForm());
        dialog.setScene(scene);
        
        // Button actions
        updateButton.setOnAction(e -> {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                String brand = brandComboBox.getValue();
                String unit = unitComboBox.getValue();
                String quantityText = quantityField.getText().trim();
                String unitCostText = unitCostField.getText().trim();
                String salePriceText = salePriceField.getText().trim();
                
                // Perform validations
                if (name.isEmpty()) {
                    showAlert("Missing Information", "Please enter a product name.");
                    nameField.requestFocus();
                    return;
                }
                
                if (brand == null || brand.isEmpty()) {
                    showAlert("Missing Information", "Please select a brand.");
                    brandComboBox.requestFocus();
                    return;
                }
                
                if (unit == null || unit.isEmpty()) {
                    showAlert("Missing Information", "Please select a unit.");
                    unitComboBox.requestFocus();
                    return;
                }
                
                if (quantityText.isEmpty()) {
                    showAlert("Missing Information", "Please enter quantity.");
                    quantityField.requestFocus();
                    return;
                }
                
                if (unitCostText.isEmpty()) {
                    showAlert("Missing Information", "Please enter unit cost.");
                    unitCostField.requestFocus();
                    return;
                }
                
                if (salePriceText.isEmpty()) {
                    showAlert("Missing Information", "Please enter sale price.");
                    salePriceField.requestFocus();
                    return;
                }
                
                // Parse numeric values
                int quantity;
                double unitCost, salePrice;
                
                try {
                    quantity = Integer.parseInt(quantityText);
                    if (quantity <= 0) {
                        showAlert("Invalid Input", "Quantity must be greater than 0.");
                        quantityField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Quantity must be a valid number.");
                    quantityField.requestFocus();
                    return;
                }
                
                try {
                    unitCost = Double.parseDouble(unitCostText);
                    if (unitCost <= 0) {
                        showAlert("Invalid Input", "Unit cost must be greater than 0.");
                        unitCostField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Unit cost must be a valid number.");
                    unitCostField.requestFocus();
                    return;
                }
                
                try {
                    salePrice = Double.parseDouble(salePriceText);
                    if (salePrice <= 0) {
                        showAlert("Invalid Input", "Sale price must be greater than 0.");
                        salePriceField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Sale price must be a valid number.");
                    salePriceField.requestFocus();
                    return;
                }
                
                if (salePrice <= unitCost) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Price Warning");
                    alert.setHeaderText("Sale price is less than or equal to unit cost");
                    alert.setContentText(String.format("Sale Price: %.2f\nUnit Cost: %.2f\n\nThis will result in no profit or a loss. Do you want to continue?", salePrice, unitCost));
                    
                    if (alert.showAndWait().get() != ButtonType.OK) {
                        salePriceField.requestFocus();
                        return;
                    }
                }
                
                // Get production ID
                int productionId = Integer.parseInt(idField.getText());
                
                // Update the production stock in the database
                boolean success = updateProductionStock(productionId, name, brand, unit, quantity, unitCost, salePrice);
                
                if (success) {
                    showAlert("Success", "Production stock updated successfully!");
                    dialog.close();
                    
                    // Refresh the table view
                    refreshProductionStockTable(tableView);
                } else {
                    showAlert("Error", "Failed to update production stock. Please try again.");
                }
                
            } catch (Exception ex) {
                showAlert("Error", "An error occurred while updating: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        dialog.showAndWait();
    }
    
    /**
     * Updates a production stock item in the database
     * 
     * @param productionId The ID of the production stock to update
     * @param name Product name
     * @param brand Brand name
     * @param unit Unit name
     * @param quantity Quantity
     * @param unitCost Unit cost
     * @param salePrice Sale price
     * @return true if update was successful
     */
    private static boolean updateProductionStock(int productionId, String name, String brand, String unit, int quantity, double unitCost, double salePrice) {
        try {
            // Create SQL update statement
            String updateQuery = "UPDATE ProductionStock SET product_name = ?, brand_id = (SELECT brand_id FROM Brand WHERE brand_name = ?), "
                + "unit_id = (SELECT unit_id FROM Unit WHERE unit_name = ?), quantity = ?, unit_cost = ?, sale_price = ? "
                + "WHERE production_id = ?";
            
            // Execute the update using the database connection
            java.sql.Connection conn = database.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, name);
            pstmt.setString(2, brand);
            pstmt.setString(3, unit);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, unitCost);
            pstmt.setDouble(6, salePrice);
            pstmt.setInt(7, productionId);
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update production stock: " + e.getMessage());
            return false;
        }
    }
}
