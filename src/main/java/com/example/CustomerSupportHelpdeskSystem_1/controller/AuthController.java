package com.example.CustomerSupportHelpdeskSystem_1.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CustomerSupportHelpdeskSystem_1.dto.AuthRequest;
import com.example.CustomerSupportHelpdeskSystem_1.entity.User;
import com.example.CustomerSupportHelpdeskSystem_1.repository.UserRepository;
import com.example.CustomerSupportHelpdeskSystem_1.security.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthController(
            AuthenticationManager authManager,
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder encoder) {

        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    // Step 1 of the flow: a new person registers
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {

        user.setPassword(encoder.encode(user.getPassword()));

        // role is decided by the backend, never trusted from the request body directly
        if (user.getRole() == null
                || (!user.getRole().equalsIgnoreCase("ADMIN")
                    && !user.getRole().equalsIgnoreCase("STAFF"))) {
            user.setRole("USER");
        }

        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        savedUser.setPassword(null); // never send the password back, even encoded

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // Step 1 of the flow: person logs in and receives a token
    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        return jwtService.generateToken(request.getEmail());
    }
}