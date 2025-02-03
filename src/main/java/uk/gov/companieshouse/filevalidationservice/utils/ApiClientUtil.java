package uk.gov.companieshouse.filevalidationservice.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;

@Component
public class ApiClientUtil {

    @Autowired
    private InternalApiClient internalApiClient;

    public InternalApiClient getInternalApiClient( final String fileTransferApiUrl ) {
        internalApiClient.setBasePath( fileTransferApiUrl );
        return internalApiClient;
    }

}