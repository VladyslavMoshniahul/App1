package com.example.demo;

import com.example.demo.javaSrc.users.*;
import com.example.demo.javaSrc.security.JwtUtils;
import com.example.demo.javaSrc.worker.AuthController;
import com.example.demo.javaSrc.worker.AuthRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService peopleService;

    @Mock
    private HttpServletResponse servletResponse;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSuccessfulLogin() {
        AuthRequest request = new AuthRequest("test@example.com", "password", "STUDENT");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtUtils.generateToken(any())).thenReturn("mockToken");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000L); // 1 hour

        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(User.Role.STUDENT);

        when(peopleService.findByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<?> response = authController.login(request, servletResponse);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map<?, ?>) response.getBody()).get("role")).isEqualTo("STUDENT");
    }

    @Test
    void testBadCredentials() {
        AuthRequest request = new AuthRequest("wrong@example.com", "badpass", "STUDENT");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.login(request, servletResponse);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(((Map<?, ?>) response.getBody()).get("error"))
                .isEqualTo("Невірний email або пароль");
    }

    @Test
    void testRoleMismatch() {
        AuthRequest request = new AuthRequest("test@example.com", "password", "TEACHER");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtUtils.generateToken(any())).thenReturn("mockToken");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(3600000L);

        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(User.Role.STUDENT);

        when(peopleService.findByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<?> response = authController.login(request, servletResponse);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(((Map<?, ?>) response.getBody()).get("error"))
                .isEqualTo("Role mismatch");
    }
}
