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
    public static void generateCustomerLedgerPDF(String customerName, List<Object[]> ledgerData,
            double totalSale, double totalDiscount, double totalPayment, double totalReturn, double currentBalance, String filename) {
        try {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 50); // Landscape orientation
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);

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

            // Add data rows - correctly map customer ledger data
            for (Object[] row : ledgerData) {
                // Customer ledger data structure: [0]=S.No, [1]=Date, [2]=Time, [3]=Description, [4]=Invoice#, [5]=Total Bill, [6]=Discount, [7]=Other Discount, [8]=Net Amount, [9]=Payment, [10]=Return, [11]=Balance
                // Table columns: S.No, Date, Time, Invoice#, Total Bill, Discount, Other Discount, Net Amount, Payment, Return, Balance, Description
                
                // S.No
                String sNo = (row.length > 0 && row[0] != null) ? row[0].toString() : "";
                PdfPCell sNoCell = new PdfPCell(new Phrase(sNo, regularFont));
                sNoCell.setPadding(3);
                sNoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(sNoCell);
                
                // Date
                String date = (row.length > 1 && row[1] != null) ? row[1].toString() : "";
                PdfPCell dateCell = new PdfPCell(new Phrase(date, regularFont));
                dateCell.setPadding(3);
                dateCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(dateCell);
                
                // Time
                String time = (row.length > 2 && row[2] != null) ? row[2].toString() : "";
                PdfPCell timeCell = new PdfPCell(new Phrase(time, regularFont));
                timeCell.setPadding(3);
                timeCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(timeCell);
                
                // Invoice#
                String invoice = (row.length > 4 && row[4] != null) ? row[4].toString() : "";
                PdfPCell invoiceCell = new PdfPCell(new Phrase(invoice, regularFont));
                invoiceCell.setPadding(3);
                invoiceCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(invoiceCell);
                
                // Total Bill
                String totalBill = (row.length > 5 && row[5] != null) ? String.format("%.2f", (Double) row[5]) : "0.00";
                PdfPCell totalBillCell = new PdfPCell(new Phrase(totalBill, regularFont));
                totalBillCell.setPadding(3);
                totalBillCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(totalBillCell);
                
                // Discount
                String discount = (row.length > 6 && row[6] != null) ? String.format("%.2f", (Double) row[6]) : "0.00";
                PdfPCell discountCell = new PdfPCell(new Phrase(discount, regularFont));
                discountCell.setPadding(3);
                discountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(discountCell);
                
                // Other Discount
                String otherDiscount = (row.length > 7 && row[7] != null) ? String.format("%.2f", (Double) row[7]) : "0.00";
                PdfPCell otherDiscountCell = new PdfPCell(new Phrase(otherDiscount, regularFont));
                otherDiscountCell.setPadding(3);
                otherDiscountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(otherDiscountCell);
                
                // Net Amount
                String netAmount = (row.length > 8 && row[8] != null) ? String.format("%.2f", (Double) row[8]) : "0.00";
                PdfPCell netAmountCell = new PdfPCell(new Phrase(netAmount, regularFont));
                netAmountCell.setPadding(3);
                netAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(netAmountCell);
                
                // Payment
                String payment = (row.length > 9 && row[9] != null) ? String.format("%.2f", (Double) row[9]) : "0.00";
                PdfPCell paymentCell = new PdfPCell(new Phrase(payment, regularFont));
                paymentCell.setPadding(3);
                paymentCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(paymentCell);
                
                // Return
                String returnAmt = (row.length > 10 && row[10] != null) ? String.format("%.2f", (Double) row[10]) : "0.00";
                PdfPCell returnCell = new PdfPCell(new Phrase(returnAmt, regularFont));
                returnCell.setPadding(3);
                returnCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(returnCell);
                
                // Balance
                String balance = (row.length > 11 && row[11] != null) ? String.format("%.2f", (Double) row[11]) : "0.00";
                PdfPCell balanceCell = new PdfPCell(new Phrase(balance, regularFont));
                balanceCell.setPadding(3);
                balanceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(balanceCell);

                // Description with invoice details
                StringBuilder finalDesc = new StringBuilder();
                finalDesc.append((row.length > 3 && row[3] != null) ? row[3].toString() : "");

                if (invoice != null && !invoice.trim().isEmpty() && !invoice.equals("N/A")) {
                    finalDesc.append(getCustomerInvoiceItems(invoice));
                }

                PdfPCell descCell = new PdfPCell(new Phrase(finalDesc.toString(), regularFont));
                descCell.setPadding(3);
                descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(descCell);
            }

            document.add(table);

            // Summary section
            document.add(new Paragraph("\n"));
            PdfPTable summaryTable = new PdfPTable(5);
            summaryTable.setWidthPercentage(100);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Summary headers
            addSummaryCell(summaryTable, "Total Sale", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Discount", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Payment", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Return", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Current Balance", headerFont, BaseColor.LIGHT_GRAY);

            // Summary values
            addSummaryCell(summaryTable, String.format("%.2f", totalSale), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalDiscount), regularFont, BaseColor.WHITE);
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
                double totalPrice = quantity * unitPrice;
                double discountAmount = rs.getDouble("invoice_discount");
                detailedDesc.append(String.format("• %s\n  Qty: %.0f | Unit Price: %.2f | Total: %.2f | Discount: %.2f\n",
                    rs.getString("item_desc"),
                    quantity,
                    unitPrice,
                    totalPrice,
                    discountAmount));
            }
            rs.close();
            stmt.close();
            return detailedDesc.toString();
        } catch (Exception e) {
            return "\nError loading items: " + e.getMessage();
        }
    }

    public static void generateSupplierLedgerPDF(String supplierName, List<Object[]> ledgerData, 
            double totalPurchase, double totalDiscount, double totalPayment, double totalReturn, double currentBalance, String filename) {
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
                // S.No, Date, Time, Invoice#, Total Bill, Discount, Other Discount, Net Amount, Payment, Return, Balance
                // Expected order: 0=S.No, 1=Date, 2=Time, 3=Invoice#, 4=Total Bill, 5=Discount, 6=Other Discount, 7=Net Amount, 8=Payment, 9=Return, 10=Balance
                
                // S.No
                addDataCell(table, (row.length > 0 && row[0] != null) ? row[0].toString() : "", regularFont, Element.ALIGN_CENTER);
                
                // Date
                addDataCell(table, (row.length > 1 && row[1] != null) ? row[1].toString() : "", regularFont, Element.ALIGN_LEFT);
                
                // Time
                addDataCell(table, (row.length > 2 && row[2] != null) ? row[2].toString() : "", regularFont, Element.ALIGN_LEFT);
                
                // Invoice#
                String invoice = (row.length > 4 && row[4] != null) ? row[4].toString() : "";
                addDataCell(table, invoice, regularFont, Element.ALIGN_LEFT);
                
                // Total Bill
                String totalBill = (row.length > 5 && row[5] != null) ? String.format("%.2f", (Double) row[5]) : "0.00";
                addDataCell(table, totalBill, regularFont, Element.ALIGN_RIGHT);
                
                // Discount
                String discount = (row.length > 6 && row[6] != null) ? String.format("%.2f", (Double) row[6]) : "0.00";
                addDataCell(table, discount, regularFont, Element.ALIGN_RIGHT);
                
                // Other Discount - skip index 6, use empty for supplier ledger
                addDataCell(table, "0.00", regularFont, Element.ALIGN_RIGHT);
                
                // Net Amount
                String netAmount = (row.length > 7 && row[7] != null) ? String.format("%.2f", (Double) row[7]) : "0.00";
                addDataCell(table, netAmount, regularFont, Element.ALIGN_RIGHT);
                
                // Payment
                String payment = (row.length > 8 && row[8] != null) ? String.format("%.2f", (Double) row[8]) : "0.00";
                addDataCell(table, payment, regularFont, Element.ALIGN_RIGHT);
                
                // Return
                String returnAmt = (row.length > 9 && row[9] != null) ? String.format("%.2f", (Double) row[9]) : "0.00";
                addDataCell(table, returnAmt, regularFont, Element.ALIGN_RIGHT);
                
                // Balance
                String balance = (row.length > 10 && row[10] != null) ? String.format("%.2f", (Double) row[10]) : "0.00";
                addDataCell(table, balance, regularFont, Element.ALIGN_RIGHT);

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
            PdfPTable summaryTable = new PdfPTable(5);
            summaryTable.setWidthPercentage(100);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Summary headers
            addSummaryCell(summaryTable, "Total Purchase", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Discount", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Payment", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Return", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Current Balance", headerFont, BaseColor.LIGHT_GRAY);

            // Summary values
            addSummaryCell(summaryTable, String.format("%.2f", totalPurchase), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalDiscount), regularFont, BaseColor.WHITE);
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
    
    private static void addDataCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(3);
        table.addCell(cell);
    }
}
