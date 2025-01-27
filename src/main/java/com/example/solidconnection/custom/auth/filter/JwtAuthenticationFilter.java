package com.example.solidconnection.custom.auth.filter;

import com.example.solidconnection.custom.auth.authentication.ExpirationIgnoredToken;
import com.example.solidconnection.custom.auth.authentication.JwtAuthentication;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.auth.authentication.SiteUserAuthentication;
import com.example.solidconnection.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.solidconnection.util.JwtUtils.parseTokenFromRequest;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = parseTokenFromRequest(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtAuthentication authToken = createAuthentication(token);
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private JwtAuthentication createAuthentication(String token) {
        if (JwtUtils.isExpired(token, jwtProperties.secret())) {
            return new ExpirationIgnoredToken(token);
        }
        return new SiteUserAuthentication(token);
    }
}
