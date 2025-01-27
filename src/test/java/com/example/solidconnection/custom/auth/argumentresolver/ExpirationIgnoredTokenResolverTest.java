package com.example.solidconnection.custom.auth.argumentresolver;

import com.example.solidconnection.custom.auth.authentication.ExpirationIgnoredToken;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("만료 시간을 검증하지 않는 토큰 argument resolver 테스트")
class ExpirationIgnoredTokenResolverTest {

    @Autowired
    private ExpirationIgnoredTokenResolver expirationIgnoredTokenResolver;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void security_context_에_저장된_만료시간을_검증하지_않는_토큰을_반환한다() throws Exception {
        // given
        ExpirationIgnoredToken authentication = new ExpirationIgnoredToken("token");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        ExpirationIgnoredToken expirationIgnoredToken = (ExpirationIgnoredToken) expirationIgnoredTokenResolver.resolveArgument(null, null, null, null);

        // then
        assertThat(expirationIgnoredToken.getToken()).isEqualTo("token");
    }

    @Test
    void security_context_에_저장된_만료시간을_검증하지_않는_토큰이_없으면_null_을_반환한다() throws Exception {
        // when, then
        assertThat(expirationIgnoredTokenResolver.resolveArgument(null, null, null, null)).isNull();
    }
}
