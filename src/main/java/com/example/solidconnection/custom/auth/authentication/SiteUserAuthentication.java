package com.example.solidconnection.custom.auth.authentication;


import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;

public class SiteUserAuthentication extends JwtAuthentication {

    public SiteUserAuthentication(String token) {
        super(token, null);
        setAuthenticated(false);
    }

    public SiteUserAuthentication(String token, JwtUserDetails principal) {
        super(token, principal);
        setAuthenticated(true);
    }
}
