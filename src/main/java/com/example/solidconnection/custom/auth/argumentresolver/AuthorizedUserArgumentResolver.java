package com.example.solidconnection.custom.auth.argumentresolver;

import com.example.solidconnection.custom.auth.userdetails.JwtUserDetails;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;

@Component
@RequiredArgsConstructor
public class AuthorizedUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthorizedUser.class)
                && parameter.getParameterType().equals(SiteUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        return getUserDetails().getSiteUser();
    }

    private JwtUserDetails getUserDetails() {
        try {
            return (JwtUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
        } catch (Exception e) {
            throw new CustomException(AUTHENTICATION_FAILED);
        }
    }
}
