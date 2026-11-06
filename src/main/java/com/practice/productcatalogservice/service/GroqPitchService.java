package com.practice.productcatalogservice.service;

import com.practice.productcatalogservice.clients.GroqClient;
import com.practice.productcatalogservice.dto.GroqChatMessageDto;
import com.practice.productcatalogservice.dto.GroqChatRequestDto;
import com.practice.productcatalogservice.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class GroqPitchService implements IPitchService {
    private final GroqClient groqClient;

    @Override
    public String getPitch(Product product) {
        GroqChatRequestDto requestDto = getGroqChatRequestDto();
        requestDto.getMessages().add(new GroqChatMessageDto("user", String.format("Write a short headline to display on UI for the following product: %s. The product is described as: %s. The price is $%.2f.", product.getName(), product.getDescription(), product.getPrice())));
        try {
            return groqClient.getPitch(requestDto);
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
