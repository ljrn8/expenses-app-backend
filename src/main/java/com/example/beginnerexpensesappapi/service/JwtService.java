package com.example.beginnerexpensesappapi.service;

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
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;

@Service
public class JwtService { // doing shit to jwt's

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public String generateToken(Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);


        // generate key pair for jwt
        KeyPair keys = Keys.keyPairFor(SignatureAlgorithm.ES512);

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
                .signWith(keys.getPrivate())
                .compact();

        // TODO 
        // do i sign with private ? 
        // where do i add the public key ? 
        // where do i keep the keypair on the server
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();

        // TODO password is here
        return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
    }

}
