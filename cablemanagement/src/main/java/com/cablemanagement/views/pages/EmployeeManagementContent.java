package com.cablemanagement.views.pages;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import com.cablemanagement.database.SQLiteDatabase;
import java.util.function.Function;
import java.time.LocalDate;
import java.util.List;

public class EmployeeManagementContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane formArea = new StackPane();
        // make it scrollable if the content is large
        formArea.getChildren().add(new Label("Select an action above"));

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        addButton(buttonBar, "Manage Designations", () -> formArea.getChildren().setAll(createDesignationForm()));
        addButton(buttonBar, "Register New Employee", () -> formArea.getChildren().setAll(createRegisterEmployeeForm()));
        addButton(buttonBar, "Register Contract Employee", () -> formArea.getChildren().setAll(createRegisterContractEmployeeForm()));
        addButton(buttonBar, "Contract-Based Employee", () -> formArea.getChildren().setAll(createContractEmployeeRecordsView()));
        addButton(buttonBar, "Manage All Employees", () -> formArea.getChildren().setAll(createSalaryEmployeeForm()));
        addButton(buttonBar, "View Salary Reports", () -> formArea.getChildren().setAll(createSalaryReportForm()));
        addButton(buttonBar, "Mark Employee Attendance", () -> formArea.getChildren().setAll(createAttendanceMarkForm()));
        addButton(buttonBar, "View Attendance Report", () -> formArea.getChildren().setAll(createAttendanceReportForm()));
        addButton(buttonBar, "Grant Advance Salary", () -> formArea.getChildren().setAll(createAdvanceSalaryForm()));
        addButton(buttonBar, "Register New Loan", () -> formArea.getChildren().setAll(createLoanRegisterForm()));
        addButton(buttonBar, "View Employee Loan Report", () -> formArea.getChildren().setAll(createLoanReportForm()));

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(formArea);

        return mainLayout;
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    // ---------------- FORMS ----------------

    ////////////////////////////////////////////////////////////////////////
    ///                    Designation Management Form                  ////
    ////////////////////////////////////////////////////////////////////////
    
    private static VBox createRegisterContractEmployeeForm() {
        VBox box = baseForm("Register Contract Employee");

        SQLiteDatabase database = new SQLiteDatabase();

        // Fields
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField cnicField = new TextField();
        cnicField.setPromptText("CNIC");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        TextField remarksField = new TextField();
        remarksField.setPromptText("Remarks");

        TextField taskField = new TextField();
        taskField.setPromptText("Task (Designation)");

        TextField numTasksField = new TextField();
        numTasksField.setPromptText("Number of Tasks");

        TextField costPerTaskField = new TextField();
        costPerTaskField.setPromptText("Cost Per Task");

        TextField totalTasksDoneField = new TextField();
        totalTasksDoneField.setPromptText("Total Tasks Done");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");

        Button submitBtn = new Button("Register Contract Employee");
        submitBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        // Grouped rows using HBox
        HBox phoneCnicRow = new HBox(10, phoneField, cnicField);
        HBox tasksCostRow = new HBox(10, numTasksField, costPerTaskField);
        HBox totalDateRow = new HBox(10, totalTasksDoneField, datePicker);

        // Stretch fields to fill equally
        phoneCnicRow.setHgrow(phoneField, Priority.ALWAYS);
        phoneCnicRow.setHgrow(cnicField, Priority.ALWAYS);

        tasksCostRow.setHgrow(numTasksField, Priority.ALWAYS);
        tasksCostRow.setHgrow(costPerTaskField, Priority.ALWAYS);

        totalDateRow.setHgrow(totalTasksDoneField, Priority.ALWAYS);
        totalDateRow.setHgrow(datePicker, Priority.ALWAYS);

        // Grid layout for clean form structure
        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Phone & CNIC:"), 0, 1);
        grid.add(phoneCnicRow, 1, 1);

        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);

        grid.add(new Label("Remarks:"), 0, 3);
        grid.add(remarksField, 1, 3);

        grid.add(new Label("Task:"), 0, 4);
        grid.add(taskField, 1, 4);

        grid.add(new Label("Tasks & Cost:"), 0, 5);
        grid.add(tasksCostRow, 1, 5);

        grid.add(new Label("Total & Date:"), 0, 6);
        grid.add(totalDateRow, 1, 6);

        grid.add(submitBtn, 1, 7);
        grid.add(statusLabel, 1, 8);

        // Center everything in the VBox
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(grid);

        // Event handler
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();
            String remarks = remarksField.getText().trim();
            String task = taskField.getText().trim();
            String numTasksText = numTasksField.getText().trim();
            String costPerTaskText = costPerTaskField.getText().trim();
            String totalTasksDoneText = totalTasksDoneField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (name.isEmpty() || task.isEmpty() || numTasksText.isEmpty() ||
                costPerTaskText.isEmpty() || totalTasksDoneText.isEmpty() || date == null) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }

            try {
                int numTasks = Integer.parseInt(numTasksText);
                int totalTasksDone = Integer.parseInt(totalTasksDoneText);
                double costPerTask = Double.parseDouble(costPerTaskText);

                if (numTasks <= 0 || totalTasksDone < 0 || costPerTask <= 0) {
                    statusLabel.setText("Invalid numeric values.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }

                boolean success = database.insertContractEmployee(
                    name, phone, cnic, address, remarks, task,
                    numTasks, costPerTask, totalTasksDone, date.toString()
                );

                if (success) {
                    statusLabel.setText("Contract employee registered!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    // Clear fields
                    nameField.clear(); phoneField.clear(); cnicField.clear(); addressField.clear();
                    remarksField.clear(); taskField.clear(); numTasksField.clear();
                    costPerTaskField.clear(); totalTasksDoneField.clear(); datePicker.setValue(null);
                } else {
                    statusLabel.setText("Failed to register employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }

            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter valid numeric values.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        return box;
    }

    private static VBox createContractEmployeeRecordsView() {
        VBox box = baseForm("Contract Employee Records");

        SQLiteDatabase database = new SQLiteDatabase();

        // Filters
        DatePicker dateFromPicker = new DatePicker();
        dateFromPicker.setPromptText("From Date");

        DatePicker dateToPicker = new DatePicker();
        dateToPicker.setPromptText("To Date");

        TextField timeFromField = new TextField();
        timeFromField.setPromptText("From Time (HH:mm)");

        TextField timeToField = new TextField();
        timeToField.setPromptText("To Time (HH:mm)");

        Button filterButton = new Button("Apply Filters");
        filterButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");

        TableView<Object[]> table = new TableView<>();

        // Define columns (adjust index based on your DB schema)
        String[] columnNames = {
            "Sr", "Name", "Phone", "CNIC", "Address", "Remarks", "Task", "Num Tasks", 
            "Cost Per Task", "Total Done", "Date", "Time"
        };

        for (int i = 0; i < columnNames.length; i++) {
            final int colIndex = i;
            TableColumn<Object[], String> column = new TableColumn<>(columnNames[i]);
            column.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                    cellData.getValue()[colIndex] != null 
                    ? cellData.getValue()[colIndex].toString() 
                    : ""
                )
            );
            table.getColumns().add(column);
        }

        filterButton.setOnAction(e -> {
            LocalDate dateFrom = dateFromPicker.getValue();
            LocalDate dateTo = dateToPicker.getValue();
            String timeFrom = timeFromField.getText().trim(); // optional
            String timeTo = timeToField.getText().trim();     // optional

            List<Object[]> records = database.getContractEmployeeRecords(dateFrom, dateTo, timeFrom, timeTo);
            table.getItems().clear();
            table.getItems().addAll(records);
        });

        // Load all records initially
        filterButton.fire();

        HBox filters = new HBox(10, dateFromPicker, dateToPicker, timeFromField, timeToField, filterButton);
        filters.setPadding(new Insets(10));

        box.getChildren().addAll(filters, table);
        return box;
    }



    ////////////////////////////////////////////////////////////////////////
    ///                    Designation Management Form                  ////
    ////////////////////////////////////////////////////////////////////////

    private static VBox createDesignationForm() {
        VBox box = baseForm("Designation Management");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();
        
        // Form controls
        TextField nameField = new TextField();
        nameField.setPromptText("Designation Name");
        
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        
        // Initially disable update and delete buttons
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        
        // Button styling
        addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        updateBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        clearBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        clearBtn.getStyleClass().add("register-button");
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        
        // Table for displaying designations
        TableView<DesignationTableData> table = new TableView<>();
        table.setPrefHeight(300);
        
        // Table columns
        TableColumn<DesignationTableData, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(50);
        
        TableColumn<DesignationTableData, String> nameCol = new TableColumn<>("Designation Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(200);
        
        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        
        // Observable list for table data
        ObservableList<DesignationTableData> designationData = FXCollections.observableArrayList();
        table.setItems(designationData);
        
        // Load initial data
        loadDesignationData(database, designationData);
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        
        // Event handlers
        addBtn.setOnAction(e -> {
            String designationName = nameField.getText().trim();
            if (designationName.isEmpty()) {
                statusLabel.setText("Please enter a designation name.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (database.insertDesignation(designationName)) {
                statusLabel.setText("Designation added successfully!");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                nameField.clear();
                loadDesignationData(database, designationData);
            } else {
                statusLabel.setText("Failed to add designation. It might already exist.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        
        updateBtn.setOnAction(e -> {
            DesignationTableData selected = table.getSelectionModel().getSelectedItem();
            String designationName = nameField.getText().trim();
            
            if (selected == null || designationName.isEmpty()) {
                statusLabel.setText("Please select a designation and enter a new name.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (database.updateDesignation(selected.getId(), designationName)) {
                statusLabel.setText("Designation updated successfully!");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                nameField.clear();
                table.getSelectionModel().clearSelection();
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
                loadDesignationData(database, designationData);
            } else {
                statusLabel.setText("Failed to update designation.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        
        deleteBtn.setOnAction(e -> {
            DesignationTableData selected = table.getSelectionModel().getSelectedItem();
            
            if (selected == null) {
                statusLabel.setText("Please select a designation to delete.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Designation");
            confirmAlert.setContentText("Are you sure you want to delete the designation '" + selected.getName() + "'?");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (database.deleteDesignation(selected.getId())) {
                    statusLabel.setText("Designation deleted successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    nameField.clear();
                    table.getSelectionModel().clearSelection();
                    updateBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                    loadDesignationData(database, designationData);
                } else {
                    statusLabel.setText("Failed to delete designation. It might be in use by employees.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        clearBtn.setOnAction(e -> {
            nameField.clear();
            table.getSelectionModel().clearSelection();
            updateBtn.setDisable(true);
            deleteBtn.setDisable(true);
            statusLabel.setText("");
        });
        
        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
            } else {
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });
        
        box.getChildren().addAll(nameField, buttonBox, statusLabel, table);
        
        return box;
    }
    
    // Helper method to load designation data into the table
    private static void loadDesignationData(SQLiteDatabase database, ObservableList<DesignationTableData> designationData) {
        designationData.clear();
        for (Object[] row : database.getAllDesignations()) {
            designationData.add(new DesignationTableData((Integer) row[0], (String) row[1]));
        }
    }
    
    // Inner class for table data with JavaFX properties
    public static class DesignationTableData {
        private final javafx.beans.property.SimpleIntegerProperty id;
        private final javafx.beans.property.SimpleStringProperty name;
        
        public DesignationTableData(int id, String name) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
        }
        
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public javafx.beans.property.SimpleIntegerProperty idProperty() { return id; }
        
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public javafx.beans.property.SimpleStringProperty nameProperty() { return name; }
    }

    //////////////////////////////////////////////////////////////////////////
    ///                   Register New Employee Form                     /////
    //////////////////////////////////////////////////////////////////////////

    private static VBox createRegisterEmployeeForm() {
        VBox box = baseForm("Register New Employee");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField cnicField = new TextField();
        cnicField.setPromptText("CNIC");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        ComboBox<String> designationCombo = new ComboBox<>();
        designationCombo.setPromptText("Select Designation");
        
        // Load designations from database
        for (Object[] row : database.getAllDesignations()) {
            designationCombo.getItems().add((String) row[1]); // row[1] is the designation title
        }

        ComboBox<String> salaryTypeCombo = new ComboBox<>();
        salaryTypeCombo.setPromptText("Select Salary Type");
        salaryTypeCombo.getItems().addAll("monthly", "daily", "hourly");

        TextField salaryAmountField = new TextField();
        salaryAmountField.setPromptText("Salary Amount");

        Button submitBtn = new Button("Register Employee");
        submitBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        // Event handler
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();
            String designation = designationCombo.getValue();
            String salaryType = salaryTypeCombo.getValue();
            String salaryAmountText = salaryAmountField.getText().trim();
            
            // Validation
            if (name.isEmpty() || designation == null || salaryType == null || salaryAmountText.isEmpty()) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            try {
                double salaryAmount = Double.parseDouble(salaryAmountText);
                if (salaryAmount <= 0) {
                    statusLabel.setText("Salary amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                if (database.insertEmployee(name, phone, cnic, address, designation, salaryType, salaryAmount)) {
                    statusLabel.setText("Employee registered successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    // Clear form
                    nameField.clear();
                    phoneField.clear();
                    cnicField.clear();
                    addressField.clear();
                    designationCombo.setValue(null);
                    salaryTypeCombo.setValue(null);
                    salaryAmountField.clear();
                } else {
                    statusLabel.setText("Failed to register employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid salary amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        box.getChildren().addAll(nameField, phoneField, cnicField, addressField, 
                                designationCombo, salaryTypeCombo, salaryAmountField, 
                                submitBtn, statusLabel);
        return box;
    }

    //////////////////////////////////////////////////////////////////////////
    ///                   Contact based Employee Form                    /////
    //////////////////////////////////////////////////////////////////////////

    private static VBox createContractEmployeeForm() {
        VBox box = baseForm("Contract-Based Employees");

        SQLiteDatabase database = new SQLiteDatabase();

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, phone, or designation...");
        searchField.setMaxWidth(300);

        // Info label
        Label infoLabel = new Label("Contract-based employees are those with daily, hourly payment types.");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        // Table setup
        TableView<EmployeeTableData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EmployeeTableData, String> nameCol = new TableColumn<>("Employee Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<EmployeeTableData, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        TableColumn<EmployeeTableData, String> designationCol = new TableColumn<>("Designation");
        designationCol.setCellValueFactory(cellData -> cellData.getValue().designationProperty());

        TableColumn<EmployeeTableData, String> salaryTypeCol = new TableColumn<>("Payment Type");
        salaryTypeCol.setCellValueFactory(cellData -> cellData.getValue().salaryTypeProperty());

        TableColumn<EmployeeTableData, Double> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(cellData -> cellData.getValue().salaryAmountProperty().asObject());

        TableColumn<EmployeeTableData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        table.getColumns().addAll(nameCol, phoneCol, designationCol, salaryTypeCol, rateCol, statusCol);

        // Data setup
        ObservableList<EmployeeTableData> contractEmployeeData = FXCollections.observableArrayList();
        table.setItems(contractEmployeeData);
        loadContractEmployeeData(database, contractEmployeeData);

        // Search logic
        FilteredList<EmployeeTableData> filteredData = new FilteredList<>(contractEmployeeData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase().trim();
            filteredData.setPredicate(emp -> {
                if (lower.isEmpty()) return true;
                return emp.getName().toLowerCase().contains(lower)
                    || emp.getPhone().toLowerCase().contains(lower)
                    || emp.getDesignation().toLowerCase().contains(lower);
            });
        });
        table.setItems(filteredData);

        VBox content = new VBox(searchField, infoLabel, table);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);

        box.getChildren().add(content);
        return box;
    }

    //////////////////////////////////////////////////////////////////////////
    ///                    Manage All Employees                          /////
    //////////////////////////////////////////////////////////////////////////

    private static VBox createSalaryEmployeeForm() {
        VBox box = baseForm("Manage Employees");

        SQLiteDatabase database = new SQLiteDatabase();

        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Employee Name");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField cnicField = new TextField();
        cnicField.setPromptText("CNIC");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        ComboBox<String> designationCombo = new ComboBox<>();
        designationCombo.setPromptText("Select Designation");
        for (Object[] row : database.getAllDesignations()) {
            designationCombo.getItems().add((String) row[1]);
        }

        ComboBox<String> salaryTypeCombo = new ComboBox<>();
        salaryTypeCombo.setPromptText("Select Salary Type");
        salaryTypeCombo.getItems().addAll("monthly", "daily", "hourly", "task");

        TextField salaryAmountField = new TextField();
        salaryAmountField.setPromptText("Salary Amount");

        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");

        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        updateBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        clearBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");

        // Form layout using GridPane
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        formGrid.add(new Label("Name:"), 0, 0);
        formGrid.add(nameField, 1, 0);

        formGrid.add(new Label("Phone:"), 0, 1);
        formGrid.add(phoneField, 1, 1);

        formGrid.add(new Label("CNIC:"), 0, 2);
        formGrid.add(cnicField, 1, 2);

        formGrid.add(new Label("Address:"), 0, 3);
        formGrid.add(addressField, 1, 3);

        formGrid.add(new Label("Designation:"), 2, 0);
        formGrid.add(designationCombo, 3, 0);

        formGrid.add(new Label("Salary Type:"), 2, 1);
        formGrid.add(salaryTypeCombo, 3, 1);

        formGrid.add(new Label("Salary Amount:"), 2, 2);
        formGrid.add(salaryAmountField, 3, 2);

        HBox buttonBox = new HBox(10, updateBtn, deleteBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        formGrid.add(buttonBox, 3, 3);

        // Table
        TableView<EmployeeTableData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<TableColumn<EmployeeTableData, ?>> columns = FXCollections.observableArrayList();
        columns.add(createColumn("ID", EmployeeTableData::idProperty, 50));
        columns.add(createColumn("Name", EmployeeTableData::nameProperty, 150));
        columns.add(createColumn("Phone", EmployeeTableData::phoneProperty, 120));
        columns.add(createColumn("Designation", EmployeeTableData::designationProperty, 120));
        columns.add(createColumn("Salary Type", EmployeeTableData::salaryTypeProperty, 100));
        columns.add(createColumn("Salary Amount", EmployeeTableData::salaryAmountProperty, 120));
        columns.add(createColumn("Status", EmployeeTableData::statusProperty, 80));
        table.getColumns().addAll(columns);

        ObservableList<EmployeeTableData> employeeData = FXCollections.observableArrayList();
        table.setItems(employeeData);
        loadEmployeeData(database, employeeData);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        
        setupEventHandlers(table, database, nameField, phoneField, cnicField, addressField, 
            designationCombo, salaryTypeCombo, salaryAmountField, updateBtn, deleteBtn, statusLabel, employeeData);

        VBox content = new VBox(formGrid, statusLabel, table);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        box.getChildren().add(scrollPane);

        return box;
    }

    private static <T> TableColumn<EmployeeTableData, T> createColumn(String title, Function<EmployeeTableData, ObservableValue<T>> property, int width) {
        TableColumn<EmployeeTableData, T> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> property.apply(cellData.getValue()));
        col.setPrefWidth(width);
        return col;
    }

    private static <T> TableColumn<LoanData, T> createLoanColumn(String title, Function<LoanData, ObservableValue<T>> property) {
        TableColumn<LoanData, T> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> property.apply(cellData.getValue()));
        return col;
    }

    ////////////////////////////////////////////////////////////////////
    ///                   View Salary Reports Form                   ///
    ////////////////////////////////////////////////////////////////////

    private static VBox createSalaryReportForm() {
        VBox box = baseForm("View Salary Reports");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();
        
        // Date range filters
        HBox dateFilterBox = new HBox(10);
        dateFilterBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setValue(java.time.LocalDate.now().withDayOfMonth(1)); // Default to first day of current month
        
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setValue(java.time.LocalDate.now()); // Default to today
        
        Button generateReportBtn = new Button("Generate Report");
        generateReportBtn.getStyleClass().add("register-button");
        
        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().add("register-button");
        
        dateFilterBox.getChildren().addAll(
            new Label("From:"), startDatePicker,
            new Label("To:"), endDatePicker,
            generateReportBtn, exportBtn
        );
        
        // Salary report table
        TableView<SalaryReportData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<SalaryReportData, String> nameCol = new TableColumn<>("Employee Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().employeeNameProperty());
        nameCol.setPrefWidth(150);
        
        TableColumn<SalaryReportData, String> designationCol = new TableColumn<>("Designation");
        designationCol.setCellValueFactory(cellData -> cellData.getValue().designationProperty());
        designationCol.setPrefWidth(120);
        
        TableColumn<SalaryReportData, String> salaryTypeCol = new TableColumn<>("Salary Type");
        salaryTypeCol.setCellValueFactory(cellData -> cellData.getValue().salaryTypeProperty());
        salaryTypeCol.setPrefWidth(100);
        
        TableColumn<SalaryReportData, Double> salaryAmountCol = new TableColumn<>("Base Salary");
        salaryAmountCol.setCellValueFactory(cellData -> cellData.getValue().salaryAmountProperty().asObject());
        salaryAmountCol.setPrefWidth(100);
        
        TableColumn<SalaryReportData, Integer> presentDaysCol = new TableColumn<>("Present Days");
        presentDaysCol.setCellValueFactory(cellData -> cellData.getValue().presentDaysProperty().asObject());
        presentDaysCol.setPrefWidth(100);
        
        TableColumn<SalaryReportData, Integer> absentDaysCol = new TableColumn<>("Absent Days");
        absentDaysCol.setCellValueFactory(cellData -> cellData.getValue().absentDaysProperty().asObject());
        absentDaysCol.setPrefWidth(100);
        
        TableColumn<SalaryReportData, Double> totalHoursCol = new TableColumn<>("Total Hours");
        totalHoursCol.setCellValueFactory(cellData -> cellData.getValue().totalHoursProperty().asObject());
        totalHoursCol.setPrefWidth(100);
        
        TableColumn<SalaryReportData, Double> calculatedSalaryCol = new TableColumn<>("Calculated Salary");
        calculatedSalaryCol.setCellValueFactory(cellData -> cellData.getValue().calculatedSalaryProperty().asObject());
        calculatedSalaryCol.setPrefWidth(120);
        
        table.getColumns().addAll(nameCol, designationCol, salaryTypeCol, salaryAmountCol, 
                                 presentDaysCol, absentDaysCol, totalHoursCol, calculatedSalaryCol);
        
        // Data for table
        ObservableList<SalaryReportData> salaryData = FXCollections.observableArrayList();
        table.setItems(salaryData);
        
        // Summary info
        Label summaryLabel = new Label("Select date range and click 'Generate Report' to view salary data");
        summaryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        // Status label
        Label statusLabel = new Label();
        
        // Generate report button action
        generateReportBtn.setOnAction(e -> {
            java.time.LocalDate startDate = startDatePicker.getValue();
            java.time.LocalDate endDate = endDatePicker.getValue();
            
            if (startDate == null || endDate == null) {
                statusLabel.setText("Please select both start and end dates.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                statusLabel.setText("Start date cannot be after end date.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            loadSalaryReportData(database, salaryData, startDate.toString(), endDate.toString());
            
            if (salaryData.isEmpty()) {
                summaryLabel.setText("No salary data found for the selected date range.");
                summaryLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            } else {
                double totalSalary = salaryData.stream().mapToDouble(SalaryReportData::getCalculatedSalary).sum();
                summaryLabel.setText(String.format("Showing %d employees | Total Calculated Salary: %.2f", 
                    salaryData.size(), totalSalary));
                summaryLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
            statusLabel.setText("");
        });
        
        // Export button action (placeholder)
        exportBtn.setOnAction(e -> {
            if (salaryData.isEmpty()) {
                statusLabel.setText("No data to export. Please generate a report first.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            statusLabel.setText("Export functionality coming soon...");
            statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        });
        
        VBox content = new VBox(dateFilterBox, statusLabel, summaryLabel, table);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        box.getChildren().add(scrollPane);

        return box;
    }

    ////////////////////////////////////////////////////////////////////
    ///                   Mark Employee Attendance Form              ///
    ////////////////////////////////////////////////////////////////////

    private static VBox createAttendanceMarkForm() {
        VBox box = baseForm("Mark All Employees Attendance");

        SQLiteDatabase database = new SQLiteDatabase();

        // Top: Date Picker
        Label dateLabel = new Label("Select Date:");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        // Table Columns: ID, Name, Role, Attendance, Hours
        TableView<EmployeeRow> table = new TableView<>();
        ObservableList<EmployeeRow> rows = FXCollections.observableArrayList();

        for (Object[] emp : database.getAllEmployees()) {
            if ("Active".equals(emp[8])) {
                rows.add(new EmployeeRow(
                    (int) emp[0],
                    emp[1].toString(),
                    emp[5].toString()
                ));
            }
        }

        table.setItems(rows);
        table.setEditable(true);

        // Columns
        TableColumn<EmployeeRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id)));

        TableColumn<EmployeeRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().name));

        TableColumn<EmployeeRow, String> roleCol = new TableColumn<>("Designation");
        roleCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().role));

        TableColumn<EmployeeRow, Boolean> statusCol = new TableColumn<>("Status (P/A)");
        statusCol.setCellFactory(col -> new TableCell<>() {
            final ToggleGroup group = new ToggleGroup();
            final RadioButton present = new RadioButton("P");
            final RadioButton absent = new RadioButton("A");
            {
                present.setToggleGroup(group);
                absent.setToggleGroup(group);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EmployeeRow emp = getTableView().getItems().get(getIndex());
                    present.setSelected(emp.present);
                    absent.setSelected(!emp.present);

                    group.selectedToggleProperty().addListener((obs, old, val) -> {
                        emp.present = (val == present);
                        table.refresh();
                    });

                    HBox box = new HBox(5, present, absent);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, roleCol, statusCol);
        table.setPrefHeight(400);

        // Submit Button & Status Label
        Button markBtn = new Button("Mark Attendance");
        Label statusLabel = new Label();

        markBtn.setOnAction(e -> {
            LocalDate date = datePicker.getValue();
            if (date == null) {
                statusLabel.setText("Please select a date.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            boolean allGood = true;
            for (EmployeeRow emp : rows) {
                String status = emp.present ? "present" : "absent";
                // Set working hours to 0 since we're not tracking them
                double hours = 0.0;
                boolean success = database.insertEmployeeAttendance(emp.id, date.toString(), status, hours);
                if (!success) {
                    allGood = false;
                    statusLabel.setText("Failed to mark attendance for " + emp.name);
                    statusLabel.setStyle("-fx-text-fill: red;");
                    break;
                }
            }

            if (allGood) {
                statusLabel.setText("Attendance marked successfully.");
                statusLabel.setStyle("-fx-text-fill: green;");
                table.refresh();
            }
        });

        box.getChildren().addAll(
            new VBox(dateLabel, datePicker),
            new Separator(),
            table,
            new Separator(),
            markBtn,
            statusLabel
        );

        return box;
    }

    public static class EmployeeRow {
        int id;
        String name;
        String role;
        boolean present = true;

        public EmployeeRow(int id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }
    }

    private static VBox createAttendanceReportForm() {
        VBox box = baseForm("View Attendance Report");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();
        
        // Date range filters
        HBox dateFilterBox = new HBox(10);
        dateFilterBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(30)); // Default to last 30 days
        
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setValue(java.time.LocalDate.now());
        
        Button filterBtn = new Button("Filter");
        filterBtn.getStyleClass().add("register-button");
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("register-button");
        
        dateFilterBox.getChildren().addAll(
            new Label("From:"), startDatePicker,
            new Label("To:"), endDatePicker,
            filterBtn, refreshBtn
        );
        
        // Attendance table
        TableView<AttendanceTableData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<AttendanceTableData, String> nameCol = new TableColumn<>("Employee Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().employeeNameProperty());
        nameCol.setPrefWidth(150);
        
        TableColumn<AttendanceTableData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        dateCol.setPrefWidth(100);
        
        TableColumn<AttendanceTableData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(80);
        
        TableColumn<AttendanceTableData, Double> hoursCol = new TableColumn<>("Working Hours");
        hoursCol.setCellValueFactory(cellData -> cellData.getValue().workingHoursProperty().asObject());
        hoursCol.setPrefWidth(100);
        
        table.getColumns().addAll(nameCol, dateCol, statusCol, hoursCol);
        
        // Data for table
        ObservableList<AttendanceTableData> attendanceData = FXCollections.observableArrayList();
        table.setItems(attendanceData);
        
        // Load initial data
        loadAttendanceData(database, attendanceData, null, null);
        
        // Filter button action
        filterBtn.setOnAction(e -> {
            java.time.LocalDate startDate = startDatePicker.getValue();
            java.time.LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Date Range");
                    alert.setHeaderText("Invalid Date Range");
                    alert.setContentText("Start date cannot be after end date.");
                    alert.showAndWait();
                    return;
                }
                loadAttendanceData(database, attendanceData, startDate.toString(), endDate.toString());
            } else {
                loadAttendanceData(database, attendanceData, null, null);
            }
        });
        
        // Refresh button action
        refreshBtn.setOnAction(e -> {
            java.time.LocalDate startDate = startDatePicker.getValue();
            java.time.LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                loadAttendanceData(database, attendanceData, startDate.toString(), endDate.toString());
            } else {
                loadAttendanceData(database, attendanceData, null, null);
            }
        });
        
        // Summary info
        Label summaryLabel = new Label("Attendance records will appear here");
        summaryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        VBox content = new VBox(dateFilterBox, summaryLabel, table);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        box.getChildren().add(scrollPane);

        return box;
    }

    ///////////////////////////////////////////////////////////////////
    ///                  Grant Advance Salary Form                /////
    ///////////////////////////////////////////////////////////////////

    private static VBox createAdvanceSalaryForm() {
        VBox box = baseForm("Grant Advance Salary");
        
        // Database instance
        SQLiteDatabase database = new SQLiteDatabase();

        // Employee search section
        VBox searchSection = new VBox(10);
        Label searchLabel = new Label("Search Employee:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Type employee name to search...");
        searchField.setPrefWidth(300);
        
        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.setPrefWidth(300);
        
        // Load all active employees initially
        loadEmployeeComboBox(database, employeeCombo, "");
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadEmployeeComboBox(database, employeeCombo, newVal.trim());
        });
        
        searchSection.getChildren().addAll(searchLabel, searchField, employeeCombo);
        
        // Employee details section
        VBox detailsSection = new VBox(10);
        Label detailsLabel = new Label("Employee Details:");
        detailsLabel.setStyle("-fx-font-weight: bold;");
        
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        
        Label empIdLabel = new Label("Employee ID:");
        Label empIdValue = new Label("-");
        empIdValue.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        Label designationLabel = new Label("Designation:");
        Label designationValue = new Label("-");
        designationValue.setStyle("-fx-text-fill: #2c3e50;");
        
        Label salaryTypeLabel = new Label("Salary Type:");
        Label salaryTypeValue = new Label("-");
        salaryTypeValue.setStyle("-fx-text-fill: #2c3e50;");
        
        Label baseSalaryLabel = new Label("Base Salary:");
        Label baseSalaryValue = new Label("-");
        baseSalaryValue.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        detailsGrid.add(empIdLabel, 0, 0);
        detailsGrid.add(empIdValue, 1, 0);
        detailsGrid.add(designationLabel, 2, 0);
        detailsGrid.add(designationValue, 3, 0);
        detailsGrid.add(salaryTypeLabel, 0, 1);
        detailsGrid.add(salaryTypeValue, 1, 1);
        detailsGrid.add(baseSalaryLabel, 2, 1);
        detailsGrid.add(baseSalaryValue, 3, 1);
        
        detailsSection.getChildren().addAll(detailsLabel, detailsGrid);
        
        // Advance salary form section
        VBox formSection = new VBox(10);
        Label formLabel = new Label("Advance Salary Details:");
        formLabel.setStyle("-fx-font-weight: bold;");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Advance Amount");
        amountField.setPrefWidth(200);

        DatePicker dateField = new DatePicker();
        dateField.setPromptText("Advance Date");
        dateField.setValue(java.time.LocalDate.now());
        dateField.setPrefWidth(200);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description/Reason");
        descriptionField.setPrefWidth(300);

        Button grantBtn = new Button("Grant Advance");
        grantBtn.getStyleClass().add("register-button");
        grantBtn.setPrefWidth(150);
        
        Button viewAdvancesBtn = new Button("View All Advances");
        viewAdvancesBtn.getStyleClass().add("register-button");
        viewAdvancesBtn.setPrefWidth(150);
        
        HBox buttonBox = new HBox(10, grantBtn, viewAdvancesBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        formSection.getChildren().addAll(formLabel, amountField, dateField, descriptionField, buttonBox);
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        
        // Advance history table
        TableView<AdvanceSalaryData> historyTable = new TableView<>();
        historyTable.setPrefHeight(200);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<AdvanceSalaryData, String> historyDateCol = new TableColumn<>("Date");
        historyDateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        
        TableColumn<AdvanceSalaryData, Double> historyAmountCol = new TableColumn<>("Amount");
        historyAmountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        
        TableColumn<AdvanceSalaryData, String> historyDescCol = new TableColumn<>("Description");
        historyDescCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        
        TableColumn<AdvanceSalaryData, String> historyStatusCol = new TableColumn<>("Status");
        historyStatusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        historyTable.getColumns().addAll(historyDateCol, historyAmountCol, historyDescCol, historyStatusCol);
        
        ObservableList<AdvanceSalaryData> historyData = FXCollections.observableArrayList();
        historyTable.setItems(historyData);
        
        Label historyLabel = new Label("Recent Advance History:");
        historyLabel.setStyle("-fx-font-weight: bold;");
        
        // Employee selection event handler
        employeeCombo.setOnAction(e -> {
            String selectedEmployee = employeeCombo.getValue();
            if (selectedEmployee != null && !selectedEmployee.isEmpty()) {
                loadEmployeeDetails(database, selectedEmployee, empIdValue, designationValue, 
                                 salaryTypeValue, baseSalaryValue);
                // Clear advance history for now - could load employee-specific history here
                historyData.clear();
            } else {
                clearEmployeeDetails(empIdValue, designationValue, salaryTypeValue, baseSalaryValue);
                historyData.clear();
            }
        });

        // Grant advance button event handler
        grantBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String amountText = amountField.getText().trim();
            java.time.LocalDate date = dateField.getValue();
            String description = descriptionField.getText().trim();
            
            if (employee == null || employee.isEmpty()) {
                statusLabel.setText("Please select an employee.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (amountText.isEmpty() || date == null) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    statusLabel.setText("Amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                int employeeId = database.getEmployeeIdByName(employee);
                if (employeeId == -1) {
                    statusLabel.setText("Employee not found.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                if (database.insertAdvanceSalary(employeeId, amount, date.toString(), description)) {
                    statusLabel.setText("Advance salary granted successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    
                    // Clear form
                    amountField.clear();
                    dateField.setValue(java.time.LocalDate.now());
                    descriptionField.clear();
                    
                    // Refresh history
                    loadRecentAdvances(database, historyData);
                } else {
                    statusLabel.setText("Failed to grant advance salary. Please try again.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        
        // View all advances button
        viewAdvancesBtn.setOnAction(e -> {
            loadRecentAdvances(database, historyData);
            statusLabel.setText("Showing recent advance salary records.");
            statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        });

        // Create responsive layout
        VBox content = new VBox(20);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);
        
        // Add sections with separators
        content.getChildren().addAll(
            searchSection,
            new Separator(),
            detailsSection,
            new Separator(),
            formSection,
            statusLabel,
            new Separator(),
            historyLabel,
            historyTable
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        box.getChildren().add(scrollPane);

        return box;
    }


    ////////////////////////////////////////////////////////////////////
    ///                  Register New Employee Loan Form             ///
    ////////////////////////////////////////////////////////////////////

    private static VBox createLoanRegisterForm() {
        VBox box = baseForm("Register New Employee Loan");
        SQLiteDatabase database = new SQLiteDatabase();

        // --- Search Section ---
        VBox searchSection = new VBox(10);
        searchSection.setPadding(new Insets(5));
        Label searchLabel = new Label("Search Employee:");
        searchLabel.setStyle("-fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Type employee name to search...");
        searchField.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.setMaxWidth(Double.MAX_VALUE);

        loadEmployeeComboBox(database, employeeCombo, "");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadEmployeeComboBox(database, employeeCombo, newVal.trim());
        });

        searchSection.getChildren().addAll(searchLabel, searchField, employeeCombo);

        // --- Details Section ---
        VBox detailsSection = new VBox(10);
        Label detailsLabel = new Label("Employee Details:");
        detailsLabel.setStyle("-fx-font-weight: bold;");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);

        Label empIdValue = new Label("-");
        Label designationValue = new Label("-");
        Label salaryTypeValue = new Label("-");
        Label baseSalaryValue = new Label("-");

        addRow(detailsGrid, "Employee ID:", empIdValue, 0, 0);
        addRow(detailsGrid, "Designation:", designationValue, 2, 0);
        addRow(detailsGrid, "Salary Type:", salaryTypeValue, 0, 1);
        addRow(detailsGrid, "Base Salary:", baseSalaryValue, 2, 1);

        detailsSection.getChildren().addAll(detailsLabel, detailsGrid);

        // --- Loan Form Section ---
        VBox formSection = new VBox(10);
        Label formLabel = new Label("Loan Details:");
        formLabel.setStyle("-fx-font-weight: bold;");

        FlowPane formFlow = new FlowPane(10, 10);
        formFlow.setPrefWrapLength(600); // Wraps controls on smaller windows
        formFlow.setPadding(new Insets(5));

        TextField amountField = new TextField();
        amountField.setPromptText("Loan Amount");
        amountField.setPrefWidth(200);

        DatePicker loanDateField = new DatePicker(LocalDate.now());
        loanDateField.setPromptText("Loan Date");
        loanDateField.setPrefWidth(200);

        DatePicker dueDateField = new DatePicker();
        dueDateField.setPromptText("Due Date (Optional)");
        dueDateField.setPrefWidth(200);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description/Purpose");
        descriptionField.setPrefWidth(300);

        Button saveBtn = new Button("Register Loan");
        saveBtn.setPrefWidth(150);
        saveBtn.getStyleClass().add("register-button");

        Button viewLoansBtn = new Button("View All Loans");
        viewLoansBtn.setPrefWidth(150);
        viewLoansBtn.getStyleClass().add("register-button");

        HBox buttonBox = new HBox(10, saveBtn, viewLoansBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        formFlow.getChildren().addAll(amountField, loanDateField, dueDateField, descriptionField);
        formSection.getChildren().addAll(formLabel, formFlow, buttonBox);

        // --- Status Label ---
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-weight: bold;");

        // --- Recent Loans Table ---
        Label recentLoansLabel = new Label("Recent Loans:");
        recentLoansLabel.setStyle("-fx-font-weight: bold;");

        TableView<LoanData> recentLoansTable = new TableView<>();
        recentLoansTable.setPrefHeight(200);
        recentLoansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(recentLoansTable, Priority.ALWAYS);

        recentLoansTable.getColumns().addAll(
            createLoanColumn("Employee", data -> data.employeeNameProperty()),
            createLoanColumn("Loan Amount", data -> data.loanAmountProperty().asObject()),
            createLoanColumn("Loan Date", data -> data.loanDateProperty()),
            createLoanColumn("Due Date", data -> data.dueDateProperty()),
            createLoanColumn("Status", data -> data.statusProperty()),
            createLoanColumn("Remaining", data -> data.remainingAmountProperty().asObject())
        );

        ObservableList<LoanData> recentLoansData = FXCollections.observableArrayList();
        recentLoansTable.setItems(recentLoansData);

        // Employee selection event handler
        employeeCombo.setOnAction(e -> {
            String selectedEmployee = employeeCombo.getValue();
            if (selectedEmployee != null && !selectedEmployee.isEmpty()) {
                loadEmployeeDetails(database, selectedEmployee, empIdValue, designationValue, 
                                 salaryTypeValue, baseSalaryValue);
                // Clear status
                statusLabel.setText("");
            } else {
                clearEmployeeDetails(empIdValue, designationValue, salaryTypeValue, baseSalaryValue);
            }
        });

        // Save loan button event handler
        saveBtn.setOnAction(e -> {
            String employee = employeeCombo.getValue();
            String amountText = amountField.getText().trim();
            LocalDate loanDate = loanDateField.getValue();
            LocalDate dueDate = dueDateField.getValue();
            String description = descriptionField.getText().trim();
            
            if (employee == null || employee.isEmpty()) {
                statusLabel.setText("Please select an employee.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            if (amountText.isEmpty() || loanDate == null) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    statusLabel.setText("Loan amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                int employeeId = database.getEmployeeIdByName(employee);
                if (employeeId == -1) {
                    statusLabel.setText("Employee not found.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                
                String dueDateStr = (dueDate != null) ? dueDate.toString() : null;
                
                if (database.insertEmployeeLoan(employeeId, amount, loanDate.toString(), dueDateStr, description)) {
                    statusLabel.setText("Loan registered successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    
                    // Clear form
                    amountField.clear();
                    loanDateField.setValue(LocalDate.now());
                    dueDateField.setValue(null);
                    descriptionField.clear();
                    
                    // Refresh recent loans
                    loadRecentLoans(database, recentLoansData);
                } else {
                    statusLabel.setText("Failed to register loan. Please try again.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid loan amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        
        // View all loans button
        viewLoansBtn.setOnAction(e -> {
            loadRecentLoans(database, recentLoansData);
            statusLabel.setText("Showing recent loan records.");
            statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        });

        // Load initial data
        loadRecentLoans(database, recentLoansData);

        // --- Responsive Layout ---
        VBox content = new VBox(20);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);

        content.getChildren().addAll(
            searchSection,
            new Separator(),
            detailsSection,
            new Separator(),
            formSection,
            statusLabel,
            new Separator(),
            recentLoansLabel,
            recentLoansTable
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        box.getChildren().add(scrollPane);

        return box;
    }


    private static VBox createLoanReportForm() {
        VBox box = baseForm("View Employee Loan Report");

        SQLiteDatabase database = new SQLiteDatabase();

        // --- Filter Section ---
        FlowPane filterPane = new FlowPane(10, 10);
        filterPane.setPrefWrapLength(900); // Adjust as needed
        filterPane.setPadding(new Insets(10));
        filterPane.setAlignment(Pos.CENTER_LEFT);

        TextField employeeSearchField = new TextField();
        employeeSearchField.setPromptText("Search by employee name...");
        employeeSearchField.setPrefWidth(200);
        employeeSearchField.setMaxWidth(Double.MAX_VALUE);

        DatePicker startDatePicker = new DatePicker(LocalDate.now().minusDays(90));
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setMaxWidth(Double.MAX_VALUE);

        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPromptText("End Date");
        endDatePicker.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "active", "paid", "defaulted", "written_off");
        statusFilter.setValue("All");
        statusFilter.setPromptText("Filter by Status");
        statusFilter.setMaxWidth(Double.MAX_VALUE);

        Button filterBtn = new Button("Filter");
        filterBtn.getStyleClass().add("register-button");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("register-button");

        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().add("register-button");

        filterPane.getChildren().addAll(
            new Label("Employee:"), employeeSearchField,
            new Label("From:"), startDatePicker,
            new Label("To:"), endDatePicker,
            new Label("Status:"), statusFilter,
            filterBtn, refreshBtn, exportBtn
        );

        // --- Loan Report Table ---
        TableView<LoanData> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<LoanData, String> employeeNameCol = new TableColumn<>("Employee Name");
        employeeNameCol.setCellValueFactory(cell -> cell.getValue().employeeNameProperty());

        TableColumn<LoanData, Double> loanAmountCol = new TableColumn<>("Loan Amount");
        loanAmountCol.setCellValueFactory(cell -> cell.getValue().loanAmountProperty().asObject());

        TableColumn<LoanData, String> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(cell -> cell.getValue().loanDateProperty());

        TableColumn<LoanData, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(cell -> cell.getValue().dueDateProperty());

        TableColumn<LoanData, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cell -> cell.getValue().descriptionProperty());

        TableColumn<LoanData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "active":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "paid":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "defaulted":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "written_off":
                            setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        TableColumn<LoanData, Double> remainingAmountCol = new TableColumn<>("Remaining Amount");
        remainingAmountCol.setCellValueFactory(cell -> cell.getValue().remainingAmountProperty().asObject());

        TableColumn<LoanData, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button updateBtn = new Button("Update");
            {
                updateBtn.getStyleClass().add("register-button");
                updateBtn.setOnAction(e -> {
                    LoanData loan = getTableView().getItems().get(getIndex());
                    showUpdateLoanDialog(database, loan, table);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : updateBtn);
            }
        });

        table.getColumns().addAll(employeeNameCol, loanAmountCol, loanDateCol, dueDateCol,
                descriptionCol, statusCol, remainingAmountCol, actionCol);

        ObservableList<LoanData> loanData = FXCollections.observableArrayList();
        table.setItems(loanData);

        Label summaryLabel = new Label("Loan records will appear here");
        summaryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        Label statusLabel = new Label();

        // Initial Data Load
        loadLoanReportData(database, loanData, null, null, null, null);
        updateSummaryLabel(loanData, summaryLabel);

        filterBtn.setOnAction(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String employeeName = employeeSearchField.getText().trim();
            String status = statusFilter.getValue();

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                statusLabel.setText("Start date cannot be after end date.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }

            String startStr = startDate != null ? startDate.toString() : null;
            String endStr = endDate != null ? endDate.toString() : null;
            String empFilter = employeeName.isEmpty() ? null : employeeName;
            String statusVal = "All".equals(status) ? null : status;

            loadLoanReportData(database, loanData, startStr, endStr, empFilter, statusVal);
            updateSummaryLabel(loanData, summaryLabel);
            statusLabel.setText("");
        });

        refreshBtn.setOnAction(e -> {
            loadLoanReportData(database, loanData, null, null, null, null);
            updateSummaryLabel(loanData, summaryLabel);
            statusLabel.setText("Data refreshed.");
            statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        });

        exportBtn.setOnAction(e -> {
            if (loanData.isEmpty()) {
                statusLabel.setText("No data to export. Please apply filters first.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("Export functionality coming soon...");
                statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            }
        });

        VBox content = new VBox(filterPane, statusLabel, summaryLabel, table);
        content.setStyle("-fx-background-color: transparent;");
        content.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        box.getChildren().add(scrollPane);

        return box;
    }

    private static void addRow(GridPane grid, String labelText, Label value, int col, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");
        value.setStyle("-fx-text-fill: #2c3e50;");
        grid.add(label, col, row);
        grid.add(value, col + 1, row);
    }


    /////////////////////////////////////////////////////////////
    ///                    baseForm Method                    ///   
    /////////////////////////////////////////////////////////////

    private static VBox baseForm(String title) {
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 20px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        form.getChildren().add(heading);
        return form;
    }
    

    // Helper method to load employee data into the table
    private static void loadEmployeeData(SQLiteDatabase database, ObservableList<EmployeeTableData> employeeData) {
        employeeData.clear();
        for (Object[] row : database.getAllEmployees()) {
            employeeData.add(new EmployeeTableData(
                (Integer) row[0],  // employee_id
                (String) row[1],   // employee_name
                (String) row[2],   // phone_number
                (String) row[3],   // cnic
                (String) row[4],   // address
                (String) row[5],   // designation_title
                (String) row[6],   // salary_type
                (Double) row[7],   // salary_amount
                (String) row[8]    // status
            ));
        }
    }
    
    // Helper method to load contract employee data (non-monthly employees)
    private static void loadContractEmployeeData(SQLiteDatabase database, ObservableList<EmployeeTableData> contractEmployeeData) {
        contractEmployeeData.clear();
        for (Object[] row : database.getAllEmployees()) {
            String salaryType = (String) row[6];
            // Only include non-monthly employees (contract-based)
            if (!"monthly".equals(salaryType)) {
                contractEmployeeData.add(new EmployeeTableData(
                    (Integer) row[0],  // employee_id
                    (String) row[1],   // employee_name
                    (String) row[2],   // phone_number
                    (String) row[3],   // cnic
                    (String) row[4],   // address
                    (String) row[5],   // designation_title
                    (String) row[6],   // salary_type
                    (Double) row[7],   // salary_amount
                    (String) row[8]    // status
                ));
            }
        }
    }
    
    // Helper method to clear form fields
    private static void clearForm(TextField nameField, TextField phoneField, TextField cnicField, 
                                 TextField addressField, ComboBox<String> designationCombo, 
                                 ComboBox<String> salaryTypeCombo, TextField salaryAmountField) {
        nameField.clear();
        phoneField.clear();
        cnicField.clear();
        addressField.clear();
        designationCombo.setValue(null);
        salaryTypeCombo.setValue(null);
        salaryAmountField.clear();
    }
    
    // Helper method to setup event handlers for employee management table and form
    private static void setupEventHandlers(
        TableView<EmployeeTableData> table,
        SQLiteDatabase database,
        TextField nameField,
        TextField phoneField,
        TextField cnicField,
        TextField addressField,
        ComboBox<String> designationCombo,
        ComboBox<String> salaryTypeCombo,
        TextField salaryAmountField,
        Button updateBtn,
        Button deleteBtn,
        Label statusLabel,
        ObservableList<EmployeeTableData> employeeData
    ) {
        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                phoneField.setText(newSelection.getPhone());
                cnicField.setText(newSelection.getCnic());
                addressField.setText(newSelection.getAddress());
                designationCombo.setValue(newSelection.getDesignation());
                salaryTypeCombo.setValue(newSelection.getSalaryType());
                salaryAmountField.setText(String.valueOf(newSelection.getSalaryAmount()));
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
            } else {
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });

        // Update button handler
        updateBtn.setOnAction(e -> {
            EmployeeTableData selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Please select an employee to update.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String cnic = cnicField.getText().trim();
            String address = addressField.getText().trim();
            String designation = designationCombo.getValue();
            String salaryType = salaryTypeCombo.getValue();
            String salaryAmountText = salaryAmountField.getText().trim();

            if (name.isEmpty() || designation == null || salaryType == null || salaryAmountText.isEmpty()) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }

            try {
                double salaryAmount = Double.parseDouble(salaryAmountText);
                if (salaryAmount <= 0) {
                    statusLabel.setText("Salary amount must be positive.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }
                boolean success = database.updateEmployee(
                    selected.getId(), name, phone, cnic, address, designation, salaryType, salaryAmount
                );
                if (success) {
                    statusLabel.setText("Employee updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    clearForm(nameField, phoneField, cnicField, addressField, designationCombo, salaryTypeCombo, salaryAmountField);
                    table.getSelectionModel().clearSelection();
                    updateBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                    loadEmployeeData(database, employeeData);
                } else {
                    statusLabel.setText("Failed to update employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid salary amount.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        // Delete button handler
        deleteBtn.setOnAction(e -> {
            EmployeeTableData selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Please select an employee to delete.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Employee");
            confirmAlert.setContentText("Are you sure you want to delete employee '" + selected.getName() + "'?");
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                boolean success = database.deleteEmployee(selected.getId());
                if (success) {
                    statusLabel.setText("Employee deleted successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    clearForm(nameField, phoneField, cnicField, addressField, designationCombo, salaryTypeCombo, salaryAmountField);
                    table.getSelectionModel().clearSelection();
                    updateBtn.setDisable(true);
                    deleteBtn.setDisable(true);
                    loadEmployeeData(database, employeeData);
                } else {
                    statusLabel.setText("Failed to delete employee.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });

        // Clear button handler
        // Find the clear button in the buttonBox if needed, or add a handler here if passed
        // For this implementation, clearBtn is not passed, so you may need to add it if you want to handle it here
    }

    // Inner class for employee table data with JavaFX properties
    public static class EmployeeTableData {
        private final javafx.beans.property.SimpleIntegerProperty id;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty phone;
        private final javafx.beans.property.SimpleStringProperty cnic;
        private final javafx.beans.property.SimpleStringProperty address;
        private final javafx.beans.property.SimpleStringProperty designation;
        private final javafx.beans.property.SimpleStringProperty salaryType;
        private final javafx.beans.property.SimpleDoubleProperty salaryAmount;
        private final javafx.beans.property.SimpleStringProperty status;
        
        public EmployeeTableData(int id, String name, String phone, String cnic, String address,
                               String designation, String salaryType, double salaryAmount, String status) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.phone = new javafx.beans.property.SimpleStringProperty(phone);
            this.cnic = new javafx.beans.property.SimpleStringProperty(cnic);
            this.address = new javafx.beans.property.SimpleStringProperty(address);
            this.designation = new javafx.beans.property.SimpleStringProperty(designation);
            this.salaryType = new javafx.beans.property.SimpleStringProperty(salaryType);
            this.salaryAmount = new javafx.beans.property.SimpleDoubleProperty(salaryAmount);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }
        
        // ID property
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public javafx.beans.property.SimpleIntegerProperty idProperty() { return id; }
        
        // Name property
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public javafx.beans.property.SimpleStringProperty nameProperty() { return name; }
        
        // Phone property
        public String getPhone() { return phone.get(); }
        public void setPhone(String phone) { this.phone.set(phone); }
        public javafx.beans.property.SimpleStringProperty phoneProperty() { return phone; }
        
        // CNIC property
        public String getCnic() { return cnic.get(); }
        public void setCnic(String cnic) { this.cnic.set(cnic); }
        public javafx.beans.property.SimpleStringProperty cnicProperty() { return cnic; }
        
        // Address property
        public String getAddress() { return address.get(); }
        public void setAddress(String address) { this.address.set(address); }
        public javafx.beans.property.SimpleStringProperty addressProperty() { return address; }
        
        // Designation property
        public String getDesignation() { return designation.get(); }
        public void setDesignation(String designation) { this.designation.set(designation); }
        public javafx.beans.property.SimpleStringProperty designationProperty() { return designation; }
        
        // Salary Type property
        public String getSalaryType() { return salaryType.get(); }
        public void setSalaryType(String salaryType) { this.salaryType.set(salaryType); }
        public javafx.beans.property.SimpleStringProperty salaryTypeProperty() { return salaryType; }
        
        // Salary Amount property
        public double getSalaryAmount() { return salaryAmount.get(); }
        public void setSalaryAmount(double salaryAmount) { this.salaryAmount.set(salaryAmount); }
        public javafx.beans.property.SimpleDoubleProperty salaryAmountProperty() { return salaryAmount; }
        
        // Status property
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
    }

    // Helper method to load attendance data into the table
    private static void loadAttendanceData(SQLiteDatabase database, ObservableList<AttendanceTableData> attendanceData, String startDate, String endDate) {
        attendanceData.clear();
        List<Object[]> attendanceList;
        
        if (startDate != null && endDate != null) {
            attendanceList = database.getEmployeeAttendanceByDateRange(startDate, endDate);
        } else {
            attendanceList = database.getAllEmployeeAttendance();
        }
        
        for (Object[] row : attendanceList) {
            if (startDate != null && endDate != null) {
                // For date range query: employee_id, employee_name, attendance_date, status, working_hours
                attendanceData.add(new AttendanceTableData(
                    (String) row[1],   // employee_name
                    (String) row[2],   // attendance_date
                    (String) row[3],   // status
                    (Double) row[4]    // working_hours
                ));
            } else {
                // For all attendance query: employee_name, attendance_date, status, working_hours
                attendanceData.add(new AttendanceTableData(
                    (String) row[0],   // employee_name
                    (String) row[1],   // attendance_date
                    (String) row[2],   // status
                    (Double) row[3]    // working_hours
                ));
            }
        }
    }

    // Inner class for attendance table data with JavaFX properties
    public static class AttendanceTableData {
        private final javafx.beans.property.SimpleStringProperty employeeName;
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleDoubleProperty workingHours;
        
        public AttendanceTableData(String employeeName, String date, String status, double workingHours) {
            this.employeeName = new javafx.beans.property.SimpleStringProperty(employeeName);
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.workingHours = new javafx.beans.property.SimpleDoubleProperty(workingHours);
        }
        
        // Employee Name property
        public String getEmployeeName() { return employeeName.get(); }
        public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
        public javafx.beans.property.SimpleStringProperty employeeNameProperty() { return employeeName; }
        
        // Date property
        public String getDate() { return date.get(); }
        public void setDate(String date) { this.date.set(date); }
        public javafx.beans.property.SimpleStringProperty dateProperty() { return date; }
        
        // Status property
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
        
        // Working Hours property
        public double getWorkingHours() { return workingHours.get(); }
        public void setWorkingHours(double workingHours) { this.workingHours.set(workingHours); }
        public javafx.beans.property.SimpleDoubleProperty workingHoursProperty() { return workingHours; }
    }

    // Helper method to load salary report data into the table
    private static void loadSalaryReportData(SQLiteDatabase database, ObservableList<SalaryReportData> salaryData, String startDate, String endDate) {
        salaryData.clear();
        List<Object[]> reportList = database.getSalaryReportByDateRange(startDate, endDate);
        
        for (Object[] row : reportList) {
            // Row: employee_id, employee_name, designation_title, salary_type, salary_amount, total_hours, present_days, absent_days
            String salaryType = (String) row[3];
            double baseSalary = (Double) row[4];
            double totalHours = (Double) row[5];
            int presentDays = (Integer) row[6];
            
            // Calculate salary based on type
            double calculatedSalary = calculateSalary(salaryType, baseSalary, totalHours, presentDays);
            
            salaryData.add(new SalaryReportData(
                (String) row[1],   // employee_name
                (String) row[2],   // designation_title
                salaryType,        // salary_type
                baseSalary,        // salary_amount
                presentDays,       // present_days
                (Integer) row[7],  // absent_days
                totalHours,        // total_hours
                calculatedSalary   // calculated_salary
            ));
        }
    }
    
    // Helper method to calculate salary based on type
    private static double calculateSalary(String salaryType, double baseSalary, double totalHours, int presentDays) {
        switch (salaryType.toLowerCase()) {
            case "monthly":
                return baseSalary; // Fixed monthly salary
            case "daily":
                return baseSalary * presentDays; // Daily rate * present days
            case "hourly":
                return baseSalary * totalHours; // Hourly rate * total hours
            case "task":
                return baseSalary; // Fixed task-based payment
            default:
                return 0.0;
        }
    }

    // Inner class for salary report table data with JavaFX properties
    public static class SalaryReportData {
        private final javafx.beans.property.SimpleStringProperty employeeName;
        private final javafx.beans.property.SimpleStringProperty designation;
        private final javafx.beans.property.SimpleStringProperty salaryType;
        private final javafx.beans.property.SimpleDoubleProperty salaryAmount;
        private final javafx.beans.property.SimpleIntegerProperty presentDays;
        private final javafx.beans.property.SimpleIntegerProperty absentDays;
        private final javafx.beans.property.SimpleDoubleProperty totalHours;
        private final javafx.beans.property.SimpleDoubleProperty calculatedSalary;
        
        public SalaryReportData(String employeeName, String designation, String salaryType, 
                               double salaryAmount, int presentDays, int absentDays, 
                               double totalHours, double calculatedSalary) {
            this.employeeName = new javafx.beans.property.SimpleStringProperty(employeeName);
            this.designation = new javafx.beans.property.SimpleStringProperty(designation);
            this.salaryType = new javafx.beans.property.SimpleStringProperty(salaryType);
            this.salaryAmount = new javafx.beans.property.SimpleDoubleProperty(salaryAmount);
            this.presentDays = new javafx.beans.property.SimpleIntegerProperty(presentDays);
            this.absentDays = new javafx.beans.property.SimpleIntegerProperty(absentDays);
            this.totalHours = new javafx.beans.property.SimpleDoubleProperty(totalHours);
            this.calculatedSalary = new javafx.beans.property.SimpleDoubleProperty(calculatedSalary);
        }
        
        // Employee Name property
        public String getEmployeeName() { return employeeName.get(); }
        public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
        public javafx.beans.property.SimpleStringProperty employeeNameProperty() { return employeeName; }
        
        // Designation property
        public String getDesignation() { return designation.get(); }
        public void setDesignation(String designation) { this.designation.set(designation); }
        public javafx.beans.property.SimpleStringProperty designationProperty() { return designation; }
        
        // Salary Type property
        public String getSalaryType() { return salaryType.get(); }
        public void setSalaryType(String salaryType) { this.salaryType.set(salaryType); }
        public javafx.beans.property.SimpleStringProperty salaryTypeProperty() { return salaryType; }
        
        // Salary Amount property
        public double getSalaryAmount() { return salaryAmount.get(); }
        public void setSalaryAmount(double salaryAmount) { this.salaryAmount.set(salaryAmount); }
        public javafx.beans.property.SimpleDoubleProperty salaryAmountProperty() { return salaryAmount; }
        
        // Present Days property
        public int getPresentDays() { return presentDays.get(); }
        public void setPresentDays(int presentDays) { this.presentDays.set(presentDays); }
        public javafx.beans.property.SimpleIntegerProperty presentDaysProperty() { return presentDays; }
        
        // Absent Days property
        public int getAbsentDays() { return absentDays.get(); }
        public void setAbsentDays(int absentDays) { this.absentDays.set(absentDays); }
        public javafx.beans.property.SimpleIntegerProperty absentDaysProperty() { return absentDays; }
        
        // Total Hours property
        public double getTotalHours() { return totalHours.get(); }
        public void setTotalHours(double totalHours) { this.totalHours.set(totalHours); }
        public javafx.beans.property.SimpleDoubleProperty totalHoursProperty() { return totalHours; }
        
        // Calculated Salary property
        public double getCalculatedSalary() { return calculatedSalary.get(); }
        public void setCalculatedSalary(double calculatedSalary) { this.calculatedSalary.set(calculatedSalary); }
        public javafx.beans.property.SimpleDoubleProperty calculatedSalaryProperty() { return calculatedSalary; }
    }

    // Helper methods for advance salary form
    private static void loadEmployeeComboBox(SQLiteDatabase database, ComboBox<String> comboBox, String searchTerm) {
        comboBox.getItems().clear();
        
        for (Object[] row : database.getAllEmployees()) {
            String status = (String) row[8];
            String employeeName = (String) row[1];
            
            if ("Active".equals(status)) {
                if (searchTerm.isEmpty() || 
                    employeeName.toLowerCase().contains(searchTerm.toLowerCase())) {
                    comboBox.getItems().add(employeeName);
                }
            }
        }
    }
    
    private static void loadEmployeeDetails(SQLiteDatabase database, String employeeName, 
                                          Label empIdValue, Label designationValue, 
                                          Label salaryTypeValue, Label baseSalaryValue) {
        for (Object[] row : database.getAllEmployees()) {
            String name = (String) row[1];
            if (name.equals(employeeName)) {
                empIdValue.setText(String.valueOf(row[0]));
                designationValue.setText((String) row[5]);
                salaryTypeValue.setText((String) row[6]);
                baseSalaryValue.setText(String.format("%.2f", (Double) row[7]));
                break;
            }
        }
    }
    
    private static void clearEmployeeDetails(Label empIdValue, Label designationValue, 
                                           Label salaryTypeValue, Label baseSalaryValue) {
        empIdValue.setText("-");
        designationValue.setText("-");
        salaryTypeValue.setText("-");
        baseSalaryValue.setText("-");
    }
    
    private static void loadRecentAdvances(SQLiteDatabase database, ObservableList<AdvanceSalaryData> historyData) {
        historyData.clear();
        List<Object[]> advances = database.getAllAdvanceSalaries();
        
        // Load only the most recent 10 records
        int count = 0;
        for (Object[] row : advances) {
            if (count >= 10) break;
            historyData.add(new AdvanceSalaryData(
                (String) row[2],   // advance_date
                (Double) row[1],   // amount
                (String) row[3],   // description
                (String) row[4]    // status
            ));
            count++;
        }
    }
    
    private static void loadRecentLoans(SQLiteDatabase database, ObservableList<LoanData> loansData) {
        loansData.clear();
        List<Object[]> loans = database.getAllEmployeeLoans();
        
        // Load only the most recent 10 records
        int count = 0;
        for (Object[] row : loans) {
            if (count >= 10) break;
            loansData.add(new LoanData(
                (String) row[0],    // employee_name
                (Double) row[1],    // loan_amount
                (String) row[2],    // loan_date
                (String) row[3],    // due_date
                (String) row[4],    // description
                (String) row[5],    // status
                (Double) row[6],    // remaining_amount
                (Integer) row[7]    // loan_id
            ));
            count++;
        }
    }

    // Inner class for advance salary table data with JavaFX properties
    public static class AdvanceSalaryData {
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleDoubleProperty amount;
        private final javafx.beans.property.SimpleStringProperty description;
        private final javafx.beans.property.SimpleStringProperty status;
        
        public AdvanceSalaryData(String date, double amount, String description, String status) {
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.amount = new javafx.beans.property.SimpleDoubleProperty(amount);
            this.description = new javafx.beans.property.SimpleStringProperty(description != null ? description : "");
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }
        
        // Date property
        public String getDate() { return date.get(); }
        public void setDate(String date) { this.date.set(date); }
        public javafx.beans.property.SimpleStringProperty dateProperty() { return date; }
        
        // Amount property
        public double getAmount() { return amount.get(); }
        public void setAmount(double amount) { this.amount.set(amount); }
        public javafx.beans.property.SimpleDoubleProperty amountProperty() { return amount; }
        
        // Description property
        public String getDescription() { return description.get(); }
        public void setDescription(String description) { this.description.set(description); }
        public javafx.beans.property.SimpleStringProperty descriptionProperty() { return description; }
        
        // Status property
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
    }

    // Helper methods for loan functionality
    private static void loadLoanReportData(SQLiteDatabase database, ObservableList<LoanData> loanData, 
                                         String startDate, String endDate, String employeeName, String status) {
        loanData.clear();
        List<Object[]> loans;
        
        if (startDate != null && endDate != null) {
            loans = database.getEmployeeLoansByDateRange(startDate, endDate);
        } else if (employeeName != null) {
            loans = database.getLoansByEmployee(employeeName);
        } else {
            loans = database.getAllEmployeeLoans();
        }
        
        for (Object[] row : loans) {
            String loanStatus = (String) row[5];
            
            // Apply status filter
            if (status != null && !loanStatus.equalsIgnoreCase(status)) {
                continue;
            }
            
            // Apply employee name filter if not already filtered by database query
            if (employeeName != null && startDate != null && endDate != null) {
                String empName = (String) row[0];
                if (!empName.toLowerCase().contains(employeeName.toLowerCase())) {
                    continue;
                }
            }
            
            loanData.add(new LoanData(
                (String) row[0],   // employee_name
                (Double) row[1],   // loan_amount
                (String) row[2],   // loan_date
                (String) row[3],   // due_date
                (String) row[4],   // description
                loanStatus,        // status
                (Double) row[6],   // remaining_amount
                (Integer) row[7]   // loan_id
            ));
        }
    }
    
    private static void updateSummaryLabel(ObservableList<LoanData> loanData, Label summaryLabel) {
        if (loanData.isEmpty()) {
            summaryLabel.setText("No loan records found.");
            summaryLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
        } else {
            double totalLoanAmount = loanData.stream().mapToDouble(LoanData::getLoanAmount).sum();
            double totalRemaining = loanData.stream().mapToDouble(LoanData::getRemainingAmount).sum();
            long activeLoans = loanData.stream().filter(loan -> "active".equalsIgnoreCase(loan.getStatus())).count();
            
            summaryLabel.setText(String.format("Total: %d loans | Total Amount: %.2f | Remaining: %.2f | Active: %d", 
                loanData.size(), totalLoanAmount, totalRemaining, activeLoans));
            summaryLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }
    
    private static void showUpdateLoanDialog(SQLiteDatabase database, LoanData loan, TableView<LoanData> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Loan Status");
        dialog.setHeaderText("Update loan for: " + loan.getEmployeeName());
        
        // Create the dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label currentStatusLabel = new Label("Current Status: " + loan.getStatus());
        currentStatusLabel.setStyle("-fx-font-weight: bold;");
        
        Label currentRemainingLabel = new Label("Current Remaining: " + String.format("%.2f", loan.getRemainingAmount()));
        currentRemainingLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "paid", "defaulted", "written_off");
        statusCombo.setValue(loan.getStatus());
        
        TextField remainingField = new TextField();
        remainingField.setText(String.valueOf(loan.getRemainingAmount()));
        remainingField.setPromptText("Remaining Amount");
        
        content.getChildren().addAll(
            currentStatusLabel,
            currentRemainingLabel,
            new Label("New Status:"),
            statusCombo,
            new Label("New Remaining Amount:"),
            remainingField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Handle the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String newStatus = statusCombo.getValue();
                    double newRemaining = Double.parseDouble(remainingField.getText());
                    
                    if (newRemaining < 0) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Amount");
                        alert.setContentText("Remaining amount cannot be negative.");
                        alert.showAndWait();
                        return;
                    }
                    
                    if (database.updateLoanStatus(loan.getLoanId(), newStatus, newRemaining)) {
                        // Refresh the table
                        loan.setStatus(newStatus);
                        loan.setRemainingAmount(newRemaining);
                        table.refresh();
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Success");
                        success.setContentText("Loan status updated successfully!");
                        success.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Error");
                        error.setContentText("Failed to update loan status.");
                        error.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Amount");
                    alert.setContentText("Please enter a valid remaining amount.");
                    alert.showAndWait();
                }
            }
        });
    }

    // Inner class for loan table data with JavaFX properties
    public static class LoanData {
        private final javafx.beans.property.SimpleStringProperty employeeName;
        private final javafx.beans.property.SimpleDoubleProperty loanAmount;
        private final javafx.beans.property.SimpleStringProperty loanDate;
        private final javafx.beans.property.SimpleStringProperty dueDate;
        private final javafx.beans.property.SimpleStringProperty description;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleDoubleProperty remainingAmount;
        private final int loanId;
        
        public LoanData(String employeeName, double loanAmount, String loanDate, String dueDate, 
                       String description, String status, double remainingAmount, int loanId) {
            this.employeeName = new javafx.beans.property.SimpleStringProperty(employeeName);
            this.loanAmount = new javafx.beans.property.SimpleDoubleProperty(loanAmount);
            this.loanDate = new javafx.beans.property.SimpleStringProperty(loanDate);
            this.dueDate = new javafx.beans.property.SimpleStringProperty(dueDate != null ? dueDate : "N/A");
            this.description = new javafx.beans.property.SimpleStringProperty(description != null ? description : "");
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.remainingAmount = new javafx.beans.property.SimpleDoubleProperty(remainingAmount);
            this.loanId = loanId;
        }
        
        // Employee Name property
        public String getEmployeeName() { return employeeName.get(); }
        public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
        public javafx.beans.property.SimpleStringProperty employeeNameProperty() { return employeeName; }
        
        // Loan Amount property
        public double getLoanAmount() { return loanAmount.get(); }
        public void setLoanAmount(double loanAmount) { this.loanAmount.set(loanAmount); }
        public javafx.beans.property.SimpleDoubleProperty loanAmountProperty() { return loanAmount; }
        
        // Loan Date property
        public String getLoanDate() { return loanDate.get(); }
        public void setLoanDate(String loanDate) { this.loanDate.set(loanDate); }
        public javafx.beans.property.SimpleStringProperty loanDateProperty() { return loanDate; }
        
        // Due Date property
        public String getDueDate() { return dueDate.get(); }
        public void setDueDate(String dueDate) { this.dueDate.set(dueDate); }
        public javafx.beans.property.SimpleStringProperty dueDateProperty() { return dueDate; }
        
        // Description property
        public String getDescription() { return description.get(); }
        public void setDescription(String description) { this.description.set(description); }
        public javafx.beans.property.SimpleStringProperty descriptionProperty() { return description; }
        
        // Status property
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
        
        // Remaining Amount property
        public double getRemainingAmount() { return remainingAmount.get(); }
        public void setRemainingAmount(double remainingAmount) { this.remainingAmount.set(remainingAmount); }
        public javafx.beans.property.SimpleDoubleProperty remainingAmountProperty() { return remainingAmount; }
        
        // Loan ID getter
        public int getLoanId() { return loanId; }
    }
}
