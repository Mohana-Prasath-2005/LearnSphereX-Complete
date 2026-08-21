package com.learnspherex.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.learnspherex.auth.*;
import com.learnspherex.auth.User;

@Service
@RequiredArgsConstructor
public class LearnSphereXUserDetailsService implements UserDetailsService {
	private final UserRepository users;

	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		User u = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
				.password(u.getPassword())
				.authorities(u.getUserRoles().stream()
						.map(x -> new SimpleGrantedAuthority("ROLE_" + x.getRole().getName())).toList())
				.disabled(u.getStatus() != AccountStatus.ACTIVE).accountLocked(u.isAccountLocked()).build();
	}
}
