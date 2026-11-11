package com.practice.productcatalogservice.clients;

import com.practice.productcatalogservice.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class UserAuthClient {
    private final RestClient restClient;

    public UserAuthClient(RestClient.Builder builder, @Value("${auth.service.url}") String authServiceUrl) {
        this.restClient = builder.baseUrl(authServiceUrl).build();
    }

    @Retryable(
            includes = {ResourceAccessException.class, HttpServerErrorException.class},
            excludes = HttpClientErrorException.class,
            maxRetries = 2, delay = 200, multiplier = 2, maxDelay = 1000)
    public UserDto getUser(String token) {
        return restClient.get()
                .uri("/users/me")
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(UserDto.class);
    }
}
