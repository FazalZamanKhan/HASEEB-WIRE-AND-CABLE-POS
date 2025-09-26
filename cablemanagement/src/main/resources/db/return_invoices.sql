-- Production Return Invoices table
CREATE TABLE IF NOT EXISTS production_return_invoices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number VARCHAR(50) UNIQUE NOT NULL,
    return_date DATE NOT NULL,
    production_invoice_id INTEGER,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (production_invoice_id) REFERENCES production_invoices(id)
);

-- Production Return Invoice Items table
CREATE TABLE IF NOT EXISTS production_return_invoice_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    return_invoice_number VARCHAR(50) NOT NULL,
    product_id INTEGER NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (return_invoice_number) REFERENCES production_return_invoices(return_invoice_number),
    FOREIGN KEY (product_id) REFERENCES production_stock(id)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_return_invoice_number ON production_return_invoice_items(return_invoice_number);
