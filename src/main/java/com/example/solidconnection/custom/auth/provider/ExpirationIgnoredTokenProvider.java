package com.example.solidconnection.custom.auth.provider;


import com.example.solidconnection.custom.auth.authentication.ExpirationIgnoredToken;
import com.example.solidconnection.custom.auth.authentication.JwtAuthentication;
import com.example.solidconnection.config.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubjectIgnoringExpiration;
import static org.springframework.data.util.CastUtils.cast;

@Component
@RequiredArgsConstructor
public class ExpirationIgnoredTokenProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        JwtAuthentication jwtAuthentication = cast(auth);
        String token = (String) jwtAuthentication.getCredentials();
        String subject = parseSubjectIgnoringExpiration(token, jwtProperties.secret());

        return new ExpirationIgnoredToken(token, subject);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ExpirationIgnoredToken.class.isAssignableFrom(authentication);
    }
}
