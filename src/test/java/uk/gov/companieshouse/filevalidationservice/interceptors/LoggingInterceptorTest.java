package uk.gov.companieshouse.filevalidationservice.interceptors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class LoggingInterceptorTest {

    @Test
    void prehandleSucceeds(){
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        Assertions.assertTrue( new LoggingInterceptor().preHandle( request, response, null ) );
        Assertions.assertEquals( 200, response.getStatus() );
    }

}
