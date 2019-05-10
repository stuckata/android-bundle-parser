package com.stoyankirkov;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AndroidBundleParser {

    public static void main(String[] args) {
        File dir = new File("files");

        if (dir.exists() && dir.isDirectory() && dir.listFiles() != null) {
            File[] files = dir.listFiles();
//            List<File> files = Arrays.stream(dir.listFiles())
//                    .filter(f -> f.getName().endsWith(".xml"))
//                    .collect(Collectors.toList());

            for (File file : files) {
                if (file.getName().endsWith(".xml")) {
                    parseXml(file);
                } else if (file.getName().endsWith(".xlsx")) {
                    parseXlsx(file);
                }
            }
        }
    }

    private static void parseXmlRows(Document doc, String fileName) {
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
        String newFileName = getNewFileName(fileName);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(newFileName);
        int rowNum = 0;
        createXlsxRow(sheet, rowNum++, "Key", "Value");

        for (ImmutablePair<String, String> pair : pairs) {
            createXlsxRow(sheet, rowNum++, pair.getKey(), pair.getValue());
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

    private static String getNewFileName(String fileName) {
        return fileName.substring(0, fileName.indexOf("."));
    }

    private static void createXlsxRow(Sheet sheet, int rowNum, String key, String value) {
        Row header = sheet.createRow(rowNum);
        Cell cell = header.createCell(0);
        cell.setCellValue(key);

        cell = header.createCell(1);
        cell.setCellValue(value);
    }


    private static void parseXml(File xmlFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            Document doc = builder.parse(xmlFile);
            parseXmlRows(doc, xmlFile.getName());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseXlsx(File file) {
        try (Workbook workbook = new XSSFWorkbook(file)) {
            parseXlsxRows(workbook, file.getName());
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseXlsxRows(Workbook workbook, String fileName) {
        if(workbook == null) {
            return;
        }
        Sheet sheet = workbook.getSheetAt(0);
        List<ImmutablePair<String, String>> pairs = new ArrayList<>();

        for (Row row : sheet) {
            if(row.getRowNum() == 0) {
                continue;
            }
            pairs.add(new ImmutablePair<>(
                    row.getCell(0).getStringCellValue(),
                    row.getCell(1).getStringCellValue()));
        }
        writeToXml(pairs, fileName);
    }

    private static void writeToXml(List<ImmutablePair<String, String>> pairs, String fileName) {
        if(pairs.size() == 0) {
            return;
        }
        String newFileName = getNewFileName(fileName);
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            // root element
            Element root = document.createElement("resources");
            document.appendChild(root);

            for (ImmutablePair<String, String> pair : pairs) {
                // string element
                Element str = document.createElement("string");
                root.appendChild(str);
                // set an attribute to string element
                Attr attr = document.createAttribute("name");
                attr.setValue(pair.getKey());
                str.setAttributeNode(attr);
                // append text to string element
                str.appendChild(document.createTextNode(pair.getValue()));
            }
            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("files/" + newFileName + ".xml"));
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
