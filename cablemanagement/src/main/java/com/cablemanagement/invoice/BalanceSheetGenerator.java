
package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.cablemanagement.config;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Balance Sheet PDF Generator using iText library
 */
public class BalanceSheetGenerator {
    
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    
    /**
     * Generate Balance Sheet PDF
     * @param filename The output filename for the PDF
     * @return true if generation was successful
     */
    public static boolean generateBalanceSheetPDF(String filename) {
        try {
            // Get balance sheet data from database
            Object[] balanceData = config.database.getBalanceSheetData();
            
            // Defensive programming - check for null data
            if (balanceData == null || balanceData.length < 8) {
                System.err.println("Error: Invalid balance sheet data returned from database");
                return false;
            }
            
            // Safely extract data with null checks
            double totalBankBalance = (balanceData[0] != null) ? (Double) balanceData[0] : 0.0;
            double customersOweUs = (balanceData[1] != null) ? (Double) balanceData[1] : 0.0;
            double weOweCustomers = (balanceData[2] != null) ? (Double) balanceData[2] : 0.0;
            double suppliersOweUs = (balanceData[3] != null) ? (Double) balanceData[3] : 0.0;
            double weOweSuppliers = (balanceData[4] != null) ? (Double) balanceData[4] : 0.0;
            double totalReceivables = (balanceData[5] != null) ? (Double) balanceData[5] : 0.0;
            double totalPayables = (balanceData[6] != null) ? (Double) balanceData[6] : 0.0;
            double netWorth = (balanceData[7] != null) ? (Double) balanceData[7] : 0.0;
            
            // Create document
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            
            // Add footer event
            writer.setPageEvent(new FooterEvent());
            
            document.open();
            
            // Add title
            Paragraph title = new Paragraph("BALANCE SHEET", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);
            
            // Add company name
            Paragraph companyName = new Paragraph("Cable Management System", HEADER_FONT);
            companyName.setAlignment(Element.ALIGN_CENTER);
            companyName.setSpacingAfter(5);
            document.add(companyName);
            
            // Add date
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            Paragraph asOfDate = new Paragraph("As of " + currentDate, NORMAL_FONT);
            asOfDate.setAlignment(Element.ALIGN_CENTER);
            asOfDate.setSpacingAfter(20);
            document.add(asOfDate);
            
            // Create main table with two columns (label and amount)
            PdfPTable mainTable = new PdfPTable(2);
            mainTable.setWidthPercentage(100);
            mainTable.setWidths(new float[]{70, 30});
            
            // ASSETS SECTION
            addSectionHeader(mainTable, "ASSETS");
            
            // Current Assets
            addBalanceItem(mainTable, "Current Assets:", "", true);
            addBalanceItem(mainTable, "  Cash in Hand (All Banks)", formatAmount(totalBankBalance), false);
            addBalanceItem(mainTable, "  Accounts Receivable - Customers", formatAmount(customersOweUs), false);
            addBalanceItem(mainTable, "  Accounts Receivable - Suppliers", formatAmount(suppliersOweUs), false);
            
            // Total Assets
            addEmptyRow(mainTable);
            addBalanceItem(mainTable, "TOTAL ASSETS", formatAmount(totalBankBalance + totalReceivables), true);
            
            // Add some space
            addEmptyRow(mainTable);
            addEmptyRow(mainTable);
            
            // LIABILITIES SECTION
            addSectionHeader(mainTable, "LIABILITIES");
            
            // Current Liabilities
            addBalanceItem(mainTable, "Current Liabilities:", "", true);
            addBalanceItem(mainTable, "  Accounts Payable - Customers", formatAmount(weOweCustomers), false);
            addBalanceItem(mainTable, "  Accounts Payable - Suppliers", formatAmount(weOweSuppliers), false);
            
            // Total Liabilities
            addEmptyRow(mainTable);
            addBalanceItem(mainTable, "TOTAL LIABILITIES", formatAmount(totalPayables), true);
            
            // Add some space
            addEmptyRow(mainTable);
            addEmptyRow(mainTable);
            
            // NET WORTH SECTION
            addSectionHeader(mainTable, "NET WORTH");
            addBalanceItem(mainTable, "NET WORTH (Assets - Liabilities)", formatAmount(netWorth), true);
            
            document.add(mainTable);
            
            // Add footer information
            document.add(new Paragraph("\n"));
            
            Paragraph footerInfo = new Paragraph();
            footerInfo.add(new Chunk("Generated on: ", NORMAL_FONT));
            footerInfo.add(new Chunk(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), BOLD_FONT));
            footerInfo.add(new Chunk("\nCable Management System - Financial Report", NORMAL_FONT));
            footerInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(footerInfo);
            
            // CODOC Footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
            document.add(new Paragraph("\n"));
            Paragraph codocLine1 = new Paragraph("⚡ Software Developed by CODOC", footerFont);
            codocLine1.setAlignment(Element.ALIGN_CENTER);
            Paragraph codocLine2 = new Paragraph("Reach out for your own custom solution", footerFont);
            codocLine2.setAlignment(Element.ALIGN_CENTER);
            Paragraph codocLine3 = new Paragraph("📧 info@codoc.it.com | 📞 0312-0854678", footerFont);
            codocLine3.setAlignment(Element.ALIGN_CENTER);
            document.add(codocLine1);
            document.add(codocLine2);
            document.add(codocLine3);
            
            document.close();
            System.out.println("Balance Sheet PDF generated successfully: " + filename);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error generating Balance Sheet PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a section header to the table
     */
    private static void addSectionHeader(PdfPTable table, String sectionName) {
        PdfPCell headerCell = new PdfPCell(new Phrase(sectionName, SECTION_FONT));
        headerCell.setColspan(2);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPaddingTop(10);
        headerCell.setPaddingBottom(5);
        headerCell.setBackgroundColor(new BaseColor(240, 240, 240));
        table.addCell(headerCell);
    }
    
    /**
     * Add a balance sheet item to the table
     */
    private static void addBalanceItem(PdfPTable table, String label, String amount, boolean isBold) {
        Font labelFont = isBold ? BOLD_FONT : NORMAL_FONT;
        Font amountFont = isBold ? BOLD_FONT : NORMAL_FONT;
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(3);
        labelCell.setPaddingBottom(3);
        table.addCell(labelCell);
        
        PdfPCell amountCell = new PdfPCell(new Phrase(amount, amountFont));
        amountCell.setBorder(Rectangle.NO_BORDER);
        amountCell.setPaddingTop(3);
        amountCell.setPaddingBottom(3);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amountCell);
    }
    
    /**
     * Add an empty row for spacing
     */
    private static void addEmptyRow(PdfPTable table) {
        PdfPCell emptyCell = new PdfPCell(new Phrase(" ", NORMAL_FONT));
        emptyCell.setColspan(2);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setFixedHeight(8);
        table.addCell(emptyCell);
    }
    
    /**
     * Format amount as currency string
     */
    private static String formatAmount(double amount) {
        if (amount == 0) {
            return "Rs. 0.00";
        }
        return String.format("Rs. %,.2f", amount);
    }
    
    /**
     * Generate and open Balance Sheet for preview/printing
     * @param filename The temporary filename for the PDF
     * @return true if generation and opening were successful
     */
    public static boolean generateAndPreviewBalanceSheet(String filename) {
        try {
            // Generate the PDF
            if (!generateBalanceSheetPDF(filename)) {
                return false;
            }
            
            // Use the same preview logic as invoices
            return PrintManager.openPDFForPreview(filename, "Balance Sheet");
            
        } catch (Exception e) {
            System.err.println("Error generating and previewing balance sheet: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
