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
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.filevalidationservice.validation.CsvRecordValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class CsvProcessor {

    private static final  List<String> VALID_HEADERS = List.of("UniqueID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth");
    private static final Integer NUMBER_OF_COLUMN = 7 ;
    private static final int INDEX_OF_UNIQUE_ID = 0;
    private static final int INDEX_OF_COMPANY_NAME = 1;
    private static final int INDEX_OF_COMPANY_NUMBER = 2;
    private static final int INDEX_OF_TRADING_NAME = 3;
    private static final int INDEX_OF_FIRST_NAME = 4;
    private static final int INDEX_OF_LAST_NAME = 5;
    private static final int INDEX_OF_DATE_OF_BIRTH = 6;

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
                    CsvRecordValidator.validateUniqueId(record.get(INDEX_OF_UNIQUE_ID));
                    CsvRecordValidator.validateRegisteredCompanyName(record.get(INDEX_OF_COMPANY_NAME));
                    CsvRecordValidator.validateCompanyNumber(record.get(INDEX_OF_COMPANY_NUMBER));
                    CsvRecordValidator.validateTradingName(record.get(INDEX_OF_TRADING_NAME));
                    CsvRecordValidator.validateFirstName(record.get(INDEX_OF_FIRST_NAME));
                    CsvRecordValidator.validateLastName(record.get(INDEX_OF_LAST_NAME));
                    CsvRecordValidator.validateDateOfBirth(record.get(INDEX_OF_DATE_OF_BIRTH));
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
