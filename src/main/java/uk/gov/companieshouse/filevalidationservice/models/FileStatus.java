package uk.gov.companieshouse.filevalidationservice.models;

public enum FileStatus {
    PENDING("pending"),
    COMPLETED("completed"),
    IN_PROGRESS("in-progress"),

    DOWNLOAD_ERROR("download-error"),
    UPLOAD_ERROR("upload-error"),
    VALIDATION_ERROR("validation-error");

    public final String label;

    FileStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
