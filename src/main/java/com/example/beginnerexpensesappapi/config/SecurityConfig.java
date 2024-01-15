package com.example.beginnerexpensesappapi.config;

import com.example.beginnerexpensesappapi.JwtAuthenticationFilter;
import com.example.beginnerexpensesappapi.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;


	// need to go through this now ->> https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html#servlet-authentication-unpwd-input
	// final thing ^^

@Log
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired private CustomerService customerService;
	@Autowired private PasswordEncoder passwordEncoder;


	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authprovider = new DaoAuthenticationProvider();
		authprovider.setUserDetailsService(customerService);
		authprovider.setPasswordEncoder(passwordEncoder);
		authprovider.setPreAuthenticationChecks(userDetails -> {
        if (userDetails.getPassword() == null || userDetails.getPassword().isEmpty()) {
				log.info("! password null or emtpy");
				throw new BadCredentialsException("Empty password");
			}
		});
		return authprovider;
	}

	// EVERYTHING HAPPENS HERE
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests((authorize) -> authorize
					.requestMatchers(HttpMethod.POST, "/verification", "/register", "/registration").permitAll()
//					.requestMatchers(HttpMethod.GET, "/**").permitAll()
					.anyRequest().authenticated() // everyone else required authentication
			)
				.authenticationProvider(authenticationProvider())

				// process jWT if receiving a HTTP request with a JWT in it (skipped if not present)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				//// ** UsernamePasswordAuthenticationFilter happens here (default) **  ////

				.httpBasic(Customizer.withDefaults());

		return http.build();
	}




	/// IN MEMORY VER
	/*@Bean
	 public UserDetailsService userDetailsService() {
		// TODO find a way to connect bcrypt
	 	UserDetails userDetails = User.withDefaultPasswordEncoder()
	 		.username("user")
	 		.password("password")
	 		.roles("USER")
	 		.build();

	 	return new InMemoryUserDetailsManager(userDetails);
	 }
*/
	 /// DATABASE VER
	/*@Bean
	UserDetailsManager users(DataSource dataSource) {

		//// ADD USER
		UserDetails user = User.builder()
				.username("user")
				.password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
				.roles("USER")
				.build();
		UserDetails admin = User.builder()
				.username("admin")
				.password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
				.roles("USER", "ADMIN")
				.build();
		JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
		users.createUser(user);
		users.createUser(admin);
		return users;


		//// GET USERNAME PASSWORD
	}
*/

	/*


	example jdbc SQL schema for user DB


	create table users(
		username varchar_ignorecase(50) not null primary key,
		password varchar_ignorecase(500) not null,               <<< encrypted PW
		enabled boolean not null
	);

	create table authorities (
		username varchar_ignorecase(50) not null,
		authority varchar_ignorecase(50) not null,
		constraint fk_authorities_users foreign key(username) references users(username)
	);

	create unique index ix_auth_username on authorities (username,authority);

	*/

}	