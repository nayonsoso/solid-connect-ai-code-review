package com.example.solidconnection.config.web;

import com.example.solidconnection.custom.auth.argumentresolver.AuthorizedUserArgumentResolver;
import com.example.solidconnection.custom.auth.argumentresolver.ExpirationIgnoredTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthorizedUserArgumentResolver authorizedUserArgumentResolver;
    private final ExpirationIgnoredTokenResolver expirationIgnoredTokenResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(List.of(
                authorizedUserArgumentResolver,
                expirationIgnoredTokenResolver
        ));
    }
}
