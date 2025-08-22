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
        ledgerStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Ledger for: " + customerName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        
        // Date range selection
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDatePicker = new DatePicker();
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = new Button("Filter");
        Button showAllBtn = new Button("Show All");
        
        dateRangeBox.getChildren().addAll(
            new Label("From:"), fromDatePicker,
            new Label("To:"), toDatePicker,
            filterBtn, showAllBtn
        );
        
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
        descCol.setPrefWidth(150);
        
        TableColumn<Object[], String> invoiceCol = new TableColumn<>("Invoice Number");
        invoiceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[4]));
        invoiceCol.setPrefWidth(120);
        
        TableColumn<Object[], String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[5])));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-text-fill: #28a745;"); // Green for amount
        
        TableColumn<Object[], String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[6])));
        paymentCol.setPrefWidth(100);
        paymentCol.setStyle("-fx-text-fill: #28a745;"); // Green for payment
        
        TableColumn<Object[], String> returnCol = new TableColumn<>("Return Amount");
        returnCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
        returnCol.setPrefWidth(100);
        returnCol.setStyle("-fx-text-fill: #dc3545;"); // Red for return amount
        
        TableColumn<Object[], String> balanceCol = new TableColumn<>("Remaining/Balance");
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[8])));
        balanceCol.setPrefWidth(120);
        balanceCol.setStyle("-fx-text-fill: #dc3545;"); // Red for remaining balance
        
        ledgerTable.getColumns().addAll(serialCol, dateCol, timeCol, descCol, invoiceCol, 
                                       amountCol, paymentCol, returnCol, balanceCol);
        
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
                if (row.length >= 9) {
                    totalSale += (Double) row[5];      // Amount column (invoices)
                    totalPayment += (Double) row[6];   // Payment column
                    totalReturn += (Double) row[7];    // Return Amount column
                    currentBalance = (Double) row[8];  // Last balance (current balance)
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
                    // Refresh the table data
                    customerData.clear();
                    customerData.addAll(getAllCustomersWithLocation());
                    customerTable.refresh();
                    
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
        ledgerStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Ledger for: " + supplierName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        
        // Date range selection
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker fromDatePicker = new DatePicker();
        DatePicker toDatePicker = new DatePicker(LocalDate.now());
        Button filterBtn = new Button("Filter");
        Button showAllBtn = new Button("Show All");
        
        dateRangeBox.getChildren().addAll(
            new Label("From:"), fromDatePicker,
            new Label("To:"), toDatePicker,
            filterBtn, showAllBtn
        );
        
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
        descCol.setPrefWidth(150);
        
        TableColumn<Object[], String> invoiceCol = new TableColumn<>("Invoice Number");
        invoiceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[4]));
        invoiceCol.setPrefWidth(120);
        
        TableColumn<Object[], String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[5])));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-text-fill: #28a745;"); // Green for amount
        
        TableColumn<Object[], String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[6])));
        paymentCol.setPrefWidth(100);
        paymentCol.setStyle("-fx-text-fill: #28a745;"); // Green for payment
        
        TableColumn<Object[], String> returnCol = new TableColumn<>("Return Amount");
        returnCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[7])));
        returnCol.setPrefWidth(100);
        returnCol.setStyle("-fx-text-fill: #dc3545;"); // Red for return amount
        
        TableColumn<Object[], String> balanceCol = new TableColumn<>("Remaining/Balance");
        balanceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", (Double) cellData.getValue()[8])));
        balanceCol.setPrefWidth(120);
        balanceCol.setStyle("-fx-text-fill: #dc3545;"); // Red for remaining balance
        
        ledgerTable.getColumns().addAll(serialCol, dateCol, timeCol, descCol, invoiceCol, 
                                       amountCol, paymentCol, returnCol, balanceCol);
        
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
                if (row.length >= 9) {
                    totalPurchase += (Double) row[5];    // Amount column (invoices)
                    totalPayment += (Double) row[6];     // Payment column
                    totalReturn += (Double) row[7];      // Return Amount column
                    currentBalance = (Double) row[8];    // Last balance (current balance)
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
