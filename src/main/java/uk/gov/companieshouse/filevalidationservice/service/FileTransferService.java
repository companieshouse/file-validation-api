package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;
import uk.gov.companieshouse.filevalidationservice.models.File;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferEndpoint;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

            HttpStatus status = HttpStatus.resolve(response.getStatusCode());
            switch (Objects.requireNonNull(status)) {
                case NOT_FOUND:
                    return Optional.empty();
                case OK:
                    return Optional.ofNullable(response.getData());
                default:
                    var message = "Unexpected response status from file transfer api when getting file details.";
                    LOGGER.errorContext(id, message, null, Map.of(
                            "expected", "200 or 404",
                            "status", response.getStatusCode()
                    ));
                    throw new RuntimeException(message);
            }
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<File> get(String id) {
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
            var file = new File(id, details.get().getName(), response.getData().getBody());
            return Optional.of(file);
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
