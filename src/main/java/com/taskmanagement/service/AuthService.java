package com.taskmanagement.service;

import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.dto.LoginRequest;
import com.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.UserAlreadyExistsException;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // 1. Email zaten var mı kontrol et
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // 2. User oluştur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // BCrypt ile hashle
        user.setFullName(request.getFullName());
        user.setRole(Role.USER);  // Default role: USER

        // 3. Database'e kaydet
        User savedUser = userRepository.save(user);

        // 4. JWT token oluştur
        String token = jwtUtil.generateToken(savedUser);

        // 5. Response oluştur
        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Email + Password ile authenticate et
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // ✅ Başarılı → Devam et
        // ❌ Hatalı → BadCredentialsException fırlatır (GlobalExceptionHandler yakalar)

        // 2. User'ı database'den çek
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. JWT token oluştur
        String token = jwtUtil.generateToken(user);

        // 4. Response oluştur
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}