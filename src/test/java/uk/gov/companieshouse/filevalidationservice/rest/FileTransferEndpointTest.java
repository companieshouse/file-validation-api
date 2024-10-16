package uk.gov.companieshouse.filevalidationservice.rest;

import static org.mockito.ArgumentMatchers.any;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException.Builder;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.PrivateFileTransferResourceHandler;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferDelete;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferDownload;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferGetDetails;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferUpload;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
import uk.gov.companieshouse.filevalidationservice.utils.ApiClientUtil;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class FileTransferEndpointTest {

    @Mock
    private ApiClientUtil apiClientUtil;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateFileTransferResourceHandler privateFileTransferResourceHandler;

    @Mock
    private PrivateModelFileTransferUpload privateModelFileTransferUpload;

    @Mock
    private PrivateModelFileTransferGetDetails privateModelFileTransferGetDetails;

    @Mock
    private PrivateModelFileTransferDownload privateModelFileTransferDownload;

    @Mock
    private PrivateModelFileTransferDelete privateModelFileTransferDelete;

    @InjectMocks
    private FileTransferEndpoint fileTransferEndpoint;

    @Test
    void uploadReturnsUnsupportedMediaTypeWhenFileIsInvalid() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferUpload ).when( privateFileTransferResourceHandler ).upload( any() );
        Mockito.doThrow( new ApiErrorResponseException( new Builder( 415, "Internal Server Error", new HttpHeaders() ) ) ).when( privateModelFileTransferUpload ).execute();

        Assertions.assertThrows( ApiErrorResponseException.class, () -> fileTransferEndpoint.upload( new FileApi() ) );
    }

    @Test
    void uploadReturnsOkWhenUploadSucceeds() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferUpload ).when( privateFileTransferResourceHandler ).upload( any() );
        Mockito.doReturn( new ApiResponse<>( 200, Map.of(), new IdApi( "1" ) ) ).when( privateModelFileTransferUpload ).execute();

        final var response = fileTransferEndpoint.upload( new FileApi() );

        Assertions.assertEquals( 200, response.getStatusCode() );
        Assertions.assertEquals( "1", response.getData().getId() );
    }

    @Test
    void detailsReturnsNotFoundWhenFileDoesNotExist() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferGetDetails ).when( privateFileTransferResourceHandler ).details( any() );
        Mockito.doThrow( new ApiErrorResponseException( new Builder( 404, "Not Found", new HttpHeaders() ) ) ).when( privateModelFileTransferGetDetails ).execute();

        Assertions.assertThrows( ApiErrorResponseException.class, () -> fileTransferEndpoint.details( "1" ) );
    }

    @Test
    void detailsReturnsFileDetailsWhenFileExists() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferGetDetails ).when( privateFileTransferResourceHandler ).details( any() );
        Mockito.doReturn( new ApiResponse<>( 200, Map.of(), new FileDetailsApi() ) ).when( privateModelFileTransferGetDetails ).execute();

        final var response = fileTransferEndpoint.details( "1" );

        Assertions.assertEquals( 200, response.getStatusCode() );
        Assertions.assertEquals( new FileDetailsApi(), response.getData() );
    }

    @Test
    void downloadReturnsNotFoundWhenFileDoesNotExist() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferDownload ).when( privateFileTransferResourceHandler ).download( any() );
        Mockito.doThrow( new ApiErrorResponseException( new Builder( 404, "Not Found", new HttpHeaders() ) ) ).when( privateModelFileTransferDownload ).execute();

        Assertions.assertThrows( ApiErrorResponseException.class, () -> fileTransferEndpoint.download( "1" ) );
    }

    @Test
    void downloadReturnsFileWhenFileExists() throws ApiErrorResponseException, URIValidationException {
        final var file = new FileApi("file.csv", new byte[]{}, "application/json", 0, ".csv" );

        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferDownload ).when( privateFileTransferResourceHandler ).download( any() );
        Mockito.doReturn( new ApiResponse<>( 200, Map.of(), file ) ).when( privateModelFileTransferDownload ).execute();

        final var response = fileTransferEndpoint.download( "1" );

        Assertions.assertEquals( 200, response.getStatusCode() );
        Assertions.assertEquals( file, response.getData() );
    }

    @Test
    void deleteReturnsInternalServerErrorWhenDeletionFails() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferDelete ).when( privateFileTransferResourceHandler ).delete( any() );
        Mockito.doThrow( new ApiErrorResponseException( new Builder( 500, "Internal Sever Error", new HttpHeaders() ) ) ).when( privateModelFileTransferDelete ).execute();

        Assertions.assertThrows( ApiErrorResponseException.class, () -> fileTransferEndpoint.delete( "1" ) );
    }

    @Test
    void deleteReturnsNoContentWhenDeletionSucceeds() throws ApiErrorResponseException, URIValidationException {
        Mockito.doReturn( internalApiClient ).when( apiClientUtil ).getInternalApiClient( any() );
        Mockito.doReturn( privateFileTransferResourceHandler ).when( internalApiClient ).privateFileTransferResourceHandler();
        Mockito.doReturn( privateModelFileTransferDelete ).when( privateFileTransferResourceHandler ).delete( any() );
        Mockito.doReturn( new ApiResponse<>( 204, Map.of() ) ).when( privateModelFileTransferDelete ).execute();

        final var response = fileTransferEndpoint.delete( "1" );

        Assertions.assertEquals( 204, response.getStatusCode() );
    }

}

