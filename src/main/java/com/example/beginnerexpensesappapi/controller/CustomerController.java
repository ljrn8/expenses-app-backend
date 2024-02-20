package com.example.beginnerexpensesappapi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.example.beginnerexpensesappapi.DTO.PurchasesDTO;
import com.example.beginnerexpensesappapi.DTO.UsernamePasswordDTO;
import com.example.beginnerexpensesappapi.service.DTOService;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.beginnerexpensesappapi.domain.Customer;
import com.example.beginnerexpensesappapi.service.CustomerService;
import com.example.beginnerexpensesappapi.service.JwtService;

@Log
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DTOService dtoService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomerModelAssembler customerModelAssembler;

    @GetMapping(path = "/customers/me")
    public ResponseEntity<EntityModel<Customer>> get() throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("wasn't authenticated");
        }
        String username = authentication.getName();
        if (username == null) {
            throw new AuthenticationCredentialsNotFoundException("username in authentication was null");
        }
        return ResponseEntity.ok(
                customerModelAssembler.toModel(
                    customerService.loadCustomerByUsername(username)
                )
        );
    }

    @GetMapping(path = "/customers")
    public ResponseEntity<List<EntityModel<Customer>>> all() {
        return ResponseEntity.ok(
                customerService.fetchAll().stream().map(customer ->
                        customerModelAssembler.toModel(customer)).collect(Collectors.toList())
        );
    }

    @PutMapping(path = "/customers/me/purchases", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Customer>> updateCustomerPurchases(@RequestBody PurchasesDTO purchasesDTO) throws Exception {
        HashMap<String, Integer> newPurchases = dtoService.PurchasestoHash(purchasesDTO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            log.info("authentication object was not authenticated");
            return ResponseEntity.internalServerError().body(null);
        }
        String username = authentication.getName();
        Customer updatedCustomer = customerService.updatePurchases(username, newPurchases);
        return ResponseEntity.ok(customerModelAssembler.toModel(updatedCustomer));
    }


    /// REGISTRATION
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Customer>> registerNewCustomer(@RequestBody UsernamePasswordDTO registerRequest) {
        String userName = registerRequest.getUsername();
        String plainTextPassword = registerRequest.getPassword();
        String encodedPassword = passwordEncoder.encode(plainTextPassword);
        if (userName == null || plainTextPassword == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (customerService.customerExists(userName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(customerModelAssembler.toModel(
                customerService.registerNewCustomer(userName, encodedPassword)
        ));
    }

    /// AUTHENTICATION
    @PostMapping(path = "/verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody UsernamePasswordDTO loginRequest) {
        UsernamePasswordAuthenticationToken authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.getUsername(), loginRequest.getPassword());
        try {
            log.info("about to try and authenticate a login");
            Authentication authenticationResponse =
                    this.authenticationProvider.authenticate(authenticationRequest);

            if (authenticationResponse.isAuthenticated()) {
                log.info("was correct and is authenticated - sending OK http and JWT");
                String jwt = this.jwtService.generateToken(authenticationResponse);
                return ResponseEntity.ok("Bearer " + jwt);

            } else {
                log.info("was not authenticated but didnt throw Ex, - sedning UNUATHERIZED http");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (BadCredentialsException ex) {
            log.info("wrong password / username - sending BAD_REQ http");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + ex.getMessage());
        }

    }

    @GetMapping(path="/test")
    public ResponseEntity<EntityModel<Customer>> roseTest() throws UsernameNotFoundException {
        return ResponseEntity.ok(
                customerModelAssembler.toModel(
                        customerService.loadCustomerByUsername("rose")
                )
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> usernameNotFoundHandler(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("username not found: " + e);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<String> authenticationCredentialsHandler(AuthenticationCredentialsNotFoundException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("authentication empty error " + e);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> badToken(SignatureException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("bad token + e");
    }

}

