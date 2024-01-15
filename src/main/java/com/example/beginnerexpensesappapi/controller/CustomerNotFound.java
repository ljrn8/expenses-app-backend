package com.example.beginnerexpensesappapi.controller;

public class CustomerNotFound extends RuntimeException {
    CustomerNotFound(String userName) {
        super("Could not find customer " + userName);
    }
}
