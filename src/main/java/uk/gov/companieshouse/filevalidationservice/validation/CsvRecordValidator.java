package uk.gov.companieshouse.filevalidationservice.validation;

import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static uk.gov.companieshouse.filevalidationservice.utils.Constants.*;

public class CsvRecordValidator {

    public static void validateUniqueId(String uniqueId) {
        if (uniqueId.isEmpty() || uniqueId.length() > MAX_UNIQUE_ID_LENGTH) {
            throw new CSVDataValidationException("Unique ID is not valid");
        }
    }

    public static void validateRegisteredCompanyName(String registeredCompanyName) {
        if (registeredCompanyName.length() > MAX_COMPANY_NAME_LENGTH) {
            throw new CSVDataValidationException("Registered Company name is over 160 characters long");
        }
    }

    public static void validateCompanyNumber(String companyNumber) {
        if (companyNumber.length() > MAX_COMPANY_NUMBER_LENGTH) {
            throw new CSVDataValidationException("Company number is over 10 characters long");
        }
    }

    public static void validateTradingName(String tradingName) {
        if (tradingName.length() > MAX_TRADING_NAME_LENGTH) {
            throw new CSVDataValidationException("Trading name is over 160 characters long");
        }
    }

    public static void validateFirstName(String firstName) {
        if (firstName.length() > MAX_FIRST_NAME_LENGTH) {
            throw new CSVDataValidationException("First name is over 50 characters long");
        }
    }

    public static void validateLastName(String lastName) {
        if (lastName.length() > MAX_LAST_NAME_LENGTH) {
            throw new CSVDataValidationException("Last name is over 160 characters long");
        }
    }

    public static void validateDateOfBirth(String dateOfBirth) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDate.parse(dateOfBirth, formatter);
        } catch (DateTimeParseException e) {
            throw new CSVDataValidationException("Date of birth format is incorrect");
        }
    }

    public static void validatePropertyNameOrNo(String propertyNameOrNo) {
        if (propertyNameOrNo.length() > MAX_PROP_NAME_OR_NO_LENGTH) {
            throw new CSVDataValidationException("Property Name or Number is over 200 characters long");
        }
    }

    public static void validateAddressLine1(String addressLine1) {
        if (addressLine1.length() > MAX_ADDRESSLINE1_LENGTH) {
            throw new CSVDataValidationException("AddressLine1 is over 50 characters long");
        }
    }

    public static void validateAddressLine2(String addressLine2) {
        if (addressLine2.length() > MAX_ADDRESSLINE2_LENGTH) {
            throw new CSVDataValidationException("AddressLine2 is over 50 characters long");
        }
    }

    public static void validateCityOrTown(String cityOrTown) {
        if (cityOrTown.length() > MAX_CITY_OR_TOWN_LENGTH) {
            throw new CSVDataValidationException("City or Town is over 50 characters long");
        }
    }

    public static void validatePostcode(String postCode) {
        if (postCode.length() > MAX_POSTCODE_LENGTH) {
            throw new CSVDataValidationException("Postcode is over 20 characters long");
        }
    }

    public static void validateCountry(String country) {
        if (country.length() > MAX_COUNTRY_LENGTH) {
            throw new CSVDataValidationException("Country is over 50 characters long");
        }
    }
}
