package com.example.demo.javaSrc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.javaSrc.peoples.*;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils,
            UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwtUtils, uds);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/teacher.html").hasRole("TEACHER")
              .requestMatchers("/parent.html").hasRole("PARENT")
              .requestMatchers("/student.html").hasRole("STUDENT")
              .requestMatchers("/director.html").hasRole("DIRECTOR")

              .requestMatchers("/login.html", "/api/login",
                               "/styles/**", "/scripts/**", "/images/**").permitAll()

              .requestMatchers("/ws-stomp/**").permitAll()
          .requestMatchers("/admin.html").hasRole("ADMIN")

              .requestMatchers("/api/**").authenticated()

              .anyRequest().authenticated()
          )
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

          .formLogin(form -> form
              .loginPage("/login.html")
              .loginProcessingUrl("/api/login")
              .successHandler((req, resp, auth) -> {
                  resp.setStatus(HttpServletResponse.SC_OK);
                  resp.setContentType("application/json;charset=UTF-8");
                  String role = auth.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .findFirst().orElse("");
                  if (role.startsWith("ROLE_")) role = role.substring(5);
                  resp.getWriter().write("{\"role\":\"" + role + "\"}");
              })
              .failureHandler((req, resp, exc) ->
                  resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed")
              )
              .permitAll()
          )

          .logout(logout -> logout
              .logoutUrl("/api/logout")
              .logoutSuccessUrl("/login.html")
              .invalidateHttpSession(true)
              .deleteCookies("JSESSIONID")
          )

          .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(PeopleRepository repo) {
        return username -> repo.findByEmail(username)
            .map(person -> User.withUsername(person.getEmail())
                    .password(person.getPassword())
                    .authorities("ROLE_" + person.getRole())
                    .build())
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}