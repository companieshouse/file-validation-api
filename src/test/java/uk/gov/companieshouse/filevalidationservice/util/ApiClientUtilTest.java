package uk.gov.companieshouse.filevalidationservice.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.filevalidationservice.utils.ApiClientUtil;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class ApiClientUtilTest {

    @Mock
    private InternalApiClient internalApiClient;

    @InjectMocks
    private ApiClientUtil apiClientUtil;

    @Test
    void getInternalApiClientSetsTheInternalBasePathToSpecifiedPath(){
        final var internalBasePath = "http://api.chs.local:4001";
        apiClientUtil.getInternalApiClient( internalBasePath );
        Mockito.verify( internalApiClient ).setInternalBasePath( internalBasePath );
    }

}
