package uk.gov.companieshouse.filevalidationservice.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResponseErrorHandler;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class ApiClientConfigTest {

    @Test
    void testGetResponseErrorHandler(){
        Assertions.assertNotNull( new ApiClientConfig().getResponseErrorHandler() );
    }

    @Test
    void testGetRestTemplate(){
        ResponseErrorHandler handler = new ApiClientConfig().getResponseErrorHandler();
        Assertions.assertNotNull( new ApiClientConfig().getRestTemplate(handler) );
    }

}
