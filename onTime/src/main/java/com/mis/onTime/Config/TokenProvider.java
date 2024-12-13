package com.mis.onTime.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import com.mis.onTime.Modal.User;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenProvider {

    private final SecretKey jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000); 

        return Jwts.builder()
            .setSubject(user.getGoogleId())
            .claim("email", user.getEmail())
            .claim("username", user.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
            .compact();
    }

    public String getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.getSubject();
    }
public String getUserEmailFromJWT(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(jwtSecretKey)
        .parseClaimsJws(token)
        .getBody();
    return claims.get("email", String.class); 
}

    public String getUserNameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.get("username", String.class); 
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
        }
        return false;
    }
}