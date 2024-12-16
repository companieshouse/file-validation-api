package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.filevalidationservice.exception.FileDownloadException;
import uk.gov.companieshouse.filevalidationservice.exception.FileUploadException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.filevalidationservice.exception.DownloadAvStatusException;
import uk.gov.companieshouse.filevalidationservice.models.FileMetaData;
import uk.gov.companieshouse.filevalidationservice.models.FileStatus;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;
import uk.gov.companieshouse.filevalidationservice.repositories.FileValidationRepository;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferEndpoint;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

@Service
public class FileTransferService {

    private final FileTransferEndpoint fileTransferEndpoint;

    private final FileValidationRepository fileValidationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    public FileTransferService(final FileTransferEndpoint fileTransferEndpoint,
                                FileValidationRepository fileValidationRepository) {
        this.fileTransferEndpoint = fileTransferEndpoint;
        this.fileValidationRepository = fileValidationRepository;

    }

    private Optional<FileDetailsApi> getFileDetails(final String id) {
        try {
            ApiResponse<FileDetailsApi> response = fileTransferEndpoint.details(id);
            return Optional.ofNullable(response.getData());
        } catch ( URIValidationException e) {
            throw new RuntimeException(e);
        } catch (ApiErrorResponseException e) {
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            }
            var message = "Unexpected response status from file transfer api when getting file details.";
            LOGGER.errorContext(id, message, null, Map.of(
                    "expected", "200",
                    "status", e.getStatusCode()
            ));
            throw new RuntimeException(e.getMessage());
        }
    }

    public Optional<FileApi> get(String id) {
        try{
            Optional<FileDetailsApi> details = getFileDetails(id);

            // No file with id
            if (details.isEmpty()) {
                return Optional.empty();
            } else if (!details.get().getAvStatusApi().equals(AvStatusApi.CLEAN)) {
                throw new DownloadAvStatusException(String.format("Av Status is not clean, current status is %s for file %s", details.get().getAvStatusApi(), id));
            }

            ApiResponse<FileApi> response = fileTransferEndpoint.download(id);
            return Optional.of(response.getData());
        } catch (Exception e) {
            throw new FileDownloadException(e.getMessage());
        }
    }
    public String upload(MultipartFile file, FileMetaData fileMetaData) throws FileUploadException {
        try {
            var fileApi = new FileApi(fileMetaData.getFileName(),file.getBytes(),"text/csv",(int)file.getSize(),".csv");
            var uploadResponse =  fileTransferEndpoint.upload(fileApi);
            var fileValidation = setFileToValidate(uploadResponse.getData().getId(), fileMetaData);
            var insertedRecord = fileValidationRepository.insert(fileValidation);
            return insertedRecord.getId();
        } catch (Exception e) {
            LOGGER.error("Error uploading the file : " + e.getMessage());
            throw new FileUploadException(e.getMessage());
        }
    }

    private FileValidation setFileToValidate(String fileId,FileMetaData fileMetaData) {
        FileValidation fileValidation = new FileValidation();
        fileValidation.setId(autoGenerateId());
        fileValidation.setFileId(fileId);
        fileValidation.setCreatedAt(LocalDateTime.now());
        fileValidation.setCreatedBy("System");
        fileValidation.setStatus(FileStatus.PENDING.getLabel());
        fileValidation.setFileName(fileMetaData.getFileName());
        fileValidation.setFromLocation(fileMetaData.getFromLocation());
        fileValidation.setToLocation(fileMetaData.getToLocation());
        return fileValidation;
    }

    private String autoGenerateId() {
        var random = new SecureRandom();
        var values = new byte[4];
        random.nextBytes(values);
        var rand = String.format("%010d", random.nextInt(Integer.MAX_VALUE));
        var time = String.format("%08d", Calendar.getInstance().getTimeInMillis() / 100000L);
        return rand + time;
    }
}
