package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.dto.GroqChatMessageDto;
import com.practice.productcatalogservice.dto.GroqChatRequestDto;
import com.practice.productcatalogservice.dto.GroqChatResponseDto;
import com.practice.productcatalogservice.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class GroqPitchService implements IPitchService {
    private final RestTemplate restTemplate;
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final String GROQ_API_KEY;

    public GroqPitchService(RestTemplateBuilder restTemplateBuilder, @Value("${groq.api.key}") String groqApiKey) {
        GROQ_API_KEY = groqApiKey;
        restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String getPitch(Product product) {
        GroqChatRequestDto requestDto = getGroqChatRequestDto();
        requestDto.getMessages().add(new GroqChatMessageDto("user", String.format("Write a short headline to display on UI for the following product: %s. The product is described as: %s. The price is $%.2f.", product.getName(), product.getDescription(), product.getPrice())));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(GROQ_API_KEY);
        HttpEntity<GroqChatRequestDto> requestEntity = new HttpEntity<>(requestDto, httpHeaders);
        try {
            ResponseEntity<GroqChatResponseDto> response = restTemplate.postForEntity(GROQ_URL, requestEntity, GroqChatResponseDto.class);
            return response != null ? response.getBody().getChoices().get(0).getMessage().getContent() : getDefaultPitch(product);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultPitch(product);
        }
    }
    private String getDefaultPitch(Product product) {
        return String.format("Introducing our latest product: %s! This %s is priced at just $%.2f. Don't miss out on this amazing opportunity to own a high-quality product that will exceed your expectations. Get yours today!", product.getName(), product.getDescription(), product.getPrice());
    }
    private static GroqChatRequestDto getGroqChatRequestDto() {
        GroqChatRequestDto requestDto = new GroqChatRequestDto();
        requestDto.setModel("openai/gpt-oss-20b");
        requestDto.setMax_completion_tokens(300);
        requestDto.setReasoning_effort("low");
        requestDto.setMessages(new ArrayList<>(Arrays.asList(new GroqChatMessageDto("system", "You are a marketing copywriter for ShopNest, an online store. You write short push notifications that make people want to open the app."))));
        return requestDto;
    }
}
