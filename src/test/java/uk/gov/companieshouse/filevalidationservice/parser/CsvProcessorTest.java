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

@ExtendWith(MockitoExtension.class)
class CsvProcessorTest {

    @InjectMocks
    private CsvProcessor csvProcessor;

    @Mock
    private CSVFormat csvFormat;

    @ParameterizedTest
    @ValueSource( strings = {
            "src/test/resources/oneGoodRecord.csv",
            "src/test/resources/noUniqueId.csv",
            "src/test/resources/good_multiple_records.csv",
            "src/test/resources/correctHeadersWithBOM.csv",
            "src/test/resources/good_multiple_records_with_case_mismatch_column_names.csv",
            "src/test/resources/good_multiple_records_with_column_names_in_quotes.csv"
    })
    void validRecordsMustParse(String filePath) throws IOException{
        File file = new File(filePath);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        assertDoesNotThrow(() -> csvProcessor.parseRecords(bytes));
    }

    @Test
    void validRecordsMustParseWithBOM() throws IOException {
        File file = new File("src/test/resources/good_multiple_records.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);
        byte[] bomBytes = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};

        byte[] combinedBytes = new byte[bomBytes.length + bytes.length];
        System.arraycopy(bomBytes, 0, combinedBytes, 0, bomBytes.length);
        System.arraycopy(bytes, 0, combinedBytes, bomBytes.length, bytes.length);
        assertDoesNotThrow(() -> csvProcessor.parseRecords(combinedBytes));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/resources/emptyCsv.csv",
            "src/test/resources/tooFewHeaders.csv",
            "src/test/resources/onlyHeaders.csv",
            "src/test/resources/tooFewColumns.csv",
            "src/test/resources/tooManyColumns.csv",
            "src/test/resources/uniqueIdOverCharLimit.csv",
            "src/test/resources/registeredCompanyNameOverCharLimit.csv",
            "src/test/resources/companyNumberOverCharLimit.csv",
            "src/test/resources/tradingNameOverCharLimit.csv",
            "src/test/resources/firstNameOverCharLimit.csv",
            "src/test/resources/lastNameOverCharLimit.csv",
            "src/test/resources/incorrectDateFormat.csv",
            "src/test/resources/propertyNameOrNoOverCharLimit.csv",
            "src/test/resources/addressLine1OverCharLimit.csv",
            "src/test/resources/addressLine2OverCharLimit.csv",
            "src/test/resources/cityOrTownOverCharLimit.csv",
            "src/test/resources/postCodeOverCharLimit.csv",
            "src/test/resources/countryOverCharLimit.csv"
    })
    void csvRecordWithIncorrectInputMustFailToParse(String filePath) throws IOException {
        File file = new File(filePath);
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
