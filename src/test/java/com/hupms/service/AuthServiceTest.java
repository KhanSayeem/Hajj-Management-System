package com.hupms.service;

import com.hupms.dto.request.RegisterRequest;
import com.hupms.enums.Role;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.repository.UserRepository;
import com.hupms.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final AuthService service = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);

    @Test
    void registerRejectsAdditionalAdmin() {
        RegisterRequest request = new RegisterRequest("Admin Two", "admin2@example.com", "securepass123", Role.ADMIN);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class, () -> service.register(request));
    }
}
