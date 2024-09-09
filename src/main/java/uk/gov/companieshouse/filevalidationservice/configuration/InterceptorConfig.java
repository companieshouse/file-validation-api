package uk.gov.companieshouse.filevalidationservice.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.filevalidationservice.interceptors.LoggingInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Autowired
    public InterceptorConfig( final LoggingInterceptor loggingInterceptor ) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors( @NonNull final InterceptorRegistry registry ) {
        addLoggingInterceptor( registry );
        addEricInterceptors( registry );
    }

    private void addLoggingInterceptor( final InterceptorRegistry registry ) {
        registry.addInterceptor( loggingInterceptor );
    }

    private void addEricInterceptors( final InterceptorRegistry registry ) {
        registry.addInterceptor( new InternalUserInterceptor() ).excludePathPatterns( "/*/healthcheck" );
    }

}
