package uk.gov.companieshouse.filevalidationservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.filevalidationservice.models.FileValidation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileValidationRepository extends MongoRepository<FileValidation, String> {

    @Query(value = "{ 'status' : { '$in' : ?0 } }")
    List<FileValidation> findByStatuses(String... status);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'status' : ?1, updated_at : ?2, updated_by : ?3 }}")
    void updateStatusById(String id, String newStatus, LocalDateTime updatedAt, String updatedBy);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'status' : ?1, 'error_message' : ?2, updated_at : ?3, updated_by : ?4 }}")
    void updateStatusAndErrorMessageById(String id, String newStatus, String errorMessage, LocalDateTime updatedAt, String updatedBy);

    @Query(value = "{ '_id' : ?0 }")
    @Update("{ '$set' : { 'status' : ?1, 'updated_at' : ?2, 'updated_by' : ?3 }, '$unset' : { 'error_message' : '' }}")
    void updateStatusAndRemoveErrorMessageById(String id, String newStatus, LocalDateTime updatedAt, String updatedBy);

}
