package com.example.beginnerexpensesappapi.config;

import com.example.beginnerexpensesappapi.JwtAuthenticationFilter;
import com.example.beginnerexpensesappapi.service.CustomerService;

import java.util.Arrays;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

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
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.anyRequest().authenticated() // everyone else required authentication
			)
				.authenticationProvider(authenticationProvider())

				// process jWT if receiving a HTTP request with a JWT in it (skipped if not present)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				//// ** UsernamePasswordAuthenticationFilter happens here (default) **  ////

				.httpBasic(Customizer.withDefaults());

		return http.build();
	}

	// CORS
	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList("*")));
        configuration.setAllowedMethods(Collections.unmodifiableList(Arrays.asList(
			"HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"
		)));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.unmodifiableList(Arrays.asList("Authorization", "Cache-Control", "Content-Type")));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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