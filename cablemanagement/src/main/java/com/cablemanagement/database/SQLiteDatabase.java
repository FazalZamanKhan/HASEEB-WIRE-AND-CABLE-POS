package com.cablemanagement.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cablemanagement.model.Bank;
import com.cablemanagement.model.BankTransaction;
import com.cablemanagement.model.Brand;
import com.cablemanagement.model.Customer;
import com.cablemanagement.model.Manufacturer;
import com.cablemanagement.model.RawStockPurchaseItem;
import com.cablemanagement.model.RawStockUseItem;
import com.cablemanagement.model.Supplier;
import java.nio.file.Path;

public class SQLiteDatabase implements db {

    public SQLiteDatabase() {
        // First try the current directory, then fall back to relative path
        String currentDir = System.getProperty("user.dir");
        System.out.println("DEBUG: Current working directory: " + currentDir);
        
        if (currentDir.endsWith("cablemanagement")) {
            this.databasePath = "cable_management.db";
        } else {
            this.databasePath = "CableManagement/cablemanagement/cable_management.db";
        }
        
        System.out.println("DEBUG: Database path set to: " + this.databasePath);
        
        // Auto-connect when instantiated
        connect(null, null, null);
        // Initialize all required tables
        initializeDatabase();
    }


    public SQLiteDatabase(String databasePath) {
        this.databasePath = databasePath;
        // Auto-connect when instantiated
        connect(databasePath, null, null);
        // Initialize all required tables
        initializeDatabase();
    }

    private Connection connection;
    private String databasePath;

    public Connection getConnection() {
        return this.connection;
    }

    
    public List<Object[]> getLastProductionReturnInvoice() {
        List<Object[]> result = new ArrayList<>();
        String query = "SELECT return_invoice_number, return_date, notes FROM Production_Return_Invoice " +
                      "ORDER BY production_return_invoice_id DESC LIMIT 1";
                      
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String jdbcUrl = "jdbc:sqlite:cable_management.db";
            connection = DriverManager.getConnection(jdbcUrl);
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getString("notes")
                };
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    

