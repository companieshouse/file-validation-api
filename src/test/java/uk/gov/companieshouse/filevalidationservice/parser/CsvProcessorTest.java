package uk.gov.companieshouse.filevalidationservice.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;


@ExtendWith(MockitoExtension.class)
class CsvProcessorTest {

    @InjectMocks
    private CsvProcessor csvProcessor;

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

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
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

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithTooManyColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooManyColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void oneGoodRecordMustParse() throws IOException {
        File file = new File("src/test/resources/oneGoodRecord.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertDoesNotThrow(() -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void csvRecordWithNoUniqueIdMustFailToParse() throws IOException {
        File file = new File("src/test/resources/noUniqueId.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertThrows(CSVDataValidationException.class, () -> csvProcessor.parseRecords(bytes));
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
    void TwoGoodRecordMustParse() throws IOException {
        File file = new File("src/test/resources/good_multiple_records.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertDoesNotThrow(() -> csvProcessor.parseRecords(bytes));
    }
}
