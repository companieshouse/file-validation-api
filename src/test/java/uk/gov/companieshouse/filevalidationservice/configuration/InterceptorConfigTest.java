package uk.gov.companieshouse.filevalidationservice.configuration;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.filevalidationservice.interceptors.LoggingInterceptor;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class InterceptorConfigTest {

    @Test
    void addInterceptorsRegistersLoggingAndInternalUserInterceptor(){
        final var interceptorConfig = new InterceptorConfig( new LoggingInterceptor() );
        final var interceptorRegistry = Mockito.mock( InterceptorRegistry.class );

        Mockito.doReturn( new InterceptorRegistration( new LoggingInterceptor() ) ).when( interceptorRegistry ).addInterceptor( any( LoggingInterceptor.class ) );
        Mockito.doReturn( new InterceptorRegistration( new InternalUserInterceptor() ) ).when( interceptorRegistry ).addInterceptor( any( InternalUserInterceptor.class ) );

        interceptorConfig.addInterceptors( interceptorRegistry );

        Mockito.verify( interceptorRegistry ).addInterceptor( any( LoggingInterceptor.class ) );
        Mockito.verify( interceptorRegistry ).addInterceptor( any( InternalUserInterceptor.class ) );
    }

}
