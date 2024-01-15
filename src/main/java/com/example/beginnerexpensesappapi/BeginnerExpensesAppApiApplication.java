package com.example.beginnerexpensesappapi;

import com.example.beginnerexpensesappapi.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


// jwt ->> https://dev.to/abhi9720/a-comprehensive-guide-to-jwt-authentication-with-spring-boot-117p
// final ^^

@SpringBootApplication
@EnableMongoRepositories()
public class BeginnerExpensesAppApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeginnerExpensesAppApiApplication.class, args);
	}


	@Autowired
	private CustomerService customerService;

	@Bean
	CommandLineRunner initDatabase(CustomerRepository repository) {
		return args -> {
			customerService.registerNewCustomerFromPlainText("rose", "esor");
		};
	}

}
