package uk.gov.companieshouse.filevalidationservice.models;

import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        if (!uniqueId.isEmpty() && uniqueId.length() <= 256) {
            this.uniqueId = uniqueId;
        } else {
            throw new CSVDataValidationException("Unique ID is not valid");
        }
    }

    public String getRegisteredCompanyName() {
        return registeredCompanyName;
    }

    public void setRegisteredCompanyName(String registeredCompanyName) {
        if (registeredCompanyName.length() <= 160) {
            this.registeredCompanyName = registeredCompanyName;
        } else {
            throw new CSVDataValidationException("Registered Company name is over 160 characters long");
        }
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        if (companyNumber.length() <= 10) {
            this.companyNumber = companyNumber;
        } else {
            throw new CSVDataValidationException("Company number is over 10 characters long");
        }
    }

    public String getTradingName() {
        return tradingName;
    }

    public void setTradingName(String tradingName) {
        if (tradingName.length() <= 160) {
            this.tradingName = tradingName;
        } else {
            throw new CSVDataValidationException("Trading name is over 160 characters long");
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName.length() <= 50) {
            this.firstName = firstName;
        } else {
            throw new CSVDataValidationException("First name is over 50 characters long");
        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName.length() <= 160) {
            this.lastName = lastName;
        } else {
            throw new CSVDataValidationException("Last name is over 160 characters long");
        }
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            this.dateOfBirth = LocalDate.parse(dateOfBirth, formatter);
        } catch (DateTimeParseException e) {
            throw new CSVDataValidationException("Date of birth format is incorrect");
        }
    }
}
