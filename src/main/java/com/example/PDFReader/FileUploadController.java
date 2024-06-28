package com.example.PDFReader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final PDFService pdfService;

    public FileUploadController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping
    public ResponseEntity<byte[]> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            Map<String, Object> extractedInfo = pdfService.extractInfoFromPDF(file);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Invoice Data");

            createRow(sheet, 0, "Invoice Number", (String) extractedInfo.get("Invoice Number"));
            createRow(sheet, 1, "Invoice Date", (String) extractedInfo.get("Invoice Date"));
            createRow(sheet, 2, "Total Amount", (String) extractedInfo.get("Total Amount"));

            Row headerRow = sheet.createRow(3);
            headerRow.createCell(0).setCellValue("Item");
            headerRow.createCell(1).setCellValue("Quantity");
            headerRow.createCell(2).setCellValue("Price");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> itemDetails = (List<Map<String, String>>) extractedInfo.get("Item Details");
            int rowNum = 4;
            for (Map<String, String> itemDetail : itemDetails) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(itemDetail.get("Item"));
                row.createCell(1).setCellValue(itemDetail.get("Quantity"));
                row.createCell(2).setCellValue(itemDetail.get("Price"));
            }

            workbook.write(byteArrayOutputStream);
            workbook.close();

            byte[] excelContent = byteArrayOutputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "invoice_data.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelContent);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    private void createRow(Sheet sheet, int rowNum, String cell1Value, String cell2Value) {
        Row row = sheet.createRow(rowNum);
        Cell cell1 = row.createCell(0);
        Cell cell2 = row.createCell(1);
        cell1.setCellValue(cell1Value);
        cell2.setCellValue(cell2Value);
    }
}



