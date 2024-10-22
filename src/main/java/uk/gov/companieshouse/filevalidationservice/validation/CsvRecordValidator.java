package uk.gov.companieshouse.filevalidationservice.validation;

import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static uk.gov.companieshouse.filevalidationservice.utils.Constants.MAX_UNIQUE_ID_LENGTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.MAX_COMPANY_NAME_LENGTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.MAX_COMPANY_NUMBER_LENGTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.MAX_TRADING_NAME_LENGTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.MAX_FIRST_NAME_LENGTH;
import static uk.gov.companieshouse.filevalidationservice.utils.Constants.MAX_LAST_NAME_LENGTH;

public class CsvRecordValidator {

    public static void validateUniqueId(String uniqueId) {
        if (uniqueId == null || uniqueId.isEmpty() || uniqueId.length() > MAX_UNIQUE_ID_LENGTH) {
            throw new CSVDataValidationException("Unique ID is not valid");
        }
    }

    public static void validateRegisteredCompanyName(String registeredCompanyName) {
        if (registeredCompanyName == null || registeredCompanyName.length() > MAX_COMPANY_NAME_LENGTH) {
            throw new CSVDataValidationException("Registered Company name is over 160 characters long");
        }
    }

    public static void validateCompanyNumber(String companyNumber) {
        if (companyNumber == null || companyNumber.length() > MAX_COMPANY_NUMBER_LENGTH) {
            throw new CSVDataValidationException("Company number is over 10 characters long");
        }
    }

    public static void validateTradingName(String tradingName) {
        if (tradingName == null || tradingName.length() > MAX_TRADING_NAME_LENGTH) {
            throw new CSVDataValidationException("Trading name is over 160 characters long");
        }
    }

    public static void validateFirstName(String firstName) {
        if (firstName == null || firstName.length() > MAX_FIRST_NAME_LENGTH) {
            throw new CSVDataValidationException("First name is over 50 characters long");
        }
    }

    public static void validateLastName(String lastName) {
        if (lastName == null || lastName.length() > MAX_LAST_NAME_LENGTH) {
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
}
