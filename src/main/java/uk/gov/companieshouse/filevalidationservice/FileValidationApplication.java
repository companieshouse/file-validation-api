package uk.gov.companieshouse.filevalidationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileValidationApplication {

	public static final String APPLICATION_NAMESPACE = "file-validation-api";
	public static void main( String[] args ) {
		SpringApplication.run( FileValidationApplication.class, args );
	}
}
