package uk.gov.companieshouse.filevalidationservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;

import java.util.List;

@Repository
public interface FileValidationRepository extends MongoRepository<FileValidation, String> {

    List<FileValidation> findByStatus(String status);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'status' : ?1 }}")
    void updateStatusById(String id, String newStatus);

}
