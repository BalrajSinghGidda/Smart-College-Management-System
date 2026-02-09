package com.college.management;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportGenerator {

    public static void exportStudentsCSV(String filePath) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM users WHERE role = 'STUDENT'");
             CSVPrinter printer = new CSVPrinter(new FileWriter(filePath), CSVFormat.DEFAULT.withHeader("ID", "Username", "Role"))) {
            while (rs.next()) {
                printer.printRecord(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
            }
        }
    }

    public static void exportStudentsPDF(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Student Report").setFontSize(20).setBold());

        float[] columnWidths = {50f, 200f, 100f};
        Table table = new Table(columnWidths);

        table.addCell("ID");
        table.addCell("Username");
        table.addCell("Role");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM users WHERE role = 'STUDENT'")) {
            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("id")));
                table.addCell(rs.getString("username"));
                table.addCell(rs.getString("role"));
            }
        }

        document.add(table);
        document.close();
    }
}
