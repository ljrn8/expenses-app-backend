package com.example.beginnerexpensesappapi.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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

/*          old shit
    @PostMapping("/customers")
    Customer newCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }
*/

    public record RegisterRequest(String username, String password) { }

    @PostMapping("/register")
    Customer registerNewCustomer(@RequestBody RegisterRequest request) {
        String userName = request.username();
        String plainTextPassword = request.password();
        String encodedPassword = passwordEncoder.encode(plainTextPassword);
        return customerService.registerNewCustomer(userName, encodedPassword);
    }

    @DeleteMapping("/customers/{userName}")
    @PreAuthorize("hasRole('USER')")
    void delete(@PathVariable String userName) {
        repository.deleteById(userName);
    }
 
    @GetMapping("/customers/{userName}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<Customer> get(@PathVariable String userName) throws CustomerNotFound {
        Customer customer = repository.findById(userName).orElseThrow(
                () -> new CustomerNotFound(userName));
        return assembler.toModel(customer);
    }

    @PutMapping("/customers/{userName}")
    @PreAuthorize("hasRole('USER')")
    Customer updateCustomerPurchases(@PathVariable String userName,
            @RequestBody HashMap<String, Integer> newPurchases) throws CustomerNotFound {
        Customer customer = repository.findById(userName).orElseThrow(
                () -> new CustomerNotFound(userName));
        customer.setPurchases(newPurchases);
        return repository.save(customer);
    }


    

    @PostMapping("/verification")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {

        // authentication obj
        UsernamePasswordAuthenticationToken authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.username(), loginRequest.password()
        );



        // try with encoded pw?
        // UsernamePasswordAuthenticationToken authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
        //         loginRequest.username(), passwordEncoder.encode(loginRequest.password())
        // );

        log.info("got these in body: " + loginRequest.username() + " | " + loginRequest.password());

        try {

            log.info("about to try and authenticate a login");

            Authentication authenticationResponse =
                    this.authenticationProvider.authenticate(authenticationRequest);


            if (authenticationResponse.isAuthenticated()) {
                log.info("was correct and is autheticated - sending OK http");
            } else {
                log.info("was not authenticated but didnt throw Ex, - sedning OK http");
            }
            return new ResponseEntity<>(HttpStatus.OK);
 

        } catch(BadCredentialsException ex) {
            log.info("wrong password / username - sending BAD_REQ http");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // basically a local struct wiht auto assinged params (DTO?)
    public record LoginRequest(String username, String password) { }
}

