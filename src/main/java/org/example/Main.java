package org.example;

import static org.example.file.FileWork.*;

public class Main {
    public static void main(String[] args) throws Exception{



//        findDifference("C:\\Users\\HP\\Desktop\\task\\pom\\text.xml", "C:\\Users\\HP\\Desktop\\task\\pom\\fake.xml","C:\\Users\\HP\\Desktop\\task\\pom\\result.xlsx" );
        compare("C:\\Users\\HP\\Desktop\\task\\pom\\text.xml", "C:\\Users\\HP\\Desktop\\task\\pom\\fake.xml", "C:\\Users\\HP\\Desktop\\task\\pom\\difference.xlsx");
//        compherensive("C:\\Users\\HP\\Desktop\\task\\pom\\text.xml", "C:\\Users\\HP\\Desktop\\task\\pom\\fake.xml");
    }

}