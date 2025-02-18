package uk.gov.companieshouse.filevalidationservice.controllers;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.filevalidationservice.configuration.InterceptorConfig;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.NotFoundRuntimeException;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CsvValidationController.class)
class ControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileTransferService fileTransferService;

    @MockitoBean
    private Tika tika;

    @MockitoBean
    private InterceptorConfig interceptorConfig;

    @MockitoBean
    private StaticPropertyUtil staticPropertyUtil;


    @BeforeEach
    void setUp() {
        Mockito.doNothing().when(interceptorConfig).addInterceptors(any());
    }

    @Test
    void testNotFoundRuntimeError() throws Exception {
        Mockito.doThrow(new NotFoundRuntimeException("file-validation-api", "Not Found"))
                .when(fileTransferService).get("test-id");

        mockMvc.perform(post("/wrong-uri")
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "111"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testBadRequestRuntimeError() throws Exception {
        mockMvc.perform(post( "/file-validation-api/upload" )
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "111")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOnInternalServerError() throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "Hello, World!".getBytes());
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        Mockito.doThrow(new NullPointerException("Couldn't find file"))
                .when(fileTransferService).get("FileID");

        mockMvc.perform(multipart( "/file-validation-api/upload" )
                        .file(file)
                        .param("metadata", metaData)
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "111"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testOnInternalServerErrorRuntimeException() throws Exception {
        Mockito.doThrow(new InternalServerErrorRuntimeException("Couldn't find file"))
                .when(fileTransferService).get("FileID");


        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "Hello, World!".getBytes());
        String metaData = "{\"fileName\":\"Test file\",\"fromLocation\":\"abc\",\"toLocation\":\"S3:abc\"}";

        mockMvc.perform(multipart( "/file-validation-api/upload" )
                        .file(file)
                        .param("metadata", metaData)
                        .header("X-Request-Id", "theId123")
                        .header("ERIC-Identity", "111"))
                .andExpect(status().isInternalServerError());
    }
}