package uk.gov.companieshouse.filevalidationservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.fileValidation.api.FileValidationInterface;
import uk.gov.companieshouse.api.fileValidation.model.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.filevalidationservice.exception.BadRequestRuntimeException;
import uk.gov.companieshouse.filevalidationservice.exception.FileUploadException;
import uk.gov.companieshouse.filevalidationservice.exception.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.filevalidationservice.models.FileMetaData;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;
import uk.gov.companieshouse.filevalidationservice.utils.Constants;

import java.net.URI;

@RestController
public class CsvValidationController implements FileValidationInterface {

    // TODO: use private-api-sdk-java interfaces, instead of @RequestMapping

    private final FileTransferService fileTransferService;

    public CsvValidationController( final FileTransferService fileTransferService ) {
        this.fileTransferService = fileTransferService;
    }

    @Override
    public ResponseEntity<FileUploadResponse> uploadFile(MultipartFile file, @Valid String metadata){
        try {
            if (file.isEmpty() || !file.getContentType().equals("text/csv")){
                throw new BadRequestRuntimeException("Please upload a valid CSV file");
            }
            var objectMapper = new ObjectMapper();
            var fileMetaData = objectMapper.readValue(metadata, FileMetaData.class);
            if(StringUtils.isEmpty(fileMetaData.getFileName()) || StringUtils.isEmpty(fileMetaData.getFromLocation()) || StringUtils.isEmpty(fileMetaData.getToLocation())){
                throw new BadRequestRuntimeException("Please provide a valid metadata: " + fileMetaData);
            }
            String id = fileTransferService.upload(file, fileMetaData);
            return ResponseEntity.created(URI.create(Constants.UPLOAD_URI_PATTERN)).body(new FileUploadResponse().id(id));
        } catch (FileUploadException | JsonProcessingException e) {
            throw new InternalServerErrorRuntimeException(e.getMessage());
        }
    }
}
