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
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.net.URI;

@RestController
public class CsvValidationController implements FileValidationInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger( StaticPropertyUtil.APPLICATION_NAMESPACE );

    private final FileTransferService fileTransferService;

    public CsvValidationController( final FileTransferService fileTransferService ) {
        this.fileTransferService = fileTransferService;
    }

    @Override
    public ResponseEntity<FileUploadResponse> uploadFile(MultipartFile file, @Valid String metadata){
        try {

//            // checking file extension in file name
//            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
//                throw new BadRequestRuntimeException("Please upload a valid CSV file");
//            }
            Tika tika = new Tika();
            String type = tika.detect(file.getInputStream(), file.getOriginalFilename());

            LOGGER.info("File type: " + type);

            var objectMapper = new ObjectMapper();
            var fileMetaData = objectMapper.readValue(metadata, FileMetaData.class);
            if(StringUtils.isEmpty(fileMetaData.getFileName()) || StringUtils.isEmpty(fileMetaData.getFromLocation()) || StringUtils.isEmpty(fileMetaData.getToLocation())){
                throw new BadRequestRuntimeException("Please provide a valid metadata: " + fileMetaData);
            }
            String id = fileTransferService.upload(file, fileMetaData);
            return ResponseEntity.created(URI.create(Constants.UPLOAD_URI_PATTERN)).body(new FileUploadResponse().id(id));
        } catch (FileUploadException | IOException e) {
            throw new InternalServerErrorRuntimeException(e.getMessage());
        }
    }
}
