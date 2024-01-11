package com.example.beginnerexpensesappapi;

import java.util.HashMap;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customer")
public class Customer {

    @Id
    private String userName;
    private String password;
    private HashMap<String, Integer> purchases;

    Customer(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.purchases = new HashMap<>();
        purchases.put("bananas", 0);
        purchases.put("apples", 0);
        purchases.put("oranges", 0);
    }

}
