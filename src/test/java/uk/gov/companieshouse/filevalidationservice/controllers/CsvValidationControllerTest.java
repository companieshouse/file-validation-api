package uk.gov.companieshouse.filevalidationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.filevalidationservice.exception.BadRequestRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.FileUploadException;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.models.FileMetaData;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class CsvValidationControllerTest {

    @Mock
    FileTransferService fileTransferService;

    @Mock
    Tika tika;

    @InjectMocks
    CsvValidationController csvValidationController;

    @Test
    void testUploadFileReturnsIdAndStatus200() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", "Test.csv", "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        // When
        String id = "123";
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("text/csv");
        when(fileTransferService.upload(any(),any())).thenReturn(id);
        var response = csvValidationController.uploadFile(file, metaData);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void testUploadFileFails() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        // When
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("text/csv");
        when(fileTransferService.upload(any(),any())).thenThrow(new InternalServerErrorRuntimeException("ERROR uploading"));

        // Then
        Exception thrown = assertThrows(
                InternalServerErrorRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("ERROR"));
    }

    @Test
    void testUploadIncorrectMetaDataReturnsStatus400() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc","fileName", "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"toLocation\":\"S3:abc\"}";

        // When
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("text/csv");
        var objectMapper = new ObjectMapper();
        var fileMetaData = objectMapper.readValue(metaData, FileMetaData.class);

        // Then
        Exception thrown = assertThrows(
                BadRequestRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("Please provide a valid metadata: " + fileMetaData));
    }

    @Test
    void testUploadInvalidFileContentReturnsStatus400() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", "test.png", "", "Hello world".getBytes() );
            String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("image/png");

        // Then
        Exception thrown = assertThrows(
                BadRequestRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("Please upload a valid CSV file"));
    }

    @Test
    void testUploadInvalidFileReturnsStatus500() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", "filename", "text/csv", "Hello world".getBytes() );
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        // When
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("text/csv");
        when(fileTransferService.upload(any(),any())).thenThrow(new FileUploadException("Please upload a valid CSV file"));

        // Then
        Exception thrown = assertThrows(
                InternalServerErrorRuntimeException.class,
                () -> csvValidationController.uploadFile(file, metaData)
        );
        assertTrue(thrown.getMessage().contains("Please upload a valid CSV file"));
    }
}
