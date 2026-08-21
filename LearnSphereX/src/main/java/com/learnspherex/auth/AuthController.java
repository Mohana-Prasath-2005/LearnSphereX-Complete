package com.learnspherex.auth;

import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.learnspherex.security.CurrentUserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final PasswordResetService resetService;
    private final CurrentUserService currentUserService;


    @PostMapping("/register")
    ResponseEntity<AuthDtos.UserResponse> register(
            @Valid @RequestBody AuthDtos.RegisterRequest r,
            HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.register(
                        r,
                        req.getRemoteAddr()));
    }


    @PostMapping("/login")
    AuthDtos.AuthResponse login(
            @Valid @RequestBody AuthDtos.LoginRequest r,
            HttpServletRequest req) {

        return service.login(
                r,
                req.getRemoteAddr());
    }


    @PostMapping("/password-reset/request")
    ResponseEntity<Void> requestReset(
            @RequestParam String email) {

        // Same response whether or not the email exists, and the token is
        // never returned here - it only ever goes out by email.
        resetService.request(email);

        return ResponseEntity.accepted().build();
    }


    @PostMapping("/password-reset/confirm")
    ResponseEntity<Void> confirmReset(
            @RequestParam String token,
            @RequestParam String newPassword) {

        resetService.reset(
                token,
                newPassword);

        return ResponseEntity
                .noContent()
                .build();
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    List<AuthDtos.UserResponse> list() {

        return service.list();
    }


    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    AuthDtos.UserResponse get(
            @PathVariable Long id) {

        return service.get(id);
    }


    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    AuthDtos.UserResponse status(
            @PathVariable Long id,
            @Valid @RequestBody
            AuthDtos.AccountStatusRequest r,
            Authentication a,
            HttpServletRequest req) {

        return service.setStatus(
                id,
                r.active(),
                currentUserService.currentUser(a).getId(),
                req.getRemoteAddr());
    }


    @PostMapping("/me/change-password")
    ResponseEntity<Void> changePassword(
            @Valid @RequestBody
            AuthDtos.ChangePasswordRequest r,
            Authentication a,
            HttpServletRequest req) {

        service.changeMyPassword(
                a.getName(),
                r,
                req.getRemoteAddr());

        return ResponseEntity
                .noContent()
                .build();
    }


    // ==================================================
    // CREATE STAFF USER - ADMIN ONLY
    // ==================================================

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<AuthDtos.UserResponse> createStaffUser(
            @Valid @RequestBody AuthDtos.RegisterRequest r,
            Authentication a,
            HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createStaffUser(
                        r,
                        req.getRemoteAddr(),
                        currentUserService.currentUser(a).getId()));
    }
}