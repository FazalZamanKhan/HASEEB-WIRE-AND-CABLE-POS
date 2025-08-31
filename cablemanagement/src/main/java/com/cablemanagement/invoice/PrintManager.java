package com.cablemanagement.invoice;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Centralized print management for all invoice-related printing operations
 */
public class PrintManager {
    
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Print an invoice with user confirmation
     * @param invoiceData The invoice data to print
     * @param invoiceType The type of invoice (e.g., "Sales", "Purchase", "Return")
     * @return true if printing was successful
     */
    public static boolean printInvoice(InvoiceData invoiceData, String invoiceType) {
        return printInvoice(invoiceData, invoiceType, true);
    }
    
    /**
     * Print an invoice with optional user confirmation
     * @param invoiceData The invoice data to print
     * @param invoiceType The type of invoice (e.g., "Sales", "Purchase", "Return")
     * @param showConfirmation Whether to show confirmation dialog
     * @return true if printing was successful
     */
    public static boolean printInvoice(InvoiceData invoiceData, String invoiceType, boolean showConfirmation) {
        try {
            if (showConfirmation) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Print Invoice");
                confirmAlert.setHeaderText("Print " + invoiceType + " Invoice");
                confirmAlert.setContentText("Do you want to print Invoice #" + invoiceData.getInvoiceNumber() + "?");
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return false;
                }
            }
            
            // Generate temporary filename
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_Invoice_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate and print
            boolean success = InvoiceGenerator.generateAndPrint(invoiceData, filename);
            
