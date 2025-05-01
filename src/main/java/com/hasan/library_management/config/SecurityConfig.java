package com.hasan.library_management.config;

import com.hasan.library_management.security.JwtAuthenticationFilter;
import com.hasan.library_management.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Open endpoints
                        .requestMatchers("/auth/**").permitAll()

                        // Book access
                        .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("LIBRARIAN", "PATRON")
                        .requestMatchers(HttpMethod.POST, "/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("LIBRARIAN")

                        // User access
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("LIBRARIAN")

                        // Borrowing
                        .requestMatchers(HttpMethod.POST, "/borrow-records/**").hasRole("PATRON")
                        .requestMatchers(HttpMethod.PUT, "/borrow-records/return/**").hasRole("PATRON")
                        .requestMatchers(HttpMethod.GET, "/borrow-records/user/**").hasRole("PATRON")
                        .requestMatchers(HttpMethod.GET, "/borrow-records/overdue").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/borrow-records").hasRole("LIBRARIAN")

                        // All others
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}