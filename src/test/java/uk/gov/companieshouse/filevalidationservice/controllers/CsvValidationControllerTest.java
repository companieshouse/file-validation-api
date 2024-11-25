package uk.gov.companieshouse.filevalidationservice.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class CsvValidationControllerTest {

    private static final String FILE_ID = "file_id";

    @Mock
    FileTransferService fileTransferService;

    @InjectMocks
    CsvValidationController csvValidationController;


    @Test
    void testDownloadFileReturnsFileAndStatus200() {
        // Given
        byte[] downloadedFile = "Hello world".getBytes();

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
    void testUploadFileReturnsIdAndStatus201() {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );

        // When
        String id = "123";
        when(fileTransferService.upload(any())).thenReturn(new ResponseEntity<>(id, HttpStatus.CREATED));
        var response = csvValidationController.uploadFile(file);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("123", response.getBody());
    }

    @Test
    void testUploadFileReturnsNullAndStatus500() {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );

        // When
        when(fileTransferService.upload(any())).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        var response = csvValidationController.uploadFile(file);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    @Test
    void testUploadEmptyFileReturnsStatus400() {
        // Given
        MultipartFile file = new MockMultipartFile("abc","", "text/csv", new byte[0] );

        var response = csvValidationController.uploadFile(file);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

        @Test
    void testUploadInvalidFileReturnsStatus400() {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "", "Hello world".getBytes() );

        var response = csvValidationController.uploadFile(file);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
