package com.example.demo.javaSrc.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;
    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public void setSecret(String secret) {
        this.jwtSecret = secret;
    }

    public void setValidityMs(int validityMs) {
        this.jwtExpirationMs = validityMs;
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
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
                .claim("role", role.replace("ROLE_", ""))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpirationMs / 1000));
        cookie.setAttribute("SameSite", "None"); 
        return cookie;
    }

}
