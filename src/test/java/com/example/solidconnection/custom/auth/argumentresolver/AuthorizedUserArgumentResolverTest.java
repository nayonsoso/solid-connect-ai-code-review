package com.example.solidconnection.custom.auth.argumentresolver;

import com.example.solidconnection.custom.auth.authentication.ValidAuthenticationToken;
import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@TestContainerSpringBootTest
@DisplayName("인증된 사용자 argument resolver 테스트")
class AuthorizedUserArgumentResolverTest {

    @Autowired
    private AuthorizedUserArgumentResolver authorizedUserArgumentResolver;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Test
    void security_context_에_저장된_인증된_사용자를_반환한다() throws Exception {
        // given
        SiteUser siteUser = siteUserRepository.save(createSiteUser());
        JwtUserDetails userDetails = new JwtUserDetails(siteUser);
        ValidAuthenticationToken authentication = new ValidAuthenticationToken("token", userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        SiteUser resolveSiteUser = (SiteUser) authorizedUserArgumentResolver.resolveArgument(null, null, null, null);

        // then
        assertThat(resolveSiteUser).isEqualTo(siteUser);
    }

    @Test
    void security_context_에_저장된_사용자가_없으면_예외_응답을_반환한다() {
        // when, then
        assertThatCode(() -> authorizedUserArgumentResolver.resolveArgument(null, null, null, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "email",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE,
                AuthType.KAKAO
        );
    }
}
