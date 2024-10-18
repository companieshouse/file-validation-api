package uk.gov.companieshouse.filevalidationservice.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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


}
