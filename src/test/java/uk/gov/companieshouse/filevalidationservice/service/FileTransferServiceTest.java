package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferEndpoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Supplier;

@ExtendWith( MockitoExtension.class )
class FileTransferServiceTest {

    private final static String TEST_FILE_ID = "test-file-id";
    private final static String TEST_FILE_NAME = "test-file-name";

    @Mock
    private FileTransferEndpoint fileTransferEndpoint;

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
    void testGetFile() throws ApiErrorResponseException, URIValidationException {
        // given
        var data = "Hello World!".getBytes();
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        FileApi fileApi = new FileApi(TEST_FILE_NAME, data, "mimeType", 100, "extension");
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);
        ApiResponse<FileApi> downloadResponse = new ApiResponse<>(200, null, fileApi);

        setupRetryStrategy();

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        when(fileTransferEndpoint.download(TEST_FILE_ID)).thenReturn(downloadResponse);
        Optional<FileApi> maybeFile = fileTransferService.get(TEST_FILE_ID);

        // then
        assertTrue(maybeFile.isPresent());
        assertThat(maybeFile.get().getFileName(), is(equalTo(TEST_FILE_NAME)));
        assertThat(maybeFile.get().getBody(), is(equalTo("Hello World!".getBytes())));

    }

    @Test
    void testGetFileNotFound() throws ApiErrorResponseException, URIValidationException {
        // given
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(404, null, null);

        setupRetryStrategy();

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        Optional<FileApi> maybeFile = fileTransferService.get(TEST_FILE_ID);

        // then
        assertTrue(maybeFile.isEmpty());
    }

    @Test
    void testThrowRetryExceptionWhenFileNotScanned() throws ApiErrorResponseException, URIValidationException {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.NOT_SCANNED, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);

        setupRetryStrategy();

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);

        // then
        assertThrows(RetryException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testThrowsRuntimeExceptionWhenAPIErrorGettingDetails() throws ApiErrorResponseException, URIValidationException {
        // given
        setupRetryStrategy();
        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenThrow(mock(ApiErrorResponseException.class));
        // then
        assertThrows(RuntimeException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testThrowsRuntimeExceptionWhenAPIErrorGettingFile() throws ApiErrorResponseException, URIValidationException {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);

        setupRetryStrategy();

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        when(fileTransferEndpoint.download(TEST_FILE_ID)).thenThrow(mock(ApiErrorResponseException.class));

        // then
        assertThrows(RuntimeException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }
}
