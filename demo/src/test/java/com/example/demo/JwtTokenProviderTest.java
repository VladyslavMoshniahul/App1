package com.example.demo;

import com.example.demo.javaSrc.security.JwtTokenProvider;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProvider.setSecret("my-very-secret-key-that-should-be-long-enough");
        jwtTokenProvider.setValidityMs(3600000);
        jwtTokenProvider.init();
    }

    @Test
    void testCreateAndValidateToken() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("testuser");

        String token = jwtTokenProvider.createToken(auth);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("testuser");
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    void testGetUsernameFromToken() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("testuser");

        String token = jwtTokenProvider.createToken(auth);
        String username = jwtTokenProvider.getUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void testCreateJwtCookie() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("testuser");

        String token = jwtTokenProvider.createToken(auth);
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600); 

        assertThat(cookie.getName()).isEqualTo("JWT");
        assertThat(cookie.getValue()).isEqualTo(token);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(3600);
    }
}
