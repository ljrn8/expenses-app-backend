package com.example.beginnerexpensesappapi.service;

import com.example.beginnerexpensesappapi.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.java.Log;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Log
public class JwtService { // doing shit to jwt's


    @Value("${jwt.secret}") // allows me to change the value in application.properties
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

        // create jwt
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                // .signWith(

                //         Keys.hmacShaKeyFor(
                //                 jwtSecret.getBytes(StandardCharsets.UTF_8)
                //         ),
                //         SignatureAlgorithm.HS512
                // )
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

    }

    public Claims extractClaims(String token) throws SignatureException {
        return (Claims) Jwts.parserBuilder()
            .setSigningKey(
                Keys.hmacShaKeyFor(jwtSecretKey.getBytes())
            )
            // .requireIssuer("https://issuer.example.com")
            .build()
            .parse(token).getBody();    
    }


    public UsernamePasswordAuthenticationToken getAuthentication(String token) throws SignatureException {
        log.info("reviced this token from the client: {" + token + "}");
        Claims claims = extractClaims(token);
        String username = claims.getSubject();

        // all authorities are users - no admins
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("user")
        ); 
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public boolean verifyToken(String token, String username) throws SignatureException {
        Claims claims = extractClaims(token);
        Date expiryDate = claims.getExpiration();
        String claimedUsername = claims.getSubject();
        // TODO
        // if (expiryDate.after(new Date())) { 
        //     log.info(username + " - invalid token: expired");
        //     throw new ExpiredJwtException(null, claims, claimedUsername);
        // }
        if (!claimedUsername.equals(username)) {
            log.info(username + " - invalid token: username mismatch");
            throw new JwtException("usernmae mismatch in jwt");
        }
        return true;
    }



}
