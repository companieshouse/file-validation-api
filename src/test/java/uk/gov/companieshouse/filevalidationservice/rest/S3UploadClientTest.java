package uk.gov.companieshouse.filevalidationservice.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3UploadClientTest {

    @Mock
    private S3Client mockS3Client;

    @InjectMocks
    private S3UploadClient s3UploadClient;


    @BeforeEach
    void setup() {
        s3UploadClient = new S3UploadClient(mockS3Client, "testBucket");
    }

    @Test
    void testUploadSuccess() {
        // given
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("testBucket")
                .key("testFolder/testFile")
                .build();

        // when
        s3UploadClient.uploadFile("hello".getBytes(), "testFile", "testFolder");

        // then
        verify(mockS3Client).putObject(eq(putObjectRequest), (RequestBody) any());
    }

}