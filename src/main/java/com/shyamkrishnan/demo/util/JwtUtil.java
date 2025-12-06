package com.shyamkrishnan.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "YourSecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // extracting email from token
    public String getUserEmail(String token) {
        return getClaimData(token, Claims::getSubject);
    }

    // getting expiry date from token
    public Date getExpiryDate(String token) {
        return getClaimData(token, Claims::getExpiration);
    }

    // generic method for extracting claims
    public <T> T getClaimData(String token, Function<Claims, T> resolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return resolver.apply(claims);
    }

    // parsing token to get all claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // check if token has expird
    private Boolean checkExpiry(String token) {
        return getExpiryDate(token).before(new Date());
    }

    // generating new jwt token for user
    public String makeToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, email);
    }

    // building token with claims and subject
    private String buildToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours validity
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // validating token for a user
    public Boolean checkTokenValidity(String token, String email) {
        final String emailFromToken = getUserEmail(token);
        return (emailFromToken.equals(email) && !checkExpiry(token));
    }
}
