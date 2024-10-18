package uk.gov.companieshouse.filevalidationservice.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;

import java.util.Optional;

@RestController
@RequestMapping("/file-validation-api")
public class CsvValidationController {

    // TODO: use private-api-sdk-java interfaces, instead of @RequestMapping

    private final FileTransferService fileTransferService;

    public CsvValidationController( final FileTransferService fileTransferService ) {
        this.fileTransferService = fileTransferService;
    }

    @GetMapping("/document/{document_id}")
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

}
