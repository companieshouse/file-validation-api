package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.filevalidationservice.rest.FileTransferEndpoint;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class FileTransferService {

    private final FileTransferEndpoint fileTransferEndpoint;

    private static final Logger LOG = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    public FileTransferService( final FileTransferEndpoint fileTransferEndpoint ) {
        this.fileTransferEndpoint = fileTransferEndpoint;
    }

    /*
    TODO:
    fileTransferEndpoint can be used to call endpoints in the FileTransferService.

    Implement methods here that call the FileTransferService endpoints via fileTransferEndpoint.
    These methods can also implement the logic to handle the endpoint responses.

    This might be a useful resource, because it looks like it implements a lot of the logic already:
    https://github.com/companieshouse/account-validator-api/blob/main/src/main/java/uk/gov/companieshouse/account/validator/service/file/transfer/FileTransferService.java
     */

}
