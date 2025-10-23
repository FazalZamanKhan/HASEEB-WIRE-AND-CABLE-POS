
package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SupplierLedgerPrint {
    public static void generate(String supplierName, List<Object[]> ledgerData, double totalPurchase, double totalDiscount, double totalPayment, double totalReturn, double currentBalance, String filename) {
        try {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);

            Paragraph title = new Paragraph("Supplier Ledger Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph supplierInfo = new Paragraph("Supplier: " + supplierName, headerFont);
            supplierInfo.setAlignment(Element.ALIGN_LEFT);
            supplierInfo.setSpacingAfter(10);
            document.add(supplierInfo);

            String printDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph dateInfo = new Paragraph("Generated on: " + printDate, regularFont);
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingAfter(15);
            document.add(dateInfo);

            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            float[] columnWidths = {1f, 2f, 1.5f, 2f, 2f, 1.5f, 2f, 1.5f, 1.5f, 2f, 5f};
            table.setWidths(columnWidths);

            String[] headers = {"S.No", "Date", "Time", "Invoice#", "Total Bill", "Discount", "Net Amount", "Payment", "Return", "Balance", "Description"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Object[] row : ledgerData) {
                int[] columnOrder = {0, 1, 2, 4, 5, 8, 9, 10, 11};
                for (int colIdx : columnOrder) {
                    String cellValue = "";
                    if (row.length > colIdx && row[colIdx] != null) {
                        if (colIdx >= 5 && colIdx <= 11) {
                            cellValue = String.format("%.2f", (Double) row[colIdx]);
                        } else {
                            cellValue = row[colIdx].toString();
                        }
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, regularFont));
                    cell.setPadding(3);
                    cell.setHorizontalAlignment(colIdx == 0 ? Element.ALIGN_CENTER : Element.ALIGN_RIGHT);
                    table.addCell(cell);
                }
                String invoiceNumber = (String) row[4];
                StringBuilder finalDesc = new StringBuilder();
                finalDesc.append(row[3] != null ? row[3].toString() : "");
                if (invoiceNumber != null && !invoiceNumber.trim().isEmpty() && !invoiceNumber.equals("N/A")) {
                    finalDesc.append("\nInvoice Details: " + invoiceNumber);
                }
                PdfPCell descCell = new PdfPCell(new Phrase(finalDesc.toString(), regularFont));
                descCell.setPadding(3);
                descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(descCell);
            }

            document.add(table);

            document.add(new Paragraph("\n"));
            PdfPTable summaryTable = new PdfPTable(5);
            summaryTable.setWidthPercentage(100);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            addSummaryCell(summaryTable, "Total Purchase", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Discount", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Payment", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Total Return", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, "Current Balance", headerFont, BaseColor.LIGHT_GRAY);
            addSummaryCell(summaryTable, String.format("%.2f", totalPurchase), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalDiscount), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalPayment), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", totalReturn), regularFont, BaseColor.WHITE);
            addSummaryCell(summaryTable, String.format("%.2f", currentBalance), regularFont, currentBalance > 0 ? BaseColor.RED : currentBalance < 0 ? BaseColor.GREEN : BaseColor.WHITE);
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
