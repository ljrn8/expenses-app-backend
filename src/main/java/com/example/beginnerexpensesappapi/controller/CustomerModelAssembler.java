package com.example.beginnerexpensesappapi.controller;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.example.beginnerexpensesappapi.Customer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.example.beginnerexpensesappapi.controller.CustomerController;

/**
 * from documentation for proper restfulness -
 * https://spring.io/guides/tutorials/rest/
 * adds uri's to json
 */
@Component
class CustomerModelAssembler implements RepresentationModelAssembler<Customer, EntityModel<Customer>> {

    @Override
    public EntityModel<Customer> toModel(Customer customer) {
        // TODO
        return null;
    }
        // return EntityModel.of(customer, // 
        //         linkTo(methodOn(CustomerController.class).get(customer.getUsername())).withSelfRel(),
        //         linkTo(methodOn(CustomerController.class).all()).withRel("customers"));
        // }
}
