package com.example.demo.javaSrc.worker;

import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleService;
import com.example.demo.javaSrc.security.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private final AuthenticationManager authManager;
    @Autowired
    private final JwtUtils jwtUtils;
    @Autowired
    private final PeopleService peopleService;

    
    public AuthController(AuthenticationManager authManager,
                          JwtUtils jwtUtils,
                          PeopleService peopleService) {
        this.authManager   = authManager;
        this.jwtUtils      = jwtUtils;
        this.peopleService = peopleService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest,
                                HttpServletResponse response) {
        try {
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtils.generateToken(authentication);

            Cookie jwtCookie = jwtUtils.createJwtCookie(token);

            response.addCookie(jwtCookie);


            People u = peopleService.findByEmail(authRequest.getEmail());
            if (u == null) {
                throw new UsernameNotFoundException("User not found");
            }
            if (!u.getRole().name().equalsIgnoreCase(authRequest.getRole())) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Role mismatch"));
            }
            return ResponseEntity.ok(Map.of(
                "role", u.getRole().name()
            ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Невірний email або пароль"));
        }
    }

    
}