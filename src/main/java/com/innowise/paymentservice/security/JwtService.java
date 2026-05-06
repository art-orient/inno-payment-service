package com.innowise.paymentservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_TYPE = "type";
  private static final String ACCESS = "access";

  @Value("${jwt.secret}")
  private String secret;

  public Long extractUserId(String token) {
    return extractAllClaims(token).get(CLAIM_USER_ID, Long.class);
  }

  public String extractRole(String token) {
    return extractAllClaims(token).get(CLAIM_ROLE, String.class);
  }

  public boolean isTokenValid(String token) {
    try {
      Claims claims = extractAllClaims(token);
      String type = claims.get(CLAIM_TYPE, String.class);
      return ACCESS.equals(type) && !isExpired(claims);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isExpired(Claims claims) {
    return claims.getExpiration().before(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
