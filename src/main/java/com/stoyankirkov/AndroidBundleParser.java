package com.stoyankirkov;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndroidBundleParser {

    public static void main(String[] args) {
        File dir = new File(AndroidBundleParser.class.getResource("/input").getFile());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File xmlFile : files) {
                Document doc =  parseXml(xmlFile);
                parseRows(doc);
            }
        }
    }

    private static void parseRows(Document doc) {
        if(doc == null) {
            return;
        }
        List<ImmutablePair<String, String>> pairs = new ArrayList<>();

        NodeList children = doc.getElementsByTagName("resources").item(0).getChildNodes();
        for (int i = 0; i<children.getLength(); i++) {
            Node childNode = children.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element el = (Element) childNode;
                pairs.add(new ImmutablePair<>(el.getAttribute("name"), el.getTextContent()));
            }
        }
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
