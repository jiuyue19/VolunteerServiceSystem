package com.volunteer.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateAccessToken(Long userId, String username, String role) {
        return generateAccessToken(userId, username, role, null);
    }
    
    public String generateAccessToken(Long userId, String username, String role, String userType) {
        var builder = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration));
        
        if (userType != null) {
            builder.claim("userType", userType);
        }
        
        return builder.signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generateRefreshToken(Long userId, String username, String role) {
        return generateRefreshToken(userId, username, role, null);
    }
    
    public String generateRefreshToken(Long userId, String username, String role, String userType) {
        var builder = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration));
        
        if (userType != null) {
            builder.claim("userType", userType);
        }
        
        return builder.signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}