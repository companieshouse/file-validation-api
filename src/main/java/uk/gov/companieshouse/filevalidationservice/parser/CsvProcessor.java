package uk.gov.companieshouse.filevalidationservice.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class CsvProcessor {


    private static final  List<String> VALID_HEADERS = List.of("UniqueID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth");
    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    private final Reader reader;



    public CsvProcessor( byte[] bytesToParse) {
        ByteArrayInputStream decodedBase64AsStream = new ByteArrayInputStream(bytesToParse);
        reader = new InputStreamReader(decodedBase64AsStream);
    }

    public boolean parseRecords() throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
        Iterator<CSVRecord> it = records.iterator();
        CSVRecord headers = records.iterator().next();
        try {
            return isValidFieldHeaders(headers) && isDataAfterHeaders(it);
        } catch (IllegalStateException ex) {
            LOGGER.error("Error parsing, could be corrupt CSV, at record number" + ex);
            return false;
        } finally {
            reader.close();
        }
    }


    private Boolean isValidFieldHeaders (CSVRecord record) {
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
        iterator.next();
        if (!iterator.hasNext()) {
            LOGGER.error("No records in file after headers");
            return false;
        }
        return true;
    }
}
