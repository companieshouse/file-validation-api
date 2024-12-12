package uk.gov.companieshouse.filevalidationservice.configuration;

import com.mongodb.client.MongoClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "${amlData.fileValidation.scheduler.default-lock-at-most-for}") // Lock for maximum XX minutes
public class ShedLockConfig {

    @Value( "${spring.data.mongodb.database}" )
    private String mongoDB;

    @Bean
    public LockProvider lockProvider(MongoClient mongo) {
        return new MongoLockProvider(mongo.getDatabase(mongoDB));
    }
}
