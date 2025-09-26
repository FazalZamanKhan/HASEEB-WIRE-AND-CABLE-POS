package com.cablemanagement.views.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.List;

import com.cablemanagement.model.Salesman;
import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;

public class SalesmanContent {

    private static final ObservableList<Salesman> salesmanList = FXCollections.observableArrayList();
    private static Salesman selectedSalesman = null;
    private static final db database = new SQLiteDatabase();

    public static Node get() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("bordered-box");

        Label heading = new Label("SALESMAN");
        heading.setStyle("-fx-font-size: 24px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        Label subHeading = new Label("Manage Salesman Accounts");
        subHeading.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");

        // Input Fields
        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField contactField = new TextField(); contactField.setPromptText("Contact Number");
        TextField addressField = new TextField(); addressField.setPromptText("Address");
        TextField commissionField = new TextField(); commissionField.setPromptText("Commission Rate");

        Button actionButton = new Button("Add Salesman");
        actionButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");

        // Layout for fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));
        formGrid.addRow(0, nameField, contactField);
        formGrid.addRow(1, addressField, commissionField, actionButton);

        // Make inputs responsive
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col, col, col);

        // Table - load data from database initially
        refreshSalesmanTable();
        TableView<Salesman> table = new TableView<>(salesmanList);
        table.setPrefHeight(300);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getColumns().add(createCol("Name", "name"));
        table.getColumns().add(createCol("Contact", "contact"));
        table.getColumns().add(createCol("Address", "address"));
        table.getColumns().add(createCol("Commission Rate", "commissionRate"));
        table.getColumns().add(createActionCol(nameField, contactField, addressField, commissionField, actionButton, table));

        // Add / Update button action
        actionButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String address = addressField.getText().trim();
            String commissionStr = commissionField.getText().trim();
            double commissionRate = 0.0;
            try {
                commissionRate = commissionStr.isEmpty() ? 0.0 : Double.parseDouble(commissionStr);
            } catch (NumberFormatException ex) {
                showAlert("Error", "Commission Rate must be a number!");
                return;
            }

            if (name.isEmpty() || contact.isEmpty()) {
                showAlert("Error", "Name and Contact are required!");
                return;
            }

            if (selectedSalesman == null) {
                // Add new salesman to database
                boolean success = database.insertSalesman(name, contact, address, commissionRate);
                if (success) {
                    refreshSalesmanTable(); // Refresh to get the new ID from database
                    showAlert("Success", "Salesman added successfully!");
                } else {
                    showAlert("Error", "Failed to add salesman to database!");
                    return;
                }
            } else {
                // Update existing salesman in database
                boolean success = database.updateSalesman(selectedSalesman.getId(), name, contact, address, commissionRate);
                if (success) {
                    refreshSalesmanTable(); // Refresh to get updated data
                    showAlert("Success", "Salesman updated successfully!");
                } else {
                    showAlert("Error", "Failed to update salesman in database!");
                    return;
                }
                selectedSalesman = null;
                actionButton.setText("Add Salesman");
            }

            nameField.clear(); contactField.clear(); addressField.clear(); commissionField.clear();
        });

        mainLayout.getChildren().addAll(heading, subHeading, formGrid, table);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

    private static TableColumn<Salesman, String> createCol(String title, String property) {
        TableColumn<Salesman, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private static TableColumn<Salesman, Void> createActionCol(
        TextField nameField, TextField contactField,
        TextField addressField, TextField commissionField,
        Button actionButton, TableView<Salesman> table
    ) {
        TableColumn<Salesman, Void> actionCol = new TableColumn<>("Action");

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.getStyleClass().add("form-submit");
                editBtn.setOnAction(e -> {
                    selectedSalesman = getTableView().getItems().get(getIndex());
                    nameField.setText(selectedSalesman.getName());
                    contactField.setText(selectedSalesman.getContact());
                    addressField.setText(selectedSalesman.getAddress());
                    commissionField.setText(String.valueOf(selectedSalesman.getCommissionRate()));
                    actionButton.setText("Update Salesman");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        return actionCol;
    }

    private static void refreshSalesmanTable() {
        salesmanList.clear();
        List<Object[]> salesmen = database.getAllSalesmen();

        for (Object[] row : salesmen) {
            salesmanList.add(new Salesman(
                (Integer) row[0], // salesman_id
                (String) row[1],  // salesman_name
                (String) row[2],  // contact_number
                (String) row[3],  // address
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0 // commission_rate
            ));
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
