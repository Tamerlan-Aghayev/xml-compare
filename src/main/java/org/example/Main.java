package org.example;

import static org.example.file.FileWork.*;

public class Main {
    public static void main(String[] args) throws Exception{

//        findDifference("C:\\Users\\HP\\Desktop\\task\\pom\\text.xml", "C:\\Users\\HP\\Desktop\\task\\pom\\fake.xml","C:\\Users\\HP\\Desktop\\task\\pom\\result.xlsx" );
        compareXMLWithTxt("C:\\Users\\HP\\Desktop\\task\\notwellformed\\text.xml", "C:\\Users\\HP\\Desktop\\task\\notwellformed\\fake.xml", "C:\\Users\\HP\\Desktop\\task\\notwellformed\\text.txt", "C:\\Users\\HP\\Desktop\\task\\notwellformed\\fake.txt", "C:\\Users\\HP\\Desktop\\task\\notwellformed\\difference.xlsx");
        System.out.println("hi");
    }
}