    public List<Object[]> getProductionReturnInvoiceItems(String returnInvoiceNumber) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT ps.product_name, pri.quantity_returned, pri.unit_cost " +
                      "FROM Production_Return_Invoice_Item pri " +
                      "JOIN Production_Return_Invoice priv ON pri.production_return_invoice_id = priv.production_return_invoice_id " +
                      "JOIN ProductionStock ps ON pri.production_id = ps.production_id " +
                      "WHERE priv.return_invoice_number = ?";
                      
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String jdbcUrl = "jdbc:sqlite:cable_management.db";
            connection = DriverManager.getConnection(jdbcUrl);
            stmt = connection.prepareStatement(query);
            stmt.setString(1, returnInvoiceNumber);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] item = {
                    rs.getString("product_name"),
                    rs.getDouble("quantity_returned"),
                    rs.getDouble("unit_cost")
                };
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return items;
    }
    
    @Override
    public List<Object[]> getAllSalesReturnInvoicesForDropdown() {
        List<Object[]> returnInvoices = new ArrayList<>();
        String query = "SELECT sr.sales_return_invoice_id, sr.return_invoice_number, c.customer_name, sr.return_date " +
                      "FROM SalesReturnInvoice sr " +
                      "INNER JOIN Customer c ON sr.customer_id = c.customer_id " +
                      "ORDER BY sr.sales_return_invoice_id DESC";
                      
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Object[] invoiceData = {
                    rs.getInt("sales_return_invoice_id"),
                    rs.getString("return_invoice_number"),
                    rs.getString("customer_name"),
                    rs.getString("return_date")
                };
                returnInvoices.add(invoiceData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnInvoices;
    }

    @Override
    public boolean updateBankBalance(double newBalance) {
        // Update the balance for all banks (or you may want to specify a bank_id)
        String query = "UPDATE Bank SET balance = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBank(int bankId) {
        String query = "DELETE FROM Bank WHERE bank_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bankId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public double getCashBalance() {
        // This method is similar to getCurrentCashBalance()
        return getCurrentCashBalance();
    }

    @Override
    public boolean insertCashTransaction(BankTransaction transaction) {
        // Assuming BankTransaction has getters for required fields
        String query = "INSERT INTO Cash_Transaction (transaction_type, amount, description, transaction_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, transaction.getTransactionType());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getTransactionDate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCashTransaction(BankTransaction transaction) {
        String query = "UPDATE Cash_Transaction SET transaction_type = ?, amount = ?, description = ?, transaction_date = ? WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, transaction.getTransactionType());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getTransactionDate());
            pstmt.setInt(5, transaction.getTransactionId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCashTransaction(int transactionId) {
        String query = "DELETE FROM Cash_Transaction WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateBank(Bank bank) {
        String query = "UPDATE Bank SET branch_name = ?, balance = ?, account_number = ? WHERE bank_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bank.getBranchName());
            pstmt.setDouble(2, bank.getBalance());
            pstmt.setString(3, bank.getAccountNumber());
            pstmt.setInt(4, bank.getBankId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertBankTransaction(BankTransaction transaction) {
        String query = "INSERT INTO Bank_Transaction (bank_id, transaction_date, transaction_type, amount, description, related_bank_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            
            // Verify bank_id exists first
            if (!bankExists(transaction.getBankId())) {
                System.err.println("Bank ID " + transaction.getBankId() + " doesn't exist");
                return false;
            }
            
            // Verify related_bank_id exists if specified
            if (transaction.getRelatedBankId() != 0 && !bankExists(transaction.getRelatedBankId())) {
                System.err.println("Related Bank ID " + transaction.getRelatedBankId() + " doesn't exist");
                return false;
            }
            
            pstmt.setInt(1, transaction.getBankId());
            pstmt.setString(2, transaction.getTransactionDate());
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getDescription());
            
            // Handle null related_bank_id
            if (transaction.getRelatedBankId() != 0) {
                pstmt.setInt(6, transaction.getRelatedBankId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting transaction: " + e.getMessage());
            return false;
        }
    }

    private boolean bankExists(int bankId) throws SQLException {
        String query = "SELECT 1 FROM Bank WHERE bank_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, bankId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
        

    @Override
    public String connect(String url, String user, String password) {
        try {
            String jdbcUrl = "jdbc:sqlite:" + (url != null ? url : databasePath);
            connection = DriverManager.getConnection(jdbcUrl);
            
            try (Statement stmt = connection.createStatement()) {
                // Enable foreign keys
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Set timeout for busy connections (in milliseconds)
                stmt.execute("PRAGMA busy_timeout = 30000");
                
                // Use WAL mode for better concurrency
                stmt.execute("PRAGMA journal_mode = WAL");
                
                // Optimize for better performance
                stmt.execute("PRAGMA synchronous = NORMAL");
                stmt.execute("PRAGMA cache_size = 10000");
                stmt.execute("PRAGMA temp_store = memory");
                
                System.out.println("DEBUG: SQLite connection configured with lock prevention settings");
            }
            
            return "Connected to SQLite database successfully";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to connect: " + e.getMessage();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Object executeQuery(String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            List<List<Object>> results = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int executeUpdate(String query) {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean SignIn(String userId, String password) {
        // Ensure User table exists first
        ensureUserTableExists();
        
        String query = "SELECT COUNT(*) FROM User WHERE username = ? AND password_hash = ? AND is_active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void ensureUserTableExists() {
        try {
            // Check if User table exists
            String checkTableQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='User'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(checkTableQuery);
            
            if (!rs.next()) {
                // Create User table
                String createUserTable = "CREATE TABLE IF NOT EXISTS User (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "role TEXT DEFAULT 'user'," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "is_active INTEGER DEFAULT 1" +
                    ")";
                
                stmt.execute(createUserTable);
                
                // Insert default users
                String insertUsers = "INSERT INTO User (username, password_hash, role) VALUES " +
                    "('admin', 'admin123', 'admin')," +
                    "('cashier1', 'cash123', 'cashier')," +
                    "('manager1', 'manager123', 'manager')";
                
                stmt.execute(insertUsers);
                
                System.out.println("User table created with default credentials:");
                System.out.println("- admin / admin123");
                System.out.println("- cashier1 / cash123");
                System.out.println("- manager1 / manager123");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        try {
            // First ensure the Views directory exists in the database
            ensureViewsExist();
            
            // Drop and recreate View_Purchase_Book
            connection.createStatement().execute("DROP VIEW IF EXISTS View_Purchase_Book");
            
            // Create detailed view with item-level data
            String sql = "CREATE VIEW View_Purchase_Book AS " +
                       "SELECT " +
                       "    rpi.raw_purchase_invoice_id, " +
                       "    rpi.invoice_number, " +
                       "    s.supplier_name, " +
                       "    rpi.invoice_date, " +
                       "    rs.item_name, " +
                       "    b.brand_name, " +
                       "    m.manufacturer_name, " +
                       "    rpii.quantity, " +
                       "    rpii.unit_price, " +
                       "    (rpii.quantity * rpii.unit_price) AS item_total, " +
                       "    rpi.total_amount, " +
                       "    rpi.discount_amount, " +
                       "    rpi.paid_amount, " +
                       "    (rpi.total_amount - rpi.paid_amount) AS balance " +
                       "FROM Raw_Purchase_Invoice rpi " +
                       "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                       "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                       "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                       "JOIN Brand b ON rs.brand_id = b.brand_id " +
                       "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);

            // Drop and recreate View_Production_Book
            connection.createStatement().execute("DROP VIEW IF EXISTS View_Production_Book");
            
            // Create production view
            sql = "CREATE VIEW View_Production_Book AS " +
                 "SELECT " +
                 "    pi.production_invoice_id, " +
                 "    pi.production_date, " +
                 "    ps.product_name, " +
                 "    pii.quantity_produced AS quantity, " +
                 "    ps.unit_cost, " +
                 "    (pii.quantity_produced * ps.unit_cost) AS total_cost, " +
                 "    b.brand_name, " +
                 "    m.manufacturer_name, " +
                 "    pi.notes " +
                 "FROM Production_Invoice pi " +
                 "JOIN Production_Invoice_Item pii ON pi.production_invoice_id = pii.production_invoice_id " +
                 "JOIN ProductionStock ps ON pii.production_id = ps.production_id " +
                 "JOIN Brand b ON ps.brand_id = b.brand_id " +
                 "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Successfully created View_Production_Book view");
            
            // Drop and recreate View_Return_Purchase_Book
            connection.createStatement().execute("DROP VIEW IF EXISTS View_Return_Purchase_Book");
            
            // Create return purchases view with same column structure as View_Purchase_Book
            sql = "CREATE VIEW View_Return_Purchase_Book AS " +
                 "SELECT " +
                 "    rpri.raw_purchase_return_invoice_id AS raw_purchase_invoice_id, " +
                 "    rpri.return_invoice_number AS invoice_number, " +
                 "    s.supplier_name, " +
                 "    rpri.return_date AS invoice_date, " +
                 "    rs.item_name, " +
                 "    b.brand_name, " +
                 "    m.manufacturer_name, " +
                 "    rprii.quantity AS quantity, " +
                 "    rprii.unit_price, " +
                 "    (rprii.quantity * rprii.unit_price) AS item_total, " +
                 "    rpri.total_return_amount AS total_amount, " +
                 "    0 AS discount_amount, " +  // Return invoices don't have discounts
                 "    rpri.total_return_amount AS paid_amount, " + // Full amount is considered paid in returns
                 "    0 AS balance " +  // No balance in returns
                 "FROM Raw_Purchase_Return_Invoice rpri " +
                 "JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                 "JOIN Raw_Purchase_Return_Invoice_Item rprii ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                 "JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                 "JOIN Brand b ON rs.brand_id = b.brand_id " +
                 "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Successfully created View_Return_Purchase_Book view");
            
            // Drop and recreate View_Return_Production_Book
            connection.createStatement().execute("DROP VIEW IF EXISTS View_Return_Production_Book");
            
            // Create return production view
            sql = "CREATE VIEW View_Return_Production_Book AS " +
                 "SELECT " +
                 "    pri.production_return_invoice_id, " +
                 "    pri.return_invoice_number, " +
                 "    pri.return_date, " +
                 "    prii.quantity_returned AS quantity, " +
                 "    prii.unit_cost, " +
                 "    prii.total_cost, " +
                 "    ps.product_name, " +
                 "    b.brand_name, " +
                 "    m.manufacturer_name, " +
                 "    pri.notes " +
                 "FROM Production_Return_Invoice pri " +
                 "JOIN Production_Return_Invoice_Item prii ON pri.production_return_invoice_id = prii.production_return_invoice_id " +
                 "JOIN ProductionStock ps ON prii.production_id = ps.production_id " +
                 "JOIN Brand b ON ps.brand_id = b.brand_id " +
                 "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Successfully created View_Return_Production_Book view");
            
            System.out.println("All database views initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error initializing views: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database views", e);
        }
    }

    private String readSqlFile(String filePath) throws IOException {
        try {
            // Try to read from the absolute path first
            if (new File(filePath).exists()) {
                return new String(Files.readAllBytes(Path.of(filePath)));
            }
            
            // Try to find the file in the classpath resources
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            if (inputStream != null) {
                return new String(inputStream.readAllBytes());
            }
            
            // Try to find the file relative to the project root
            String projectRoot = System.getProperty("user.dir");
            Path projectPath = Path.of(projectRoot, filePath);
            if (Files.exists(projectPath)) {
                return new String(Files.readAllBytes(projectPath));
            }
            
            // As a last resort, check a few common locations
            String[] commonLocations = {
                projectRoot + "/schema.sql",
                projectRoot + "/CableManagement/cablemanagement/schema.sql",
                projectRoot + "/cablemanagement/schema.sql",
                projectRoot + "/src/main/resources/schema.sql",
                projectRoot + "/src/main/resources/db/schema.sql"
            };
            
            for (String location : commonLocations) {
                File file = new File(location);
                if (file.exists()) {
                    System.out.println("Found schema file at: " + location);
                    return new String(Files.readAllBytes(file.toPath()));
                }
            }
            
            throw new IOException("Could not find schema file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading SQL file: " + filePath);
            throw e;
        }
    }

    private void initializeDatabase() {
        try {
            Statement stmt = connection.createStatement();

            // Enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON");

            // Read SQL from file - first try the project root location
            String projectRoot = System.getProperty("user.dir");
            String schemaPath = projectRoot + "/schema.sql";
            
            // If not found in project root, try other common locations
            if (!new File(schemaPath).exists()) {
                schemaPath = projectRoot + "/CableManagement/cablemanagement/schema.sql";
            }
            if (!new File(schemaPath).exists()) {
                schemaPath = projectRoot + "/cablemanagement/schema.sql";
            }
            
            System.out.println("Looking for schema at: " + schemaPath);
            String sql = readSqlFile(schemaPath);

            // Split statements and execute them one by one
            String[] queries = sql.split(";");
            for (String query : queries) {
                String trimmed = query.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed + ";");
                }
            }

            stmt.close();
            System.out.println("Database initialized successfully with SQL file.");

            // Initialize views
            initializeViews();

        } catch (SQLException | IOException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public List<String> getAllTehsils() {
        List<String> tehsils = new ArrayList<>();
        String query = "SELECT tehsil_name FROM Tehsil ORDER BY tehsil_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                tehsils.add(rs.getString("tehsil_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tehsils;
    }

    @Override
    public List<String> getTehsilsByDistrict(String districtName) {
        List<String> tehsils = new ArrayList<>();
        String query = "SELECT t.tehsil_name FROM Tehsil t " +
                      "JOIN District d ON t.district_id = d.district_id " +
                      "WHERE d.district_name = ? ORDER BY t.tehsil_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, districtName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tehsils.add(rs.getString("tehsil_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tehsils;
    }

    @Override
    public boolean insertTehsil(String tehsilName, String districtName) {
        String getDistrictQuery = "SELECT district_id FROM District WHERE district_name = ?";
        String insertQuery = "INSERT INTO Tehsil (tehsil_name, district_id) VALUES (?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getDistrictQuery)) {
            getStmt.setString(1, districtName);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int districtId = rs.getInt("district_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, tehsilName);
                        insertStmt.setInt(2, districtId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tehsilExists(String tehsilName) {
        String query = "SELECT COUNT(*) FROM Tehsil WHERE tehsil_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tehsilName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllDistricts() {
        List<String> districts = new ArrayList<>();
        String query = "SELECT district_name FROM District ORDER BY district_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                districts.add(rs.getString("district_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return districts;
    }

    @Override
    public List<String> getDistrictsByProvince(String provinceName) {
        List<String> districts = new ArrayList<>();
        String query = "SELECT d.district_name FROM District d " +
                      "JOIN Province p ON d.province_id = p.province_id " +
                      "WHERE p.province_name = ? ORDER BY d.district_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, provinceName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    districts.add(rs.getString("district_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return districts;
    }

    @Override
    public boolean insertDistrict(String districtName, String provinceName) {
        String getProvinceQuery = "SELECT province_id FROM Province WHERE province_name = ?";
        String insertQuery = "INSERT INTO District (district_name, province_id) VALUES (?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getProvinceQuery)) {
            getStmt.setString(1, provinceName);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int provinceId = rs.getInt("province_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, districtName);
                        insertStmt.setInt(2, provinceId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllProvinces() {
        List<String> provinces = new ArrayList<>();
        String query = "SELECT province_name FROM Province ORDER BY province_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                provinces.add(rs.getString("province_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return provinces;
    }

    @Override
    public boolean insertProvince(String provinceName) {
        String query = "INSERT INTO Province (province_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, provinceName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT category_name FROM Category ORDER BY category_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public boolean insertCategory(String categoryName) {
        String query = "INSERT INTO Category (category_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get category_id by category name
     */
    public int getCategoryIdByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return 1; // Default to first category
        }
        
        String query = "SELECT category_id FROM Category WHERE category_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If category not found, try to insert it and return the ID
        return insertCategoryAndGetId(categoryName.trim());
    }
    
    /**
     * Insert new category and return its ID
     */
    private int insertCategoryAndGetId(String categoryName) {
        String query = "INSERT INTO Category (category_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categoryName);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 1; // Return default category ID if insertion fails
    }

    /**
     * Get manufacturer_id by manufacturer name
     */
    public int getManufacturerIdByName(String manufacturerName) {
        if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
            return 1; // Default to first manufacturer
        }
        
        String query = "SELECT manufacturer_id FROM Manufacturer WHERE manufacturer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, manufacturerName.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("manufacturer_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If manufacturer not found, try to insert it and return the ID
        return insertManufacturerAndGetId(manufacturerName.trim());
    }
    
    /**
     * Insert new manufacturer and return its ID
     */
    private int insertManufacturerAndGetId(String manufacturerName) {
        String query = "INSERT INTO Manufacturer (manufacturer_name, tehsil_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, manufacturerName);
            pstmt.setInt(2, 1); // Default tehsil_id = 1
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 1; // Return default manufacturer ID if insertion fails
    }

    @Override
    public List<Manufacturer> getAllManufacturers() {
        List<Manufacturer> manufacturers = new ArrayList<>();
        String query = "SELECT m.manufacturer_name, t.tehsil_name, d.district_name, p.province_name " +
                      "FROM Manufacturer m " +
                      "JOIN Tehsil t ON m.tehsil_id = t.tehsil_id " +
                      "JOIN District d ON t.district_id = d.district_id " +
                      "JOIN Province p ON d.province_id = p.province_id " +
                      "ORDER BY m.manufacturer_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("manufacturer_name");
                String province = rs.getString("province_name");
                String district = rs.getString("district_name");
                String tehsil = rs.getString("tehsil_name");
                
                manufacturers.add(new Manufacturer(name, province, district, tehsil));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return manufacturers;
    }

    @Override
    public boolean insertManufacturer(String name, String province, String district, String tehsil) {
        String getTehsilQuery = "SELECT t.tehsil_id FROM Tehsil t " +
                               "JOIN District d ON t.district_id = d.district_id " +
                               "JOIN Province p ON d.province_id = p.province_id " +
                               "WHERE p.province_name = ? AND d.district_name = ? AND t.tehsil_name = ?";
        String insertQuery = "INSERT INTO Manufacturer (manufacturer_name, tehsil_id) VALUES (?, ?)";
        
        try (PreparedStatement getStmt = connection.prepareStatement(getTehsilQuery)) {
            getStmt.setString(1, province);
            getStmt.setString(2, district);
            getStmt.setString(3, tehsil);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setInt(2, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean manufacturerExists(String name) {
        String query = "SELECT COUNT(*) FROM Manufacturer WHERE manufacturer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        String query = "SELECT b.brand_name, t.tehsil_name, d.district_name, p.province_name " +
                    "FROM Brand b " +
                    "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id " +
                    "JOIN Tehsil t ON m.tehsil_id = t.tehsil_id " +
                    "JOIN District d ON t.district_id = d.district_id " +
                    "JOIN Province p ON d.province_id = p.province_id " +
                    "ORDER BY b.brand_name";
        
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("brand_name");
                String province = rs.getString("province_name");
                String district = rs.getString("district_name");
                String tehsil = rs.getString("tehsil_name");
                
                brands.add(new Brand(name, province, district, tehsil));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brands;
    }
        
    @Override
    public boolean insertBrand(String name, String province, String district, String tehsil) {
        try {
            connection.setAutoCommit(false);
            
            // First ensure default province exists
            String checkProvinceQuery = "SELECT province_id FROM Province WHERE province_id = 1";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(checkProvinceQuery);
                if (!rs.next()) {
                    String createDefaultProvince = "INSERT INTO Province (province_id, province_name) VALUES (1, 'Default Province')";
                    stmt.executeUpdate(createDefaultProvince);
                }
            }
            
            // Ensure default district exists
            String checkDistrictQuery = "SELECT district_id FROM District WHERE district_id = 1";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(checkDistrictQuery);
                if (!rs.next()) {
                    String createDefaultDistrict = "INSERT INTO District (district_id, district_name, province_id) VALUES (1, 'Default District', 1)";
                    stmt.executeUpdate(createDefaultDistrict);
                }
            }
            
            // Ensure default tehsil exists
            String checkTehsilQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_id = 1";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(checkTehsilQuery);
                if (!rs.next()) {
                    String createDefaultTehsil = "INSERT INTO Tehsil (tehsil_id, tehsil_name, district_id) VALUES (1, 'Default Tehsil', 1)";
                    stmt.executeUpdate(createDefaultTehsil);
                }
            }

            // Ensure default manufacturer exists
            String checkManufacturerQuery = "SELECT manufacturer_id FROM Manufacturer WHERE manufacturer_id = 1";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(checkManufacturerQuery);
                if (!rs.next()) {
                    String createDefaultManufacturer = "INSERT INTO Manufacturer (manufacturer_id, manufacturer_name, tehsil_id) VALUES (1, 'Default Manufacturer', 1)";
                    stmt.executeUpdate(createDefaultManufacturer);
                }
            }

            // Now insert the brand using default manufacturer
            String insertQuery = "INSERT INTO Brand (brand_name, manufacturer_id) VALUES (?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, name);
                insertStmt.setInt(2, 1); // Use default manufacturer_id = 1
                
                boolean result = insertStmt.executeUpdate() > 0;
                connection.commit();
                return result;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.err.println("Error inserting brand: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean brandExists(String name) {
        String query = "SELECT COUNT(*) FROM Brand WHERE brand_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT c.customer_id, c.customer_name, c.contact_number, c.balance, t.tehsil_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "ORDER BY c.customer_name";
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String name = rs.getString("customer_name");
                String contact = rs.getString("contact_number");
                String tehsil = rs.getString("tehsil_name");
                if (tehsil == null) tehsil = "";
                
                // Calculate current balance based on invoice history
                double currentBalance = getCustomerCurrentBalance(name);
                
                customers.add(new Customer(customerId, name, contact, tehsil, currentBalance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override

    public boolean insertCustomer(String name, String contact) {
        String getTehsilQuery = "SELECT tehsil_id FROM Tehsil LIMIT 1";
        String insertQuery = "INSERT INTO Customer (customer_name, contact_number, tehsil_id, balance) VALUES (?, ?, ?, 0.00)";
        
        try (Statement getStmt = connection.createStatement();
             ResultSet rs = getStmt.executeQuery(getTehsilQuery)) {
            
            if (rs.next()) {
                int tehsilId = rs.getInt("tehsil_id");
                
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, contact);
                    insertStmt.setInt(3, tehsilId);
                    
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertCustomer(String name, String contact, String tehsilName) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String insertQuery = "INSERT INTO Customer (customer_name, contact_number, tehsil_id, balance) VALUES (?, ?, ?, 0.00)";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, contact);
                        insertStmt.setInt(3, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertCustomer(String name, String contact, String tehsilName, double balance) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String insertQuery = "INSERT INTO Customer (customer_name, contact_number, tehsil_id, balance) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, contact);
                        insertStmt.setInt(3, tehsilId);
                        insertStmt.setDouble(4, balance);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean customerExists(String name) {
        String query = "SELECT COUNT(*) FROM Customer WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT s.supplier_name, s.contact_number, s.balance, t.tehsil_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "ORDER BY s.supplier_name";
        
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String name = rs.getString("supplier_name");
                String contact = rs.getString("contact_number");
                String tehsil = rs.getString("tehsil_name");
                if (tehsil == null) tehsil = "";
                
                // Calculate current balance based on invoice history
                double currentBalance = getSupplierCurrentBalance(name);
                
                suppliers.add(new Supplier(name, contact, tehsil, currentBalance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public boolean insertSupplier(String name, String contact) {
        String getTehsilQuery = "SELECT tehsil_id FROM Tehsil LIMIT 1";
        String insertQuery = "INSERT INTO Supplier (supplier_name, contact_number, tehsil_id, balance) VALUES (?, ?, ?, 0.00)";
        
        try (Statement getStmt = connection.createStatement();
            ResultSet rs = getStmt.executeQuery(getTehsilQuery)) {
            
            if (rs.next()) {
                int tehsilId = rs.getInt("tehsil_id");
                
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, contact);
                    insertStmt.setInt(3, tehsilId);
                    
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertSupplier(String name, String contact, String tehsilName) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String insertQuery = "INSERT INTO Supplier (supplier_name, contact_number, tehsil_id, balance) VALUES (?, ?, ?, 0.00)";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, contact);
                        insertStmt.setInt(3, tehsilId);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertSupplier(String name, String contact, String tehsilName, double balance) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String insertQuery = "INSERT INTO Supplier (supplier_name, contact_number, tehsil_id, balance) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, contact);
                        insertStmt.setInt(3, tehsilId);
                        insertStmt.setDouble(4, balance);
                        
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
        
    @Override
    public boolean supplierExists(String name) {
        String query = "SELECT COUNT(*) FROM Supplier WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCustomerBalance(String customerName, double amount) {
        String query = "UPDATE Customer SET balance = balance + ? WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, customerName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateSupplierBalance(String supplierName, double amount) {
        String query = "UPDATE Supplier SET balance = balance + ? WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, supplierName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public double getCustomerBalance(String customerName) {
        String query = "SELECT balance FROM Customer WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, customerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double getSupplierBalance(String supplierName) {
        String query = "SELECT balance FROM Supplier WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double getCustomerCurrentBalance(String customerName) {
        // Simply return the stored balance from the Customer table
        double storedBalance = getCustomerBalance(customerName);
        
        System.out.println("DEBUG: Current balance for customer: " + customerName + " = " + storedBalance);
        
        return storedBalance;
    }

    @Override
    public double getSupplierCurrentBalance(String supplierName) {
        // Return the stored balance directly from Supplier table
        return getSupplierBalance(supplierName);
    }

    /**
     * Get customer's balance BEFORE a specific invoice (for PDF generation)
     * @param customerName Customer name
     * @param excludeInvoiceNumber Invoice to exclude from calculation 
     * @return Previous balance before the specified invoice
     */
    @Override
    public double getCustomerPreviousBalance(String customerName, String excludeInvoiceNumber) {
        // Get the current stored balance
        double currentBalance = getCustomerBalance(customerName);
        
        // Determine if this is a return invoice by checking if the invoice number starts with "SRI"
        boolean isReturnInvoice = excludeInvoiceNumber != null && excludeInvoiceNumber.startsWith("SRI");
        
        double currentInvoiceNetAmount = 0.0;
        
        if (isReturnInvoice) {
            // For return invoices, look in Sales_Return_Invoice table
            String query = "SELECT sri.total_return_amount as net_amount " +
                          "FROM Sales_Return_Invoice sri " +
                          "JOIN Customer c ON sri.customer_id = c.customer_id " +
                          "WHERE c.customer_name = ? AND sri.return_invoice_number = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, customerName);
                pstmt.setString(2, excludeInvoiceNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentInvoiceNetAmount = rs.getDouble("net_amount");
                        // For returns, we ADD back the return amount to get previous balance
                        // because the current balance has already been reduced by the return
                        currentBalance += currentInvoiceNetAmount;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // For regular sales invoices, look in Sales_Invoice table
            String query = "SELECT (si.total_amount - si.discount_amount - si.paid_amount) as net_amount " +
                          "FROM Sales_Invoice si " +
                          "JOIN Customer c ON si.customer_id = c.customer_id " +
                          "WHERE c.customer_name = ? AND si.sales_invoice_number = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, customerName);
                pstmt.setString(2, excludeInvoiceNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentInvoiceNetAmount = rs.getDouble("net_amount");
                        // For regular invoices, subtract the invoice amount to get previous balance
                        currentBalance -= currentInvoiceNetAmount;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        double previousBalance = currentBalance;
        
        System.out.println("DEBUG: Previous balance calculation for customer: " + customerName);
        System.out.println("  Invoice number: " + excludeInvoiceNumber + " (Return: " + isReturnInvoice + ")");
        System.out.println("  Current stored balance: " + getCustomerBalance(customerName));
        System.out.println("  Current invoice net amount: " + currentInvoiceNetAmount);
        System.out.println("  Calculated previous balance: " + previousBalance);
        
        return previousBalance;
    }

    /**
     * Get supplier's balance BEFORE a specific invoice (for PDF generation)
     * @param supplierName Supplier name
     * @param excludeInvoiceNumber Invoice to exclude from calculation 
     * @return Previous balance before the specified invoice
     */
    @Override
    public double getSupplierPreviousBalance(String supplierName, String excludeInvoiceNumber) {
        // Get the current stored balance
        double currentBalance = getSupplierBalance(supplierName);
        
        System.out.println("DEBUG: getSupplierPreviousBalance called for " + supplierName + " invoice " + excludeInvoiceNumber);
        System.out.println("  Current stored balance: " + currentBalance);
        
        // Determine if this is a return invoice by checking if the invoice number starts with "RPRI"
        boolean isReturnInvoice = excludeInvoiceNumber != null && excludeInvoiceNumber.startsWith("RPRI");
        
        double currentInvoiceNetAmount = 0.0;
        
        if (isReturnInvoice) {
            // For return invoices, look in Raw_Purchase_Return_Invoice table
            String returnQuery = "SELECT total_return_amount FROM Raw_Purchase_Return_Invoice rpri " +
                               "JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                               "WHERE s.supplier_name = ? AND rpri.return_invoice_number = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(returnQuery)) {
                pstmt.setString(1, supplierName);
                pstmt.setString(2, excludeInvoiceNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentInvoiceNetAmount = rs.getDouble("total_return_amount");
                        // For return invoice: previous balance = current balance + return amount
                        // (since return reduces balance, we add it back to get previous)
                        System.out.println("  Found return invoice with amount: " + currentInvoiceNetAmount);
                        double previousBalance = currentBalance + currentInvoiceNetAmount;
                        System.out.println("  Calculated previous balance: " + previousBalance);
                        return previousBalance;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // For regular purchase invoices, look in Raw_Purchase_Invoice table
            String purchaseQuery = "SELECT (total_amount - discount_amount - paid_amount) as net_amount FROM Raw_Purchase_Invoice rpi " +
                                 "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                                 "WHERE s.supplier_name = ? AND rpi.invoice_number = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(purchaseQuery)) {
                pstmt.setString(1, supplierName);
                pstmt.setString(2, excludeInvoiceNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentInvoiceNetAmount = rs.getDouble("net_amount");
                        // For purchase invoice: previous balance = current balance - net amount  
                        // (since purchase increases balance, we subtract it to get previous)
                        System.out.println("  Found purchase invoice with net amount: " + currentInvoiceNetAmount);
                        double previousBalance = currentBalance - currentInvoiceNetAmount;
                        System.out.println("  Calculated previous balance: " + previousBalance);
                        return previousBalance;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        // If invoice not found, return current balance (this means we're generating PDF before saving invoice)
        // In this case, current balance IS the previous balance since the invoice hasn't been saved yet
        System.out.println("  Invoice not found in database, returning current balance: " + currentBalance);
        return currentBalance;
    }

    /**
     * Get invoice balance details for PDF generation
     * @param customerName Customer name
     * @param invoiceNumber Current invoice number
     * @param currentInvoiceTotal Current invoice total amount
     * @param currentInvoicePaid Current invoice paid amount
     * @return Object array with [previousBalance, totalBalance, netBalance]
     */
    @Override
    public Object[] getCustomerInvoiceBalanceDetails(String customerName, String invoiceNumber, 
                                                   double currentInvoiceTotal, double currentInvoicePaid) {
        double previousBalance = getCustomerPreviousBalance(customerName, invoiceNumber);
        
        // Note: currentInvoiceTotal should be the net amount (after discount)
        // The net amount that will be added to customer balance
        double netInvoiceAmount = currentInvoiceTotal - currentInvoicePaid;
        double totalBalance = previousBalance + netInvoiceAmount;
        double netBalance = totalBalance; // Net balance is same as total balance since paid amount is already subtracted
        
        System.out.println("DEBUG: Balance details for " + customerName + " invoice " + invoiceNumber + ":");
        System.out.println("  Previous Balance: " + previousBalance);
        System.out.println("  currentInvoiceTotal (net after discount): " + currentInvoiceTotal);
        System.out.println("  currentInvoicePaid: " + currentInvoicePaid);
        System.out.println("  netInvoiceAmount: " + netInvoiceAmount);
        System.out.println("  Calculated Total Balance: " + totalBalance);
        System.out.println("  Calculated Net Balance: " + netBalance);
        
        return new Object[]{previousBalance, totalBalance, netBalance};
    }

    /**
     * Get supplier invoice balance details for PDF generation
     * @param supplierName Supplier name
     * @param invoiceNumber Current invoice number
     * @param currentInvoiceTotal Current invoice total amount (after discount)
     * @param currentInvoicePaid Current invoice paid amount
     * @return Object array with [previousBalance, totalBalance, netBalance]
     */
    @Override
    public Object[] getSupplierInvoiceBalanceDetails(String supplierName, String invoiceNumber, 
                                                   double currentInvoiceTotal, double currentInvoicePaid) {
        
        System.out.println("DEBUG: getSupplierInvoiceBalanceDetails for " + supplierName + " invoice " + invoiceNumber);
        System.out.println("  Input currentInvoiceTotal: " + currentInvoiceTotal);
        System.out.println("  Input currentInvoicePaid: " + currentInvoicePaid);
        
        // Get current balance (which already includes all invoices including this one if it's been saved)
        double currentBalance = getSupplierBalance(supplierName);
        System.out.println("  Current balance from database: " + currentBalance);
        
        // Try to get the net amount from database for this specific invoice
        double databaseNetAmount = 0.0;
        String purchaseQuery = "SELECT (total_amount - discount_amount - paid_amount) as net_amount FROM Raw_Purchase_Invoice rpi " +
                             "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                             "WHERE s.supplier_name = ? AND rpi.invoice_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(purchaseQuery)) {
            pstmt.setString(1, supplierName);
            pstmt.setString(2, invoiceNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    databaseNetAmount = rs.getDouble("net_amount");
                    System.out.println("  Found invoice in database with net amount: " + databaseNetAmount);
                    
                    // Invoice exists in database, so current balance already includes it
                    // Previous balance = current balance - this invoice's net amount
                    double previousBalance = currentBalance - databaseNetAmount;
                    double totalBalance = currentBalance; // Current balance is already the total after this invoice
                    double netBalance = totalBalance;
                    
                    System.out.println("  Calculated previousBalance: " + previousBalance);
                    System.out.println("  Calculated totalBalance: " + totalBalance);
                    System.out.println("  Calculated netBalance: " + netBalance);
                    
                    return new Object[]{previousBalance, totalBalance, netBalance};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Invoice not found in database - this is for preview before saving
        // Use the input parameters to calculate
        double netInvoiceAmount = currentInvoiceTotal - currentInvoicePaid;
        double previousBalance = currentBalance; // Current balance is the previous balance
        double totalBalance = previousBalance + netInvoiceAmount;
        double netBalance = totalBalance;
        
        System.out.println("  Invoice not in database - calculating from inputs");
        System.out.println("  netInvoiceAmount: " + netInvoiceAmount);
        System.out.println("  previousBalance: " + previousBalance);
        System.out.println("  totalBalance: " + totalBalance);
        System.out.println("  netBalance: " + netBalance);
        System.out.println("  Returning: [" + previousBalance + ", " + totalBalance + ", " + netBalance + "]");
        
        return new Object[]{previousBalance, totalBalance, netBalance};
    }

    @Override
    public Customer getCustomerWithCurrentBalance(String customerName) {
        String query = "SELECT c.customer_name, c.contact_number, t.tehsil_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "WHERE c.customer_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, customerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("customer_name");
                    String contact = rs.getString("contact_number");
                    String tehsil = rs.getString("tehsil_name");
                    if (tehsil == null) tehsil = "";
                    
                    // Get current balance including invoice history
                    double currentBalance = getCustomerCurrentBalance(name);
                    
                    return new Customer(name, contact, tehsil, currentBalance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Supplier getSupplierWithCurrentBalance(String supplierName) {
        String query = "SELECT s.supplier_name, s.contact_number, t.tehsil_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "WHERE s.supplier_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("supplier_name");
                    String contact = rs.getString("contact_number");
                    String tehsil = rs.getString("tehsil_name");
                    if (tehsil == null) tehsil = "";
                    
                    // Get current balance including invoice history
                    double currentBalance = getSupplierCurrentBalance(name);
                    
                    return new Supplier(name, contact, tehsil, currentBalance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Object[]> getCustomerBalanceSummary() {
        List<Object[]> summary = new ArrayList<>();
        String query = "SELECT c.customer_name, c.contact_number, c.balance as initial_balance, t.tehsil_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "ORDER BY c.customer_name";
        
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String customerName = rs.getString("customer_name");
                String contact = rs.getString("contact_number");
                double initialBalance = rs.getDouble("initial_balance");
                String tehsil = rs.getString("tehsil_name");
                if (tehsil == null) tehsil = "";
                
                double currentBalance = getCustomerCurrentBalance(customerName);
                
                Object[] row = {
                    customerName,
                    contact,
                    tehsil,
                    initialBalance,
                    currentBalance,
                    (currentBalance - initialBalance) // Net change from invoices
                };
                summary.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    @Override
    public List<Object[]> getSupplierBalanceSummary() {
        List<Object[]> summary = new ArrayList<>();
        String query = "SELECT s.supplier_name, s.contact_number, s.balance as initial_balance, t.tehsil_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "ORDER BY s.supplier_name";
        
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String supplierName = rs.getString("supplier_name");
                String contact = rs.getString("contact_number");
                double initialBalance = rs.getDouble("initial_balance");
                String tehsil = rs.getString("tehsil_name");
                if (tehsil == null) tehsil = "";
                
                double currentBalance = getSupplierCurrentBalance(supplierName);
                
                Object[] row = {
                    supplierName,
                    contact,
                    tehsil,
                    initialBalance,
                    currentBalance,
                    (currentBalance - initialBalance) // Net change from invoices
                };
                summary.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    // --------------------------
    // Customer Update Operations
    // --------------------------
    
    @Override
    public boolean updateCustomer(int customerId, String name, String contact, String tehsilName) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String updateQuery = "UPDATE Customer SET customer_name = ?, contact_number = ?, tehsil_id = ? WHERE customer_id = ?";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, name);
                        updateStmt.setString(2, contact);
                        updateStmt.setInt(3, tehsilId);
                        updateStmt.setInt(4, customerId);
                        
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public Customer getCustomerById(int customerId) {
        String query = "SELECT c.customer_name, c.contact_number, t.tehsil_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "WHERE c.customer_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("customer_name");
                    String contact = rs.getString("contact_number");
                    String tehsil = rs.getString("tehsil_name");
                    if (tehsil == null) tehsil = "";
                    
                    // Get current balance including invoice history
                    double currentBalance = getCustomerCurrentBalance(name);
                    
                    return new Customer(name, contact, tehsil, currentBalance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public int getCustomerIdByName(String customerName) {
        String query = "SELECT customer_id FROM Customer WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, customerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if customer not found
    }

    // --------------------------
    // Customer Payment and Ledger Operations
    // --------------------------
    
    @Override
    public boolean addCustomerPayment(String customerName, double paymentAmount, String paymentDate, String description) {
        int customerId = getCustomerIdByName(customerName);
        if (customerId == -1) {
            return false; // Customer not found
        }
        return addCustomerPayment(customerId, paymentAmount, paymentDate, description);
    }
    
    @Override
    public boolean addCustomerPayment(int customerId, double paymentAmount, String paymentDate, String description) {
        try {
            connection.setAutoCommit(false);
            
            // Get current balance
            double currentBalance = 0.0;
            String getBalanceQuery = "SELECT balance_after_transaction FROM Customer_Transaction " +
                                   "WHERE customer_id = ? ORDER BY transaction_id DESC LIMIT 1";
            try (PreparedStatement pstmt = connection.prepareStatement(getBalanceQuery)) {
                pstmt.setInt(1, customerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getDouble("balance_after_transaction");
                    } else {
                        // If no transactions exist, get initial balance from Customer table
                        currentBalance = getCustomerCurrentBalance(getCustomerNameById(customerId));
                    }
                }
            }
            
            // Calculate new balance after payment (payment reduces the balance owed)
            double newBalance = currentBalance - paymentAmount;
            
            // Insert payment transaction
            String insertQuery = "INSERT INTO Customer_Transaction " +
                               "(customer_id, transaction_date, transaction_type, amount, description, balance_after_transaction) " +
                               "VALUES (?, ?, 'payment_received', ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, customerId);
                insertStmt.setString(2, paymentDate);
                insertStmt.setDouble(3, paymentAmount);
                insertStmt.setString(4, description);
                insertStmt.setDouble(5, newBalance);
                
                if (insertStmt.executeUpdate() > 0) {
                    // Also update the Customer table balance field
                    String updateCustomerBalanceQuery = "UPDATE Customer SET balance = balance - ? WHERE customer_id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateCustomerBalanceQuery)) {
                        updateStmt.setDouble(1, paymentAmount);
                        updateStmt.setInt(2, customerId);
                        updateStmt.executeUpdate();
                        
                        System.out.println("DEBUG: Customer balance reduced by payment: " + paymentAmount);
                        connection.commit();
                        return true;
                    }
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Helper method to get customer name by ID
     */
    private String getCustomerNameById(int customerId) {
        String query = "SELECT customer_name FROM Customer WHERE customer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("customer_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    @Override
    public List<Object[]> getCustomerLedger(String customerName) {
        int customerId = getCustomerIdByName(customerName);
        if (customerId == -1) {
            return new ArrayList<>(); // Return empty list if customer not found
        }
        return getCustomerLedger(customerId);
    }
    
    @Override
    public List<Object[]> getCustomerLedger(int customerId) {
        List<Object[]> ledger = new ArrayList<>();
        String query = "SELECT transaction_date, transaction_type, amount, description, " +
                      "balance_after_transaction, reference_invoice_number " +
                      "FROM Customer_Transaction " +
                      "WHERE customer_id = ? " +
                      "ORDER BY transaction_id ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] transaction = {
                        rs.getString("transaction_date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getDouble("balance_after_transaction"),
                        rs.getString("reference_invoice_number")
                    };
                    ledger.add(transaction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ledger;
    }
    
    @Override
    public List<Object[]> getCustomerLedgerByDateRange(String customerName, String startDate, String endDate) {
        List<Object[]> ledger = new ArrayList<>();
        int customerId = getCustomerIdByName(customerName);
        if (customerId == -1) {
            return ledger; // Return empty list if customer not found
        }
        
        String query = "SELECT transaction_date, transaction_type, amount, description, " +
                      "balance_after_transaction, reference_invoice_number " +
                      "FROM Customer_Transaction " +
                      "WHERE customer_id = ? AND transaction_date BETWEEN ? AND ? " +
                      "ORDER BY transaction_id ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] transaction = {
                        rs.getString("transaction_date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getDouble("balance_after_transaction"),
                        rs.getString("reference_invoice_number")
                    };
                    ledger.add(transaction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ledger;
    }

    @Override
    public List<String> getAllUnits() {
        List<String> units = new ArrayList<>();
        String query = "SELECT unit_name FROM Unit ORDER BY unit_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                units.add(rs.getString("unit_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return units;
    }

    /**
     * Get unit_id by unit name
     */
    public int getUnitIdByName(String unitName) {
        if (unitName == null || unitName.trim().isEmpty()) {
            return 1; // Default to first unit (Piece)
        }
        
        String query = "SELECT unit_id FROM Unit WHERE unit_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unit_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If unit not found, try to insert it and return the ID
        return insertUnitAndGetId(unitName.trim());
    }
    
    /**
     * Insert new unit and return its ID
     */
    private int insertUnitAndGetId(String unitName) {
        String query = "INSERT INTO Unit (unit_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, unitName);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int unitId = generatedKeys.getInt(1);
                        System.out.println("Auto-created new unit: " + unitName + " with ID: " + unitId);
                        return unitId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting unit " + unitName + ": " + e.getMessage());
        }
        
        return 1; // Default to first unit if all else fails
    }

    /**
     * Get unit name by unit_id
     */
    public String getUnitNameById(int unitId) {
        String query = "SELECT unit_name FROM Unit WHERE unit_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, unitId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("unit_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return "Piece"; // Default unit name
    }

    @Override
    public boolean insertUnit(String unitName) {
        String query = "INSERT INTO Unit (unit_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean unitExists(String unitName) {
        String query = "SELECT COUNT(*) FROM Unit WHERE unit_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertUser(String username, String password, String role) {
        ensureUserTableExists();
        String query = "INSERT INTO User (username, password_hash, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role != null ? role : "user");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean userExists(String username) {
        ensureUserTableExists();
        String query = "SELECT COUNT(*) FROM User WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        ensureUserTableExists();
        
        // First verify old password
        if (!SignIn(username, oldPassword)) {
            return false;
        }
        
        // Update password
        String query = "UPDATE User SET password_hash = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllUsers() {
        ensureUserTableExists();
        List<String> users = new ArrayList<>();
        String query = "SELECT username FROM User WHERE is_active = 1 ORDER BY username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // --------------------------
    // Supplier Update Operations
    // --------------------------
    
    @Override
    public boolean updateSupplier(int supplierId, String name, String contact, String tehsilName) {
        String getTehsilIdQuery = "SELECT tehsil_id FROM Tehsil WHERE tehsil_name = ?";
        String updateQuery = "UPDATE Supplier SET supplier_name = ?, contact_number = ?, tehsil_id = ? WHERE supplier_id = ?";
        
        try (PreparedStatement getTehsilStmt = connection.prepareStatement(getTehsilIdQuery)) {
            getTehsilStmt.setString(1, tehsilName);
            
            try (ResultSet rs = getTehsilStmt.executeQuery()) {
                if (rs.next()) {
                    int tehsilId = rs.getInt("tehsil_id");
                    
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, name);
                        updateStmt.setString(2, contact);
                        updateStmt.setInt(3, tehsilId);
                        updateStmt.setInt(4, supplierId);
                        
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public Supplier getSupplierById(int supplierId) {
        String query = "SELECT s.supplier_name, s.contact_number, t.tehsil_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "WHERE s.supplier_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("supplier_name");
                    String contact = rs.getString("contact_number");
                    String tehsil = rs.getString("tehsil_name");
                    if (tehsil == null) tehsil = "";
                    
                    // Get current balance including invoice history
                    double currentBalance = getSupplierCurrentBalance(name);
                    
                    return new Supplier(name, contact, tehsil, currentBalance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public int getSupplierIdByName(String supplierName) {
        String query = "SELECT supplier_id FROM Supplier WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("supplier_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Supplier not found
    }

    // --------------------------
    // Supplier Payment and Ledger Operations
    // --------------------------
    
    @Override
    public boolean addSupplierPayment(String supplierName, double paymentAmount, String paymentDate, String description) {
        int supplierId = getSupplierIdByName(supplierName);
        if (supplierId == -1) {
            return false; // Supplier not found
        }
        return addSupplierPayment(supplierId, paymentAmount, paymentDate, description);
    }
    
    @Override
    public boolean addSupplierPayment(int supplierId, double paymentAmount, String paymentDate, String description) {
        try {
            connection.setAutoCommit(false);
            
            // Update supplier balance directly: payment reduces the balance owed to supplier
            String updateSupplierBalanceQuery = "UPDATE Supplier SET balance = balance - ? WHERE supplier_id = ?";
            try (PreparedStatement balanceStmt = connection.prepareStatement(updateSupplierBalanceQuery)) {
                balanceStmt.setDouble(1, paymentAmount);
                balanceStmt.setInt(2, supplierId);
                balanceStmt.executeUpdate();
                
                System.out.println("DEBUG: Supplier balance reduced by payment amount: " + paymentAmount);
            }
            
            // Insert payment transaction for record keeping
            String insertQuery = "INSERT INTO Supplier_Transaction " +
                               "(supplier_id, transaction_date, transaction_type, amount, description, balance_after_transaction) " +
                               "VALUES (?, ?, 'payment_made', ?, ?, ?)";
            
            // Get updated balance for transaction record
            double updatedBalance = 0.0;
            String getBalanceQuery = "SELECT balance FROM Supplier WHERE supplier_id = ?";
            try (PreparedStatement getBalanceStmt = connection.prepareStatement(getBalanceQuery)) {
                getBalanceStmt.setInt(1, supplierId);
                try (ResultSet rs = getBalanceStmt.executeQuery()) {
                    if (rs.next()) {
                        updatedBalance = rs.getDouble("balance");
                    }
                }
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                pstmt.setInt(1, supplierId);
                pstmt.setString(2, paymentDate);
                pstmt.setDouble(3, paymentAmount);
                pstmt.setString(4, description);
                pstmt.setDouble(5, updatedBalance);
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    connection.commit();
                    connection.setAutoCommit(true);
                    return true;
                } else {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public List<Object[]> getSupplierLedger(String supplierName) {
        int supplierId = getSupplierIdByName(supplierName);
        if (supplierId == -1) {
            return new ArrayList<>(); // Supplier not found
        }
        return getSupplierLedger(supplierId);
    }
    
    @Override
    public List<Object[]> getSupplierLedger(int supplierId) {
        List<Object[]> ledger = new ArrayList<>();
        String query = "SELECT transaction_date, transaction_type, amount, description, balance_after_transaction, reference_invoice_number " +
                      "FROM Supplier_Transaction " +
                      "WHERE supplier_id = ? " +
                      "ORDER BY transaction_date, transaction_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] transaction = {
                        rs.getString("transaction_date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getDouble("balance_after_transaction"),
                        rs.getString("reference_invoice_number")
                    };
                    ledger.add(transaction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ledger;
    }
    
    @Override
    public List<Object[]> getSupplierLedgerByDateRange(String supplierName, String startDate, String endDate) {
        int supplierId = getSupplierIdByName(supplierName);
        if (supplierId == -1) {
            return new ArrayList<>(); // Supplier not found
        }
        
        List<Object[]> ledger = new ArrayList<>();
        String query = "SELECT transaction_date, transaction_type, amount, description, balance_after_transaction, reference_invoice_number " +
                      "FROM Supplier_Transaction " +
                      "WHERE supplier_id = ? AND transaction_date BETWEEN ? AND ? " +
                      "ORDER BY transaction_date, transaction_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] transaction = {
                        rs.getString("transaction_date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getDouble("balance_after_transaction"),
                        rs.getString("reference_invoice_number")
                    };
                    ledger.add(transaction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ledger;
    }

    // Helper method to get supplier name by ID
    private String getSupplierNameById(int supplierId) {
        String query = "SELECT supplier_name FROM Supplier WHERE supplier_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("supplier_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --------------------------
    // Raw Stock Operations
    // --------------------------
    @Override
    public List<Object[]> getAllRawStocks() {
        List<Object[]> rawStocks = new ArrayList<>();
    String query = "SELECT rs.stock_id, rs.item_name, c.category_name, m.manufacturer_name, " +
                "b.brand_name, u.unit_name, rs.quantity, rs.unit_price, rs.total_cost " +
                "FROM Raw_Stock rs " +
                "JOIN Category c ON rs.category_id = c.category_id " +
                "JOIN Manufacturer m ON rs.manufacturer_id = m.manufacturer_id " +
                "JOIN Brand b ON rs.brand_id = b.brand_id " +
                "LEFT JOIN Unit u ON rs.unit_id = u.unit_id " +
                "ORDER BY rs.item_name";

        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("stock_id"),          // row[0]
                    rs.getString("item_name"),      // row[1]
                    rs.getString("brand_name"),     // row[2]
                    rs.getString("category_name"),  // row[3]
                    rs.getString("manufacturer_name"), // row[4]
                    rs.getString("unit_name") != null ? rs.getString("unit_name") : "N/A", // row[5]
                    (double) rs.getInt("quantity"), // row[6] - Convert int to double for consistency
                    rs.getDouble("unit_price"),     // row[7]
                    rs.getDouble("total_cost")      // row[8]
                };
                rawStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rawStocks;
    }

    @Override
    public boolean insertRawStock(String name, String category, String brand, String unit, 
                                double openingQty, double purchasePrice, double reorderLevel) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Invalid item name for insertRawStock: null or empty");
            return false;
        }
        
        // Get unit_id from unit name
        int unitId = getUnitIdByName(unit);
        
        // Get category_id from category name
        int categoryId = getCategoryIdByName(category);
        
        double totalCost = openingQty * purchasePrice;
        int quantity = (int) Math.round(openingQty);
        
        String query = "INSERT INTO Raw_Stock (item_name, category_id, manufacturer_id, brand_id, unit_id, quantity, unit_price, total_cost, supplier_id) " +
                    "SELECT ?, ?, b.manufacturer_id, b.brand_id, ?, ?, ?, ?, ? FROM Brand b WHERE b.brand_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, unitId);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, purchasePrice);
            pstmt.setDouble(6, totalCost);
            pstmt.setInt(7, 1); // Use supplier_id = 1 (matches 'rewf')
            pstmt.setString(8, brand);
            
            System.out.println("Attempting to insert Raw_Stock: item_name=" + name + ", category=" + category + 
                            " (category_id=" + categoryId + "), brand=" + brand + 
                            ", unit=" + unit + " (unit_id=" + unitId + "), quantity=" + quantity + 
                            ", unit_price=" + purchasePrice + ", supplier_id=1");
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("Failed to insert Raw_Stock: no rows affected for item " + name + " with brand " + brand);
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int stockId = generatedKeys.getInt(1);
                    System.out.println("Successfully inserted Raw_Stock with stock_id: " + stockId);
                    return true;
                } else {
                    System.err.println("No generated key returned for Raw_Stock: " + name);
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting Raw_Stock for item " + name + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateRawStock(Integer id, String name, String brand, String unit, double quantity, double unitPrice) {
        if (id == null || id <= 0 || name == null || name.trim().isEmpty()) {
            System.err.println("Invalid parameters for updateRawStock");
            return false;
        }
        
        // Get unit_id from unit name
        int unitId = getUnitIdByName(unit);
        if (unitId <= 0) {
            System.err.println("Invalid unit name for updateRawStock: " + unit);
            return false;
        }
        
        // Calculate total cost
        double totalCost = quantity * unitPrice;
        
        // Update raw stock
        String query = "UPDATE Raw_Stock SET item_name = ?, unit_id = ?, quantity = ?, unit_price = ?, total_cost = ? " +
                      "WHERE stock_id = ? AND brand_id = (SELECT brand_id FROM Brand WHERE brand_name = ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, unitId);
            pstmt.setDouble(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.setDouble(5, totalCost);
            pstmt.setInt(6, id);
            pstmt.setString(7, brand);
            
            System.out.println("Attempting to update Raw_Stock with stock_id=" + id + 
                               ": name=" + name + ", brand=" + brand + ", unit_id=" + unitId + 
                               ", quantity=" + quantity + ", unit_price=" + unitPrice);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("Failed to update Raw_Stock: no rows affected for stock_id " + id);
                return false;
            }
            
            System.out.println("Successfully updated Raw_Stock with stock_id: " + id);
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating Raw_Stock for stock_id " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    

    @Override
    public boolean insertRawPurchaseInvoice(String invoiceNumber, int supplierId, String invoiceDate, 
                                           double totalAmount, double discountAmount, double paidAmount) {
        String query = "INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, " +
                      "total_amount, discount_amount, paid_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, supplierId);
            pstmt.setString(3, invoiceDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public int insertRawPurchaseInvoiceAndGetId(String invoiceNumber, int supplierId, String invoiceDate, 
                                              double totalAmount, double discountAmount, double paidAmount) {
        String query = "INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, " +
                      "total_amount, discount_amount, paid_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, supplierId);
            pstmt.setString(3, invoiceDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if failed
    }

    // New methods for enhanced invoice functionality
    @Override
    public String generateNextInvoiceNumber(String prefix) {
        String query = "SELECT MAX(CAST(SUBSTR(invoice_number, LENGTH(?) + 1) AS INTEGER)) " +
                      "FROM Raw_Purchase_Invoice WHERE invoice_number LIKE ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, prefix);
            pstmt.setString(2, prefix + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int maxNumber = rs.getInt(1);
                    return prefix + String.format("%06d", maxNumber + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If no invoices found or error, start with 000001
        return prefix + "000001";
    }
    
    @Override
    public List<Object[]> getAllRawStocksForDropdown() {
        List<Object[]> rawStocks = new ArrayList<>();
        String query = "SELECT rs.stock_id, rs.item_name, c.category_name, b.brand_name, " +
                      "u.unit_name, rs.unit_price, m.manufacturer_name FROM Raw_Stock rs " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "LEFT JOIN Unit u ON rs.unit_id = u.unit_id " +
                      "LEFT JOIN Category c ON rs.category_id = c.category_id " +
                      "LEFT JOIN Manufacturer m ON rs.manufacturer_id = m.manufacturer_id " +
                      "ORDER BY rs.item_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("stock_id"),          // row[0]
                    rs.getString("item_name"),      // row[1]
                    rs.getString("category_name") != null ? rs.getString("category_name") : "General", // row[2]
                    rs.getString("brand_name"),     // row[3]
                    rs.getString("unit_name") != null ? rs.getString("unit_name") : "Piece", // row[4]
                    rs.getDouble("unit_price"),     // row[5]
                    rs.getString("manufacturer_name") != null ? rs.getString("manufacturer_name") : "N/A" // row[6]
                };
                rawStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rawStocks;
    }
    
    @Override
    public List<String> getAllSupplierNames() {
        List<String> supplierNames = new ArrayList<>();
        String query = "SELECT supplier_name FROM Supplier ORDER BY supplier_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                supplierNames.add(rs.getString("supplier_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return supplierNames;
    }
    
    public int getRawStockIdByName(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            System.err.println("Invalid item name provided to getRawStockIdByName: null or empty");
            return -1;
        }
        String query = "SELECT stock_id FROM Raw_Stock WHERE item_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, itemName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int stockId = rs.getInt("stock_id");
                System.out.println("Found stock_id: " + stockId + " for item_name: " + itemName);
                return stockId;
            } else {
                System.out.println("No stock_id found for item_name: " + itemName);
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving stock_id for item_name " + itemName + ": " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }


    @Override
    public boolean ensureBrandExists(String brandName, int tehsilId) {
        String checkBrandQuery = "SELECT brand_id FROM Brand WHERE brand_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkBrandQuery)) {
            pstmt.setString(1, brandName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true; // Brand exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking brand existence: " + e.getMessage());
            return false;
        }

        // Insert Default Brand with a default manufacturer_id (no tehsil_id needed)
        String insertBrandQuery = "INSERT INTO Brand (brand_name, manufacturer_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertBrandQuery)) {
            pstmt.setString(1, brandName);
            pstmt.setInt(2, 1); // Assume manufacturer_id = 1 exists; adjust as needed
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Inserted Default Brand: " + brandName);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to insert brand " + brandName + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean insertSimpleRawPurchaseInvoice(String invoiceNumber, String supplierName, String invoiceDate,
                                                double totalAmount, double discountAmount, double paidAmount,
                                                List<RawStockPurchaseItem> items) {
        try {
            connection.setAutoCommit(false); // Start transaction
            System.out.println("Starting insertSimpleRawPurchaseInvoice: invoiceNumber=" + invoiceNumber + ", supplierName=" + supplierName);

            // 1. Validate inputs
            if (items == null || items.isEmpty()) {
                System.err.println("Error: Items list is null or empty for invoice " + invoiceNumber);
                connection.rollback();
                return false;
            }
            System.out.println("Items list size: " + items.size());
            for (int i = 0; i < items.size(); i++) {
                RawStockPurchaseItem item = items.get(i);
                System.out.println("Item " + (i + 1) + ": name=" + (item != null ? item.getRawStockName() : "null") + 
                                ", quantity=" + (item != null ? item.getQuantity() : "null") + 
                                ", unit_price=" + (item != null ? item.getUnitPrice() : "null"));
            }

            // 2. Get supplier_id
            int supplierId = getSupplierIdByName(supplierName);
            if (supplierId == -1) {
                System.err.println("Supplier not found: " + supplierName);
                connection.rollback();
                return false;
            }
            System.out.println("Found supplier_id: " + supplierId + " for supplier: " + supplierName);

            // 3. Get a valid tehsil_id
            int tehsilId = -1;
            String getTehsilQuery = "SELECT tehsil_id FROM Tehsil LIMIT 1";
            try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(getTehsilQuery)) {
                if (rs.next()) {
                    tehsilId = rs.getInt("tehsil_id");
                    System.out.println("Found tehsil_id: " + tehsilId);
                } else {
                    System.err.println("No tehsil found in Tehsil table");
                    connection.rollback();
                    return false;
                }
            }

            // 4. Ensure Default Brand exists
            if (!ensureBrandExists("Default Brand", tehsilId)) {
                System.err.println("Failed to ensure Default Brand exists");
                connection.rollback();
                return false;
            }

            // 5. Insert into Raw_Purchase_Invoice
            String insertInvoiceQuery = "INSERT INTO Raw_Purchase_Invoice (invoice_number, supplier_id, invoice_date, total_amount, discount_amount, paid_amount) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";
            int rawPurchaseInvoiceId;
            try (PreparedStatement pstmt = connection.prepareStatement(insertInvoiceQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, invoiceNumber);
                pstmt.setInt(2, supplierId);
                pstmt.setString(3, invoiceDate);
                pstmt.setDouble(4, totalAmount);
                pstmt.setDouble(5, discountAmount);
                pstmt.setDouble(6, paidAmount);

                System.out.println("Inserting Raw_Purchase_Invoice: invoiceNumber=" + invoiceNumber + ", supplierId=" + supplierId +
                                ", invoiceDate=" + invoiceDate + ", totalAmount=" + totalAmount +
                                ", discountAmount=" + discountAmount + ", paidAmount=" + paidAmount);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    System.err.println("Failed to insert into Raw_Purchase_Invoice");
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rawPurchaseInvoiceId = generatedKeys.getInt(1);
                        System.out.println("Inserted Raw_Purchase_Invoice with ID: " + rawPurchaseInvoiceId);
                    } else {
                        System.err.println("Failed to retrieve generated invoice ID");
                        connection.rollback();
                        return false;
                    }
                }
            }

            // 6. Insert items into Raw_Purchase_Invoice_Item
            String insertItemQuery = "INSERT INTO Raw_Purchase_Invoice_Item (raw_purchase_invoice_id, raw_stock_id, quantity, unit_price) " +
                        "VALUES (?, ?, ?, ?)";
            // Print all items that are going to be inserted
            System.out.println("Items to be inserted into Raw_Purchase_Invoice_Item:");
            for (RawStockPurchaseItem item : items) {
                System.out.println("Raw Purchase Invoice ID: " + rawPurchaseInvoiceId +
                ", Raw Stock ID: " + (item != null ? item.getRawStockId() : "null") +
                ", Quantity: " + (item != null ? item.getQuantity() : "null") +
                ", Unit Price: " + (item != null ? item.getUnitPrice() : "null"));
            }
                    // Disable foreign key checks
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = OFF");
            }
            try (PreparedStatement pstmt = connection.prepareStatement(insertItemQuery)) {
                for (RawStockPurchaseItem item : items) {
                    try {
                        if (item == null || item.getRawStockName() == null || item.getRawStockName().trim().isEmpty()) {
                            System.err.println("Invalid item: null or empty RawStockName");
                            connection.rollback();
                            return false;
                        }
                        System.out.println("Processing item: " + item.getRawStockName() + ", quantity=" + item.getQuantity() + 
                                        ", unit_price=" + item.getUnitPrice());

                        int rawStockId = getRawStockIdByName(item.getRawStockName());
                        System.out.println("Initial getRawStockIdByName for " + item.getRawStockName() + ": " + rawStockId);
                        if (rawStockId == -1) {
                            boolean inserted = insertRawStock(item.getRawStockName(), "General", "Default Brand", "Piece", 0, item.getUnitPrice(), 0);
                            if (!inserted) {
                                System.err.println("Failed to insert new raw stock: " + item.getRawStockName());
                                connection.rollback();
                                return false;
                            }
                            rawStockId = getRawStockIdByName(item.getRawStockName());
                            System.out.println("Post-insert getRawStockIdByName for " + item.getRawStockName() + ": " + rawStockId);
                            if (rawStockId == -1) {
                                System.err.println("Failed to retrieve new raw stock ID for: " + item.getRawStockName());
                                connection.rollback();
                                return false;
                            }
                        }

                        // Verify raw_stock_id exists in Raw_Stock
                        String verifyStockQuery = "SELECT stock_id FROM Raw_Stock WHERE stock_id = ?";
                        try (PreparedStatement verifyStmt = connection.prepareStatement(verifyStockQuery)) {
                            verifyStmt.setInt(1, rawStockId);
                            ResultSet rs = verifyStmt.executeQuery();
                            if (!rs.next()) {
                                System.err.println("Invalid raw_stock_id: " + rawStockId + " does not exist in Raw_Stock for item: " + item.getRawStockName());
                                connection.rollback();
                                return false;
                            }
                        }

                        pstmt.setInt(1, rawPurchaseInvoiceId);
                        pstmt.setInt(2, rawStockId);
                        pstmt.setInt(3, item.getQuantity().intValue());
                        pstmt.setDouble(4, item.getUnitPrice());
                        System.out.println("Adding batch for Raw_Purchase_Invoice_Item: raw_purchase_invoice_id=" + rawPurchaseInvoiceId +
                                        ", raw_stock_id=" + rawStockId + ", quantity=" + item.getQuantity() +
                                        ", unit_price=" + item.getUnitPrice());
                        pstmt.addBatch();
                    } catch (SQLException e) {
                        System.err.println("SQLException in item loop for " + item.getRawStockName() + ": " + e.getMessage());
                        e.printStackTrace();
                        connection.rollback();
                        return false;
                    }
                }
                System.out.println("Executing batch insert for Raw_Purchase_Invoice_Item");
                pstmt.executeBatch();
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            // 7. Update Raw_Stock quantities
            String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity + ?, total_cost = total_cost + ? WHERE stock_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateStockQuery)) {
                for (RawStockPurchaseItem item : items) {
                    try {
                        if (item == null || item.getRawStockName() == null || item.getRawStockName().trim().isEmpty()) {
                            System.err.println("Invalid item for stock update: null or empty RawStockName");
                            connection.rollback();
                            return false;
                        }
                        int rawStockId = getRawStockIdByName(item.getRawStockName());
                        if (rawStockId == -1) {
                            System.err.println("Raw stock ID not found for update: " + item.getRawStockName());
                            connection.rollback();
                            return false;
                        }
                        double totalCost = item.getQuantity() * item.getUnitPrice();
                        System.out.println("Updating stock for item: " + item.getRawStockName() + 
                                        ", Quantity: " + item.getQuantity() + ", Total Cost: " + totalCost);

                        pstmt.setInt(1, item.getQuantity().intValue());
                        pstmt.setDouble(2, totalCost);
                        pstmt.setInt(3, rawStockId);
                        pstmt.addBatch();
                    } catch (SQLException e) {
                        System.err.println("SQLException in stock update loop for " + item.getRawStockName() + ": " + e.getMessage());
                        e.printStackTrace();
                        connection.rollback();
                        return false;
                    }
                }
                System.out.println("Executing batch update for Raw_Stock");
                pstmt.executeBatch();
            }

            // Update supplier balance: add net amount owed (total - discount - paid)
            double netAmountOwed = (totalAmount - discountAmount) - paidAmount;
            if (netAmountOwed > 0) {
                String updateSupplierBalanceQuery = "UPDATE Supplier SET balance = balance + ? WHERE supplier_id = ?";
                try (PreparedStatement balanceStmt = connection.prepareStatement(updateSupplierBalanceQuery)) {
                    balanceStmt.setDouble(1, netAmountOwed);
                    balanceStmt.setInt(2, supplierId);
                    balanceStmt.executeUpdate();
                    
                    System.out.println("DEBUG: Supplier balance increased by net amount owed: " + netAmountOwed + 
                                     " (total: " + totalAmount + ", discount: " + discountAmount + ", paid: " + paidAmount + ")");
                }
            }

            connection.commit();
            System.out.println("Successfully inserted Raw_Purchase_Invoice and items for invoice: " + invoiceNumber);
            return true;
        } catch (SQLException e) {
            System.err.println("SQLException during insertSimpleRawPurchaseInvoice: " + e.getMessage());
            e.printStackTrace();
            try {
                connection.rollback();
                System.err.println("Rolled back transaction due to error");
            } catch (SQLException rollbackEx) {
                System.err.println("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to restore auto-commit: " + e.getMessage());
            }
        }
    }
    
    @Override
    public List<Object[]> getAllRawPurchaseInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT rpi.invoice_number, rpi.invoice_date, s.supplier_name, " +
                      "rpi.total_amount, rpi.discount_amount, rpi.paid_amount " +
                      "FROM Raw_Purchase_Invoice rpi " +
                      "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                      "ORDER BY rpi.invoice_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("invoice_number"),
                    rs.getString("invoice_date"),
                    rs.getString("supplier_name"),
                    rs.getDouble("total_amount"),
                    rs.getDouble("discount_amount"),
                    rs.getDouble("paid_amount")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    @Override
    public List<Object[]> getAllRawStockUsage() {
        List<Object[]> usage = new ArrayList<>();
        String query = "SELECT rsu.usage_date, rs.raw_stock_name, rsu.quantity_used, rsu.reference " +
                      "FROM RawStock_Usage rsu " +
                      "JOIN Raw_Stock rs ON rsu.raw_stock_id = rs.stock_id " +
                      "ORDER BY rsu.usage_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("usage_date"),
                    rs.getString("raw_stock_name"),
                    rs.getDouble("quantity_used"),
                    rs.getString("reference")
                };
                usage.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usage;
    }

    // --------------------------
    // Raw Purchase Return Invoice Operations
    // --------------------------
    
    /**
     * Generate auto-increment return invoice number
     */
    public String generateReturnInvoiceNumber() {
        String query = "SELECT COUNT(*) + 1 as next_id FROM Raw_Purchase_Return_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                return String.format("INV-RPR-%03d", nextId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "INV-RPR-001"; // fallback
    }
    
    /**
     * Get all raw purchase invoices for dropdown selection
     */
    public List<Object[]> getAllRawPurchaseInvoicesForDropdown() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT rpi.raw_purchase_invoice_id, rpi.invoice_number, s.supplier_name, " +
                      "rpi.invoice_date, rpi.total_amount " +
                      "FROM Raw_Purchase_Invoice rpi " +
                      "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                      "ORDER BY rpi.invoice_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("raw_purchase_invoice_id"),
                    rs.getString("invoice_number"),
                    rs.getString("supplier_name"),
                    rs.getString("invoice_date"),
                    rs.getDouble("total_amount")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    /**
     * Get raw stock items from a specific purchase invoice
     */
    public List<Object[]> getRawStockItemsByInvoiceId(int invoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT rpii.raw_stock_id, rs.item_name, c.category_name, " +
                      "b.brand_name, COALESCE(u.unit_name, 'Piece') as unit_name, rpii.quantity, rpii.unit_price, " +
                      "m.manufacturer_name " +
                      "FROM Raw_Purchase_Invoice_Item rpii " +
                      "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "JOIN Category c ON rs.category_id = c.category_id " +
                      "JOIN Manufacturer m ON rs.manufacturer_id = m.manufacturer_id " +
                      "LEFT JOIN Unit u ON rs.unit_id = u.unit_id " +
                      "WHERE rpii.raw_purchase_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, invoiceId);
            System.out.println("DEBUG: Executing getRawStockItemsByInvoiceId with invoiceId: " + invoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("raw_stock_id"),        // index 0
                    rs.getString("item_name"),        // index 1 - rawStockName
                    rs.getString("brand_name"),       // index 2 - brandName
                    rs.getDouble("quantity"),         // index 3 - quantity
                    rs.getDouble("unit_price"),       // index 4 - unitPrice
                    rs.getString("unit_name"),        // index 5 - unitName
                    rs.getString("manufacturer_name"), // index 6 - manufacturerName
                    rs.getString("category_name")     // index 7 - categoryName
                };
                System.out.println("DEBUG: Fetched raw stock item: rawStockId=" + row[0] + ", name=" + row[1] + ", brand=" + row[2]);
                items.add(row);
            }
            System.out.println("DEBUG: Total raw stock items fetched for invoice " + invoiceId + ": " + items.size());
        } catch (SQLException e) {
            System.err.println("Error fetching raw stock items for invoice " + invoiceId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Insert raw purchase return invoice and return the generated ID
     */
    public int insertRawPurchaseReturnInvoiceAndGetId(String returnInvoiceNumber, int originalInvoiceId, 
                                                     int supplierId, String returnDate, double totalReturnAmount) {
        String insertQuery = "INSERT INTO Raw_Purchase_Return_Invoice " +
                           "(return_invoice_number, original_invoice_id, supplier_id, return_date, total_return_amount) " +
                           "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, returnInvoiceNumber);
            pstmt.setInt(2, originalInvoiceId);
            pstmt.setInt(3, supplierId);
            pstmt.setString(4, returnDate);
            pstmt.setDouble(5, totalReturnAmount);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Get current stock quantity for a raw stock item
     */
    private double getCurrentStockQuantity(int stockId) throws SQLException {
        String query = "SELECT quantity FROM Raw_Stock WHERE stock_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, stockId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("quantity");
                }
            }
        }
        return 0.0;
    }

    /**
     * Get current stock quantity for a raw stock item (public method)
     */
    public double getCurrentRawStockQuantity(int stockId) {
        try {
            return getCurrentStockQuantity(stockId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Insert raw purchase return invoice items and update stock quantities
     */
    public boolean insertRawPurchaseReturnInvoiceItems(int returnInvoiceId, 
                                                      List<com.cablemanagement.model.RawStockPurchaseItem> items) {
        String insertQuery = "INSERT INTO Raw_Purchase_Return_Invoice_Item " +
                           "(raw_purchase_return_invoice_id, raw_stock_id, quantity, unit_price) " +
                           "VALUES (?, ?, ?, ?)";
        
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // First validate that we have enough stock for all items
            for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                double currentStock = getCurrentStockQuantity(item.getRawStockId());
                if (currentStock < item.getQuantity()) {
                    System.err.println("Insufficient stock for item ID " + item.getRawStockId() + 
                                     ": Current=" + currentStock + ", Requested=" + item.getQuantity());
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }
            }
            
            // Insert return invoice items
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                    pstmt.setInt(1, returnInvoiceId);
                    pstmt.setInt(2, item.getRawStockId());
                    pstmt.setDouble(3, item.getQuantity());
                    pstmt.setDouble(4, item.getUnitPrice());
                    pstmt.addBatch();
                }
                
                int[] insertResults = pstmt.executeBatch();
                
                // Check if all items were inserted successfully
                for (int result : insertResults) {
                    if (result <= 0) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            // Update raw stock quantities (reduce stock as items are being returned to supplier)
            String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity - ?, total_cost = total_cost - ? " +
                                    "WHERE stock_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                    double totalCostReduction = item.getQuantity() * item.getUnitPrice();
                    updatePstmt.setDouble(1, item.getQuantity()); // reduce quantity
                    updatePstmt.setDouble(2, totalCostReduction); // reduce total cost
                    updatePstmt.setInt(3, item.getRawStockId());
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            // Update supplier balance: subtract return amount (we owe supplier less)
            // First, get supplier ID from the return invoice
            int supplierId = -1;
            String getSupplierQuery = "SELECT supplier_id FROM Raw_Purchase_Return_Invoice WHERE raw_purchase_return_invoice_id = ?";
            try (PreparedStatement supplierStmt = connection.prepareStatement(getSupplierQuery)) {
                supplierStmt.setInt(1, returnInvoiceId);
                try (ResultSet rs = supplierStmt.executeQuery()) {
                    if (rs.next()) {
                        supplierId = rs.getInt("supplier_id");
                    }
                }
            }
            
            if (supplierId != -1) {
                // Calculate total return amount
                double totalReturnAmount = 0.0;
                for (com.cablemanagement.model.RawStockPurchaseItem item : items) {
                    totalReturnAmount += item.getQuantity() * item.getUnitPrice();
                }
                
                String updateSupplierBalanceQuery = "UPDATE Supplier SET balance = balance - ? WHERE supplier_id = ?";
                try (PreparedStatement balanceStmt = connection.prepareStatement(updateSupplierBalanceQuery)) {
                    balanceStmt.setDouble(1, totalReturnAmount);
                    balanceStmt.setInt(2, supplierId);
                    balanceStmt.executeUpdate();
                    
                    System.out.println("DEBUG: Supplier balance reduced by return amount: " + totalReturnAmount);
                }
            }
            
            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all raw purchase return invoices
     */
    public List<Object[]> getAllRawPurchaseReturnInvoices() {
        List<Object[]> returnInvoices = new ArrayList<>();
        String query = "SELECT rpri.return_invoice_number, rpri.return_date, s.supplier_name, " +
                      "rpi.invoice_number as original_invoice, rpri.total_return_amount " +
                      "FROM Raw_Purchase_Return_Invoice rpri " +
                      "JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                      "JOIN Raw_Purchase_Invoice rpi ON rpri.original_invoice_id = rpi.raw_purchase_invoice_id " +
                      "ORDER BY rpri.return_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getString("supplier_name"),
                    rs.getString("original_invoice"),
                    rs.getDouble("total_return_amount")
                };
                returnInvoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnInvoices;
    }

    // --------------------------
    // Raw Stock Use Invoice Operations
    // --------------------------
    
    /**
     * Generate auto-increment use invoice number
     */
    @Override
    public String generateUseInvoiceNumber() {
        String query = "SELECT COUNT(*) FROM Raw_Stock_Use_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("INV-RSU-%03d", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "INV-RSU-001";
    }
    
    /**
     * Get all raw stocks with their units for dropdown selection
     */
    @Override
    public List<Object[]> getAllRawStocksWithUnitsForDropdown() {
        List<Object[]> rawStocks = new ArrayList<>();
        String query = "SELECT rs.stock_id, rs.item_name, b.brand_name, " +
                      "u.unit_name, rs.quantity, rs.unit_price " +
                      "FROM Raw_Stock rs " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "LEFT JOIN Unit u ON rs.unit_id = u.unit_id " +
                      "WHERE rs.quantity > 0 " +
                      "ORDER BY rs.item_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("stock_id"),
                    rs.getString("item_name"),
                    "N/A", // category_name (not available in Raw_Stock table)
                    rs.getString("brand_name"),
                    rs.getString("unit_name") != null ? rs.getString("unit_name") : "N/A",
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price")
                };
                rawStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rawStocks;
    }
    
    /**
     * Insert raw stock use invoice and return the generated ID
     */
    @Override
    public int insertRawStockUseInvoiceAndGetId(String useInvoiceNumber, String usageDate, 
                                               double totalUsageAmount, String referencePurpose) {
        String query = "INSERT INTO Raw_Stock_Use_Invoice (use_invoice_number, usage_date, " +
                      "total_usage_amount, reference_purpose) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, useInvoiceNumber);
            pstmt.setString(2, usageDate);
            pstmt.setDouble(3, totalUsageAmount);
            pstmt.setString(4, referencePurpose);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if insertion failed
    }
    
    /**
     * Insert raw stock use invoice items
     */
    @Override
    public boolean insertRawStockUseInvoiceItems(int useInvoiceId, List<RawStockUseItem> items) {
        String insertItemQuery = "INSERT INTO Raw_Stock_Use_Invoice_Item (raw_stock_use_invoice_id, " +
                      "raw_stock_id, quantity_used, unit_cost, total_cost) VALUES (?, ?, ?, ?, ?)";
        String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity - ? WHERE stock_id = ? AND quantity >= ?";
        String checkStockQuery = "SELECT quantity FROM Raw_Stock WHERE stock_id = ?";
        
        try (PreparedStatement insertPstmt = connection.prepareStatement(insertItemQuery);
             PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery);
             PreparedStatement checkPstmt = connection.prepareStatement(checkStockQuery)) {
            
            connection.setAutoCommit(false); // Start transaction
            
            System.out.println("DEBUG: Inserting " + items.size() + " raw stock use items and updating stock quantities");
            
            // First, validate that all items have sufficient stock
            for (RawStockUseItem item : items) {
                checkPstmt.setInt(1, item.getRawStockId());
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        double currentStock = rs.getDouble("quantity");
                        if (currentStock < item.getQuantityUsed()) {
                            connection.rollback();
                            connection.setAutoCommit(true);
                            System.err.println("ERROR: Insufficient stock for item " + item.getRawStockName() + 
                                             ". Available: " + currentStock + ", Requested: " + item.getQuantityUsed());
                            return false;
                        }
                    } else {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        System.err.println("ERROR: Raw stock item not found: " + item.getRawStockName());
                        return false;
                    }
                }
            }
            
            for (RawStockUseItem item : items) {
                // Insert the use item record
                insertPstmt.setInt(1, useInvoiceId);
                insertPstmt.setInt(2, item.getRawStockId());
                insertPstmt.setDouble(3, item.getQuantityUsed());
                insertPstmt.setDouble(4, item.getUnitCost());
                insertPstmt.setDouble(5, item.getTotalCost());
                insertPstmt.addBatch();
                
                // Update raw stock quantity by deducting the used quantity
                // The WHERE clause includes quantity >= ? as a safety check
                updatePstmt.setDouble(1, item.getQuantityUsed());
                updatePstmt.setInt(2, item.getRawStockId());
                updatePstmt.setDouble(3, item.getQuantityUsed()); // Safety check: only update if sufficient stock
                updatePstmt.addBatch();
                
                System.out.println("DEBUG: Prepared to deduct " + item.getQuantityUsed() + 
                                 " from raw stock ID " + item.getRawStockId() + " (" + item.getRawStockName() + ")");
            }
            
            // Execute both batches
            int[] insertResults = insertPstmt.executeBatch();
            int[] updateResults = updatePstmt.executeBatch();
            
            // Check if all operations were successful
            boolean allSuccessful = true;
            for (int result : insertResults) {
                if (result <= 0) {
                    allSuccessful = false;
                    System.err.println("ERROR: Failed to insert use item record");
                    break;
                }
            }
            for (int result : updateResults) {
                if (result <= 0) {
                    allSuccessful = false;
                    System.err.println("ERROR: Failed to update raw stock quantity (possibly insufficient stock)");
                    break;
                }
            }
            
            if (allSuccessful) {
                connection.commit(); // Commit transaction
                System.out.println("DEBUG: Successfully inserted use items and updated raw stock quantities");
            } else {
                connection.rollback(); // Rollback on error
                System.err.println("ERROR: Failed to insert use items or update stock quantities");
                return false;
            }
            
            connection.setAutoCommit(true); // Reset auto-commit
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on error
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.err.println("ERROR: SQLException in insertRawStockUseInvoiceItems: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all raw stock use invoices
     */
    @Override
    public List<Object[]> getAllRawStockUseInvoices() {
        List<Object[]> useInvoices = new ArrayList<>();
        String query = "SELECT rsui.use_invoice_number, rsui.usage_date, " +
                      "rsui.total_usage_amount, rsui.reference_purpose " +
                      "FROM Raw_Stock_Use_Invoice rsui " +
                      "ORDER BY rsui.usage_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("use_invoice_number"),
                    rs.getString("usage_date"),
                    rs.getDouble("total_usage_amount"),
                    rs.getString("reference_purpose")
                };
                useInvoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return useInvoices;
    }

    /**
     * Get raw stock usage report by date range
     */
    public List<Object[]> getRawStockUsageReportByDateRange(String startDate, String endDate) {
        List<Object[]> usageReport = new ArrayList<>();
        String query = "SELECT rs.item_name, b.brand_name, " +
                      "SUM(rsuii.quantity_used) as total_quantity_used, " +
                      "rsuii.unit_cost, " +
                      "SUM(rsuii.total_cost) as total_cost_used, " +
                      "COUNT(DISTINCT rsui.raw_stock_use_invoice_id) as usage_count " +
                      "FROM Raw_Stock_Use_Invoice_Item rsuii " +
                      "JOIN Raw_Stock_Use_Invoice rsui ON rsuii.raw_stock_use_invoice_id = rsui.raw_stock_use_invoice_id " +
                      "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "WHERE rsui.usage_date BETWEEN ? AND ? " +
                      "GROUP BY rs.stock_id, rs.item_name, b.brand_name, rsuii.unit_cost " +
                      "ORDER BY total_cost_used DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("item_name"),
                        rs.getString("brand_name"),
                        rs.getDouble("total_quantity_used"),
                        rs.getDouble("unit_cost"),
                        rs.getDouble("total_cost_used"),
                        rs.getInt("usage_count")
                    };
                    usageReport.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usageReport;
    }

    /**
     * Get usage summary statistics for a date range
     */
    public Object[] getUsageSummaryStatistics(String startDate, String endDate) {
        String query = "SELECT " +
                      "COUNT(DISTINCT rsui.raw_stock_use_invoice_id) as total_invoices, " +
                      "COUNT(DISTINCT rsuii.raw_stock_id) as unique_items_used, " +
                      "SUM(rsuii.quantity_used) as total_quantity_used, " +
                      "SUM(rsuii.total_cost) as total_cost " +
                      "FROM Raw_Stock_Use_Invoice rsui " +
                      "JOIN Raw_Stock_Use_Invoice_Item rsuii ON rsui.raw_stock_use_invoice_id = rsuii.raw_stock_use_invoice_id " +
                      "WHERE rsui.usage_date BETWEEN ? AND ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getInt("total_invoices"),
                        rs.getInt("unique_items_used"),
                        rs.getDouble("total_quantity_used"),
                        rs.getDouble("total_cost")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[] {0, 0, 0.0, 0.0};
    }

    /**
     * Get usage details for a specific date range (invoices with items)
     */
    public List<Object[]> getRawStockUsageDetails(String startDate, String endDate) {
        List<Object[]> usageDetails = new ArrayList<>();
        String query = "SELECT rsui.use_invoice_number, rsui.usage_date, rsui.reference_purpose, " +
                      "rs.item_name, b.brand_name, rsuii.quantity_used, rsuii.unit_cost, rsuii.total_cost " +
                      "FROM Raw_Stock_Use_Invoice rsui " +
                      "JOIN Raw_Stock_Use_Invoice_Item rsuii ON rsui.raw_stock_use_invoice_id = rsuii.raw_stock_use_invoice_id " +
                      "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                      "JOIN Brand b ON rs.brand_id = b.brand_id " +
                      "WHERE rsui.usage_date BETWEEN ? AND ? " +
                      "ORDER BY rsui.usage_date DESC, rsui.use_invoice_number";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("use_invoice_number"),
                        rs.getString("usage_date"),
                        rs.getString("reference_purpose"),
                        rs.getString("item_name"),
                        rs.getString("brand_name"),
                        rs.getDouble("quantity_used"),
                        rs.getDouble("unit_cost"),
                        rs.getDouble("total_cost")
                    };
                    usageDetails.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usageDetails;
    }

    // --------------------------
    // Production Stock Operations
    // --------------------------
    @Override
    public List<Object[]> getAllProductionStocks() {
        List<Object[]> productionStocks = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, " +
                    "c.category_name, m.manufacturer_name, " +
                    "b.brand_name, u.unit_name, " +
                    "ps.quantity, ps.unit_cost, ps.sale_price, ps.total_cost, ps.production_date " +
                    "FROM ProductionStock ps " +
                    "JOIN Category c ON ps.category_id = c.category_id " +
                    "JOIN Manufacturer m ON ps.manufacturer_id = m.manufacturer_id " +
                    "JOIN Brand b ON ps.brand_id = b.brand_id " +
                    "LEFT JOIN Unit u ON ps.unit_id = u.unit_id " +
                    "ORDER BY ps.product_name";

        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),        // 0
                    rs.getString("product_name"),      // 1
                    rs.getString("category_name"),     // 2
                    rs.getString("brand_name"),        // 3
                    rs.getString("manufacturer_name"), // 4
                    rs.getString("unit_name") != null ? rs.getString("unit_name") : "N/A", // 5 - Unit name
                    rs.getInt("quantity"),             // 6
                    rs.getDouble("unit_cost"),         // 7
                    rs.getDouble("sale_price"),        // 8
                    rs.getDouble("total_cost"),        // 9
                    rs.getString("production_date")    // 10
                };
                productionStocks.add(row);
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get all production stocks: " + e.getMessage());
            e.printStackTrace();
        }
        return productionStocks;
    }

    @Override
    public List<Object[]> getAllProductionStocksForDropdown() {
        List<Object[]> productionStocks = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, b.brand_name, " +
                      "u.unit_name, ps.unit_cost, ps.quantity " +
                      "FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "LEFT JOIN Unit u ON ps.unit_id = u.unit_id " +
                      "WHERE ps.quantity > 0 " +
                      "ORDER BY ps.product_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    "N/A", // category_name (not available in ProductionStock table)
                    rs.getString("brand_name"),
                    rs.getString("unit_name") != null ? rs.getString("unit_name") : "N/A",
                    rs.getDouble("unit_cost"),
                    rs.getDouble("quantity")
                };
                productionStocks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productionStocks;
    }

    @Override
    public String generateProductionInvoiceNumber() {
        String query = "SELECT COUNT(*) FROM Production_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                return String.format("PI-%04d", count + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PI-0001";
    }

    @Override
    public int insertProductionInvoiceAndGetId(String productionDate, String notes) {
        String query = "INSERT INTO Production_Invoice (production_date, notes) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, productionDate);
            pstmt.setString(2, notes);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean insertProductionInvoiceItems(int productionInvoiceId, List<Object[]> productionItems) {
        String insertQuery = "INSERT INTO Production_Invoice_Item (production_invoice_id, " +
                           "production_id, quantity_produced) VALUES (?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            // Insert production invoice items
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (Object[] item : productionItems) {
                    pstmt.setInt(1, productionInvoiceId);
                    pstmt.setInt(2, (Integer) item[0]); // production_id from ProductionStock table
                    pstmt.setDouble(3, (Double) item[1]); // quantity_produced
                    pstmt.addBatch();
                }
                
                int[] insertResults = pstmt.executeBatch();
                
                // Check if all items were inserted successfully
                for (int result : insertResults) {
                    if (result <= 0) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            // Update production stock quantities (increase stock as items are being produced)
            String updateStockQuery = "UPDATE ProductionStock SET quantity = quantity + ? " +
                                    "WHERE production_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (Object[] item : productionItems) {
                    updatePstmt.setDouble(1, (Double) item[1]); // quantity_produced (add to stock)
                    updatePstmt.setInt(2, (Integer) item[0]); // production_id
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get current production stock quantity for validation
     */
    public double getCurrentProductionStockQuantity(int productionId) {
        String query = "SELECT quantity FROM ProductionStock WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public boolean insertProductionStockRawUsage(int productionInvoiceId, List<Object[]> rawMaterialsUsed) {
        String query = "INSERT INTO Production_Stock_Raw_Usage (production_invoice_id, " +
                      "raw_stock_id, quantity_used) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            for (Object[] material : rawMaterialsUsed) {
                pstmt.setInt(1, productionInvoiceId);
                pstmt.setInt(2, (Integer) material[0]); // stock_id from Raw_Stock table
                pstmt.setDouble(3, (Double) material[1]); // quantity_used
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            
            // Update raw stock quantities
            String updateStockQuery = "UPDATE Raw_Stock SET quantity = quantity - ? " +
                                    "WHERE stock_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (Object[] material : rawMaterialsUsed) {
                    updatePstmt.setDouble(1, (Double) material[1]); // quantity_used
                    updatePstmt.setInt(2, (Integer) material[0]); // stock_id
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean insertProductionStock(String name, String category, String manufacturer, String brand, String unit,
                                        double openingQty, double unitCost, double salePrice) {
        // Get IDs from names
        int categoryId = getCategoryIdByName(category);
        int manufacturerId = getManufacturerIdByName(manufacturer);
        int unitId = getUnitIdByName(unit);

        String query = "INSERT INTO ProductionStock " +
                "(product_name, category_id, manufacturer_id, brand_id, unit_id, quantity, unit_cost, total_cost, sale_price) " +
                "VALUES (?, ?, ?, (SELECT brand_id FROM Brand WHERE brand_name = ? LIMIT 1), ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false); // Start transaction

            // Ensure brand exists (linked with manufacturer)
            ensureBrandExists(brand, manufacturerId);

            PreparedStatement pstmt = connection.prepareStatement(query);
            double totalCost = openingQty * unitCost;

            pstmt.setString(1, name);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, manufacturerId);
            pstmt.setString(4, brand);
            pstmt.setInt(5, unitId);
            pstmt.setDouble(6, openingQty);
            pstmt.setDouble(7, unitCost);
            pstmt.setDouble(8, totalCost);
            pstmt.setDouble(9, salePrice);

            int result = pstmt.executeUpdate();
            pstmt.close();

            connection.commit(); // Commit transaction
            connection.setAutoCommit(true);

            System.out.println("DEBUG: Inserted ProductionStock - " +
                    "Name: " + name +
                    ", Category: " + category + " (id=" + categoryId + ")" +
                    ", Manufacturer: " + manufacturer + " (id=" + manufacturerId + ")" +
                    ", Brand: " + brand +
                    ", Unit: " + unit + " (unit_id=" + unitId + ")" +
                    ", Qty: " + openingQty +
                    ", Unit Cost: " + unitCost +
                    ", Sale Price: " + salePrice);

            return result > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.err.println("ERROR: Failed to insert production stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Object[]> getAllProductionInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT pi.production_invoice_id, pi.production_date, pi.notes, " +
                      "ps.product_name, pii.quantity_produced " +
                      "FROM Production_Invoice pi " +
                      "JOIN Production_Invoice_Item pii ON pi.production_invoice_id = pii.production_invoice_id " +
                      "JOIN ProductionStock ps ON pii.production_id = ps.production_id " +
                      "ORDER BY pi.production_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_invoice_id"),
                    rs.getString("production_date"),
                    rs.getString("notes"),
                    rs.getString("product_name"),
                    rs.getDouble("quantity_produced")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }


    /**
     * Generate auto-increment production return invoice number
     */
    public String generateProductionReturnInvoiceNumber() {
        String query = "SELECT COUNT(*) FROM Production_Return_Invoice";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("PRI-%04d", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PRI-0001";
    }

    /**
     * Complete production return invoice transaction
     * This method handles the complete flow: insert return invoice, insert items, and update stock
     */
    public boolean processProductionReturnInvoice(int originalProductionInvoiceId, String returnDate,
                                                 String notes, List<Object[]> returnItems) {
        try {
            connection.setAutoCommit(false);
            
            // Calculate totals
            double totalQuantity = 0;
            double totalAmount = 0;
            for (Object[] item : returnItems) {
                totalQuantity += (Double) item[1]; // quantity_returned
                totalAmount += (Double) item[3]; // total_cost
            }
            
            // Generate return invoice number
            String returnInvoiceNumber = generateProductionReturnInvoiceNumber();
            
            // Insert return invoice
            int returnInvoiceId = insertProductionReturnInvoiceAndGetId(
                returnInvoiceNumber, originalProductionInvoiceId, returnDate, 
                totalQuantity, totalAmount, notes
            );
            
            if (returnInvoiceId == -1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Insert return items
            boolean itemsInserted = insertProductionReturnInvoiceItems(returnInvoiceId, returnItems);
            
            if (!itemsInserted) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all production invoices for dropdown selection
     */
    public List<Object[]> getAllProductionInvoicesForDropdown() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT pi.production_invoice_id, pi.production_date, pi.notes " +
                      "FROM Production_Invoice pi " +
                      "ORDER BY pi.production_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_invoice_id"),
                    rs.getString("production_date"),
                    rs.getString("notes")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    /**
     * Get production items from a specific production invoice
     */
    public List<Object[]> getProductionItemsByInvoiceId(int productionInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT pii.production_id, ps.product_name, b.brand_name, " +
                      "pii.quantity_produced, ps.unit_cost " +
                      "FROM Production_Invoice_Item pii " +
                      "JOIN ProductionStock ps ON pii.production_id = ps.production_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE pii.production_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productionInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    rs.getDouble("quantity_produced"),
                    rs.getDouble("unit_cost")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Insert production return invoice and return the generated ID
     */
    public int insertProductionReturnInvoiceAndGetId(String returnInvoiceNumber, int originalProductionInvoiceId,
                                                    String returnDate, double totalReturnQuantity, 
                                                    double totalReturnAmount, String notes) {
        String query = "INSERT INTO Production_Return_Invoice " +
                      "(return_invoice_number, original_production_invoice_id, return_date, " +
                      "total_return_quantity, total_return_amount, notes) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, returnInvoiceNumber);
            pstmt.setInt(2, originalProductionInvoiceId);
            pstmt.setString(3, returnDate);
            pstmt.setDouble(4, totalReturnQuantity);
            pstmt.setDouble(5, totalReturnAmount);
            pstmt.setString(6, notes);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Insert production return invoice items
     */
    public boolean insertProductionReturnInvoiceItems(int returnInvoiceId, 
                                                     List<Object[]> returnItems) {
        String query = "INSERT INTO Production_Return_Invoice_Item " +
                      "(production_return_invoice_id, production_id, quantity_returned, unit_cost, total_cost) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            
            for (Object[] item : returnItems) {
                pstmt.setInt(1, returnInvoiceId);
                pstmt.setInt(2, (Integer) item[0]); // production_id
                pstmt.setDouble(3, (Double) item[1]); // quantity_returned
                pstmt.setDouble(4, (Double) item[2]); // unit_cost
                pstmt.setDouble(5, (Double) item[3]); // total_cost
                pstmt.addBatch();
                
                // Update production stock quantity
                updateProductionStockAfterReturn((Integer) item[0], (Double) item[1]);
            }
            
            int[] results = pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    

    /**
     * Get available items for return from a specific production invoice
     */
    public List<Object[]> getAvailableItemsForReturn(int productionInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, b.brand_name, " +
                      "ps.quantity, ps.unit_cost " +
                      "FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE ps.quantity > 0 " +
                      "ORDER BY ps.product_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_cost")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Get all production return invoices
     */
    public List<Object[]> getAllProductionReturnInvoices() {
        List<Object[]> returnInvoices = new ArrayList<>();
        String query = "SELECT pri.production_return_invoice_id, pri.return_invoice_number, pri.return_date, " +
                      "pri.total_return_quantity, pri.total_return_amount, pri.notes, " +
                      "pi.production_date as original_production_date " +
                      "FROM Production_Return_Invoice pri " +
                      "JOIN Production_Invoice pi ON pri.original_production_invoice_id = pi.production_invoice_id " +
                      "ORDER BY pri.return_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_return_invoice_id"),
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getDouble("total_return_quantity"),
                    rs.getDouble("total_return_amount"),
                    rs.getString("notes"),
                    rs.getString("original_production_date")
                };
                returnInvoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnInvoices;
    }

    /**
     * Update production stock quantity after return
     */
    public boolean updateProductionStockAfterReturn(int productionId, double returnedQuantity) {
        String query = "UPDATE ProductionStock SET quantity = quantity - ? WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, returnedQuantity);
            pstmt.setInt(2, productionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to decrease production stock when items are sold
    public boolean decreaseProductionStock(int productionId, double soldQuantity) {
        String query = "UPDATE ProductionStock SET quantity = quantity - ? WHERE production_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, soldQuantity);
            pstmt.setInt(2, productionId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("DEBUG: Decreased stock for production_id " + productionId + " by " + soldQuantity + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to decrease production stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if production stock with same name and brand already exists
    public boolean productionStockExists(String productName, String brandName) {
        String query = "SELECT COUNT(*) FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE ps.product_name = ? AND b.brand_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, productName);
            pstmt.setString(2, brandName);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to check production stock existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Add quantity to existing production stock
    public boolean addToProductionStock(String productName, String brandName, int addedQuantity, double unitCost, double salePrice) {
        String query = "UPDATE ProductionStock " +
                      "SET quantity = quantity + ?, unit_cost = ?, sale_price = ? " +
                      "WHERE product_name = ? AND brand_id = (SELECT brand_id FROM Brand WHERE brand_name = ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, addedQuantity);
            pstmt.setDouble(2, unitCost);
            pstmt.setDouble(3, salePrice);
            pstmt.setString(4, productName);
            pstmt.setString(5, brandName);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("DEBUG: Added " + addedQuantity + " to existing stock for " + productName + " (" + brandName + "), rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to add to production stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get production return invoice details by ID
     */
    public Object[] getProductionReturnInvoiceById(int returnInvoiceId) {
        String query = "SELECT pri.production_return_invoice_id, pri.return_invoice_number, pri.return_date, " +
                      "pri.total_return_quantity, pri.total_return_amount, pri.notes, " +
                      "pi.production_date as original_production_date " +
                      "FROM Production_Return_Invoice pri " +
                      "JOIN Production_Invoice pi ON pri.original_production_invoice_id = pi.production_invoice_id " +
                      "WHERE pri.production_return_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, returnInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Object[] {
                    rs.getInt("production_return_invoice_id"),
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getDouble("total_return_quantity"),
                    rs.getDouble("total_return_amount"),
                    rs.getString("notes"),
                    rs.getString("original_production_date")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get production return invoice items by return invoice ID
     */
    public List<Object[]> getProductionReturnInvoiceItems(int returnInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT prii.production_return_invoice_item_id, prii.production_id, " +
                      "ps.product_name, b.brand_name, prii.quantity_returned, " +
                      "prii.unit_cost, prii.total_cost " +
                      "FROM Production_Return_Invoice_Item prii " +
                      "JOIN ProductionStock ps ON prii.production_id = ps.production_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE prii.production_return_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, returnInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_return_invoice_item_id"),
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getString("brand_name"),
                    rs.getDouble("quantity_returned"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_cost")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<Object[]> getAllSalesInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT si.sales_invoice_number, si.sales_date, c.customer_name, " +
                      "si.total_amount, si.discount_amount, si.paid_amount " +
                      "FROM Sales_Invoice si " +
                      "JOIN Customer c ON si.customer_id = c.customer_id " +
                      "ORDER BY si.sales_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("sales_invoice_number"),
                    rs.getString("sales_date"),
                    rs.getString("customer_name"),
                    rs.getDouble("total_amount"),
                    rs.getDouble("discount_amount"),
                    rs.getDouble("paid_amount")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    // --------------------------
    // Sales Invoice Operations
    // --------------------------
    @Override
    public String generateSalesInvoiceNumber() {
        String query = "SELECT MAX(CAST(SUBSTR(sales_invoice_number, 5) AS INTEGER)) as max_number " +
                      "FROM Sales_Invoice WHERE sales_invoice_number LIKE 'SI-%'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int maxNumber = rs.getInt("max_number");
                return String.format("SI-%03d", maxNumber + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SI-001";
    }

    @Override
    public List<Object[]> getAllCustomersForDropdown() {
        List<Object[]> customers = new ArrayList<>();
        String query = "SELECT customer_id, customer_name FROM Customer ORDER BY customer_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("customer_id"),
                    rs.getString("customer_name")
                };
                customers.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public List<Object[]> getAllProductionStocksWithPriceForDropdown() {
        List<Object[]> products = new ArrayList<>();
        String query = "SELECT ps.production_id, ps.product_name, " +
                      "CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE ps.unit_cost * 1.2 END as sale_price, " +
                      "ps.quantity, 'N/A' as category_name, b.brand_name, 'N/A' as unit_name " +
                      "FROM ProductionStock ps " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE ps.quantity > 0 " +
                      "ORDER BY ps.product_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_id"),
                    rs.getString("product_name"),
                    rs.getDouble("sale_price"),
                    rs.getDouble("quantity"),
                    rs.getString("category_name"),
                    rs.getString("brand_name"),
                    rs.getString("unit_name")
                };
                products.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public int getProductionStockIdByName(String productName) {
        String query = "SELECT production_id FROM ProductionStock WHERE product_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, productName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("production_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int insertSalesInvoiceAndGetId(String invoiceNumber, int customerId, String salesDate, 
                                         double totalAmount, double discountAmount, double paidAmount) {
        String query = "INSERT INTO Sales_Invoice (sales_invoice_number, customer_id, sales_date, " +
                      "total_amount, discount_amount, paid_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, invoiceNumber);
            pstmt.setInt(2, customerId);
            pstmt.setString(3, salesDate);
            pstmt.setDouble(4, totalAmount);
            pstmt.setDouble(5, discountAmount);
            pstmt.setDouble(6, paidAmount);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    generatedKeys.close();
                    pstmt.close();
                    return generatedId;
                }
                generatedKeys.close();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error inserting sales invoice: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean insertSalesInvoiceItems(int salesInvoiceId, List<Object[]> items) {
        String query = "INSERT INTO Sales_Invoice_Item (sales_invoice_id, production_stock_id, quantity, unit_price, discount_percentage, discount_amount, total_price) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (Object[] item : items) {
                int productionStockId = (Integer) item[0];
                double quantity = (Double) item[1];
                double unitPrice = (Double) item[2];
                double discountPercentage = item.length > 3 ? (Double) item[3] : 0.0;
                double discountAmount = item.length > 4 ? (Double) item[4] : 0.0;
                
                // Calculate total price with discounts
                double basePrice = quantity * unitPrice;
                double percentageDiscount = basePrice * (discountPercentage / 100.0);
                double totalPrice = basePrice - percentageDiscount - discountAmount;
                totalPrice = Math.max(0, totalPrice); // Ensure price is not negative
                
                pstmt.setInt(1, salesInvoiceId);
                pstmt.setInt(2, productionStockId);
                pstmt.setDouble(3, quantity);
                pstmt.setDouble(4, unitPrice);
                pstmt.setDouble(5, discountPercentage);
                pstmt.setDouble(6, discountAmount);
                pstmt.setDouble(7, totalPrice);
                pstmt.addBatch();
                
                // Decrease production stock for each sold item
                if (!decreaseProductionStock(productionStockId, quantity)) {
                    System.err.println("WARNING: Failed to decrease production stock for product ID: " + productionStockId);
                    pstmt.close();
                    return false;
                }
            }
            
            int[] results = pstmt.executeBatch();
            pstmt.close();
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            System.out.println("DEBUG: Successfully inserted sales invoice items and updated stock levels");
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting sales invoice items: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertSalesInvoice(String invoiceNumber, int customerId, String salesDate, 
                                     double totalAmount, double discountAmount, double paidAmount, 
                                     List<Object[]> items) {
        try {
            // Set a timeout for database operations to prevent indefinite locks
            connection.setAutoCommit(false);
            
            System.out.println("DEBUG: Starting sales invoice transaction...");
            
            int salesInvoiceId = insertSalesInvoiceAndGetId(invoiceNumber, customerId, salesDate, 
                                                           totalAmount, discountAmount, paidAmount);
            
            if (salesInvoiceId > 0) {
                System.out.println("DEBUG: Sales invoice created with ID: " + salesInvoiceId);
                
                if (insertSalesInvoiceItems(salesInvoiceId, items)) {
                    System.out.println("DEBUG: Sales invoice items inserted successfully");
                    
                    // Update customer balance: add net invoice amount (total - discount - paid)
                    double netInvoiceAmount = totalAmount - discountAmount - paidAmount;
                    String updateBalanceQuery = "UPDATE Customer SET balance = balance + ? WHERE customer_id = ?";
                    
                    try (PreparedStatement balanceStmt = connection.prepareStatement(updateBalanceQuery)) {
                        balanceStmt.setDouble(1, netInvoiceAmount);
                        balanceStmt.setInt(2, customerId);
                        int updated = balanceStmt.executeUpdate();
                        
                        if (updated > 0) {
                            System.out.println("DEBUG: Customer balance updated by: " + netInvoiceAmount);
                            connection.commit();
                            System.out.println("DEBUG: Transaction committed successfully");
                            return true;
                        } else {
                            System.out.println("DEBUG: Failed to update customer balance, rolling back");
                            connection.rollback();
                            return false;
                        }
                    }
                } else {
                    System.out.println("DEBUG: Failed to insert sales invoice items, rolling back");
                    connection.rollback();
                    return false;
                }
            } else {
                System.out.println("DEBUG: Failed to create sales invoice, rolling back");
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in insertSalesInvoice: " + e.getMessage());
            try {
                connection.rollback();
                System.out.println("DEBUG: Transaction rolled back due to exception");
            } catch (SQLException rollbackEx) {
                System.err.println("DEBUG: Failed to rollback transaction: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                System.out.println("DEBUG: AutoCommit restored to true");
            } catch (SQLException e) {
                System.err.println("DEBUG: Failed to restore AutoCommit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --------------------------
    // Sales Return Invoice Operations
    // --------------------------
    @Override
    public String generateSalesReturnInvoiceNumber() {
        String query = "SELECT MAX(CAST(SUBSTR(return_invoice_number, 5) AS INTEGER)) as max_number " +
                      "FROM Sales_Return_Invoice WHERE return_invoice_number LIKE 'SRI-%'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int maxNumber = rs.getInt("max_number");
                return String.format("SRI-%03d", maxNumber + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SRI-001";
    }

    @Override
    public List<Object[]> getAllSalesInvoicesForDropdown() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT si.sales_invoice_id, si.sales_invoice_number, c.customer_name, si.sales_date " +
                      "FROM Sales_Invoice si " +
                      "JOIN Customer c ON si.customer_id = c.customer_id " +
                      "ORDER BY si.sales_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("sales_invoice_id"),
                    rs.getString("sales_invoice_number"),
                    rs.getString("customer_name"),
                    rs.getString("sales_date")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    @Override
    public List<Object[]> getSalesInvoiceItemsByInvoiceId(int salesInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT sii.production_stock_id, ps.product_name, sii.quantity, sii.unit_price, " +
                      "COALESCE(sii.discount_percentage, 0.0) as discount_percentage, " +
                      "COALESCE(sii.discount_amount, 0.0) as discount_amount " +
                      "FROM Sales_Invoice_Item sii " +
                      "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                      "WHERE sii.sales_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salesInvoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("production_stock_id"),
                        rs.getString("product_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("discount_percentage"),
                        rs.getDouble("discount_amount")
                    };
                    items.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Object[] getSalesInvoiceById(int salesInvoiceId) {
        String query = "SELECT si.sales_invoice_id, si.sales_invoice_number, si.customer_id, c.customer_name, " +
                      "si.sales_date, si.total_amount, si.discount_amount, si.paid_amount " +
                      "FROM Sales_Invoice si " +
                      "JOIN Customer c ON si.customer_id = c.customer_id " +
                      "WHERE si.sales_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salesInvoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getInt("sales_invoice_id"),
                        rs.getString("sales_invoice_number"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("sales_date"),
                        rs.getDouble("total_amount"),
                        rs.getDouble("discount_amount"),
                        rs.getDouble("paid_amount")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insertSalesReturnInvoiceAndGetId(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                               int customerId, String returnDate, double totalReturnAmount) {
        String query = "INSERT INTO Sales_Return_Invoice (return_invoice_number, original_sales_invoice_id, " +
                      "customer_id, return_date, total_return_amount) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, returnInvoiceNumber);
            pstmt.setInt(2, originalSalesInvoiceId);
            pstmt.setInt(3, customerId);
            pstmt.setString(4, returnDate);
            pstmt.setDouble(5, totalReturnAmount);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean insertSalesReturnInvoiceItems(int salesReturnInvoiceId, List<Object[]> items) {
        String insertQuery = "INSERT INTO Sales_Return_Invoice_Item (sales_return_invoice_id, production_stock_id, quantity, unit_price, total_price) " +
                           "VALUES (?, ?, ?, ?, ?)";
        
        try {
            // Insert sales return invoice items
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                for (Object[] item : items) {
                    pstmt.setInt(1, salesReturnInvoiceId);
                    pstmt.setInt(2, (Integer) item[0]); // production_stock_id
                    pstmt.setDouble(3, (Double) item[1]); // quantity
                    pstmt.setDouble(4, (Double) item[2]); // unit_price
                    pstmt.setDouble(5, (Double) item[1] * (Double) item[2]); // total_price = quantity * unit_price
                    pstmt.addBatch();
                }
                
                int[] insertResults = pstmt.executeBatch();
                
                // Check if all items were inserted successfully
                for (int result : insertResults) {
                    if (result <= 0) {
                        return false;
                    }
                }
            }
            
            // Update production stock quantities (increase stock as items are being returned)
            String updateStockQuery = "UPDATE ProductionStock SET quantity = quantity + ? " +
                                    "WHERE production_id = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateStockQuery)) {
                for (Object[] item : items) {
                    updatePstmt.setDouble(1, (Double) item[1]); // quantity (add to stock)
                    updatePstmt.setInt(2, (Integer) item[0]); // production_stock_id
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();
            }
            
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertSalesReturnInvoice(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                           int customerId, String returnDate, double totalReturnAmount, 
                                           List<Object[]> items, boolean updateBalance) {
        try {
            connection.setAutoCommit(false);
            
            int salesReturnInvoiceId = insertSalesReturnInvoiceAndGetId(returnInvoiceNumber, originalSalesInvoiceId, 
                                                                       customerId, returnDate, totalReturnAmount);
            
            if (salesReturnInvoiceId > 0 && insertSalesReturnInvoiceItems(salesReturnInvoiceId, items)) {
                // Update customer balance only if refund method is "Refund to Balance"
                if (updateBalance) {
                    String updateBalanceQuery = "UPDATE Customer SET balance = balance - ? WHERE customer_id = ?";
                    
                    try (PreparedStatement balanceStmt = connection.prepareStatement(updateBalanceQuery)) {
                        balanceStmt.setDouble(1, totalReturnAmount);
                        balanceStmt.setInt(2, customerId);
                        balanceStmt.executeUpdate();
                        
                        System.out.println("DEBUG: Customer balance reduced by return amount: " + totalReturnAmount);
                    }
                } else {
                    System.out.println("DEBUG: Cash refund - customer balance not updated");
                }
                
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Overloaded method for backward compatibility - defaults to updating balance
    public boolean insertSalesReturnInvoice(String returnInvoiceNumber, int originalSalesInvoiceId, 
                                           int customerId, String returnDate, double totalReturnAmount, 
                                           List<Object[]> items) {
        return insertSalesReturnInvoice(returnInvoiceNumber, originalSalesInvoiceId, customerId, 
                                      returnDate, totalReturnAmount, items, true);
    }

    @Override
    public List<Object[]> getAllSalesReturnInvoices() {
        List<Object[]> invoices = new ArrayList<>();
        String query = "SELECT sri.return_invoice_number, sri.return_date, c.customer_name, " +
                      "sri.total_return_amount, si.sales_invoice_number " +
                      "FROM Sales_Return_Invoice sri " +
                      "JOIN Customer c ON sri.customer_id = c.customer_id " +
                      "JOIN Sales_Invoice si ON sri.original_sales_invoice_id = si.sales_invoice_id " +
                      "ORDER BY sri.return_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("return_invoice_number"),
                    rs.getString("return_date"),
                    rs.getString("customer_name"),
                    rs.getDouble("total_return_amount"),
                    rs.getString("sales_invoice_number")
                };
                invoices.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    // --------------------------
    // Bank Management Operations
    // --------------------------
    @Override
    public List<Object[]> getAllBanks() {
        List<Object[]> banks = new ArrayList<>();
        String query = "SELECT bank_id, bank_name, account_number, branch_name, balance FROM Bank ORDER BY bank_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("bank_id"),
                    rs.getString("bank_name"),
                    rs.getString("account_number"),
                    rs.getString("branch_name"),
                    rs.getDouble("balance")
                };
                banks.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banks;
    }

    @Override
    public boolean insertBank(String bankName, String accountNumber, String branchName) {
        // Check if this is the first bank entry to assign ID 0
        try {
            Statement countStmt = connection.createStatement();
            ResultSet rs = countStmt.executeQuery("SELECT COUNT(*) FROM Bank");
            boolean isFirstBank = false;
            if (rs.next() && rs.getInt(1) == 0) {
                isFirstBank = true;
            }
            rs.close();
            countStmt.close();
            
            String query = isFirstBank ? 
                "INSERT INTO Bank (bank_id, bank_name, account_number, branch_name) VALUES (0, ?, ?, ?)" : 
                "INSERT INTO Bank (bank_name, account_number, branch_name) VALUES (?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, bankName);
                pstmt.setString(2, accountNumber);
                pstmt.setString(3, branchName);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Object[]> getAllBankTransactions() {
        List<Object[]> transactions = new ArrayList<>();
        String query = "SELECT bt.transaction_date, b.bank_name, bt.transaction_type, " +
                      "bt.amount, bt.description " +
                      "FROM Bank_Transaction bt " +
                      "JOIN Bank b ON bt.bank_id = b.bank_id " +
                      "ORDER BY bt.transaction_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("transaction_date"),
                    rs.getString("bank_name"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getString("description")
                };
                transactions.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }


    @Override
    public List<Object[]> getAllCashTransactions() {
        List<Object[]> transactions = new ArrayList<>();

        // Use correct column names for each table
        String cashQuery = "SELECT transaction_date AS date, transaction_type, amount, description, 'cash' AS source " +
                        "FROM Cash_Transaction";
        String bankQuery = "SELECT transaction_date AS date, transaction_type, amount, description, 'bank' AS source " +
                        "FROM Bank_Transaction";

        try {
            // Cash transactions
            try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(cashQuery)) {
                while (rs.next()) {
                    Object[] row = new Object[] {
                        rs.getString("date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("source")
                    };
                    transactions.add(row);
                }
            }

            // Bank transactions
            try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(bankQuery)) {
                while (rs.next()) {
                    Object[] row = new Object[] {
                        rs.getString("date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("source")
                    };
                    transactions.add(row);
                }
            }

            // Sort by date descending
            transactions.sort((a, b) -> {
                String dateA = (String) a[0];
                String dateB = (String) b[0];
                return dateB.compareTo(dateA);
            });

        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }
        

    @Override
    public double getCurrentCashBalance() {
        String query = "SELECT " +
                      "IFNULL(SUM(CASE WHEN transaction_type IN ('cash_in', 'transfer_from_bank') THEN amount ELSE 0 END), 0) - " +
                      "IFNULL(SUM(CASE WHEN transaction_type IN ('cash_out', 'transfer_to_bank') THEN amount ELSE 0 END), 0) " +
                      "AS current_cash_balance " +
                      "FROM Cash_Transaction";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getDouble("current_cash_balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // --------------------------
    // Employee Management Operations
    // --------------------------
    @Override
    public List<Object[]> getAllEmployees() {
        List<Object[]> employees = new ArrayList<>();
        String query = "SELECT e.employee_id, e.employee_name, e.phone_number, e.cnic, e.address, d.designation_title, " +
                      "e.salary_type, e.salary_amount, " +
                      "CASE WHEN e.is_active = 1 THEN 'Active' ELSE 'Inactive' END as status " +
                      "FROM Employee e " +
                      "JOIN Designation d ON e.designation_id = d.designation_id " +
                      "ORDER BY e.employee_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("phone_number"),
                    rs.getString("cnic"),
                    rs.getString("address"),
                    rs.getString("designation_title"),
                    rs.getString("salary_type"),
                    rs.getDouble("salary_amount"),
                    rs.getString("status")
                };
                employees.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public boolean insertEmployee(String name, String phone, String cnic, String address, 
                                 String designation, String salaryType, double salaryAmount) {
        String query = "INSERT INTO Employee (employee_name, phone_number, cnic, address, hire_date, " +
                      "designation_id, salary_type, salary_amount) " +
                      "SELECT ?, ?, ?, ?, DATE('now'), d.designation_id, ?, ? " +
                      "FROM Designation d " +
                      "WHERE d.designation_title = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, cnic);
            pstmt.setString(4, address);
            pstmt.setString(5, salaryType);
            pstmt.setDouble(6, salaryAmount);
            pstmt.setString(7, designation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Object[]> getAllEmployeeAttendance() {
        List<Object[]> attendance = new ArrayList<>();
        String query = "SELECT e.employee_name, ea.attendance_date, ea.status, ea.working_hours " +
                      "FROM Employee_Attendance ea " +
                      "JOIN Employee e ON ea.employee_id = e.employee_id " +
                      "ORDER BY ea.attendance_date DESC, e.employee_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status"),
                    rs.getDouble("working_hours")
                };
                attendance.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    @Override
    public List<Object[]> getAllEmployeeSalaryPayments() {
        List<Object[]> payments = new ArrayList<>();
        String query = "SELECT e.employee_name, esp.payment_date, esp.salary_amount, esp.description " +
                      "FROM Employee_Salary_Payment esp " +
                      "JOIN Employee e ON esp.employee_id = e.employee_id " +
                      "ORDER BY esp.payment_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("payment_date"),
                    rs.getDouble("salary_amount"),
                    rs.getString("description")
                };
                payments.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public List<Object[]> getAllEmployeeLoans() {
        List<Object[]> loans = new ArrayList<>();
        String query = "SELECT e.employee_name, el.loan_amount, el.loan_date, el.due_date, el.description, " +
                      "el.status, el.remaining_amount, el.loan_id " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "ORDER BY el.loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("loan_amount"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDouble("remaining_amount"),
                    rs.getInt("loan_id")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public boolean updateEmployee(int employeeId, String name, String phone, String cnic, String address, 
                                 String designation, String salaryType, double salaryAmount) {
        String query = "UPDATE Employee SET employee_name = ?, phone_number = ?, cnic = ?, address = ?, " +
                      "designation_id = (SELECT designation_id FROM Designation WHERE designation_title = ?), " +
                      "salary_type = ?, salary_amount = ? " +
                      "WHERE employee_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, cnic);
            pstmt.setString(4, address);
            pstmt.setString(5, designation);
            pstmt.setString(6, salaryType);
            pstmt.setDouble(7, salaryAmount);
            pstmt.setInt(8, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteEmployee(int employeeId) {
        String query = "UPDATE Employee SET is_active = 0 WHERE employee_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --------------------------
    // Employee Attendance Operations
    // --------------------------
    @Override
    public boolean insertEmployeeAttendance(int employeeId, String attendanceDate, String status, double workingHours) {
        // Check if attendance already exists for this employee on this date
        String checkQuery = "SELECT COUNT(*) FROM Employee_Attendance WHERE employee_id = ? AND attendance_date = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, employeeId);
            checkStmt.setString(2, attendanceDate);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Attendance already exists, update it instead
                String updateQuery = "UPDATE Employee_Attendance SET status = ?, working_hours = ? WHERE employee_id = ? AND attendance_date = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, status.toLowerCase());
                    updateStmt.setDouble(2, workingHours);
                    updateStmt.setInt(3, employeeId);
                    updateStmt.setString(4, attendanceDate);
                    return updateStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Insert new attendance record
        String insertQuery = "INSERT INTO Employee_Attendance (employee_id, attendance_date, status, working_hours) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, employeeId);
            pstmt.setString(2, attendanceDate);
            pstmt.setString(3, status.toLowerCase());
            pstmt.setDouble(4, workingHours);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Object[]> getEmployeeAttendanceByDateRange(String startDate, String endDate) {
        List<Object[]> attendance = new ArrayList<>();
        String query = "SELECT e.employee_id, e.employee_name, ea.attendance_date, ea.status, ea.working_hours " +
                      "FROM Employee_Attendance ea " +
                      "JOIN Employee e ON ea.employee_id = e.employee_id " +
                      "WHERE ea.attendance_date >= ? AND ea.attendance_date <= ? " +
                      "ORDER BY ea.attendance_date DESC, e.employee_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status"),
                    rs.getDouble("working_hours")
                };
                attendance.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    @Override
    public List<Object[]> getEmployeeAttendanceByEmployee(int employeeId) {
        List<Object[]> attendance = new ArrayList<>();
        String query = "SELECT e.employee_name, ea.attendance_date, ea.status, ea.working_hours " +
                      "FROM Employee_Attendance ea " +
                      "JOIN Employee e ON ea.employee_id = e.employee_id " +
                      "WHERE ea.employee_id = ? " +
                      "ORDER BY ea.attendance_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status"),
                    rs.getDouble("working_hours")
                };
                attendance.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    @Override
    public int getEmployeeIdByName(String employeeName) {
        String query = "SELECT employee_id FROM Employee WHERE employee_name = ? AND is_active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, employeeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("employee_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if employee not found
    }

    // --------------------------
    // Salesman Operations
    // --------------------------
    @Override
    public List<Object[]> getAllSalesmen() {
        List<Object[]> salesmen = new ArrayList<>();
        String query = "SELECT salesman_id, salesman_name, contact_number, address, commission_rate FROM Salesman ORDER BY salesman_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("salesman_id"),
                    rs.getString("salesman_name"),
                    rs.getString("contact_number"),
                    rs.getString("address"),
                    rs.getDouble("commission_rate")
                };
                salesmen.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesmen;
    }

    @Override
    public boolean insertSalesman(String name, String contact, String address, double commissionRate) {
        String query = "INSERT INTO Salesman (salesman_name, contact_number, address, commission_rate) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setDouble(4, commissionRate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateSalesman(int salesmanId, String name, String contact, String address, double commissionRate) {
        String query = "UPDATE Salesman SET salesman_name = ?, contact_number = ?, address = ?, commission_rate = ? WHERE salesman_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setDouble(4, commissionRate);
            pstmt.setInt(5, salesmanId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    // Delete Methods
    @Override
    public String getSupplierAddress(String supplierName) {
        String query = "SELECT s.contact_number || ' - ' || t.tehsil_name as address " +
                      "FROM Supplier s " +
                      "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                      "WHERE s.supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("address");
            }
            return supplierName; // Return supplier name if no address found
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Object[] getSupplierDetails(String supplierName) {
        String query = "SELECT s.supplier_id, s.supplier_name, s.address, t.tehsil_name, s.contact_number " +
                      "FROM Supplier s " +
                      "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                      "WHERE s.supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, supplierName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Get the values before returning
                    int id = rs.getInt("supplier_id");
                    String name = rs.getString("supplier_name");
                    String address = rs.getString("address");
                    String tehsil = rs.getString("tehsil_name");
                    String contact = rs.getString("contact_number");
                    
                    // Debug information
                    System.out.println("Supplier Details: ID=" + id + ", Name=" + name + 
                                     ", Address=" + address + ", Tehsil=" + tehsil + 
                                     ", Contact=" + contact);
                    
                    return new Object[] {
                        id,
                        name,
                        address,
                        tehsil,
                        contact
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[] {-1, supplierName, "", "", ""};
    }

    @Override
    public boolean deleteCategory(String categoryName) {
        String query = "DELETE FROM Category WHERE category_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteManufacturer(String name) {
        String query = "DELETE FROM Manufacturer WHERE manufacturer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBrand(String name) {
        String query = "DELETE FROM Brand WHERE brand_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteProvince(String provinceName) {
        String query = "DELETE FROM Province WHERE province_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, provinceName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteDistrict(String districtName) {
        String query = "DELETE FROM District WHERE district_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, districtName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteTehsil(String tehsilName) {
        String query = "DELETE FROM Tehsil WHERE tehsil_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tehsilName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteUnit(String unitName) {
        String query = "DELETE FROM Unit WHERE unit_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCustomer(String name) {
        String query = "DELETE FROM Customer WHERE customer_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteSupplier(String name) {
        String query = "DELETE FROM Supplier WHERE supplier_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --------------------------
    // Designation Operations Implementation
    // --------------------------
    @Override
    public List<Object[]> getAllDesignations() {
        List<Object[]> designations = new ArrayList<>();
        String query = "SELECT designation_id, designation_title FROM Designation ORDER BY designation_title";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("designation_id"),
                    rs.getString("designation_title")
                };
                designations.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return designations;
    }

    @Override
    public boolean insertDesignation(String designationTitle) {
        String query = "INSERT INTO Designation (designation_title) VALUES (?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, designationTitle);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateDesignation(int designationId, String designationTitle) {
        String query = "UPDATE Designation SET designation_title = ? WHERE designation_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, designationTitle);
            pstmt.setInt(2, designationId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteDesignation(int designationId) {
        // First check if the designation is being used by any employee
        String checkQuery = "SELECT COUNT(*) FROM Employee WHERE designation_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, designationId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                // Designation is being used, cannot delete
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // If not being used, proceed with deletion
        String deleteQuery = "DELETE FROM Designation WHERE designation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, designationId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getLastAttendanceStatus(int empId) {
        String query = "SELECT status FROM Employee_Attendance WHERE employee_id = ? ORDER BY attendance_date DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, empId);
            try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("status");
            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --------------------------
    // Employee Advance Salary Operations
    // --------------------------
    public boolean insertAdvanceSalary(int employeeId, double amount, String advanceDate, String description) {
        String query = "INSERT INTO Employee_Advance_Salary (employee_id, amount, advance_date, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, advanceDate);
            pstmt.setString(4, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Object[]> getAllAdvanceSalaries() {
        List<Object[]> advances = new ArrayList<>();
        String query = "SELECT e.employee_name, eas.amount, eas.advance_date, eas.description, eas.status " +
                      "FROM Employee_Advance_Salary eas " +
                      "JOIN Employee e ON eas.employee_id = e.employee_id " +
                      "ORDER BY eas.advance_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("amount"),
                    rs.getString("advance_date"),
                    rs.getString("description"),
                    rs.getString("status")
                };
                advances.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return advances;
    }

    public List<Object[]> getAdvanceSalariesByDateRange(String startDate, String endDate) {
        List<Object[]> advances = new ArrayList<>();
        String query = "SELECT e.employee_name, eas.amount, eas.advance_date, eas.description, eas.status " +
                      "FROM Employee_Advance_Salary eas " +
                      "JOIN Employee e ON eas.employee_id = e.employee_id " +
                      "WHERE eas.advance_date >= ? AND eas.advance_date <= ? " +
                      "ORDER BY eas.advance_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("amount"),
                    rs.getString("advance_date"),
                    rs.getString("description"),
                    rs.getString("status")
                };
                advances.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return advances;
    }

    // --------------------------
    // Salary Report Operations
    // --------------------------
    public List<Object[]> getSalaryReportByDateRange(String startDate, String endDate) {
        List<Object[]> salaryData = new ArrayList<>();
        String query = "SELECT e.employee_id, e.employee_name, d.designation_title, e.salary_type, e.salary_amount, " +
                      "COALESCE(SUM(CASE WHEN ea.status = 'present' THEN ea.working_hours ELSE 0 END), 0) as total_hours, " +
                      "COALESCE(COUNT(CASE WHEN ea.status = 'present' THEN 1 END), 0) as present_days, " +
                      "COALESCE(COUNT(CASE WHEN ea.status = 'absent' THEN 1 END), 0) as absent_days " +
                      "FROM Employee e " +
                      "LEFT JOIN Designation d ON e.designation_id = d.designation_id " +
                      "LEFT JOIN Employee_Attendance ea ON e.employee_id = ea.employee_id " +
                      "AND ea.attendance_date >= ? AND ea.attendance_date <= ? " +
                      "WHERE e.is_active = 1 " +
                      "GROUP BY e.employee_id, e.employee_name, d.designation_title, e.salary_type, e.salary_amount " +
                      "ORDER BY e.employee_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("designation_title"),
                    rs.getString("salary_type"),
                    rs.getDouble("salary_amount"),
                    rs.getDouble("total_hours"),
                    rs.getInt("present_days"),
                    rs.getInt("absent_days")
                };
                salaryData.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salaryData;
    }
    
    // --------------------------
    // Employee Loan Operations
    // --------------------------
    public boolean insertEmployeeLoan(int employeeId, double loanAmount, String loanDate, String dueDate, String description) {
        String query = "INSERT INTO Employee_Loan (employee_id, loan_amount, loan_date, due_date, description, remaining_amount) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setDouble(2, loanAmount);
            pstmt.setString(3, loanDate);
            pstmt.setString(4, dueDate);
            pstmt.setString(5, description);
            pstmt.setDouble(6, loanAmount); // Initially, remaining amount equals loan amount
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Object[]> getEmployeeLoansByDateRange(String startDate, String endDate) {
        List<Object[]> loans = new ArrayList<>();
        String query = "SELECT e.employee_name, el.loan_amount, el.loan_date, el.due_date, el.description, " +
                      "el.status, el.remaining_amount, el.loan_id " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "WHERE el.loan_date >= ? AND el.loan_date <= ? " +
                      "ORDER BY el.loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("loan_amount"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDouble("remaining_amount"),
                    rs.getInt("loan_id")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public List<Object[]> getLoansByEmployee(String employeeName) {
        List<Object[]> loans = new ArrayList<>();
        String query = "SELECT e.employee_name, el.loan_amount, el.loan_date, el.due_date, el.description, " +
                      "el.status, el.remaining_amount, el.loan_id " +
                      "FROM Employee_Loan el " +
                      "JOIN Employee e ON el.employee_id = e.employee_id " +
                      "WHERE e.employee_name LIKE ? " +
                      "ORDER BY el.loan_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + employeeName + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("employee_name"),
                    rs.getDouble("loan_amount"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDouble("remaining_amount"),
                    rs.getInt("loan_id")
                };
                loans.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public boolean updateLoanStatus(int loanId, String status, double remainingAmount) {
        String query = "UPDATE Employee_Loan SET status = ?, remaining_amount = ? WHERE loan_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setDouble(2, remainingAmount);
            pstmt.setInt(3, loanId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Object[]> getViewData(String viewName, Map<String, String> filters) {
        List<Object[]> results = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM " + viewName);
        List<String> values = new ArrayList<>();

        // Map view names to their respective date columns
        Map<String, String> dateColumnMap = new HashMap<>();
        dateColumnMap.put("View_Purchase_Book", "invoice_date");
        dateColumnMap.put("View_Return_Purchase_Book", "invoice_date");
        dateColumnMap.put("View_Raw_Stock_Book", "invoice_date");
        dateColumnMap.put("View_Production_Book", "production_date");
        dateColumnMap.put("View_Return_Production_Book", "return_date");
        // Add other views and their date columns as needed

        String dateColumn = dateColumnMap.getOrDefault(viewName, "date"); // Default to 'date' if view not mapped

        System.out.println("View Name: " + viewName);
        if (filters != null && !filters.isEmpty()) {
            query.append(" WHERE ");
            List<String> clauses = new ArrayList<>();

            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("fromDate")) {
                    clauses.add(dateColumn + " >= ?");
                    values.add(value);
                } else if (key.equals("toDate")) {
                    clauses.add(dateColumn + " <= ?");
                    values.add(value);
                } else {
                    clauses.add(key + " LIKE ?");
                    values.add("%" + value + "%");
                }
            }
            query.append(String.join(" AND ", clauses));
        }

        System.out.println("Query: " + query.toString() + ", Values: " + values);
        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setString(i + 1, values.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                System.out.println("DEBUG: ResultSet has " + columnCount + " columns");
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    System.out.println("DEBUG: Row created with " + row.length + " elements");
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Rows returned: " + results.size());
        System.out.println("View_" + viewName + " results: " + results.size() + " rows");
        System.out.println("Table items count: " + results.size());
        return results;
    }
    @Override
    public List<Object[]> getAllProductionStock() {
        // Dummy implementation: returns an empty list
        return new ArrayList<>();
    }

    @Override
    public List<Object[]> getAllRawStock() {
        // Dummy implementation: returns an empty list
        return new ArrayList<>();
    }

    @Override
    public List<Object[]> getInvoiceItemsByID(Integer invoiceID) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT rpii.raw_stock_id, rs.item_name, c.category_name, b.brand_name, " +
                    "COALESCE(u.unit_name, 'Piece') as unit_name, rpii.quantity, rpii.unit_price, " +
                    "m.manufacturer_name " +
                    "FROM Raw_Purchase_Invoice_Item rpii " +
                    "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                    "JOIN Category c ON rs.category_id = c.category_id " +
                    "JOIN Manufacturer m ON rs.manufacturer_id = m.manufacturer_id " +
                    "JOIN Brand b ON rs.brand_id = b.brand_id " +
                    "LEFT JOIN Unit u ON rs.unit_id = u.unit_id " +
                    "JOIN Raw_Purchase_Invoice rpi ON rpii.raw_purchase_invoice_id = rpi.raw_purchase_invoice_id " +
                    "WHERE rpi.raw_purchase_invoice_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, invoiceID);
            System.out.println("DEBUG: Executing getInvoiceItemsByID query with invoiceID: " + invoiceID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("raw_stock_id"),        // index 0
                        rs.getString("item_name"),        // index 1 - rawStockName
                        rs.getString("brand_name"),       // index 2 - brandName  
                        rs.getDouble("quantity"),         // index 3 - quantity
                        rs.getDouble("unit_price"),       // index 4 - unitPrice
                        rs.getString("unit_name"),        // index 5 - unitName
                        rs.getString("manufacturer_name"), // index 6 - manufacturerName
                        rs.getString("category_name")     // index 7 - categoryName
                    };
                    System.out.println("DEBUG: Fetched invoice item: rawStockId=" + row[0] + ", name=" + row[1] + ", brand=" + row[2]);
                    items.add(row);
                }
                System.out.println("DEBUG: Total items fetched for invoice " + invoiceID + ": " + items.size());
            }
        } catch (SQLException e) {
            System.err.println("SQL Error for invoiceID " + invoiceID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }  


    /////////////////////////////////////////////////////////////////////////////
    ///                       reports                                        ////
    //////////////////////////////////////////////////////////////////////////////

    public List<Object[]> getPurchaseReportList(Date fromDate, Date toDate) {
        List<Object[]> reports = new ArrayList<>();
        String query = "SELECT " +
                "rpi.invoice_number AS invoiceNumber, " +
                "rpi.invoice_date AS invoiceDate, " +
                "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
                "rpi.total_amount AS totalAmount, " +
                "rpi.discount_amount AS discountAmount, " +
                "rpi.paid_amount AS paidAmount " +
                "FROM Raw_Purchase_Invoice rpi " +
                "LEFT JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                "WHERE rpi.invoice_date BETWEEN ? AND ? " +
                "ORDER BY rpi.invoice_date DESC";
                
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // Debug logging
            System.out.println("DEBUG: getPurchaseReportList called with dates:");
            System.out.println("DEBUG: fromDate: " + fromDate + " -> " + fromDateStr);
            System.out.println("DEBUG: toDate: " + toDate + " -> " + toDateStr);
            System.out.println("DEBUG: Query: " + query);
            
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("invoiceNumber"),
                        rs.getString("invoiceDate"),
                        rs.getString("supplierName"),
                        rs.getDouble("totalAmount"),
                        rs.getDouble("discountAmount"),
                        rs.getDouble("paidAmount")
                    };
                    reports.add(row);
                }
                System.out.println("DEBUG: Loaded " + reports.size() + " purchase report rows");
            }
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getPurchaseReportList: " + e.getMessage());
            e.printStackTrace();
        }
        return reports;
    }

public ResultSet getPurchaseReport(Date fromDate, Date toDate, String reportType) {
    String baseQuery =
        "SELECT " +
        "rpi.invoice_number AS invoiceNumber, " +
        "rpi.invoice_date AS invoiceDate, " +
        "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
        "rpi.total_amount AS totalAmount, " +
        "rpi.discount_amount AS discountAmount, " +
        "rpi.paid_amount AS paidAmount " +
        "FROM Raw_Purchase_Invoice rpi " +
        "LEFT JOIN Supplier s ON rpi.supplier_id = s.supplier_id ";

    String whereClause = "WHERE rpi.invoice_date BETWEEN ? AND ? ";
    String orderBy = "ORDER BY rpi.invoice_date DESC";

    try {
        String finalQuery;

        switch (reportType.trim()) {
            case "Product-wise Report":
                finalQuery =
                    "SELECT " +
                    "rpi.invoice_number AS invoiceNumber, " +
                    "rpi.invoice_date AS invoiceDate, " +
                    "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
                    "rs.item_name AS productName, " +
                    "rpii.quantity AS quantity, " +
                    "rpii.unit_price AS unitPrice, " +
                    "(rpii.quantity * rpii.unit_price) AS totalCost " +
                    "FROM Raw_Purchase_Invoice rpi " +
                    "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                    "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                    "LEFT JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                    whereClause + orderBy;
                break;

            case "Category-wise Report":
                finalQuery =
                    "SELECT " +
                    "c.category_name AS categoryName, " +
                    "SUM(rpii.quantity * rpii.unit_price) AS totalCost " +
                    "FROM Raw_Purchase_Invoice rpi " +
                    "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                    "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                    "JOIN Category c ON rs.category_id = c.category_id " +
                    "WHERE rpi.invoice_date BETWEEN ? AND ? " +
                    "GROUP BY c.category_name " +
                    "ORDER BY totalCost DESC";
                break;

            case "Brand-wise Report":
                finalQuery =
                    "SELECT " +
                    "b.brand_name AS brandName, " +
                    "SUM(rpii.quantity * rpii.unit_price) AS totalCost " +
                    "FROM Raw_Purchase_Invoice rpi " +
                    "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                    "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                    "JOIN Brand b ON rs.brand_id = b.brand_id " +
                    "WHERE rpi.invoice_date BETWEEN ? AND ? " +
                    "GROUP BY b.brand_name " +
                    "ORDER BY totalCost DESC";
                break;

            case "Manufacturer-wise Report":
                finalQuery =
                    "SELECT " +
                    "m.manufacturer_name AS manufacturerName, " +
                    "SUM(rpii.quantity * rpii.unit_price) AS totalCost " +
                    "FROM Raw_Purchase_Invoice rpi " +
                    "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                    "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                    "JOIN Manufacturer m ON rs.manufacturer_id = m.manufacturer_id " +
                    "WHERE rpi.invoice_date BETWEEN ? AND ? " +
                    "GROUP BY m.manufacturer_name " +
                    "ORDER BY totalCost DESC";
                break;

            default: // "All Reports"
                finalQuery = baseQuery + whereClause + orderBy;
                break;
        }

        System.out.println("DEBUG: Final Query: " + finalQuery);

        PreparedStatement pstmt = connection.prepareStatement(finalQuery);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        pstmt.setString(1, sdf.format(fromDate));
        pstmt.setString(2, sdf.format(toDate));

        return pstmt.executeQuery();

    } catch (SQLException e) {
        System.err.println("DEBUG: SQLException in getPurchaseReport: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}


    // Checked by Umer Ghafoor
    @Override
    public ResultSet getSalesReport(Date fromDate, Date toDate, String reportType) {
        String query = "";
        
        // Base date filter
        String dateFilter = " WHERE si.sales_date BETWEEN ? AND ? ";

        switch (reportType) {
            case "Product-wise Report":
                query = "SELECT " +
                        "ps.product_name AS Product, " +
                        "cat.category_name AS Category, " +
                        "b.brand_name AS Brand, " +
                        "SUM(sii.quantity) AS Quantity, " +
                        "SUM(sii.total_price) AS TotalAmount " +
                        "FROM Sales_Invoice si " +
                        "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                        "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                        "LEFT JOIN Category cat ON ps.category_id = cat.category_id " +
                        "LEFT JOIN Brand b ON ps.brand_id = b.brand_id " +
                        dateFilter +
                        "GROUP BY ps.product_name, cat.category_name, b.brand_name " +
                        "ORDER BY TotalAmount DESC";
                System.out.println("DEBUG: Generated Product-wise Report Query: " + query);
                break;

            case "Category-wise Report":
                query = "SELECT " +
                        "cat.category_name AS Category, " +
                        "SUM(sii.quantity) AS Quantity, " +
                        "SUM(sii.total_price) AS TotalAmount " +
                        "FROM Sales_Invoice si " +
                        "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                        "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                        "JOIN Category cat ON ps.category_id = cat.category_id " +
                        dateFilter +
                        "GROUP BY cat.category_name " +
                        "ORDER BY TotalAmount DESC";
                System.out.println("DEBUG: Generated Category-wise Report Query: " + query);
                break;

            case "Brand-wise Report":
                query = "SELECT " +
                        "b.brand_name AS Brand, " +
                        "SUM(sii.quantity) AS Quantity, " +
                        "SUM(sii.total_price) AS TotalAmount " +
                        "FROM Sales_Invoice si " +
                        "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                        "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                        "JOIN Brand b ON ps.brand_id = b.brand_id " +
                        dateFilter +
                        "GROUP BY b.brand_name " +
                        "ORDER BY TotalAmount DESC";
                System.out.println("DEBUG: Generated Brand-wise Report Query: " + query);
                break;

            case "Manufacturer-wise Report":
                query = "SELECT " +
                        "m.manufacturer_name AS Manufacturer, " +
                        "SUM(sii.quantity) AS Quantity, " +
                        "SUM(sii.total_price) AS TotalAmount " +
                        "FROM Sales_Invoice si " +
                        "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                        "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                        "JOIN Manufacturer m ON ps.manufacturer_id = m.manufacturer_id " +
                        dateFilter +
                        "GROUP BY m.manufacturer_name " +
                        "ORDER BY TotalAmount DESC";
                System.out.println("DEBUG: Generated Manufacturer-wise Report Query: " + query);
                break;

            default: // "All Reports"
                query = "SELECT " +
                        "si.sales_invoice_number AS Invoice, " +
                        "si.sales_date AS Date, " +
                        "COALESCE(c.customer_name, 'Unknown') AS Customer, " +
                        "si.total_amount AS Amount, " +
                        "si.discount_amount AS Discount, " +
                        "si.paid_amount AS Paid " +
                        "FROM Sales_Invoice si " +
                        "LEFT JOIN Customer c ON si.customer_id = c.customer_id " +
                        dateFilter +
                        "ORDER BY si.sales_date DESC";
                System.out.println("DEBUG: Generated All Reports Query: " + query);
                break;
        }

        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);

            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);

            System.out.println("DEBUG: getSalesReport [" + reportType + "]");
            System.out.println("Query: " + query);

            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error in getSalesReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getReturnPurchaseReport(Date fromDate, Date toDate, String reportType) {
        String query = "";
        
        // Base date filter
        String dateFilter = " WHERE rpri.return_date BETWEEN ? AND ? ";
        
        switch (reportType) {
        case "Product-wise Report":
            query = "SELECT " +
                    "rs.item_name AS Product, " +
                    "b.brand_name AS Brand, " +
                    "SUM(rprii.quantity) AS Quantity, " +
                    "SUM(rprii.quantity * rprii.unit_price) AS TotalAmount " +
                    "FROM Raw_Purchase_Return_Invoice rpri " +
                    "JOIN Raw_Purchase_Return_Invoice_Item rprii " +
                    "    ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                    "JOIN Raw_Stock rs " +
                    "    ON rprii.raw_stock_id = rs.stock_id " +
                    "LEFT JOIN Brand b " +
                    "    ON rs.brand_id = b.brand_id " +
                    dateFilter +
                    "GROUP BY rs.item_name, b.brand_name " +
                    "ORDER BY TotalAmount DESC";
            break;

        case "Brand-wise Report":
            query = "SELECT " +
                    "b.brand_name AS Brand, " +
                    "SUM(rprii.quantity) AS Quantity, " +
                    "SUM(rprii.quantity * rprii.unit_price) AS TotalAmount " +
                    "FROM Raw_Purchase_Return_Invoice rpri " +
                    "JOIN Raw_Purchase_Return_Invoice_Item rprii " +
                    "    ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                    "JOIN Raw_Stock rs " +
                    "    ON rprii.raw_stock_id = rs.stock_id " +
                    "LEFT JOIN Brand b " +
                    "    ON rs.brand_id = b.brand_id " +
                    dateFilter +
                    "GROUP BY b.brand_name " +
                    "ORDER BY TotalAmount DESC";
            break;
        case "Category-wise Report":
            query = "SELECT " +
                "cat.category_name AS Category, " +
                "SUM(rprii.quantity) AS Quantity, " +
                "SUM(rprii.quantity * rprii.unit_price) AS TotalAmount " +
                "FROM Raw_Purchase_Return_Invoice rpri " +
                "JOIN Raw_Purchase_Return_Invoice_Item rprii ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                "JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                "JOIN Category cat ON rs.category_id = cat.category_id " +
                dateFilter +
                "GROUP BY cat.category_name " +
                "ORDER BY TotalAmount DESC";
            break;

            case "Manufacturer-wise Report":
            query = "SELECT " +
                "m.manufacturer_name AS Manufacturer, " +
                "SUM(rprii.quantity) AS Quantity, " +
                "SUM(rprii.quantity * rprii.unit_price) AS TotalAmount " +
                "FROM Raw_Purchase_Return_Invoice rpri " +
                "JOIN Raw_Purchase_Return_Invoice_Item rprii ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                "JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                "JOIN Manufacturer m ON rs.manufacturer_id = m.manufacturer_id " +
                dateFilter +
                "GROUP BY m.manufacturer_name " +
                "ORDER BY TotalAmount DESC";
            break;

            default: // "All Reports"
            query = "SELECT " +
                "rpri.return_invoice_number AS invoiceNumber, " +
                "rpri.return_date AS invoiceDate, " +
                "COALESCE(s.supplier_name, 'Unknown Supplier') AS supplierName, " +
                "rpri.total_return_amount AS totalAmount, " +
                "0.00 AS discountAmount, " +
                "rpri.total_return_amount AS paidAmount " +
                "FROM Raw_Purchase_Return_Invoice rpri " +
                "LEFT JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                dateFilter +
                "ORDER BY rpri.return_date DESC";
            break;
        }
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // Debug logging
            System.out.println("DEBUG: getReturnPurchaseReport called with dates:");
            System.out.println("DEBUG: fromDate: " + fromDate + " -> " + fromDateStr);
            System.out.println("DEBUG: toDate: " + toDate + " -> " + toDateStr);
            System.out.println("DEBUG: reportType: " + reportType);
            System.out.println("DEBUG: Query: " + query);
            
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Debug: Just log that we're returning the ResultSet
            System.out.println("DEBUG: Returning ResultSet from getReturnPurchaseReport");
            
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getReturnPurchaseReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getReturnSalesReport(Date fromDate, Date toDate, String reportType) {
        System.out.println("DEBUG: getReturnSalesReport called with dates: " + fromDate + " to " + toDate + " and reportType: " + reportType);
        
        // Convert Date to string format for comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sdf.format(fromDate);
        String toDateStr = sdf.format(toDate);
        
        System.out.println("DEBUG: Date strings: " + fromDateStr + " to " + toDateStr + ", Report type: " + reportType);
        
        String query;
        
        switch (reportType) {
            case "Product-wise Report":
                query = "SELECT " +
                        "ps.product_name AS product_name, " +
                        "SUM(srii.quantity) AS total_quantity_returned, " +
                        "SUM(srii.total_price) AS total_returned_amount " +
                        "FROM Sales_Return_Invoice sri " +
                        "LEFT JOIN Sales_Return_Invoice_Item srii ON sri.sales_return_invoice_id = srii.sales_return_invoice_id " +
                        "LEFT JOIN ProductionStock ps ON srii.production_stock_id = ps.production_id " +
                        "WHERE sri.return_date >= ? AND sri.return_date <= ? " +
                        "GROUP BY ps.product_name " +
                        "ORDER BY total_returned_amount DESC";
                break;
                
            case "Category-wise Report":
                // Since ProductionStock doesn't have category_id, we'll use a simplified approach
                // or join through Brand to get some categorization
                query = "SELECT " +
                        "b.brand_name AS category, " +
                        "SUM(srii.quantity) AS total_quantity_returned, " +
                        "SUM(srii.total_price) AS total_returned_amount " +
                        "FROM Sales_Return_Invoice sri " +
                        "LEFT JOIN Sales_Return_Invoice_Item srii ON sri.sales_return_invoice_id = srii.sales_return_invoice_id " +
                        "LEFT JOIN ProductionStock ps ON srii.production_stock_id = ps.production_id " +
                        "LEFT JOIN Brand b ON ps.brand_id = b.brand_id " +
                        "WHERE sri.return_date >= ? AND sri.return_date <= ? " +
                        "GROUP BY b.brand_name " +
                        "ORDER BY total_returned_amount DESC";
                break;
                
            case "Brand-wise Report":
                query = "SELECT " +
                        "b.brand_name AS brand_name, " +
                        "SUM(srii.quantity) AS total_quantity_returned, " +
                        "SUM(srii.total_price) AS total_returned_amount " +
                        "FROM Sales_Return_Invoice sri " +
                        "LEFT JOIN Sales_Return_Invoice_Item srii ON sri.sales_return_invoice_id = srii.sales_return_invoice_id " +
                        "LEFT JOIN ProductionStock ps ON srii.production_stock_id = ps.production_id " +
                        "LEFT JOIN Brand b ON ps.brand_id = b.brand_id " +
                        "WHERE sri.return_date >= ? AND sri.return_date <= ? " +
                        "GROUP BY b.brand_name " +
                        "ORDER BY total_returned_amount DESC";
                break;
                
            case "Manufacturer-wise Report":
                query = "SELECT " +
                        "m.manufacturer_name AS manufacturer_name, " +
                        "SUM(srii.quantity) AS total_quantity_returned, " +
                        "SUM(srii.total_price) AS total_returned_amount " +
                        "FROM Sales_Return_Invoice sri " +
                        "LEFT JOIN Sales_Return_Invoice_Item srii ON sri.sales_return_invoice_id = srii.sales_return_invoice_id " +
                        "LEFT JOIN ProductionStock ps ON srii.production_stock_id = ps.production_id " +
                        "LEFT JOIN Brand b ON ps.brand_id = b.brand_id " +
                        "LEFT JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id " +
                        "WHERE sri.return_date >= ? AND sri.return_date <= ? " +
                        "GROUP BY m.manufacturer_name " +
                        "ORDER BY total_returned_amount DESC";
                break;
                
            default: // "All Reports"
                query = "SELECT " +
                        "sri.return_invoice_number AS return_invoice_number, " +
                        "sri.return_date AS return_date, " +
                        "c.customer_name AS customer_name, " +
                        "sri.total_return_amount AS total_return_amount " +
                        "FROM Sales_Return_Invoice sri " +
                        "LEFT JOIN Customer c ON sri.customer_id = c.customer_id " +
                        "WHERE sri.return_date >= ? AND sri.return_date <= ? " +
                        "ORDER BY sri.return_date DESC";
                break;
        }
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            System.out.println("DEBUG: Executing return sales report query: " + query);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getReturnSalesReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBankTransferReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: getBankTransferReport called with dates: " + fromDate + " to " + toDate);
        
        // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sdf.format(fromDate);
        String toDateStr = sdf.format(toDate);
        
        System.out.println("DEBUG: Date strings: " + fromDateStr + " to " + toDateStr);
        
        // First, let's check all bank transactions to see what's available
        String debugQuery = "SELECT transaction_type, COUNT(*) as count FROM Bank_Transaction GROUP BY transaction_type";
        try (PreparedStatement debugStmt = connection.prepareStatement(debugQuery)) {
            ResultSet debugRs = debugStmt.executeQuery();
            System.out.println("DEBUG: Available transaction types in Bank_Transaction:");
            while (debugRs.next()) {
                System.out.println("  - " + debugRs.getString("transaction_type") + ": " + debugRs.getInt("count") + " records");
            }
            debugRs.close();
        } catch (SQLException e) {
            System.err.println("DEBUG: Error checking transaction types: " + e.getMessage());
        }
        
        // Check transactions in date range
        String dateCheckQuery = "SELECT COUNT(*) as count FROM Bank_Transaction WHERE transaction_date >= ? AND transaction_date <= ?";
        try (PreparedStatement dateStmt = connection.prepareStatement(dateCheckQuery)) {
            dateStmt.setString(1, fromDateStr);
            dateStmt.setString(2, toDateStr);
            ResultSet dateRs = dateStmt.executeQuery();
            if (dateRs.next()) {
                System.out.println("DEBUG: Transactions in date range " + fromDateStr + " to " + toDateStr + ": " + dateRs.getInt("count"));
            }
            dateRs.close();
        } catch (SQLException e) {
            System.err.println("DEBUG: Error checking date range: " + e.getMessage());
        }
        
        String query = "SELECT " +
                "bt.transaction_date, " +
                "COALESCE(" +
                "  CASE WHEN bt.bank_id != 0 THEN (SELECT bank_name FROM Bank WHERE bank_id = bt.bank_id) ELSE 'Cash' END," +
                "  'Unknown'" +
                ") as from_bank, " +
                "COALESCE(" +
                "  CASE WHEN bt.related_bank_id != 0 THEN (SELECT bank_name FROM Bank WHERE bank_id = bt.related_bank_id) ELSE 'Cash' END," +
                "  'Cash'" +
                ") as to_bank, " +
                "bt.amount, " +
                "bt.transaction_type, " +
                "COALESCE(bt.description, 'Bank Transfer') as description " +
                "FROM Bank_Transaction bt " +
                "WHERE bt.transaction_type IN ('transfer_in', 'transfer_out') " +
                "AND bt.transaction_date >= ? AND bt.transaction_date <= ? " +
                "ORDER BY bt.transaction_date DESC";
                
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            System.out.println("DEBUG: Executing bank transfer query: " + query);
            System.out.println("DEBUG: Parameters: " + fromDateStr + ", " + toDateStr);
            
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getBankTransferReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getProfitReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: Profit Report - Getting data from " + fromDate + " to " + toDate);
        
        // First, let's check table counts for debugging
        try {
            Statement stmt = connection.createStatement();
            ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM Sales_Invoice");
            if (countRs.next()) {
                System.out.println("DEBUG: Total Sales_Invoice records: " + countRs.getInt("count"));
            }
            
            countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM Sales_Invoice_Item");
            if (countRs.next()) {
                System.out.println("DEBUG: Total Sales_Invoice_Item records: " + countRs.getInt("count"));
            }
            
            countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM ProductionStock");
            if (countRs.next()) {
                System.out.println("DEBUG: Total ProductionStock records: " + countRs.getInt("count"));
            }
        } catch (SQLException e) {
            System.out.println("DEBUG: Error getting table counts: " + e.getMessage());
        }
        
        String query = "SELECT " +
                      "si.sales_invoice_number, " +
                      "si.sales_date, " +
                      "SUM(sii.quantity * CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE sii.unit_price END) as sale_amount, " +
                      "SUM(sii.quantity * ps.unit_cost) as cost_amount, " +
                      "(SUM(sii.quantity * CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE sii.unit_price END) - SUM(sii.quantity * ps.unit_cost)) as profit " +
                      "FROM Sales_Invoice si " +
                      "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                      "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                      "WHERE si.sales_date BETWEEN ? AND ? " +
                      "GROUP BY si.sales_invoice_number, si.sales_date " +
                      "ORDER BY si.sales_date DESC";
        
        System.out.println("DEBUG: Executing profit query: " + query);
        System.out.println("DEBUG: Date range: " + fromDate + " to " + toDate);
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Profit query executed successfully");
            
            // Test query without date filter to see if we get any results
            try {
                Statement testStmt = connection.createStatement();
                ResultSet testRs = testStmt.executeQuery(
                    "SELECT si.sales_invoice_number, si.sales_date, " +
                    "SUM(sii.quantity * CASE WHEN ps.sale_price > 0 THEN ps.sale_price ELSE sii.unit_price END) as sale_amount, " +
                    "SUM(sii.quantity * ps.unit_cost) as cost_amount " +
                    "FROM Sales_Invoice si " +
                    "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                    "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                    "GROUP BY si.sales_invoice_number, si.sales_date LIMIT 3"
                );
                int testCount = 0;
                while (testRs.next()) {
                    System.out.println("DEBUG: Test result - Invoice: " + testRs.getString("sales_invoice_number") + 
                                     ", Date: " + testRs.getString("sales_date") + 
                                     ", Sale: " + testRs.getDouble("sale_amount") + 
                                     ", Cost: " + testRs.getDouble("cost_amount"));
                    testCount++;
                }
                System.out.println("DEBUG: Test query returned " + testCount + " results");
            } catch (SQLException e) {
                System.out.println("DEBUG: Test query failed: " + e.getMessage());
            }
            
            return rs;
        } catch (SQLException e) {
            System.out.println("DEBUG: Error executing profit query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSummaryReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: Summary Report - Getting data from " + fromDate + " to " + toDate);
        
        // First, let's check what data exists in each table
        try {
            System.out.println("DEBUG: Checking table contents...");
            
            // Check Raw_Purchase_Invoice table
            String checkPurchases = "SELECT COUNT(*) as count, MIN(invoice_date) as min_date, MAX(invoice_date) as max_date, SUM(total_amount) as total FROM Raw_Purchase_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkPurchases)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Raw_Purchase_Invoice - Count: " + rs.getInt("count") + 
                                     ", Date range: " + rs.getString("min_date") + " to " + rs.getString("max_date") + 
                                     ", Total amount: " + rs.getDouble("total"));
                }
            }
            
            // Check Sales_Invoice table
            String checkSales = "SELECT COUNT(*) as count, MIN(sales_date) as min_date, MAX(sales_date) as max_date, SUM(total_amount) as total FROM Sales_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSales)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Sales_Invoice - Count: " + rs.getInt("count") + 
                                     ", Date range: " + rs.getString("min_date") + " to " + rs.getString("max_date") + 
                                     ", Total amount: " + rs.getDouble("total"));
                }
            }
            
            // Check Bank table
            String checkBank = "SELECT COUNT(*) as count, SUM(balance) as total FROM Bank";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkBank)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Bank - Count: " + rs.getInt("count") + ", Total balance: " + rs.getDouble("total"));
                }
            }
            
            // Check ProductionStock table
            String checkStock = "SELECT COUNT(*) as count, SUM(quantity * unit_cost) as total_value FROM ProductionStock";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkStock)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: ProductionStock - Count: " + rs.getInt("count") + ", Total value: " + rs.getDouble("total_value"));
                }
            }
            
            // Check return tables
            String checkPurchaseReturns = "SELECT COUNT(*) as count, SUM(total_return_amount) as total FROM Raw_Purchase_Return_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkPurchaseReturns)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Raw_Purchase_Return_Invoice - Count: " + rs.getInt("count") + ", Total: " + rs.getDouble("total"));
                }
            }
            
            String checkSalesReturns = "SELECT COUNT(*) as count, SUM(total_return_amount) as total FROM Sales_Return_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSalesReturns)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Sales_Return_Invoice - Count: " + rs.getInt("count") + ", Total: " + rs.getDouble("total"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("DEBUG: Error checking table contents: " + e.getMessage());
        }
        
        String query = "SELECT " +
                    "(SELECT COALESCE(SUM(total_amount), 0) FROM Raw_Purchase_Invoice WHERE invoice_date BETWEEN ? AND ?) AS total_purchases, " +
                    "(SELECT COALESCE(SUM(total_amount), 0) FROM Sales_Invoice WHERE sales_date BETWEEN ? AND ?) AS total_sales, " +
                    "(SELECT COALESCE(SUM(total_return_amount), 0) FROM Raw_Purchase_Return_Invoice WHERE return_date BETWEEN ? AND ?) AS total_purchase_returns, " +
                    "(SELECT COALESCE(SUM(total_return_amount), 0) FROM Sales_Return_Invoice WHERE return_date BETWEEN ? AND ?) AS total_sales_returns, " +
                    "(SELECT COALESCE(SUM(balance), 0) FROM Bank) AS total_bank_balance, " +
                    "(SELECT COUNT(*) FROM Customer) AS total_customers, " +
                    "(SELECT COUNT(*) FROM Supplier) AS total_suppliers, " +
                    "(SELECT COALESCE(SUM(quantity * unit_cost), 0) FROM ProductionStock) AS total_inventory_value";
        
        System.out.println("DEBUG: Executing summary query: " + query);
        
        try {
            // Don't use try-with-resources since we need to return the ResultSet
            PreparedStatement pstmt = connection.prepareStatement(query);
            
            // Debug the date parameters being used
            System.out.println("DEBUG: Date parameters:");
            System.out.println("  fromDate: " + fromDate + " (SQL Date: " + fromDate.toString() + ")");
            System.out.println("  toDate: " + toDate + " (SQL Date: " + toDate.toString() + ")");
            
            pstmt.setDate(1, fromDate);
            pstmt.setDate(2, toDate);
            pstmt.setDate(3, fromDate);
            pstmt.setDate(4, toDate);
            pstmt.setDate(5, fromDate);
            pstmt.setDate(6, toDate);
            pstmt.setDate(7, fromDate);
            pstmt.setDate(8, toDate);
            
            // Test individual subqueries to see which ones return data
            System.out.println("DEBUG: Testing individual subqueries...");
            
            // Test purchases in date range
            try (PreparedStatement testStmt = connection.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount), 0) as result FROM Raw_Purchase_Invoice WHERE invoice_date BETWEEN ? AND ?")) {
                testStmt.setDate(1, fromDate);
                testStmt.setDate(2, toDate);
                ResultSet testRs = testStmt.executeQuery();
                if (testRs.next()) {
                    System.out.println("  Purchases in date range: " + testRs.getDouble("result"));
                }
            }
            
            // Test sales in date range
            try (PreparedStatement testStmt = connection.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount), 0) as result FROM Sales_Invoice WHERE sales_date BETWEEN ? AND ?")) {
                testStmt.setDate(1, fromDate);
                testStmt.setDate(2, toDate);
                ResultSet testRs = testStmt.executeQuery();
                if (testRs.next()) {
                    System.out.println("  Sales in date range: " + testRs.getDouble("result"));
                }
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("DEBUG: Summary query executed successfully");
            
            return rs;
        } catch (SQLException e) {
            System.out.println("DEBUG: Error executing summary query: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBalanceSheet() {
        String query = "SELECT bank_name, balance FROM Bank";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object[] getBalanceSheetData() {
        try {
            System.out.println("DEBUG: Starting balance sheet data calculation...");
            
            // 1. Cash in hand from all banks
            double totalBankBalance = 0.0;
            String bankQuery = "SELECT COALESCE(SUM(balance), 0) as total FROM Bank";
            try (PreparedStatement pstmt = connection.prepareStatement(bankQuery)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalBankBalance = rs.getDouble("total");
                }
                System.out.println("DEBUG: Total bank balance: " + totalBankBalance);
            } catch (SQLException e) {
                System.err.println("DEBUG: Error getting bank balance: " + e.getMessage());
                totalBankBalance = 0.0;
            }
            
            // 2. Customer balances (money they owe us vs we owe them)
            double customersOweUs = 0.0;
            double weOweCustomers = 0.0;
            String customerQuery = "SELECT customer_name, balance FROM Customer";
            try (PreparedStatement pstmt = connection.prepareStatement(customerQuery)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    double balance = rs.getDouble("balance");
                    if (balance > 0) {
                        customersOweUs += balance; // Positive means they owe us
                    } else {
                        weOweCustomers += Math.abs(balance); // Negative means we owe them
                    }
                }
                System.out.println("DEBUG: Customers owe us: " + customersOweUs + ", We owe customers: " + weOweCustomers);
            } catch (SQLException e) {
                System.err.println("DEBUG: Error getting customer balances: " + e.getMessage());
            }
            
            // 3. Supplier balances (money we owe them vs they owe us)
            double weOweSuppliers = 0.0;
            double suppliersOweUs = 0.0;
            String supplierQuery = "SELECT supplier_name, balance FROM Supplier";
            try (PreparedStatement pstmt = connection.prepareStatement(supplierQuery)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    double balance = rs.getDouble("balance");
                    if (balance > 0) {
                        weOweSuppliers += balance; // Positive means we owe them
                    } else {
                        suppliersOweUs += Math.abs(balance); // Negative means they owe us
                    }
                }
                System.out.println("DEBUG: We owe suppliers: " + weOweSuppliers + ", Suppliers owe us: " + suppliersOweUs);
            } catch (SQLException e) {
                System.err.println("DEBUG: Error getting supplier balances: " + e.getMessage());
            }
            
            // Calculate net amounts
            double totalAssetsFromPeople = customersOweUs + suppliersOweUs; // Money coming to us
            double totalLiabilitiesToPeople = weOweCustomers + weOweSuppliers; // Money going from us
            double netWorth = totalBankBalance + totalAssetsFromPeople - totalLiabilitiesToPeople;
            
            System.out.println("DEBUG: Balance sheet calculated successfully");
            System.out.println("DEBUG: Net worth: " + netWorth);
            
            return new Object[] {
                totalBankBalance,        // 0 - Cash in hand (all banks)
                customersOweUs,          // 1 - Customers owe us
                weOweCustomers,          // 2 - We owe customers
                suppliersOweUs,          // 3 - Suppliers owe us
                weOweSuppliers,          // 4 - We owe suppliers
                totalAssetsFromPeople,   // 5 - Total receivables
                totalLiabilitiesToPeople,// 6 - Total payables
                netWorth                 // 7 - Net worth
            };
            
        } catch (Exception e) {
            System.err.println("Error calculating balance sheet: " + e.getMessage());
            e.printStackTrace();
            // Return array with proper default values instead of nulls
            return new Object[] {
                0.0, // totalBankBalance
                0.0, // customersOweUs  
                0.0, // weOweCustomers
                0.0, // suppliersOweUs
                0.0, // weOweSuppliers
                0.0, // totalAssetsFromPeople
                0.0, // totalLiabilitiesToPeople
                0.0  // netWorth
            };
        }
    }

    @Override
    public ResultSet getCustomersReport() {
        String query = "SELECT * FROM Customer";
        System.out.println("DEBUG: Executing customers report query: " + query);
        
        try {
            // First check if the table exists and has data
            String countQuery = "SELECT COUNT(*) as total FROM Customer";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalCustomers = countRs.getInt("total");
                    System.out.println("DEBUG: Total customers in database: " + totalCustomers);
                }
                countRs.close();
            }
            
            // Now execute the main query
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed customers report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getCustomersReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSuppliersReport() {
        String query = "SELECT * FROM Supplier";
        System.out.println("DEBUG: Executing suppliers report query: " + query);
        
        try {
            // First check if the table exists and has data
            String countQuery = "SELECT COUNT(*) as total FROM Supplier";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalSuppliers = countRs.getInt("total");
                    System.out.println("DEBUG: Total suppliers in database: " + totalSuppliers);
                }
                countRs.close();
            }
            
            // Now execute the main query
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed suppliers report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getSuppliersReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBrandSalesReport(Date fromDate, Date toDate) {
        System.out.println("DEBUG: getBrandSalesReport called with dates: " + fromDate + " to " + toDate);
        
        // Convert Date to String format (YYYY-MM-DD) for SQLite comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = sdf.format(fromDate);
        String toDateStr = sdf.format(toDate);
        
        System.out.println("DEBUG: Date strings: " + fromDateStr + " to " + toDateStr);
        
        // First, let's check what tables exist and have data
        try {
            // Check Sales_Invoice table
            String checkSalesQuery = "SELECT COUNT(*) as count FROM Sales_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSalesQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total Sales_Invoice records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check Sales_Invoice_Item table
            String checkItemsQuery = "SELECT COUNT(*) as count FROM Sales_Invoice_Item";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkItemsQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total Sales_Invoice_Item records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check ProductionStock table
            String checkProdQuery = "SELECT COUNT(*) as count FROM ProductionStock";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkProdQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total ProductionStock records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check Brand table
            String checkBrandQuery = "SELECT COUNT(*) as count FROM Brand";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkBrandQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Total Brand records: " + rs.getInt("count"));
                }
                rs.close();
            }
            
            // Check dates in Sales_Invoice
            String checkDatesQuery = "SELECT MIN(sales_date) as min_date, MAX(sales_date) as max_date FROM Sales_Invoice";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkDatesQuery)) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("DEBUG: Sales_Invoice date range: " + rs.getString("min_date") + " to " + rs.getString("max_date"));
                }
                rs.close();
            }
            
            // Test the query first without parameters to see all data
            String testQuery = "SELECT b.brand_name, SUM(sii.quantity) AS total_quantity, SUM(sii.total_price) AS total_sales FROM Sales_Invoice si JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id JOIN Brand b ON ps.brand_id = b.brand_id GROUP BY b.brand_name";
            try (PreparedStatement testStmt = connection.prepareStatement(testQuery)) {
                ResultSet testRs = testStmt.executeQuery();
                System.out.println("DEBUG: All brand sales data (no date filter):");
                while (testRs.next()) {
                    System.out.println("  Brand: " + testRs.getString("brand_name") + 
                                     ", Quantity: " + testRs.getDouble("total_quantity") + 
                                     ", Sales: " + testRs.getDouble("total_sales"));
                }
                testRs.close();
            }
            
        } catch (SQLException e) {
            System.err.println("DEBUG: Error checking table data: " + e.getMessage());
        }
        
        String query = "SELECT " +
                      "b.brand_name, " +
                      "SUM(sii.quantity) AS total_quantity, " +
                      "SUM(sii.total_price) AS total_sales, " +
                      "'N/A' AS salesman_name " +
                      "FROM Sales_Invoice si " +
                      "JOIN Sales_Invoice_Item sii ON si.sales_invoice_id = sii.sales_invoice_id " +
                      "JOIN ProductionStock ps ON sii.production_stock_id = ps.production_id " +
                      "JOIN Brand b ON ps.brand_id = b.brand_id " +
                      "WHERE si.sales_date >= ? AND si.sales_date <= ? " +
                      "GROUP BY b.brand_name " +
                      "ORDER BY total_sales DESC";
        
        System.out.println("DEBUG: Executing brand sales query: " + query);
        System.out.println("DEBUG: Parameters: " + fromDateStr + ", " + toDateStr);
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, fromDateStr);
            pstmt.setString(2, toDateStr);
            
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Query executed successfully, returning ResultSet");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getBrandSalesReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getBrandProfitReport(Date fromDate, Date toDate) {
        String query = "SELECT b.brand_name, SUM(s.quantity * s.unit_price - r.unit_cost) AS profit " +
                    "FROM Brand b JOIN Sales_Invoice_Item s ON b.brand_id = s.brand_id " +
                    "JOIN Raw_Stock r ON s.raw_stock_id = r.stock_id " +
                    "WHERE s.invoice_date BETWEEN ? AND ? GROUP BY b.brand_name";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, fromDate);
            pstmt.setDate(2, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getCustomerSalesReport(int customerId, Date fromDate, Date toDate) {
        String query = "SELECT * FROM Sales_Invoice WHERE customer_id = ? AND invoice_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            pstmt.setDate(2, fromDate);
            pstmt.setDate(3, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getSupplierSalesReport(int supplierId, Date fromDate, Date toDate) {
        String query = "SELECT * FROM Raw_Purchase_Invoice WHERE supplier_id = ? AND invoice_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            pstmt.setDate(2, fromDate);
            pstmt.setDate(3, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getAttendanceReport(int employeeId, Date fromDate, Date toDate) {
        String query = "SELECT * FROM Employee_Attendance WHERE employee_id = ? AND attendance_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setDate(2, fromDate);
            pstmt.setDate(3, toDate);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getAreaWiseReport() {
        String query = "SELECT 'Customer' as party_type, c.customer_name as name, " +
                    "c.contact_number, t.tehsil_name, d.district_name, p.province_name " +
                    "FROM Customer c " +
                    "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                    "LEFT JOIN District d ON t.district_id = d.district_id " +
                    "LEFT JOIN Province p ON d.province_id = p.province_id " +
                    "UNION ALL " +
                    "SELECT 'Supplier' as party_type, s.supplier_name as name, " +
                    "s.contact_number, t.tehsil_name, d.district_name, p.province_name " +
                    "FROM Supplier s " +
                    "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                    "LEFT JOIN District d ON t.district_id = d.district_id " +
                    "LEFT JOIN Province p ON d.province_id = p.province_id " +
                    "ORDER BY party_type, name";
        
        System.out.println("DEBUG: Executing area-wise report query: " + query);
        
        try {
            // First check if the tables exist and have data
            String countCustomersQuery = "SELECT COUNT(*) as total FROM Customer";
            try (PreparedStatement countStmt = connection.prepareStatement(countCustomersQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalCustomers = countRs.getInt("total");
                    System.out.println("DEBUG: Total customers for area-wise report: " + totalCustomers);
                }
                countRs.close();
            }
            
            String countSuppliersQuery = "SELECT COUNT(*) as total FROM Supplier";
            try (PreparedStatement countStmt = connection.prepareStatement(countSuppliersQuery)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalSuppliers = countRs.getInt("total");
                    System.out.println("DEBUG: Total suppliers for area-wise report: " + totalSuppliers);
                }
                countRs.close();
            }
            
            // Now execute the main query
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed area-wise report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getAreaWiseReport: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResultSet getAreaWiseReport(String partyType, String areaType, String areaValue) {
        StringBuilder query = new StringBuilder();
        
        // Base query for customers
        String customerQuery = "SELECT 'Customer' as party_type, c.customer_name as name, " +
                             "c.contact_number, t.tehsil_name, d.district_name, p.province_name " +
                             "FROM Customer c " +
                             "LEFT JOIN Tehsil t ON c.tehsil_id = t.tehsil_id " +
                             "LEFT JOIN District d ON t.district_id = d.district_id " +
                             "LEFT JOIN Province p ON d.province_id = p.province_id";
        
        // Base query for suppliers
        String supplierQuery = "SELECT 'Supplier' as party_type, s.supplier_name as name, " +
                             "s.contact_number, t.tehsil_name, d.district_name, p.province_name " +
                             "FROM Supplier s " +
                             "LEFT JOIN Tehsil t ON s.tehsil_id = t.tehsil_id " +
                             "LEFT JOIN District d ON t.district_id = d.district_id " +
                             "LEFT JOIN Province p ON d.province_id = p.province_id";
        
        // Add WHERE clause based on area type and value if specified
        String whereClause = "";
        if (areaValue != null && !areaValue.trim().isEmpty() && !areaValue.equals("All")) {
            switch (areaType.toLowerCase()) {
                case "province":
                    whereClause = " WHERE p.province_name = ?";
                    break;
                case "district":
                    whereClause = " WHERE d.district_name = ?";
                    break;
                case "tehsil":
                    whereClause = " WHERE t.tehsil_name = ?";
                    break;
            }
        }
        
        // Build final query based on party type
        if (partyType.equals("Customer")) {
            query.append(customerQuery).append(whereClause);
        } else if (partyType.equals("Supplier")) {
            query.append(supplierQuery).append(whereClause);
        } else {
            // Both customers and suppliers
            query.append(customerQuery).append(whereClause)
                 .append(" UNION ALL ")
                 .append(supplierQuery).append(whereClause);
        }
        
        query.append(" ORDER BY party_type, name");
        
        System.out.println("DEBUG: Executing filtered area-wise report query: " + query.toString());
        System.out.println("DEBUG: Parameters - PartyType: " + partyType + ", AreaType: " + areaType + ", AreaValue: " + areaValue);
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(query.toString());
            
            // Set parameters if needed
            if (areaValue != null && !areaValue.trim().isEmpty() && !areaValue.equals("All")) {
                if (partyType.equals("Both")) {
                    // Set parameter for both queries in UNION
                    pstmt.setString(1, areaValue);
                    pstmt.setString(2, areaValue);
                } else {
                    // Set parameter for single query
                    pstmt.setString(1, areaValue);
                }
            }
            
            ResultSet rs = pstmt.executeQuery();
            System.out.println("DEBUG: Successfully executed filtered area-wise report query");
            return rs;
        } catch (SQLException e) {
            System.err.println("DEBUG: SQLException in getAreaWiseReport (filtered): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ensure all required views exist in the database
     */
    public void ensureViewsExist() {
        try {
            // Drop and recreate View_Purchase_Book to fix column mismatch
            try {
                connection.createStatement().execute("DROP VIEW IF EXISTS View_Purchase_Book");
            } catch (SQLException e) {
                // Ignore if view doesn't exist
            }
            
            // Create detailed view with item-level data (14 columns as expected)
            String sql = "CREATE VIEW View_Purchase_Book AS " +
                       "SELECT " +
                       "    rpi.raw_purchase_invoice_id, " +           // 0: invoice_id
                       "    rpi.invoice_number, " +                    // 1: invoice_number
                       "    s.supplier_name, " +                      // 2: supplier_name
                       "    rpi.invoice_date, " +                     // 3: invoice_date
                       "    rs.item_name, " +                         // 4: item_name
                       "    b.brand_name, " +                         // 5: brand_name
                       "    m.manufacturer_name, " +                  // 6: manufacturer_name
                       "    rpii.quantity, " +                        // 7: quantity
                       "    rpii.unit_price, " +                      // 8: unit_price
                       "    (rpii.quantity * rpii.unit_price) AS item_total, " + // 9: item_total
                       "    rpi.total_amount, " +                     // 10: total_amount
                       "    rpi.discount_amount, " +                  // 11: discount_amount
                       "    rpi.paid_amount, " +                      // 12: paid_amount
                       "    (rpi.total_amount - rpi.paid_amount) AS balance " + // 13: balance
                       "FROM Raw_Purchase_Invoice rpi " +
                       "JOIN Supplier s ON rpi.supplier_id = s.supplier_id " +
                       "JOIN Raw_Purchase_Invoice_Item rpii ON rpi.raw_purchase_invoice_id = rpii.raw_purchase_invoice_id " +
                       "JOIN Raw_Stock rs ON rpii.raw_stock_id = rs.stock_id " +
                       "JOIN Brand b ON rs.brand_id = b.brand_id " +
                       "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Created View_Purchase_Book with 14 columns (item-level details)");
            
            // Drop and recreate View_Return_Purchase_Book to fix column mismatch
            try {
                connection.createStatement().execute("DROP VIEW IF EXISTS View_Return_Purchase_Book");
            } catch (SQLException e) {
                // Ignore if view doesn't exist
            }
            
            sql = "CREATE VIEW View_Return_Purchase_Book AS " +
                "SELECT " +
                "    rpri.raw_purchase_return_invoice_id AS raw_purchase_invoice_id, " +     // 0: invoice_id
                "    rpri.return_invoice_number AS invoice_number, " +          // 1: invoice_number
                "    s.supplier_name, " +                                       // 2: supplier_name
                "    rpri.return_date AS invoice_date, " +                      // 3: invoice_date
                "    rs.item_name, " +                                          // 4: item_name
                "    b.brand_name, " +                                          // 5: brand_name
                "    m.manufacturer_name, " +                                   // 6: manufacturer_name
                "    rprii.quantity AS quantity, " +                            // 7: quantity
                "    rprii.unit_price, " +                                      // 8: unit_price
                "    (rprii.quantity * rprii.unit_price) AS item_total, " +     // 9: item_total
                "    rpri.total_return_amount AS total_amount, " +              // 10: total_amount
                "    0.0 AS discount_amount, " +                                // 11: discount_amount
                "    rpri.total_return_amount AS paid_amount, " +               // 12: paid_amount
                "    0.0 AS balance " +                                         // 13: balance
                "FROM Raw_Purchase_Return_Invoice rpri " +
                "JOIN Supplier s ON rpri.supplier_id = s.supplier_id " +
                "JOIN Raw_Purchase_Return_Invoice_Item rprii ON rpri.raw_purchase_return_invoice_id = rprii.raw_purchase_return_invoice_id " +
                "JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                "JOIN Brand b ON rs.brand_id = b.brand_id " +
                "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Created View_Return_Purchase_Book with 14 columns (item-level details)");
            
            // Drop and recreate View_Raw_Stock_Book to fix column mismatch
            try {
                connection.createStatement().execute("DROP VIEW IF EXISTS View_Raw_Stock_Book");
            } catch (SQLException e) {
                // Ignore if view doesn't exist
            }
            
            sql = "CREATE VIEW View_Raw_Stock_Book AS " +
                "SELECT " +
                "    rsui.raw_stock_use_invoice_id AS raw_purchase_invoice_id, " +         // 0: invoice_id
                "    rsui.use_invoice_number AS invoice_number, " +              // 1: invoice_number
                "    'Internal Usage' AS supplier_name, " +                      // 2: supplier_name
                "    rsui.usage_date AS invoice_date, " +                        // 3: invoice_date
                "    rs.item_name, " +                                           // 4: item_name
                "    b.brand_name, " +                                           // 5: brand_name
                "    m.manufacturer_name, " +                                    // 6: manufacturer_name
                "    rsuii.quantity_used AS quantity, " +                        // 7: quantity
                "    rsuii.unit_cost AS unit_price, " +                          // 8: unit_price
                "    rsuii.total_cost AS item_total, " +                         // 9: item_total
                "    rsui.total_usage_amount AS total_amount, " +                // 10: total_amount
                "    0.0 AS discount_amount, " +                                 // 11: discount_amount
                "    rsui.total_usage_amount AS paid_amount, " +                 // 12: paid_amount
                "    0.0 AS balance " +                                          // 13: balance
                "FROM Raw_Stock_Use_Invoice rsui " +
                "JOIN Raw_Stock_Use_Invoice_Item rsuii ON rsui.raw_stock_use_invoice_id = rsuii.raw_stock_use_invoice_id " +
                "JOIN Raw_Stock rs ON rsuii.raw_stock_id = rs.stock_id " +
                "JOIN Brand b ON rs.brand_id = b.brand_id " +
                "JOIN Manufacturer m ON b.manufacturer_id = m.manufacturer_id";
            connection.createStatement().execute(sql);
            System.out.println("Created View_Raw_Stock_Book with 14 columns (item-level details)");
            
        } catch (SQLException e) {
            System.err.println("Error creating views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int getSalesInvoiceIdByNumber(String invoiceNumber) {
        String query = "SELECT sales_invoice_id FROM Sales_Invoice WHERE sales_invoice_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, invoiceNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("sales_invoice_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales invoice ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getSalesReturnInvoiceIdByNumber(String returnInvoiceNumber) {
        String query = "SELECT sales_return_invoice_id FROM Sales_Return_Invoice WHERE return_invoice_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, returnInvoiceNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("sales_return_invoice_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales return invoice ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Object[]> getSalesReturnInvoiceItemsByInvoiceId(int returnInvoiceId) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT srii.production_stock_id, ps.product_name, srii.quantity, srii.unit_price " +
                    "FROM Sales_Return_Invoice_Item srii " +
                    "JOIN ProductionStock ps ON srii.production_stock_id = ps.production_id " +
                    "WHERE srii.sales_return_invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, returnInvoiceId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("production_stock_id"),
                    rs.getString("product_name"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price")
                };
                items.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales return invoice items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public boolean insertContractEmployee(String name, String phone, String cnic, String address, String remarks, String task, int numTasks,
            double costPerTask, int totalTasksDone, String date) {
        String sql = "INSERT INTO Contract_Employee (name, phone, cnic, address, remarks, task, num_tasks, cost_per_task, total_tasks_done, date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, cnic);
            pstmt.setString(4, address);
            pstmt.setString(5, remarks);
            pstmt.setString(6, task);
            pstmt.setInt(7, numTasks);
            pstmt.setDouble(8, costPerTask);
            pstmt.setInt(9, totalTasksDone);
            pstmt.setString(10, date);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting contract employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getContractEmployeeRecords(LocalDate dateFrom, LocalDate dateTo, String timeFrom, String timeTo) {
        List<Object[]> records = new ArrayList<>();
        if (dateFrom == null || dateTo == null) {
            System.err.println("Error: dateFrom or dateTo is null in getContractEmployeeRecords");
            return records;
        }
        String query = "SELECT * FROM Contract_Employee WHERE date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dateFrom.toString());
            pstmt.setString(2, dateTo.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("employee_id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("cnic"),
                    rs.getString("address"),
                    rs.getString("remarks"),
                    rs.getString("task"),
                    rs.getInt("num_tasks"),
                    rs.getDouble("cost_per_task"),
                    rs.getInt("total_tasks_done"),
                    rs.getString("date"),
                    rs.getString("created_at")
                };
                records.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting contract employee records: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

}


