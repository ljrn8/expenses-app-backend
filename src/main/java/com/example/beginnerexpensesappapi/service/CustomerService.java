package com.example.beginnerexpensesappapi.service;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

    public void registerNewCustomerFromPlainText(@NonNull String username, @NonNull String password) {
        // UserDetails ud = User.builder()
        //         .username(username)
        //         .password(passwordEncoder.encode(password))
        //         .roles("user")
        //         .build();

        // Customer newCustomer = (Customer) ud;

        if (repository.existsById(username)) {
            log.info("not addind customer, " + username + "already exists");
        }

        // NOTE no role?
        Customer newCustomer = Customer.builder()
            .username(username)
            .encryptedPassword(passwordEncoder.encode(password))
            .build();


        HashMap<String, Integer> purchases = new HashMap<>();
        purchases.put("apples", 0);
        purchases.put("bananas", 0);
        purchases.put("oranges", 0);
        newCustomer.setPurchases(purchases);
        repository.save(newCustomer);
        // return newCustomer;
    }

    
    public Customer registerNewCustomer(String username, String encryptedPassword) {
        Customer newCustomer = Customer.builder()
            .username(username)
            .encryptedPassword(encryptedPassword)
            .build();

        HashMap<String, Integer> purchases = new HashMap<>();
        purchases.put("apples", 0);
        purchases.put("bananas", 0);
        purchases.put("oranges", 0);
        newCustomer.setPurchases(purchases);
        repository.save(newCustomer);
        return newCustomer;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) return null;
        
        Customer customer = null;
        try {
            customer =  repository.findById(username).get();
        } catch(NoSuchElementException e) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        log.info("grabbed this customer from the DB: " + customer.toString());
        return customer;
    }




    // other methods are just used directly by controller (<- TODO move here)
}
