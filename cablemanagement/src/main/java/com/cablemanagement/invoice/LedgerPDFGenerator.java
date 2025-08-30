package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.cablemanagement.config;

public class LedgerPDFGenerator {
    
    private static String getCustomerInvoiceItems(String invoiceNumber) {
        try {
            StringBuilder detailedDesc = new StringBuilder();
            String itemQuery = "SELECT si.quantity, si.unit_price, si.discount_amount, " +
                "(si.quantity * si.unit_price) as total_price, " +
                "(si.quantity * si.unit_price - si.discount_amount) as net_price, " +
                "sv.discount_amount as invoice_discount, " +
                "sv.other_discount, " +  // Added other_discount
                "COALESCE(ps.product_name, 'Product') as item_desc " +
                "FROM Sales_Invoice_Item si " +
                "LEFT JOIN ProductionStock ps ON si.production_stock_id = ps.production_id " +
                "LEFT JOIN Sales_Invoice sv ON si.sales_invoice_id = sv.sales_invoice_id " +
                "WHERE si.sales_invoice_id = (SELECT sales_invoice_id FROM Sales_Invoice WHERE sales_invoice_number = ?)";
            
            Connection conn = config.database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(itemQuery);
            stmt.setString(1, invoiceNumber);
            ResultSet rs = stmt.executeQuery();

            detailedDesc.append("\nItems:\n");
            while (rs.next()) {
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("unit_price");
                double totalPrice = quantity * unitPrice;
                double discountAmount = rs.getDouble("discount_amount");
                double netPrice = totalPrice - discountAmount;

                detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total: %.2f | Item Disc: %.2f | Net: %.2f\n",
                    rs.getString("item_desc"),
                    quantity,
                    unitPrice,
                    totalPrice,
                    discountAmount,
                    netPrice));
            }
            rs.close();
            stmt.close();
            return detailedDesc.toString();
        } catch (Exception e) {
            return "\nError loading items: " + e.getMessage();
        }
    }

    private static String getSupplierInvoiceItems(String invoiceNumber) {
        try {
            StringBuilder detailedDesc = new StringBuilder();
            String itemQuery = "SELECT rpi.quantity, rpi.unit_price, " +
                "(rpi.quantity * rpi.unit_price) as total_price, " +
                "rpin.discount_amount as invoice_discount, " +
                "COALESCE(rs.item_name, 'Item') as item_desc " +
                "FROM Raw_Purchase_Invoice_Item rpi " +
                "LEFT JOIN Raw_Stock rs ON rpi.raw_stock_id = rs.stock_id " +
                "WHERE rpi.raw_purchase_invoice_id = (SELECT raw_purchase_invoice_id FROM Raw_Purchase_Invoice WHERE invoice_number = ?)";
            
            Connection conn = config.database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(itemQuery);
            stmt.setString(1, invoiceNumber);
            ResultSet rs = stmt.executeQuery();

            detailedDesc.append("\nItems:\n");
            while (rs.next()) {
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("unit_price");
                double totalPrice = rs.getDouble("total_price");

                detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total: %.2f\n",
                    rs.getString("item_desc"),
                    quantity,
                    unitPrice,
                    totalPrice));
            }
            rs.close();
            stmt.close();
            return detailedDesc.toString();
        } catch (Exception e) {
            return "\nError loading items: " + e.getMessage();
        }
    }

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

            // Create table with 12 columns (Description moved after Balance)
            PdfPTable table = new PdfPTable(12);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            // Set relative column widths (increase Description width)
            float[] columnWidths = {1f, 2f, 1.5f, 2f, 2f, 1.5f, 1.5f, 2f, 1.5f, 1.5f, 2f, 5f};
            table.setWidths(columnWidths);

            // Headers (Description last)
            String[] headers = {"S.No", "Date", "Time", "Invoice#", "Total Bill", "Discount", "Other Discount", "Net Amount", "Payment", "Return", "Balance", "Description"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows (Description last, show all details)
            for (Object[] row : ledgerData) {
                // Print columns 0-11, but Description (3) goes last
                for (int i = 0; i < row.length; i++) {
                    if (i == 3) continue; // Skip Description for now
                    String cellValue = "";
                    if (row[i] != null) {
                        if (i >= 5 && i <= 11) { // Updated to handle the new column
                            cellValue = String.format("%.2f", (Double) row[i]);
                        } else {
                            cellValue = row[i].toString();
                        }
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, regularFont));
                    cell.setPadding(3);
                    if (i >= 5 && i <= 11) { // Updated to handle the new column
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else if (i == 0) {
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(cell);
                }

                // Now add Description with all invoice details
                String invoiceNumber = (String) row[4]; // Invoice number is at index 4
                StringBuilder finalDesc = new StringBuilder();
                finalDesc.append(row[3] != null ? row[3].toString() : "");

                if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                    finalDesc.append(getCustomerInvoiceItems(invoiceNumber));
                }

                PdfPCell descCell = new PdfPCell(new Phrase(finalDesc.toString(), regularFont));
                descCell.setPadding(3);
                descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(descCell);
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

            // Create table with 12 columns (Description moved after Balance)
            PdfPTable table = new PdfPTable(12);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            // Set relative column widths (increase Description width)
            float[] columnWidths = {1f, 2f, 1.5f, 2f, 2f, 1.5f, 1.5f, 2f, 1.5f, 1.5f, 2f, 5f};
            table.setWidths(columnWidths);

            // Headers (Description last)
            String[] headers = {"S.No", "Date", "Time", "Invoice#", "Total Bill", "Discount", "Other Discount", "Net Amount", "Payment", "Return", "Balance", "Description"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows (Description last, show all details)
            for (Object[] row : ledgerData) {
                // Print columns 0-11, but Description (3) goes last
                for (int i = 0; i < row.length; i++) {
                    if (i == 3) continue; // Skip Description for now
                    String cellValue = "";
                    if (row[i] != null) {
                        if (i >= 5 && i <= 11) { // Updated to handle the new column
                            cellValue = String.format("%.2f", (Double) row[i]);
                        } else {
                            cellValue = row[i].toString();
                        }
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, regularFont));
                    cell.setPadding(3);
                    if (i >= 5 && i <= 11) { // Updated to handle the new column
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else if (i == 0) {
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(cell);
                }

                // Now add Description with all invoice details
                String invoiceNumber = (String) row[4]; // Invoice number is at index 4
                StringBuilder finalDesc = new StringBuilder();
                finalDesc.append(row[3] != null ? row[3].toString() : "");

                if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                    finalDesc.append(getSupplierInvoiceItems(invoiceNumber));
                }

                PdfPCell descCell = new PdfPCell(new Phrase(finalDesc.toString(), regularFont));
                descCell.setPadding(3);
                descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(descCell);
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
