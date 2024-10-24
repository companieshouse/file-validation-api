package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferEndpoint;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import java.io.IOException;

@Service
public class FileTransferService {

    private final FileTransferEndpoint fileTransferEndpoint;
    private final RetryService retryService;

    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    public FileTransferService( final FileTransferEndpoint fileTransferEndpoint,
                                RetryService retryService ) {
        this.fileTransferEndpoint = fileTransferEndpoint;
        this.retryService = retryService;
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
        Optional<FileDetailsApi> details = retryService.attempt(() -> {
            Optional<FileDetailsApi> maybeFileDetails;
            maybeFileDetails = getFileDetails(id);
            var stillAwaitingScan = maybeFileDetails
                    .map(fileDetailsApi -> fileDetailsApi.getAvStatusApi().equals(AvStatusApi.NOT_SCANNED))
                    .orElse(false);

            LOGGER.debugContext(id, "File still awaiting scan. Retrying.", null);

            if (stillAwaitingScan) {
                // AvScan has still not been completed. Attempt to retry
                throw new RetryException();
            }

            return maybeFileDetails;
        });

        // No file with id
        if (details.isEmpty()) {
            return Optional.empty();
        }
        try{
            ApiResponse<FileApi> response = fileTransferEndpoint.download(id);
            return Optional.of(response.getData());
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw new RuntimeException(e);
        }
    }
    public ApiResponse<IdApi> upload(MultipartFile file) {
        try {
            var fileApi = new FileApi(file.getName(),file.getBytes(),"text/csv",(int)file.getSize(),".csv");
            return fileTransferEndpoint.upload(fileApi);
        } catch (URIValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
