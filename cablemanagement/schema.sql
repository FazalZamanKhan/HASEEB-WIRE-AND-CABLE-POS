-- Province table
CREATE TABLE IF NOT EXISTS Province (
    province_id INTEGER PRIMARY KEY AUTOINCREMENT,
    province_name TEXT NOT NULL UNIQUE
);

-- District table
CREATE TABLE IF NOT EXISTS District (
    district_id INTEGER PRIMARY KEY AUTOINCREMENT,
    district_name TEXT NOT NULL,
    province_id INTEGER NOT NULL,
    FOREIGN KEY (province_id) REFERENCES Province(province_id)
);

-- Tehsil table
CREATE TABLE IF NOT EXISTS Tehsil (
    tehsil_id INTEGER PRIMARY KEY AUTOINCREMENT,
    tehsil_name TEXT NOT NULL,
    district_id INTEGER NOT NULL,
    FOREIGN KEY (district_id) REFERENCES District(district_id)
);

-- Category table
CREATE TABLE IF NOT EXISTS Category (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_name TEXT NOT NULL UNIQUE
);

-- Designation table
CREATE TABLE IF NOT EXISTS Designation (
    designation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    designation_title TEXT NOT NULL UNIQUE
);

-- Manufacturer table
CREATE TABLE IF NOT EXISTS Manufacturer (
    manufacturer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    manufacturer_name TEXT NOT NULL,
    tehsil_id INTEGER NOT NULL,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);

-- Brand table
CREATE TABLE IF NOT EXISTS Brand (
    brand_id INTEGER PRIMARY KEY AUTOINCREMENT,
    brand_name TEXT NOT NULL,
    manufacturer_id INTEGER NOT NULL,
    FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(manufacturer_id)
);

-- Customer table
CREATE TABLE IF NOT EXISTS Customer (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    contact_number TEXT,
    address TEXT,
    tehsil_id INTEGER NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);

-- Supplier table
CREATE TABLE IF NOT EXISTS Supplier (
    supplier_id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_name TEXT NOT NULL,
    contact_number TEXT,
    address TEXT,
    tehsil_id INTEGER NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (tehsil_id) REFERENCES Tehsil(tehsil_id)
);

-- Bank table
CREATE TABLE IF NOT EXISTS Bank (
    bank_id INTEGER PRIMARY KEY,
    bank_name TEXT NOT NULL,
    account_number TEXT,
    branch_name TEXT,
    balance REAL DEFAULT 0.0
);

-- Employee table
CREATE TABLE IF NOT EXISTS Employee (
    employee_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_name TEXT NOT NULL,
    phone_number TEXT,
    cnic TEXT,
    address TEXT,
    hire_date TEXT NOT NULL,
    designation_id INTEGER NOT NULL,
    salary_type TEXT NOT NULL CHECK(salary_type IN ('monthly', 'daily', 'hourly', 'task')),
    salary_amount REAL NOT NULL,
    is_active INTEGER DEFAULT 1,
    FOREIGN KEY (designation_id) REFERENCES Designation(designation_id)
);

-- Employee Attendance table
CREATE TABLE IF NOT EXISTS Employee_Attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    attendance_date TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('present', 'absent', 'leave')),
    working_hours REAL DEFAULT 0,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Employee Advance Salary table
