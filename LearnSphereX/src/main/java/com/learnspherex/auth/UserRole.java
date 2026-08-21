package com.learnspherex.auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_roles", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "role_id" }))
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserRole {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	UserRole(User u, Role r) {
		user = u;
		role = r;
	}
}
