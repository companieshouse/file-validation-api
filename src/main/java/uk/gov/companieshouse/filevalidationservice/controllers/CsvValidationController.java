package uk.gov.companieshouse.filevalidationservice.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
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

    @PostMapping("/document")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file){
        if (file.isEmpty() || !file.getContentType().equals("text/csv")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a valid CSV file.");
        }
        ApiResponse<IdApi> id = fileTransferService.upload(file);
        if(HttpStatus.OK.value() == id.getStatusCode()){
            return ResponseEntity.status(id.getStatusCode()).body(id.getData().getId());
        }else{
            return ResponseEntity.status(id.getStatusCode()).body("");
        }
    }
}
