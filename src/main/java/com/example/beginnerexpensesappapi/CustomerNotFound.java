package com.example.beginnerexpensesappapi;

class CustomerNotFound extends RuntimeException {
    CustomerNotFound(String userName) {
        super("Could not find customer " + userName);
    }
}
