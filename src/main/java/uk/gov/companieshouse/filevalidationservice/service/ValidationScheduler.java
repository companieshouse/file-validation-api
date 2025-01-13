package uk.gov.companieshouse.filevalidationservice.service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ValidationScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    private static final String SYSTEM = "System";

    FileValidationRepository fileValidationRepository;
    private final FileTransferService fileTransferService;
    private final S3UploadClient s3UploadClient;
    private final CsvProcessor csvProcessor;

    public ValidationScheduler(FileTransferService fileTransferService,
                               FileValidationRepository fileValidationRepository,
                               S3UploadClient s3UploadClient,
                               CsvProcessor csvProcessor) {
        this.fileTransferService = fileTransferService;
        this.fileValidationRepository = fileValidationRepository;
        this.s3UploadClient = s3UploadClient;
        this.csvProcessor = csvProcessor;
    }

    @SchedulerLock(
            name = "ValidationScheduler_processFiles",
            lockAtLeastFor = "${amlData.fileValidation.scheduler.lock-at-least-for}", // Hold lock for at least XX minutes
            lockAtMostFor = "${amlData.fileValidation.scheduler.lock-at-most-for}"  // Hold lock for maximum XX minutes
    )
    @Scheduled(cron = "${amlData.fileValidation.cron}")
    public void processFiles() {
        LOGGER.info("Scheduler started at : "+ LocalDateTime.now());
        try {
            List<FileValidation> recordsToProcess = fileValidationRepository.findByStatuses(FileStatus.PENDING.getLabel(), FileStatus.DOWNLOAD_ERROR.getLabel(), FileStatus.UPLOAD_ERROR.getLabel());
            LOGGER.info("Total number of files to process : "+ recordsToProcess.size());
            recordsToProcess.forEach(recordToProcess -> {
                Optional<FileApi> downloadedFile = Optional.empty();
                try {
                    LOGGER.info("Processing record with id: " + recordToProcess.getId());
                    fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.IN_PROGRESS.getLabel(), LocalDateTime.now(), SYSTEM);
                    downloadedFile = fileTransferService.get(recordToProcess.getFileId());
                    csvProcessor.parseRecords(downloadedFile.get().getBody());
                    s3UploadClient.uploadFile(downloadedFile.get().getBody(),
                            recordToProcess.getFileName(),
                            recordToProcess.getToLocation());
                    fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.COMPLETED.getLabel(), LocalDateTime.now(), SYSTEM);
                } catch (FileDownloadException e) {
                    var errorMessage = String.format("Failed to download file: %s with message %s", recordToProcess.getId(), e.getMessage());
                    LOGGER.error(errorMessage);
                    fileValidationRepository.updateStatusAndErrorMessageById(recordToProcess.getFileId(), FileStatus.DOWNLOAD_ERROR.getLabel(), errorMessage, LocalDateTime.now(), SYSTEM);
                }  catch (DownloadAvStatusException e) {
                    var errorMessage = String.format("Failed to download file: %s with message %s", recordToProcess.getId(), e.getMessage());
                    LOGGER.error(errorMessage);
                    fileValidationRepository.updateStatusAndErrorMessageById(recordToProcess.getFileId(), FileStatus.DOWNLOAD_AV_ERROR.getLabel(), errorMessage, LocalDateTime.now(), SYSTEM);
                } catch (S3UploadException e) {
                    var errorMessage = String.format("Failed to upload to S3 for file: %s with message %s", recordToProcess.getId(), e.getMessage());
                    LOGGER.error(errorMessage);
                    fileValidationRepository.updateStatusAndErrorMessageById(recordToProcess.getFileId(), FileStatus.UPLOAD_ERROR.getLabel(), errorMessage, LocalDateTime.now(), SYSTEM);
                } catch (CSVDataValidationException e){
                    var errorMessage = String.format("Failed to validate file: %s with message %s", recordToProcess.getId(), e.getMessage());
                    LOGGER.error(errorMessage);
                    fileValidationRepository.updateStatusAndErrorMessageById(recordToProcess.getFileId(), FileStatus.VALIDATION_ERROR.getLabel(), errorMessage, LocalDateTime.now(), SYSTEM);
                    s3UploadClient.uploadFileOnError(downloadedFile.get().getBody(), recordToProcess.getFileName(),
                            recordToProcess.getToLocation());
                } catch (Exception e) {
                    LOGGER.error(String.format("An unknown error occurred while running scheduler %s, with record id %s", e.getMessage(), recordToProcess.getId()));
                }
            });
        }catch (Exception e){
            LOGGER.error(String.format("Error getting records to process %s", e.getMessage()));
        }
        LOGGER.info("Scheduler finished at : "+ LocalDateTime.now());
    }
}