CREATE TABLE IF NOT EXISTS Employee_Advance_Salary (
    advance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    advance_date TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'granted' CHECK(status IN ('granted', 'adjusted', 'refunded')),
    created_date TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Employee Loan table
CREATE TABLE IF NOT EXISTS Employee_Loan (
    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    loan_amount REAL NOT NULL,
    loan_date TEXT NOT NULL,
    due_date TEXT,
    description TEXT,
    status TEXT DEFAULT 'active' CHECK(status IN ('active', 'paid', 'defaulted', 'written_off')),
    remaining_amount REAL NOT NULL,
    created_date TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Salesman table
CREATE TABLE IF NOT EXISTS Salesman (
    salesman_id INTEGER PRIMARY KEY AUTOINCREMENT,
    salesman_name TEXT NOT NULL,
    contact_number TEXT,
    address TEXT,
    commission_rate REAL DEFAULT 0.0
);

-- Raw Stock table
CREATE TABLE IF NOT EXISTS Raw_Stock (
    stock_id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    manufacturer_id INTEGER NOT NULL,
    brand_id INTEGER NOT NULL,
    unit_id INTEGER NOT NULL, 
    quantity INTEGER NOT NULL,
    unit_price REAL NOT NULL,
    total_cost REAL NOT NULL,
    supplier_id INTEGER,
    purchase_date TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Category(category_id),
    FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(manufacturer_id),
    FOREIGN KEY (brand_id) REFERENCES Brand(brand_id),
    FOREIGN KEY (unit_id) REFERENCES Unit(unit_id),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);


-- Raw Purchase Invoice table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Invoice (
    raw_purchase_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT NOT NULL UNIQUE,
    supplier_id INTEGER NOT NULL,
    invoice_date TEXT NOT NULL,
    total_amount REAL NOT NULL,
    discount_amount REAL DEFAULT 0,
    paid_amount REAL DEFAULT 0,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- Raw Purchase Invoice Item table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Invoice_Item (
    raw_purchase_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (raw_purchase_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Raw Purchase Return Invoice table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Return_Invoice (
    raw_purchase_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_invoice_id INTEGER NOT NULL,
    supplier_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_amount REAL NOT NULL,
    FOREIGN KEY (original_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- Raw Purchase Return Invoice Item table
CREATE TABLE IF NOT EXISTS Raw_Purchase_Return_Invoice_Item (
    raw_purchase_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_return_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    FOREIGN KEY (raw_purchase_return_invoice_id) REFERENCES Raw_Purchase_Return_Invoice(raw_purchase_return_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Production_Stock_Raw_Usage table
CREATE TABLE IF NOT EXISTS Production_Stock_Raw_Usage (
    usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity_used REAL NOT NULL,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Raw Stock Usage table
CREATE TABLE IF NOT EXISTS Raw_Stock_Usage (
    raw_stock_usage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_id INTEGER NOT NULL,
    usage_date TEXT NOT NULL,
    quantity_used REAL NOT NULL,
    reference TEXT,
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

CREATE TABLE IF NOT EXISTS ProductionStock (
    production_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    manufacturer_id INTEGER NOT NULL,
    brand_id INTEGER NOT NULL,
    unit_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    production_date TEXT DEFAULT CURRENT_TIMESTAMP,
    sale_price REAL DEFAULT 0.0,
    FOREIGN KEY (category_id) REFERENCES Category(category_id),
    FOREIGN KEY (manufacturer_id) REFERENCES Manufacturer(manufacturer_id),
    FOREIGN KEY (brand_id) REFERENCES Brand(brand_id),
    FOREIGN KEY (unit_id) REFERENCES Unit(unit_id)
);


-- Unit table
CREATE TABLE IF NOT EXISTS Unit (
    unit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    unit_name TEXT NOT NULL UNIQUE
);

-- Production Invoice table
CREATE TABLE IF NOT EXISTS Production_Invoice (
    production_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_date TEXT NOT NULL,
    notes TEXT
);

-- Production Invoice Item table
CREATE TABLE IF NOT EXISTS Production_Invoice_Item (
    production_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    production_id INTEGER NOT NULL,
    quantity_produced REAL NOT NULL,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id),
    FOREIGN KEY (production_id) REFERENCES ProductionStock(production_id)
);

-- Production Return Invoice table
CREATE TABLE IF NOT EXISTS Production_Return_Invoice (
    production_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_production_invoice_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_quantity REAL NOT NULL,
    total_return_amount REAL NOT NULL,
    notes TEXT,
    FOREIGN KEY (original_production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)
);

-- Production Return Invoice Item table
CREATE TABLE IF NOT EXISTS Production_Return_Invoice_Item (
    production_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_return_invoice_id INTEGER NOT NULL,
    production_id INTEGER NOT NULL,
    quantity_returned REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    FOREIGN KEY (production_return_invoice_id) REFERENCES Production_Return_Invoice(production_return_invoice_id),
    FOREIGN KEY (production_id) REFERENCES ProductionStock(production_id)
);

-- Cash Transaction table
CREATE TABLE IF NOT EXISTS Cash_Transaction (
    cash_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('cash_in', 'cash_out', 'transfer_to_bank', 'transfer_from_bank')),
    amount REAL NOT NULL,
    description TEXT
);

-- Bank Transaction table
CREATE TABLE IF NOT EXISTS Bank_Transaction (
    bank_transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id INTEGER NOT NULL,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('deposit', 'withdraw', 'transfer_in', 'transfer_out', 'invoice_payment')),
    amount REAL NOT NULL,
    description TEXT,
    related_bank_id INTEGER,
    FOREIGN KEY (bank_id) REFERENCES Bank(bank_id),
    FOREIGN KEY (related_bank_id) REFERENCES Bank(bank_id)
);

-- Raw Stock Use Invoice table
CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Invoice (
    raw_stock_use_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    use_invoice_number TEXT NOT NULL UNIQUE,
    usage_date TEXT NOT NULL,
    total_usage_amount REAL NOT NULL DEFAULT 0.0,
    reference_purpose TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Raw Stock Use Invoice Item table
CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Invoice_Item (
    raw_stock_use_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_use_invoice_id INTEGER NOT NULL,
    raw_stock_id INTEGER NOT NULL,
    quantity_used REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    FOREIGN KEY (raw_stock_use_invoice_id) REFERENCES Raw_Stock_Use_Invoice(raw_stock_use_invoice_id),
    FOREIGN KEY (raw_stock_id) REFERENCES Raw_Stock(stock_id)
);

-- Sales Invoice table
CREATE TABLE IF NOT EXISTS Sales_Invoice (
    sales_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_invoice_number TEXT NOT NULL UNIQUE,
    customer_id INTEGER NOT NULL,
    sales_date TEXT NOT NULL,
    total_amount REAL NOT NULL DEFAULT 0.0,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    other_discount REAL NOT NULL DEFAULT 0.0,
    paid_amount REAL NOT NULL DEFAULT 0.0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

CREATE TABLE IF NOT EXISTS Sales_Invoice_Item (
    sales_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_invoice_id INTEGER NOT NULL,
    production_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    discount_percentage REAL NOT NULL DEFAULT 0.0,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    total_price REAL NOT NULL,
    FOREIGN KEY (sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id),
    FOREIGN KEY (production_stock_id) REFERENCES ProductionStock(production_id)
);

-- Sales Return Invoice table
CREATE TABLE IF NOT EXISTS Sales_Return_Invoice (
    sales_return_invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number TEXT NOT NULL UNIQUE,
    original_sales_invoice_id INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    return_date TEXT NOT NULL,
    total_return_amount REAL NOT NULL DEFAULT 0.0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (original_sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Sales Return Invoice Item table
CREATE TABLE IF NOT EXISTS Sales_Return_Invoice_Item (
    sales_return_invoice_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_return_invoice_id INTEGER NOT NULL,
    production_stock_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    total_price REAL NOT NULL,
    FOREIGN KEY (sales_return_invoice_id) REFERENCES Sales_Return_Invoice(sales_return_invoice_id),
    FOREIGN KEY (production_stock_id) REFERENCES ProductionStock(production_id)
);

-- Contract based employee table
CREATE TABLE IF NOT EXISTS Contract_Employee (
    employee_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    cnic TEXT NOT NULL,
    address TEXT NOT NULL,
    remarks TEXT NOT NULL,
    task TEXT NOT NULL,
    num_tasks INTEGER NOT NULL,
    cost_per_task REAL NOT NULL,
    total_tasks_done INTEGER NOT NULL,
    date TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Customer Transaction table for payments and ledger tracking
CREATE TABLE IF NOT EXISTS Customer_Transaction (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('payment_received', 'invoice_charge', 'adjustment', 'opening_balance')),
    amount REAL NOT NULL,
    description TEXT,
    reference_invoice_number TEXT,
    balance_after_transaction REAL NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Supplier Transaction table for payments and ledger tracking
CREATE TABLE IF NOT EXISTS Supplier_Transaction (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_id INTEGER NOT NULL,
    transaction_date TEXT NOT NULL,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('payment_made', 'invoice_charge', 'adjustment', 'opening_balance')),
    amount REAL NOT NULL,
    description TEXT,
    reference_invoice_number TEXT,
    balance_after_transaction REAL NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);




-- Books Tables (replacing views)

-- Purchase Book table
CREATE TABLE IF NOT EXISTS Purchase_Book (
    purchase_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_invoice_id INTEGER NOT NULL,
    invoice_number TEXT NOT NULL,
    supplier_name TEXT NOT NULL,
    invoice_date TEXT NOT NULL,
    item_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    item_total REAL NOT NULL,
    total_amount REAL NOT NULL,
    discount_amount REAL DEFAULT 0,
    paid_amount REAL DEFAULT 0,
    balance REAL DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (raw_purchase_invoice_id) REFERENCES Raw_Purchase_Invoice(raw_purchase_invoice_id)
);

-- Return Purchase Book table
CREATE TABLE IF NOT EXISTS Return_Purchase_Book (
    return_purchase_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_purchase_return_invoice_id INTEGER NOT NULL,
    return_invoice_number TEXT NOT NULL,
    supplier_name TEXT NOT NULL,
    return_date TEXT NOT NULL,
    item_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    item_total REAL NOT NULL,
    total_return_amount REAL NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (raw_purchase_return_invoice_id) REFERENCES Raw_Purchase_Return_Invoice(raw_purchase_return_invoice_id)
);

-- Raw Stock Use Book table
CREATE TABLE IF NOT EXISTS Raw_Stock_Use_Book (
    raw_stock_use_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    raw_stock_use_invoice_id INTEGER NOT NULL,
    use_invoice_number TEXT NOT NULL,
    usage_date TEXT NOT NULL,
    item_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity_used REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    total_usage_amount REAL NOT NULL,
    reference_purpose TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (raw_stock_use_invoice_id) REFERENCES Raw_Stock_Use_Invoice(raw_stock_use_invoice_id)
);

-- Production Book table
CREATE TABLE IF NOT EXISTS Production_Book (
    production_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_invoice_id INTEGER NOT NULL,
    production_date TEXT NOT NULL,
    product_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity_produced REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (production_invoice_id) REFERENCES Production_Invoice(production_invoice_id)
);

-- Return Production Book table
CREATE TABLE IF NOT EXISTS Return_Production_Book (
    return_production_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    production_return_invoice_id INTEGER NOT NULL,
    return_invoice_number TEXT NOT NULL,
    return_date TEXT NOT NULL,
    product_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity_returned REAL NOT NULL,
    unit_cost REAL NOT NULL,
    total_cost REAL NOT NULL,
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (production_return_invoice_id) REFERENCES Production_Return_Invoice(production_return_invoice_id)
);

-- Sales Book table
CREATE TABLE IF NOT EXISTS Sales_Book (
    sales_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_invoice_id INTEGER NOT NULL,
    sales_invoice_number TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    sales_date TEXT NOT NULL,
    product_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    discount_percentage REAL DEFAULT 0,
    discount_amount REAL DEFAULT 0,
    item_total REAL NOT NULL,
    total_amount REAL NOT NULL,
    other_discount REAL DEFAULT 0,
    paid_amount REAL DEFAULT 0,
    balance REAL DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sales_invoice_id) REFERENCES Sales_Invoice(sales_invoice_id)
);

-- Return Sales Book table
CREATE TABLE IF NOT EXISTS Return_Sales_Book (
    return_sales_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_return_invoice_id INTEGER NOT NULL,
    return_invoice_number TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    return_date TEXT NOT NULL,
    product_name TEXT NOT NULL,
    brand_name TEXT,
    manufacturer_name TEXT,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    item_total REAL NOT NULL,
    total_return_amount REAL NOT NULL,
    previous_balance REAL NOT NULL,
    original_sales_invoice_number TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sales_return_invoice_id) REFERENCES Sales_Return_Invoice(sales_return_invoice_id)
);

-- SELECT * FROM Province;