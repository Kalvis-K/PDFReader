package com.example.PDFReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PDFService {

    public Map<String, Object> extractInfoFromPDF(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper pdfStripper = new PDFTextStripper();

        StringBuilder textBuilder = new StringBuilder();
        for (int page = 1; page <= document.getNumberOfPages(); page++) {
            pdfStripper.setStartPage(page);
            pdfStripper.setEndPage(page);
            textBuilder.append(pdfStripper.getText(document));
        }
        String text = textBuilder.toString();
        document.close();

        Map<String, Object> extractedInfo = new HashMap<>();
        extractedInfo.put("Invoice Number", extractInvoiceNumber(text));
        extractedInfo.put("Invoice Date", extractInvoiceDate(text));
        extractedInfo.put("Total Amount", extractTotalAmount(text));

        List<Map<String, String>> itemDetails = extractItems(text);
        extractedInfo.put("Item Details", itemDetails);

        return extractedInfo;
    }

    private String extractInvoiceNumber(String text) {
        String marker = "Pavadzīme Nr.:";
        int index = text.indexOf(marker);
        if (index != -1) {
            int start = index + marker.length();
            int end = text.indexOf('\n', start);
            return text.substring(start, end).trim();
        }
        return "Not found";
    }

    private String extractInvoiceDate(String text) {
        Pattern pattern = Pattern.compile("(\\d{4})\\. gada (\\d{1,2})\\. ([a-zA-Zāēīūčņļķģšž]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String year = matcher.group(1);
            String day = matcher.group(2);
            String month = matcher.group(3);

            return day + "." + translateMonth(month) + "." + year;
        }
        return "Not found";
    }

    private String translateMonth(String monthInLatvian) {
        switch (monthInLatvian.toLowerCase()) {
            case "janvāris":
                return "01";
            case "februāris":
                return "02";
            case "marts":
                return "03";
            case "aprīlis":
                return "04";
            case "maijs":
                return "05";
            case "jūnijs":
                return "06";
            case "jūlijs":
                return "07";
            case "augusts":
                return "08";
            case "septembris":
                return "09";
            case "oktobris":
                return "10";
            case "novembris":
                return "11";
            case "decembris":
                return "12";
            default:
                return "00";
        }
    }

    private String extractTotalAmount(String text) {
        Pattern pattern = Pattern.compile("\\s*USD\\s*([\\d,]+\\.\\d{2})Summa kopā");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Not found";
    }

    private List<Map<String, String>> extractItems(String text) {
        List<Map<String, String>> items = new ArrayList<>();

        Pattern pattern = Pattern.compile("^([^\\s]+).*?\\n(\\d+)\\s+([\\d,.]+)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            Map<String, String> item = new HashMap<>();
            item.put("Item", matcher.group(1).trim());
            item.put("Quantity", matcher.group(2));
            item.put("Price", matcher.group(3));

            items.add(item);
        }
        return items;
    }
}



