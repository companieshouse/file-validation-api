package uk.gov.companieshouse.filevalidationservice.exception;

public class FileUploadException extends RuntimeException{
    public FileUploadException(String message){ super(message); }
}
