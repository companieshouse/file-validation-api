package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.S3Exception;
import uk.gov.companieshouse.filevalidationservice.exception.S3UploadException;
import uk.gov.companieshouse.filevalidationservice.rest.S3UploadClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    private static final String FILE_ID = "testFileId";
    private static final String AML_BODY ="AMLBody";

    @Mock
    S3UploadClient s3UploadClient;

    @InjectMocks
    FileUploadService fileUploadService;


    @Test
    void testUploadToS3Success() {
        // given
        byte[] fileInBytes = "Hello World".getBytes();
        // when
        doNothing().when(s3UploadClient).uploadFile(fileInBytes, FILE_ID, AML_BODY);
        fileUploadService.uploadToS3AndUpdateStatus(fileInBytes, FILE_ID, AML_BODY);
        // then
        verify(s3UploadClient, times(1)).uploadFile(fileInBytes, FILE_ID, AML_BODY);
        verifyNoMoreInteractions(s3UploadClient);
    }

    @Test
    void testUploadToS3Failure() {
        // given
        byte[] fileInBytes = "Hello World".getBytes();
        // when
        doThrow(S3Exception.class).when(s3UploadClient).uploadFile(fileInBytes, FILE_ID, AML_BODY);
        // then
        assertThrows(S3UploadException.class, () -> fileUploadService.uploadToS3AndUpdateStatus(fileInBytes, FILE_ID, AML_BODY));
    }
}
