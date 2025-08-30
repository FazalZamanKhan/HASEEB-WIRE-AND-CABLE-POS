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

public class SupplierLedgerPrintV2 {
    
    public static void generate(String supplierName, List<Object[]> ledgerData, double totalPurchase, double totalPayment, double totalReturn, double currentBalance, String filename) {
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

            // Create table with 11 columns to match supplier ledger structure
            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            // Set relative column widths 
            float[] columnWidths = {1f, 2f, 1.5f, 2f, 2f, 1.5f, 2f, 1.5f, 1.5f, 2f, 5f};
            table.setWidths(columnWidths);

            // Headers - exactly matching supplier ledger UI
            String[] headers = {"S.No", "Date", "Time", "Invoice#", "Total Bill", "Discount", "Net Amount", "Payment", "Return", "Balance", "Description"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows - using correct supplier ledger data structure
            for (Object[] row : ledgerData) {
                // Supplier ledger data: [0]=S.No, [1]=Date, [2]=Time, [3]=Description, [4]=Invoice#, [5]=Total Bill, [6]=Discount, [7]=Net Amount, [8]=Payment, [9]=Return Amount, [10]=Remaining/Balance
                
                // S.No
                addCell(table, row.length > 0 && row[0] != null ? row[0].toString() : "", regularFont, Element.ALIGN_CENTER);
                
                // Date
                addCell(table, row.length > 1 && row[1] != null ? row[1].toString() : "", regularFont, Element.ALIGN_LEFT);
                
                // Time
                addCell(table, row.length > 2 && row[2] != null ? row[2].toString() : "", regularFont, Element.ALIGN_LEFT);
                
                // Invoice#
                String invoiceNumber = row.length > 4 && row[4] != null ? row[4].toString() : "";
                addCell(table, invoiceNumber, regularFont, Element.ALIGN_LEFT);
                
                // Total Bill
                addCell(table, row.length > 5 && row[5] != null ? String.format("%.2f", (Double) row[5]) : "0.00", regularFont, Element.ALIGN_RIGHT);
                
                // Discount
                addCell(table, row.length > 6 && row[6] != null ? String.format("%.2f", (Double) row[6]) : "0.00", regularFont, Element.ALIGN_RIGHT);
                
                // Net Amount
                addCell(table, row.length > 7 && row[7] != null ? String.format("%.2f", (Double) row[7]) : "0.00", regularFont, Element.ALIGN_RIGHT);
                
                // Payment
                addCell(table, row.length > 8 && row[8] != null ? String.format("%.2f", (Double) row[8]) : "0.00", regularFont, Element.ALIGN_RIGHT);
                
                // Return Amount
                addCell(table, row.length > 9 && row[9] != null ? String.format("%.2f", (Double) row[9]) : "0.00", regularFont, Element.ALIGN_RIGHT);
                
                // Balance
                addCell(table, row.length > 10 && row[10] != null ? String.format("%.2f", (Double) row[10]) : "0.00", regularFont, Element.ALIGN_RIGHT);

                // Description with invoice details
                StringBuilder finalDesc = new StringBuilder();
                String description = row.length > 3 && row[3] != null ? row[3].toString() : "";
                finalDesc.append(description);

                if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                    // Check if this is a return invoice by looking at the description
                    if (description != null && description.contains("Purchase Return")) {
                        finalDesc.append(getSupplierReturnInvoiceItems(invoiceNumber));
                    } else {
                        finalDesc.append(getSupplierInvoiceItems(invoiceNumber));
                    }
                }

                addCell(table, finalDesc.toString(), regularFont, Element.ALIGN_LEFT);
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

    private static String getSupplierInvoiceItems(String invoiceNumber) {
        try {
            StringBuilder detailedDesc = new StringBuilder();
            String itemQuery = "SELECT rpi.quantity, rpi.unit_price, " +
                "(rpi.quantity * rpi.unit_price) as item_total, " +
                "rpin.paid_amount, rpin.total_amount, rpin.discount_amount as invoice_discount, " +
                "COALESCE(rs.item_name, 'Item') as item_desc " +
                "FROM Raw_Purchase_Invoice_Item rpi " +
                "LEFT JOIN Raw_Stock rs ON rpi.raw_stock_id = rs.stock_id " +
                "LEFT JOIN Raw_Purchase_Invoice rpin ON rpi.raw_purchase_invoice_id = rpin.raw_purchase_invoice_id " +
                "WHERE rpi.raw_purchase_invoice_id = (SELECT raw_purchase_invoice_id FROM Raw_Purchase_Invoice WHERE invoice_number = ?)";
            
            Connection conn = config.database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(itemQuery);
            stmt.setString(1, invoiceNumber);
            ResultSet rs = stmt.executeQuery();

            detailedDesc.append("\nItems:\n");
            double invoicePaidAmount = 0.0;
            double invoiceTotalAmount = 0.0;
            double invoiceDiscountAmount = 0.0;
            
            while (rs.next()) {
                if (invoicePaidAmount == 0.0) {
                    invoicePaidAmount = rs.getDouble("paid_amount");
                    invoiceTotalAmount = rs.getDouble("total_amount");
                    invoiceDiscountAmount = rs.getDouble("invoice_discount");
                }
                
                double itemTotal = rs.getDouble("item_total");
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("unit_price");
                double itemDiscount = (invoiceTotalAmount > 0) ? (itemTotal / invoiceTotalAmount) * invoiceDiscountAmount : 0.0;
                double itemNet = itemTotal - itemDiscount;
                
                detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total: %.2f | Discount: %.2f | Net: %.2f\n",
                    rs.getString("item_desc"),
                    quantity,
                    unitPrice,
                    itemTotal,
                    itemDiscount,
                    itemNet));
            }
            rs.close();
            stmt.close();
            return detailedDesc.toString();
        } catch (Exception e) {
            return "\nError loading items: " + e.getMessage();
        }
    }

