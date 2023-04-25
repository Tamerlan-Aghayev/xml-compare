package org.example.file;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



public class FileWork {
    //this method checks whether the file exist or not. It gets xml file path as input
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
    //this method checks whether the file is in well-form. It gets xml file path as an input
    public static boolean fileWellFormCheck(String path)throws Exception{
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


    //this method checks two excel sheets, and highlights different cells  with yellow background, it gets excel file path as input

    public static void compareExcels(String filePath) throws Exception{

        FileInputStream file = new FileInputStream(new File(filePath));

        Workbook workbook = new XSSFWorkbook(file);


        // Get the sheets from workbook
        Sheet beforeSheet = workbook.getSheet("BeforeCode");
        Sheet afterSheet = workbook.getSheet("AfterCode");

        // Iterate through rows in both sheets and compare their cells
        Iterator<Row> beforeRowIterator = beforeSheet.iterator();
        Iterator<Row> afterRowIterator = afterSheet.iterator();
        while (beforeRowIterator.hasNext() && afterRowIterator.hasNext()) {
            Row beforeRow = beforeRowIterator.next();
            Row afterRow = afterRowIterator.next();
            Iterator<Cell> beforeCellIterator = beforeRow.iterator();
            Iterator<Cell> afterCellIterator = afterRow.iterator();
            while (beforeCellIterator.hasNext() && afterCellIterator.hasNext()) {
                Cell beforeCell = beforeCellIterator.next();
                Cell afterCell = afterCellIterator.next();
                if (!beforeCell.getCellType().equals(afterCell.getCellType())) {
                    highlightCell(beforeSheet, beforeCell);
                    highlightCell(afterSheet, afterCell);
                } else {
                    if (beforeCell.getCellType().equals(CellType.NUMERIC)) {
                        if (beforeCell.getNumericCellValue() != afterCell.getNumericCellValue()) {
                            highlightCell(beforeSheet, beforeCell);
                            highlightCell(afterSheet, afterCell);
                        }
                    } else if (beforeCell.getCellType().equals(CellType.STRING)) {
                        if (!beforeCell.getStringCellValue().equals(afterCell.getStringCellValue())) {
                            highlightCell(beforeSheet, beforeCell);
                            highlightCell(afterSheet, afterCell);
                        }
                    }
                }
            }
        }

        // Write the changes to the output Excel files
        FileOutputStream beforeOutput = new FileOutputStream(new File(filePath));
        workbook.write(beforeOutput);

        // Close all resources
        file.close();
        beforeOutput.close();
        workbook.close();
    }
    //this method is used in compareExcel method
    private static void highlightCell(Sheet sheet, Cell cell) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        cell.setCellStyle(style);
    }
    //this method adds difference sheet to workbook
    public static XSSFWorkbook getDifferenceShell( XSSFWorkbook differences) throws  Exception{

        DataFormatter formatter = new DataFormatter();


//        FileInputStream file1 = new FileInputStream(new File(file1Name));
//        FileInputStream file2 = new FileInputStream(new File(file2Name));

//        Workbook workbook1 = new XSSFWorkbook(file1);
//        Workbook workbook2 = new XSSFWorkbook(file2);

        Sheet sheet1 = differences.getSheet("BeforeCode");
        Sheet sheet2 = differences.getSheet("AfterCode");

//        Workbook differences = new XSSFWorkbook();
        Sheet differencesSheet = differences.createSheet("Difference");

        Row headerRow = differencesSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Row");
        headerRow.createCell(1).setCellValue("Name");
        headerRow.createCell(2).setCellValue("BeforeCode");
        headerRow.createCell(3).setCellValue("AfterCode");

        int numRows = sheet1.getLastRowNum();

        for (int i = 1; i <= numRows; i++) {
            Row row1 = sheet1.getRow(i);
            Row row2 = sheet2.getRow(i);

            for (int j = 0; j < row1.getLastCellNum(); j++) {
                Cell cell1 = row1.getCell(j);
                Cell cell2 = row2.getCell(j);

                String value1 = formatter.formatCellValue(cell1);
                String value2 = formatter.formatCellValue(cell2);

                if (!value1.equals(value2)) {
                    Row diffRow = differencesSheet.createRow(differencesSheet.getLastRowNum() + 1);
                    diffRow.createCell(0).setCellValue(i + 1);
                    diffRow.createCell(1).setCellValue(sheet1.getRow(0).getCell(j).getStringCellValue());
                    switch (cell1.getCellType()) {
                        case NUMERIC:
                            double numericValue1 = cell1.getNumericCellValue();
                            double numericValue2 = cell2.getNumericCellValue();
                            diffRow.createCell(2).setCellValue(numericValue1);
                            diffRow.createCell(3).setCellValue(numericValue2);

                            // process numeric value
                            break;

                        case BOOLEAN:
                            boolean booleanValue1 = cell1.getBooleanCellValue();
                            boolean booleanValue2 = cell2.getBooleanCellValue();
                            diffRow.createCell(2).setCellValue(booleanValue1);
                            diffRow.createCell(3).setCellValue(booleanValue2);

                            // process boolean value
                            break;
                        default:
                            String stringValue1 = cell1.getStringCellValue();
                            String stringValue2 = cell2.getStringCellValue();
                            diffRow.createCell(2).setCellValue(stringValue1);
                            diffRow.createCell(3).setCellValue(stringValue2);

                            break;

                }
            }
        }

//        FileOutputStream differencesFile = new FileOutputStream(new File(differencesName));
//        differences.write(differencesFile);
//        differencesFile.close();

//        workbook1.close();
//        workbook2.close();
//        differences.close();


    }
    return differences;
}
//this method will compare xmls by converting them to txt. It is alternative way to use if comparing by excel doesn't work.
    //It gets xml files' pathes, pathes that txt files going to be, and path that excel file going to be.
    public static void compareXMLWithTxt(String xml1,String xml2, String txt1, String txt2, String outputFile) throws Exception{


    // Load the contents of the input text files into lists of strings
    List<String> lines1 = loadFileContents(xml1);
    List<String> lines2 = loadFileContents(xml2);
    File output1 = new File(txt1);
    File output2 = new File(txt2);
    FileWriter writer1 = new FileWriter(output1);
    FileWriter writer2 = new FileWriter(output2);
    // Create a new Excel workbook and sheet to store the differences
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Differences");

    // Add column headers to the sheet
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Row");
    headerRow.createCell(1).setCellValue("Tagname");
    headerRow.createCell(2).setCellValue("BeforeCode");
    headerRow.createCell(3).setCellValue("AfterCode");

    // Compare the contents of the two input files and write any differences to the Excel sheet
    int rowNumber = 1; // Start at row 1 (since row 0 is for headers)
    for (int i = 0; i < lines1.size(); i++) {
        String line1 = lines1.get(i);
        String line2 = lines2.get(i);
        if (!line1.equals(line2)) {
            // The lines are different, so split them into tags and codes
            String[] tagAndCode1 = line1.split(">");
            String[] tagAndCode2 = line2.split(">");

            String tagname = tagAndCode1[0].split("<")[1].trim();
            String beforeCode = tagAndCode1[1].split("<")[0].trim();
            String afterCode = tagAndCode2[1].split("<")[0].trim();

            // Create a new row in the Excel sheet for this difference
            Row dataRow = sheet.createRow(rowNumber++);
            dataRow.createCell(0).setCellValue(i + 1);
            dataRow.createCell(1).setCellValue(tagname);
            dataRow.createCell(2).setCellValue(beforeCode);
            dataRow.createCell(3).setCellValue(afterCode);


            String lineAdd1=tagAndCode1[0].split("<")[0]+"<"+tagname+">"+"***"+beforeCode+"***"+"<"+tagAndCode1[1].split("<")[1];
            String lineAdd2=tagAndCode1[0].split("<")[0]+"<"+tagname+">"+"***"+afterCode+"***"+"<"+tagAndCode1[1].split("<")[1];
            writer1.write(lineAdd1+"\n");
            writer2.write(lineAdd2+"\n");
            continue;
        }
        writer1.write(line1+"\n");
        writer2.write(line2+"\n");
    }

    // Write the Excel workbook to a file
    FileOutputStream outputStream = new FileOutputStream(outputFile);
    workbook.write(outputStream);
    workbook.close();
    outputStream.close();
    writer2.close();
    writer1.close();

}
    //used in compareXMLWithTxt method. Input is txt file path
    private static List<String> loadFileContents(String fileName) throws Exception {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
}
//it converts xml to txt, if needed.
    public static void convertXMLToTxt(String xml, String txt) throws  Exception{
        try {
            // Open the input file
            File inputFile = new File(xml);
            FileInputStream inputStream = new FileInputStream(inputFile);

            // Create a reader for the input file
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Open the output file
            File outputFile = new File(txt);
            FileWriter writer = new FileWriter(outputFile);

            // Read each line of the input file and write it to the output file
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write("\n");
            }

            // Close the input and output streams
            reader.close();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //it converts xml to excel ,then adds them to workbook sheet given as input
    public static XSSFWorkbook convertXMLToExcel(XSSFWorkbook workbook,String XMLPath, String sheetName) throws Exception{
        // Replace with the path to your XML file
        // Replace with the path to your XML file
        File inputFile = new File(XMLPath);

        // Replace with the path to your output Excel file
//        File outputFile = new File(excelPath);

        // Create a new workbook
//        XSSFWorkbook workbook=new XSSFWorkbook();

        // Create a new sheet in the workbook

        workbook.createSheet(sheetName);

        // Get the root element of the XML document
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        Element rootElement = document.getDocumentElement();

        // Get the child nodes of the root element
        NodeList nodeList = rootElement.getChildNodes();

        // Traverse the child nodes and write the data to the Excel sheet
        int rowIndex = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
             Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                List<String> tagnames=getTagNames(element);

                Row row = workbook.getSheet(sheetName).createRow(rowIndex++);
                if(rowIndex==1){
                    int index=0;

                    for(String header:tagnames){

                        Cell cellx=row.createCell(index++);
                        cellx.setCellValue(header);
                    }
                    row=workbook.getSheet(sheetName).createRow(rowIndex++);
                }


                int cellIndex = 0;
                for (String tagName : tagnames) {
                    Cell cell = row.createCell(cellIndex);
                    NodeList tagList = element.getElementsByTagName(tagName);

                    if ((tagList != null) ) {

                        if ((tagList.getLength()>0)) {
                            Node tagNode = tagList.item(0);
                            String tagContent = tagNode.getTextContent();
                            if (tagContent != null) {
                                try{
                                    double x=Double.parseDouble(tagContent);
                                    cell.setCellValue(x);
                                }catch(NumberFormatException ex) {
                                    cell.setCellValue(tagContent);
                                }
                                workbook.getSheet(sheetName).autoSizeColumn(cellIndex);
                            }
                        }
                    }
                    cellIndex++;
                }
            }
        }

        // Write the workbook to the output file
//        FileOutputStream outputStream = new FileOutputStream(outputFile);
//        workbook.write(outputStream);
//        workbook.close();
//        outputStream.close();
        return workbook;
    }
//used in convertXMLTOExcel method
    private static List<String> getTagNames(Element element) throws Exception {
        NodeList nodeList = element.getChildNodes();

        List<String> tagNames = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                tagNames.add(node.getNodeName());

            }
        }

        return tagNames;
    }
    //method, that creates excel file with three sheets with beforecode, aftercode, and differences. Also highlights the difference
    //It gets xml files' pathes and file path that excel file going to be.
    public static void findDifference(String XML1, String XML2, String excelFilePath)throws Exception{
        if(!(fileExists(XML1)&& fileExists(XML2))) {
            return;
        }
        if(!(fileWellFormCheck(XML1) && fileWellFormCheck(XML2))){
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();

        workbook=convertXMLToExcel(workbook, XML1, "BeforeCode");
        workbook=convertXMLToExcel(workbook, XML2, "AfterCode");
        workbook=getDifferenceShell(workbook);
        File outputFile = new File(excelFilePath);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        compareExcels(excelFilePath);

}
}






