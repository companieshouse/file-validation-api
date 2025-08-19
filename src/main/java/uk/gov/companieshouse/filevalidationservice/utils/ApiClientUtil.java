package uk.gov.companieshouse.filevalidationservice.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;

@Component
public class ApiClientUtil {

    private InternalApiClient internalApiClient;

    public ApiClientUtil(InternalApiClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

    public InternalApiClient getInternalApiClient( final String fileTransferApiUrl ) {
        internalApiClient.setBasePath( fileTransferApiUrl );
        return internalApiClient;
    }

}