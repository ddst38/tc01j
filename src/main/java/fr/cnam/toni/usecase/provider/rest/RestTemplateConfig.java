package fr.cnam.toni.usecase.provider.rest;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, AuthAppV3HeaderInterceptor interceptor) {
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
//                .interceptors(interceptor)
                .build();
    }
}
