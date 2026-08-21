package com.learnspherex.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnspherex.audit.AuditService;
import com.learnspherex.auth.UserRepository;

/**
 * The session-based (Thymeleaf) login goes through Spring Security's default
 * UsernamePasswordAuthenticationFilter, which never touches AuthService.login() -
 * so lastLogin/audit tracking only ever happened for the JWT API login path.
 * This handler gives the web login path the same side effects.
 *
 * Talks to UserRepository/AuditService directly rather than AuthService: AuthService
 * depends on the AuthenticationManager bean, which SecurityConfig itself produces -
 * routing through AuthService here would be a circular bean dependency
 * (SecurityConfig -> this handler -> AuthService -> AuthenticationManager -> SecurityConfig).
 */
@Component
public class FormLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public FormLoginSuccessHandler(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        setAlwaysUseDefaultTargetUrl(true);
        setDefaultTargetUrl("/dashboard");
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        userRepository.findByUsername(authentication.getName()).ifPresent(u -> {
            u.login();
            auditService.record(u.getId(), "LOGIN", "User", u.getId(),
                    request.getRemoteAddr(), "Successful login (web)");
        });

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
