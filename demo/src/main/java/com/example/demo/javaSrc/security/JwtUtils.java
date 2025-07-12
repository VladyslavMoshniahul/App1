package com.example.demo.javaSrc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")      private String jwtSecret;
    @Value("${app.jwt.expiration-ms}") private long jwtExpirationMs;
    @Value("${app.jwt.issuer}")      private String jwtIssuer;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication auth) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        String role = auth.getAuthorities().stream()
                          .map(GrantedAuthority::getAuthority)
                          .findFirst().orElse("ROLE_STUDENT");

        return Jwts.builder()
                   .setSubject(auth.getName())
                   .setIssuer(jwtIssuer)
                   .setIssuedAt(now)
                   .setExpiration(exp)
                   .claim("role", role.replace("ROLE_",""))  // без префикса
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException|IllegalArgumentException e) {
            return false;
        }
    }
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

}
