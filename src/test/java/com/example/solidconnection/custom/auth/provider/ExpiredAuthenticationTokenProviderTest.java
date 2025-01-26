package com.example.solidconnection.custom.auth.provider;

import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.auth.authentication.ExpiredAuthenticationToken;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.net.PasswordAuthentication;
import java.util.Date;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestContainerSpringBootTest
@DisplayName("인증되지 않은 토큰 provider 테스트")
class ExpiredAuthenticationTokenProviderTest {

    @Autowired
    private ExpiredAuthenticationTokenProvider expiredAuthenticationTokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void 처리할_수_있는_타입인지를_반환한다() {
        // given
        Class<?> supportedType = ExpiredAuthenticationToken.class;
        Class<?> notSupportedType = PasswordAuthentication.class;

        // when & then
        assertAll(
                () -> assertTrue(expiredAuthenticationTokenProvider.supports(supportedType)),
                () -> assertFalse(expiredAuthenticationTokenProvider.supports(notSupportedType))
        );
    }

    @Test
    void 만료된_토큰의_인증_정보를_반환한다() {
        // given
        String expiredToken = createExpiredToken();
        ExpiredAuthenticationToken expiredAuthenticationToken = new ExpiredAuthenticationToken(expiredToken);

        // when
        Authentication result = expiredAuthenticationTokenProvider.authenticate(expiredAuthenticationToken);

        // then
        assertAll(
                () -> assertThat(result).isInstanceOf(ExpiredAuthenticationToken.class),
                () -> assertThat(result.isAuthenticated()).isFalse()
        );
    }

    @Test
    void 유효하지_않은_토큰이면_예외_응답을_반환한다() {
        // given
        ExpiredAuthenticationToken expiredAuthenticationToken = new ExpiredAuthenticationToken("invalid token");

        // when & then
        assertThatCode(() -> expiredAuthenticationTokenProvider.authenticate(expiredAuthenticationToken))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(INVALID_TOKEN.getMessage());
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }
}
