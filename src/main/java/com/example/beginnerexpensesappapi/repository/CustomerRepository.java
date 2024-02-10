package com.example.beginnerexpensesappapi.repository;

import com.example.beginnerexpensesappapi.domain.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {}
