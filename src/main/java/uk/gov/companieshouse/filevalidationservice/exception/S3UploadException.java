package uk.gov.companieshouse.filevalidationservice.exception;

public class S3UploadException extends RuntimeException {
    public S3UploadException(String message) {
        super(message);
    }
}
