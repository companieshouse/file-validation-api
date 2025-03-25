package uk.gov.companieshouse.filevalidationservice.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.RequestLogger;

import static uk.gov.companieshouse.filevalidationservice.FileValidationApplication.APPLICATION_NAMESPACE;

@Component
public class LoggingInterceptor implements HandlerInterceptor, RequestLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger( APPLICATION_NAMESPACE );

    @Override
    public boolean preHandle( final HttpServletRequest request, @NonNull final HttpServletResponse response, @NonNull final Object handler ) {
        logStartRequestProcessing( request, LOGGER );
        return true;
    }

    @Override
    public void postHandle( final HttpServletRequest request, final HttpServletResponse response, final Object handler, @Nullable final ModelAndView modelAndView ) {
        logEndRequestProcessing( request, response, LOGGER );
    }

}
