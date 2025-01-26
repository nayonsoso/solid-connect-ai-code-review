package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.auth.provider.ValidAuthenticationTokenProvider;
import com.example.solidconnection.custom.auth.provider.ExpiredAuthenticationTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

@RequiredArgsConstructor
@Configuration
public class AuthenticationManagerConfig {

    private final ValidAuthenticationTokenProvider validAuthenticationTokenProvider;
    private final ExpiredAuthenticationTokenProvider expiredAuthenticationTokenProvider;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(
                validAuthenticationTokenProvider,
                expiredAuthenticationTokenProvider
        );
    }
}
