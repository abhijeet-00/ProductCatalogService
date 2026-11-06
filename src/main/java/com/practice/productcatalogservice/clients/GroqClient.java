package com.practice.productcatalogservice.clients;

import com.practice.productcatalogservice.dto.GroqChatRequestDto;
import com.practice.productcatalogservice.dto.GroqChatResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroqClient {
    private final RestClient restClient;
    public GroqClient(RestClient.Builder builder, @Value("${GROQ_URL}") String groqUrl,
                      @Value("${GROQ_API_KEY}") String groqAPIKey) {
        this.restClient = builder.baseUrl(groqUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(groqAPIKey)).build();
    }
    public String getPitch(GroqChatRequestDto requestDto) {
        try {
            return restClient.post().body(requestDto).retrieve().body(GroqChatResponseDto.class).getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            throw new RuntimeException("Error while calling Groq API: " + e.getMessage(), e);
        }
    }
}
