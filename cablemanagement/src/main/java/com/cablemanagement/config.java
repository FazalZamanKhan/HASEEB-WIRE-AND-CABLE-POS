package com.cablemanagement;

import com.cablemanagement.database.SQLiteDatabase;
import com.cablemanagement.database.db;

public class config {
    public static db database = new SQLiteDatabase();
    
    // Current user session management
    private static String currentUsername = null;
    private static String currentUserRole = null;
    
    public static void setCurrentUser(String username, String role) {
        currentUsername = username;
        currentUserRole = role;
    }
    
    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    public static String getCurrentUserRole() {
        return currentUserRole;
    }
    
    public static boolean isCurrentUserAdmin() {
        return "admin".equals(currentUserRole);
    }
    
    public static void clearCurrentUser() {
        currentUsername = null;
        currentUserRole = null;
    }
    
    public static boolean hasCurrentUserRight(String pageName) {
        if (currentUsername == null) {
            return false;
        }
        
        if (isCurrentUserAdmin()) {
            return true; // Admin has access to all pages
        }
        
        return database != null && database.hasUserRight(currentUsername, pageName);
    }
}
