package org.example.file;
import org.jdom2.Element;
import java.io.*;

import java.util.ArrayList;

import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
//salam


public class FileWork {
// the two first methods are for your use. The others are the methods that is used in these two methods
    //input: first xml filepath(xml1), second xml filepath(xml2), and the filepath that excel is gonna be(where we want)
    //biz foldere girende orda yuxarida "C:\\Users\\HP\\Desktop" falan olur, fayl 
    // olan papkaya gelirik, sonra ardina faylinadi.extension(bizde bele olacaq: example.xml 
    //meselen "C:\\Users\\HP\\Desktop\\task\\notwellformed\\text.xml" siz iki \ qoyun (\\).

    

    




    public static boolean fileExists(String path){
        File file = new File(path);
        if(file.exists()) {
            System.out.println("File exists");
            return true;
        } else {
            System.out.println("File does not exist");
            return false;
        }
    }





    public static boolean fileWellFormCheck(String path){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            InputSource source = new InputSource(path);

            builder.parse(source);

            System.out.println("XML file is well-formed");
            return true;

        } catch (Exception e) {
            System.out.println("XML file is not well-formed: " + e.getMessage());

            System.out.println("it is recommended to compare them by txt format, use compareXMLWithTxt() method");
            return false;
        }
    }






    public static void compare(String before, String after, String output){
        if(!(fileExists(before) && fileExists(after))) return;
        if (!(fileWellFormCheck(before)&& fileWellFormCheck(after))) return;
        try {

            // Load the XML files
            File file1 = new File(before);
            File file2 = new File(after);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setCoalescing(true);
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc1 = dBuilder.parse(file1);
            Document doc2 = dBuilder.parse(file2);
            doc1.getDocumentElement().normalize();
            doc2.getDocumentElement().normalize();

            List<String> columnNames=new ArrayList<>();
            List<List<String>> data1=new ArrayList<List<String>>();
            List<List<String>> data2=new ArrayList<List<String>>();
            StringBuilder builder=new StringBuilder("");
            // Compare the XML files
            Diff diff = DiffBuilder.compare(Input.fromDocument(doc1))
                    .withTest(Input.fromDocument(doc2))
                    .ignoreWhitespace()
                    .ignoreComments().build();
            List<Difference> diffs= (List<Difference>) diff.getDifferences();
            List<String[]> differences = compareNodes(doc1.getDocumentElement(), doc2.getDocumentElement(), builder, columnNames, data1, data2);
//            for(String name:columnNames){
//                System.out.println(name);
//            }
            data1=transpose(data1);
            data2=transpose(data2);

            // Write the differences to an Excel file
            XSSFWorkbook workbook = new XSSFWorkbook();


            XSSFSheet sheetBefore = workbook.createSheet("BeforeCode");
            XSSFSheet sheetAfter = workbook.createSheet("AfterCode");
            XSSFSheet sheet = workbook.createSheet("Differences");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);

            writeExcel(columnNames, data1, sheetBefore);
            writeExcel(columnNames, data2, sheetAfter);

            int index=0;
            int policyCount=0;
            int rowNumber = 0;
            Row headerRow = sheet.createRow(rowNumber++);
            headerRow.createCell(0).setCellValue("Application ID");
            headerRow.createCell(1).setCellValue("XPath");
            headerRow.createCell(2).setCellValue("Data in First XML");
            headerRow.createCell(3).setCellValue("Data in Second XML");
            headerRow.createCell(4).setCellValue("Description");
//            headerRow.createCell(4).setCellValue("PolicyMessage");
            for (String[] difference : differences) {
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0).setCellValue(difference[0]);

                String xpath=diffs.get(index).getComparison().getControlDetails().getXPath();
                row.createCell(1).setCellValue(xpath);
                try {
                    row.createCell(2).setCellValue(Integer.parseInt(difference[1]));
                    row.createCell(3).setCellValue(Integer.parseInt(difference[2]));
                }catch(Exception ex){
                    row.createCell(2).setCellValue((difference[1]));
                    row.createCell(3).setCellValue((difference[2]));
                }

                row.createCell(4).setCellValue(diffs.get(index).getComparison().toString());
                if(xpath.contains("PolicyMessage")){  //.....PolicyMessage[3]
//                    int policy =xpath.indexOf("PolicyMessage");
//                    policy+="olicyMessage[".length();
//                    System.out.println(xpath.charAt(policy+1));
//                    row.createCell(4).setCellValue(Character.getNumericValue(xpath.charAt(policy+1)));
                    policyCount++;
                }
                index++;
            }
            rowNumber+=2;




