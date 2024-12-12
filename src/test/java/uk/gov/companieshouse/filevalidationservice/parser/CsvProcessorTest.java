package uk.gov.companieshouse.filevalidationservice.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filevalidationservice.repositories.FileValidationRepository;


@ExtendWith(MockitoExtension.class)
class CsvProcessorTest {

    @Mock
    private FileValidationRepository fileValidationRepository;

    @InjectMocks
    private CsvProcessor csvProcessor;

    @Test
    void emptyFileMustFailToParse() throws IOException {
        File file = new File("src/test/resources/emptyCsv.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void csvFileWithTooFewHeadersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooFewHeaders.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void csvFileWithOnlyHeadersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/onlyHeaders.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void csvRecordWithTooFewColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooFewColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void csvRecordWithTooManyColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooManyColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void oneGoodRecordMustParse() throws IOException {
        File file = new File("src/test/resources/oneGoodRecord.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertTrue(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void csvRecordWithNoUniqueIdMustFailToParse() throws IOException {
        File file = new File("src/test/resources/noUniqueId.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }
    @Test
    void csvRecordWithUniqueIdOver256CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/uniqueIdOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }
    @Test
    void csvRecordWithRegisteredCompanyOver160CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/registeredCompanyNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

    @Test
    void csvRecordWithCompanyNumberOver10CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/companyNumberOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }
    @Test
    void csvRecordWithTradingNameOver160CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tradingNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }
    @Test
    void csvRecordWithFirstNameOver50CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/firstNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }
    @Test
    void csvRecordWithLastNameOver160CharactersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/lastNameOverCharLimit.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }
    @Test
    void csvRecordWithIncorrectDateFormatMustFailToParse() throws IOException {
        File file = new File("src/test/resources/incorrectDateFormat.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        assertFalse(csvProcessor.parseRecords(bytes, "1"));
    }

}