            if (success) {
                showSuccessAlert("Print Successful", 
                    invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + " has been sent to the printer.");
            } else {
                showErrorAlert("Print Failed", 
                    "Failed to print " + invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + 
                    ". Please check your printer connection and try again.");
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Print Error", "An error occurred while printing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Print with printer selection dialog
     * @param invoiceData The invoice data to print
     * @param invoiceType The type of invoice
     * @return true if printing was successful
     */
    public static boolean printInvoiceWithPrinterSelection(InvoiceData invoiceData, String invoiceType) {
        try {
            // Get available printers
            String[] printers = InvoiceGenerator.getAvailablePrinters();
            
            if (printers.length == 0) {
                showErrorAlert("No Printers", "No printers are available. Please check your printer installation.");
                return false;
            }
            
            // Show printer selection dialog
            ChoiceDialog<String> printerDialog = new ChoiceDialog<>(printers[0], printers);
            printerDialog.setTitle("Select Printer");
            printerDialog.setHeaderText("Print " + invoiceType + " Invoice");
            printerDialog.setContentText("Choose a printer for Invoice #" + invoiceData.getInvoiceNumber() + ":");
            
            Optional<String> selectedPrinter = printerDialog.showAndWait();
            if (selectedPrinter.isEmpty()) {
                return false;
            }
            
            // Generate temporary filename
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_Invoice_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate PDF first
            InvoiceGenerator.generatePDF(invoiceData, filename);
            
            // Print to selected printer
            boolean success = InvoiceGenerator.printToSpecificPrinter(filename, selectedPrinter.get());
            
            if (success) {
                showSuccessAlert("Print Successful", 
                    invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + 
                    " has been sent to printer: " + selectedPrinter.get());
            } else {
                showErrorAlert("Print Failed", 
                    "Failed to print " + invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + 
                    " to printer: " + selectedPrinter.get());
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Print Error", "An error occurred while printing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Print multiple invoices in batch
     * @param invoiceDataList List of invoice data to print
     * @param invoiceType The type of invoices
     * @return Number of successfully printed invoices
     */
    public static int printInvoicesBatch(List<InvoiceData> invoiceDataList, String invoiceType) {
        if (invoiceDataList.isEmpty()) {
            showErrorAlert("No Data", "No invoices to print.");
            return 0;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Batch Print");
        confirmAlert.setHeaderText("Print Multiple " + invoiceType + " Invoices");
        confirmAlert.setContentText("Do you want to print " + invoiceDataList.size() + " " + invoiceType + " invoices?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return 0;
        }
        
        int successCount = 0;
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        for (int i = 0; i < invoiceDataList.size(); i++) {
            InvoiceData invoiceData = invoiceDataList.get(i);
            
            try {
                String filename = TEMP_DIR + File.separator + invoiceType + "_Invoice_" + 
                                 invoiceData.getInvoiceNumber() + "_" + timestamp + "_" + (i + 1) + ".pdf";
                
                if (InvoiceGenerator.generateAndPrint(invoiceData, filename)) {
                    successCount++;
                }
                
                // Small delay between prints to avoid overwhelming the printer
                Thread.sleep(500);
                
            } catch (Exception e) {
                System.err.println("Failed to print invoice " + invoiceData.getInvoiceNumber() + ": " + e.getMessage());
            }
        }
        
        if (successCount == invoiceDataList.size()) {
            showSuccessAlert("Batch Print Successful", 
                "All " + successCount + " " + invoiceType + " invoices have been sent to the printer.");
        } else {
            showErrorAlert("Partial Print Success", 
                successCount + " out of " + invoiceDataList.size() + " " + invoiceType + 
                " invoices were printed successfully.");
        }
        
        return successCount;
    }
    
    /**
     * Preview and then print an invoice
     * @param invoiceData The invoice data
     * @param invoiceType The type of invoice
     * @return true if printing was successful
     */
    public static boolean previewAndPrint(InvoiceData invoiceData, String invoiceType) {
        try {
            // Generate temporary filename for preview
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_Preview_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate PDF for preview
            InvoiceGenerator.generatePDF(invoiceData, filename);
            
            // Print directly without showing preview dialog
            return InvoiceGenerator.printPDF(filename);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Preview Error", "An error occurred while generating preview: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Open invoice PDF for preview and allow user to print using system print dialog (like Ctrl+P)
     * This opens the PDF in the default PDF viewer where users can use Ctrl+P to print
     * @param invoiceData The invoice data
     * @param invoiceType The type of invoice
     * @return true if PDF was successfully opened for preview
     */
    public static boolean openInvoiceForPrintPreview(InvoiceData invoiceData, String invoiceType) {
        final File[] pdfFile = new File[1];
        try {
            // Generate temporary filename for preview
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_PrintPreview_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate PDF and ensure it's properly created
            System.out.println("Generating PDF: " + filename);
            
            try {
                InvoiceGenerator.generatePDF(invoiceData, filename);
            } catch (Exception pdfEx) {
                showErrorAlert("PDF Generation Error", "Failed to generate PDF: " + pdfEx.getMessage());
                return false;
            }
            
            // Verify the PDF file was created and is not empty
            pdfFile[0] = new File(filename);
            if (!pdfFile[0].exists()) {
                showErrorAlert("PDF Generation Failed", "The PDF file could not be created.\nFile: " + filename);
                return false;
            }
            
            if (pdfFile[0].length() == 0) {
                showErrorAlert("PDF Generation Failed", "The PDF file is empty or corrupted.\nFile: " + filename);
                return false;
            }
            
            System.out.println("PDF generated successfully: " + pdfFile[0].length() + " bytes");
            
            // Add a small delay to ensure file is completely written and closed
            Thread.sleep(1000);
            
            // Open PDF in default viewer (like when pressing Ctrl+P)
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    System.out.println("Opening PDF in default viewer...");
                    
                    // Open PDF directly without dialog
                    // Open PDF in a new thread to prevent blocking
                    new Thread(() -> {
                        try {
                            String os = System.getProperty("os.name").toLowerCase();
                            if (os.contains("linux")) {
                                // Use xdg-open on Linux for better compatibility
                                Runtime.getRuntime().exec(new String[]{"xdg-open", pdfFile[0].getAbsolutePath()});
                            } else {
                                desktop.open(pdfFile[0]);
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                showErrorAlert("Preview Error", 
                                    "Could not open PDF viewer automatically.\n\n" +
                                    "The PDF has been saved at:\n" + filename + "\n\n" +
                                    "You can manually open this file with any PDF viewer.\n\n" +
                                    "Error: " + e.getMessage());
                            });
                        }
                    }, "PDFPreviewThread").start();
                    
                    return true;
                } else {
                    showErrorAlert("Cannot Open File", "Your system doesn't support opening files automatically.\n\nThe PDF has been saved at:\n" + filename + "\n\nYou can manually open this file with any PDF viewer.");
                    return false;
                }
            } else {
                showErrorAlert("Desktop Not Supported", "Desktop operations are not supported on this system.\n\nThe PDF has been saved at:\n" + filename + "\n\nYou can manually open this file with any PDF viewer.");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Preview Error", "An error occurred while opening the invoice for preview:\n\n" + e.getMessage() + 
                          (pdfFile[0] != null ? "\n\nFile location: " + pdfFile[0].getAbsolutePath() : ""));
            return false;
        }
    }
    
    /**
     * Get printer status information
     * @return String containing printer status
     */
    public static String getPrinterStatus() {
        try {
            String[] printers = InvoiceGenerator.getAvailablePrinters();
            if (printers.length == 0) {
                return "No printers available";
            }
            
            StringBuilder status = new StringBuilder();
            status.append("Available Printers (").append(printers.length).append("):\n");
            for (String printer : printers) {
                status.append("• ").append(printer).append("\n");
            }
            
            return status.toString().trim();
            
        } catch (Exception e) {
            return "Error getting printer status: " + e.getMessage();
        }
    }
    
    /**
     * Open any PDF file for preview and printing
     * @param filename The path to the PDF file
     * @param documentType The type of document (e.g., "Balance Sheet", "Report")
     * @return true if PDF was successfully opened for preview
     */
    public static boolean openPDFForPreview(String filename, String documentType) {
        try {
            File pdfFile = new File(filename);
            if (!pdfFile.exists()) {
                System.out.println("INFO: PDF file not found, but continuing: " + filename);
                return false;
            }
            
            if (pdfFile.length() == 0) {
                System.out.println("INFO: PDF file is empty, but continuing: " + filename);
                return false;
            }
            
            System.out.println("Opening " + documentType + " PDF: " + pdfFile.length() + " bytes");
            
            // Add a small delay to ensure file is completely written and closed
            Thread.sleep(500);
            
            // Open PDF in default viewer
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    System.out.println("Opening PDF in default viewer...");
                    
                    // Open PDF directly without dialog
                    // Open PDF in a new thread to prevent blocking
                    new Thread(() -> {
                        try {
                            String os = System.getProperty("os.name").toLowerCase();
                            if (os.contains("linux")) {
                                // Use xdg-open on Linux for better compatibility
                                Runtime.getRuntime().exec(new String[]{"xdg-open", pdfFile.getAbsolutePath()});
                            } else {
                                desktop.open(pdfFile);
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                showErrorAlert("Preview Error", 
                                    "Could not open PDF viewer automatically.\n\n" +
                                    "The PDF has been saved at:\n" + filename + "\n\n" +
                                    "You can manually open this file with any PDF viewer.\n\n" +
                                    "Error: " + e.getMessage());
                            });
                        }
                    }, "PDFPreviewThread").start();
                    
                    return true;
                } else {
                    showErrorAlert("Desktop Not Supported", 
                        "Cannot open files automatically on this system.\n\n" +
                        "The PDF has been saved at:\n" + filename + "\n\n" +
                        "Please open this file manually with your PDF viewer.");
                    return false;
                }
            } else {
                showErrorAlert("Desktop Not Supported", 
                    "Desktop operations are not supported on this system.\n\n" +
                    "The PDF has been saved at:\n" + filename + "\n\n" +
                    "Please open this file manually with your PDF viewer.");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Preview Error", "An error occurred while opening the PDF: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clean up temporary print files older than 24 hours
     */
    public static void cleanupTempFiles() {
        try {
            File tempDir = new File(TEMP_DIR);
            File[] files = tempDir.listFiles((dir, name) -> 
                name.contains("Invoice_") && name.endsWith(".pdf"));
            
            if (files != null) {
                long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
                int deletedCount = 0;
                
                for (File file : files) {
                    if (file.lastModified() < oneDayAgo) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                }
                
                if (deletedCount > 0) {
                    System.out.println("Cleaned up " + deletedCount + " temporary print files.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error cleaning up temporary files: " + e.getMessage());
        }
    }
    
    /**
     * Print Area Summary Report as PDF
     * @param summaryTable The table containing the area summary data
     * @param summaryType The type of summary (Tehsil, District, Province)
     * @param partyType The party type (Customer, Supplier)
     * @param fromDate Start date for the report
     * @param toDate End date for the report
     * @return true if PDF generation was successful
     */
    public static boolean printAreaSummaryReport(javafx.scene.control.TableView<?> summaryTable, 
                                               String summaryType, String partyType, 
                                               java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        try {
            // Generate PDF filename
            String timestamp = java.time.LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = String.format("AreaSummary_%s_%s_%s.pdf", summaryType, partyType, timestamp);
            String fullPath = TEMP_DIR + File.separator + filename;
            
            // Create PDF using iText (you may need to add iText dependency)
            // For now, let's create a simple text-based approach
            generateAreaSummaryPDF(summaryTable, summaryType, partyType, fromDate, toDate, fullPath);
            
            // Open the PDF
            boolean opened = openPDFForPreview(filename, "Area Summary Report");
            
            if (opened) {
                showSuccessAlert("PDF Generated", "Area Summary Report PDF has been generated and opened successfully!");
                return true;
            } else {
                System.out.println("INFO: PDF was generated but could not be opened automatically.");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("INFO: An error occurred while generating PDF: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Generate Area Summary PDF using simple text formatting
     */
    private static void generateAreaSummaryPDF(javafx.scene.control.TableView<?> summaryTable,
                                             String summaryType, String partyType,
                                             java.time.LocalDate fromDate, java.time.LocalDate toDate,
                                             String filePath) throws Exception {
        
        // For now, create a simple HTML file that can be converted to PDF
        // This is a basic implementation - you might want to use a proper PDF library
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html>\n<head>\n");
        htmlContent.append("<title>Area Summary Report</title>\n");
        htmlContent.append("<style>\n");
        htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        htmlContent.append("h1 { color: #2E7D32; text-align: center; }\n");
        htmlContent.append("h2 { color: #666; text-align: center; font-size: 14px; }\n");
        htmlContent.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
        htmlContent.append("th, td { padding: 12px; text-align: left; border: 1px solid #ddd; }\n");
        htmlContent.append("th { background-color: #4CAF50; color: white; }\n");
        htmlContent.append("tr:nth-child(even) { background-color: #f2f2f2; }\n");
        htmlContent.append("tr:last-child { font-weight: bold; background-color: #e8f5e8; }\n");
        htmlContent.append(".amount { text-align: right; }\n");
        htmlContent.append("</style>\n");
        htmlContent.append("</head>\n<body>\n");
        
        // Header
        htmlContent.append("<h1>Area Summary Report - ").append(summaryType).append(" wise ").append(partyType).append(" Summary</h1>\n");
        
        String dateRange = "All Dates";
        if (fromDate != null && toDate != null) {
            dateRange = "From: " + fromDate + " To: " + toDate;
        } else if (fromDate != null) {
            dateRange = "From: " + fromDate;
        } else if (toDate != null) {
            dateRange = "To: " + toDate;
        }
        
        htmlContent.append("<h2>Period: ").append(dateRange).append("</h2>\n");
        htmlContent.append("<h2>Generated on: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</h2>\n");
        
        // Table
        htmlContent.append("<table>\n");
        htmlContent.append("<thead>\n<tr>\n");
        htmlContent.append("<th>").append(summaryType).append(" Name</th>\n");
        htmlContent.append("<th class=\"amount\">Total ").append(partyType.equals("Customer") ? "Sales" : "Purchase").append(" Amount</th>\n");
        htmlContent.append("</tr>\n</thead>\n<tbody>\n");
        
        // Table data
        var items = summaryTable.getItems();
        for (Object item : items) {
            if (item != null) {
                // Assuming the item has getAreaName() and getTotalSales() methods
                try {
                    String areaName = (String) item.getClass().getMethod("getAreaName").invoke(item);
                    String totalSales = (String) item.getClass().getMethod("getTotalSales").invoke(item);
                    
                    htmlContent.append("<tr>\n");
                    htmlContent.append("<td>").append(areaName != null ? areaName : "").append("</td>\n");
                    htmlContent.append("<td class=\"amount\">").append(totalSales != null ? totalSales : "0.00").append("</td>\n");
                    htmlContent.append("</tr>\n");
                } catch (Exception e) {
                    // Skip if reflection fails
                    continue;
                }
            }
        }
        
        htmlContent.append("</tbody>\n</table>\n");
        htmlContent.append("</body>\n</html>");
        
        // Write HTML file (can be opened as PDF in browsers or converted)
        String htmlFilePath = filePath.replace(".pdf", ".html");
        try (java.io.FileWriter writer = new java.io.FileWriter(htmlFilePath)) {
            writer.write(htmlContent.toString());
        }
        
        // For now, just show the HTML file - in production you might want to convert to actual PDF
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(new File(htmlFilePath));
        }
    }
    
    // Helper methods for showing alerts
    private static void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
