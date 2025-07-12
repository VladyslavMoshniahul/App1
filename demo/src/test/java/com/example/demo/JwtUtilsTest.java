package com.example.demo;

import com.example.demo.javaSrc.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import org.springframework.test.context.TestPropertySource;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class JwtUtilsTest {
    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void testGenerateAndValidateToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);

        assertThat(token).isNotNull();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUsername(token)).isEqualTo("testuser");
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThat(jwtUtils.validateToken(invalidToken)).isFalse();
    }

    @Test
    void testGetUsernameFromToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);
        String username = jwtUtils.getUsername(token);

        assertThat(username).isEqualTo("testuser");
    }
}
