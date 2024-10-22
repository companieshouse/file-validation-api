package uk.gov.companieshouse.filevalidationservice.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;
import uk.gov.companieshouse.filevalidationservice.models.CsvRecord;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class CsvProcessor {


    private static final  List<String> VALID_HEADERS = List.of("UniqueID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth");
    private static final Integer NUMBER_OF_COLUMN = 7 ;
    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    private final Reader reader;



    public CsvProcessor( byte[] bytesToParse) {
        ByteArrayInputStream decodedBase64AsStream = new ByteArrayInputStream(bytesToParse);
        reader = new InputStreamReader(decodedBase64AsStream);
    }

    public boolean parseRecords() throws IOException {
        boolean isFileValid = true;
        int currentRow = 0;
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
        Iterator<CSVRecord> it = records.iterator();

        try {
            if (isDataAfterHeaders(it) ) {
                currentRow ++;
                while (it.hasNext()){
                    CSVRecord record = it.next();
                    currentRow ++;
                    if (!NUMBER_OF_COLUMN.equals(record.size())) {
                        LOGGER.error("Incorrect number of columns");
                        return false;
                    }
                    CsvRecord csvRow = new CsvRecord();
                    csvRow.setUniqueId(record.get(0));
                    csvRow.setRegisteredCompanyName(record.get(1));
                    csvRow.setCompanyNumber(record.get(2));
                    csvRow.setTradingName(record.get(3));
                    csvRow.setFirstName(record.get(4));
                    csvRow.setLastName(record.get(5));
                    csvRow.setDateOfBirth(record.get(6));
                }

            } else {
                isFileValid = false;
            }
        } catch (IllegalStateException ex) {
            LOGGER.error("Error parsing, could be corrupt CSV, at record number "  + currentRow + " " + ex);
            isFileValid = false;
        } catch (CSVDataValidationException ex) {
            LOGGER.error("Data validation exception: " + ex.getMessage() + " at row number " + currentRow);
            isFileValid = false;
        } finally {
            reader.close();
        }
        return isFileValid;
    }


    private Boolean isValidFieldHeaders(CSVRecord record) {
        List<String>  actualHeaders = record.toList();
        if (!actualHeaders.equals(VALID_HEADERS)) {
            LOGGER.error("Headers did not match expected headers");
            return false;
        }
        return true;
    }


    private boolean isDataAfterHeaders (Iterator<CSVRecord> iterator) {
        if (!iterator.hasNext()) {
            LOGGER.error("No records in file, not even headers");
            return false;
        }
        if(!isValidFieldHeaders(iterator.next())){
            return false;
        }
        if (!iterator.hasNext()) {
            LOGGER.error("No records in file after headers");
            return false;
        }
        return true;
    }
}
