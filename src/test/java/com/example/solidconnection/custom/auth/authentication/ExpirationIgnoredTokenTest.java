package com.example.solidconnection.custom.auth.authentication;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("만료 무시 토큰 테스트")
class ExpirationIgnoredTokenTest {

    @Test
    void 인증_정보에_저장된_토큰을_반환한다() {
        // given
        String token = "token123";
        ExpirationIgnoredToken authentication = new ExpirationIgnoredToken(token);

        // when
        String result = authentication.getToken();

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    void 인증_정보에_저장된_토큰의_subject_를_반환한다() {
        // given
        String subject = "subject321";
        String token = createToken(subject);
        ExpirationIgnoredToken authentication = new ExpirationIgnoredToken(token, subject);

        // when
        String result = authentication.getSubject();

        // then
        assertThat(result).isEqualTo(subject);
    }

    private String createToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, "secret")
                .compact();
    }
}
