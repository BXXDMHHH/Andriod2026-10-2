package com.chatflow.auth;
import com.chatflow.security.JwtService;
import com.chatflow.user.UserEntity;
import com.chatflow.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class AuthService {
 private final UserRepository users;private final PasswordEncoder encoder;private final JwtService jwt;private final long expiry;
 public AuthService(UserRepository users,PasswordEncoder encoder,JwtService jwt,@Value("${security.jwt.expiration-seconds:3600}")long expiry){this.users=users;this.encoder=encoder;this.jwt=jwt;this.expiry=expiry;}
 public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest r){if(users.existsByUsername(r.username()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Username already exists");return token(users.save(new UserEntity(r.username(),encoder.encode(r.password()),r.nickname())));}
 public AuthDtos.AuthResponse login(AuthDtos.LoginRequest r){UserEntity u=users.findByUsername(r.username()).filter(x->encoder.matches(r.password(),x.getPasswordHash())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password"));if(!"ACTIVE".equals(u.getStatus()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Account is disabled");return token(u);}
 public AuthDtos.UserResponse currentUser(String username){UserEntity u=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User no longer exists"));return new AuthDtos.UserResponse(u.getId(),u.getUsername(),u.getNickname());}
 private AuthDtos.AuthResponse token(UserEntity u){return new AuthDtos.AuthResponse(jwt.issue(u.getUsername()),"Bearer",expiry,u.getId(),u.getUsername());}
}