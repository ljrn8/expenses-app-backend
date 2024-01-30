package com.example.beginnerexpensesappapi.controller;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.example.beginnerexpensesappapi.PurchasesDTO;
import com.example.beginnerexpensesappapi.UsernamePasswordDTO;
import com.example.beginnerexpensesappapi.service.DTOService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.beginnerexpensesappapi.Customer;
import com.example.beginnerexpensesappapi.CustomerRepository;
import com.example.beginnerexpensesappapi.service.CustomerService;
import com.example.beginnerexpensesappapi.service.JwtService;

@Log
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder; // bcrypt

    @Autowired
    private CustomerService customerService; // TODO should replace autowiring the repo

    @Autowired
    private CustomerModelAssembler assembler;

    @Autowired
    private DTOService dtoService;

    @Autowired
    private JwtService jwtService;


    // TODO give this shit its own controller
    // TODO fix this sinful method
    @GetMapping(path = "/customers/me") // i grab ur username in the jwt
    public ResponseEntity<Customer> get() {
        
        // jwt set to authentication by filter
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.internalServerError().body(null);
        }

        // Retrieve customer details based on the authenticated user
        String username = authentication.getName();
        if (username == null) {
            return ResponseEntity.internalServerError().body(null);
        };

        // TODO this NEEDS to be a customer service method
        Customer customer = null;
        try {
            customer = repository.findById(username).get();
        } catch(NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        }

        return customer != null ? ResponseEntity.ok(customer) 
            : ResponseEntity.internalServerError().body(null);
    }


    @PutMapping(path = "/customers/me/purchases", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Customer> updateCustomerPurchases(@RequestBody PurchasesDTO purchasesDTO) {

        log.info("received update purchases request with " + purchasesDTO);
        HashMap<String, Integer> newPurchases = dtoService.PurchasestoHash(purchasesDTO);
        log.info("DTO converted to this hashmap " + newPurchases.toString());

        // TODO redundant code (check auth and not null password in seperate service level method - easy)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            log.info("authentication object was not authenticated");
            return ResponseEntity.internalServerError().body(null);
        }
        String username = authentication.getName();

        Optional<Customer> optionalCustomer = repository.findById(username);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setPurchases(newPurchases);
            Customer updatedCustomer = repository.save(customer);
            return ResponseEntity.ok(updatedCustomer);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // REGISTRATION 

    public record RegisterRequest(String username, String password) { }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Customer> registerNewCustomer(@RequestBody RegisterRequest request) {
        String userName = request.username();
        String plainTextPassword = request.password();
        String encodedPassword = passwordEncoder.encode(plainTextPassword);
        
        if (userName == null || plainTextPassword == null) { 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
        }
        
        if (repository.existsById(userName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }

        return ResponseEntity.ok(customerService.registerNewCustomer(userName, encodedPassword));
    } 
    
    // AUTHENTICATION


    @PostMapping(path = "/verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody UsernamePasswordDTO loginRequest) {
        // authentication obj
        UsernamePasswordAuthenticationToken authenticationRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(
                    loginRequest.getUsername(), loginRequest.getPassword());

        log.info("recieved this from frontend: " + loginRequest.getUsername() + loginRequest.getPassword());

        try {
            log.info("about to try and authenticate a login");
            Authentication authenticationResponse =
                    this.authenticationProvider.authenticate(authenticationRequest);

            if (authenticationResponse.isAuthenticated()) {
                log.info("was correct and is autheticated - sending OK http and JWT");
                String jwt = this.jwtService.generateToken(authenticationResponse);

                // now in body
                return ResponseEntity.ok("Bearer " + jwt);

            } else {
                log.info("was not authenticated but didnt throw Ex, - sedning UNUATHERIZED http");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch(BadCredentialsException ex) {
            log.info("wrong password / username - sending BAD_REQ http");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication failed: " + ex.getMessage());
        }
    }

}

