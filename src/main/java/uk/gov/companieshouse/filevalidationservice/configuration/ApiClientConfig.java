package uk.gov.companieshouse.filevalidationservice.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiClientConfig {

    @Bean
    ResponseErrorHandler getResponseErrorHandler() {
        return new DefaultResponseErrorHandler();
    }

    @Bean
    RestTemplate getRestTemplate(final ResponseErrorHandler handler) {
        final RestTemplate template = new RestTemplateBuilder().build();
        template.setErrorHandler(handler);
        return template;
    }

}
