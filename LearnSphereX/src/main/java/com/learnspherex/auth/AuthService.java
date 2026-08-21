package com.learnspherex.auth;

import java.util.*;
import java.util.stream.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;
import com.learnspherex.common.ApiException;
import com.learnspherex.security.JwtService;

import jakarta.validation.Valid;

import com.learnspherex.audit.AuditService;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository users;
	private final RoleRepository roles;
	private final PasswordEncoder encoder;
	private final AuthenticationManager authManager;
	private final JwtService jwt;
	private final AuditService audit;

	@Transactional
	public AuthDtos.UserResponse register(AuthDtos.RegisterRequest r, String ip) {
		if (r.role() != RoleName.STUDENT)
			throw new ApiException(HttpStatus.FORBIDDEN, "Public registration may create only STUDENT accounts");
		return createUser(r, ip, null);
	}

	@Transactional
	public AuthDtos.UserResponse createStaffUser(AuthDtos.RegisterRequest r, String ip, Long actorId) {
		return createUser(r, ip, actorId);
	}

	// actorId is null for public self-registration (the new account is its own actor);
	// non-null when an admin creates a staff account on someone else's behalf, so the
	// audit trail correctly names the admin rather than the account just created.
	private AuthDtos.UserResponse createUser(AuthDtos.RegisterRequest r, String ip, Long actorId) {
		if (users.existsByUsername(r.username()) || users.existsByEmail(r.email()))
			throw new ApiException(HttpStatus.CONFLICT, "Username or email already exists");
		Role role = roles.findByName(r.role())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role is not configured"));
		User u = new User(r.username(), r.email(), encoder.encode(r.password()), r.firstName(), r.lastName(),
				r.phone());
		u.addRole(role);
		users.save(u);
		Long effectiveActorId = actorId != null ? actorId : u.getId();
		audit.record(effectiveActorId, "CREATE", "User", u.getId(), ip,
				actorId != null ? "Staff account created by admin" : "Account registered");
		return view(u);
	}

	@Transactional
	public AuthDtos.AuthResponse login(AuthDtos.LoginRequest r, String ip) {
		authManager.authenticate(new UsernamePasswordAuthenticationToken(r.username(), r.password()));
		User u = users.findByUsername(r.username()).orElseThrow();
		u.login();
		Set<RoleName> rs = roleNames(u);
		audit.record(u.getId(), "LOGIN", "User", u.getId(), ip, "Successful login");
		return new AuthDtos.AuthResponse(jwt.generateToken(u.getUsername(), rs), "Bearer", u.getId(), u.getUsername(),
				rs);
	}

	@Transactional(readOnly = true)
	public AuthDtos.UserResponse get(Long id) {
		return view(find(id));
	}

	@Transactional(readOnly = true)
	public List<AuthDtos.UserResponse> list() {
		return users.findAll().stream().map(this::view).toList();
	}

	@Transactional
	public AuthDtos.UserResponse setStatus(Long id, boolean active, Long actorId, String ip) {
		User u = find(id);
		u.setActive(active);
		audit.record(actorId, "UPDATE_STATUS", "User", u.getId(), ip,
				active ? "Account activated" : "Account deactivated");
		return view(u);
	}

	@Transactional(readOnly = true)
	public AuthDtos.UserResponse me(String username) {
		return users.findByUsername(username)
				.map(this::view)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
	}

	@Transactional
	public AuthDtos.UserResponse updateMyProfile(String username, AuthDtos.ProfileUpdateRequest r, String ip) {
		User u = users.findByUsername(username)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
		u.updateProfile(r.firstName(), r.lastName(), r.phone());
		audit.record(u.getId(), "UPDATE_PROFILE", "User", u.getId(), ip, "Profile updated");
		return view(u);
	}

	@Transactional
	public void changeMyPassword(String username, AuthDtos.ChangePasswordRequest r, String ip) {
		User u = users.findByUsername(username)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
		if (!encoder.matches(r.currentPassword(), u.getPassword()))
			throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
		u.changePassword(encoder.encode(r.newPassword()));
		audit.record(u.getId(), "CHANGE_PASSWORD", "User", u.getId(), ip, "Password changed");
	}

	private User find(Long id) {
		return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public static Set<RoleName> roleNames(User u) {
		return u.getUserRoles().stream().map(x -> x.getRole().getName()).collect(Collectors.toUnmodifiableSet());
	}

	private AuthDtos.UserResponse view(User u) {
		return new AuthDtos.UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(),
				u.getPhone(), u.getStatus(), roleNames(u));
	}
	
	
}
