package com.example.solidconnection.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public abstract class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String credentials;

    private final Object principal;

    public JwtAuthenticationToken(String credentials, Object principal) {
        super(null);
        this.credentials = credentials;
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
