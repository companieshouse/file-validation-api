package uk.gov.companieshouse.filevalidationservice.models;

public class FileMetaData {
    private String fileName;
    private String fromLocation;
    private String toLocation;

    public String getFileName() {
        return fileName;
    }

    public FileMetaData() {
    }

    public FileMetaData(String fileName, String fromLocation, String toLocation) {
        this.fileName = fileName;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    @Override
    public String toString() {
        return "FileMetaData{" +
                "fileName='" + fileName + '\'' +
                ", fromLocation='" + fromLocation + '\'' +
                ", toLocation='" + toLocation + '\'' +
                '}';
    }
}
