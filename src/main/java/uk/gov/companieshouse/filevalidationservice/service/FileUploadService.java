package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import uk.gov.companieshouse.filevalidationservice.rest.S3UploadClient;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class FileUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    private final S3UploadClient s3UploadClient;

    public FileUploadService(S3UploadClient s3UploadClient){
        this.s3UploadClient = s3UploadClient;
    }

    public void uploadToS3AndUpdateStatus(byte[] file, String fileId, String amlBodyName) {
        try {
            s3UploadClient.uploadFile(file, fileId, amlBodyName);
            // TODO when db connection has been added implement logic to update the status to "Processed Successfully"
        } catch (SdkException e) {
            LOGGER.error(String.format("Failed to upload to S3 for file: %s with message %s", fileId, e.getMessage()));
            // TODO when db connection has been added implement logic to update the status to "Failed"
            throw e;
        }
    }
}
