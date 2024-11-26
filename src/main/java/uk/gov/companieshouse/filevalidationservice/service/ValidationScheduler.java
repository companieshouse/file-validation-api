package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.filevalidationservice.models.FileStatus;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;
import uk.gov.companieshouse.filevalidationservice.parser.CsvProcessor;
import uk.gov.companieshouse.filevalidationservice.repositories.FileValidationRepository;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ValidationScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    FileValidationRepository fileValidationRepository;

    private final FileTransferService fileTransferService;


    public ValidationScheduler(FileTransferService fileTransferService,
                               FileValidationRepository fileValidationRepository) {
        this.fileTransferService = fileTransferService;
        this.fileValidationRepository = fileValidationRepository;
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void cronJobSch() {
        LOGGER.info("Scheduler started at : "+ LocalDateTime.now());
        try {
            List<FileValidation> recordsToProcess = fileValidationRepository.findByStatus(FileStatus.PENDING.getLabel());
            LOGGER.info("Total number of files to process : "+ recordsToProcess.size());
            recordsToProcess.forEach(recordToProcess -> {
                LOGGER.info("Processing record with id: "+ recordToProcess.getId());
                try {
                    fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.IN_PROGRESS.getLabel());
                    Optional<FileApi> downloadedFile = fileTransferService.get(recordToProcess.getFileId());
                    CsvProcessor csvProcessor = new CsvProcessor(downloadedFile.get().getBody());
                    boolean isValidFile = csvProcessor.parseRecords();
                    if(isValidFile){
                        //upload to chips S3
                        fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.COMPLETED.getLabel());
                    }
                        fileValidationRepository.updateStatusById(recordToProcess.getId(), FileStatus.ERROR.getLabel());
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
            });

        }catch (Exception e){
            LOGGER.error("Error : "+e.getMessage());
        }
        LOGGER.info("Scheduler finished at : "+ LocalDateTime.now());
    }
}
