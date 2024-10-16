package uk.gov.companieshouse.filevalidationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.filevalidationservice.utils.StaticPropertyUtil;

@SpringBootApplication
public class FileValidationApplication {

	StaticPropertyUtil staticPropertyUtil;

	@Autowired
	public FileValidationApplication( final StaticPropertyUtil staticPropertyUtil ) {
		this.staticPropertyUtil = staticPropertyUtil;
	}

	public static void main( String[] args ) {
		SpringApplication.run( FileValidationApplication.class, args );
	}
}
