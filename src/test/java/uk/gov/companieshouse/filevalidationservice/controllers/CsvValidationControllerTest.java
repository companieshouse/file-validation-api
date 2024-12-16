package uk.gov.companieshouse.filevalidationservice.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.filevalidationservice.exception.BadRequestRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.FileUploadException;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class CsvValidationControllerTest {

    private static final String FILE_ID = "file_id";
    private static final String FILE_NAME = "file_name";

    @Mock
    FileTransferService fileTransferService;

    @InjectMocks
    CsvValidationController csvValidationController;


    @Test
    void testDownloadFileReturnsFileAndStatus200() {
        // Given
        FileApi downloadedFile = new FileApi(FILE_NAME, "Hello world".getBytes(), ".csv", 100, ".csv");

        // When
        when(fileTransferService.get(FILE_ID)).thenReturn(Optional.of(downloadedFile));
        var response = csvValidationController.downloadFile(FILE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(downloadedFile, response.getBody());
    }

    @Test
    void testDownloadFileReturns404WhenNoFileFound() {
        // When
        when(fileTransferService.get(FILE_ID)).thenReturn(Optional.empty());
        var response = csvValidationController.downloadFile(FILE_ID);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDownloadFileReturns500WhenErrorOccurs() {
        // When
        when(fileTransferService.get(FILE_ID)).thenThrow(new RuntimeException("Error downloading"));
        var response = csvValidationController.downloadFile(FILE_ID);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error downloading", response.getBody());
    }

    @Test
    void testUploadFileReturnsIdAndStatus200(){
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        // When
        String id = "123";
        when(fileTransferService.upload(any(),any())).thenReturn(id);
        var response = csvValidationController.uploadFile(file, metaData);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void testUploadFileFails(){
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";
        when(fileTransferService.upload(any(),any())).thenThrow(new InternalServerErrorRuntimeException("ERROR uploading"));
        Exception thrown = assertThrows(
                InternalServerErrorRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("ERROR"));
    }

    @Test
    void testUploadEmptyFileReturnsStatus400() {
        // Given
        MultipartFile file = new MockMultipartFile("abc","fileName", "text/csv", new byte[0] );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        Exception thrown = assertThrows(
                BadRequestRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("Please upload a valid CSV file"));
    }

    @Test
    void testUploadInvalidFileContentReturnsStatus400(){
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "", "Hello world".getBytes() );
            String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

            Exception thrown = assertThrows(
                    BadRequestRuntimeException.class,
                    () -> csvValidationController.uploadFile(file, metaData)
            );
            assertTrue(thrown.getMessage().contains("Please upload a valid CSV file"));
    }

    @Test
    void testUploadInvalidFileReturnsStatus500(){
        // Given
        MultipartFile file = new MockMultipartFile("abc", "filename", "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        when(fileTransferService.upload(any(),any())).thenThrow(new FileUploadException("Please upload a valid CSV file"));
        Exception thrown = assertThrows(
                InternalServerErrorRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("Please upload a valid CSV file"));
    }
}
