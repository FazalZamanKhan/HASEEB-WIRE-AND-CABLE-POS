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
        System.out.println("DEBUG: Loading customer data...");
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
            System.out.println("DEBUG: Loaded " + customers.size() + " customers");
            // Don't close the connection as it's managed by the database class
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to load customer data: " + e.getMessage());
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
        try {
            System.out.println("DEBUG: Loading AccountsContent...");
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

            System.out.println("DEBUG: AccountsContent loaded successfully");
            return mainLayout;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load AccountsContent: " + e.getMessage());
            e.printStackTrace();
            
            // Return error view
            VBox errorView = new VBox(20);
            errorView.setPadding(new Insets(20));
            Label errorLabel = new Label("Error loading Accounts page: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            errorView.getChildren().add(errorLabel);
            return errorView;
        }
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

        Button deleteBtn = new Button("Delete Customer");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        deleteBtn.setPrefWidth(130);

        // Initially disable buttons until a customer is selected and user has rights
        updateBtn.setDisable(true);
        ledgerBtn.setDisable(true);
        paymentBtn.setDisable(true);
        
        deleteBtn.setDisable(true);

        // Enable/disable buttons based on selection and rights
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("Update Customer"));
            ledgerBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("View Customer Ledger"));
            paymentBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("Add Customer Payment"));
            deleteBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("Delete Customer"));
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

        deleteBtn.setOnAction(e -> {
            CustomerAccountData selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                showDeleteCustomerDialog(selectedCustomer, customerData, customerTable);
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, ledgerBtn, paymentBtn, deleteBtn);

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

        Button deleteBtn = new Button("Delete Supplier");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        deleteBtn.setPrefWidth(130);

        // Initially disable buttons until a supplier is selected and user has rights
        updateBtn.setDisable(true);
        ledgerBtn.setDisable(true);
        paymentBtn.setDisable(true);
        deleteBtn.setDisable(true);

        // Enable/disable buttons based on selection and rights
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("Update Supplier"));
            ledgerBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("View Supplier Ledger"));
            paymentBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("Add Supplier Payment"));
            deleteBtn.setDisable(!hasSelection || !config.hasCurrentUserRight("Delete Supplier"));
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

        deleteBtn.setOnAction(e -> {
            SupplierAccountData selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
            if (selectedSupplier != null) {
                showDeleteSupplierDialog(selectedSupplier, supplierData, supplierTable);
            }
        });

        actionButtonsRow.getChildren().addAll(updateBtn, ledgerBtn, paymentBtn, deleteBtn);

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
        
        // Get current balance for display
        double currentBalance = config.database.getCustomerCurrentBalance(selectedCustomer.getCustomerName());
        Label currentBalanceLabel = new Label(String.format("Current Balance: %.2f", currentBalance));
        currentBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentBalanceLabel.setStyle("-fx-text-fill: " + (currentBalance > 0 ? "#e74c3c" : "#27ae60") + ";");
        
        // Balance adjustment fields
        TextField balanceAdjustmentField = new TextField();
        balanceAdjustmentField.setPromptText("Enter balance adjustment (+ to increase, - to decrease)");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter description for balance adjustment (optional)");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        
        // New balance preview label
        Label newBalanceLabel = new Label("New Balance: " + String.format("%.2f", currentBalance));
        newBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        newBalanceLabel.setStyle("-fx-text-fill: #3498db;");
        
        // Update new balance preview when adjustment amount changes
        balanceAdjustmentField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.trim().isEmpty()) {
                    double adjustment = Double.parseDouble(newValue);
                    double newBalance = currentBalance + adjustment;
                    newBalanceLabel.setText("New Balance: " + String.format("%.2f", newBalance));
                    newBalanceLabel.setStyle("-fx-text-fill: " + (newBalance > 0 ? "#e74c3c" : "#27ae60") + "; -fx-font-weight: bold;");
                } else {
                    newBalanceLabel.setText("New Balance: " + String.format("%.2f", currentBalance));
                    newBalanceLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException e) {
                newBalanceLabel.setText("New Balance: Invalid Input");
                newBalanceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });
        
        grid.add(new Label("Customer Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Tehsil:"), 0, 2);
        grid.add(tehsilCombo, 1, 2);
        
        // Balance section
        grid.add(new Separator(), 0, 3, 2, 1);
        grid.add(currentBalanceLabel, 0, 4, 2, 1);
        grid.add(new Label("Balance Adjustment:"), 0, 5);
        grid.add(balanceAdjustmentField, 1, 5);
        grid.add(newBalanceLabel, 0, 6, 2, 1);
        grid.add(new Label("Description:"), 0, 7);
        grid.add(descriptionArea, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int customerId = config.database.getCustomerIdByName(selectedCustomer.getCustomerName());
                boolean customerUpdateSuccess = config.database.updateCustomer(customerId, nameField.getText(), 
                                                                contactField.getText(), tehsilCombo.getValue());
                
                boolean balanceAdjustmentSuccess = true;
                String adjustmentText = balanceAdjustmentField.getText().trim();
                
                // Handle balance adjustment if amount is provided
                if (!adjustmentText.isEmpty()) {
                    try {
                        double adjustmentAmount = Double.parseDouble(adjustmentText);
                        if (adjustmentAmount != 0) {
                            String description = descriptionArea.getText().trim();
                            if (description.isEmpty()) {
                                description = adjustmentAmount > 0 ? "Balance increase adjustment" : "Balance decrease adjustment";
                            }
                            
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            String adjustmentDate = LocalDate.now().format(formatter);
                            
                            balanceAdjustmentSuccess = config.database.addCustomerBalanceAdjustment(
                                selectedCustomer.getCustomerName(), adjustmentAmount, adjustmentDate, description);
                        }
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Input");
                        alert.setHeaderText(null);
                        alert.setContentText("Please enter a valid balance adjustment amount.");
                        alert.showAndWait();
                        return;
                    }
                }
                
                if (customerUpdateSuccess && balanceAdjustmentSuccess) {
                    // Refresh the table data
                    customerData.clear();
                    customerData.addAll(getAllCustomersWithLocation());
                    customerTable.refresh();
                    
                    String message = "Customer updated successfully!";
                    if (!adjustmentText.isEmpty() && Double.parseDouble(adjustmentText) != 0) {
                        double newBalance = config.database.getCustomerCurrentBalance(selectedCustomer.getCustomerName());
                        message += String.format("\nBalance adjusted by %.2f\nNew balance: %.2f", 
                                                Double.parseDouble(adjustmentText), newBalance);
                    }
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    String errorMessage = "Failed to update customer.";
                    if (!customerUpdateSuccess) errorMessage += " Customer information update failed.";
                    if (!balanceAdjustmentSuccess) errorMessage += " Balance adjustment failed.";
                    alert.setContentText(errorMessage);
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
    descCol.setPrefWidth(800);
        
        // Show full invoice item details inline in the Description column (customer ledger)
        descCol.setCellValueFactory(cellData -> {
            String invoiceNumber = (String) cellData.getValue()[4];
            if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                try {
                    StringBuilder detailedDesc = new StringBuilder();
                    if (invoiceNumber.startsWith("SRI")) {
                        // Return Sale Invoice: show returned items
                        int returnInvoiceId = config.database.getSalesReturnInvoiceIdByNumber(invoiceNumber);
                        if (returnInvoiceId != -1) {
                            List<Object[]> items = config.database.getSalesReturnInvoiceItemsByInvoiceId(returnInvoiceId);
                            for (Object[] item : items) {
                                detailedDesc.append(String.format("• %s | Qty: %.2f | Unit Price: %.2f\n",
                                    item[1], item[2], item[3]));
                            }
                        }
                    } else {
                        // Regular Sale Invoice: show sold items
                        String itemQuery = "SELECT si.quantity, si.unit_price, si.discount_amount, " +
                            "(si.quantity * si.unit_price) as total_price, " +
                            "(si.quantity * si.unit_price - si.discount_amount) as net_price, " +
                            "sv.paid_amount, sv.total_amount, sv.discount_amount as invoice_discount, " +
                            "sv.other_discount, " +
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
                            double netPrice = totalPrice - discountAmount;
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
                    }
                    return new javafx.beans.property.SimpleStringProperty(detailedDesc.toString());
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("Error loading invoice details: " + e.getMessage());
                }
            }
            return new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[3]);
        });
        descCol.setPrefWidth(400);
    descCol.setPrefWidth(800);
        // Add tooltip to show full description on hover
        descCol.setCellFactory(col -> new TableCell<Object[], String>() {
            private final Tooltip tooltip = new Tooltip();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setTooltip(null);
                } else {
                    Object[] row = getTableView().getItems().get(getIndex());
                    StringBuilder details = new StringBuilder();
                    if (row != null && row.length >= 12) {
                        String invoiceNo = (String) row[4];
                        if (invoiceNo != null && !invoiceNo.trim().isEmpty() && !invoiceNo.equals("N/A")) {
                            try {
                                if (invoiceNo.startsWith("SRI")) {
                                    // Return Sale Invoice: show returned items
                                    int returnInvoiceId = com.cablemanagement.config.database.getSalesReturnInvoiceIdByNumber(invoiceNo);
                                    if (returnInvoiceId != -1) {
                                        java.util.List<Object[]> items = com.cablemanagement.config.database.getSalesReturnInvoiceItemsByInvoiceId(returnInvoiceId);
                                        details.append("Return Sale Invoice - ").append(invoiceNo).append("\nReturned Items:\n");
                                        for (Object[] sriItem : items) {
                                            details.append(String.format("• %s | Qty: %.2f | Unit Price: %.2f\n",
                                                sriItem[1], sriItem[2], sriItem[3]));
                                        }
                                    }
                                } else {
                                    // Regular Sale Invoice: show sold items
                                    String itemQuery = "SELECT si.quantity, si.unit_price, si.discount_amount, " +
                                        "(si.quantity * si.unit_price) as total_price, " +
                                        "sv.discount_amount as invoice_discount, " +
                                        "sv.other_discount, " +
                                        "COALESCE(ps.product_name, 'Product') as item_desc " +
                                        "FROM Sales_Invoice_Item si " +
                                        "LEFT JOIN ProductionStock ps ON si.production_stock_id = ps.production_id " +
                                        "LEFT JOIN Sales_Invoice sv ON si.sales_invoice_id = sv.sales_invoice_id " +
                                        "WHERE si.sales_invoice_id = (SELECT sales_invoice_id FROM Sales_Invoice WHERE sales_invoice_number = ?)";
                                    java.sql.Connection conn = com.cablemanagement.config.database.getConnection();
                                    java.sql.PreparedStatement stmt = conn.prepareStatement(itemQuery);
                                    stmt.setString(1, invoiceNo);
                                    java.sql.ResultSet rs = stmt.executeQuery();
                                    details.append("Sales Invoice - ").append(invoiceNo).append("\nItems:\n");
                                    while (rs.next()) {
                                        double quantity = rs.getDouble("quantity");
                                        double unitPrice = rs.getDouble("unit_price");
                                        double totalPrice = rs.getDouble("total_price");
                                        double discountAmount = rs.getDouble("discount_amount");
                                        double netPrice = totalPrice - discountAmount;
                                        details.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total Price: %.2f | Discount: %.2f | Net: %.2f\n",
                                            rs.getString("item_desc"),
                                            quantity,
                                            unitPrice,
                                            totalPrice,
                                            discountAmount,
                                            netPrice));
                                    }
                                    rs.close();
                                    stmt.close();
                                }
                            } catch (Exception ex) {
                                details.append("Error loading invoice items: ").append(ex.getMessage());
                            }
                        } else {
                            details.append(item);
                        }
                    } else {
                        details.append(item);
                    }
                    setText(item);
                    tooltip.setText(details.toString());
                    setTooltip(tooltip);
                }
            }
        });

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

        TableColumn<Object[], String> otherDiscountCol = new TableColumn<>("Other Discount");
        otherDiscountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
        otherDiscountCol.setPrefWidth(100);
        otherDiscountCol.setStyle("-fx-text-fill: #fd7e14;"); // Orange for other discount
        
        TableColumn<Object[], String> amountCol = new TableColumn<>("Net Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[8])));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-text-fill: #28a745;"); // Green for amount
        
        TableColumn<Object[], String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[9])));
        paymentCol.setPrefWidth(100);
        paymentCol.setStyle("-fx-text-fill: #28a745;"); // Green for payment
        
        TableColumn<Object[], String> returnCol = new TableColumn<>("Return Amount");
        returnCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[10])));
        returnCol.setPrefWidth(100);
        returnCol.setStyle("-fx-text-fill: #dc3545;"); // Red for return amount
        
        TableColumn<Object[], String> balanceCol = new TableColumn<>("Remaining/Balance");
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[11])));
        balanceCol.setPrefWidth(120);
        balanceCol.setStyle("-fx-text-fill: #dc3545;"); // Red for remaining balance
        
    ledgerTable.getColumns().addAll(serialCol, dateCol, timeCol, invoiceCol, totalBillCol, discountCol, otherDiscountCol, amountCol, paymentCol, returnCol, balanceCol, descCol);
        
        // Create summary labels
        Label totalSaleLabel = new Label("Total Sale: 0.00");
        totalSaleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalSaleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label totalDiscountLabel = new Label("Total Discount: 0.00");
        totalDiscountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalDiscountLabel.setStyle("-fx-text-fill: #fd7e14;");
        
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
        summaryBox.getChildren().addAll(totalSaleLabel, totalDiscountLabel, totalPaymentLabel, totalReturnLabel, currentBalanceLabel);
        
        // Load ledger data
        ObservableList<Object[]> ledgerData = FXCollections.observableArrayList();
        
        // Method to update totals
        Runnable updateTotals = () -> {
            double totalSale = 0.0;
            double totalDiscount = 0.0;
            double totalPayment = 0.0;
            double totalReturn = 0.0;
            double currentBalance = 0.0;
            
            for (Object[] row : ledgerData) {
                if (row.length >= 12) {
                    totalSale += (Double) row[5];      // Total Bill column (invoices)
                    totalDiscount += (Double) row[6] + (Double) row[7]; // Discount + Other Discount columns
                    totalPayment += (Double) row[9];   // Payment column
                    totalReturn += (Double) row[10];    // Return Amount column
                    currentBalance = (Double) row[11]; // Last balance (current balance)
                }
            }
            
            totalSaleLabel.setText(String.format("Total Sale: %.2f", totalSale));
            totalDiscountLabel.setText(String.format("Total Discount: %.2f", totalDiscount));
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
                // Generate timestamp for unique filename
                String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                
                // Create temporary filename
                String tempDir = System.getProperty("java.io.tmpdir");
                String filename = tempDir + java.io.File.separator + "Customer_Ledger_" + 
                                customerName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";
                
                // Recalculate summary values for PDF generation
                double pdfTotalSale = 0.0;
                double pdfTotalDiscount = 0.0;
                double pdfTotalPayment = 0.0;
                double pdfTotalReturn = 0.0;
                double pdfCurrentBalance = 0.0;
                
                for (Object[] row : ledgerData) {
                    if (row.length >= 12) {
                        pdfTotalSale += (Double) row[5];      // Total Bill column
                        pdfTotalDiscount += (Double) row[6] + (Double) row[7]; // Discount + Other Discount columns
                        pdfTotalPayment += (Double) row[9];   // Payment column
                        pdfTotalReturn += (Double) row[10];    // Return Amount column
                        pdfCurrentBalance = (Double) row[11]; // Last balance (current balance)
                    }
                }
                
                // Generate PDF
                com.cablemanagement.invoice.LedgerPDFGenerator.generateCustomerLedgerPDF(
                    customerName, ledgerData, pdfTotalSale, pdfTotalDiscount, pdfTotalPayment, pdfTotalReturn, pdfCurrentBalance, filename);
                
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
                                new java.io.File(filename).delete();
                            } catch (Exception deleteEx) {
                                // Ignore deletion errors
                            }
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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Print Error");
                alert.setHeaderText("Failed to generate ledger PDF");
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
        ledgerStage.show();
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
    // Make the payment amount more prominent: larger and bolder
    paymentAmountField.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
    paymentAmountField.setAlignment(Pos.CENTER_RIGHT);
        
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
            String description = (String) cellData.getValue()[3];
            
            if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                try {
                    StringBuilder detailedDesc = new StringBuilder();
                    
                    // Check if this is a return invoice by checking the description for "Purchase Return"
                    if (description != null && description.contains("Purchase Return")) {
                        // This is a return invoice - get return invoice items
                        String returnItemQuery = "SELECT rprii.quantity, rprii.unit_price, " +
                            "(rprii.quantity * rprii.unit_price) as item_total, " +
                            "rpri.total_return_amount, " +
                            "COALESCE(rs.item_name, 'Item') as item_desc " +
                            "FROM Raw_Purchase_Return_Invoice_Item rprii " +
                            "LEFT JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                            "LEFT JOIN Raw_Purchase_Return_Invoice rpri ON rprii.raw_purchase_return_invoice_id = rpri.raw_purchase_return_invoice_id " +
                            "WHERE rpri.return_invoice_number = ?";
                        
                        Connection conn = config.database.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(returnItemQuery);
                        stmt.setString(1, invoiceNumber);
                        ResultSet rs = stmt.executeQuery();
                        
                        double totalReturnAmount = 0.0;
                        while (rs.next()) {
                            if (totalReturnAmount == 0.0) {
                                totalReturnAmount = rs.getDouble("total_return_amount");
                            }
                            double itemTotal = rs.getDouble("item_total");
                            double quantity = rs.getDouble("quantity");
                            double unitPrice = rs.getDouble("unit_price");
                            
                            detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Return Amount: %.2f\n",
                                rs.getString("item_desc"),
                                quantity,
                                unitPrice,
                                itemTotal));
                        }
                        detailedDesc.append(String.format("Total Return Amount: %.2f", totalReturnAmount));
                        rs.close();
                        stmt.close();
                        return new javafx.beans.property.SimpleStringProperty(detailedDesc.toString());
                    } else {
                        // Regular purchase invoice - get purchase invoice items
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
                    }
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("Error loading invoice details: " + e.getMessage());
                }
            }
            return new javafx.beans.property.SimpleStringProperty(description);
        });
        descCol.setPrefWidth(400);
            // Add hover effect to show full description in tooltip
            descCol.setCellFactory(col -> new TableCell<Object[], String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.trim().isEmpty()) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item.split("\n")[0]); // Show first line only
                        Tooltip tooltip = new Tooltip(item);
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(600);
                        setTooltip(tooltip);
                    }
                }
            });
        
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
        
        Label totalDiscountLabel = new Label("Total Discount: 0.00");
        totalDiscountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalDiscountLabel.setStyle("-fx-text-fill: #fd7e14;");
        
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
        summaryBox.getChildren().addAll(totalPurchaseLabel, totalDiscountLabel, totalPaymentLabel, totalReturnLabel, currentBalanceLabel);
        
        // Load ledger data
        ObservableList<Object[]> ledgerData = FXCollections.observableArrayList();
        
        // Method to update totals
        Runnable updateTotals = () -> {
            double totalPurchase = 0.0;
            double totalDiscount = 0.0;
            double totalPayment = 0.0;
            double totalReturn = 0.0;
            double currentBalance = 0.0;
            
            for (Object[] row : ledgerData) {
                if (row.length >= 11) {
                    totalPurchase += (Double) row[5];    // Total Bill column (sum of all invoice amounts)
                    totalDiscount += (Double) row[6];    // Discount column
                    totalPayment += (Double) row[8];     // Payment column
                    totalReturn += (Double) row[9];      // Return Amount column
                    currentBalance = (Double) row[10];   // Last balance (current balance)
                }
            }
            
            totalPurchaseLabel.setText(String.format("Total Purchase: %.2f", totalPurchase));
            totalDiscountLabel.setText(String.format("Total Discount: %.2f", totalDiscount));
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
                        currentBalance = (Double) row[10];  // Last balance (current balance)
                    }
                }
                
                // Generate PDF using the new V2 implementation
                com.cablemanagement.invoice.SupplierLedgerPrintV2.generate(
                    supplierName, ledgerData, totalPurchase, totalDiscount, totalPayment, totalReturn, currentBalance, filename);
                
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
                                new java.io.File(filename).delete();
                            } catch (Exception deleteEx) {
                                // Ignore deletion errors
                            }
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
        ledgerStage.show();
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
    // Make the payment amount more prominent: larger and bolder
    paymentAmountField.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
    paymentAmountField.setAlignment(Pos.CENTER_RIGHT);
        
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

    // Delete dialog methods with cascading options
    private static void showDeleteCustomerDialog(CustomerAccountData selectedCustomer, 
                                               ObservableList<CustomerAccountData> customerData, 
                                               TableView<CustomerAccountData> customerTable) {
        
        // Create custom dialog with two options
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete Customer");
        dialog.setHeaderText("Choose deletion method for: " + selectedCustomer.getCustomerName());
        
        // Create custom buttons
        ButtonType safeDeleteButton = new ButtonType("Safe Delete", ButtonBar.ButtonData.OTHER);
        ButtonType forceDeleteButton = new ButtonType("Force Delete (All Records)", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(safeDeleteButton, forceDeleteButton, cancelButton);
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label warningLabel = new Label("⚠️ WARNING: This action cannot be undone!");
        warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Label safeDeleteDesc = new Label("🛡️ Safe Delete: Only deletes if no related records exist\n" +
                                       "   • Will show error if customer has sales, returns, or transactions\n" +
                                       "   • Protects data integrity");
        safeDeleteDesc.setStyle("-fx-text-fill: #2ecc71;");
        
        Label forceDeleteDesc = new Label("💥 Force Delete: Removes customer AND all related records\n" +
                                        "   • Deletes all sales invoices, returns, and transactions\n" +
                                        "   • Permanently removes ALL data for this customer\n" +
                                        "   • Use with extreme caution!");
        forceDeleteDesc.setStyle("-fx-text-fill: #e74c3c;");
        
        content.getChildren().addAll(warningLabel, safeDeleteDesc, forceDeleteDesc);
        dialog.getDialogPane().setContent(content);
        
        // Handle the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == safeDeleteButton) return "safe";
            if (dialogButton == forceDeleteButton) return "force";
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String deleteType = result.get();
            boolean success = false;
            
            if ("safe".equals(deleteType)) {
                success = config.database.deleteCustomer(selectedCustomer.getCustomerName());
                if (!success) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Cannot Delete Customer");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Cannot delete customer '" + selectedCustomer.getCustomerName() + 
                                             "' because it is referenced in sales invoices, returns, or transactions.\n\n" +
                                             "To delete this customer, use 'Force Delete' or manually remove all related records first.");
                    errorAlert.showAndWait();
                    return;
                }
            } else if ("force".equals(deleteType)) {
                // Additional confirmation for force delete
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("⚠️ FINAL CONFIRMATION");
                confirmAlert.setHeaderText("Are you absolutely sure?");
                confirmAlert.setContentText("This will permanently delete '" + selectedCustomer.getCustomerName() + 
                                          "' and ALL related records including:\n" +
                                          "• All sales invoices\n" +
                                          "• All sales returns\n" + 
                                          "• All customer transactions\n" +
                                          "• All ledger entries\n\n" +
                                          "This action CANNOT be undone!");
                
                Optional<ButtonType> finalConfirm = confirmAlert.showAndWait();
                if (finalConfirm.isPresent() && finalConfirm.get() == ButtonType.OK) {
                    success = config.database.deleteCustomerCascade(selectedCustomer.getCustomerName());
                }
            }
            
            if (success) {
                // Remove from table and show success
                customerData.remove(selectedCustomer);
                customerTable.refresh();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Customer '" + selectedCustomer.getCustomerName() + 
                                          "' and " + ("force".equals(deleteType) ? "all related records have" : "has") + 
                                          " been deleted successfully.");
                successAlert.showAndWait();
            }
        }
    }

    private static void showDeleteSupplierDialog(SupplierAccountData selectedSupplier, 
                                               ObservableList<SupplierAccountData> supplierData, 
                                               TableView<SupplierAccountData> supplierTable) {
        
        // Create custom dialog with two options
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete Supplier");
        dialog.setHeaderText("Choose deletion method for: " + selectedSupplier.getSupplierName());
        
        // Create custom buttons
        ButtonType safeDeleteButton = new ButtonType("Safe Delete", ButtonBar.ButtonData.OTHER);
        ButtonType forceDeleteButton = new ButtonType("Force Delete (All Records)", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(safeDeleteButton, forceDeleteButton, cancelButton);
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label warningLabel = new Label("⚠️ WARNING: This action cannot be undone!");
        warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Label safeDeleteDesc = new Label("🛡️ Safe Delete: Only deletes if no related records exist\n" +
                                       "   • Will show error if supplier has purchases, returns, or transactions\n" +
                                       "   • Protects data integrity");
        safeDeleteDesc.setStyle("-fx-text-fill: #2ecc71;");
        
        Label forceDeleteDesc = new Label("💥 Force Delete: Removes supplier AND all related records\n" +
                                        "   • Deletes all purchase invoices, raw stock, and transactions\n" +
                                        "   • Permanently removes ALL data for this supplier\n" +
                                        "   • Use with extreme caution!");
        forceDeleteDesc.setStyle("-fx-text-fill: #e74c3c;");
        
        content.getChildren().addAll(warningLabel, safeDeleteDesc, forceDeleteDesc);
        dialog.getDialogPane().setContent(content);
        
        // Handle the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == safeDeleteButton) return "safe";
            if (dialogButton == forceDeleteButton) return "force";
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String deleteType = result.get();
            boolean success = false;
            
            if ("safe".equals(deleteType)) {
                success = config.database.deleteSupplier(selectedSupplier.getSupplierName());
                if (!success) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Cannot Delete Supplier");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Cannot delete supplier '" + selectedSupplier.getSupplierName() + 
                                             "' because it is referenced in raw stock, purchase invoices, returns, or transactions.\n\n" +
                                             "To delete this supplier, use 'Force Delete' or manually remove all related records first.");
                    errorAlert.showAndWait();
                    return;
                }
            } else if ("force".equals(deleteType)) {
                // Additional confirmation for force delete
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("⚠️ FINAL CONFIRMATION");
                confirmAlert.setHeaderText("Are you absolutely sure?");
                confirmAlert.setContentText("This will permanently delete '" + selectedSupplier.getSupplierName() + 
                                          "' and ALL related records including:\n" +
                                          "• All purchase invoices\n" +
                                          "• All raw stock items\n" + 
                                          "• All supplier transactions\n" +
                                          "• All ledger entries\n\n" +
                                          "This action CANNOT be undone!");
                
                Optional<ButtonType> finalConfirm = confirmAlert.showAndWait();
                if (finalConfirm.isPresent() && finalConfirm.get() == ButtonType.OK) {
                    success = config.database.deleteSupplierCascade(selectedSupplier.getSupplierName());
                }
            }
            
            if (success) {
                // Remove from table and show success
                supplierData.remove(selectedSupplier);
                supplierTable.refresh();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Supplier '" + selectedSupplier.getSupplierName() + 
                                          "' and " + ("force".equals(deleteType) ? "all related records have" : "has") + 
                                          " been deleted successfully.");
                successAlert.showAndWait();
            }
        }
    }
}
