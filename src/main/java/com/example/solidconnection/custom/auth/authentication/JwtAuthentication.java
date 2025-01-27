package com.example.solidconnection.custom.auth.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public abstract class JwtAuthentication extends AbstractAuthenticationToken {

    private final String credentials;

    private final Object principal;

    public JwtAuthentication(String credentials, Object principal) {
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

    public final String getToken() {
        return (String) getCredentials();
    }
}
