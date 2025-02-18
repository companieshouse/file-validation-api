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
import uk.gov.companieshouse.filevalidationservice.validation.CsvRecordValidator;

import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_UNIQUE_ID;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.NUMBER_OF_COLUMNS;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_COMPANY_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_COMPANY_NUMBER;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_TRADING_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_FIRST_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_LAST_NAME;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_DATE_OF_BIRTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_PROPERTY_NAME_OR_NO;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_ADDRESSLINE1;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_ADDRESSLINE2;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_CITY_OR_TOWN;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_POSTCODE;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.INDEX_OF_COUNTRY;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.VALID_HEADERS;


@Component
public class CsvProcessor {

    public void parseRecords(byte[] bytesToParse) {
        int currentRow = 0;
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(bytesToParse))) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            Iterator<CSVRecord> it = records.iterator();

            isDataAfterHeaders(it);
            currentRow++;
            while (it.hasNext()) {
                CSVRecord record = it.next();

                if (!NUMBER_OF_COLUMNS.equals(record.size())) {
                    throw new CSVDataValidationException("Incorrect number of columns");
                }
                CsvRecordValidator.validateUniqueId(record.get(INDEX_OF_UNIQUE_ID));
                CsvRecordValidator.validateRegisteredCompanyName(record.get(INDEX_OF_COMPANY_NAME));
                CsvRecordValidator.validateCompanyNumber(record.get(INDEX_OF_COMPANY_NUMBER));
                CsvRecordValidator.validateTradingName(record.get(INDEX_OF_TRADING_NAME));
                CsvRecordValidator.validateFirstName(record.get(INDEX_OF_FIRST_NAME));
                CsvRecordValidator.validateLastName(record.get(INDEX_OF_LAST_NAME));
                CsvRecordValidator.validateDateOfBirth(record.get(INDEX_OF_DATE_OF_BIRTH));
                CsvRecordValidator.validatePropertyNameOrNo(record.get(INDEX_OF_PROPERTY_NAME_OR_NO));
                CsvRecordValidator.validateAddressLine1(record.get(INDEX_OF_ADDRESSLINE1));
                CsvRecordValidator.validateAddressLine2(record.get(INDEX_OF_ADDRESSLINE2));
                CsvRecordValidator.validateCityOrTown(record.get(INDEX_OF_CITY_OR_TOWN));
                CsvRecordValidator.validatePostcode(record.get(INDEX_OF_POSTCODE));
                CsvRecordValidator.validateCountry(record.get(INDEX_OF_COUNTRY));
                currentRow++;
            }

        } catch (IllegalStateException ex) {
            throw new CSVDataValidationException(String.format("Error parsing, could be corrupt CSV, at record number %s,  %s", currentRow , ex.getMessage()));
        } catch (CSVDataValidationException ex) {
            throw new CSVDataValidationException(String.format("Data validation exception: %s at row number %s", ex.getMessage(), currentRow));
        } catch (IOException e) {
            throw new CSVDataValidationException(String.format("Data validation reading the file: %s", e.getMessage()));
        }
    }


    private void isValidFieldHeaders(CSVRecord csvRecord) {
        List<String>  actualHeaders = csvRecord.stream()
                .map(header -> {
                    String withoutQuotes = header.replace("\"", "");
                    String trimmed = withoutQuotes.trim();
                    return trimmed.toLowerCase();
                })
                .toList();
        if (!actualHeaders.equals(VALID_HEADERS)) {
            throw new CSVDataValidationException("Headers did not match expected headers");
        }
    }


    private void isDataAfterHeaders (Iterator<CSVRecord> iterator) {
        if (!iterator.hasNext()) {
            throw new CSVDataValidationException("No records in file, not even headers");
        }
        isValidFieldHeaders(iterator.next());
        if (!iterator.hasNext()) {
            throw new CSVDataValidationException("No records in file after headers");
        }
    }
}
