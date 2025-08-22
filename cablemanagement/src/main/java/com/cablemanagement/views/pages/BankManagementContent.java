package com.cablemanagement.views.pages;

import com.cablemanagement.config;
import com.cablemanagement.model.Bank;
import com.cablemanagement.model.BankTransaction;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BankManagementContent {

    private static final ObservableList<Bank> registeredBanks = FXCollections.observableArrayList();
    private static final ObservableList<BankTransaction> bankTransactions = FXCollections.observableArrayList();
    private static final ObservableList<BankTransaction> cashTransactions = FXCollections.observableArrayList();
    
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Node get() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        StackPane contentArea = new StackPane();
        contentArea.getChildren().add(viewManageBanks());

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(buttonBar);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(72);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        addButton(buttonBar, "Manage Banks", () -> contentArea.getChildren().setAll(viewManageBanks()));
        addButton(buttonBar, "Bank Transactions", () -> contentArea.getChildren().setAll(viewTransactionSection()));
        addButton(buttonBar, "View Cash In Hand", () -> contentArea.getChildren().setAll(viewCashInHand()));
        addButton(buttonBar, "View Cash Ledger", () -> contentArea.getChildren().setAll(viewCashLedger()));

        mainLayout.setTop(scrollPane);
        mainLayout.setCenter(contentArea);

        // Load initial data
        loadBanksFromDatabase();
        loadTransactionsFromDatabase();

        return mainLayout;
    }

    private static void addButton(HBox bar, String label, Runnable action) {
        Button btn = new Button(label);
        btn.getStyleClass().add("register-button");
        btn.setOnAction(e -> action.run());
        bar.getChildren().add(btn);
    }

    private static void loadBanksFromDatabase() {
        registeredBanks.clear();
        if (config.database != null && config.database.isConnected()) {
            List<Object[]> bankRows = config.database.getAllBanks();
            List<Bank> banks = FXCollections.observableArrayList();
            for (Object[] row : bankRows) {
                // Columns: bank_id, bank_name, account_number, branch_name, balance
                int id = (int) row[0];
                String name = (String) row[1];
                String accNum = (String) row[2];
                String branch = (String) row[3];
                double balance = (double) row[4];
                // Bank constructor: (bankId, bankName, branchName, accountNumber, balance)
                banks.add(new Bank(id, name, branch, accNum, balance));
            }
            registeredBanks.addAll(banks);
        }
    }

    private static void loadTransactionsFromDatabase() {
    bankTransactions.clear();
    cashTransactions.clear();
    
    if (config.database != null && config.database.isConnected()) {
        try {
            // Load bank transactions
            List<Object[]> transactionRows = config.database.getAllBankTransactions();
            for (Object[] row : transactionRows) {
                try {
                    BankTransaction transaction = createTransactionFromRow(row, true);
                    if (transaction != null) {
                        bankTransactions.add(transaction);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing bank transaction row: " + e.getMessage());
                }
            }

            // Load cash transactions
            List<Object[]> cashRows = config.database.getAllCashTransactions();
            for (Object[] row : cashRows) {
                try {
                    BankTransaction transaction = createTransactionFromRow(row, false);
                    if (transaction != null) {
                        cashTransactions.add(transaction);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing cash transaction row: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
    }
}

private static BankTransaction createTransactionFromRow(Object[] row, boolean isBankTransaction) {
    if (row == null || row.length < 4) { // Minimum: date, type, amount, description
        System.err.println("Invalid row data - expected at least 4 columns, got " + 
                         (row == null ? "null" : row.length));
        return null;
    }

    try {
        if (isBankTransaction) {
            // Bank transaction format: [transaction_date, bank_name, transaction_type, amount, description]
            String date = row[0] != null ? row[0].toString() : LocalDate.now().toString();
            String bankName = row[1] != null ? row[1].toString() : "";
            String type = row[2] != null ? row[2].toString() : "unknown";
            double amount = safeParseDouble(row[3]);
            String description = row.length > 4 && row[4] != null ? row[4].toString() : "";
            
            // Find bank ID from bank name
            int bankId = findBankIdByName(bankName);
            System.out.println("Creating bank transaction - Bank Name: " + bankName + ", Found Bank ID: " + bankId);
            
            return new BankTransaction(0, bankId, date, type, amount, description, 0);
        } else {
            // Cash transaction format: [date, transaction_type, amount, description, source]
            String date = row[0] != null ? row[0].toString() : LocalDate.now().toString();
            String type = row[1] != null ? row[1].toString() : "unknown";
            double amount = safeParseDouble(row[2]);
            String description = row.length > 3 && row[3] != null ? row[3].toString() : "";
            
            // For cash transactions, bankId is 0
            int bankId = 0;
            int relatedBankId = 0;
            
            // If it's a transfer involving a bank, try to find the related bank
            if (type.equals("transfer_from_bank") || type.equals("transfer_to_bank")) {
                // You might need to parse the description or have additional logic to find the related bank
                relatedBankId = 0; // For now, set to 0 but could be enhanced
            }
            
            return new BankTransaction(0, bankId, date, type, amount, description, relatedBankId);
        }
    } catch (Exception e) {
        System.err.println("Error creating transaction from row: " + e.getMessage());
        return null;
    }
}

private static int findBankIdByName(String bankName) {
    for (Bank bank : registeredBanks) {
        if (bank.getBankName().equals(bankName)) {
            return bank.getBankId();
        }
    }
    return 0; // Return 0 if bank not found
}

private static double safeParseDouble(Object value) {
    if (value == null) return 0.0;
    try {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
        return 0.0;
    }
}

    private static VBox viewManageBanks() {
        VBox box = createSection("Manage Banks", "Register a new bank with full details and see the list.");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("text-field");
        TextField branchField = new TextField();
        branchField.getStyleClass().add("text-field");
        TextField accountNumberField = new TextField();
        accountNumberField.getStyleClass().add("text-field");


        Button registerBtn = new Button("Register Bank");
        registerBtn.getStyleClass().add("register-button");

        Button updateBtn = new Button("Update Bank");
        updateBtn.getStyleClass().add("update-button");
        updateBtn.setVisible(false);

        HBox row1 = new HBox(10,
                new VBox(new Label("Bank Name:"), nameField),
                new VBox(new Label("Branch:"), branchField)
        );

        HBox row2 = new HBox(10,
                new VBox(new Label("Account Number:"), accountNumberField)
        );

        HBox buttonRow = new HBox(10, registerBtn, updateBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        TableView<Bank> bankTable = new TableView<>(registeredBanks);
        bankTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bankTable.setPrefHeight(250);

        TableColumn<Bank, String> nameCol = new TableColumn<>("Bank Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("bankName"));

        TableColumn<Bank, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branchName"));

        TableColumn<Bank, String> accNumCol = new TableColumn<>("Account Number");
        accNumCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));


        TableColumn<Bank, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(String.format("Rs. %.2f", data.getValue().getBalance())));

        TableColumn<Bank, Void> editCol = new TableColumn<>("Actions");
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.getStyleClass().add("button");
                deleteButton.getStyleClass().add("delete-button");
                
                editButton.setOnAction(e -> {
                    Bank selected = getTableView().getItems().get(getIndex());
                    nameField.setText(selected.getBankName());
                    branchField.setText(selected.getBranchName());
                    accountNumberField.setText(selected.getAccountNumber());

                    registerBtn.setVisible(false);
                    updateBtn.setVisible(true);

                    updateBtn.setOnAction(ev -> {
                        selected.setBankName(nameField.getText().trim());
                        selected.setBranchName(branchField.getText().trim());
                        selected.setAccountNumber(accountNumberField.getText().trim());
                        
                        if (config.database != null && config.database.isConnected()) {
                            if (config.database.updateBank(selected)) {
                                bankTable.refresh();
                                nameField.clear();
                                branchField.clear();
                                accountNumberField.clear();
                                registerBtn.setVisible(true);
                                updateBtn.setVisible(false);
                                showAlert("Success", "Bank updated successfully!");
                            } else {
                                showAlert("Error", "Failed to update bank in database!");
                            }
                        }
                    });
                });

                deleteButton.setOnAction(e -> {
                    Bank selected = getTableView().getItems().get(getIndex());
                    if (config.database != null && config.database.isConnected()) {
                        if (config.database.deleteBank(selected.getBankId())) {
                            registeredBanks.remove(selected);
                            showAlert("Success", "Bank deleted successfully!");
                        } else {
                            showAlert("Error", "Failed to delete bank from database!");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        bankTable.getColumns().addAll(nameCol, branchCol, accNumCol, balanceCol, editCol);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String branch = branchField.getText().trim();
            String accNum = accountNumberField.getText().trim();

            if (!name.isEmpty() && !accNum.isEmpty()) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertBank(name, branch, accNum)) {
                        // Optionally, fetch the new bank's ID and balance from DB if needed
                        Bank newBank = new Bank(0, name, branch, accNum, 0.0);
                        registeredBanks.add(newBank);
                        nameField.clear();
                        branchField.clear();
                        accountNumberField.clear();
                        showAlert("Success", "Bank registered successfully!");
                    } else {
                        showAlert("Error", "Failed to register bank in database!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            } else {
                showAlert("Error", "Bank name and account number are required!");
            }
        });

        VBox form = new VBox(15, row1, row2, buttonRow);
        form.setPadding(new Insets(10));

        box.getChildren().addAll(form, new Label("Registered Banks:"), bankTable);
        return box;
    }

    private static VBox viewTransactionSection() {
        VBox box = createSection("Bank Transactions", "Select a bank and perform a financial action.");

        ComboBox<Bank> bankSelector = new ComboBox<>(registeredBanks);
        bankSelector.setPromptText("Select Bank");
        bankSelector.setPrefWidth(250);
        bankSelector.setConverter(new StringConverter<Bank>() {
            @Override
            public String toString(Bank bank) {
                return bank == null ? "" : bank.getBankName() + " (" + bank.getAccountNumber() + ")";
            }

            @Override
            public Bank fromString(String string) {
                return null;
            }
        });
        bankSelector.getStyleClass().add("combo-box");

        ComboBox<String> actionSelector = new ComboBox<>();
        actionSelector.getItems().addAll("Deposit", "Withdraw", "Transfer Bank to Bank", "Transfer Bank to Cash");
        actionSelector.setPromptText("Select Action");
        actionSelector.setPrefWidth(250);
        actionSelector.getStyleClass().add("combo-box");

        HBox selectionRow = new HBox(20,
                new VBox(new Label("Bank:"), bankSelector),
                new VBox(new Label("Action:"), actionSelector)
        );
        selectionRow.setPadding(new Insets(10, 0, 10, 0));
        selectionRow.setAlignment(Pos.CENTER_LEFT);

        VBox dynamicFormArea = new VBox(10);
        dynamicFormArea.setPadding(new Insets(10, 0, 0, 0));

        actionSelector.setOnAction(e -> {
            String selectedAction = actionSelector.getValue();
            dynamicFormArea.getChildren().clear();
            if (selectedAction == null) return;

            switch (selectedAction) {
                case "Deposit":
                    dynamicFormArea.getChildren().add(depositForm(bankSelector));
                    break;
                case "Withdraw":
                    dynamicFormArea.getChildren().add(withdrawForm(bankSelector));
                    break;
                case "Transfer Bank to Bank":
                    dynamicFormArea.getChildren().add(transferBankToBankForm());
                    break;
                case "Transfer Bank to Cash":
                    dynamicFormArea.getChildren().add(transferBankToCashForm(bankSelector));
                    break;
            }
        });

        box.getChildren().addAll(selectionRow, dynamicFormArea);
        return box;
    }

    private static Node depositForm(ComboBox<Bank> bankSelector) {
        TextField amountField = new TextField();
        amountField.getStyleClass().add("text-field");
        TextField descriptionField = new TextField();
        descriptionField.getStyleClass().add("text-field");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");

        Button submit = new Button("Deposit");
        submit.getStyleClass().add("button");

        submit.setOnAction(e -> {
            Bank selectedBank = bankSelector.getValue();
            String amountText = amountField.getText().trim();
            String description = descriptionField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (selectedBank != null && !amountText.isEmpty() && date != null) {
                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        showAlert("Error", "Amount must be greater than 0");
                        return;
                    }

                    BankTransaction transaction = new BankTransaction(
                            0, selectedBank.getBankId(), date.format(dateFormatter),
                            "deposit", amount, description, 0
                    );

                    if (config.database != null && config.database.isConnected()) {
                        if (config.database.insertBankTransaction(transaction)) {
                            // Update bank balance
                            selectedBank.setBalance(selectedBank.getBalance() + amount);
                            config.database.updateBank(selectedBank);
                            
                            bankTransactions.add(transaction);
                            amountField.clear();
                            descriptionField.clear();
                            showAlert("Success", "Deposit recorded successfully!");
                        } else {
                            showAlert("Error", "Failed to record deposit in database!");
                        }
                    } else {
                        showAlert("Error", "Database not connected!");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid amount format!");
                }
            } else {
                showAlert("Error", "Bank selection, amount and date are required!");
            }
        });

        return new VBox(10,
                new Label("Amount to Deposit:"), amountField,
                new Label("Description:"), descriptionField,
                new Label("Date:"), datePicker,
                submit
        );
    }

    private static Node withdrawForm(ComboBox<Bank> bankSelector) {
        TextField amountField = new TextField();
        amountField.getStyleClass().add("text-field");
        TextField descriptionField = new TextField();
        descriptionField.getStyleClass().add("text-field");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");

        Button submit = new Button("Withdraw");
        submit.getStyleClass().add("button");

        submit.setOnAction(e -> {
            Bank selectedBank = bankSelector.getValue();
            String amountText = amountField.getText().trim();
            String description = descriptionField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (selectedBank != null && !amountText.isEmpty() && date != null) {
                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        showAlert("Error", "Amount must be greater than 0");
                        return;
                    }

                    if (amount > selectedBank.getBalance()) {
                        showAlert("Error", "Insufficient funds in bank account!");
                        return;
                    }

                    BankTransaction transaction = new BankTransaction(
                            0, selectedBank.getBankId(), date.format(dateFormatter),
                            "withdraw", amount, description, 0
                    );

                    if (config.database != null && config.database.isConnected()) {
                        if (config.database.insertBankTransaction(transaction)) {
                            // Update bank balance
                            selectedBank.setBalance(selectedBank.getBalance() - amount);
                            config.database.updateBank(selectedBank);
                            
                            bankTransactions.add(transaction);
                            amountField.clear();
                            descriptionField.clear();
                            showAlert("Success", "Withdrawal recorded successfully!");
                        } else {
                            showAlert("Error", "Failed to record withdrawal in database!");
                        }
                    } else {
                        showAlert("Error", "Database not connected!");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid amount format!");
                }
            } else {
                showAlert("Error", "Bank selection, amount and date are required!");
            }
        });

        return new VBox(10,
                new Label("Amount to Withdraw:"), amountField,
                new Label("Description:"), descriptionField,
                new Label("Date:"), datePicker,
                submit
        );
    }

    private static Node transferBankToBankForm() {
        ComboBox<Bank> fromBank = new ComboBox<>(registeredBanks);
        ComboBox<Bank> toBank = new ComboBox<>(registeredBanks);
        fromBank.getStyleClass().add("combo-box");
        toBank.getStyleClass().add("combo-box");
        fromBank.setConverter(new StringConverter<Bank>() {
            @Override
            public String toString(Bank bank) {
                return bank == null ? "" : bank.getBankName() + " (" + bank.getAccountNumber() + ")";
            }

            @Override
            public Bank fromString(String string) {
                return null;
            }
        });
        toBank.setConverter(new StringConverter<Bank>() {
            @Override
            public String toString(Bank bank) {
                return bank == null ? "" : bank.getBankName() + " (" + bank.getAccountNumber() + ")";
            }

            @Override
            public Bank fromString(String string) {
                return null;
            }
        });

        TextField amount = new TextField();
        amount.getStyleClass().add("text-field");
        TextField description = new TextField();
        description.getStyleClass().add("text-field");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");

        Button submit = new Button("Transfer");
        submit.getStyleClass().add("button");

        submit.setOnAction(e -> {
            Bank from = fromBank.getValue();
            Bank to = toBank.getValue();
            String amountText = amount.getText().trim();
            String desc = description.getText().trim();
            LocalDate date = datePicker.getValue();

            if (from != null && to != null && !amountText.isEmpty() && date != null) {
                if (from.getBankName().equals(to.getBankName()) && from.getAccountNumber().equals(to.getAccountNumber())) {
                    showAlert("Error", "Cannot transfer to the same bank account!");
                    return;
                }

                try {
                    double transferAmount = Double.parseDouble(amountText);
                    if (transferAmount <= 0) {
                        showAlert("Error", "Amount must be greater than 0");
                        return;
                    }

                    if (transferAmount > from.getBalance()) {
                        showAlert("Error", "Insufficient funds in source account!");
                        return;
                    }

                    if (config.database != null && config.database.isConnected()) {
                        // Create transaction for source account (transfer_out)
                        BankTransaction outTransaction = new BankTransaction(
                                0, from.getBankId(), date.format(dateFormatter),
                                "transfer_out", transferAmount, desc, to.getBankId()
                        );

                        // Create transaction for destination account (transfer_in)
                        BankTransaction inTransaction = new BankTransaction(
                                0, to.getBankId(), date.format(dateFormatter),
                                "transfer_in", transferAmount, desc, from.getBankId()
                        );

                        if (config.database.insertBankTransaction(outTransaction) && 
                            config.database.insertBankTransaction(inTransaction)) {
                            
                            // Update balances
                            from.setBalance(from.getBalance() - transferAmount);
                            to.setBalance(to.getBalance() + transferAmount);
                            
                            config.database.updateBank(from);
                            config.database.updateBank(to);
                            
                            bankTransactions.add(outTransaction);
                            bankTransactions.add(inTransaction);
                            
                            amount.clear();
                            description.clear();
                            fromBank.getSelectionModel().clearSelection();
                            toBank.getSelectionModel().clearSelection();
                            
                            showAlert("Success", "Transfer completed successfully!");
                        } else {
                            showAlert("Error", "Failed to record transfer in database!");
                        }
                    } else {
                        showAlert("Error", "Database not connected!");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid amount format!");
                }
            } else {
                showAlert("Error", "All fields are required!");
            }
        });

        return new VBox(10,
                new Label("From Bank:"), fromBank,
                new Label("To Bank:"), toBank,
                new Label("Amount:"), amount,
                new Label("Description:"), description,
                new Label("Date:"), datePicker,
                submit
        );
    }

    private static Node transferBankToCashForm(ComboBox<Bank> bankSelector) {
        TextField amountField = new TextField();
        amountField.getStyleClass().add("text-field");
        TextField descriptionField = new TextField();
        descriptionField.getStyleClass().add("text-field");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");

        Button submit = new Button("Transfer to Cash");
        submit.getStyleClass().add("button");

        submit.setOnAction(e -> {
            Bank selectedBank = bankSelector.getValue();
            String amountText = amountField.getText().trim();
            String description = descriptionField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (selectedBank != null && !amountText.isEmpty() && date != null) {
                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        showAlert("Error", "Amount must be greater than 0");
                        return;
                    }

                    if (amount > selectedBank.getBalance()) {
                        showAlert("Error", "Insufficient funds in bank account!");
                        return;
                    }

                    // Create bank transaction (transfer_out)
                    BankTransaction bankTransaction = new BankTransaction(
                            0, selectedBank.getBankId(), date.format(dateFormatter),
                            "transfer_out", amount, description, 0
                    );

                    // Create cash transaction (transfer_from_bank)
                    BankTransaction cashTransaction = new BankTransaction(
                            0, 0, date.format(dateFormatter),
                            "transfer_from_bank", amount, description, selectedBank.getBankId()
                    );

                    if (config.database != null && config.database.isConnected()) {
                        if (config.database.insertBankTransaction(bankTransaction) && 
                            config.database.insertCashTransaction(cashTransaction)) {
                            
                            // Update bank balance
                            selectedBank.setBalance(selectedBank.getBalance() - amount);
                            config.database.updateBank(selectedBank);
                            
                            bankTransactions.add(bankTransaction);
                            cashTransactions.add(cashTransaction);
                            
                            amountField.clear();
                            descriptionField.clear();
                            showAlert("Success", "Transfer to cash completed successfully!");
                        } else {
                            showAlert("Error", "Failed to record transfer in database!");
                        }
                    } else {
                        showAlert("Error", "Database not connected!");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid amount format!");
                }
            } else {
                showAlert("Error", "Bank selection, amount and date are required!");
            }
        });

        return new VBox(10,
                new Label("Amount to Convert:"), amountField,
                new Label("Description:"), descriptionField,
                new Label("Date:"), datePicker,
                submit
        );
    }

    private static VBox viewCashInHand() {
        VBox box = createSection("Cash In Hand Management", "Manage cash transactions - Add, Update, or Remove cash entries.");

        // Current Cash Balance Display
        double cashBalance = calculateCashBalance();
        Label balanceLabel = new Label(String.format("Current Cash Balance: Rs. %.2f", cashBalance));
        balanceLabel.getStyleClass().add("amount-label");
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #007bff;");

        // Cash Transaction Form
        VBox transactionForm = createCashTransactionForm(balanceLabel);
        
     

        box.getChildren().addAll(balanceLabel, transactionForm );
        return box;
    }

    private static VBox createCashTransactionForm(Label balanceLabel) {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("form-container");
        form.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

        Label formHeading = new Label("Cash Transaction Entry");
        formHeading.setStyle("-fx-font-size: 18px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        // Transaction Type Selection
        ComboBox<String> transactionType = new ComboBox<>();
        transactionType.getItems().addAll("Cash In", "Cash Out", "Cash Adjustment");
        transactionType.setPromptText("Select Transaction Type");
        transactionType.setPrefWidth(200);
        transactionType.getStyleClass().add("combo-box");

        // Amount Field
        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount");
        amountField.getStyleClass().add("text-field");

        // Description Field
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Enter Description/Purpose");
        descriptionField.getStyleClass().add("text-field");

        // Date Picker
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");

        // Buttons
        Button addBtn = new Button("Add Transaction");
        addBtn.getStyleClass().add("register-button");
        
        Button updateBtn = new Button("Update Transaction");
        updateBtn.getStyleClass().add("update-button");
        updateBtn.setVisible(false);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setVisible(false);

        HBox buttonRow = new HBox(10, addBtn, updateBtn, cancelBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        // Form Layout
        HBox row1 = new HBox(15,
            new VBox(5, new Label("Transaction Type:"), transactionType),
            new VBox(5, new Label("Amount:"), amountField)
        );
        
        HBox row2 = new HBox(15,
            new VBox(5, new Label("Description:"), descriptionField),
            new VBox(5, new Label("Date:"), datePicker)
        );

        form.getChildren().addAll(formHeading, row1, row2, buttonRow);

        // Add Transaction Action
        addBtn.setOnAction(e -> {
            if (validateCashTransactionForm(transactionType, amountField, descriptionField, datePicker)) {
                String type = transactionType.getValue().toLowerCase().replace(" ", "_");
                double amount = Double.parseDouble(amountField.getText().trim());
                String description = descriptionField.getText().trim();
                String date = datePicker.getValue().format(dateFormatter);

                BankTransaction transaction = new BankTransaction(
                    0, 0, date, type, amount, description, 0
                );

                if (config.database != null && config.database.isConnected()) {
                    if (config.database.insertCashTransaction(transaction)) {
                        // Update balance display
                        double newBalance = calculateCashBalance();
                        balanceLabel.setText(String.format("Current Cash Balance: Rs. %.2f", newBalance));
                        
                        // Clear form
                        clearCashTransactionForm(transactionType, amountField, descriptionField, datePicker);
                        
                        // Refresh table if it exists
                        refreshCashTransactionsTable();
                        
                        showAlert("Success", "Cash transaction added successfully!");
                    } else {
                        showAlert("Error", "Failed to add cash transaction!");
                    }
                } else {
                    showAlert("Error", "Database not connected!");
                }
            }
        });

        // Cancel Action
        cancelBtn.setOnAction(e -> {
            clearCashTransactionForm(transactionType, amountField, descriptionField, datePicker);
            addBtn.setVisible(true);
            updateBtn.setVisible(false);
            cancelBtn.setVisible(false);
        });

        return form;
    }

    private static boolean validateCashTransactionForm(ComboBox<String> type, TextField amount, 
                                                     TextField description, DatePicker date) {
        if (type.getValue() == null) {
            showAlert("Validation Error", "Please select a transaction type!");
            return false;
        }

        String amountText = amount.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Validation Error", "Please enter an amount!");
            return false;
        }

        try {
            double amountValue = Double.parseDouble(amountText);
            if (amountValue <= 0) {
                showAlert("Validation Error", "Amount must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid amount!");
            return false;
        }

        if (description.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a description!");
            return false;
        }

        if (date.getValue() == null) {
            showAlert("Validation Error", "Please select a date!");
            return false;
        }

        return true;
    }

    private static void clearCashTransactionForm(ComboBox<String> type, TextField amount, 
                                               TextField description, DatePicker date) {
        type.setValue(null);
        amount.clear();
        description.clear();
        date.setValue(LocalDate.now());
    }

    private static void editCashTransaction(BankTransaction transaction) {
        // Create edit dialog
        Dialog<BankTransaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Cash Transaction");
        dialog.setHeaderText("Modify the transaction details");

        // Create form fields
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Cash In", "Cash Out", "Cash Adjustment");
        String currentType = transaction.getTransactionType().replace("_", " ");
        typeCombo.setValue(currentType.substring(0, 1).toUpperCase() + currentType.substring(1));

        TextField amountField = new TextField(String.valueOf(transaction.getAmount()));
        TextField descField = new TextField(transaction.getDescription());
        DatePicker datePicker = new DatePicker(LocalDate.parse(transaction.getTransactionDate()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String newType = typeCombo.getValue().toLowerCase().replace(" ", "_");
                    double newAmount = Double.parseDouble(amountField.getText().trim());
                    String newDesc = descField.getText().trim();
                    String newDate = datePicker.getValue().format(dateFormatter);

                    transaction.setTransactionType(newType);
                    transaction.setAmount(newAmount);
                    transaction.setDescription(newDesc);
                    transaction.setTransactionDate(newDate);

                    return transaction;
                } catch (Exception e) {
                    showAlert("Error", "Invalid input data!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedTransaction -> {
            if (config.database != null && config.database.isConnected()) {
                if (config.database.updateCashTransaction(updatedTransaction)) {
                    // Refresh the cash transactions by reloading the cash in hand view
                    showAlert("Success", "Transaction updated successfully!");
                } else {
                    showAlert("Error", "Failed to update transaction!");
                }
            }
        });
    }

    private static void deleteCashTransaction(BankTransaction transaction, TableView<BankTransaction> table) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        alert.setHeaderText("Are you sure you want to delete this transaction?");
        alert.setContentText(String.format("Type: %s\nAmount: Rs. %.2f\nDescription: %s", 
            transaction.getTransactionType(), transaction.getAmount(), transaction.getDescription()));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (config.database != null && config.database.isConnected()) {
                    if (config.database.deleteCashTransaction(transaction.getTransactionId())) {
                        table.getItems().remove(transaction);
                        showAlert("Success", "Transaction deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete transaction!");
                    }
                }
            }
        });
    }

    private static void loadCashTransactionsTable(TableView<BankTransaction> table) {
        ObservableList<BankTransaction> transactions = FXCollections.observableArrayList();
        
        if (config.database != null && config.database.isConnected()) {
            try {
                List<Object[]> cashRows = config.database.getAllCashTransactions();
                for (Object[] row : cashRows) {
                    BankTransaction transaction = createTransactionFromRow(row, false);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading cash transactions: " + e.getMessage());
            }
        }
        
        table.setItems(transactions);
    }

    private static void refreshCashTransactionsTable() {
        // This method can be called to refresh any existing cash transactions table
        // Since we're using a modular approach, the table will be refreshed when the view is reloaded
        cashTransactions.clear();
        loadTransactionsFromDatabase();
    }

    private static double calculateCashBalance() {
        double balance = 0.0;
        if (config.database != null && config.database.isConnected()) {
            balance = config.database.getCashBalance();
        }
        return balance;
    }

private static VBox viewCashLedger() {
    VBox box = createSection("Cash Ledger", "Complete record of all cash and bank transactions");
    
    // Filter controls
    TextField searchField = new TextField();
    searchField.setPromptText("Search by description...");
    searchField.setPrefWidth(250);
    searchField.getStyleClass().add("text-field");
    
    ComboBox<String> sourceFilter = new ComboBox<>();
    sourceFilter.getItems().addAll("All Sources", "Cash Only", "Bank Only");
    sourceFilter.setValue("All Sources");
    sourceFilter.setPrefWidth(150);
    sourceFilter.getStyleClass().add("combo-box");
    
    ComboBox<String> typeFilter = new ComboBox<>();
    typeFilter.getItems().addAll("All Types", "Cash In", "Cash Out", "Transfer", "Deposit", "Withdraw");
    typeFilter.setValue("All Types");
    typeFilter.setPrefWidth(150);
    typeFilter.getStyleClass().add("combo-box");
    
    DatePicker fromDatePicker = new DatePicker();
    fromDatePicker.setPromptText("From Date");
    fromDatePicker.setPrefWidth(150);
    
    DatePicker toDatePicker = new DatePicker();
    toDatePicker.setPromptText("To Date");
    toDatePicker.setPrefWidth(150);
    
    Button resetButton = new Button("Reset Filters");
    resetButton.getStyleClass().add("button");
    
    HBox filterRow1 = new HBox(10, 
        new VBox(5, new Label("Search:"), searchField),
        new VBox(5, new Label("Source:"), sourceFilter),
        new VBox(5, new Label("Type:"), typeFilter)
    );
    
    HBox filterRow2 = new HBox(10, 
        new VBox(5, new Label("From Date:"), fromDatePicker),
        new VBox(5, new Label("To Date:"), toDatePicker),
        new VBox(5, new Label(" "), resetButton)
    );
    
    VBox filterContainer = new VBox(10, filterRow1, filterRow2);
    filterContainer.setPadding(new Insets(0, 0, 20, 0));
    
    // Transaction table
    TableView<BankTransaction> ledgerTable = new TableView<>();
    ledgerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Date Column
    TableColumn<BankTransaction, String> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(cell -> {
        String dateStr = cell.getValue().getTransactionDate();
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return new ReadOnlyStringWrapper(date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        } catch (Exception e) {
            return new ReadOnlyStringWrapper(dateStr); // Fallback to raw string
        }
    });

    // Bank In Column - Shows the bank where money is coming from or being received
    TableColumn<BankTransaction, String> bankInCol = new TableColumn<>("Bank In");
    bankInCol.setCellValueFactory(cell -> {
        BankTransaction txn = cell.getValue();
        int bankId = txn.getBankId();
        int relatedBankId = txn.getRelatedBankId();
        String type = txn.getTransactionType().toLowerCase();
        
        System.out.println("DEBUG Bank In - Type: " + type + ", BankId: " + bankId + ", RelatedBankId: " + relatedBankId);
        
        switch (type) {
            case "deposit":
            case "transfer_in":
            case "transfer_from_bank":
                // Money is coming INTO this bank
                Bank bank = findBankById(bankId);
                if (bank != null) {
                    System.out.println("DEBUG Bank In - Found bank: " + bank.getBankName());
                    return new ReadOnlyStringWrapper(bank.getBankName());
                } else if (type.equals("transfer_from_bank") && relatedBankId > 0) {
                    // For cash transactions from bank, show the source bank
                    Bank sourceBank = findBankById(relatedBankId);
                    String result = sourceBank != null ? sourceBank.getBankName() : "-";
                    System.out.println("DEBUG Bank In - Transfer from bank, result: " + result);
                    return new ReadOnlyStringWrapper(result);
                }
                System.out.println("DEBUG Bank In - No bank found, returning -");
                return new ReadOnlyStringWrapper("-");
                
            case "cash_in":
                // Pure cash transaction, no bank involved
                System.out.println("DEBUG Bank In - Cash in, returning -");
                return new ReadOnlyStringWrapper("-");
                
            default:
                System.out.println("DEBUG Bank In - Default case, returning -");
                return new ReadOnlyStringWrapper("-");
        }
    });

    // Bank Out Column - Shows the bank where money is going out from
    TableColumn<BankTransaction, String> bankOutCol = new TableColumn<>("Bank Out");
    bankOutCol.setCellValueFactory(cell -> {
        BankTransaction txn = cell.getValue();
        int bankId = txn.getBankId();
        int relatedBankId = txn.getRelatedBankId();
        String type = txn.getTransactionType().toLowerCase();
        
        System.out.println("DEBUG Bank Out - Type: " + type + ", BankId: " + bankId + ", RelatedBankId: " + relatedBankId);
        
        switch (type) {
            case "withdraw":
            case "transfer_out":
            case "transfer_to_bank":
                // Money is going OUT from this bank
                Bank bank = findBankById(bankId);
                if (bank != null) {
                    System.out.println("DEBUG Bank Out - Found bank: " + bank.getBankName());
                    return new ReadOnlyStringWrapper(bank.getBankName());
                } else if (type.equals("transfer_to_bank") && relatedBankId > 0) {
                    // For cash transactions to bank, show the destination bank
                    Bank destBank = findBankById(relatedBankId);
                    String result = destBank != null ? destBank.getBankName() : "-";
                    System.out.println("DEBUG Bank Out - Transfer to bank, result: " + result);
                    return new ReadOnlyStringWrapper(result);
                }
                System.out.println("DEBUG Bank Out - No bank found, returning -");
                return new ReadOnlyStringWrapper("-");
                
            case "cash_out":
                // Pure cash transaction, no bank involved
                System.out.println("DEBUG Bank Out - Cash out, returning -");
                return new ReadOnlyStringWrapper("-");
                
            default:
                System.out.println("DEBUG Bank Out - Default case, returning -");
                return new ReadOnlyStringWrapper("-");
        }
    });

    // Type Column
    TableColumn<BankTransaction, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(cell -> {
        String type = cell.getValue().getTransactionType().toLowerCase();
        
        // Map internal transaction type names to user-friendly display names
        switch (type) {
            case "deposit": 
                return new ReadOnlyStringWrapper("Deposit");
            case "withdraw": 
                return new ReadOnlyStringWrapper("Withdraw");
            case "transfer_in": 
                return new ReadOnlyStringWrapper("Transfer (In)");
            case "transfer_out": 
                return new ReadOnlyStringWrapper("Transfer (Out)");
            case "transfer_to_bank": 
                return new ReadOnlyStringWrapper("Transfer to Bank");
            case "transfer_from_bank": 
                return new ReadOnlyStringWrapper("Transfer from Bank");
            case "cash_in": 
                return new ReadOnlyStringWrapper("Cash In");
            case "cash_out": 
                return new ReadOnlyStringWrapper("Cash Out");
            default:
                // Fallback: Convert underscore to spaces and capitalize
                type = type.replace("_", " ");
                return new ReadOnlyStringWrapper(type.substring(0, 1).toUpperCase() + type.substring(1));
        }
    });

    // Amount Column
    TableColumn<BankTransaction, String> amountCol = new TableColumn<>("Amount");
    amountCol.setCellValueFactory(cell -> 
        new ReadOnlyStringWrapper(String.format("Rs. %,.2f", cell.getValue().getAmount())));

    // Description Column
    TableColumn<BankTransaction, String> descCol = new TableColumn<>("Description");
    descCol.setCellValueFactory(cell -> 
        new ReadOnlyStringWrapper(cell.getValue().getDescription()));

    ledgerTable.getColumns().addAll(dateCol, bankInCol, bankOutCol, typeCol, amountCol, descCol);
    
    // Load all transactions (bank and cash)
    ObservableList<BankTransaction> allTransactions = FXCollections.observableArrayList();
    
    // Make sure we have loaded the bank data
    if (registeredBanks.isEmpty()) {
        loadBanksFromDatabase();
    }
    
    // Add transactions to the combined list
    allTransactions.addAll(bankTransactions);
    allTransactions.addAll(cashTransactions);
    
    // Sort by date (newest first)
    allTransactions.sort((t1, t2) -> {
        try {
            LocalDate d1 = LocalDate.parse(t1.getTransactionDate());
            LocalDate d2 = LocalDate.parse(t2.getTransactionDate());
            return d2.compareTo(d1); // Descending order
        } catch (Exception e) {
            return 0;
        }
    });
    
    // Set the full dataset
    ledgerTable.setItems(allTransactions);
    
    // Create filtered list for search and filtering
    FilteredList<BankTransaction> filteredData = new FilteredList<>(allTransactions, p -> true);
    
    // Add listeners to all filter controls
    searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters(
        filteredData, searchField.getText(), sourceFilter.getValue(), 
        typeFilter.getValue(), fromDatePicker.getValue(), toDatePicker.getValue()
    ));
    
    sourceFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters(
        filteredData, searchField.getText(), sourceFilter.getValue(), 
        typeFilter.getValue(), fromDatePicker.getValue(), toDatePicker.getValue()
    ));
    
    typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters(
        filteredData, searchField.getText(), sourceFilter.getValue(), 
        typeFilter.getValue(), fromDatePicker.getValue(), toDatePicker.getValue()
    ));
    
    fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters(
        filteredData, searchField.getText(), sourceFilter.getValue(), 
        typeFilter.getValue(), fromDatePicker.getValue(), toDatePicker.getValue()
    ));
    
    toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters(
        filteredData, searchField.getText(), sourceFilter.getValue(), 
        typeFilter.getValue(), fromDatePicker.getValue(), toDatePicker.getValue()
    ));
    
    // Reset button action
    resetButton.setOnAction(e -> {
        searchField.clear();
        sourceFilter.setValue("All Sources");
        typeFilter.setValue("All Types");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        
        // Explicitly reapply filters to reset the table view
        applyFilters(
            filteredData, null, "All Sources", "All Types", null, null
        );
    });
    
    // Connect filtered list to table
    ledgerTable.setItems(filteredData);
    
    box.getChildren().addAll(filterContainer, ledgerTable);
    return box;
}