// Loop through the cells in the row and apply the style

            Row row = sheet.createRow(rowNumber++);
            row.createCell(1).setCellValue("Total difference");
            row.createCell(2).setCellValue(diffs.size());

            row = sheet.createRow(rowNumber);
            row.createCell(1).setCellValue("Total policy message");
            row.createCell(2).setCellValue(policyCount);




            // Save the Excel file
            FileOutputStream outputStream = new FileOutputStream(output);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            System.out.println("Differences saved to differences.xlsx.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> compareNodes(Node node1, Node node2, StringBuilder id, List<String> columnNames, List<List<String>> list1, List<List<String>> list2) {
        List<String[]> differences = new ArrayList<>();
        if (!node1.getNodeName().equals(node2.getNodeName())) {
            return differences;
        }
        if (node1.getNodeName().equalsIgnoreCase("applicationID")) {
            id.replace(0, id.length(), node1.getTextContent());
        }
        if (node1.getNodeType() == Node.TEXT_NODE && node2.getNodeType() == Node.TEXT_NODE) {
            if (!node1.getNodeValue().trim().equals("")) {

            String data1 = node1.getNodeValue().trim();
            String data2 = node2.getNodeValue().trim();
            Node parent = node1.getParentNode();
            while (parent != null && parent.getNodeType() != Node.ELEMENT_NODE) {
                parent = parent.getParentNode();

            }

            if (parent != null && parent.getFirstChild().getNodeType() == Node.TEXT_NODE) {

                String tagName = parent.getNodeName();
                System.out.println(tagName);
                if (columnNames.contains(tagName)) {
                    int index = columnNames.indexOf(tagName);
                    list1.get(index).add(data1);
                    list2.get(index).add(data2);
                } else {
                    columnNames.add(tagName);
                    int index = columnNames.indexOf(tagName);
                    list1.add(new ArrayList<>());
                    list2.add(new ArrayList<>());
                    list1.get(index).add(data1);
                    list2.get(index).add(data2);
                }
                if (!data1.equals(data2)) {
                    String[] difference = new String[3];
                    difference[0] = id.toString();// Assumes that the ID is an attribute of the parent node
                    difference[1] = data1;
                    difference[2] = data2;
                    differences.add(difference);
                }
            }
        }
        } else {
            NodeList children1 = node1.getChildNodes();
            NodeList children2 = node2.getChildNodes();
            for (int i = 0; i < children1.getLength() && i < children2.getLength(); i++) {
                Node child1 = children1.item(i);
                Node child2 = children2.item(i);
                differences.addAll(compareNodes(child1, child2, id, columnNames, list1, list2));
            }
        }
        return differences;
    }


    public static void writeExcel(List<String> columns, List<List<String>> data, Sheet sheet) {

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i));
            }

            // Create data rows
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data.get(i).size(); j++) {
                    Cell cell = row.createCell(j);
                    try {
                        cell.setCellValue(Integer.parseInt(data.get(i).get(j)));
                    }
                    catch(Exception ex){
                        cell.setCellValue(String.valueOf(data.get(i).get(j)));
                    }
                }
            }



    }
    public static <T> List<List<T>> transpose(List<List<T>> original) {
        int numRows = original.size();
        int numCols = original.stream().mapToInt(List::size).max().orElse(0);

        List<List<T>> transposed = new ArrayList<>(numCols);
        for (int col = 0; col < numCols; col++) {
            List<T> newRow = new ArrayList<>(numRows);
            for (int row = 0; row < numRows; row++) {
                List<T> curRow = original.get(row);
                T cellValue = (col < curRow.size()) ? curRow.get(col) : null;
                newRow.add(cellValue);
            }
            transposed.add(newRow);
        }

        return transposed;
    }

}






