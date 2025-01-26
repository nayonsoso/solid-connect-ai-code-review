package com.example.solidconnection.custom.auth.authentication;


import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;

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
