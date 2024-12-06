package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
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

    @Scheduled(cron = "${amlData.fileValidation.cron}")
    public void processFiles() {
        LOGGER.info("Scheduler started at : "+ LocalDateTime.now());
        try {
            List<FileValidation> recordsToProcess = fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel());
            LOGGER.info("Total number of files to process : "+ recordsToProcess.size());
            recordsToProcess.forEach(recordToProcess -> {
                try {
                    LOGGER.info("Processing record with id: "+ recordToProcess.getId());
                    fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.IN_PROGRESS.getLabel());
                    Optional<FileApi> downloadedFile = fileTransferService.get(recordToProcess.getFileId());
                    boolean isValidFile = csvProcessor.parseRecords(downloadedFile.get().getBody());
                    if(isValidFile){
                        s3UploadClient.uploadFile(downloadedFile.get().getBody(),
                                recordToProcess.getFileName(),
                                recordToProcess.getToLocation());
                        fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.COMPLETED.getLabel());
                    }else {
                        s3UploadClient.uploadFileOnError(downloadedFile.get().getBody(), recordToProcess.getFileName(),
                                recordToProcess.getToLocation());
                        fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.ERROR.getLabel());
                    }
                } catch (Exception e) {
                    LOGGER.error(String.format("Error while running scheduler %s, with record id %s", e.getMessage(), recordToProcess.getFileId()));
                    // TODO: Update the status' when upload and download fail to allow the files to be retried at a later date
                    fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.ERROR.getLabel());
                }
            });
        }catch (Exception e){
            LOGGER.error(String.format("Error getting records to process %s", e.getMessage()));
        }
        LOGGER.info("Scheduler finished at : "+ LocalDateTime.now());
    }
}
