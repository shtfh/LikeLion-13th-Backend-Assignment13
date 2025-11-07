package com.likelion.idtoken.filter;

import com.likelion.idtoken.service.GoogleJwkService;
import com.likelion.idtoken.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final GoogleJwkService googleJwkService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String token = authorizationHeader.substring(7);
                String keyId = extractKeyId(token);
                Map<String, Object> jsonWebKey = googleJwkService.findJsonWebKey(keyId);
                Claims claims = jwtService.validateToken(token, jsonWebKey);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                claims,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception exception) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractKeyId(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            Map<String, Object> headerMap = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(headerJson, Map.class);
            return (String) headerMap.get("kid");
        } catch (Exception exception) {
            throw new RuntimeException("토큰에서 키 ID를 추출하는데 실패했습니다");
        }
    }
}