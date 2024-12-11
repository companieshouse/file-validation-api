package uk.gov.companieshouse.filevalidationservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

import java.util.HashMap;

import static uk.gov.companieshouse.filevalidationservice.controllers.ControllerAdvice.FILE_VALIDATION_API;

/**
 * A controller advice that handles {@link MaxUploadSizeExceededException} exceptions.
 *
 * This advice is necessary because the exception will be thrown before it reaches the controller, so it cannot be
 * handled there. The maximum upload size is configured by the {@code spring.servlet.multipart.max-file-size} property,
 * which is in turn configured by the {@code MAX_FILE_SIZE} environment variable.
 *
 * When a {@code MaxUploadSizeExceededException} is thrown, this advice logs an error message and returns a
 * {@code ResponseEntity} with an HTTP status code of {@link HttpStatus#PAYLOAD_TOO_LARGE} and a message
 * indicating that the uploaded file is too large.
 */

@ControllerAdvice
@Order(1)
public class FileSizeExceptionAdvice {
    private static final Logger LOG = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    /**
     * Handles {@link MaxUploadSizeExceededException} exceptions by logging an error message and returning a
     * {@code ResponseEntity} with an HTTP status code of {@link HttpStatus#PAYLOAD_TOO_LARGE} and a message
     * indicating that the uploaded file is too large.
     *
     * @param exception the {@code MaxUploadSizeExceededException} to handle
     * @return a {@code ResponseEntity} with an HTTP status code of {@link HttpStatus#PAYLOAD_TOO_LARGE}
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ResponseBody
    Errors handleFileTooLarge(MaxUploadSizeExceededException exception, final HttpServletRequest request ) {
        var loggedVars = new HashMap<String, Object>();
        loggedVars.put("maxFileSize", exception.getMaxUploadSize());
        LOG.error("Uploaded file was too large", exception, loggedVars);
        final var errors = new Errors();
        errors.addError( Err.invalidBodyBuilderWithLocation( FILE_VALIDATION_API ).withError( exception.getMessage() ).build() );
        return errors;
    }
}
