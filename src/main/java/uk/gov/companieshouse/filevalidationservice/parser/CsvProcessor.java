package uk.gov.companieshouse.filevalidationservice.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CsvProcessor {

    private static final int INDEX_OF_UNIQUE_ID = 0;
    private static final int INDEX_OF_REGISTERED_COMPANY_NAME = 1;
    private static final int INDEX_OF_COMPANY_NUMBER = 2;
    private static final int INDEX_OF_TRADING_NAME = 3;
    private static final int INDEX_OF_FIRST_NAME = 4;
    private static final int INDEX_OF_LAST_NAME = 5;
    private static final int INDEX_OF_DATE_OF_BIRTH = 6;

    private final Reader reader;
    private boolean successfullyProcessedSoFar = true;
    private int currentRecordBeingParsed = -1;

    private static final String NULL_FIELD = "-";


    public CsvProcessor(Reader reader) {
        this.reader = reader;
    }

    public boolean parseRecords() throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withNullString(NULL_FIELD).parse(reader);
        Iterator<CSVRecord> it = records.iterator();
        try {
            if (movePastHeadersToStartOfData(it, INITIAL_HEADER_RECORDS_TO_IGNORE)) {
                while (it.hasNext()) {
                    currentRecordBeingParsed++;
                    parseRecord(it.next());
                }
            } else {
                successfullyProcessedSoFar = false;
            }
        } catch (IllegalStateException ex) {
            LOG.error("Error parsing, could be corrupt CSV, at record number ([{}]: {}",
                    currentRecordBeingParsed, ex, ex);
            successfullyProcessedSoFar = false;
        } finally {
            reader.close();
        }
        return successfullyProcessedSoFar;
    }


    public Boolean validateFieldHeaders (List<String> csvFileLines) {
        String headers = csvFileLines.getFirst();
        String[] validHeaders = {"UniqueID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth"};

        String[] separatedHeaders = headers.split(",");

        return Arrays.equals(validHeaders, separatedHeaders);

    }
}
