package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferApiClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

@ExtendWith( MockitoExtension.class )
class FileTransferServiceTest {

    private final static String TEST_FILE_ID = "test-file-id";
    private final static String TEST_FILE_NAME = "test-file-name";

    @Mock
    private FileTransferApiClient fileTransferApiClient;

    @Mock
    private RetryService retryService;

    @InjectMocks
    private FileTransferService fileTransferService;

    private void setupRetryStrategy() {
        setupRetryStrategy(null);
    }

    private void setupRetryStrategy(Runnable onRetry) {
        when(retryService.attempt(any())).thenAnswer(a -> {
            for (int i = 0; i < 10; i++) {
                try {
                    return a.getArgument(0, Supplier.class).get();
                } catch (RetryException e) {
                    if (onRetry == null) {
                        throw e;
                    }

                    onRetry.run();
                }
            }

            return null;
        });
    }
    @Test
    void testGetFile() {
        // given
        var data = "Hello World!".getBytes();
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        ResponseEntity<FileDetailsApi> detailsResponse = new ResponseEntity<>(fileDetailsApi, HttpStatus.OK);
        ResponseEntity<byte[]> downloadResponse = new ResponseEntity<>(data, HttpStatus.OK);

        setupRetryStrategy();

        // when
        when(fileTransferApiClient.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        when(fileTransferApiClient.download(TEST_FILE_ID)).thenReturn(downloadResponse);
        Optional<byte[]> maybeFile = fileTransferService.get(TEST_FILE_ID);

        // then
        assertTrue(maybeFile.isPresent());
        assertThat(maybeFile.get(), is(equalTo(data)));
    }

    @Test
    void testGetFileNotFound() {
        // given
        ResponseEntity<FileDetailsApi> detailsResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        setupRetryStrategy();

        // when
        when(fileTransferApiClient.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        Optional<byte[]> maybeFile = fileTransferService.get(TEST_FILE_ID);

        // then
        assertTrue(maybeFile.isEmpty());
    }

    @Test
    void testThrowRetryExceptionWhenFileNotScanned() {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.NOT_SCANNED, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        ResponseEntity<FileDetailsApi> detailsResponse = new ResponseEntity<>(fileDetailsApi, HttpStatus.OK);

        setupRetryStrategy();

        // when
        when(fileTransferApiClient.details(TEST_FILE_ID)).thenReturn(detailsResponse);

        // then
        assertThrows(RetryException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testThrowsRuntimeExceptionWhenAPIErrorGettingDetails() {
        // given
        setupRetryStrategy();
        // when
        when(fileTransferApiClient.details(TEST_FILE_ID)).thenThrow(mock(RestClientException.class));
        // then
        assertThrows(RuntimeException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testThrowsRuntimeExceptionWhenAPIErrorGettingFile() {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        ResponseEntity<FileDetailsApi> detailsResponse = new ResponseEntity<>(fileDetailsApi, HttpStatus.OK);

        setupRetryStrategy();

        // when
        when(fileTransferApiClient.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        when(fileTransferApiClient.download(TEST_FILE_ID)).thenThrow(mock(RestClientException.class));

        // then
        assertThrows(RuntimeException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testUploadFile() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );

        // when
        String id = "123";
        when(fileTransferApiClient.upload(any())).thenReturn(new ResponseEntity<>(id, HttpStatus.CREATED));
        var response = fileTransferService.upload(file);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("123", response.getBody());
    }

    @Test
    void testUploadFileThrowsApiErrorResponseException() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );

        // when
        IdApi idApi = new IdApi("123");
        when(fileTransferApiClient.upload(any())).thenThrow(mock(ApiErrorResponseException.class));

        // then
        assertThrows(RuntimeException.class, () -> fileTransferService.upload(file));
    }
}
