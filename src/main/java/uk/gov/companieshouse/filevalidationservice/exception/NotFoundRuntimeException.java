package uk.gov.companieshouse.filevalidationservice.exception;

public class NotFoundRuntimeException extends RuntimeException {

    private final String fieldLocation;

    public NotFoundRuntimeException( final String fieldLocation, final String message ) {
        super( message );
        this.fieldLocation = fieldLocation;
    }

    public String getFieldLocation() {
        return fieldLocation;
    }
}


