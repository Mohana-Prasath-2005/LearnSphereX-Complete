package com.learnspherex.auth;

import java.time.*;
import java.util.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 50)
	private String username;
	@Column(nullable = false, unique = true, length = 120)
	private String email;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false, length = 80)
	private String firstName;
	@Column(nullable = false, length = 80)
	private String lastName;
	@Column(length = 20)
	private String phone;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountStatus status = AccountStatus.ACTIVE;
	@Column(nullable = false)
	private boolean accountLocked = false;
	private Instant lastLogin;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	@Column(nullable = false)
	private Instant updatedAt;
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<UserRole> userRoles = new HashSet<>();

	public User(String u, String e, String p, String f, String l, String ph) {
		username = u;
		email = e;
		password = p;
		firstName = f;
		lastName = l;
		phone = ph;
	}

	@PrePersist
	void created() {
		createdAt = updatedAt = Instant.now();
	}

	@PreUpdate
	void updated() {
		updatedAt = Instant.now();
	}

	public void addRole(Role r) {
		userRoles.add(new UserRole(this, r));
	}

	public void login() {
		lastLogin = Instant.now();
	}

	public void setActive(boolean a) {
		status = a ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
	}

	public void changePassword(String p) {
		password = p;
	}
}
