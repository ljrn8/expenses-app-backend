package com.example.beginnerexpensesappapi.config;

import com.example.beginnerexpensesappapi.JwtAuthenticationFilter;
import com.example.beginnerexpensesappapi.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomerService customerService;
	private final PasswordEncoder passwordEncoder;


	// PASSWORD CHECKING HAPPENS INSIDE HERE
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authprovider = new DaoAuthenticationProvider();
		authprovider.setUserDetailsService(customerService);
		authprovider.setPasswordEncoder(passwordEncoder);
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
				// receiving UN and PW and snding back JWT (w/ public key in it)
				.authenticationProvider(authenticationProvider())

				// receiving a HTTP request with (supposidly) a JWT in it
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				.httpBasic(Customizer.withDefaults())
				.formLogin(Customizer.withDefaults());

		return http.build();
	}







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