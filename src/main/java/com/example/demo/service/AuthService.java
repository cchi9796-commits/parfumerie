package com.example.demo.service;

import com.example.demo.dto.Dtos.*;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim();
        if (users.existsByEmail(email))
            throw new ApiException(HttpStatus.CONFLICT, "Email đã được sử dụng");

        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.CUSTOMER);
        users.save(u);
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), toDto(u));
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sai email hoặc mật khẩu"));
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), toDto(u));
    }

    public UserDto me(String email) {
        return toDto(find(email));
    }

    public UserDto update(String email, UpdateProfileRequest r) {
        User u = find(email);
        u.setName(r.name().trim());
        u.setPhone(r.phone() == null ? "" : r.phone().trim());
        u.setAddress(r.address() == null ? "" : r.address().trim());
        if (r.password() != null && !r.password().isBlank()) {
            if (r.password().length() < 6)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu tối thiểu 6 ký tự");
            u.setPasswordHash(encoder.encode(r.password()));
        }
        return toDto(users.save(u));
    }

    private User find(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(),
                u.getPhone(), u.getAddress(), u.getRole().name());
    }
}