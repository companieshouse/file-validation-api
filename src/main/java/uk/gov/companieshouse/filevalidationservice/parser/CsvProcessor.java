package uk.gov.companieshouse.filevalidationservice.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.filevalidationservice.validation.CsvRecordValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.filevalidationservice.utils.Constants.NUMBER_OF_COLUMNS;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_UNIQUE_ID;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_COMPANY_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_COMPANY_NUMBER;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_TRADING_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_FIRST_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_LAST_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_DATE_OF_BIRTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.VALID_HEADERS;

@Component
public class CsvProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    public boolean parseRecords(byte[] bytesToParse) {
        boolean isFileValid = true;
        int currentRow = 0;
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(bytesToParse))) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            Iterator<CSVRecord> it = records.iterator();

            if (isDataAfterHeaders(it)) {
                currentRow++;
                while (it.hasNext()) {
                    CSVRecord record = it.next();
                    currentRow++;
                    if (!NUMBER_OF_COLUMNS.equals(record.size())) {
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
            LOGGER.error("Error parsing, could be corrupt CSV, at record number " + currentRow + " " + ex);
            isFileValid = false;
        } catch (CSVDataValidationException ex) {
            LOGGER.error("Data validation exception: " + ex.getMessage() + " at row number " + currentRow);
            isFileValid = false;
        } catch (IOException e) {
            LOGGER.error("Data validation reading the file: " + e.getMessage());
            isFileValid = false;
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
