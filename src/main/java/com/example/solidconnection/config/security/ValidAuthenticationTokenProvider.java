package com.example.solidconnection.config.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.util.JwtUtils.parseSubject;
import static org.springframework.data.util.CastUtils.cast;

@Component
@RequiredArgsConstructor
public class ValidAuthenticationTokenProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;
    private final JwtUserDetailsService jwtUserDetailsService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        ValidAuthenticationToken jwtAuth = cast(auth);
        String token = (String) jwtAuth.getCredentials();

        String username = parseSubject(token, jwtProperties.secret());
        JwtUserDetails userDetails = (JwtUserDetails) jwtUserDetailsService.loadUserByUsername(username);
        return new ValidAuthenticationToken(token, userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ValidAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
