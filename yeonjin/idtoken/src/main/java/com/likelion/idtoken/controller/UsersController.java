package com.likelion.idtoken.controller;

import com.likelion.idtoken.dto.ApiResponse;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> retrieveUserInfo(
            @AuthenticationPrincipal OidcUser oidcUser,
            @AuthenticationPrincipal Claims claims) {

        if (oidcUser != null) {
            Map<String, Object> data = Map.of(
                    "authType", "session",
                    "subject", oidcUser.getSubject(),
                    "email", oidcUser.getEmail(),
                    "name", oidcUser.getFullName()
            );

            return ApiResponse.success("사용자 정보 조회 성공", data);
        }

        if (claims != null) {
            Map<String, Object> data = Map.of(
                    "authType", "bearer_token",
                    "subject", claims.getSubject(),
                    "email", claims.get("email", String.class),
                    "name", claims.get("name", String.class),
                    "issuer", claims.getIssuer()
            );

            return ApiResponse.success("사용자 정보 조회 성공", data);
        }

        throw new RuntimeException("인증되지 않은 사용자입니다");
    }
}