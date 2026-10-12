package com.practice.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroqChatRequestDto {
    private String model;
    private String reasoning_effort;
    private int max_completion_tokens;
    private List<GroqChatMessageDto> messages;
}
