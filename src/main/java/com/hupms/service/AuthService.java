package com.hupms.service;

import com.hupms.dto.request.LoginRequest;
import com.hupms.dto.request.RegisterRequest;
import com.hupms.dto.response.LoginResponse;
import com.hupms.dto.response.UserResponse;
import com.hupms.enums.Role;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.model.User;
import com.hupms.repository.UserRepository;
import com.hupms.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            throw new UnauthorizedAccessException("Admin account already exists");
        }
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);
        user.setId(userRepository.save(user));
        return UserResponse.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtTokenProvider.expiresInSeconds(), UserResponse.from(user));
    }

    public User currentUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Unknown user"));
    }
}