// Helper method for applying filters to the transaction list
private static void applyFilters(FilteredList<BankTransaction> filteredData, String searchText, 
                               String sourceFilter, String typeFilter, 
                               LocalDate fromDate, LocalDate toDate) {
    filteredData.setPredicate(transaction -> {
        // If all filters are empty or default, show all items
        boolean matchesSearch = searchText == null || searchText.isEmpty() || 
                transaction.getDescription().toLowerCase().contains(searchText.toLowerCase());
        
        // Source filtering
        boolean matchesSource = true;
        if (sourceFilter != null && !sourceFilter.equals("All Sources")) {
            String txnType = transaction.getTransactionType().toLowerCase();
            int bankId = transaction.getBankId();
            
            if (sourceFilter.equals("Cash Only")) {
                // Cash source: bankId = 0 OR transaction type is transfer_from_bank or contains "cash"
                matchesSource = bankId == 0 || txnType.equals("transfer_from_bank") || txnType.contains("cash");
                
                // Special case: Exclude bank transactions even if they have a cash-related type
                if (bankId > 0 && !txnType.equals("transfer_to_cash") && !txnType.equals("transfer_from_bank")) {
                    matchesSource = false;
                }
            } else if (sourceFilter.equals("Bank Only")) {
                // Bank source: bankId > 0 OR transaction type is bank-related
                matchesSource = bankId > 0 || txnType.equals("deposit") || txnType.equals("withdraw") ||
                               txnType.contains("transfer") && !txnType.contains("cash");
                
                // Special case: Exclude cash transactions even if they have a bank-related type
                if (bankId == 0 && !txnType.equals("transfer_to_bank") && !txnType.contains("bank")) {
                    matchesSource = false;
                }
            }
        }
        
        // Type filtering
        boolean matchesType = true;
        if (typeFilter != null && !typeFilter.equals("All Types")) {
            String txnType = transaction.getTransactionType().toLowerCase();
            
            switch (typeFilter) {
                case "Cash In":
                    matchesType = txnType.equals("cash_in") || txnType.equals("transfer_from_bank");
                    break;
                case "Cash Out":
                    matchesType = txnType.equals("cash_out") || txnType.equals("transfer_to_bank");
                    break;
                case "Transfer":
                    matchesType = txnType.contains("transfer");
                    break;
                case "Deposit":
                    matchesType = txnType.equals("deposit");
                    break;
                case "Withdraw":
                    matchesType = txnType.equals("withdraw");
                    break;
                default:
                    matchesType = true;
            }
        }
        
        // Date filtering
        boolean matchesDateRange = true;
        try {
            LocalDate txnDate = LocalDate.parse(transaction.getTransactionDate());
            if (fromDate != null && txnDate.isBefore(fromDate)) {
                matchesDateRange = false;
            }
            if (toDate != null && txnDate.isAfter(toDate)) {
                matchesDateRange = false;
            }
        } catch (Exception e) {
            // If date parsing fails, keep the record
        }
        
        return matchesSearch && matchesSource && matchesType && matchesDateRange;
    });
}
// Method removed as it's not used
    private static Bank findBankById(int bankId) {
        for (Bank bank : registeredBanks) {
            if (bank.getBankId() == bankId) {
                return bank;
            }
        }
        return null;
    }

    private static VBox createSection(String title, String description) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_LEFT);
        box.getStyleClass().add("form-container");

        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 20px; -fx-text-fill: #007bff; -fx-font-weight: bold;");

        Label note = new Label(description);
        note.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        box.getChildren().addAll(heading, note);
        return box;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}