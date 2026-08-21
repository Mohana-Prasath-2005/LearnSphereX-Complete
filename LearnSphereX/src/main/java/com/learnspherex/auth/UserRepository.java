package com.learnspherex.auth; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param; public interface UserRepository extends JpaRepository<User,Long>{Optional<User> findByUsername(String username); Optional<User> findByEmail(String email); boolean existsByUsername(String username); boolean existsByEmail(String email);
    // userRoles is LAZY; callers outside an open transaction (most services here don't declare one)
    // need roles pre-fetched in the same query to avoid a LazyInitializationException.
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
}
