package com.practice.productcatalogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GroqChatMessageDto {
    private String role;
    private String content;
}
