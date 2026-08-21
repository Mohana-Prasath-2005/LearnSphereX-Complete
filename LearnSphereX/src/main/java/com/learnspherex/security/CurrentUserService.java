package com.learnspherex.security;

import com.learnspherex.auth.User;
import com.learnspherex.auth.UserRepository;
import com.learnspherex.exception.UnauthorizedOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository users;

    public User currentUser(Authentication authentication) {
        return users.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedOperationException("Authenticated user not found"));
    }

    public boolean hasRole(Authentication authentication, String role) {
        String target = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(target::equals);
    }

    /**
     * Allows the request through only if the caller owns {@code ownerUserId}
     * (i.e. it equals their own User.id) or holds one of the given roles.
     */
    public void assertOwnerOrRole(Authentication authentication, Long ownerUserId, String... allowedRoles) {
        if (ownerUserId != null && ownerUserId.equals(currentUser(authentication).getId())) {
            return;
        }
        for (String role : allowedRoles) {
            if (hasRole(authentication, role)) {
                return;
            }
        }
        throw new UnauthorizedOperationException("You are not allowed to access this resource");
    }

    /**
     * Same as {@link #assertOwnerOrRole}, but for resources that can legitimately
     * belong to more than one user id (e.g. the submitting student OR the
     * project's assigned trainer).
     */
    public void assertAnyOwnerOrRole(Authentication authentication, List<Long> ownerUserIds, String... allowedRoles) {
        Long currentId = currentUser(authentication).getId();
        for (Long ownerId : ownerUserIds) {
            if (ownerId != null && ownerId.equals(currentId)) {
                return;
            }
        }
        for (String role : allowedRoles) {
            if (hasRole(authentication, role)) {
                return;
            }
        }
        throw new UnauthorizedOperationException("You are not allowed to access this resource");
    }
}
