package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.File;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LedgerPDFGenerator {
    
    public static void generateCustomerLedgerPDF(String customerName, List<Object[]> ledgerData, 
            double totalSale, double totalPayment, double totalReturn, double currentBalance, String filename) {
        try {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 50); // Landscape orientation
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

            // Company Header
            Paragraph title = new Paragraph("Customer Ledger Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Customer Name
            Paragraph customerInfo = new Paragraph("Customer: " + customerName, headerFont);
            customerInfo.setAlignment(Element.ALIGN_LEFT);
            customerInfo.setSpacingAfter(10);
            document.add(customerInfo);

            // Print Date
            String printDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph dateInfo = new Paragraph("Generated on: " + printDate, regularFont);
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingAfter(15);
            document.add(dateInfo);

            // Create table with 11 columns
            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            
            // Set relative column widths
            float[] columnWidths = {1f, 2f, 1.5f, 3f, 2f, 2f, 1.5f, 2f, 1.5f, 1.5f, 2f};
            table.setWidths(columnWidths);

            // Headers
            String[] headers = {"S.No", "Date", "Time", "Description", "Invoice#", 
                              "Total Bill", "Discount", "Net Amount", "Payment", "Return", "Balance"};
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows
            for (Object[] row : ledgerData) {
                for (int i = 0; i < row.length; i++) {
                    String cellValue = "";
                    if (row[i] != null) {
                        if (i >= 5 && i <= 10) { // Numeric columns (amounts)
                            cellValue = String.format("%.2f", (Double) row[i]);
                        } else {
                            cellValue = row[i].toString();
                        }
                    }
                    
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, regularFont));
                    cell.setPadding(3);
                    
                    // Align numeric columns to right
                    if (i >= 5 && i <= 10) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else if (i == 0) { // Serial number center
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    
                    table.addCell(cell);
                }
            }

            document.add(table);

            // Summary section
            document.add(new Paragraph("\n"));
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(80);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Summary headers
            addSummaryCell(summaryTable, "Total Sale", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Payment", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Return", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Current Balance", headerFont, BaseColor.LIGHT_GRAY);

            // Summary values
            addSummaryCell(summaryTable, String.format("%.2f", totalSale), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalPayment), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalReturn), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", currentBalance), regularFont, 
                          currentBalance > 0 ? BaseColor.RED : currentBalance < 0 ? BaseColor.GREEN : BaseColor.WHITE);

            document.add(summaryTable);

            document.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate customer ledger PDF: " + e.getMessage());
        }
    }

    public static void generateSupplierLedgerPDF(String supplierName, List<Object[]> ledgerData, 
            double totalPurchase, double totalPayment, double totalReturn, double currentBalance, String filename) {
        try {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 50); // Landscape orientation
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

            // Company Header
            Paragraph title = new Paragraph("Supplier Ledger Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Supplier Name
            Paragraph supplierInfo = new Paragraph("Supplier: " + supplierName, headerFont);
            supplierInfo.setAlignment(Element.ALIGN_LEFT);
            supplierInfo.setSpacingAfter(10);
            document.add(supplierInfo);

            // Print Date
            String printDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph dateInfo = new Paragraph("Generated on: " + printDate, regularFont);
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingAfter(15);
            document.add(dateInfo);

            // Create table with 11 columns
            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            
            // Set relative column widths
            float[] columnWidths = {1f, 2f, 1.5f, 3f, 2f, 2f, 1.5f, 2f, 1.5f, 1.5f, 2f};
            table.setWidths(columnWidths);

            // Headers
            String[] headers = {"S.No", "Date", "Time", "Description", "Invoice#", 
                              "Total Bill", "Discount", "Net Amount", "Payment", "Return", "Balance"};
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows
            for (Object[] row : ledgerData) {
                for (int i = 0; i < row.length; i++) {
                    String cellValue = "";
                    if (row[i] != null) {
                        if (i >= 5 && i <= 10) { // Numeric columns (amounts)
                            cellValue = String.format("%.2f", (Double) row[i]);
                        } else {
                            cellValue = row[i].toString();
                        }
                    }
                    
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, regularFont));
                    cell.setPadding(3);
                    
                    // Align numeric columns to right
                    if (i >= 5 && i <= 10) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else if (i == 0) { // Serial number center
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    
                    table.addCell(cell);
                }
            }

            document.add(table);

            // Summary section
            document.add(new Paragraph("\n"));
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(80);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Summary headers
            addSummaryCell(summaryTable, "Total Purchase", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Payment", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Return", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Current Balance", headerFont, BaseColor.LIGHT_GRAY);

            // Summary values
            addSummaryCell(summaryTable, String.format("%.2f", totalPurchase), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalPayment), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalReturn), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", currentBalance), regularFont, 
                          currentBalance > 0 ? BaseColor.RED : currentBalance < 0 ? BaseColor.GREEN : BaseColor.WHITE);

            document.add(summaryTable);

            document.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate supplier ledger PDF: " + e.getMessage());
        }
    }

    private static void addSummaryCell(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }
}
