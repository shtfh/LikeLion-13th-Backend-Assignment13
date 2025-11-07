package com.likelion.likelionjwt.oauth.api;

import com.likelion.likelionjwt.oauth.api.dto.Token;
import com.likelion.likelionjwt.oauth.application.AuthLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login/oauth2")
public class AuthLoginController {

    private final AuthLoginService authLoginService;

    // 기존 테스트용 코드 주석처리
    /*
    @GetMapping("/code/{registrationId}")
    public void kakaoLogin(@RequestParam(name = "code", required = true) String code,
                            @PathVariable(value = "registrationId") String registrationId) {
        authLoginService.socialLogin(code, registrationId);
    }
    */

    @GetMapping("/code/kakao")
    public Token kakaoCallback(@RequestParam(name = "code") String code) {
        // 사용자 정보 조회 + 회원 저장 + JWT 생성
        return authLoginService.kakaoLoginOrSignUp(code);
    }
}