package uk.gov.companieshouse.filevalidationservice.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;
import uk.gov.companieshouse.filevalidationservice.validation.CsvRecordValidator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.filevalidationservice.FileValidationApplication.APPLICATION_NAMESPACE;
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

    private static final Logger LOGGER = LoggerFactory.getLogger( APPLICATION_NAMESPACE );

    public void parseRecords(byte[] bytesToParse) {
        int currentRow = 1;

        // Check for UTF-8 BOM (0xEF,0xBB,0xBF)
        if (bytesToParse.length >= 3 &&
            (bytesToParse[0] & 0xFF) == 0xEF &&
            (bytesToParse[1] & 0xFF) == 0xBB &&
            (bytesToParse[2] & 0xFF) == 0xBF) {
            // Strip BOM
            bytesToParse = Arrays.copyOfRange(bytesToParse, 3, bytesToParse.length);
        }

        try (var reader = new InputStreamReader(new ByteArrayInputStream(bytesToParse), StandardCharsets.UTF_8)) {

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);
            Iterator<CSVRecord> it = parser.iterator();

            isDataAfterHeaders(it);

            isValidFieldHeaders(parser.getHeaderNames());

            currentRow++;

            while (it.hasNext()) {
                var csvRecord = it.next();
                LOGGER.debug(String.format("Processing record: %s", csvRecord.toString()));

                if (!NUMBER_OF_COLUMNS.equals(csvRecord.size())) {
                    throw new CSVDataValidationException(String.format("Incorrect number of columns. Received: %s Expected: %s", csvRecord.size(), NUMBER_OF_COLUMNS ));
                }
                CsvRecordValidator.validateUniqueId(csvRecord.get(INDEX_OF_UNIQUE_ID));
                CsvRecordValidator.validateRegisteredCompanyName(csvRecord.get(INDEX_OF_COMPANY_NAME));
                CsvRecordValidator.validateCompanyNumber(csvRecord.get(INDEX_OF_COMPANY_NUMBER));
                CsvRecordValidator.validateTradingName(csvRecord.get(INDEX_OF_TRADING_NAME));
                CsvRecordValidator.validateFirstName(csvRecord.get(INDEX_OF_FIRST_NAME));
                CsvRecordValidator.validateLastName(csvRecord.get(INDEX_OF_LAST_NAME));
                CsvRecordValidator.validateDateOfBirth(csvRecord.get(INDEX_OF_DATE_OF_BIRTH));
                CsvRecordValidator.validatePropertyNameOrNo(csvRecord.get(INDEX_OF_PROPERTY_NAME_OR_NO));
                CsvRecordValidator.validateAddressLine1(csvRecord.get(INDEX_OF_ADDRESSLINE1));
                CsvRecordValidator.validateAddressLine2(csvRecord.get(INDEX_OF_ADDRESSLINE2));
                CsvRecordValidator.validateCityOrTown(csvRecord.get(INDEX_OF_CITY_OR_TOWN));
                CsvRecordValidator.validatePostcode(csvRecord.get(INDEX_OF_POSTCODE));
                CsvRecordValidator.validateCountry(csvRecord.get(INDEX_OF_COUNTRY));
                currentRow++;
            }

        } catch (IllegalStateException ex) {
            throw new CSVDataValidationException(String.format("Error parsing, could be corrupt CSV, on line %s,  %s", currentRow , ex.getMessage()));
        } catch (CSVDataValidationException ex) {
            throw new CSVDataValidationException(String.format("Data validation exception: %s on line %s", ex.getMessage(), currentRow));
        } catch (IOException e) {
            throw new CSVDataValidationException(String.format("Data validation reading the file: %s", e.getMessage()));
        }
    }


    private void isValidFieldHeaders(List<String> headers) {
        LOGGER.debug("Validating headers");
        List<String>  actualHeaders = headers.stream()
                .map(header -> {
                    String withoutQuotes = header.replace("\"", "");
                    String trimmed = withoutQuotes.strip();
                    return trimmed.toLowerCase(Locale.ENGLISH);
                })
                .toList();
        List<String> mismatchedHeaders = VALID_HEADERS.stream()
                .filter(element -> !actualHeaders.contains(element)).toList();
        if (!mismatchedHeaders.isEmpty()) {
            LOGGER.error(String.format("Incorrect headers provided: %s", actualHeaders));
            throw new CSVDataValidationException(String.format("Headers did not match expected headers, following headers are missing: %s", mismatchedHeaders));
        }
    }


    private void isDataAfterHeaders (Iterator<CSVRecord> iterator) {
        if (!iterator.hasNext()) {
            throw new CSVDataValidationException("No records in file after headers");
        }
    }
}
