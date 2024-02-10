package com.example.beginnerexpensesappapi.security;

import com.example.beginnerexpensesappapi.service.CustomerService;
import com.example.beginnerexpensesappapi.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;

@Log
@Component
@RequiredArgsConstructor // subsitutes autow
@Slf4j // see in logs
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // autowires in args constructor (good practice?)

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            
            log.info("recieved request" + request.toString() + "about to extract jwt .. ");
            // get encrypted JWT from user request
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                UsernamePasswordAuthenticationToken authentication = jwtService.getAuthentication(jwt);
                String username = (String) authentication.getPrincipal();
                if (!jwtService.verifyToken(jwt, username)) throw new JwtException("invalid token");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (SignatureException | JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Invalid signature: " + e.getMessage());
        }
    }

    /// JWTs
    // HTTP request headers = JSON proprties EG "method: POST" ect
    //
    // "authentication: Bearer awtoih9wwayyryearye.aerawryyeauyeaurayaeye.arweyaeyeatueauearyawyarwy " << b64 encoded JWT (not encrypted) with public key in its signature
    //                          ^ encoded headers    ^ encoded payload     ^ encrypted signature
    //
    // "The signature is created using the header, the payload, and the secret that is saved on the server."
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.info("found this in the authorization header: " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); 
        }
        return null;
    }


}
