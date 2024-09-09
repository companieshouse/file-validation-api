package uk.gov.companieshouse.filevalidationservice.configuration;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableMongoRepositories( "uk.gov.companieshouse.filevalidationservice.repositories" )
@EnableMongoAuditing( dateTimeProviderRef = "mongodbDatetimeProvider" )
public class MongoConfig {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener( final LocalValidatorFactoryBean localValidatorFactoryBean ) {
        return new ValidatingMongoEventListener( localValidatorFactoryBean );
    }

    @Bean( name = "mongodbDatetimeProvider" )
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of( LocalDateTime.now() );
    }

    // TODO: when we know which MongoDB repositories we need to interact with, we need to
    // create the DataAccessObjects in the models package, and the create a package called
    // uk.gov.companieshouse.filevalidationservice.repositories, and add MongoRepository's there there
    // based on these DataAccessObjects

}
