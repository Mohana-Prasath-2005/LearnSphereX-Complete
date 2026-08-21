package com.learnspherex.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.learnspherex.auth.*;

@Configuration
public class RoleSeeder {
    @Bean
    CommandLineRunner rolesAndDevelopmentAdmin(RoleRepository roles, UserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            for (RoleName roleName : RoleName.values()) {
                roles.findByName(roleName).orElseGet(() -> roles.save(new Role(roleName)));
            }

            // Development account: remove this block before final deployment.
            if (!users.existsByUsername("admin")) {
                Role adminRole = roles.findByName(RoleName.ADMIN).orElseThrow();
                User admin = new User("admin", "admin@learnspherex.local", passwordEncoder.encode("Admin@123"), "System", "Admin", "0000000000");
                admin.addRole(adminRole);
                users.save(admin);
            }
        };
    }
}
