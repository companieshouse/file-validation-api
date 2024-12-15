package uk.gov.companieshouse.filevalidationservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.filevalidationservice.controllers.ControllerAdvice.FILE_VALIDATION_API;

@ExtendWith(MockitoExtension.class)
class FileSizeExceptionAdviceTest {

    @InjectMocks
    private FileSizeExceptionAdvice fileSizeExceptionAdvice;

    @Mock
    private HttpServletRequest request;

    @Test
    void testHandleFileTooLargeException() {

        long maxFileSize = 1024L; // 1KB for the max file size
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(maxFileSize);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        Errors response = fileSizeExceptionAdvice.handleFileTooLarge(exception, mockRequest);
        assertNotNull(response);
        assertTrue(response.containsError(Err.invalidBodyBuilderWithLocation( FILE_VALIDATION_API ).withError( exception.getMessage() ).build()));

    }
}