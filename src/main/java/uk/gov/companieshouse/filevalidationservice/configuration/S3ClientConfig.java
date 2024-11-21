package uk.gov.companieshouse.filevalidationservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {

    @Bean
    S3Client getS3Client() {
        return S3Client.create();
    }
}
