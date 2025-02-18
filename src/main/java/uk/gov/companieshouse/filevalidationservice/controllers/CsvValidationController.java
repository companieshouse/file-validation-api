package uk.gov.companieshouse.filevalidationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
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

import java.io.IOException;
import java.net.URI;

@RestController
public class CsvValidationController implements FileValidationInterface {

    private final FileTransferService fileTransferService;

    private final Tika tika;

    public CsvValidationController( final FileTransferService fileTransferService, Tika tika) {
        this.fileTransferService = fileTransferService;
        this.tika = tika;
    }

    @Override
    public ResponseEntity<FileUploadResponse> uploadFile(MultipartFile file, @Valid String metadata){
        try {

            var objectMapper = new ObjectMapper();
            var fileMetaData = objectMapper.readValue(metadata, FileMetaData.class);
            if(StringUtils.isEmpty(fileMetaData.getFileName()) || StringUtils.isEmpty(fileMetaData.getFromLocation()) || StringUtils.isEmpty(fileMetaData.getToLocation())){
                throw new BadRequestRuntimeException("Please provide a valid metadata: " + fileMetaData);
            }
            String fileType = tika.detect(file.getInputStream(), file.getOriginalFilename());
            if (!fileType.equals("text/csv")){
                throw new BadRequestRuntimeException(String.format("Please upload a valid CSV file. fileName: %s, amlBodyName: %s", fileMetaData.getFileName(), fileMetaData.getFromLocation()));
            }
            String id = fileTransferService.upload(file, fileMetaData);
            return ResponseEntity.created(URI.create(Constants.UPLOAD_URI_PATTERN)).body(new FileUploadResponse().id(id));
        } catch (FileUploadException | IOException e) {
            throw new InternalServerErrorRuntimeException(e.getMessage());
        }
    }
}
