package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.BeforeEach;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationSchedulerTest {

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

    FileApi fileApi;
    private final static String TEST_FILE_NAME = "test.csv";
    private final static String FILE_LOCATION = "s3://location";

    @BeforeEach
    void setUp() {
        var data = "Hello World!".getBytes();
        fileApi = new FileApi(TEST_FILE_NAME, data, "mimeType", 100, "extension");
    }
    @Test
    void testNoPendingFiles() {
        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.emptyList());

        scheduler.processFiles();

        verify(fileValidationRepository).findByStatus(FileStatus.PENDING.getLabel());
        verifyNoInteractions(fileTransferService, s3UploadClient);
    }

    @Test
    void testSuccessfulFileProcessing() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));
        when(csvProcessor.parseRecords(any()))
                .thenReturn(true);
        doNothing().when(s3UploadClient).uploadFile(fileApi.getBody(), file.getFileName(), file.getToLocation());

        scheduler.processFiles();

        verifySuccessfulProcessing(file, fileApi);
    }

    @Test
    void testFirstFileErrorDownloadingSecondFileSuccessfulProcessing() {
        FileValidation file1 = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);
        FileValidation file2 = createFileValidation("2", "file2", "test2.csv", FILE_LOCATION);


        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Arrays.asList(file1, file2));
        when(fileTransferService.get(file1.getFileId()))
                .thenThrow(RuntimeException.class);
        when(fileTransferService.get(file2.getFileId()))
                .thenReturn(Optional.of(fileApi));
        when(csvProcessor.parseRecords(any()))
                .thenReturn(true);
        doNothing().when(s3UploadClient).uploadFile(fileApi.getBody(), file2.getFileName(), file2.getToLocation());

        scheduler.processFiles();

        verify(fileValidationRepository).updateStatusById(file1.getId(), FileStatus.ERROR.getLabel());
        verifySuccessfulProcessing(file2, fileApi);
    }

    @Test
    void testInvalidFileProcessing() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));
        when(csvProcessor.parseRecords(any()))
                .thenReturn(false);
        doNothing().when(s3UploadClient).uploadFileOnError(fileApi.getBody(), file.getFileName(), file.getToLocation());

        scheduler.processFiles();

        verifyErrorProcessing(file, fileApi);
    }

    @Test
    void testFileTransferServiceError() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(anyString()))
                .thenThrow(new RuntimeException("Transfer failed"));

        scheduler.processFiles();

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


    private void verifySuccessfulProcessing(FileValidation file, FileApi fileApi) {
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.IN_PROGRESS.getLabel());
        verify(s3UploadClient).uploadFile(fileApi.getBody(), file.getFileName(), file.getToLocation());
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.COMPLETED.getLabel());
    }

    private void verifyErrorProcessing(FileValidation file, FileApi fileApi) {
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.IN_PROGRESS.getLabel());
        verify(s3UploadClient).uploadFileOnError(fileApi.getBody(), file.getFileName(), file.getToLocation());
        verify(fileValidationRepository).updateStatusById(file.getId(), FileStatus.ERROR.getLabel());
    }
}