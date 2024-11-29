package uk.gov.companieshouse.filevalidationservice.models;

public enum FileStatus {
    PENDING("pending"),
    COMPLETE("complete");

    public final String label;

    FileStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
