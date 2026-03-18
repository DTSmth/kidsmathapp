package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.auth.AuthResponse;
import org.example.kidsmathapp.dto.auth.LoginRequest;
import org.example.kidsmathapp.dto.auth.RegisterRequest;
import org.example.kidsmathapp.dto.auth.UserDto;
import org.example.kidsmathapp.entity.User;
import org.example.kidsmathapp.entity.enums.Role;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.UserRepository;
import org.example.kidsmathapp.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.PARENT)
            .enabled(true)
            .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
            .token(token)
            .user(mapToUserDto(user))
            .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        if (!user.getEnabled()) {
            throw new ApiException("Account is disabled", HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
            .token(token)
            .user(mapToUserDto(user))
            .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> ApiException.notFound("User not found"));

        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}
