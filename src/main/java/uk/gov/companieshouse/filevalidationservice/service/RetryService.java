package uk.gov.companieshouse.filevalidationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.filevalidationservice.exception.RetryException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
public class RetryService {

    @Value("${retry.strategy.baseDelay}")
    private long baseDelay;
    @Value("${retry.strategy.delayIncrement}")
    private long delayIncrement;
    @Value("${retry.strategy.timeout}")
    private long timeout;
    @Value("${retry.strategy.maxDelay}")
    private long maxDelay;

    public void setBaseDelay(long baseDelay) {
        this.baseDelay = baseDelay;
    }

    public void setDelayIncrement(long delayIncrement) {
        this.delayIncrement = delayIncrement;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMaxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public <T> T attempt(Supplier<T> func) {
        Duration delay = Duration.ofMillis(baseDelay);
        Instant timeoutInstant = Instant.now().plusMillis(timeout);

        while (true) {
            try {
                return func.get();
            } catch (RetryException e) {
                // See if sleeping again will exceed the timeout. If so, re-throw the exception
                if (Instant.now().plusMillis(delay.toMillis()).isAfter(timeoutInstant)) {
                    throw new RuntimeException(new TimeoutException());
                }

                try {
                    //noinspection BusyWait
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted");
                }
                delay = delay.plus(Duration.ofMillis(delayIncrement));
                delay = Duration.ofMillis(Math.min(delay.toMillis(), maxDelay));
            }
        }
    }
}