    private static String getSupplierReturnInvoiceItems(String returnInvoiceNumber) {
        try {
            StringBuilder detailedDesc = new StringBuilder();
            String returnItemQuery = "SELECT rprii.quantity, rprii.unit_price, " +
                "(rprii.quantity * rprii.unit_price) as item_total, " +
                "rpri.total_return_amount, " +
                "COALESCE(rs.item_name, 'Item') as item_desc " +
                "FROM Raw_Purchase_Return_Invoice_Item rprii " +
                "LEFT JOIN Raw_Stock rs ON rprii.raw_stock_id = rs.stock_id " +
                "LEFT JOIN Raw_Purchase_Return_Invoice rpri ON rprii.raw_purchase_return_invoice_id = rpri.raw_purchase_return_invoice_id " +
                "WHERE rpri.return_invoice_number = ?";
            
            Connection conn = config.database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(returnItemQuery);
            stmt.setString(1, returnInvoiceNumber);
            ResultSet rs = stmt.executeQuery();

            detailedDesc.append("\nReturned Items:\n");
            double totalReturnAmount = 0.0;
            
            while (rs.next()) {
                if (totalReturnAmount == 0.0) {
                    totalReturnAmount = rs.getDouble("total_return_amount");
                }
                
                double itemTotal = rs.getDouble("item_total");
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("unit_price");
                
                detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Return Amount: %.2f\n",
                    rs.getString("item_desc"),
                    quantity,
                    unitPrice,
                    itemTotal));
            }
            detailedDesc.append(String.format("Total Return Amount: %.2f", totalReturnAmount));
            rs.close();
            stmt.close();
            return detailedDesc.toString();
        } catch (Exception e) {
            return "\nError loading return items: " + e.getMessage();
        }
    }

    private static void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private static void addSummaryCell(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }
}
