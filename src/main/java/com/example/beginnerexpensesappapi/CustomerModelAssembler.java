package com.example.beginnerexpensesappapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/**
 * from documentation for proper restfulness -
 * https://spring.io/guides/tutorials/rest/
 * adds uri's to json
 */
@Component
class CustomerModelAssembler implements RepresentationModelAssembler<Customer, EntityModel<Customer>> {

    @Override
    public EntityModel<Customer> toModel(Customer customer) {
        return EntityModel.of(customer, // 
                linkTo(methodOn(CustomerController.class).get(customer.getUserName())).withSelfRel(),
                linkTo(methodOn(CustomerController.class).all()).withRel("customers"));
        }
}
