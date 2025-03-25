package uk.gov.companieshouse.filevalidationservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.filevalidationservice.exception.BadRequestRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.NotFoundRuntimeException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

import static uk.gov.companieshouse.filevalidationservice.FileValidationApplication.APPLICATION_NAMESPACE;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger( APPLICATION_NAMESPACE );
    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String FILE_VALIDATION_API = "file_validation_api";
    private static final String QUERY_PARAMETERS = "query-parameters";

    @ExceptionHandler( NotFoundRuntimeException.class )
    @ResponseStatus( HttpStatus.NOT_FOUND )
    @ResponseBody
    public Errors onNotFoundRuntimeException( final NotFoundRuntimeException exception, HttpServletRequest request ) {
        final var xRequestId = request.getHeader(X_REQUEST_ID);

        final Map<String, Object> contextMap = new HashMap<>();
        contextMap.put( "url", request.getRequestURL().toString() );
        contextMap.put( QUERY_PARAMETERS, request.getQueryString() != null ? "?" + request.getQueryString() : "" );

        LOG.errorContext( xRequestId, exception.getMessage(), null, contextMap );

        final var errors = new Errors();
        errors.addError( Err.serviceErrBuilder().withError( exception.getMessage() ).build() );
        return errors;
    }

    @ExceptionHandler( BadRequestRuntimeException.class )
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    @ResponseBody
    public Errors onBadRequestRuntimeException( final BadRequestRuntimeException exception, final HttpServletRequest request ) {
        final var xRequestId = request.getHeader( X_REQUEST_ID );

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put( "url", request.getRequestURL().toString() );
        contextMap.put( QUERY_PARAMETERS, request.getQueryString() != null ? "?" + request.getQueryString() : "" );

        LOG.errorContext( xRequestId, exception.getMessage(), null, contextMap );

        final var errors = new Errors();
        errors.addError( Err.serviceErrBuilder().withError( exception.getMessage() ).build() );
        return errors;
    }

    @ExceptionHandler( InternalServerErrorRuntimeException.class )
    @ResponseStatus( HttpStatus.INTERNAL_SERVER_ERROR )
    @ResponseBody
    public Errors onInternalServerErrorRuntimeException( final InternalServerErrorRuntimeException exception, final HttpServletRequest request ) {
        final var xRequestId = request.getHeader( X_REQUEST_ID );

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put( "url", request.getRequestURL().toString() );
        contextMap.put( QUERY_PARAMETERS, request.getQueryString() != null ? "?" + request.getQueryString() : "" );

        LOG.errorContext( xRequestId, exception.getMessage(), null, contextMap );

        final var errors = new Errors();
        errors.addError( Err.invalidBodyBuilderWithLocation( FILE_VALIDATION_API ).withError( exception.getMessage() ).build() );
        return errors;
    }

    @ExceptionHandler( Exception.class )
    @ResponseStatus( HttpStatus.INTERNAL_SERVER_ERROR )
    @ResponseBody
    public Errors onException( final Exception exception, final HttpServletRequest request ) {
        final var errors = new Errors();
        final var xRequestId = request.getHeader( X_REQUEST_ID );
        final var msg = request.getRequestURL() + ( request.getQueryString() != null ? "?" + request.getQueryString() : "" ) + ". " + exception.getMessage();
        LOG.errorContext( xRequestId, msg, exception, null );

        errors.addError( Err.invalidBodyBuilderWithLocation( FILE_VALIDATION_API ).withError( exception.getMessage() ).build() );

        return errors;
    }
}