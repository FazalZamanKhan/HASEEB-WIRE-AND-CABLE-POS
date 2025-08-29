package com.cablemanagement.views.pages;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.cablemanagement.config;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class AccountsContent {

    // Data model for table display
    public static class CustomerAccountData {
        private final String customerName;
        private final String contact;
        private final String balance;
        private final String tehsil;
        private final String district;
        private final String province;

        public CustomerAccountData(String customerName, String contact, String balance, 
                                 String tehsil, String district, String province) {
            this.customerName = customerName;
            this.contact = contact;
            this.balance = balance;
            this.tehsil = tehsil;
            this.district = district;
            this.province = province;
        }

        public String getCustomerName() { return customerName; }
        public String getContact() { return contact; }
        public String getBalance() { return balance; }
        public String getTehsil() { return tehsil; }
        public String getDistrict() { return district; }
        public String getProvince() { return province; }
    }

    public static class SupplierAccountData {
        private final String supplierName;
        private final String contact;
        private final String balance;
        private final String tehsil;
        private final String district;
        private final String province;

        public SupplierAccountData(String supplierName, String contact, String balance, 
                                 String tehsil, String district, String province) {
            this.supplierName = supplierName;
            this.contact = contact;
            this.balance = balance;
            this.tehsil = tehsil;
            this.district = district;
            this.province = province;
        }

        public String getSupplierName() { return supplierName; }
        public String getContact() { return contact; }
        public String getBalance() { return balance; }
        public String getTehsil() { return tehsil; }
        public String getDistrict() { return district; }
        public String getProvince() { return province; }
    }

    // Methods to get data with location details
    private static List<CustomerAccountData> getAllCustomersWithLocation() {
        List<CustomerAccountData> customers = new ArrayList<>();
        String query = "SELECT c.customer_name, c.contact_number, c.balance, " +
                      "COALESCE(t.tehsil_name, '') as tehsil_name, " +
                      "COALESCE(d.district_name, '') as district_name, " +
                      "COALESCE(p.province_name, '') as province_name " +
                      "FROM Customer c " +
                      "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                      "LEFT JOIN District d ON t.district_id = d.district_id " +
                      "LEFT JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY c.customer_name";
        
        try {
            Connection conn = config.database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("customer_name");
                String contact = rs.getString("contact_number");
                double balance = config.database.getCustomerCurrentBalance(name);
                String tehsil = rs.getString("tehsil_name");
                String district = rs.getString("district_name");
                String province = rs.getString("province_name");
                
                customers.add(new CustomerAccountData(name, contact, 
                    String.format("%.2f", balance), tehsil, district, province));
            }
            
            rs.close();
            stmt.close();
            // Don't close the connection as it's managed by the database class
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    private static List<SupplierAccountData> getAllSuppliersWithLocation() {
        List<SupplierAccountData> suppliers = new ArrayList<>();
        String query = "SELECT s.supplier_name, s.contact_number, s.balance, " +
                      "COALESCE(t.tehsil_name, '') as tehsil_name, " +
                      "COALESCE(d.district_name, '') as district_name, " +
                      "COALESCE(p.province_name, '') as province_name " +
                      "FROM Supplier s " +
                      "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                      "LEFT JOIN District d ON t.district_id = d.district_id " +
                      "LEFT JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY s.supplier_name";
        
        try {
            Connection conn = config.database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("supplier_name");
                String contact = rs.getString("contact_number");
                double balance = config.database.getSupplierCurrentBalance(name);
                String tehsil = rs.getString("tehsil_name");
                String district = rs.getString("district_name");
                String province = rs.getString("province_name");
                
                suppliers.add(new SupplierAccountData(name, contact, 
                    String.format("%.2f", balance), tehsil, district, province));
            }
            
            rs.close();
            stmt.close();
            // Don't close the connection as it's managed by the database class
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Title Section
        VBox titleSection = new VBox(10);
        titleSection.setPadding(new Insets(0, 0, 20, 0));
        titleSection.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("💰 Accounts Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label descriptionLabel = new Label("Manage customer and supplier account information");
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        descriptionLabel.setStyle("-fx-text-fill: #7f8c8d;");

        titleSection.getChildren().addAll(titleLabel, descriptionLabel);

        // Create TabPane
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(500);

        // Customer Account Tab
        Tab customerTab = new Tab("Customer Accounts");
        customerTab.setClosable(false);
        customerTab.setContent(createCustomerAccountsContent());

        // Supplier Account Tab
        Tab supplierTab = new Tab("Supplier Accounts");
        supplierTab.setClosable(false);
        supplierTab.setContent(createSupplierAccountsContent());

        tabPane.getTabs().addAll(customerTab, supplierTab);

        mainLayout.setTop(titleSection);
        mainLayout.setCenter(tabPane);

        return mainLayout;
    }

    private static VBox createCustomerAccountsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label heading = new Label("Customer Account Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        heading.setStyle("-fx-text-fill: #2c3e50;");

        // Search Row (removed filter and refresh)
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setPrefWidth(300);

        searchRow.getChildren().addAll(new Label("Search:"), searchField);

        // Customer Table with real data
        TableView<CustomerAccountData> customerTable = new TableView<>();
        customerTable.setPrefHeight(350);
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<CustomerAccountData, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));
        
        TableColumn<CustomerAccountData, String> contactCol = new TableColumn<>("Contact");
        contactCol.setPrefWidth(150);
        contactCol.setMinWidth(120);
        contactCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        
        TableColumn<CustomerAccountData, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setPrefWidth(130);
        balanceCol.setMinWidth(100);
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBalance()));
        
        TableColumn<CustomerAccountData, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setPrefWidth(130);
        tehsilCol.setMinWidth(100);
        tehsilCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTehsil()));
        
        TableColumn<CustomerAccountData, String> districtCol = new TableColumn<>("District");
        districtCol.setPrefWidth(130);
        districtCol.setMinWidth(100);
        districtCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDistrict()));
        
        TableColumn<CustomerAccountData, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setPrefWidth(130);
        provinceCol.setMinWidth(100);
        provinceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProvince()));

        customerTable.getColumns().addAll(nameCol, contactCol, balanceCol, tehsilCol, districtCol, provinceCol);

        // Load real data from database
        ObservableList<CustomerAccountData> customerData = FXCollections.observableArrayList();
        try {
            customerData.addAll(getAllCustomersWithLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
        customerTable.setItems(customerData);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                customerTable.setItems(customerData);
            } else {
                ObservableList<CustomerAccountData> filteredList = FXCollections.observableArrayList();
                for (CustomerAccountData customer : customerData) {
                    if (customer.getCustomerName().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getContact().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getTehsil().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getDistrict().toLowerCase().contains(newValue.toLowerCase()) ||
                        customer.getProvince().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(customer);
                    }
                }
                customerTable.setItems(filteredList);
            }
        });

        // Action buttons below the table
        HBox actionButtonsRow = new HBox(15);
        actionButtonsRow.setAlignment(Pos.CENTER_LEFT);
        actionButtonsRow.setPadding(new Insets(15, 0, 0, 0));

        Button updateBtn = new Button("Update Customer");
        updateBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        updateBtn.setPrefWidth(130);
        
        Button ledgerBtn = new Button("View Ledger");
        ledgerBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        ledgerBtn.setPrefWidth(130);
        
        Button paymentBtn = new Button("Add Payment");
        paymentBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        paymentBtn.setPrefWidth(130);

        // Initially disable buttons until a customer is selected
        updateBtn.setDisable(true);
        ledgerBtn.setDisable(true);
        paymentBtn.setDisable(true);

        // Enable/disable buttons based on selection
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateBtn.setDisable(!hasSelection);
            ledgerBtn.setDisable(!hasSelection);
            paymentBtn.setDisable(!hasSelection);
        });

        // Button actions
        updateBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                showUpdateCustomerDialog(selectedCustomer, customerData, customerTable);
            }
        });
        
        ledgerBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                showCustomerLedgerDialog(selectedCustomer.getCustomerName());
            }
        });
        
        paymentBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                showAddPaymentDialog(selectedCustomer.getCustomerName(), customerData, customerTable);
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, ledgerBtn, paymentBtn);

        // Placeholder data message when no customers exist
        if (customerData.isEmpty()) {
            Label noDataLabel = new Label("No customer data available. Add customers in the Register section.");
            noDataLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            customerTable.setPlaceholder(noDataLabel);
        }

        content.getChildren().addAll(heading, searchRow, customerTable, actionButtonsRow);
        return content;
    }

    private static VBox createSupplierAccountsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label heading = new Label("Supplier Account Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        heading.setStyle("-fx-text-fill: #2c3e50;");

        // Search Row (removed filter and refresh)
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search suppliers...");
        searchField.setPrefWidth(300);

        searchRow.getChildren().addAll(new Label("Search:"), searchField);

        // Supplier Table with real data
        TableView<SupplierAccountData> supplierTable = new TableView<>();
        supplierTable.setPrefHeight(350);
        supplierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        supplierTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<SupplierAccountData, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplierName()));
        
        TableColumn<SupplierAccountData, String> contactCol = new TableColumn<>("Contact");
        contactCol.setPrefWidth(150);
        contactCol.setMinWidth(120);
        contactCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        
        TableColumn<SupplierAccountData, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setPrefWidth(130);
        balanceCol.setMinWidth(100);
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBalance()));
        
        TableColumn<SupplierAccountData, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setPrefWidth(130);
        tehsilCol.setMinWidth(100);
        tehsilCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTehsil()));
        
        TableColumn<SupplierAccountData, String> districtCol = new TableColumn<>("District");
        districtCol.setPrefWidth(130);
        districtCol.setMinWidth(100);
        districtCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDistrict()));
        
        TableColumn<SupplierAccountData, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setPrefWidth(130);
        provinceCol.setMinWidth(100);
        provinceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProvince()));

        supplierTable.getColumns().addAll(nameCol, contactCol, balanceCol, tehsilCol, districtCol, provinceCol);

        // Load real data from database
        ObservableList<SupplierAccountData> supplierData = FXCollections.observableArrayList();
        try {
            supplierData.addAll(getAllSuppliersWithLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
        supplierTable.setItems(supplierData);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                supplierTable.setItems(supplierData);
            } else {
                ObservableList<SupplierAccountData> filteredList = FXCollections.observableArrayList();
                for (SupplierAccountData supplier : supplierData) {
                    if (supplier.getSupplierName().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getContact().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getTehsil().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getDistrict().toLowerCase().contains(newValue.toLowerCase()) ||
                        supplier.getProvince().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(supplier);
                    }
                }
                supplierTable.setItems(filteredList);
            }
        });

        // Action buttons below the table
        HBox actionButtonsRow = new HBox(15);
        actionButtonsRow.setAlignment(Pos.CENTER_LEFT);
        actionButtonsRow.setPadding(new Insets(15, 0, 0, 0));

        Button updateBtn = new Button("Update Supplier");
        updateBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        updateBtn.setPrefWidth(130);
        
        Button ledgerBtn = new Button("View Ledger");
        ledgerBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        ledgerBtn.setPrefWidth(130);
        
        Button paymentBtn = new Button("Add Payment");
        paymentBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        paymentBtn.setPrefWidth(130);

        // Initially disable buttons until a supplier is selected
        updateBtn.setDisable(true);
        ledgerBtn.setDisable(true);
        paymentBtn.setDisable(true);

        // Enable/disable buttons based on selection
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateBtn.setDisable(!hasSelection);
            ledgerBtn.setDisable(!hasSelection);
            paymentBtn.setDisable(!hasSelection);
        });

        // Button actions
        updateBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                showUpdateSupplierDialog(selectedSupplier, supplierData, supplierTable);
            }
        });
        
        ledgerBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                showSupplierLedgerDialog(selectedSupplier.getSupplierName());
            }
        });
        
        paymentBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                showAddSupplierPaymentDialog(selectedSupplier.getSupplierName(), supplierData, supplierTable);
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, ledgerBtn, paymentBtn);

        // Placeholder data message when no suppliers exist
        if (supplierData.isEmpty()) {
            Label noDataLabel = new Label("No supplier data available. Add suppliers in the Register section.");
            noDataLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            supplierTable.setPlaceholder(noDataLabel);
        }

        content.getChildren().addAll(heading, searchRow, supplierTable, actionButtonsRow);
        return content;
    }

    // Dialog methods for customer operations
    private static void showUpdateCustomerDialog(CustomerAccountData selectedCustomer, 
                                               ObservableList<CustomerAccountData> customerData, 
                                               TableView<CustomerAccountData> customerTable) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Customer");
        dialog.setHeaderText("Update customer information");
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(selectedCustomer.getCustomerName());
        TextField contactField = new TextField(selectedCustomer.getContact());
        
        // Get all tehsils for dropdown
        ComboBox<String> tehsilCombo = new ComboBox<>();
        try {
            List<String> tehsils = config.database.getAllTehsils();
            tehsilCombo.setItems(FXCollections.observableArrayList(tehsils));
            tehsilCombo.setValue(selectedCustomer.getTehsil());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        grid.add(new Label("Customer Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Tehsil:"), 0, 2);
        grid.add(tehsilCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int customerId = config.database.getCustomerIdByName(selectedCustomer.getCustomerName());
                boolean success = config.database.updateCustomer(customerId, nameField.getText(), 
                                                                contactField.getText(), tehsilCombo.getValue());
                if (success) {
                    // Refresh the table data
                    customerData.clear();
                    customerData.addAll(getAllCustomersWithLocation());
                    customerTable.refresh();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Customer updated successfully!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to update customer. Please try again.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private static void showCustomerLedgerDialog(String customerName) {
    Stage ledgerStage = new Stage();
    ledgerStage.setTitle("Customer Ledger - " + customerName);
    ledgerStage.initModality(Modality.NONE); // Allow minimization
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Ledger for: " + customerName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        
        // Date range selection
        HBox dateRangeBox = new HBox(10);
        DatePicker fromDatePicker = new DatePicker();
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = new Button("Filter");
        Button showAllBtn = new Button("Show All");
        Button printBtn = new Button("Print Ledger");
        printBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 15;");
        dateRangeBox.getChildren().addAll(
            new Label("From:"), fromDatePicker,
            new Label("To:"), toDatePicker,
            filterBtn, showAllBtn, printBtn
        );
    dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        
        // Ledger table
        TableView<Object[]> ledgerTable = new TableView<>();
        ledgerTable.setPrefHeight(400);
        ledgerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ledgerTable.getStyleClass().add("table-view");
        
        TableColumn<Object[], String> serialCol = new TableColumn<>("S.No");
        serialCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf((Integer) cellData.getValue()[0])));
        serialCol.setPrefWidth(50);
        
        TableColumn<Object[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
        dateCol.setPrefWidth(100);
        
        TableColumn<Object[], String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[2]));
        timeCol.setPrefWidth(80);
        
        TableColumn<Object[], String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]));
        descCol.setPrefWidth(200);
        
        // Show full invoice item details inline in the Description column (customer ledger)
        descCol.setCellValueFactory(cellData -> {
            String invoiceNumber = (String) cellData.getValue()[4];
            if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                try {
                    StringBuilder detailedDesc = new StringBuilder();
                    String itemQuery = "SELECT si.quantity, si.unit_price, si.discount_amount, " +
                        "(si.quantity * si.unit_price) as total_price, " +
                        "(si.quantity * si.unit_price - si.discount_amount) as net_price, " +
                        "sv.paid_amount, sv.total_amount, sv.discount_amount as invoice_discount, " +
                        "COALESCE(ps.product_name, 'Product') as item_desc " +
                        "FROM Sales_Invoice_Item si " +
                        "LEFT JOIN ProductionStock ps ON si.production_stock_id = ps.production_id " +
                        "LEFT JOIN Sales_Invoice sv ON si.sales_invoice_id = sv.sales_invoice_id " +
                        "WHERE si.sales_invoice_id = (SELECT sales_invoice_id FROM Sales_Invoice WHERE sales_invoice_number = ?)";
                    Connection conn = config.database.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(itemQuery);
                    stmt.setString(1, invoiceNumber);
                    ResultSet rs = stmt.executeQuery();
                    double invoicePaidAmount = 0.0;
                    double invoiceTotalAmount = 0.0;
                    double invoiceDiscountAmount = 0.0;
                    while (rs.next()) {
                        if (invoicePaidAmount == 0.0) {
                            invoicePaidAmount = rs.getDouble("paid_amount");
                            invoiceTotalAmount = rs.getDouble("total_amount");
                            invoiceDiscountAmount = rs.getDouble("invoice_discount");
                        }
                        double quantity = rs.getDouble("quantity");
                        double unitPrice = rs.getDouble("unit_price");
                        double totalPrice = rs.getDouble("total_price");
                        double discountAmount = rs.getDouble("discount_amount");
                        double netPrice = rs.getDouble("net_price");
                        detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total Price: %.2f | Discount: %.2f | Net: %.2f | Paid: %.2f | Balance: %.2f\n",
                            rs.getString("item_desc"),
                            quantity,
                            unitPrice,
                            totalPrice,
                            discountAmount,
                            netPrice,
                            invoicePaidAmount,
                            (invoiceTotalAmount - invoiceDiscountAmount - invoicePaidAmount)));
                    }
                    rs.close();
                    stmt.close();
                    return new javafx.beans.property.SimpleStringProperty(detailedDesc.toString());
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("Error loading invoice details: " + e.getMessage());
                }
            }
            return new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]);
        });
        descCol.setPrefWidth(400);

        TableColumn<Object[], String> invoiceCol = new TableColumn<>("Invoice Number");
        invoiceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[4]));
        invoiceCol.setPrefWidth(120);
        
        TableColumn<Object[], String> totalBillCol = new TableColumn<>("Total Bill");
        totalBillCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[5])));
        totalBillCol.setPrefWidth(100);
        totalBillCol.setStyle("-fx-text-fill: #6c757d;"); // Gray for total bill
        
        TableColumn<Object[], String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[6])));
        discountCol.setPrefWidth(100);
        discountCol.setStyle("-fx-text-fill: #fd7e14;"); // Orange for discount
        
        TableColumn<Object[], String> amountCol = new TableColumn<>("Net Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-text-fill: #28a745;"); // Green for amount
        
        TableColumn<Object[], String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[8])));
        paymentCol.setPrefWidth(100);
        paymentCol.setStyle("-fx-text-fill: #28a745;"); // Green for payment
        
        TableColumn<Object[], String> returnCol = new TableColumn<>("Return Amount");
        returnCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[9])));
        returnCol.setPrefWidth(100);
        returnCol.setStyle("-fx-text-fill: #dc3545;"); // Red for return amount
        
        TableColumn<Object[], String> balanceCol = new TableColumn<>("Remaining/Balance");
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[10])));
        balanceCol.setPrefWidth(120);
        balanceCol.setStyle("-fx-text-fill: #dc3545;"); // Red for remaining balance
        
    ledgerTable.getColumns().addAll(serialCol, dateCol, timeCol, invoiceCol, totalBillCol, discountCol, amountCol, paymentCol, returnCol, balanceCol, descCol);
        
        // Create summary labels
        Label totalSaleLabel = new Label("Total Sale: 0.00");
        totalSaleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalSaleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label totalPaymentLabel = new Label("Total Payment: 0.00");
        totalPaymentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalPaymentLabel.setStyle("-fx-text-fill: #27ae60;");
        
        Label totalReturnLabel = new Label("Total Return: 0.00");
        totalReturnLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalReturnLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Label currentBalanceLabel = new Label("Current Balance: 0.00");
        currentBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentBalanceLabel.setStyle("-fx-text-fill: #3498db;");
        
        // Summary box
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1px;");
        summaryBox.getChildren().addAll(totalSaleLabel, totalPaymentLabel, totalReturnLabel, currentBalanceLabel);
        
        // Load ledger data
        ObservableList<Object[]> ledgerData = FXCollections.observableArrayList();
        
        // Method to update totals
        Runnable updateTotals = () -> {
            double totalSale = 0.0;
            double totalPayment = 0.0;
            double totalReturn = 0.0;
            double currentBalance = 0.0;
            
            for (Object[] row : ledgerData) {
                if (row.length >= 11) {
                    totalSale += (Double) row[7];      // Net Amount column (invoices)
                    totalPayment += (Double) row[8];   // Payment column
                    totalReturn += (Double) row[9];    // Return Amount column
                    currentBalance = (Double) row[10]; // Last balance (current balance)
                }
            }
            
            totalSaleLabel.setText(String.format("Total Sale: %.2f", totalSale));
            totalPaymentLabel.setText(String.format("Total Payment: %.2f", totalPayment));
            totalReturnLabel.setText(String.format("Total Return: %.2f", totalReturn));
            currentBalanceLabel.setText(String.format("Current Balance: %.2f", currentBalance));
            
            // Update balance color based on value
            if (currentBalance > 0) {
                currentBalanceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red for debt
            } else if (currentBalance < 0) {
                currentBalanceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green for credit
            } else {
                currentBalanceLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); // Blue for zero
            }
        };
        
        Runnable loadLedgerData = () -> {
            try {
                ledgerData.clear();
                List<Object[]> transactions = config.database.getCustomerLedger(customerName);
                ledgerData.addAll(transactions);
                ledgerTable.setItems(ledgerData);
                updateTotals.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        Runnable filterLedgerData = () -> {
            try {
                ledgerData.clear();
                if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String startDate = fromDatePicker.getValue().format(formatter);
                    String endDate = toDatePicker.getValue().format(formatter);
                    List<Object[]> transactions = config.database.getCustomerLedgerByDateRange(customerName, startDate, endDate);
                    ledgerData.addAll(transactions);
                } else {
                    List<Object[]> transactions = config.database.getCustomerLedger(customerName);
                    ledgerData.addAll(transactions);
                }
                ledgerTable.setItems(ledgerData);
                updateTotals.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        // Button actions
        filterBtn.setOnAction(e -> filterLedgerData.run());
        showAllBtn.setOnAction(e -> loadLedgerData.run());
        printBtn.setOnAction(e -> {
            try {
                // Create print preview for customer ledger in tabular format
                StringBuilder printContent = new StringBuilder();
                printContent.append("CUSTOMER LEDGER REPORT\n");
                printContent.append("Customer: ").append(customerName).append("\n");
                printContent.append("Generated on: ").append(LocalDate.now()).append("\n");
                printContent.append("=".repeat(140) + "\n\n");
                
                // Add column headers with fixed widths
                printContent.append(String.format("%-4s | %-12s | %-8s | %-25s | %-12s | %-10s | %-8s | %-10s | %-10s | %-10s | %-10s\n",
                    "S.No", "Date", "Time", "Description", "Invoice", "Total Bill", "Discount", "Net Amt", "Payment", "Return", "Balance"));
                printContent.append("-".repeat(140) + "\n");
                
                // Add data rows with proper formatting
                for (Object[] row : ledgerData) {
                    String description = (String) row[3];
                    if (description.length() > 25) {
                        description = description.substring(0, 22) + "...";
                    }
                    printContent.append(String.format("%-4s | %-12s | %-8s | %-25s | %-12s | %10.2f | %8.2f | %10.2f | %10.2f | %10.2f | %10.2f\n",
                        row[0], row[1], row[2], description, row[4], 
                        (Double)row[5], (Double)row[6], (Double)row[7], (Double)row[8], (Double)row[9], (Double)row[10]));
                }
                
                // Add summary section
                printContent.append("\n").append("=".repeat(140)).append("\n");
                printContent.append("SUMMARY:\n");
                printContent.append(String.format("%-30s: %15.2f\n", "Total Sale", 
                    ledgerData.stream().mapToDouble(row -> (Double)row[7]).sum()));
                printContent.append(String.format("%-30s: %15.2f\n", "Total Payment", 
                    ledgerData.stream().mapToDouble(row -> (Double)row[8]).sum()));
                printContent.append(String.format("%-30s: %15.2f\n", "Total Return", 
                    ledgerData.stream().mapToDouble(row -> (Double)row[9]).sum()));
                
                if (!ledgerData.isEmpty()) {
                    Object[] lastRow = ledgerData.get(ledgerData.size() - 1);
                    printContent.append(String.format("%-30s: %15.2f\n", "Current Balance", (Double)lastRow[10]));
                }
                
                // Create proper tabular print preview
                TableView<Object[]> printTable = new TableView<>();
                printTable.setItems(FXCollections.observableArrayList(ledgerData));
                
                // Configure table columns for print
                TableColumn<Object[], String> printSerialCol = new TableColumn<>("S.No");
                printSerialCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.valueOf((Integer) cellData.getValue()[0])));
                printSerialCol.setPrefWidth(60);
                
                TableColumn<Object[], String> printDateCol = new TableColumn<>("Date");
                printDateCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
                printDateCol.setPrefWidth(100);
                
                TableColumn<Object[], String> printTimeCol = new TableColumn<>("Time");
                printTimeCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[2]));
                printTimeCol.setPrefWidth(80);
                
                TableColumn<Object[], String> printDescCol = new TableColumn<>("Description");
                printDescCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]));
                printDescCol.setPrefWidth(200);
                
                TableColumn<Object[], String> printInvoiceCol = new TableColumn<>("Invoice Number");
                printInvoiceCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[4]));
                printInvoiceCol.setPrefWidth(120);
                
                TableColumn<Object[], String> printTotalBillCol = new TableColumn<>("Total Bill");
                printTotalBillCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[5])));
                printTotalBillCol.setPrefWidth(100);
                
                TableColumn<Object[], String> printDiscountCol = new TableColumn<>("Discount");
                printDiscountCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[6])));
                printDiscountCol.setPrefWidth(100);
                
                TableColumn<Object[], String> printAmountCol = new TableColumn<>("Net Amount");
                printAmountCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
                printAmountCol.setPrefWidth(100);
                
                TableColumn<Object[], String> printPaymentCol = new TableColumn<>("Payment");
                printPaymentCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[8])));
                printPaymentCol.setPrefWidth(100);
                
                TableColumn<Object[], String> printReturnCol = new TableColumn<>("Return Amount");
                printReturnCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[9])));
                printReturnCol.setPrefWidth(100);
                
                TableColumn<Object[], String> printBalanceCol = new TableColumn<>("Remaining/Balance");
                printBalanceCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[10])));
                printBalanceCol.setPrefWidth(120);
                
                printTable.getColumns().addAll(printSerialCol, printDateCol, printTimeCol, printDescCol, 
                                               printInvoiceCol, printTotalBillCol, printDiscountCol, printAmountCol, printPaymentCol, printReturnCol, printBalanceCol);
                
                // Set table style for print
                printTable.setStyle("-fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
                printTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                
                // Create header section
                VBox headerBox = new VBox(5);
                headerBox.setPadding(new Insets(20, 20, 10, 20));
                headerBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 2 0;");
                
                Label headerTitleLabel = new Label("CUSTOMER LEDGER REPORT");
                headerTitleLabel.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 20));
                headerTitleLabel.setStyle("-fx-text-fill: #212529;");
                
                Label customerLabel = new Label("Customer: " + customerName);
                customerLabel.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.SEMI_BOLD, 16));
                customerLabel.setStyle("-fx-text-fill: #495057;");
                
                Label dateLabel = new Label("Generated on: " + java.time.LocalDate.now());
                dateLabel.setFont(javafx.scene.text.Font.font("Segoe UI", 14));
                dateLabel.setStyle("-fx-text-fill: #6c757d;");
                
                headerBox.getChildren().addAll(headerTitleLabel, customerLabel, dateLabel);
                
                // Create summary section
                HBox printSummaryBox = new HBox(30);
                printSummaryBox.setPadding(new Insets(15, 20, 15, 20));
                printSummaryBox.setAlignment(Pos.CENTER);
                printSummaryBox.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #adb5bd; -fx-border-width: 1 0;");
                
                double totalSale = ledgerData.stream().mapToDouble(row -> (Double)row[7]).sum();       // Net Amount column  
                double totalPayment = ledgerData.stream().mapToDouble(row -> (Double)row[8]).sum();  // Payment column
                double totalReturn = ledgerData.stream().mapToDouble(row -> (Double)row[9]).sum();   // Return Amount column
                double currentBalance = !ledgerData.isEmpty() ? (Double)ledgerData.get(ledgerData.size() - 1)[10] : 0.0; // Last balance (current balance)
                
                Label summarySale = new Label(String.format("Total Sale: %.2f", totalSale));
                summarySale.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 14));
                summarySale.setStyle("-fx-text-fill: #28a745;");
                
                Label summaryPayment = new Label(String.format("Total Payment: %.2f", totalPayment));
                summaryPayment.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 14));
                summaryPayment.setStyle("-fx-text-fill: #007bff;");
                
                Label summaryReturn = new Label(String.format("Total Return: %.2f", totalReturn));
                summaryReturn.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 14));
                summaryReturn.setStyle("-fx-text-fill: #fd7e14;");
                
                Label summaryBalance = new Label(String.format("Current Balance: %.2f", currentBalance));
                summaryBalance.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 14));
                summaryBalance.setStyle(currentBalance > 0 ? "-fx-text-fill: #28a745;" : 
                                       currentBalance < 0 ? "-fx-text-fill: #dc3545;" : "-fx-text-fill: #6c757d;");
                
                printSummaryBox.getChildren().addAll(summarySale, summaryPayment, summaryReturn, summaryBalance);
                
                // Print and Close buttons
                Button printButton = new Button("Print");
                printButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; " +
                                    "-fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;");
                
                Button printCloseBtn = new Button("Close");
                printCloseBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; " +
                                      "-fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;");
                
                HBox printButtonBox = new HBox(15, printButton, printCloseBtn);
                printButtonBox.setPadding(new Insets(15));
                printButtonBox.setAlignment(Pos.CENTER);
                
                // Main layout for print preview
                VBox printMainLayout = new VBox();
                printMainLayout.getChildren().addAll(headerBox, printTable, printSummaryBox, printButtonBox);
                VBox.setVgrow(printTable, Priority.ALWAYS);
                
                Stage printStage = new Stage();
                printStage.setTitle("Print Preview - Customer Ledger: " + customerName);
                printStage.setMaximized(true);
                
                Scene printScene = new Scene(printMainLayout);
                try {
                    String css = AccountsContent.class.getResource("/com/cablemanagement/global.css").toExternalForm();
                    printScene.getStylesheets().add(css);
                } catch (Exception cssEx) {
                    // CSS file not found, continue without styling
                }
                printStage.setScene(printScene);
                
                printCloseBtn.setOnAction(ev -> printStage.close());
                
                // Print button functionality
                printButton.setOnAction(ev -> {
                    try {
                        // Generate timestamp for unique filename
                        String timestamp = java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        
                        // Create temporary filename
                        String tempDir = System.getProperty("java.io.tmpdir");
                        String filename = tempDir + java.io.File.separator + "Customer_Ledger_" + 
                                        customerName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";
                        
                        // Recalculate summary values for PDF generation
                        double pdfTotalSale = 0.0;
                        double pdfTotalPayment = 0.0;
                        double pdfTotalReturn = 0.0;
                        double pdfCurrentBalance = 0.0;
                        
                        for (Object[] row : ledgerData) {
                            if (row.length >= 11) {
                                pdfTotalSale += (Double) row[7];      // Net Amount column
                                pdfTotalPayment += (Double) row[8];   // Payment column
                                pdfTotalReturn += (Double) row[9];    // Return Amount column
                                pdfCurrentBalance = (Double) row[10]; // Last balance (current balance)
                            }
                        }
                        
                        // Generate PDF
                        com.cablemanagement.invoice.LedgerPDFGenerator.generateCustomerLedgerPDF(
                            customerName, ledgerData, pdfTotalSale, pdfTotalPayment, pdfTotalReturn, pdfCurrentBalance, filename);
                        
                        // Show success message and open PDF
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("PDF Generated");
                        alert.setHeaderText(null);
                        alert.setContentText("Customer Ledger PDF generated successfully!");
                        alert.showAndWait();
                        
                        // Open the PDF and delete the temp file after viewing
                        try {
                            java.awt.Desktop.getDesktop().open(new java.io.File(filename));
                            
                            // Schedule file deletion after a delay to allow PDF viewer to open
                            java.util.Timer timer = new java.util.Timer();
                            timer.schedule(new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        java.io.File tempFile = new java.io.File(filename);
                                        if (tempFile.exists()) {
                                            tempFile.delete();
                                        }
                                    } catch (Exception e) {
                                        // Ignore deletion errors
                                    }
                                    timer.cancel();
                                }
                            }, 10000); // Delete after 10 seconds
                            
                        } catch (Exception openEx) {
                            // PDF generated but couldn't open automatically - still delete the temp file
                            try {
                                new java.io.File(filename).delete();
                            } catch (Exception deleteEx) {
                                // Ignore deletion errors
                            }
                        }
                        
                    } catch (Exception printEx) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("PDF Generation Error");
                        alert.setHeaderText("Failed to generate ledger PDF");
                        alert.setContentText(printEx.getMessage());
                        alert.showAndWait();
                        printEx.printStackTrace();
                    }
                });
                
                printStage.show();
                
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Print Error");
                alert.setHeaderText("Failed to generate print preview");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
        
        // Initial load
        loadLedgerData.run();
        
        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> ledgerStage.close());
        
        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        mainLayout.getChildren().addAll(titleLabel, dateRangeBox, ledgerTable, summaryBox, buttonBox);
        
        Scene scene = new Scene(mainLayout, 1200, 650);
        ledgerStage.setScene(scene);
        ledgerStage.showAndWait();
    }

    private static void showAddPaymentDialog(String customerName, 
                                           ObservableList<CustomerAccountData> customerData, 
                                           TableView<CustomerAccountData> customerTable) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Customer Payment");
        dialog.setHeaderText("Add payment for: " + customerName);
        
        // Get current balance to display
        double currentBalance = config.database.getCustomerCurrentBalance(customerName);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Label currentBalanceLabel = new Label(String.format("Current Balance: %.2f", currentBalance));
        currentBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentBalanceLabel.setStyle("-fx-text-fill: " + (currentBalance > 0 ? "#e74c3c" : "#27ae60") + ";");
        
        TextField paymentAmountField = new TextField();
        paymentAmountField.setPromptText("Enter payment amount");
        
        // Add remaining balance label
        Label remainingBalanceLabel = new Label("Remaining Balance: " + String.format("%.2f", currentBalance));
        remainingBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        remainingBalanceLabel.setStyle("-fx-text-fill: #3498db;");
        
        DatePicker paymentDatePicker = new DatePicker(LocalDate.now());
        
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Payment description (optional)");
        
        grid.add(currentBalanceLabel, 0, 0, 2, 1);
        grid.add(new Label("Payment Amount:"), 0, 1);
        grid.add(paymentAmountField, 1, 1);
        grid.add(remainingBalanceLabel, 0, 2, 2, 1);
        grid.add(new Label("Payment Date:"), 0, 3);
        grid.add(paymentDatePicker, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Validate payment amount and update remaining balance
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        
        paymentAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double amount = Double.parseDouble(newValue);
                okButton.setDisable(amount <= 0);
                
                // Update remaining balance display
                double remainingBalance = currentBalance - amount;
                remainingBalanceLabel.setText("Remaining Balance: " + String.format("%.2f", remainingBalance));
                remainingBalanceLabel.setStyle("-fx-text-fill: " + (remainingBalance > 0 ? "#e74c3c" : "#27ae60") + "; -fx-font-weight: bold;");
            } catch (NumberFormatException e) {
                okButton.setDisable(true);
                remainingBalanceLabel.setText("Remaining Balance: " + String.format("%.2f", currentBalance));
                remainingBalanceLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            }
        });
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double paymentAmount = Double.parseDouble(paymentAmountField.getText());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String paymentDate = paymentDatePicker.getValue().format(formatter);
                String description = descriptionField.getText().trim();
                if (description.isEmpty()) {
                    description = "Payment received";
                }
                boolean success = config.database.addCustomerPayment(customerName, paymentAmount, paymentDate, description);
                if (success) {
                    customerData.clear();
                    customerData.addAll(getAllCustomersWithLocation());
                    customerTable.refresh();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText(String.format("Payment of %.2f added successfully!\nNew balance: %.2f", paymentAmount, currentBalance - paymentAmount));
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to add payment. Please try again.");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid payment amount.");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    // Dialog methods for supplier operations
    // Dialog methods for supplier operations
    private static void showUpdateSupplierDialog(SupplierAccountData selectedSupplier, 
                                               ObservableList<SupplierAccountData> supplierData, 
                                               TableView<SupplierAccountData> supplierTable) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Supplier");
        dialog.setHeaderText("Update supplier information");
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(selectedSupplier.getSupplierName());
        TextField contactField = new TextField(selectedSupplier.getContact());
        
        // Get all tehsils for dropdown
        ComboBox<String> tehsilCombo = new ComboBox<>();
        try {
            List<String> tehsils = config.database.getAllTehsils();
            tehsilCombo.setItems(FXCollections.observableArrayList(tehsils));
            tehsilCombo.setValue(selectedSupplier.getTehsil());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        grid.add(new Label("Supplier Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Tehsil:"), 0, 2);
        grid.add(tehsilCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int supplierId = config.database.getSupplierIdByName(selectedSupplier.getSupplierName());
                boolean success = config.database.updateSupplier(supplierId, nameField.getText(), 
                                                                contactField.getText(), tehsilCombo.getValue());
                if (success) {
                    // Refresh the table data
                    supplierData.clear();
                    supplierData.addAll(getAllSuppliersWithLocation());
                    supplierTable.refresh();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Supplier updated successfully!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to update supplier. Please try again.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    private static void showSupplierLedgerDialog(String supplierName) {
    Stage ledgerStage = new Stage();
    ledgerStage.setTitle("Supplier Ledger - " + supplierName);
    ledgerStage.initModality(Modality.NONE); // Allow minimization
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Ledger for: " + supplierName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        
        // Date range selection
        HBox dateRangeBox = new HBox(10);
        
        // Date pickers and buttons
        DatePicker fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("From Date");
        DatePicker toDatePicker = new DatePicker();
        toDatePicker.setPromptText("To Date");
        Button filterBtn = new Button("Filter");
        Button showAllBtn = new Button("Show All");
        Button printBtn = new Button("Print");
        
        // Add components to date range box
        dateRangeBox.getChildren().addAll(
            new Label("From:"), fromDatePicker,
            new Label("To:"), toDatePicker,
            filterBtn, showAllBtn, printBtn
        );
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);

        // Ledger table for supplier
        TableView<Object[]> ledgerTable = new TableView<>();
        ledgerTable.setPrefHeight(400);
        ledgerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ledgerTable.getStyleClass().add("table-view");

        TableColumn<Object[], String> serialCol = new TableColumn<>("S.No");
        serialCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf((Integer) cellData.getValue()[0])));
        serialCol.setPrefWidth(50);

        TableColumn<Object[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
        dateCol.setPrefWidth(100);

        TableColumn<Object[], String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[2]));
        timeCol.setPrefWidth(80);

        TableColumn<Object[], String> descCol = new TableColumn<>("Description");

        descCol.setCellValueFactory(cellData -> {
            String invoiceNumber = (String) cellData.getValue()[4];
            if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                try {
                    StringBuilder detailedDesc = new StringBuilder();
                    String itemQuery = "SELECT rpi.quantity, rpi.unit_price, " +
                        "(rpi.quantity * rpi.unit_price) as item_total, " +
                        "rpin.paid_amount, rpin.total_amount, rpin.discount_amount as invoice_discount, " +
                        "COALESCE(rs.item_name, 'Item') as item_desc " +
                        "FROM Raw_Purchase_Invoice_Item rpi " +
                        "LEFT JOIN Raw_Stock rs ON rpi.raw_stock_id = rs.stock_id " +
                        "LEFT JOIN Raw_Purchase_Invoice rpin ON rpi.raw_purchase_invoice_id = rpin.raw_purchase_invoice_id " +
                        "WHERE rpi.raw_purchase_invoice_id = (SELECT raw_purchase_invoice_id FROM Raw_Purchase_Invoice WHERE invoice_number = ?)";
                    Connection conn = config.database.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(itemQuery);
                    stmt.setString(1, invoiceNumber);
                    ResultSet rs = stmt.executeQuery();
                    double invoicePaidAmount = 0.0;
                    double invoiceTotalAmount = 0.0;
                    double invoiceDiscountAmount = 0.0;
                    while (rs.next()) {
                        if (invoicePaidAmount == 0.0) {
                            invoicePaidAmount = rs.getDouble("paid_amount");
                            invoiceTotalAmount = rs.getDouble("total_amount");
                            invoiceDiscountAmount = rs.getDouble("invoice_discount");
                        }
                        double itemTotal = rs.getDouble("item_total");
                        double quantity = rs.getDouble("quantity");
                        double unitPrice = rs.getDouble("unit_price");
                        double itemDiscount = (invoiceTotalAmount > 0) ? (itemTotal / invoiceTotalAmount) * invoiceDiscountAmount : 0.0;
                        double itemNet = itemTotal - itemDiscount;
                        detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total Price: %.2f | Discount: %.2f | Net: %.2f | Paid: %.2f | Balance: %.2f\n",
                            rs.getString("item_desc"),
                            quantity,
                            unitPrice,
                            itemTotal,
                            itemDiscount,
                            itemNet,
                            invoicePaidAmount,
                            (invoiceTotalAmount - invoiceDiscountAmount - invoicePaidAmount)));
                    }
                    rs.close();
                    stmt.close();
                    return new javafx.beans.property.SimpleStringProperty(detailedDesc.toString());
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("Error loading purchase details: " + e.getMessage());
                }
            }
            return new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]);
        });
        descCol.setPrefWidth(400);
        
        TableColumn<Object[], String> invoiceCol = new TableColumn<>("Invoice Number");
        invoiceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[4]));
        invoiceCol.setPrefWidth(120);
        
        TableColumn<Object[], String> totalBillCol = new TableColumn<>("Total Bill");
        totalBillCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[5])));
        totalBillCol.setPrefWidth(100);
        totalBillCol.setStyle("-fx-text-fill: #6c757d;"); // Gray for total bill
        
        TableColumn<Object[], String> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[6])));
        discountCol.setPrefWidth(100);
        discountCol.setStyle("-fx-text-fill: #fd7e14;"); // Orange for discount
        
        TableColumn<Object[], String> amountCol = new TableColumn<>("Net Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-text-fill: #28a745;"); // Green for amount
        
        TableColumn<Object[], String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[8])));
        paymentCol.setPrefWidth(100);
        paymentCol.setStyle("-fx-text-fill: #28a745;"); // Green for payment
        
        TableColumn<Object[], String> returnCol = new TableColumn<>("Return Amount");
        returnCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[9])));
        returnCol.setPrefWidth(100);
        returnCol.setStyle("-fx-text-fill: #dc3545;"); // Red for return amount
        
        TableColumn<Object[], String> balanceCol = new TableColumn<>("Remaining/Balance");
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[10])));
        balanceCol.setPrefWidth(120);
        balanceCol.setStyle("-fx-text-fill: #dc3545;"); // Red for remaining balance
        
    ledgerTable.getColumns().addAll(serialCol, dateCol, timeCol, invoiceCol, totalBillCol, discountCol, amountCol, paymentCol, returnCol, balanceCol, descCol);
        
        // Create summary labels
        Label totalPurchaseLabel = new Label("Total Purchase: 0.00");
        totalPurchaseLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalPurchaseLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label totalPaymentLabel = new Label("Total Payment: 0.00");
        totalPaymentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalPaymentLabel.setStyle("-fx-text-fill: #27ae60;");
        
        Label totalReturnLabel = new Label("Total Return: 0.00");
        totalReturnLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalReturnLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Label currentBalanceLabel = new Label("Current Balance: 0.00");
        currentBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentBalanceLabel.setStyle("-fx-text-fill: #3498db;");
        
        // Summary box
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1px;");
        summaryBox.getChildren().addAll(totalPurchaseLabel, totalPaymentLabel, totalReturnLabel, currentBalanceLabel);
        
        // Load ledger data
        ObservableList<Object[]> ledgerData = FXCollections.observableArrayList();
        
        // Method to update totals
        Runnable updateTotals = () -> {
            double totalPurchase = 0.0;
            double totalPayment = 0.0;
            double totalReturn = 0.0;
            double currentBalance = 0.0;
            
            for (Object[] row : ledgerData) {
                if (row.length >= 11) {
                    totalPurchase += (Double) row[7];    // Net Amount column (invoices)
                    totalPayment += (Double) row[8];     // Payment column
                    totalReturn += (Double) row[9];      // Return Amount column
                    currentBalance = (Double) row[10];   // Last balance (current balance)
                }
            }
            
            totalPurchaseLabel.setText(String.format("Total Purchase: %.2f", totalPurchase));
            totalPaymentLabel.setText(String.format("Total Payment: %.2f", totalPayment));
            totalReturnLabel.setText(String.format("Total Return: %.2f", totalReturn));
            currentBalanceLabel.setText(String.format("Current Balance: %.2f", currentBalance));
            
            // Update balance color based on value
            if (currentBalance > 0) {
                currentBalanceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red for debt owed to supplier
            } else if (currentBalance < 0) {
                currentBalanceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green for credit
            } else {
                currentBalanceLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); // Blue for zero
            }
        };
        
        Runnable loadLedgerData = () -> {
            try {
                ledgerData.clear();
                List<Object[]> transactions = config.database.getSupplierLedger(supplierName);
                ledgerData.addAll(transactions);
                ledgerTable.setItems(ledgerData);
                updateTotals.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        Runnable filterLedgerData = () -> {
            try {
                ledgerData.clear();
                if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String startDate = fromDatePicker.getValue().format(formatter);
                    String endDate = toDatePicker.getValue().format(formatter);
                    List<Object[]> transactions = config.database.getSupplierLedgerByDateRange(supplierName, startDate, endDate);
                    ledgerData.addAll(transactions);
                } else {
                    List<Object[]> transactions = config.database.getSupplierLedger(supplierName);
                    ledgerData.addAll(transactions);
                }
                ledgerTable.setItems(ledgerData);
                updateTotals.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        // Button actions
        filterBtn.setOnAction(e -> filterLedgerData.run());
        showAllBtn.setOnAction(e -> loadLedgerData.run());
        printBtn.setOnAction(e -> {
            try {
                // Generate timestamp for unique filename
                String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                
                // Create temporary filename
                String tempDir = System.getProperty("java.io.tmpdir");
                String filename = tempDir + java.io.File.separator + "Supplier_Ledger_" + 
                                supplierName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";
                
                // Get summary values
                double totalPurchase = 0.0;
                double totalPayment = 0.0;
                double totalReturn = 0.0;
                double currentBalance = 0.0;
                
                for (Object[] row : ledgerData) {
                    if (row.length >= 11) {
                        totalPurchase += (Double) row[7];   // Net Amount column
                        totalPayment += (Double) row[8];    // Payment column
                        totalReturn += (Double) row[9];     // Return Amount column
                        currentBalance = (Double) row[10];  // Last balance (current balance)
                    }
                }
                
                // Generate PDF
                com.cablemanagement.invoice.LedgerPDFGenerator.generateSupplierLedgerPDF(
                    supplierName, ledgerData, totalPurchase, totalPayment, totalReturn, currentBalance, filename);
                
                // Show success message and open PDF
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Generated");
                alert.setHeaderText(null);
                alert.setContentText("Supplier Ledger PDF generated successfully!");
                alert.showAndWait();
                
                // Open the PDF and delete the temp file after viewing
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(filename));
                    
                    // Schedule file deletion after a delay to allow PDF viewer to open
                    java.util.Timer timer = new java.util.Timer();
                    timer.schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                java.io.File tempFile = new java.io.File(filename);
                                if (tempFile.exists()) {
                                    tempFile.delete();
                                }
                            } catch (Exception e) {
                                // Ignore deletion errors
                            }
                            timer.cancel();
                        }
                    }, 10000); // Delete after 10 seconds
                    
                } catch (Exception openEx) {
                    // PDF generated but couldn't open automatically - still delete the temp file
                    try {
                        new java.io.File(filename).delete();
                    } catch (Exception deleteEx) {
                        // Ignore deletion errors
                    }
                }
                
            } catch (Exception ex) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("PDF Generation Error");
                alert.setHeaderText("Failed to generate ledger PDF");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                ex.printStackTrace();
            }
        });
        
        // Initial load
        loadLedgerData.run();
        
        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> ledgerStage.close());
        
        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        mainLayout.getChildren().addAll(titleLabel, dateRangeBox, ledgerTable, summaryBox, buttonBox);
        
        Scene scene = new Scene(mainLayout, 1200, 650);
        ledgerStage.setScene(scene);
        ledgerStage.showAndWait();
    }
    
    private static void showAddSupplierPaymentDialog(String supplierName, 
                                                   ObservableList<SupplierAccountData> supplierData, 
                                                   TableView<SupplierAccountData> supplierTable) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Supplier Payment");
        dialog.setHeaderText("Add payment for: " + supplierName);
        
        // Get current balance to display
        double currentBalance = config.database.getSupplierCurrentBalance(supplierName);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Label currentBalanceLabel = new Label(String.format("Current Balance: %.2f", currentBalance));
        currentBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentBalanceLabel.setStyle("-fx-text-fill: " + (currentBalance > 0 ? "#e74c3c" : "#27ae60") + ";");
        
        TextField paymentAmountField = new TextField();
        paymentAmountField.setPromptText("Enter payment amount");
        
        // Add remaining balance label
        Label remainingBalanceLabel = new Label("Remaining Balance: " + String.format("%.2f", currentBalance));
        remainingBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        remainingBalanceLabel.setStyle("-fx-text-fill: #3498db;");
        
        DatePicker paymentDatePicker = new DatePicker(LocalDate.now());
        
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Payment description (optional)");
        
        grid.add(currentBalanceLabel, 0, 0, 2, 1);
        grid.add(new Label("Payment Amount:"), 0, 1);
        grid.add(paymentAmountField, 1, 1);
        grid.add(remainingBalanceLabel, 0, 2, 2, 1);
        grid.add(new Label("Payment Date:"), 0, 3);
        grid.add(paymentDatePicker, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Validate payment amount and update remaining balance
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        
        paymentAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double amount = Double.parseDouble(newValue);
                okButton.setDisable(amount <= 0);
                
                // Update remaining balance display
                double remainingBalance = currentBalance - amount;
                remainingBalanceLabel.setText("Remaining Balance: " + String.format("%.2f", remainingBalance));
                remainingBalanceLabel.setStyle("-fx-text-fill: " + (remainingBalance > 0 ? "#e74c3c" : "#27ae60") + "; -fx-font-weight: bold;");
            } catch (NumberFormatException e) {
                okButton.setDisable(true);
                remainingBalanceLabel.setText("Remaining Balance: " + String.format("%.2f", currentBalance));
                remainingBalanceLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            }
        });
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double paymentAmount = Double.parseDouble(paymentAmountField.getText());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String paymentDate = paymentDatePicker.getValue().format(formatter);
                String description = descriptionField.getText().trim();
                if (description.isEmpty()) {
                    description = "Payment made";
                }
                
                boolean success = config.database.addSupplierPayment(supplierName, paymentAmount, paymentDate, description);
                
                if (success) {
                    // Refresh the table data
                    supplierData.clear();
                    supplierData.addAll(getAllSuppliersWithLocation());
                    supplierTable.refresh();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText(String.format("Payment of %.2f added successfully!\nNew balance: %.2f", 
                        paymentAmount, currentBalance - paymentAmount));
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to add payment. Please try again.");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid payment amount.");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
