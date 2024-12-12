package uk.gov.companieshouse.filevalidationservice.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class ShedLockConfigTest {

    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDatabase mongoDatabase;

    private ShedLockConfig shedLockConfig;

    private static final String TEST_DB_NAME = "testDB";

    @BeforeEach
    void setUp() {
        shedLockConfig = new ShedLockConfig();
        ReflectionTestUtils.setField(shedLockConfig, "mongoDB", TEST_DB_NAME);
    }

    @Test
    void lockProvider_ShouldCreateMongoLockProvider() {
        // Arrange
        when(mongoClient.getDatabase(TEST_DB_NAME)).thenReturn(mongoDatabase);

        // Act
        LockProvider result = shedLockConfig.lockProvider(mongoClient);

        // Assert
        assertNotNull(result, "LockProvider should not be null");
        assertTrue(result instanceof MongoLockProvider, "LockProvider should be instance of MongoLockProvider");
        verify(mongoClient, times(1)).getDatabase(TEST_DB_NAME);
    }
    @Test
    void lockProvider_ShouldUseCorrectDatabaseName() {
        // Arrange
        String customDBName = "customDB";
        ReflectionTestUtils.setField(shedLockConfig, "mongoDB", customDBName);
        when(mongoClient.getDatabase(customDBName)).thenReturn(mongoDatabase);

        // Act
        shedLockConfig.lockProvider(mongoClient);

        // Assert
        verify(mongoClient).getDatabase(customDBName);
    }

    @Test
    void lockProvider_ShouldHandleMongoClientException() {
        // Arrange
        when(mongoClient.getDatabase(anyString())).thenThrow(new IllegalStateException("MongoDB connection failed"));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> shedLockConfig.lockProvider(mongoClient),
                "Should throw IllegalStateException when MongoDB connection fails");
    }
}