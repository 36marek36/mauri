package com.example.mauri.security;
//
//import com.example.mauri.enums.Role;
//import com.example.mauri.model.User;
//import com.example.mauri.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//import java.util.UUID;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//        private final UserDetailsService userDetailsService;
//        private final UserRepository userRepository;
//
//        @Autowired
//    public SecurityConfig(UserDetailsService userDetailsService, UserRepository userRepository) {
//        this.userDetailsService = userDetailsService;
//        this.userRepository = userRepository;
//    }
//
//    @Bean
//    public SecurityFilterChain restSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/rest/**")
//                .authorizeHttpRequests(authorizeRequests ->
//                        authorizeRequests
//                                .requestMatchers("/rest/players/**").permitAll()
//                                .requestMatchers("/rest/users/**").hasRole("ADMIN")
/// /                                .anyRequest().authenticated()
//                                .anyRequest().permitAll()
//                )
//                .userDetailsService(userDetailsService)
//                .httpBasic(withDefaults())
//                .csrf(AbstractHttpConfigurer::disable);
//        return http.build();
//    }
//
//    @Bean
//    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorizeRequests ->
//                                authorizeRequests
//                                        // Umožniť prístup na tieto adresy aj neprihláseným používateľom
//                                        .requestMatchers("/users/**", "/login", "/signup","/players/**","/leagues/**","/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
//
//                                        // Adresy ktore vyžadujú prihlásenie
//                                        .requestMatchers( "/profile/**").authenticated()
//                                        .requestMatchers( "/matches/**").authenticated()
//
//                                        // Admin sekcia
////                                        .requestMatchers("/users/**").hasRole("ADMIN")
//
//                                        // Pre všetky ostatné požiadavky musí byť používateľ prihlásený
//                                        .anyRequest().permitAll()
////                                .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/players/", true)
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/home")
//                        .permitAll()
//                )
//                .userDetailsService(userDetailsService);
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//            return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public CommandLineRunner initDatabase() {
//          return args -> {
//              String username = "admin";
//              String password = "admin";
//              if (!userRepository.existsByUsername(username)) {
//                  User user = new User(UUID.randomUUID().toString(),username,passwordEncoder().encode(password),Role.ADMIN);
//                  userRepository.save(user);
//              }
//          };
//    }
//}

import com.example.mauri.enums.Role;
import com.example.mauri.model.User;
import com.example.mauri.repository.UserRepository;
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
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/rest/auth/**").permitAll()
                        .requestMatchers("/rest/users/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
//                        .anyRequest().authenticated()
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
                User user = new User(UUID.randomUUID().toString(), username, passwordEncoder().encode(password), Role.ADMIN);
                userRepository.save(user);
            }
        };
    }
}
