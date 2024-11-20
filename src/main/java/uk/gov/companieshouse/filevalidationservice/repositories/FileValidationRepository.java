package uk.gov.companieshouse.filevalidationservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;

@Repository
public interface FileValidationRepository extends MongoRepository<FileValidation, String> {
}
