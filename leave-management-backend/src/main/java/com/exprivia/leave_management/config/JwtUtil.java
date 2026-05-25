package com.exprivia.leave_management.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
   @Value("${jwt.secret}")
   private String secret;
   private static final long EXPIRATION_MS = 86400000L;

   private SecretKey getKey() {
      return Keys.hmacShaKeyFor(this.secret.getBytes());
   }

   public String generateToken(Long employeeId, String email, String role) {
      return Jwts.builder().subject(email).claim("employeeId", employeeId).claim("role", role).issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)).signWith(this.getKey()).compact();
   }

   public Claims extractClaims(String token) {
      return (Claims) Jwts.parser().verifyWith(this.getKey()).build().parseSignedClaims(token).getPayload();
   }

   public boolean isTokenValid(String token) {
      try {
         Claims claims = this.extractClaims(token);
         return claims.getExpiration().after(new Date());
      } catch (Exception var3) {
         return false;
      }
   }

   public Long extractEmployeeId(String token) {
      Claims claims = this.extractClaims(token);
      Number employeeId = (Number) claims.get("employeeId", Number.class);
      return employeeId != null ? employeeId.longValue() : null;
   }
}
