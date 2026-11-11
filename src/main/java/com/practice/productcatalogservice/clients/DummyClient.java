package com.practice.productcatalogservice.clients;

import com.practice.productcatalogservice.dto.DummyAllProductsDto;
import com.practice.productcatalogservice.dto.DummyProductServiceDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class DummyClient {
    private final RestClient restClient;

    public DummyClient(RestClient.Builder builder, @Value("${dummy.service.url}") String dummyServiceUrl) {
        this.restClient = builder.baseUrl(dummyServiceUrl).build();
    }

    @CircuitBreaker(name = "dummy")
    public DummyProductServiceDto getProduct(long id) {
        log.info("Calling dummy service");
        return restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(DummyProductServiceDto.class);
    }

    @CircuitBreaker(name = "dummy")
    public List<DummyProductServiceDto> getAllProducts() {
        DummyAllProductsDto response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products").queryParam("limit", 0).build())
                .retrieve()
                .body(DummyAllProductsDto.class);
        return response != null ? response.getProducts() : List.of();
    }
}
