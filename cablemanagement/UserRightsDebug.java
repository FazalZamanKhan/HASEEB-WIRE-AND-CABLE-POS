import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UserRightsDebug {
    public static void main(String[] args) {
        String dbPath = "cable_management.db";
        
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to database
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to database successfully!");
            
            // Check if user 'fazal' exists
            System.out.println("\n=== CHECKING USER 'fazal' ===");
            String userQuery = "SELECT id, username, password_hash, role, is_active FROM User WHERE username = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, "fazal");
            ResultSet userRs = userStmt.executeQuery();
            
            if (userRs.next()) {
                System.out.println("User 'fazal' found:");
                System.out.println("ID: " + userRs.getInt("id"));
                System.out.println("Username: " + userRs.getString("username"));
                System.out.println("Password Hash: " + userRs.getString("password_hash"));
                System.out.println("Role: " + userRs.getString("role"));
                System.out.println("Is Active: " + userRs.getBoolean("is_active"));
            } else {
                System.out.println("User 'fazal' NOT FOUND in database!");
            }
            
            // Check existing rights for fazal
            System.out.println("\n=== EXISTING RIGHTS FOR 'fazal' ===");
            String rightsQuery = "SELECT page_name, granted_by, granted_date FROM User_Rights WHERE username = ?";
            PreparedStatement rightsStmt = conn.prepareStatement(rightsQuery);
            rightsStmt.setString(1, "fazal");
            ResultSet rightsRs = rightsStmt.executeQuery();
            
            boolean hasRights = false;
            while (rightsRs.next()) {
                hasRights = true;
                System.out.println("Page: " + rightsRs.getString("page_name"));
                System.out.println("Granted By: " + rightsRs.getString("granted_by"));
                System.out.println("Granted Date: " + rightsRs.getString("granted_date"));
                System.out.println("---");
            }
            
            if (!hasRights) {
                System.out.println("No rights found for user 'fazal'");
            }
            
            // Check all users in the system
            System.out.println("\n=== ALL USERS IN SYSTEM ===");
            String allUsersQuery = "SELECT username, role, is_active FROM User ORDER BY username";
            PreparedStatement allUsersStmt = conn.prepareStatement(allUsersQuery);
            ResultSet allUsersRs = allUsersStmt.executeQuery();
            
            while (allUsersRs.next()) {
                System.out.println("Username: " + allUsersRs.getString("username") + 
                                 ", Role: " + allUsersRs.getString("role") + 
                                 ", Active: " + allUsersRs.getBoolean("is_active"));
            }
            
            // Test adding rights to fazal if the user exists
            if (userExists(conn, "fazal")) {
                System.out.println("\n=== TESTING ADD RIGHTS ===");
                List<String> testRights = Arrays.asList("Accounts", "Register");
                boolean success = addUserRights(conn, "fazal", testRights, "admin");
                System.out.println("Add rights result: " + success);
            }
            
            conn.close();
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean userExists(Connection conn, String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM User WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    private static boolean addUserRights(Connection conn, String username, List<String> rights, String grantedBy) {
        if (rights == null || rights.isEmpty()) {
            System.out.println("Rights list is null or empty");
            return false;
        }
        
        System.out.println("DEBUG addUserRights: username=" + username + ", rights=" + rights + ", grantedBy=" + grantedBy);
        
        String query = "INSERT OR IGNORE INTO User_Rights (username, page_name, granted_by) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (String right : rights) {
                System.out.println("DEBUG: Adding right '" + right + "' for user '" + username + "'");
                pstmt.setString(1, username);
                pstmt.setString(2, right);
                pstmt.setString(3, grantedBy);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            System.out.println("DEBUG: Batch execution results: " + Arrays.toString(results));
            return results.length > 0;
        } catch (SQLException e) {
            System.err.println("ERROR in addUserRights: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}