package com.cablemanagement.invoice;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * Helper class for console/non-GUI operations
 */
public class ConsoleHelper {
    
    /**
     * Open a file with the system default application
     * @param filePath Path to the file to open
     * @return true if successful, false otherwise
     */
    public static boolean openFileWithDefaultApp(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return false;
            }
            
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file);
                    return true;
                } else {
                    // Try platform-specific commands if desktop.open is not supported
                    String os = System.getProperty("os.name").toLowerCase();
                    ProcessBuilder pb;
                    
                    if (os.contains("windows")) {
                        pb = new ProcessBuilder("cmd", "/c", "start", filePath);
                    } else if (os.contains("mac")) {
                        pb = new ProcessBuilder("open", filePath);
                    } else {
                        // Linux and others
                        pb = new ProcessBuilder("xdg-open", filePath);
                    }
                    
                    Process process = pb.start();
                    return process.waitFor() == 0;
                }
            } else {
                System.err.println("Desktop operations not supported");
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error opening file: " + e.getMessage());
            return false;
        }
    }
}
