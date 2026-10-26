package com.practice.productcatalogservice.security;

import com.practice.productcatalogservice.dto.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserTokenFilter extends OncePerRequestFilter {
    private final RestTemplate restTemplate;

    public UserTokenFilter(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equals(HttpMethod.GET.name())) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer")) {
            try {
                String token = authorization.substring(7);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                String authServiceUrl = "http://localhost:8080/users/me";
                ResponseEntity<UserDto> userDto = restTemplate.exchange(authServiceUrl, HttpMethod.GET, entity, UserDto.class);
                if (userDto.getBody() != null && request.getMethod().equals(HttpMethod.DELETE.name())
                        && !userDto.getBody().getRole().equals("ADMIN")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("You are not authorized to delete products.");
                    return;
                }
            } catch (HttpClientErrorException.Unauthorized e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            } catch (RestClientException e) {
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                return;
            }
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
