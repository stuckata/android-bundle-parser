package com.stoyankirkov;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AndroidBundleParser {

    public static void main(String[] args) {
        File dir = new File("files");
        if (dir.exists() && dir.isDirectory() && dir.listFiles() != null) {
            List<File> files = Arrays.stream(dir.listFiles())
                    .filter(f -> f.getName().endsWith(".xml"))
                    .collect(Collectors.toList());

            for (File xmlFile : files) {
                Document doc = parseXml(xmlFile);
                parseRows(doc, xmlFile.getName());
            }
        }
    }

    private static void parseRows(Document doc, String fileName) {
        if (doc == null) {
            return;
        }
        List<ImmutablePair<String, String>> pairs = new ArrayList<>();

        NodeList children = doc.getElementsByTagName("resources").item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) childNode;
                pairs.add(new ImmutablePair<>(el.getAttribute("name"), el.getTextContent()));
            }
        }
        writeToExcel(pairs, fileName);
    }

    private static void writeToExcel(List<ImmutablePair<String, String>> pairs, String fileName) {
        if (pairs.size() == 0) {
            return;
        }
        String newFileName = fileName.substring(0, fileName.indexOf("."));
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(newFileName);
        int rowNum = 0;
        createRow(sheet, rowNum++, "Key", "Value");

        for (ImmutablePair<String, String> pair : pairs) {
            createRow(sheet, rowNum++, pair.getKey(), pair.getValue());
        }

        // Write the output to a file
        try (OutputStream fileOut = new FileOutputStream("files/" + newFileName + ".xlsx")) {
            workbook.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createRow(Sheet sheet, int rowNum, String key, String value) {
        Row header = sheet.createRow(rowNum);
        Cell cell = header.createCell(0);
        cell.setCellValue(key);

        cell = header.createCell(1);
        cell.setCellValue(value);
    }


    private static Document parseXml(File xmlFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            Document doc = builder.parse(xmlFile);
            return doc;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
