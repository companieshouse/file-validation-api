package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.filevalidationservice.models.FileStatus;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;
import uk.gov.companieshouse.filevalidationservice.parser.CsvProcessor;
import uk.gov.companieshouse.filevalidationservice.repositories.FileValidationRepository;
import uk.gov.companieshouse.filevalidationservice.rest.S3UploadClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationSchedulerTest {

    private final static String TEST_FILE_NAME = "test.csv";

    @Mock
    private FileTransferService fileTransferService;
    @Mock
    private FileValidationRepository fileValidationRepository;
    @Mock
    private S3UploadClient s3UploadClient;
    @Mock
    CsvProcessor csvProcessor;
    @InjectMocks
    private ValidationScheduler scheduler;


    @Test
    void testNoPendingFiles() {
        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.emptyList());

        scheduler.cronJobSch();

        verify(fileValidationRepository).findByStatus(FileStatus.PENDING.getLabel());
        verifyNoInteractions(fileTransferService, s3UploadClient);
    }

    @Test
    void testSuccessfulFileProcessing() throws IOException {
        FileValidation file = createFileValidation("1", "file1", "test.csv", "s3://location");


        var data = "Hello World!".getBytes();
        FileApi fileApi = new FileApi(TEST_FILE_NAME, data, "mimeType", 100, "extension");

        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));
        when(csvProcessor.parseRecords(any()))
                .thenReturn(true);
        doNothing().when(s3UploadClient).uploadFile(data, file.getFileName(), file.getFromLocation());
        doNothing().when(s3UploadClient).uploadFileOnError(data, file.getFileName(), file.getFromLocation());

        scheduler.cronJobSch();

        verifySuccessfulProcessing(file, fileApi);
    }

    @Test
    void testInvalidFileProcessing() throws IOException {
        FileValidation file = createFileValidation("1", "file1", "test.csv", "s3://location");
        FileApi fileApi = createFileApi("invalid");

        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));

        scheduler.cronJobSch();

        verifyErrorProcessing(file, fileApi);
    }

    @Test
    void testFileTransferServiceError() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", "s3://location");

        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(anyString()))
                .thenThrow(new RuntimeException("Transfer failed"));

        scheduler.cronJobSch();

        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.IN_PROGRESS.getLabel());
        verifyNoMoreInteractions(s3UploadClient);
    }

    private FileValidation createFileValidation(String id, String fileId, String fileName, String location) {
        FileValidation file = new FileValidation();
        file.setId(id);
        file.setFileId(fileId);
        file.setFileName(fileName);
        file.setToLocation(location);
        return file;
    }

    private FileApi createFileApi(String content) {
        FileApi fileApi = new FileApi();
        fileApi.setBody(content.getBytes());
        return fileApi;
    }

    private void verifySuccessfulProcessing(FileValidation file, FileApi fileApi) {
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.IN_PROGRESS.getLabel());
//        verify(s3UploadClient).uploadFile(fileApi.getBody(), file.getFileName(), file.getToLocation());
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.COMPLETED.getLabel());
    }

    private void verifyErrorProcessing(FileValidation file, FileApi fileApi) {
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.IN_PROGRESS.getLabel());
        verify(s3UploadClient).uploadFileOnError(fileApi.getBody(), file.getFileName(), file.getToLocation());
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.ERROR.getLabel());
    }
}