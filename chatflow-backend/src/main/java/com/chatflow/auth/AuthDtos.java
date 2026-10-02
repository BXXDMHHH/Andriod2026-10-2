package com.chatflow.auth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public final class AuthDtos {
 private AuthDtos(){}
 public record RegisterRequest(@NotBlank @Size(min=3,max=64) @Pattern(regexp="^[a-zA-Z0-9_.-]+$") String username,@NotBlank @Size(min=8,max=72) String password,@Size(max=80) String nickname){}
 public record LoginRequest(@NotBlank String username,@NotBlank String password){}
 public record AuthResponse(String accessToken,String tokenType,long expiresIn,Long userId,String username){}
 public record UserResponse(Long id,String username,String nickname){}
}