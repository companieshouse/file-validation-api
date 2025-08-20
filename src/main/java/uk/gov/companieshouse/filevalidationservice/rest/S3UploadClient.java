package uk.gov.companieshouse.filevalidationservice.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.companieshouse.filevalidationservice.exception.S3UploadException;

@Component
public class S3UploadClient {

    private final S3Client s3;

    private final String bucketName;


    public S3UploadClient(S3Client that, @Value("${s3.bucket.name}") String bucketName){
        this.s3 = that;
        this.bucketName = bucketName;
    }

    public void uploadFile(byte[] document, String documentId, String amlBodyName) {
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(amlBodyName + "/" + documentId)
                    .build(), RequestBody.fromBytes(document));
        } catch (Exception e) {
            throw new S3UploadException(e.getMessage());
        }
    }

    public void uploadFileOnError(byte[] document, String documentId, String amlBodyName) {
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(String.format("%s/validator-error/%s", amlBodyName, documentId))
                    .build(), RequestBody.fromBytes(document));
        } catch (Exception e) {
            throw new S3UploadException(e.getMessage());
        }
    }

}
