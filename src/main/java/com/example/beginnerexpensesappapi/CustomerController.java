package com.example.beginnerexpensesappapi;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @Autowired
  	private CustomerRepository repository;

    CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    List<Customer> all() {
        return repository.findAll();
    }

    @PostMapping("/customers")
    Customer newCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    @DeleteMapping("/customers/{userName}") 
    void delete(@PathVariable String userName) {
        repository.deleteById(userName); 
    }
    
    @GetMapping("/customers/{userName}")
    Customer get(@PathVariable String userName) {
        return repository.findById(userName).orElseThrow(
            () -> new CustomerNotFound(userName)
        );
    }
}

class CustomerNotFound extends RuntimeException {

    CustomerNotFound(String userName) {
        super("Could not find customer " + userName);
    }

}