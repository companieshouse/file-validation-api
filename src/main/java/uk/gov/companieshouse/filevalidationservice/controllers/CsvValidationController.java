package uk.gov.companieshouse.filevalidationservice.controllers;

import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.filevalidationservice.service.FileTransferService;

@RestController
@RequestMapping("/api/csv")
public class CsvValidationController {

    // TODO: use private-api-sdk-java interfaces, instead of @RequestMapping

    private final FileTransferService fileTransferService;

    public CsvValidationController( final FileTransferService fileTransferService ) {
        this.fileTransferService = fileTransferService;
    }

}
