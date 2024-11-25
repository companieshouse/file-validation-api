package uk.gov.companieshouse.filevalidationservice.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FileTransferApiClientTest {

    private static final String FILE_TRANSFER_API_URL = "https://mock-api.com/file";
    private static final String FILE_TRANSFER_API_KEY = "mock-api-key";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    FileTransferApiClient fileTransferApiClient;

    @BeforeEach
    void setup() {
        fileTransferApiClient = new FileTransferApiClient(restTemplate, FILE_TRANSFER_API_URL, FILE_TRANSFER_API_KEY);
    }


    @Test
    void testReturns201WhenUploadSucceeded() throws IOException {
        // given
        byte[] fileContent = "Hello world".getBytes();
        ResponseEntity<String> mockResponse = new ResponseEntity<>("1234", HttpStatus.CREATED);
        // when
        when(multipartFile.getBytes()).thenReturn(fileContent);
        when(multipartFile.getOriginalFilename()).thenReturn("TestFile.csv");
        when(restTemplate.postForEntity(eq(FILE_TRANSFER_API_URL), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponse);
        //then
        ResponseEntity<String> response = fileTransferApiClient.upload(multipartFile);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("1234", response.getBody());
    }

    @Test
    void testThrowsIOExceptionWhenUploadingIfGetBytesFails() throws IOException {
        // when
        when(multipartFile.getBytes()).thenThrow(mock(IOException.class));
        // then
        assertThrows(IOException.class, () -> fileTransferApiClient.upload(multipartFile));
    }

    @Test
    void testReturns200AndFileDetailsWhenDetailsRetrieved() {
        // given
        String fileID = "12345";
        FileDetailsApi fileDetails = new FileDetailsApi(fileID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, "TestFile.csv", "createdOn", null);
        ResponseEntity<FileDetailsApi> mockResponse = new ResponseEntity<>(fileDetails, HttpStatus.OK);
        String detailsUrl = String.format("%s/%s", FILE_TRANSFER_API_URL, fileID);
        // when
        when(restTemplate.exchange(eq(detailsUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(FileDetailsApi.class))).thenReturn(mockResponse);
        // then
        ResponseEntity<FileDetailsApi> response = fileTransferApiClient.details(fileID);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fileID, response.getBody().getId());
    }

    @Test
    void testReturns200AndFileContentWhenFileDownloaded() {
        // given
        String fileID = "12345";
        byte[] fileContent = "hello World".getBytes();
        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(fileContent, HttpStatus.OK);
        String downloadUrl = String.format("%s/%s/download", FILE_TRANSFER_API_URL, fileID);
        // when
        when(restTemplate.exchange(eq(downloadUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class))).thenReturn(mockResponse);
        // then
        ResponseEntity<byte[]> response = fileTransferApiClient.download(fileID);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fileContent, response.getBody());
    }
}
