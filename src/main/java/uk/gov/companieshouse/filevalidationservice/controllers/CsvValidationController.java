package uk.gov.companieshouse.filevalidationservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.fileValidation.api.FileValidationInterface;
import uk.gov.companieshouse.api.fileValidation.model.FileUploadResponse;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.filevalidationservice.exception.BadRequestRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.FileUploadException;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.models.FileMetaData;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;
import uk.gov.companieshouse.filevalidationservice.utils.Constants;

import java.net.URI;
import java.util.Optional;

@RestController
public class CsvValidationController implements FileValidationInterface {

    // TODO: use private-api-sdk-java interfaces, instead of @RequestMapping

    private final FileTransferService fileTransferService;

    public CsvValidationController( final FileTransferService fileTransferService ) {
        this.fileTransferService = fileTransferService;
    }

    @GetMapping("/file-validation-api/document/{document_id}")
    public ResponseEntity<?> downloadFile(@PathVariable("document_id") String id) {
        try {
            Optional<FileApi> downloadedFile = fileTransferService.get(id);

            if(downloadedFile.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(downloadedFile.get(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<FileUploadResponse> uploadFile(MultipartFile file, @Valid String metadata){
        try {
            if (file.isEmpty() || !file.getContentType().equals("text/csv")){
                throw new BadRequestRuntimeException("Please upload a valid CSV file");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            FileMetaData fileMetaData = objectMapper.readValue(metadata, FileMetaData.class);
            String id = fileTransferService.upload(file, fileMetaData);
            return ResponseEntity.created(URI.create(Constants.UPLOAD_URI_PATTERN)).body(new FileUploadResponse().id(id));
        } catch (FileUploadException | JsonProcessingException e) {
            throw new InternalServerErrorRuntimeException(e.getMessage());
        }
    }
}
