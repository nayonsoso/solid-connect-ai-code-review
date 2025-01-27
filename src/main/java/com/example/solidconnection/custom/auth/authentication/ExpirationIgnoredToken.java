package com.example.solidconnection.custom.auth.authentication;

public class ExpirationIgnoredToken extends JwtAuthentication {

    public ExpirationIgnoredToken(String token) {
        super(token, null);
        setAuthenticated(false);
    }

    public ExpirationIgnoredToken(String token, String subject) {
        super(token, subject);
        setAuthenticated(false);
    }

    public String getSubject() {
        return (String) super.getPrincipal();
    }
}
