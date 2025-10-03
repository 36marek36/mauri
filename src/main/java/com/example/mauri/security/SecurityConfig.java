package com.example.mauri.security;

import com.example.mauri.enums.Role;
import com.example.mauri.model.User;
import com.example.mauri.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.UUID;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain restSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Unauthorized: " + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Forbidden: " + accessDeniedException.getMessage() + "\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/rest/auth/**").permitAll()
                        .requestMatchers("/rest/leagues/**").permitAll()
                        .requestMatchers("/rest/matches/**").permitAll()
                        .requestMatchers("/rest/seasons/**").permitAll()
                        .requestMatchers("/rest/players/without-user").hasRole("ADMIN")
                        .requestMatchers("/rest/players/").permitAll()
                        .requestMatchers("/rest/teams/").permitAll()
                        .requestMatchers("/rest/users/me/**").authenticated()
                        .requestMatchers("/rest/players/**").authenticated()
                        .requestMatchers("/rest/teams/**").authenticated()
                        .requestMatchers("/rest/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            String username = "admin";
            String password = "admin";
            if (!userRepository.existsByUsername(username)) {
                User user = User.builder()
                        .id(UUID.randomUUID().toString())
                        .username(username)
                        .password(passwordEncoder().encode(password))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(user);
                log.info("Admin account created with username '{}' and default password.", username);
            } else {
                log.info("Admin account already exists.");
            }
        };
    }
}
