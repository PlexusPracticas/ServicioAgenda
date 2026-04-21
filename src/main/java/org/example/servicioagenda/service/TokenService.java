package org.example.servicioagenda.service;
import org.example.servicioagenda.dto.request.TokenRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenService {
    private final RestTemplate restTemplate;

    public TokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void decrypt(String token) {
        TokenRequest request = new TokenRequest();
        request.setToken(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TokenRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.exchange("http://localhost:8083/auth/decrypt", HttpMethod.POST, entity, Object.class);
    }
}