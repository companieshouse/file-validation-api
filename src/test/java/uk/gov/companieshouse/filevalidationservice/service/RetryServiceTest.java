package uk.gov.companieshouse.filevalidationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "retry.strategy.baseDelay=PT1S")
@TestPropertySource(properties = "retry.strategy.delayIncrement=PT1S")
@TestPropertySource(properties = "retry.strategy.timeout=PT5S")
@TestPropertySource(properties = "retry.strategy.maxDelay=PT2S")
class RetryServiceTest {

    RetryService retryService;

    @BeforeEach
    void setUp() {
        retryService = new RetryService();
        retryService.setBaseDelay(10);
        retryService.setDelayIncrement(10);
        retryService.setTimeout(30);
        retryService.setMaxDelay(10);
    }

    @Test
    void testReturnsValueWithoutRetry() {
        // Given
        Supplier<Integer> fn = () -> 42;

        // When
        int value = retryService.attempt(fn);

        // Then
        assertThat(value, is(equalTo(42)));
    }

    @Test
    void testRetryWhenRetryExceptionThrown() {
        // Given
        Supplier<Integer> fn = spy(new Supplier<>() {
            private int count = 0;

            @Override
            public Integer get() {
                // Will retry first time and resolve the second.
                if (count >= 1) return 42;
                count += 1;
                throw new RetryException();
            }
        });

        // When
        int value = retryService.attempt(fn);

        // Then
        assertThat(value, is(equalTo(42)));
        verify(fn, times(2)).get();
    }

    @Test
    void testTimeoutAfterPeriodOfTime() {
        // Given
        retryService.setBaseDelay(1000);
        retryService.setDelayIncrement(1000);
        retryService.setTimeout(1000);
        retryService.setMaxDelay(10000);

        Supplier<Integer> fn = () -> {
            throw new RetryException();
        };

        // When
        Executable attempt = () -> retryService.attempt(fn);

        // Then
        assertThrows(RuntimeException.class, attempt);
    }

    @Test
    void testHandleInteruption() {
        // Given
        retryService.setBaseDelay(1000);
        retryService.setDelayIncrement(10000);
        retryService.setTimeout(30000);
        retryService.setMaxDelay(20000);
        Supplier<Integer> fn = () -> {
            throw new RetryException();
        };

        // When
        Thread.currentThread().interrupt();
        Executable attempt = () -> retryService.attempt(fn);

        // Then
        assertThrows(RuntimeException.class, attempt);
    }
}
