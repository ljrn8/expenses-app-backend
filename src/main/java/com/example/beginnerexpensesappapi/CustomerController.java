package com.example.beginnerexpensesappapi;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class CustomerController {

    @Autowired
    private final CustomerRepository repository;

    private final CustomerModelAssembler assembler;

    CustomerController(CustomerRepository repository, 
                       CustomerModelAssembler assembler,
                       AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.assembler = assembler; 
    }


    // http://localhost:8080/customers
    @GetMapping("/customers")
    CollectionModel<EntityModel<Customer>> all() {
        List<EntityModel<Customer>> customers = repository.findAll().stream() //
                .map(assembler::toModel) //
                .collect(Collectors.toList());
                
        return CollectionModel.of(customers, linkTo(methodOn(CustomerController.class).all()).withSelfRel());
    }

    @PostMapping("/customers")
    Customer newCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    @DeleteMapping("/customers/{userName}")
    @PreAuthorize("hasRole('USER')")
    void delete(@PathVariable String userName) {
        repository.deleteById(userName);
    }
 
    @GetMapping("/customers/{userName}")
    @PreAuthorize("hasRole('USER')")
    EntityModel<Customer> get(@PathVariable String userName) throws CustomerNotFound {
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

}

