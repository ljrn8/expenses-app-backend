package com.example.beginnerexpensesappapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ContextConfiguration(classes=BeginnerExpensesAppApiApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Log
public class ApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken;
    private String invalidToken;

    @BeforeEach
    void beforeAll() throws Exception {
        MvcResult result = this.mockMvc.perform(post("/verification")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "rose",
                        "password", "esor"
                )))).andExpect(status().isOk()).andReturn();

        String body = result.getResponse().getContentAsString();
        this.validToken = extractBearerToken(body);
        this.invalidToken = validToken + "e";
    }

    @Test
    void unAuthGet() throws Exception {
        this.mockMvc.perform(get("/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void failLogin() throws Exception {
        this.mockMvc.perform(post("/verification")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "no",
                        "password", "no2"
                )))).andExpect(status().isUnauthorized());
    }

    @Test
    void getCustomerBadToken() throws Exception {
        this.mockMvc.perform(get("/customers/me")
                        .header("Authorization", "Bearer " + this.invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCustomerNoAuth() throws Exception {
        this.mockMvc.perform(get("/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validGetCustomerObject() throws Exception {
        MvcResult result2 = this.mockMvc.perform(get("/customers/me")
                        // .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + this.validToken))
                .andExpect(status().isOk())
                .andReturn();

        String body2 = result2.getResponse().getContentAsString();
        Customer customer = objectMapper.readValue(body2, Customer.class);
        assertEquals("correct name", "rose", customer.getUsername());
    }

    @Test
    void validUpdatePurchases() throws Exception {
        MvcResult result3 = mockMvc.perform(put("/customers/me/purchases")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Map.of(
                        "apples", 3,
                        "bananas", 2,
                        "oranges", 23
                )))).andExpect(status().isOk()).andReturn();

        Customer customer = objectMapper.readValue(result3.getResponse().getContentAsString(), Customer.class);
        assertEquals("correct purchases", 3, customer.getPurchases().get("apples"));
        assertEquals("correct purchases", 2, customer.getPurchases().get("bananas"));
        assertEquals("correct purchases", 23, customer.getPurchases().get("oranges"));
    }

    private String extractBearerToken(String body) {
        String token = body.substring(7);
        log.info("testing suite using token: " + token);
        return token;
    }
}
