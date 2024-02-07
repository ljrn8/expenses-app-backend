package com.example.beginnerexpensesappapi.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.beginnerexpensesappapi.Customer;
import com.example.beginnerexpensesappapi.CustomerRepository;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
public class CustomerService implements UserDetailsService {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Customer> fetchAll() {
        return repository.findAll();
    }

    public Customer registerNewCustomerFromPlainText(@NonNull String username, @NonNull String password) {
        if (repository.existsById(username)) {
            log.info("ignoring adding customer: " + username + " -> already exists");
        }
        Customer newCustomer = Customer.builder()
            .username(username)
            .encryptedPassword(passwordEncoder.encode(password))
            .build();

        repository.save(newCustomer);
        return newCustomer;
    }

    
    public Customer registerNewCustomer(String username, String encryptedPassword) {
        Customer newCustomer = Customer.builder()
            .username(username)
            .encryptedPassword(encryptedPassword)
            .build();

        repository.save(newCustomer);
        return newCustomer;
    }

    public Customer updatePurchases(String username, HashMap<String, Integer> newPurchases) throws UsernameNotFoundException {
        Optional<Customer> optionalCustomer = repository.findById(username);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setPurchases(newPurchases);
            return repository.save(customer);
        } else {
            throw new UsernameNotFoundException("attempted to update purchases on non existant user: " + username);
        }
    }

    public boolean customerExists(String username) {
        return repository.existsById(username);
    }

    public Customer loadCustomerByUsername(String username) throws UsernameNotFoundException {
        if (username == null) return null;
        Optional<Customer> optionalCustomer =  repository.findById(username);
        if (optionalCustomer.isPresent()) {
            return optionalCustomer.get();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) this.loadCustomerByUsername(username);
    }

}
