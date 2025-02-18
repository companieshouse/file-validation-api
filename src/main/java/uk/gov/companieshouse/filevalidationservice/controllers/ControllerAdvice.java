package uk.gov.companieshouse.filevalidationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.filevalidationservice.exception.BadRequestRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.NotFoundRuntimeException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );
    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String FILE_VALIDATION_API = "file_validation_api";
    private static final String QUERY_PARAMETERS = "query-parameters";

    private String getJsonStringFromErrors( final String xRequestId, final Errors errors ) {

        final var objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString( errors );
        } catch ( IOException e ) {
            LOG.errorContext( xRequestId, String.format( "Fail to parse Errors object to JSON %s", e.getMessage() ), e, null );
            return "";
        }
    }

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

//    @ExceptionHandler( ConstraintViolationException.class )
//    @ResponseStatus( HttpStatus.BAD_REQUEST )
//    @ResponseBody
//    public Errors onConstraintViolationException( final ConstraintViolationException exception, final HttpServletRequest request ) {
//        final var errorsToBeLogged = new Errors();
//        for ( ConstraintViolation<?> constraintViolation : exception.getConstraintViolations() ) {
//            errorsToBeLogged.addError( Err.invalidBodyBuilderWithLocation( FILE_VALIDATION_API )
//                    .withError( String.format( "%s %s", Optional.of( constraintViolation.getInvalidValue() ).orElse(" "),constraintViolation.getMessage() ) ).build() );
//        }
//
//        final var xRequestId = request.getHeader( X_REQUEST_ID );
//        final var errorsJsonString = getJsonStringFromErrors( xRequestId, errorsToBeLogged );
//        LOG.errorContext( xRequestId, String.format( "Validation Failed with [%s]", errorsJsonString), null, null );
//
//        return errorsToBeLogged;
//    }

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

    // TODO: We need to create tests for this file, after the first endpoint has been built.
    // Test by calling the endpoint using MockMvc, and using Mockito to force appropriate exceptions
    // e.g. BadRequestRuntimeException. Then check that API returns correct response code.
    // Example: https://github.com/companieshouse/accounts-association-api/blob/main/src/test/java/uk/gov/companieshouse/accounts/association/controller/ControllerAdviceTest.java
}