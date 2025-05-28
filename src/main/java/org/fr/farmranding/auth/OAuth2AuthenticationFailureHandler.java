package org.fr.farmranding.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                        AuthenticationException exception) throws IOException {
        
        log.error("OAuth2 인증 실패: {}", exception.getMessage(), exception);
        
        String errorMessage = "OAuth2 인증에 실패했습니다.";
        String errorDetail = exception.getMessage();
        
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            errorDetail = oauth2Exception.getError().getDescription();
            log.error("OAuth2 Error Code: {}", oauth2Exception.getError().getErrorCode());
            log.error("OAuth2 Error Description: {}", oauth2Exception.getError().getDescription());
        }
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = String.format(
                "{\"success\": false, \"code\": \"FR201\", \"message\": \"%s\", \"detail\": \"%s\"}",
                errorMessage, errorDetail
        );
        
        response.getWriter().write(jsonResponse);
    }
} 