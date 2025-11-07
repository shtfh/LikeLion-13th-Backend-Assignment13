package com.likelion.likelionjwt.oauth.application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;
import com.likelion.likelionjwt.global.jwt.JwtTokenProvider;
import com.likelion.likelionjwt.member.domain.Member;
import com.likelion.likelionjwt.member.domain.Role;
import com.likelion.likelionjwt.member.domain.repository.MemberRepository;
import com.likelion.likelionjwt.oauth.api.dto.KakaoUserInfo;
import com.likelion.likelionjwt.oauth.api.dto.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthLoginService {

    /*
    // 컨트롤러에서 받은 authorization code와 소셜 로그인 id를 받아 콘솔에 출력
    public void socialLogin(String code, String registrationId){
        System.out.println("code = " + code);
        System.out.println("registrationId = " + registrationId);
    }
    */

    @Value("${kakao.client-id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;

    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 인가 코드로 액세스 토큰 발급
    public String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", KAKAO_CLIENT_ID);
        body.add("redirect_uri", KAKAO_REDIRECT_URI);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_TOKEN_URL, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(response.getBody(), Map.class);
            return (String) map.get("access_token");
        }
        throw new RuntimeException("카카오 엑세스 토큰 발급 실패");
    }

    // 액세스 토큰으로 사용자 정보 요청
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_USERINFO_URL, HttpMethod.GET, request, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            return gson.fromJson(response.getBody(), KakaoUserInfo.class);
        }

        throw new RuntimeException("카카오 유저 정보 조회 실패");
    }

    // 사용자 정보 기반 회원가입 or 로그인 + JWT 발급
    public Token kakaoLoginOrSignUp(String code) {
        // 인가 코드로 Access Token 받기
        String kakaoAccessToken = getKakaoAccessToken(code);

        // Access Token으로 사용자 정보 요청
        KakaoUserInfo kakaoUser = getKakaoUserInfo(kakaoAccessToken);

        // 이메일 없을 때 대체 이메일 생성
        String email = (kakaoUser.getKakaoAccount() != null)
                ? kakaoUser.getKakaoAccount().getEmail()
                : null;

        if (email == null) {
            email = "kakao_" + kakaoUser.getId() + "@temporary.com";
        }

        // 닉네임, 프로필 이미지 추출
        String name = null;
        String picture = null;

        if (kakaoUser.getKakaoAccount() != null && kakaoUser.getKakaoAccount().getProfile() != null) {
            name = kakaoUser.getKakaoAccount().getProfile().getNickname();
            picture = kakaoUser.getKakaoAccount().getProfile().getProfileImageUrl();
        }

        // 기존 회원 조회 or 새로 생성
        Member member = memberRepository.findByEmail(email).orElse(null);

        if (member == null) {
            member = memberRepository.save(Member.builder()
                    .email(email)
                    .name(name)
                    .pictureUrl(picture)
                    .role(Role.ROLE_USER)
                    .build());
        }

        // JWT 생성 및 반환
        String jwt = jwtTokenProvider.generateToken(member);
        return new Token(jwt);
    }
}
