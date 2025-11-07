package com.likelion.idtoken.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";

    public Claims validateToken(String token, Map<String, Object> jsonWebKey) {
        try {
            PublicKey publicKey = convertToPublicKey(jsonWebKey);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            validateClaims(claims);

            return claims;
        } catch (Exception exception) {
            throw new RuntimeException("유효하지 않은 JWT 토큰입니다: " + exception.getMessage());
        }
    }

    private void validateClaims(Claims claims) {
        String issuer = claims.getIssuer();
        if (!GOOGLE_ISSUER.equals(issuer)) {
            throw new RuntimeException("유효하지 않은 발급자입니다: " + issuer);
        }

        String audience = claims.getAudience();
        if (!clientId.equals(audience)) {
            throw new RuntimeException("유효하지 않은 대상자입니다: " + audience);
        }

        Date expirationDate = claims.getExpiration();
        if (expirationDate.before(new Date())) {
            throw new RuntimeException("토큰이 만료되었습니다");
        }
    }

    private PublicKey convertToPublicKey(Map<String, Object> jsonWebKey) throws Exception {
        String modulusString = (String) jsonWebKey.get("n");
        String exponentString = (String) jsonWebKey.get("e");

        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulusString);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponentString);

        BigInteger modulus = new BigInteger(1, modulusBytes);
        BigInteger exponent = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(publicKeySpec);
    }
}