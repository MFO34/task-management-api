package com.taskmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // 1. TOKEN OLUŞTUR
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    // 2. TOKEN OLUŞTUR (Claims ile)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)  // Email (username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. SIGNING KEY AL
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 4. TOKEN'DAN EMAIL ÇIKAR
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 5. TOKEN'DAN EXPIRATION DATE ÇIKAR
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 6. TOKEN'DAN HERHANGİ BİR CLAIM ÇIKAR
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 7. TOKEN'DAN TÜM CLAIMS'LERİ ÇIKAR
    // ✅ ESKİ API (daha stabil):
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }

    // 8. TOKEN EXPİRE OLMUŞ MU?
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 9. TOKEN GEÇERLİ Mİ?
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}