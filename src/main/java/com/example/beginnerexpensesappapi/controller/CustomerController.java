package com.example.beginnerexpensesappapi.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.beginnerexpensesappapi.Customer;
import com.example.beginnerexpensesappapi.CustomerRepository;
import com.example.beginnerexpensesappapi.service.CustomerService;
import com.example.beginnerexpensesappapi.service.JwtService;
import com.mongodb.lang.NonNull;

import jakarta.annotation.Nonnull;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Log
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

    // http://localhost:8080/customers
    @GetMapping("/customers")
    public CollectionModel<EntityModel<Customer>> all() {
        List<EntityModel<Customer>> customers = repository.findAll().stream() //
                .map(assembler::toModel) //
                .collect(Collectors.toList());
        
        return CollectionModel.of(customers, linkTo(methodOn(
                CustomerController.class).all()).withSelfRel());
    }

   
    /// !! protected endpoints /// 
    // DONT WORRY (every other route requries authentication as per secfilter chain)
    // TODO give this shit its own controller

    @DeleteMapping("/customers/{userName}")
    @PreAuthorize("#userName == authentication.principal.username")
    void delete(@PathVariable String userName) {
        repository.deleteById(userName);
    }
 
    @GetMapping("/customers/{userName}")
    @PreAuthorize("#userName == authentication.principal.username")
    public EntityModel<Customer> get(@PathVariable String userName) throws CustomerNotFound {
        Customer customer = repository.findById(userName).orElseThrow(
                () -> new CustomerNotFound(userName));
        return assembler.toModel(customer);
    }

    ////// !! //////


    /* REGISTRATION - one of these (both) aint right

    @PutMapping("/customers/{userName}")
    @PreAuthorize("#userName == authentication.principal.username")
    Customer updateCustomerPurchases(@PathVariable String userName,
            @RequestBody HashMap<String, Integer> newPurchases) throws CustomerNotFound {
        Customer customer = repository.findById(userName).orElseThrow(
                () -> new CustomerNotFound(userName));
        customer.setPurchases(newPurchases);
        return repository.save(customer);
    }

    @PostMapping("/register")
    Customer registerNewCustomer(@RequestBody RegisterRequest request) {
        String userName = request.username();
        String plainTextPassword = request.password();
        String encodedPassword = passwordEncoder.encode(plainTextPassword);
        return customerService.registerNewCustomer(userName, encodedPassword);
    } 
    
     @PostMapping("/customers")
    Customer newCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }*/

    @Autowired
    private JwtService jwtService;

    @PostMapping("/verification")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        // authentication obj
        UsernamePasswordAuthenticationToken authenticationRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(
                    loginRequest.username(), loginRequest.password());
        try {
            log.info("about to try and authenticate a login");
            Authentication authenticationResponse =
                    this.authenticationProvider.authenticate(authenticationRequest);

            if (authenticationResponse.isAuthenticated()) {
                log.info("was correct and is autheticated - sending OK http and JWT");
                String jwt = jwtService.generateToken(authenticationResponse);
                return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .body("login successful, find jwt in authorization header (keep \'bearer \' in it)");

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

    public record RegisterRequest(String username, String password) { }
    public record LoginRequest(String username, String password) { }
}

