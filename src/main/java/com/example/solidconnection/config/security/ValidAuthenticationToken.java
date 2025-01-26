package com.example.solidconnection.config.security;


public class ValidAuthenticationToken extends JwtAuthenticationToken {

    public ValidAuthenticationToken(String token) {
        super(token, null);
        setAuthenticated(false);
    }

    public ValidAuthenticationToken(String token, JwtUserDetails principal) {
        super(token, principal);
        setAuthenticated(true);
    }
}
