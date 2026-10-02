package com.chatflow.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key; private final long expirationSeconds;
 public JwtService(@Value("${security.jwt.secret}") String secret,@Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds){this.key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));this.expirationSeconds=expirationSeconds;}
 public String issue(String username){Instant now=Instant.now();return Jwts.builder().subject(username).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationSeconds))).signWith(key).compact();}
 public String username(String token){Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();return claims.getSubject();}
}