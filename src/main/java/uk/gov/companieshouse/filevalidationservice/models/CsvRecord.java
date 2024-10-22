package uk.gov.companieshouse.filevalidationservice.models;

import uk.gov.companieshouse.filevalidationservice.validation.CsvRecordValidator;

import java.time.LocalDate;

public class CsvRecord {

    private String uniqueId;
    private String registeredCompanyName;
    private String companyNumber;
    private String tradingName;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    // Getters and Setters
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        CsvRecordValidator.validateUniqueId(uniqueId);
        this.uniqueId = uniqueId;
    }

    public String getRegisteredCompanyName() {
        return registeredCompanyName;
    }

    public void setRegisteredCompanyName(String registeredCompanyName) {
        CsvRecordValidator.validateRegisteredCompanyName(registeredCompanyName);
        this.registeredCompanyName = registeredCompanyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        CsvRecordValidator.validateCompanyNumber(companyNumber);
        this.companyNumber = companyNumber;
    }

    public String getTradingName() {
        return tradingName;
    }

    public void setTradingName(String tradingName) {
        CsvRecordValidator.validateTradingName(tradingName);
        this.tradingName = tradingName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        CsvRecordValidator.validateFirstName(firstName);
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        CsvRecordValidator.validateLastName(lastName);
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = CsvRecordValidator.validateDateOfBirth(dateOfBirth);
    }
}
