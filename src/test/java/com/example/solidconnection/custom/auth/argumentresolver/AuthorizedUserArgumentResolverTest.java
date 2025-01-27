package com.example.solidconnection.custom.auth.argumentresolver;

import com.example.solidconnection.custom.auth.authentication.SiteUserAuthentication;
import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.example.solidconnection.e2e.DynamicFixture.createSiteUser;
import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("인증된 사용자 argument resolver 테스트")
class AuthorizedUserArgumentResolverTest {

    @Autowired
    private AuthorizedUserArgumentResolver authorizedUserArgumentResolver;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void security_context_에_저장된_인증된_사용자를_반환한다() throws Exception {
        // given
        SiteUser siteUser = siteUserRepository.save(createSiteUser());
        JwtUserDetails userDetails = new JwtUserDetails(siteUser);
        SiteUserAuthentication authentication = new SiteUserAuthentication("token", userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        SiteUser resolveSiteUser = (SiteUser) authorizedUserArgumentResolver.resolveArgument(null, null, null, null);

        // then
        assertThat(resolveSiteUser).isEqualTo(siteUser);
    }

    @Test
    void security_context_에_저장된_사용자가_없으면_null_을_반환한다() throws Exception {
        // when, then
        assertThat(authorizedUserArgumentResolver.resolveArgument(null, null, null, null)).isNull();
    }
}
