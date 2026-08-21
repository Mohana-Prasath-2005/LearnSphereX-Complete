package com.learnspherex.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter filter;

    private final LearnSphereXUserDetailsService details;

    private final FormLoginSuccessHandler formLoginSuccessHandler;


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


    @Bean
    AuthenticationProvider provider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(details);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


    /**
     * /api/** is a stateless JWT API: no session, no CSRF (CSRF only matters
     * when the browser silently attaches ambient credentials like cookies;
     * a bearer token in an explicit header isn't ambient, so it doesn't need it).
     */
    @Bean
    @Order(1)
    SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http

            .securityMatcher("/api/**")

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/**")
                .permitAll()

                .requestMatchers(
                    "/api/payments/**",
                    "/api/fee-plans/**"
                )
                .hasRole("ADMIN")

                .anyRequest()
                .authenticated()
            )

            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Unauthorized"
                        )
                )
            )

            .addFilterBefore(
                filter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }


    /**
     * Everything else is the session-based Thymeleaf UI: real HTTP session,
     * CSRF protection enabled (Thymeleaf auto-injects the token into every
     * th:action form via the Spring Security Thymeleaf dialect).
     */
    @Bean
    @Order(2)
    SecurityFilterChain webFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
            )

            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/css/**"
                )
                .permitAll()

                .anyRequest()
                .authenticated()
            )

            .formLogin(form ->
                form
                    .loginPage("/login")
                    .successHandler(formLoginSuccessHandler)
                    .permitAll()
            )

            .exceptionHandling(exception ->
                exception.accessDeniedHandler(
                    (request, response, accessDeniedException) -> {

                        response.sendRedirect(
                            request.getContextPath()
                            + "/dashboard?error=accessDenied"
                        );
                    }
                )
            )

            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }
}
