package uk.gov.companieshouse.filevalidationservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
import uk.gov.companieshouse.filevalidationservice.utils.ApiClientUtil;

@Component
public class FileTransferEndpoint {

    @Value( "${file.transfer.api.url}" )
    private String fileTransferApiUrl;

    private final ApiClientUtil apiClientUtil;

    @Autowired
    public FileTransferEndpoint( final ApiClientUtil apiClientUtil ) {
        this.apiClientUtil = apiClientUtil;
    }

    public ApiResponse<IdApi> upload( final FileApi fileApi ) throws ApiErrorResponseException, URIValidationException {
        return apiClientUtil.getInternalApiClient(fileTransferApiUrl)
                .privateFileTransferResourceHandler()
                .upload( fileApi )
                .execute();
    }

    public ApiResponse<FileDetailsApi> details( final String fileId ) throws ApiErrorResponseException, URIValidationException {
        return apiClientUtil.getInternalApiClient(fileTransferApiUrl)
                .privateFileTransferResourceHandler()
                .details( fileId )
                .execute();
    }

    public ApiResponse<FileApi> download( final String fileId ) throws ApiErrorResponseException, URIValidationException {
        return apiClientUtil.getInternalApiClient(fileTransferApiUrl)
                .privateFileTransferResourceHandler()
                .download( fileId )
                .execute();
    }

    public ApiResponse<Void> delete( final String fileId ) throws ApiErrorResponseException, URIValidationException {
        return apiClientUtil.getInternalApiClient(fileTransferApiUrl)
                .privateFileTransferResourceHandler()
                .delete( fileId )
                .execute();
    }

 }
