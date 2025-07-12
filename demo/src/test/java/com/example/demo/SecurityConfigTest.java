package com.example.demo;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;
import com.example.demo.javaSrc.security.JwtAuthenticationFilter;
import com.example.demo.javaSrc.security.JwtUtils;
import com.example.demo.javaSrc.security.SecurityConfig;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
public class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private PeopleRepository peopleRepository;

    @BeforeEach
    void setUp() {
        peopleRepository = mock(PeopleRepository.class);
        securityConfig = new SecurityConfig();
    }

    @Test
    void testJwtAuthenticationFilterBean() {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserDetailsService uds = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter(jwtUtils, uds);
        assertThat(filter).isNotNull();
    }

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertThat(encoder).isInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class);
    }

    @Test
    void testAuthenticationManagerBean() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        AuthenticationManager manager = securityConfig.authenticationManager(authConfig);
        assertThat(manager).isEqualTo(authManager);
    }

    @Test
    void testUserDetailsServiceReturnsUser() {
        People person = new People();
        person.setEmail("user@example.com");
        person.setPassword("encodedPassword");
        person.setRole(People.Role.STUDENT);

        when(peopleRepository.findByEmail("user@example.com")).thenReturn(Optional.of(person));

        UserDetailsService uds = securityConfig.userDetailsService(peopleRepository);
        var userDetails = uds.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }

    @Test
    void testUserDetailsServiceThrowsExceptionIfUserNotFound() {
        when(peopleRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        UserDetailsService uds = securityConfig.userDetailsService(peopleRepository);

        assertThatThrownBy(() -> uds.loadUserByUsername("notfound@example.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
