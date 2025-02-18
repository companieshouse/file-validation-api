package uk.gov.companieshouse.filevalidationservice.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class CsvProcessorTest {

    @InjectMocks
    private CsvProcessor csvProcessor;

    @Mock
    private CSVFormat csvFormat;

    @Test
    void emptyFileMustFailToParse() throws IOException {
        File file = new File("src/test/resources/emptyCsv.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvFileWithTooFewHeadersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooFewHeaders.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        Exception exception = assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
        String msg = "Data validation exception: Headers did not match expected headers, following headers are missing: [registered company name, company number] at row number 0";
        assertEquals(msg, exception.getMessage());
    }

    @Test
    void csvFileWithOnlyHeadersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/onlyHeaders.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithTooFewColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooFewColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        Exception exception =  assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
        String msg = "Data validation exception: Incorrect number of columns. Received: 6 Expected: 13 at row number 1";
        assertEquals(msg, exception.getMessage());
    }

    @Test
    void csvRecordWithTooManyColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooManyColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @ParameterizedTest
    @ValueSource( strings = {
            "src/test/resources/oneGoodRecord.csv",
            "src/test/resources/noUniqueId.csv",
            "src/test/resources/good_multiple_records.csv",
            "src/test/resources/good_multiple_records_with_case_mismatch_column_names.csv",
            "src/test/resources/good_multiple_records_with_column_names_in_quotes.csv"
    })
    void validRecordsMustParse(String filePath) throws IOException{
        File file = new File(filePath);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        assertDoesNotThrow(() -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithUniqueIdOver256CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/uniqueIdOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithRegisteredCompanyOver160CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/registeredCompanyNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithCompanyNumberOver10CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/companyNumberOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithTradingNameOver160CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tradingNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithFirstNameOver50CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/firstNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithLastNameOver160CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/lastNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithIncorrectDateFormatMustFailToParse() throws IOException {
        File file = new File("src/test/resources/incorrectDateFormat.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithPropertyNameOrNoOver200CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/propertyNameOrNoOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithAddressLine1Over50CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/addressLine1OverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithAddressLine2Over50CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/addressLine2OverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithCityOrTownOver50CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/cityOrTownOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithPostcodeOver20CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/postCodeOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordWithCountryOver50CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/countryOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
    @Test
    void csvRecordThrowsIOExceptionMustFailToParse() throws IOException {
        byte[] bytes = new byte[0];
        Reader reader = mock(InputStreamReader.class);
        lenient().when(reader.read(any(char[].class), anyInt(), anyInt())).thenThrow(new IOException("Test IOException"));

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordThrowsIllegalStateExceptionMustFailToParse() throws IOException {
        byte[] bytes = new byte[0];
        Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes));
        CSVParser parser = mock(CSVParser.class);
        lenient().when(csvFormat.parse(reader)).thenThrow(new IllegalStateException("Test IllegalStateException"));

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }
}
