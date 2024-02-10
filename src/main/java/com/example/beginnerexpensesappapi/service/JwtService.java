package com.example.beginnerexpensesappapi.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.SignatureException;
import java.util.Date;
import java.util.List;

@Service
@Log
public class JwtService {

    @Value("${jwt.secret}") // application.properties
    private String jwtSecretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // ASYMMETRIC
        // generate key pair for jwt
        // KeyPair keys = Keys.keyPairFor(SignatureAlgorithm.RS512);

        // SYMMETRIC 
        Key key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

    }

    public Claims extractClaims(String token) throws SignatureException {
        return (Claims) Jwts.parserBuilder()
            .setSigningKey(
                Keys.hmacShaKeyFor(jwtSecretKey.getBytes())
            )
            .build()
            .parse(token).getBody();    
    }


    public UsernamePasswordAuthenticationToken getAuthentication(String token) throws SignatureException {
        log.info("reviced this token from the client: {" + token + "}");
        Claims claims = extractClaims(token);
        String username = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("user")
        ); 
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public boolean verifyToken(String token, String username) throws SignatureException, ExpiredJwtException {
        Claims claims = extractClaims(token);
        Date expiryDate = claims.getExpiration();
        String claimedUsername = claims.getSubject();
        // TODO
        Date currentDate = new Date();
        if (expiryDate.before(currentDate)) {
             log.info(username + " - invalid token: expired");
             throw new ExpiredJwtException(null, claims, claimedUsername);
         }
        if (!claimedUsername.equals(username)) {
            log.info(username + " - invalid token: username mismatch");
            throw new JwtException("usernmae mismatch in jwt");
        }
        return true;
    }

}
