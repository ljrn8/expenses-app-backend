package com.example.beginnerexpensesappapi;

import java.util.HashMap;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customer")
public class Customer {

    @Id
    private String userName;
    private String password;
    private HashMap<String, Integer> purchases;

    public Customer() {
        this.userName = null;
        this.password = null;
        this.purchases = new HashMap<>(3);
        this.purchases.put("apples", 0);
        this.purchases.put("bananas", 0);
        this.purchases.put("oranges", 0);
    }

    public Customer(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.purchases = new HashMap<>(3);
        this.purchases.put("apples", 0);
        this.purchases.put("bananas", 0);
        this.purchases.put("oranges", 0);
    }

    public Customer(String userName, String password, HashMap<String,Integer> purchases) {
        this.userName = userName;
        this.password = password;
        this.purchases = purchases;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String,Integer> getPurchases() {
        return this.purchases;
    }

    public void setPurchases(HashMap<String,Integer> purchases) {
        this.purchases = purchases;
    }

    public Customer userName(String userName) {
        setUserName(userName);
        return this;
    }

    public Customer password(String password) {
        setPassword(password);
        return this;
    }

    public Customer purchases(HashMap<String,Integer> purchases) {
        setPurchases(purchases);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Customer)) {
            return false;
        }
        Customer customer = (Customer) o;
        return Objects.equals(userName, customer.userName) && Objects.equals(password, customer.password) && Objects.equals(purchases, customer.purchases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password, purchases);
    }

    @Override
    public String toString() {
        return "{" +
            " userName='" + getUserName() + "'" +
            ", password='" + getPassword() + "'" +
            ", purchases='" + getPurchases() + "'" +
            "}";
    }
    
}
