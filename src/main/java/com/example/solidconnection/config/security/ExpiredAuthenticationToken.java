package com.example.solidconnection.config.security;

public class ExpiredAuthenticationToken extends JwtAuthenticationToken {

    public ExpiredAuthenticationToken(String token) {
        super(token, null);
        setAuthenticated(false);
    }

    public ExpiredAuthenticationToken(String token, String subject) {
        super(token, subject);
        setAuthenticated(false);
    }
}
