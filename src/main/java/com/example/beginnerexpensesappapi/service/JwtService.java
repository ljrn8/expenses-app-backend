package com.example.beginnerexpensesappapi.service;

import com.example.beginnerexpensesappapi.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;

@Service
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
        Key jwtPublicKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());


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
                .signWith(jwtPublicKey, SignatureAlgorithm.HS512)
                .compact();

    }


    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody().getSubject();
        return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
    }

    public boolean verifyToken(String token, Customer customer) {
        String username = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody().getSubject();
        return (username.equals(customer.getUsername())); // TODO this is a circular check ??????
    }



}
