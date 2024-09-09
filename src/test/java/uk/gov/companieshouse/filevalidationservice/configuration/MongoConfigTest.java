package uk.gov.companieshouse.filevalidationservice.configuration;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class MongoConfigTest {

    private final MongoConfig mongoConfig = new MongoConfig();

    private static String reduceTimestampResolution( final String timestamp ) {
        return timestamp.substring( 0, timestamp.indexOf( ":" ) );
    }

    @Test
    void validatingMongoEventListenerReturnsValidatingMongoEventListener(){
        final var localValidatorFactoryBean = Mockito.mock( LocalValidatorFactoryBean.class );
        Assertions.assertNotNull( mongoConfig.validatingMongoEventListener( localValidatorFactoryBean ) );
    }

    @Test
    void dateTimeProviderReturnsCurrentTime(){
        final var expectedDate = reduceTimestampResolution( LocalDateTime.now().toString() );
        final var actualDate = reduceTimestampResolution( mongoConfig.dateTimeProvider().getNow().get().toString() );
        Assertions.assertEquals( expectedDate, actualDate );
    }

}
