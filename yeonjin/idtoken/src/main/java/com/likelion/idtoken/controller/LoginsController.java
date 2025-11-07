package com.likelion.idtoken.controller;

import com.likelion.idtoken.dto.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/logins")
public class LoginsController {

    @GetMapping("/success")
    public ApiResponse<Map<String, Object>> handleLoginSuccess(@AuthenticationPrincipal OidcUser oidcUser) {

        String idToken = oidcUser.getIdToken().getTokenValue();

        Map<String, Object> data = Map.of(
                "idToken", idToken,
                "issuer", oidcUser.getIssuer().toString(),
                "subject", oidcUser.getSubject(),
                "email", oidcUser.getEmail(),
                "name", oidcUser.getFullName(),
                "instruction", "Copy the 'idToken' value above and use it in Postman: Authorization -> Bearer Token"
        );

        return ApiResponse.success("로그인 성공", data);
    }
}