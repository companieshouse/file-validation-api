package uk.gov.companieshouse.filevalidationservice.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class ApiClientConfigTest {

    @Test
    void getInternalApiClientReturnsInternalApiClient(){
        Assertions.assertNotNull( new ApiClientConfig().getInternalApiClient() );
    }

}
