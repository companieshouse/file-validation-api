package uk.gov.companieshouse.filevalidationservice.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class S3ClientConfigTest {

    @Test
    void getS3ClientReturnsS3Client () {
        System.setProperty("aws.region", "eu-west-2");
        assertNotNull( new S3ClientConfig().getS3Client());
    }
}
