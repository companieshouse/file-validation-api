package uk.gov.companieshouse.filevalidationservice.exception;

public class FileDetailsException extends RuntimeException {
    public FileDetailsException(String message) {
        super(message);
    }
}
