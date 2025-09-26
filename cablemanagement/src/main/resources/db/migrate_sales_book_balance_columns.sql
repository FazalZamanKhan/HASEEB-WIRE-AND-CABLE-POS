-- Migration script to add balance columns to Sales_Book table
-- This script safely adds the new balance columns and migrates existing data

-- Step 1: Add new columns to Sales_Book table
ALTER TABLE Sales_Book ADD COLUMN net_invoice_amount REAL DEFAULT 0;
ALTER TABLE Sales_Book ADD COLUMN previous_balance REAL DEFAULT 0;
ALTER TABLE Sales_Book ADD COLUMN total_balance REAL DEFAULT 0;
ALTER TABLE Sales_Book ADD COLUMN net_balance REAL DEFAULT 0;

-- Step 2: Migrate existing data
-- Update net_invoice_amount: this is the old 'balance' field which was actually the net invoice amount
UPDATE Sales_Book SET net_invoice_amount = COALESCE(balance, 0);

-- Step 3: For existing records, calculate and populate the balance fields
-- Note: This is a best-effort migration. For complete accuracy, you may need to 
-- regenerate invoices from the Sales_Book table after this migration.

-- Update previous_balance, total_balance, and net_balance for existing records
-- This uses the Customer_Transaction table to reconstruct the balance history
UPDATE Sales_Book 
SET 
    previous_balance = (
        SELECT COALESCE(
            (SELECT ct.balance_after_transaction 
             FROM Customer_Transaction ct 
             JOIN Customer c ON ct.customer_id = c.customer_id 
             WHERE c.customer_name = Sales_Book.customer_name 
             AND ct.transaction_id < (
                 SELECT ct2.transaction_id 
                 FROM Customer_Transaction ct2 
                 JOIN Customer c2 ON ct2.customer_id = c2.customer_id 
                 WHERE c2.customer_name = Sales_Book.customer_name 
                 AND ct2.reference_invoice_number = Sales_Book.sales_invoice_number 
                 AND ct2.transaction_type = 'invoice_charge' 
                 LIMIT 1
             ) 
             ORDER BY ct.transaction_id DESC 
             LIMIT 1), 
            0.0
        )
    ),
    total_balance = (
        SELECT COALESCE(
            (SELECT ct.balance_after_transaction 
             FROM Customer_Transaction ct 
             JOIN Customer c ON ct.customer_id = c.customer_id 
             WHERE c.customer_name = Sales_Book.customer_name 
             AND ct.reference_invoice_number = Sales_Book.sales_invoice_number 
             AND ct.transaction_type = 'invoice_charge' 
             ORDER BY ct.transaction_id DESC 
             LIMIT 1), 
            previous_balance + net_invoice_amount
        )
    ),
    net_balance = (
        SELECT COALESCE(
            (SELECT ct.balance_after_transaction 
             FROM Customer_Transaction ct 
             JOIN Customer c ON ct.customer_id = c.customer_id 
             WHERE c.customer_name = Sales_Book.customer_name 
             AND ct.reference_invoice_number = Sales_Book.sales_invoice_number 
             AND ct.transaction_type = 'payment_received' 
             ORDER BY ct.transaction_id DESC 
             LIMIT 1), 
            total_balance - paid_amount
        )
    );

-- Step 4: Create a backup of the old balance column for reference
-- (Optional - uncomment if you want to keep the old data)
-- ALTER TABLE Sales_Book ADD COLUMN old_balance_backup REAL;
-- UPDATE Sales_Book SET old_balance_backup = balance;

-- Step 5: Drop the old balance column (uncomment when you're confident in the migration)
-- ALTER TABLE Sales_Book DROP COLUMN balance;

-- Verification queries (run these to check the migration)
-- SELECT sales_invoice_number, customer_name, net_invoice_amount, previous_balance, total_balance, net_balance 
-- FROM Sales_Book 
-- ORDER BY sales_date DESC 
-- LIMIT 10;
