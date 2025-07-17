package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;
import com.cablemanagement.model.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class RegisterContent {

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Form area (center of the screen)
        StackPane formArea = new StackPane();
        formArea.getChildren().add(createCategoryForm());

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        // Important: force button bar to compute its own height
        buttonBar.setMinHeight(Region.USE_PREF_SIZE);
        buttonBar.setMaxHeight(Region.USE_PREF_SIZE);
        buttonBar.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Wrap HBox in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false); // This disables vertical expansion
        scrollPane.setPrefHeight(72);
        scrollPane.setMinHeight(72);
        scrollPane.setMaxHeight(72);

        // Styling (optional)
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Buttons and actions
        addButton(buttonBar, "Register Category", () -> formArea.getChildren().setAll(createCategoryForm()));
        addButton(buttonBar, "Register Manufacturer", () -> formArea.getChildren().setAll(createManufacturerForm()));
        addButton(buttonBar, "Register Brand", () -> formArea.getChildren().setAll(createBrandForm()));
        addButton(buttonBar, "Register Province", () -> formArea.getChildren().setAll(createProvinceForm()));
        addButton(buttonBar, "Register District", () -> formArea.getChildren().setAll(createDistrictForm()));
        addButton(buttonBar, "Register Tehsil", () -> formArea.getChildren().setAll(createTehsilForm()));
        addButton(buttonBar, "Register Units", () -> formArea.getChildren().setAll(createUnitForm()));
        addButton(buttonBar, "Register Customer", () -> formArea.getChildren().setAll(createCustomerForm()));
        addButton(buttonBar, "Register Supplier", () -> formArea.getChildren().setAll(createSupplierForm()));

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

    // --------------------- FORMS ---------------------

    private static VBox createCategoryForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Category");
        heading.getStyleClass().add("form-heading");

        // Input Row
        Label categoryLabel = new Label("Category Name:");
        TextField categoryField = new TextField();
        Button submit = new Button("Submit Category");
        
        // Delete Button
        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.getStyleClass().add("form-delete-button");

        HBox inputRow = new HBox(10, categoryLabel, categoryField, submit, deleteSelected);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.getStyleClass().add("form-row");

        // Category List
        Label listHeading = new Label("Registered Categories:");
        ListView<String> categoryList = new ListView<>();
        categoryList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        categoryList.getStyleClass().add("category-list");

        // Load from DB
        if (config.database != null && config.database.isConnected()) {
            categoryList.getItems().addAll(config.database.getAllCategories());
        } else {
            categoryList.getItems().addAll("Fiber Optics", "Coaxial Cables", "Ethernet Cables"); // Fallback mock data
        }

        // Submit Action
        submit.setOnAction(e -> {
            String name = categoryField.getText().trim();
            if (!name.isEmpty()) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertCategory(name)) {
                        categoryList.getItems().add(name);
                        categoryField.clear();
                        showAlert("Success", "Category added!");
                    } else {
                        showAlert("Error", "Failed to add category to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Category name cannot be empty!");
            }
        });

        // Delete Action
        deleteSelected.setOnAction(e -> {
            String selected = categoryList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteCategory(selected)) {
                        categoryList.getItems().remove(selected);
                        showAlert("Success", "Category deleted!");
                    } else {
                        showAlert("Error", "Failed to delete category from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No category selected!");
            }
        });

        form.getChildren().addAll(heading, inputRow, listHeading, categoryList);
        return form;
    }

    private static VBox createManufacturerForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Manufacturer");
        heading.getStyleClass().add("form-heading");

        // Input Fields
        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getStyleClass().add("form-input");

        ComboBox<String> districtBox = new ComboBox<>();
        districtBox.getStyleClass().add("form-input");

        ComboBox<String> tehsilBox = new ComboBox<>();
        tehsilBox.getStyleClass().add("form-input");

        // Load provinces from database
        if (config.database != null && config.database.isConnected()) {
            provinceBox.getItems().addAll(config.database.getAllProvinces());
        }

        // Province selection handler
        provinceBox.setOnAction(e -> {
            String selectedProvince = provinceBox.getValue();
            if (selectedProvince != null && config.database != null && config.database.isConnected()) {
                districtBox.getItems().clear();
                tehsilBox.getItems().clear();
                districtBox.getItems().addAll(config.database.getDistrictsByProvince(selectedProvince));
            }
        });

        // District selection handler
        districtBox.setOnAction(e -> {
            String selectedDistrict = districtBox.getValue();
            if (selectedDistrict != null && config.database != null && config.database.isConnected()) {
                tehsilBox.getItems().clear();
                tehsilBox.getItems().addAll(config.database.getTehsilsByDistrict(selectedDistrict));
            }
        });

        // Labels
        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("form-label");

        // Rows
        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        HBox locationRow = new HBox(20);
        locationRow.setAlignment(Pos.CENTER_LEFT);
        locationRow.getStyleClass().add("form-row");

        VBox provinceBoxWrap = new VBox(new Label("Province:"), provinceBox);
        provinceBoxWrap.getStyleClass().add("form-combo-wrap");

        VBox districtBoxWrap = new VBox(new Label("District:"), districtBox);
        districtBoxWrap.getStyleClass().add("form-combo-wrap");

        VBox tehsilBoxWrap = new VBox(new Label("Tehsil:"), tehsilBox);
        tehsilBoxWrap.getStyleClass().add("form-combo-wrap");

        locationRow.getChildren().addAll(provinceBoxWrap, districtBoxWrap, tehsilBoxWrap);

        // Submit and Delete Buttons
        Button submitBtn = new Button("Submit Manufacturer");
        submitBtn.getStyleClass().add("form-submit");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        HBox buttonRow = new HBox(10, submitBtn, deleteBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.getStyleClass().add("form-row");

        // Table of Existing Manufacturers
        Label tableHeading = new Label("Existing Manufacturers:");
        tableHeading.getStyleClass().add("form-subheading");

        TableView<Manufacturer> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Manufacturer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Manufacturer, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(data -> data.getValue().provinceProperty());

        TableColumn<Manufacturer, String> districtCol = new TableColumn<>("District");
        districtCol.setCellValueFactory(data -> data.getValue().districtProperty());

        TableColumn<Manufacturer, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setCellValueFactory(data -> data.getValue().tehsilProperty());

        table.getColumns().addAll(nameCol, provinceCol, districtCol, tehsilCol);
        
        // Load existing manufacturers from database
        if (config.database != null && config.database.isConnected()) {
            table.getItems().addAll(config.database.getAllManufacturers());
        }

        // Submit Action
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String province = provinceBox.getValue();
            String district = districtBox.getValue();
            String tehsil = tehsilBox.getValue();

            if (!name.isEmpty() && province != null && district != null && tehsil != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertManufacturer(name, province, district, tehsil)) {
                        Manufacturer m = new Manufacturer(name, province, district, tehsil);
                        table.getItems().add(m);
                        nameField.clear();
                        provinceBox.getSelectionModel().clearSelection();
                        districtBox.getSelectionModel().clearSelection();
                        tehsilBox.getSelectionModel().clearSelection();
                        showAlert("Success", "Manufacturer added successfully!");
                    } else {
                        showAlert("Error", "Failed to add manufacturer to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "All fields are required!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            Manufacturer selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteManufacturer(selected.nameProperty().get())) {
                        table.getItems().remove(selected);
                        showAlert("Success", "Manufacturer deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete manufacturer from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No manufacturer selected!");
            }
        });

        form.getChildren().addAll(
            heading,
            nameRow,
            locationRow,
            buttonRow,
            tableHeading,
            table
        );

        return form;
    }

    private static VBox createBrandForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Brand");
        heading.getStyleClass().add("form-heading");

        // Input Fields
        TextField brandField = new TextField();
        brandField.getStyleClass().add("form-input");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getStyleClass().add("form-input");

        ComboBox<String> districtBox = new ComboBox<>();
        districtBox.getStyleClass().add("form-input");

        ComboBox<String> tehsilBox = new ComboBox<>();
        tehsilBox.getStyleClass().add("form-input");

        // Load provinces from database
        if (config.database != null && config.database.isConnected()) {
            provinceBox.getItems().addAll(config.database.getAllProvinces());
        }

        // Province selection handler
        provinceBox.setOnAction(e -> {
            String selectedProvince = provinceBox.getValue();
            if (selectedProvince != null && config.database != null && config.database.isConnected()) {
                districtBox.getItems().clear();
                tehsilBox.getItems().clear();
                districtBox.getItems().addAll(config.database.getDistrictsByProvince(selectedProvince));
            }
        });

        // District selection handler
        districtBox.setOnAction(e -> {
            String selectedDistrict = districtBox.getValue();
            if (selectedDistrict != null && config.database != null && config.database.isConnected()) {
                tehsilBox.getItems().clear();
                tehsilBox.getItems().addAll(config.database.getTehsilsByDistrict(selectedDistrict));
            }
        });

        // Labels
        Label brandLabel = new Label("Brand Name:");
        brandLabel.getStyleClass().add("form-label");

        // Rows
        HBox brandRow = new HBox(10, brandLabel, brandField);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.getStyleClass().add("form-row");

        VBox provinceWrap = new VBox(new Label("Province:"), provinceBox);
        VBox districtWrap = new VBox(new Label("District:"), districtBox);
        VBox tehsilWrap = new VBox(new Label("Tehsil:"), tehsilBox);

        provinceWrap.getStyleClass().add("form-combo-wrap");
        districtWrap.getStyleClass().add("form-combo-wrap");
        tehsilWrap.getStyleClass().add("form-combo-wrap");

        HBox locationRow = new HBox(20, provinceWrap, districtWrap, tehsilWrap);
        locationRow.setAlignment(Pos.CENTER_LEFT);
        locationRow.getStyleClass().add("form-row");

        // Submit and Delete Buttons
        Button submitBtn = new Button("Submit Brand");
        submitBtn.getStyleClass().add("form-submit");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        HBox buttonRow = new HBox(10, submitBtn, deleteBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.getStyleClass().add("form-row");

        // Table Heading
        Label tableHeading = new Label("Existing Brands:");
        tableHeading.getStyleClass().add("form-subheading");

        // Table View
        TableView<Brand> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Brand, String> brandCol = new TableColumn<>("Brand Name");
        brandCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Brand, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(data -> data.getValue().provinceProperty());

        TableColumn<Brand, String> districtCol = new TableColumn<>("District");
        districtCol.setCellValueFactory(data -> data.getValue().districtProperty());

        TableColumn<Brand, String> tehsilCol = new TableColumn<>("Tehsil");
        tehsilCol.setCellValueFactory(data -> data.getValue().tehsilProperty());

        table.getColumns().addAll(brandCol, provinceCol, districtCol, tehsilCol);

        // Load existing brands from database
        if (config.database != null && config.database.isConnected()) {
            table.getItems().addAll(config.database.getAllBrands());
        }

        // Submit Action
        submitBtn.setOnAction(e -> {
            String name = brandField.getText().trim();
            String province = provinceBox.getValue();
            String district = districtBox.getValue();
            String tehsil = tehsilBox.getValue();

            if (!name.isEmpty() && province != null && district != null && tehsil != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertBrand(name, province, district, tehsil)) {
                        Brand brand = new Brand(name, province, district, tehsil);
                        table.getItems().add(brand);
                        brandField.clear();
                        provinceBox.getSelectionModel().clearSelection();
                        districtBox.getSelectionModel().clearSelection();
                        tehsilBox.getSelectionModel().clearSelection();
                        showAlert("Success", "Brand added successfully!");
                    } else {
                        showAlert("Error", "Failed to add brand to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "All fields are required!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            Brand selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteBrand(selected.nameProperty().get())) {
                        table.getItems().remove(selected);
                        showAlert("Success", "Brand deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete brand from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No brand selected!");
            }
        });

        form.getChildren().addAll(
            heading,
            brandRow,
            locationRow,
            buttonRow,
            tableHeading,
            table
        );

        return form;
    }

    private static VBox createProvinceForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Province");
        heading.getStyleClass().add("form-heading");

        Label provinceLabel = new Label("Province Name:");
        provinceLabel.getStyleClass().add("form-label");

        TextField provinceField = new TextField();
        provinceField.getStyleClass().add("form-input");

        Button submit = new Button("Submit Province");
        submit.getStyleClass().add("form-submit");

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        HBox row = new HBox(10, provinceLabel, provinceField, submit, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");

        // Province list
        Label listHeading = new Label("Registered Provinces:");
        listHeading.getStyleClass().add("form-subheading");
        ListView<String> provinceList = new ListView<>();
        provinceList.getStyleClass().add("category-list");
        provinceList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(provinceList, Priority.ALWAYS);

        // Load existing provinces from database
        if (config.database != null && config.database.isConnected()) {
            provinceList.getItems().addAll(config.database.getAllProvinces());
        }

        submit.setOnAction(e -> {
            String name = provinceField.getText().trim();
            if (!name.isEmpty()) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertProvince(name)) {
                        provinceList.getItems().add(name);
                        provinceField.clear();
                        showAlert("Success", "Province added successfully!");
                    } else {
                        showAlert("Error", "Failed to add province to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Province name cannot be empty!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            String selected = provinceList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteProvince(selected)) {
                        provinceList.getItems().remove(selected);
                        showAlert("Success", "Province deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete province from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No province selected!");
            }
        });

        form.getChildren().addAll(heading, row, listHeading, provinceList);
        return form;
    }

    private static VBox createDistrictForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register District");
        heading.getStyleClass().add("form-heading");

        // Labels
        Label nameLabel = new Label("District Name:");
        nameLabel.getStyleClass().add("form-label");

        Label provinceLabel = new Label("Province:");
        provinceLabel.getStyleClass().add("form-label");

        // Inputs
        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        ComboBox<String> provinceBox = new ComboBox<>();
        provinceBox.getStyleClass().add("form-input");

        // Load provinces from database
        if (config.database != null && config.database.isConnected()) {
            provinceBox.getItems().addAll(config.database.getAllProvinces());
        }

        // Submit and Delete Buttons
        Button submit = new Button("Submit District");
        submit.getStyleClass().add("form-submit");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        // Rows
        HBox row1 = new HBox(10, nameLabel, nameField);
        HBox row2 = new HBox(10, provinceLabel, provinceBox, submit, deleteBtn);

        for (HBox row : new HBox[]{row1, row2}) {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("form-row");
        }

        // District list
        Label listHeading = new Label("Registered Districts:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> districtList = new ListView<>();
        districtList.getStyleClass().add("category-list");
        districtList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(districtList, Priority.ALWAYS);

        // Load existing districts from database
        if (config.database != null && config.database.isConnected()) {
            List<String> districts = config.database.getAllDistricts();
            for (String district : districts) {
                districtList.getItems().add(district);
            }
        }

        // Submit action
        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            String province = provinceBox.getValue();
            if (!name.isEmpty() && province != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertDistrict(name, province)) {
                        String display = name + " (" + province + ")";
                        districtList.getItems().add(display);
                        nameField.clear();
                        provinceBox.getSelectionModel().clearSelection();
                        showAlert("Success", "District added successfully!");
                    } else {
                        showAlert("Error", "Failed to add district to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "District name and province are required!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            String selected = districtList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    String districtName = selected.split(" \\(")[0]; // Extract name before " (province)"
                    if (config.database.deleteDistrict(districtName)) {
                        districtList.getItems().remove(selected);
                        showAlert("Success", "District deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete district from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No district selected!");
            }
        });

        form.getChildren().addAll(heading, row1, row2, listHeading, districtList);
        return form;
    }

    private static VBox createTehsilForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Tehsil");
        heading.getStyleClass().add("form-heading");

        // Labels
        Label nameLabel = new Label("Tehsil Name:");
        nameLabel.getStyleClass().add("form-label");

        Label districtLabel = new Label("District:");
        districtLabel.getStyleClass().add("form-label");

        // Inputs
        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        ComboBox<String> districtBox = new ComboBox<>();
        districtBox.getStyleClass().add("form-input");

        // Load districts from database
        if (config.database != null && config.database.isConnected()) {
            districtBox.getItems().addAll(config.database.getAllDistricts());
        }

        // Submit and Delete Buttons
        Button submit = new Button("Submit Tehsil");
        submit.getStyleClass().add("form-submit");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        // Input rows
        HBox row1 = new HBox(10, nameLabel, nameField);
        HBox row2 = new HBox(10, districtLabel, districtBox, submit, deleteBtn);

        for (HBox row : new HBox[]{row1, row2}) {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("form-row");
        }

        // Tehsil list
        Label listHeading = new Label("Registered Tehsils:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> tehsilList = new ListView<>();
        tehsilList.getStyleClass().add("category-list");
        tehsilList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(tehsilList, Priority.ALWAYS);

        // Load existing tehsils from database
        if (config.database != null && config.database.isConnected()) {
            List<String> tehsils = config.database.getAllTehsils();
            for (String tehsil : tehsils) {
                tehsilList.getItems().add(tehsil);
            }
        }

        // Submit action
        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            String district = districtBox.getValue();
            if (!name.isEmpty() && district != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertTehsil(name, district)) {
                        String display = name + " (" + district + ")";
                        tehsilList.getItems().add(display);
                        nameField.clear();
                        districtBox.getSelectionModel().clearSelection();
                        showAlert("Success", "Tehsil added successfully!");
                    } else {
                        showAlert("Error", "Failed to add tehsil to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Tehsil name and district are required!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            String selected = tehsilList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    String tehsilName = selected.split(" \\(")[0]; // Extract name before " (district)"
                    if (config.database.deleteTehsil(tehsilName)) {
                        tehsilList.getItems().remove(selected);
                        showAlert("Success", "Tehsil deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete tehsil from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No tehsil selected!");
            }
        });

        form.getChildren().addAll(heading, row1, row2, listHeading, tehsilList);
        return form;
    }

    private static VBox createUnitForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Units");
        heading.getStyleClass().add("form-heading");

        Label nameLabel = new Label("Unit Name:");
        nameLabel.getStyleClass().add("form-label");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-input");

        Button submit = new Button("Submit Unit");
        submit.getStyleClass().add("form-submit");

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        HBox row = new HBox(10, nameLabel, nameField, submit, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("form-row");

        Label listHeading = new Label("Registered Units:");
        listHeading.getStyleClass().add("form-subheading");

        ListView<String> unitList = new ListView<>();
        unitList.getStyleClass().add("category-list");
        unitList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(unitList, Priority.ALWAYS);

        // Load existing units from database
        if (config.database != null && config.database.isConnected()) {
            unitList.getItems().addAll(config.database.getAllUnits());
        }

        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertUnit(name)) {
                        unitList.getItems().add(name);
                        nameField.clear();
                        showAlert("Success", "Unit added successfully!");
                    } else {
                        showAlert("Error", "Failed to add unit to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Unit name cannot be empty!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            String selected = unitList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteUnit(selected)) {
                        unitList.getItems().remove(selected);
                        showAlert("Success", "Unit deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete unit from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No unit selected!");
            }
        });

        form.getChildren().addAll(heading, row, listHeading, unitList);
        return form;
    }

    private static VBox createCustomerForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Customer");
        heading.getStyleClass().add("form-heading");

        // Input fields
        TextField nameField = new TextField();
        TextField contactField = new TextField();
        nameField.getStyleClass().add("form-input");
        contactField.getStyleClass().add("form-input");

        Label nameLabel = new Label("Customer Name:");
        Label contactLabel = new Label("Contact:");
        nameLabel.getStyleClass().add("form-label");
        contactLabel.getStyleClass().add("form-label");

        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        HBox contactRow = new HBox(10, contactLabel, contactField);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Customer");
        submitBtn.getStyleClass().add("form-submit");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        HBox buttonRow = new HBox(10, submitBtn, deleteBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.getStyleClass().add("form-row");

        Label tableHeading = new Label("Registered Customers:");
        tableHeading.getStyleClass().add("form-subheading");

        // Table view
        TableView<Customer> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Customer, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(data -> data.getValue().contactProperty());

        table.getColumns().addAll(nameCol, contactCol);

        // Load existing customers from database
        if (config.database != null && config.database.isConnected()) {
            table.getItems().addAll(config.database.getAllCustomers());
        }

        // Submit action
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (!name.isEmpty()) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertCustomer(name, contact)) {
                        Customer customer = new Customer(name, contact);
                        table.getItems().add(customer);
                        nameField.clear();
                        contactField.clear();
                        showAlert("Success", "Customer added successfully!");
                    } else {
                        showAlert("Error", "Failed to add customer to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Customer name cannot be empty!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteCustomer(selected.nameProperty().get())) {
                        table.getItems().remove(selected);
                        showAlert("Success", "Customer deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete customer from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No customer selected!");
            }
        });

        form.getChildren().addAll(
            heading,
            nameRow,
            contactRow,
            buttonRow,
            tableHeading,
            table
        );

        return form;
    }

    private static VBox createSupplierForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("form-container");

        Label heading = new Label("Register Supplier");
        heading.getStyleClass().add("form-heading");

        // Input fields
        TextField nameField = new TextField();
        TextField contactField = new TextField();
        nameField.getStyleClass().add("form-input");
        contactField.getStyleClass().add("form-input");

        Label nameLabel = new Label("Supplier Name:");
        Label contactLabel = new Label("Contact:");
        nameLabel.getStyleClass().add("form-label");
        contactLabel.getStyleClass().add("form-label");

        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getStyleClass().add("form-row");

        HBox contactRow = new HBox(10, contactLabel, contactField);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.getStyleClass().add("form-row");

        Button submitBtn = new Button("Submit Supplier");
        submitBtn.getStyleClass().add("form-submit");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("form-delete-button");

        HBox buttonRow = new HBox(10, submitBtn, deleteBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.getStyleClass().add("form-row");

        Label tableHeading = new Label("Registered Suppliers:");
        tableHeading.getStyleClass().add("form-subheading");

        // TableView for suppliers
        TableView<Supplier> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("category-list");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Supplier, String> nameCol = new TableColumn<>("Supplier Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Supplier, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(data -> data.getValue().contactProperty());

        table.getColumns().addAll(nameCol, contactCol);

        // Load existing suppliers from database
        if (config.database != null && config.database.isConnected()) {
            table.getItems().addAll(config.database.getAllSuppliers());
        }

        // Submit action
        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (!name.isEmpty()) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertSupplier(name, contact)) {
                        Supplier supplier = new Supplier(name, contact);
                        table.getItems().add(supplier);
                        nameField.clear();
                        contactField.clear();
                        showAlert("Success", "Supplier added successfully!");
                    } else {
                        showAlert("Error", "Failed to add supplier to database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Supplier name cannot be empty!");
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            Supplier selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteSupplier(selected.nameProperty().get())) {
                        table.getItems().remove(selected);
                        showAlert("Success", "Supplier deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete supplier from database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "No supplier selected!");
            }
        });

        form.getChildren().addAll(
            heading,
            nameRow,
            contactRow,
            buttonRow,
            tableHeading,
            table
        );

        return form;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}