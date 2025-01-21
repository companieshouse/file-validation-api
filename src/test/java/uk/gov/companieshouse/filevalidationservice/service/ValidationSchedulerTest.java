package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.filevalidationservice.exception.CSVDataValidationException;
import uk.gov.companieshouse.filevalidationservice.exception.DownloadAvStatusException;
import uk.gov.companieshouse.filevalidationservice.exception.FileDownloadException;
import uk.gov.companieshouse.filevalidationservice.exception.S3UploadException;
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
        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(), FileStatus.ERROR.getLabel()))
                .thenReturn(Collections.emptyList());

        scheduler.processFiles();

        verify(fileValidationRepository).findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(), FileStatus.ERROR.getLabel());
        verifyNoInteractions(fileTransferService, s3UploadClient);
    }

    @Test
    void testSuccessfulFileProcessing() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(), FileStatus.ERROR.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));
        doNothing().when(csvProcessor).parseRecords(any());
        doNothing().when(s3UploadClient).uploadFile(fileApi.getBody(), file.getFileName(), file.getToLocation());

        scheduler.processFiles();

        verifySuccessfulProcessing(file, fileApi);
    }

    @Test
    void testFirstFileErrorDownloadingSecondFileSuccessfulProcessing() {
        FileValidation file1 = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);
        FileValidation file2 = createFileValidation("2", "file2", "test2.csv", FILE_LOCATION);


        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(),FileStatus.ERROR.getLabel()))
                .thenReturn(Arrays.asList(file1, file2));
        when(fileTransferService.get(file1.getFileId()))
                .thenThrow(FileDownloadException.class);
        when(fileTransferService.get(file2.getFileId()))
                .thenReturn(Optional.of(fileApi));
        doNothing().when(csvProcessor).parseRecords(any());
        doNothing().when(s3UploadClient).uploadFile(fileApi.getBody(), file2.getFileName(), file2.getToLocation());

        scheduler.processFiles();

        verify(fileValidationRepository).updateStatusAndErrorMessageById(eq(file1.getId()), eq(FileStatus.DOWNLOAD_ERROR.getLabel()), any(), any(), eq("System"));
        verifySuccessfulProcessing(file2, fileApi);
    }

    @Test
    void testInvalidFileProcessing() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(), FileStatus.ERROR.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));
        doThrow(CSVDataValidationException.class).when(csvProcessor).parseRecords(any());
        doNothing().when(s3UploadClient).uploadFileOnError(fileApi.getBody(), file.getFileName(), file.getToLocation());

        scheduler.processFiles();

        verifyErrorProcessing(file, fileApi);
    }

    @Test
    void testFileTransferServiceError() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(), FileStatus.ERROR.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(anyString()))
                .thenThrow(new FileDownloadException("Error downloading"));

        scheduler.processFiles();

        verify(fileValidationRepository).updateStatusById(eq(file.getId()), eq(FileStatus.IN_PROGRESS.getLabel()), any(), eq("System"));
        verifyNoMoreInteractions(s3UploadClient);
        verify(fileValidationRepository).updateStatusAndErrorMessageById(eq(file.getId()), eq(FileStatus.DOWNLOAD_ERROR.getLabel()), any(), any(), eq("System"));
    }

    @Test
    void testFileUploadError() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(),FileStatus.ERROR.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenReturn(Optional.of(fileApi));
        doNothing().when(csvProcessor).parseRecords(any());
        doThrow(S3UploadException.class).when(s3UploadClient).uploadFile(fileApi.getBody(), file.getFileName(), file.getToLocation());

        scheduler.processFiles();

        verify(fileValidationRepository).updateStatusAndErrorMessageById(eq(file.getId()), eq(FileStatus.UPLOAD_ERROR.getLabel()), any(), any(), eq("System"));
    }

    @Test
    void testDownloadErrorAvStatus() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);


        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel(), FileStatus.ERROR.getLabel()))
                .thenReturn(Collections.singletonList(file));
        when(fileTransferService.get(file.getFileId()))
                .thenThrow(DownloadAvStatusException.class);

        scheduler.processFiles();

        verify(fileValidationRepository).updateStatusAndErrorMessageById(eq(file.getId()), eq(FileStatus.DOWNLOAD_AV_ERROR.getLabel()), any(), any(), eq("System"));
    }

    @Test
    void testUnknownError() {
        FileValidation file = createFileValidation("1", "file1", "test.csv", FILE_LOCATION);

        when(fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel()))
                .thenReturn(Collections.singletonList(file));
        doThrow(RuntimeException.class).when(fileTransferService).get(file.getFileId());

        scheduler.processFiles();

        verifyNoInteractions(csvProcessor);
        verifyNoMoreInteractions(s3UploadClient);
    }

    @Test
    void testErrorGettingRecords() {
        doThrow(RuntimeException.class).when(fileValidationRepository).findByStatuses(FileStatus.PENDING.getLabel());

        scheduler.processFiles();

        verifyNoInteractions(fileTransferService);
        verifyNoInteractions(csvProcessor);
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
        verify(fileValidationRepository).updateStatusById(eq(file.getId()), eq(FileStatus.IN_PROGRESS.getLabel()), any(), eq("System"));
        verify(s3UploadClient).uploadFile(fileApi.getBody(), file.getFileName(), file.getToLocation());
        verify(fileValidationRepository).updateStatusById(eq(file.getId()), eq(FileStatus.COMPLETED.getLabel()), any(), eq("System"));
    }

    private void verifyErrorProcessing(FileValidation file, FileApi fileApi) {
        verify(fileValidationRepository).updateStatusById(eq(file.getId()), eq(FileStatus.IN_PROGRESS.getLabel()), any(), eq("System"));
        verify(s3UploadClient).uploadFileOnError(fileApi.getBody(), file.getFileName(), file.getToLocation());
        verify(fileValidationRepository).updateStatusAndErrorMessageById(eq(file.getId()), eq(FileStatus.VALIDATION_ERROR.getLabel()), any(), any(), eq("System"));
    }
}