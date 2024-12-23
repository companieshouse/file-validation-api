package uk.gov.companieshouse.filevalidationservice.validation;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;
import static org.junit.jupiter.api.Assertions.*;

class CsvRecordValidatorTest {
    @Test
    void testEmptyUniqueId() {
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateUniqueId("")
        );
        assertTrue(thrown.getMessage().contains("Unique ID is not valid"));
    }

    @Test
    void testUniqueIdLengthMoreThan256() {
        String uniqueId = "1".repeat(257);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateUniqueId(uniqueId)
        );
        assertTrue(thrown.getMessage().contains("Unique ID is not valid"));
    }

    @Test
    void testValidUniqueId() {
        String uniqueId = "1".repeat(256);
        assertDoesNotThrow(() -> CsvRecordValidator.validateUniqueId(uniqueId));
    }

    @Test
    void testCompanyNameLengthMoreThan160() {
        String companyName = "a".repeat(161);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateRegisteredCompanyName(companyName)
        );
        assertTrue(thrown.getMessage().contains("Registered Company name is over 160 characters long"));
    }

    @Test
    void testValidCompanyName() {
        String companyName = "a".repeat(160);
        assertDoesNotThrow(() -> CsvRecordValidator.validateRegisteredCompanyName(companyName));
    }

    @Test
    void testCompanyNumberLengthMoreThan10() {
        String companyNumber = "1".repeat(11);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateCompanyNumber(companyNumber)
        );
        assertTrue(thrown.getMessage().contains("Company number is over 10 characters long"));
    }

    @Test
    void testValidCompanyNumber() {
        String companyNumber = "1".repeat(10);
        assertDoesNotThrow(() -> CsvRecordValidator.validateCompanyNumber(companyNumber));
    }

    @Test
    void testTradingNameLengthMoreThan160() {
        String tradingName = "a".repeat(161);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateTradingName(tradingName)
        );
        assertTrue(thrown.getMessage().contains("Trading name is over 160 characters long"));
    }

    @Test
    void testValidTradingName() {
        String tradingName = "a".repeat(160);
        assertDoesNotThrow(() -> CsvRecordValidator.validateTradingName(tradingName));
    }

    @Test
    void testFirstNameLengthMoreThan50() {
        String firstName = "a".repeat(51);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateFirstName(firstName)
        );
        assertTrue(thrown.getMessage().contains("First name is over 50 characters long"));
    }

    @Test
    void testValidFirstName() {
        String firstName = "a".repeat(50);
        assertDoesNotThrow(() -> CsvRecordValidator.validateFirstName(firstName));
    }

    @Test
    void testLastNameLengthMoreThan160() {
        String lastName = "a".repeat(161);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateLastName(lastName)
        );
        assertTrue(thrown.getMessage().contains("Last name is over 160 characters long"));
    }

    @Test
    void testValidLastNameLengthMoreThan160() {
        String lastName = "a".repeat(160);
        assertDoesNotThrow(() -> CsvRecordValidator.validateLastName(lastName));
    }

    @Test
    void testInvalidDobFormat() {
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateDateOfBirth("01132026")
        );
        assertTrue(thrown.getMessage().contains("Date of birth format is incorrect"));
    }

    @Test
    void testValidDobFormat() {
        assertDoesNotThrow(() -> CsvRecordValidator.validateDateOfBirth("01022024"));
    }

    @Test
    void testPropertyNameOrNoLengthMorethan200() {
        String propertyName = "a".repeat(201);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validatePropertyNameOrNo(propertyName)
        );
        assertTrue(thrown.getMessage().contains("Property Name or Number is over 200 characters long"));
    }

    @Test
    void testValidPropertyNameOrNo() {
        String propertyName = "a".repeat(200);
        assertDoesNotThrow(() -> CsvRecordValidator.validatePropertyNameOrNo(propertyName));
    }

    @Test
    void testAddressLine1LengthMorethan50() {
        String addressLine1 = "a".repeat(51);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateAddressLine1(addressLine1)
        );
        assertTrue(thrown.getMessage().contains("AddressLine1 is over 50 characters long"));
    }

    @Test
    void testValidAddressLine1() {
        String addressLine1 = "a".repeat(50);
        assertDoesNotThrow(() -> CsvRecordValidator.validateAddressLine1(addressLine1));
    }

    @Test
    void testAddressLine2LengthMorethan50() {
        String addressLine2 = "a".repeat(51);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateAddressLine2(addressLine2)
        );
        assertTrue(thrown.getMessage().contains("AddressLine2 is over 50 characters long"));
    }

    @Test
    void testValidAddressLine2() {
        String addressLine2 = "a".repeat(50);
        assertDoesNotThrow(() -> CsvRecordValidator.validateAddressLine2(addressLine2));
    }

    @Test
    void testCityOrTownLengthMorethan50() {
        String CityOrTown = "a".repeat(51);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateCityOrTown(CityOrTown)
        );
        assertTrue(thrown.getMessage().contains("City or Town is over 50 characters long"));
    }

    @Test
    void testValidCityOrTown() {
        String CityOrTown = "a".repeat(50);
        assertDoesNotThrow(() -> CsvRecordValidator.validateCityOrTown(CityOrTown));
    }

    @Test
    void testPostcodeLengthMorethan20() {
        String postCode = "a".repeat(21);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validatePostcode(postCode)
        );
        assertTrue(thrown.getMessage().contains("Postcode is over 20 characters long"));
    }

    @Test
    void testValidPostcode() {
        String postCode = "a".repeat(20);
        assertDoesNotThrow(() -> CsvRecordValidator.validatePostcode(postCode));
    }

    @Test
    void testCountryLengthMorethan50() {
        String country = "a".repeat(51);
        Exception thrown = assertThrows(
                CSVDataValidationException.class,
                () -> CsvRecordValidator.validateCountry(country)
        );
        assertTrue(thrown.getMessage().contains("Country is over 50 characters long"));
    }

    @Test
    void testValidCountry() {
        String country = "a".repeat(50);
        assertDoesNotThrow(() -> CsvRecordValidator.validateCountry(country));
    }
}