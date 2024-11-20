package uk.gov.companieshouse.filevalidationservice.utils;

import java.util.List;

public class Constants {
    public static final List<String> VALID_HEADERS = List.of("UniqueID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth");
    public static final Integer NUMBER_OF_COLUMNS = 7 ;
    public static final int INDEX_OF_UNIQUE_ID = 0;
    public static final int INDEX_OF_COMPANY_NAME = 1;
    public static final int INDEX_OF_COMPANY_NUMBER = 2;
    public static final int INDEX_OF_TRADING_NAME = 3;
    public static final int INDEX_OF_FIRST_NAME = 4;
    public static final int INDEX_OF_LAST_NAME = 5;
    public static final int INDEX_OF_DATE_OF_BIRTH = 6;
    public static final int MAX_UNIQUE_ID_LENGTH = 256;
    public static final int MAX_COMPANY_NAME_LENGTH = 160;
    public static final int MAX_COMPANY_NUMBER_LENGTH = 10;
    public static final int MAX_TRADING_NAME_LENGTH = 160;
    public static final int MAX_FIRST_NAME_LENGTH = 50;
    public static final int MAX_LAST_NAME_LENGTH = 160;

    public static final String UPLOAD_URI_PATTERN = "/file-validation-api/document";

}
