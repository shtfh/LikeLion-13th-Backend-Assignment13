package com.likelion.idtoken.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GoogleJwkService {

    private static final String GOOGLE_JWK_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> findJsonWebKey(String keyId) {
        try {
            String response = restTemplate.getForObject(GOOGLE_JWK_URL, String.class);
            Map<String, Object> jsonWebKeySet = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jsonWebKeySet.get("keys");

            return keys.stream()
                    .filter(key -> keyId.equals(key.get("kid")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("해당 키 ID에 대한 JWK를 찾을 수 없습니다: " + keyId));
        } catch (Exception exception) {
            throw new RuntimeException("Google JWK를 가져오는데 실패했습니다: " + exception.getMessage());
        }
    }
}