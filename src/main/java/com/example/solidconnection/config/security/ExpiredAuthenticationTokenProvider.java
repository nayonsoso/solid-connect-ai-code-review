package com.example.solidconnection.config.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubjectIgnoringExpiration;
import static org.springframework.data.util.CastUtils.cast;

@Component
@RequiredArgsConstructor
public class ExpiredAuthenticationTokenProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        JwtAuthenticationToken jwtAuth = cast(auth);
        String token = (String) jwtAuth.getCredentials();
        String subject = parseSubjectIgnoringExpiration(token, jwtProperties.secret());

        return new ExpiredAuthenticationToken(token, subject);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ExpiredAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
