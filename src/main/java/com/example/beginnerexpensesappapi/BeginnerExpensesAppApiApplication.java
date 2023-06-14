package com.example.beginnerexpensesappapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = CustomerRepository.class)
public class BeginnerExpensesAppApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeginnerExpensesAppApiApplication.class, args);
	}

	@Bean
  	CommandLineRunner initDatabase(CustomerRepository repository) {
		return args -> {
			repository.save(new Customer("jim", "jim123"));
			repository.save(new Customer("john", "john123"));
    	};
	}

}
