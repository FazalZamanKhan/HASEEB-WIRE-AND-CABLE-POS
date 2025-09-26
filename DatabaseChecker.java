import java.sql.*;

public class DatabaseChecker {
    public static void main(String[] args) {
        String dbPath = "cablemanagement/src/main/resources/db/HASEEB_WIRES_CABLES.db";
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            // Query the User table
            String query = "SELECT * FROM User";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("User table contents:");
            System.out.println("ID | Username | Password | Role");
            System.out.println("---|----------|----------|-----");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                System.out.println(id + " | " + username + " | " + password + " | " + role);
            }
            
            // Also show table structure
            System.out.println("\nUser table structure:");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "User", null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String datatype = columns.getString("TYPE_NAME");
                int size = columns.getInt("COLUMN_SIZE");
                System.out.println("Column: " + columnName + " - Type: " + datatype + " - Size: " + size);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}