package com.example.solidconnection.custom.auth.authentication;

import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUser;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 인증 정보 테스트")
class SiteUserAuthenticationTest {

    @Test
    void 인증_정보에_저장된_토큰을_반환한다() {
        // given
        String token = "token";
        SiteUserAuthentication authentication = new SiteUserAuthentication(token);

        // when
        String result = authentication.getToken();

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    void 인증_정보에_저장된_사용자_정보를_반환한다() {
        // given
        JwtUserDetails userDetails = new JwtUserDetails(createSiteUser());
        SiteUserAuthentication authentication = new SiteUserAuthentication("token", userDetails);

        // when & then
        JwtUserDetails details = (JwtUserDetails) authentication.getPrincipal();

        // then
        assertThat(details)
                .extracting("siteUser")
                .extracting("id")
                .isEqualTo(userDetails.getSiteUser().getId());
    }
}
