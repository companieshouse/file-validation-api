package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Service
public class CsvValidationService {



    public List<String> openCsvFile (String fileName) throws FileNotFoundException {
        File importedFile = new File(fileName);
        Scanner scanner = new Scanner(importedFile);
        List<String> fileLines = new ArrayList<>();


        while (scanner.hasNextLine()) {
            fileLines.add(scanner.nextLine());
        }

        return  fileLines;
    }


    public Boolean validateFieldHeaders (List<String> csvFileLines) {
        String headers = csvFileLines.getFirst();
        String[] validHeaders = {"UniqueID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth"};

        String[] separatedHeaders = headers.split(",");

        return Arrays.equals(validHeaders, separatedHeaders);

    }

}
