package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return service.register(r);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }

    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        return service.me(auth.getName());
    }

    @PutMapping("/me")
    public UserDto update(Authentication auth, @Valid @RequestBody UpdateProfileRequest r) {
        return service.update(auth.getName(), r);
    }
}