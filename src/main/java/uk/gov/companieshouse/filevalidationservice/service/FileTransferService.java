package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferApiClient;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;

import java.util.Optional;


@Service
public class FileTransferService {

    private final FileTransferApiClient fileTransferApiClient;
    private final RetryService retryService;

    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    public FileTransferService(FileTransferApiClient fileTransferApiClient,
                                RetryService retryService) {
        this.fileTransferApiClient = fileTransferApiClient;
        this.retryService = retryService;
    }

    private Optional<FileDetailsApi> getFileDetails(final String id) {
        try {
            ResponseEntity<FileDetailsApi> response = fileTransferApiClient.details(id);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            LOGGER.error("Unexpected response status from file transfer api when getting file details.");
            throw e;
        }
    }

    public Optional<byte[]> get(String id) {
        Optional<FileDetailsApi> details = retryService.attempt(() -> {
            Optional<FileDetailsApi> maybeFileDetails;
            maybeFileDetails = getFileDetails(id);
            var stillAwaitingScan = maybeFileDetails
                    .map(fileDetailsApi -> fileDetailsApi.getAvStatusApi().equals(AvStatusApi.NOT_SCANNED))
                    .orElse(false);
            if (stillAwaitingScan) {
                // AvScan has still not been completed. Attempt to retry
                LOGGER.debugContext(id, "File still awaiting scan. Retrying.", null);
                throw new RetryException();
            }
            return maybeFileDetails;
        });

        // No file with id
        if (details.isEmpty()) {
            return Optional.empty();
        }
        try{
            ResponseEntity<byte[]> response = fileTransferApiClient.download(id);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            LOGGER.error("Unexpected response status from file transfer api when downloading file.");
            throw e;
        }
    }
    public ResponseEntity<String> upload(MultipartFile file) {
        try {
            return fileTransferApiClient.upload(file);
        } catch (HttpClientErrorException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
