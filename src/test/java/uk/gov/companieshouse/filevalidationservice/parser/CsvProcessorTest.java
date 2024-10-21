package uk.gov.companieshouse.filevalidationservice.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.io.File;
import java.io.IOException;


import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class CsvProcessorTest {

    @Test
    void emptyFileMustFailToParse() throws IOException {
        File file = new File("src/test/resources/emptyCsv.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertFalse(csvProcessor.parseRecords());
    }

    @Test
    void csvFileWithTooFewHeadersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooFewHeaders.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertFalse(csvProcessor.parseRecords());
    }

    @Test
    void csvFileWithOnlyHeadersMustFailToParse() throws IOException {
        File file = new File("src/test/resources/onlyHeaders.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertFalse(csvProcessor.parseRecords());
    }

    @Test
    void csvRecordWithTooFewColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooFewColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertFalse(csvProcessor.parseRecords());
    }

    @Test
    void csvRecordWithTooManyColumnsMustFailToParse() throws IOException {
        File file = new File("src/test/resources/tooManyColumns.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertFalse(csvProcessor.parseRecords());
    }

    @Test
    void oneGoodRecordMustParse() throws IOException {
        File file = new File("src/test/resources/oneGoodRecord.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertTrue(csvProcessor.parseRecords());
    }

    @Test
    void csvRecordWithNoUniqueIdMustFailToParse() throws IOException {
        File file = new File("src/test/resources/noUniqueId.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);

        CsvProcessor csvProcessor = new CsvProcessor(bytes);
        assertFalse(csvProcessor.parseRecords());
    }

}
