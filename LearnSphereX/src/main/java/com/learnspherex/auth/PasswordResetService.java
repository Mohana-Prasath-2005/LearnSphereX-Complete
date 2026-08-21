package com.learnspherex.auth;
import com.learnspherex.exception.*; import com.learnspherex.notification.event.NotificationEvent; import org.springframework.context.ApplicationEventPublisher; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.*; import java.util.*;
@Service
public class PasswordResetService {
 private final UserRepository users; private final PasswordResetTokenRepository tokens; private final PasswordEncoder encoder; private final ApplicationEventPublisher eventPublisher;
 public PasswordResetService(UserRepository u,PasswordResetTokenRepository t,PasswordEncoder e,ApplicationEventPublisher ep){users=u;tokens=t;encoder=e;eventPublisher=ep;}

 /**
  * Deliberately does not reveal whether the email exists, and never returns the
  * token to the caller: the token is only ever sent out-of-band, by email.
  */
 @Transactional public void request(String email){
  users.findByEmail(email).ifPresent(u->{
   String token=UUID.randomUUID().toString();
   tokens.save(new PasswordResetToken(token,u,Instant.now().plus(Duration.ofMinutes(30))));
   eventPublisher.publishEvent(new NotificationEvent(u.getId(),u.getEmail(),"Password Reset Requested",
           "Use this code to reset your password (valid for 30 minutes): "+token,"PASSWORD_RESET"));
  });
 }

 @Transactional public void reset(String token,String newPassword){var t=tokens.findByToken(token).orElseThrow(()->new InvalidOperationException("Invalid reset token"));if(t.isUsed()||t.getExpiresAt().isBefore(Instant.now()))throw new InvalidOperationException("Reset token is expired or already used");t.getUser().changePassword(encoder.encode(newPassword));t.setUsed(true);tokens.save(t);}
}
