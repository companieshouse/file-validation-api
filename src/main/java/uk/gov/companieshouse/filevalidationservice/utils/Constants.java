package uk.gov.companieshouse.filevalidationservice.utils;

import java.util.List;

public class Constants {
    public static final List<String> VALID_HEADERS = List.of("Unique ID", "Registered Company Name", "Company Number", "Trading Name", "First Name", "Last Name", "Date of Birth",
            "Property Name or Number","Address Line 1","Address Line 2","City or Town","Postcode","Country");
    public static final Integer NUMBER_OF_COLUMNS = 13 ;
    public static final int INDEX_OF_UNIQUE_ID = 0;
    public static final int INDEX_OF_COMPANY_NAME = 1;
    public static final int INDEX_OF_COMPANY_NUMBER = 2;
    public static final int INDEX_OF_TRADING_NAME = 3;
    public static final int INDEX_OF_FIRST_NAME = 4;
    public static final int INDEX_OF_LAST_NAME = 5;
    public static final int INDEX_OF_DATE_OF_BIRTH = 6;
    public static final int INDEX_OF_PROPERTY_NAME_OR_NO = 7;
    public static final int INDEX_OF_ADDRESSLINE1 = 8;
    public static final int INDEX_OF_ADDRESSLINE2 = 9;
    public static final int INDEX_OF_CITY_OR_TOWN = 10;
    public static final int INDEX_OF_POSTCODE = 11;
    public static final int INDEX_OF_COUNTRY = 12;
    public static final int MAX_UNIQUE_ID_LENGTH = 256;
    public static final int MAX_COMPANY_NAME_LENGTH = 160;
    public static final int MAX_COMPANY_NUMBER_LENGTH = 10;
    public static final int MAX_TRADING_NAME_LENGTH = 160;
    public static final int MAX_FIRST_NAME_LENGTH = 50;
    public static final int MAX_LAST_NAME_LENGTH = 160;

    public static final int MAX_PROP_NAME_OR_NO_LENGTH = 200;
    public static final int MAX_ADDRESSLINE1_LENGTH = 50;
    public static final int MAX_ADDRESSLINE2_LENGTH = 50;
    public static final int MAX_CITY_OR_TOWN_LENGTH = 50;
    public static final int MAX_POSTCODE_LENGTH = 20;
    public static final int MAX_COUNTRY_LENGTH = 50;

    public static final String UPLOAD_URI_PATTERN = "/file-validation-api/document";

}
