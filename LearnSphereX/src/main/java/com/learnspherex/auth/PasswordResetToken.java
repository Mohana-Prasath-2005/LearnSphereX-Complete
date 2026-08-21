package com.learnspherex.auth;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="password_reset_tokens") @Getter @Setter @NoArgsConstructor
public class PasswordResetToken { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true,length=100) private String token; @ManyToOne(fetch=FetchType.LAZY,optional=false) private User user; @Column(nullable=false) private Instant expiresAt; @Column(nullable=false) private boolean used=false; public PasswordResetToken(String t,User u,Instant e){token=t;user=u;expiresAt=e;} }
