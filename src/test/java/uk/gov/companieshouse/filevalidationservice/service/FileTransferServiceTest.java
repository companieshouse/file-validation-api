package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
import uk.gov.companieshouse.filevalidationservice.exception.FileDownloadException;
import uk.gov.companieshouse.filevalidationservice.models.FileMetaData;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;
import uk.gov.companieshouse.filevalidationservice.repositories.FileValidationRepository;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferEndpoint;

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

@ExtendWith( MockitoExtension.class )
class FileTransferServiceTest {

    private final static String TEST_FILE_ID = "test-file-id";
    private final static String TEST_FILE_NAME = "test-file-name";

    @Mock
    private FileTransferEndpoint fileTransferEndpoint;

    @Mock
    private FileValidationRepository fileValidationRepository;

    @InjectMocks
    private FileTransferService fileTransferService;

    @Test
    void testGetFile() throws ApiErrorResponseException, URIValidationException {
        // given
        var data = "Hello World!".getBytes();
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        FileApi fileApi = new FileApi(TEST_FILE_NAME, data, "mimeType", 100, "extension");
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);
        ApiResponse<FileApi> downloadResponse = new ApiResponse<>(200, null, fileApi);

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
    void testGetFileNotCleanThrowsException() throws ApiErrorResponseException, URIValidationException {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.INFECTED, "contentType", 100, TEST_FILE_NAME, "createdOn", null);
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        // then
        assertThrows(FileDownloadException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testGetFileNotFound() throws ApiErrorResponseException, URIValidationException {
        // given
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(404, null, null);

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        Optional<FileApi> maybeFile = fileTransferService.get(TEST_FILE_ID);

        // then
        assertTrue(maybeFile.isEmpty());
    }


    @Test
    void testThrowsRuntimeExceptionWhenAPIErrorGettingDetails() throws ApiErrorResponseException, URIValidationException {
        // given
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

        // when
        when(fileTransferEndpoint.details(TEST_FILE_ID)).thenReturn(detailsResponse);
        when(fileTransferEndpoint.download(TEST_FILE_ID)).thenThrow(mock(ApiErrorResponseException.class));

        // then
        assertThrows(RuntimeException.class, () -> fileTransferService.get(TEST_FILE_ID));
    }

    @Test
    void testUploadFile() throws ApiErrorResponseException, URIValidationException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );

        // when
        IdApi idApi = new IdApi("123");
        var fileValidationRecord = new FileValidation();
        fileValidationRecord.setId("084905471517321155");
        fileValidationRecord.setFileId("93c1a3f1-6c8f-4dbd-973d-d7c42b1bb525");
        when(fileTransferEndpoint.upload(any())).thenReturn(new ApiResponse<>(200, null, idApi));
        when(fileValidationRepository.insert((FileValidation) any())).thenReturn(fileValidationRecord);
        var response = fileTransferService.upload(file, new FileMetaData("Test file", "HMRC", "S3:HMRC" ));

        // then
        assertEquals("084905471517321155", response);
    }

    @Test
    void testUploadFileThrowsApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        // Given
        MultipartFile file = new MockMultipartFile("abc", null, "text/csv", "Hello world".getBytes() );

        // when
        when(fileTransferEndpoint.upload(any())).thenThrow(mock(ApiErrorResponseException.class));

        // then
        FileMetaData fileMetaData = new FileMetaData();
        assertThrows(RuntimeException.class, () -> fileTransferService.upload(file, fileMetaData));
    }

    @Test
    void testUploadFileThrowsIOExceptionException() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        // then

        FileMetaData fileMetaData = new FileMetaData();
        assertThrows(RuntimeException.class, () -> fileTransferService.upload(mockFile, fileMetaData));
    }
}
