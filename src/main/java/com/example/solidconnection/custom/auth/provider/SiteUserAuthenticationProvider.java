package com.example.solidconnection.custom.auth.provider;


import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.auth.authentication.SiteUserAuthentication;
import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;
import com.example.solidconnection.custom.auth.userdetails.JwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubject;
import static org.springframework.data.util.CastUtils.cast;

@Component
@RequiredArgsConstructor
public class SiteUserAuthenticationProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;
    private final JwtUserDetailsService jwtUserDetailsService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        SiteUserAuthentication jwtAuth = cast(auth);
        String token = (String) jwtAuth.getCredentials();

        String username = parseSubject(token, jwtProperties.secret());
        JwtUserDetails userDetails = (JwtUserDetails) jwtUserDetailsService.loadUserByUsername(username);
        return new SiteUserAuthentication(token, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SiteUserAuthentication.class.isAssignableFrom(authentication);
    }
}
