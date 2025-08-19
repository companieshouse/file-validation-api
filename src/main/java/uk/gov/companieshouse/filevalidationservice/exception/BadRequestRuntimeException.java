package uk.gov.companieshouse.filevalidationservice.exception;

public class BadRequestRuntimeException extends RuntimeException {

    public BadRequestRuntimeException(final String message, String badRequest) {
        super( message );
    }
}
