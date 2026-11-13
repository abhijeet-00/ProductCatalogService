package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.clients.GroqClient;
import com.practice.productcatalogservice.dto.GroqChatRequestDto;
import com.practice.productcatalogservice.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroqPitchServiceTests {
    @Mock
    private GroqClient groqClient;
    @InjectMocks
    private GroqPitchService groqPitchService;

    @Test
    void getPitch_clientReturnsPitch_returnsPitch() {
        when(groqClient.getPitch(any())).thenReturn("This is a testing pitch");
        Product product = new Product();

        assertEquals("This is a testing pitch", groqPitchService.getPitch(product));
    }

    @Test
    void getPitch_clientThrows_returnsDefaultPitch() {
        when(groqClient.getPitch(any())).thenThrow(new RuntimeException("Groq API is down"));
        Product product = buildProduct();

        String expected = String.format("Introducing our latest product: %s! This %s is priced at just $%.2f. Don't miss out on this amazing opportunity to own a high-quality product that will exceed your expectations. Get yours today!", product.getName(), product.getDescription(), product.getPrice());
        String result = groqPitchService.getPitch(product);

        assertEquals(expected, result);
    }

    /**
     * Covers the private getGroqChatRequestDto() helper: builds the expected request DTO through
     * getPitch() and captures what was actually handed to the Groq client.
     */
    @Test
    void getPitch_buildsGroqChatRequest() {
        when(groqClient.getPitch(any())).thenReturn("This is a testing pitch");
        Product product = buildProduct();
        ArgumentCaptor<GroqChatRequestDto> requestCaptor = ArgumentCaptor.forClass(GroqChatRequestDto.class);

        groqPitchService.getPitch(product);

        verify(groqClient).getPitch(requestCaptor.capture());
        GroqChatRequestDto request = requestCaptor.getValue();
        assertEquals("openai/gpt-oss-20b", request.getModel());
        assertEquals(300, request.getMax_completion_tokens());
        assertEquals("low", request.getReasoning_effort());
        assertEquals(2, request.getMessages().size());
        assertEquals("system", request.getMessages().get(0).getRole());
        assertTrue(request.getMessages().get(0).getContent().contains("ShopNest"));
        assertEquals("user", request.getMessages().get(1).getRole());
        String userPrompt = request.getMessages().get(1).getContent();
        assertTrue(userPrompt.contains(product.getName()));
        assertTrue(userPrompt.contains(product.getDescription()));
        assertTrue(userPrompt.contains("13.33"));
    }

    private static Product buildProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("13.33"));
        return product;
    }
}
