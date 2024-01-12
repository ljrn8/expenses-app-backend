package com.example.beginnerexpensesappapi;

import com.example.beginnerexpensesappapi.service.JwtService;
import com.example.beginnerexpensesappapi.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Takes in JWTs from the user and sets the security context holder with an authorization obj from it
 */
@Component
@RequiredArgsConstructor
@Slf4j // see in logs
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;


    /// USER MADE REQEUEST TO SERVER WITH A JWT ->> ONLY SUBSEQUENT CALLS (not authentication)
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {

            // get encrypted JWT from user request
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {

                // sets the security context ( Authentication manager ( authentication ) ) for this servlet
                UsernamePasswordAuthenticationToken authentication = jwtService.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException ex) {
            System.out.println("!!!!!!!!!!!!!!");
        }

        filterChain.doFilter(request, response);
    }

    /// JWT SHIT
    // HTTP request headers = JSON proprties EG "method: POST" ect
    //
    // "authentication: Bearer awtoih9wwayyryearye.aerawryyeauyeaurayaeye.arweyaeyeatueauearyawyarwy " << b64 encoded JWT (not encrypted) with public key in its signature
    //                          ^ encoded headers    ^ encoded payload     ^ encrypted signature
    //
    // "The signature is created using the header, the payload, and the secret that is saved on the server."
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // literally GET THE JWT FROM A HTTP REQUEST ('7' is excluding the 'bearer ' part)
        }
        return null;
    }


}
