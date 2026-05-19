package com.health.backend.service;

import com.health.backend.dto.LoginRequest;
import com.health.backend.dto.LoginResponse;
import com.health.backend.dto.RegisterRequest;
import com.health.backend.model.User;
import com.health.backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SecretKey signingKey;

    public AuthService(
            UserRepository userRepository,
            @Value("${JWT_SECRET:HealthAssistSystem2024SuperSecretKeyForJWT!!}") String jwtSecret) {
        this.userRepository = userRepository;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !verifyPassword(request.getPassword(), user.getPasswordHash())) {
            return LoginResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }

        String token = generateToken(user);
        String role = getRoleName(user.getRoleId());

        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .token(token)
                .userId(user.getUserId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(role)
                .build();
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return LoginResponse.builder()
                    .success(false)
                    .message("Email already registered")
                    .build();
        }

        int roleId = request.getRoleId() > 0 ? request.getRoleId() : 1;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashPassword(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoleId(roleId);
        user.setGenderCode(request.getGenderCode());

        userRepository.save(user);
        int userId = userRepository.getLastInsertId();

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            userRepository.saveContact(userId, "PHONE", request.getPhone());
        }

        switch (roleId) {
            case 1 -> { // PATIENT
                userRepository.savePatient(userId, request.getDateOfBirth(),
                        request.getBloodCode(), request.getAddress());
            }
            case 2 -> { // DOCTOR
                userRepository.saveDoctor(userId, request.getLicenseNumber(),
                        request.getYearsOfExperience(), request.getClinicAddress());
                int doctorId = userRepository.getLastDoctorId();
                if (request.getSpecializations() != null && !request.getSpecializations().isEmpty()) {
                    for (String spec : request.getSpecializations().split(",")) {
                        Integer specId = userRepository.getSpecializationIdByName(spec.trim());
                        if (specId != null) {
                            userRepository.saveDoctorSpecialization(doctorId, specId);
                        }
                    }
                }
            }
            case 3 -> { // ADMIN
                userRepository.saveAdmin(userId, request.getDepartment(),
                        request.getAccessLevel() > 0 ? request.getAccessLevel() : 1);
            }
        }

        String token = generateToken(user);

        return LoginResponse.builder()
                .success(true)
                .message("Registration successful")
                .token(token)
                .userId(userId)
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(getRoleName(roleId))
                .build();
    }

    private boolean verifyPassword(String rawPassword, String storedHash) {
        return hashPassword(rawPassword).equals(storedHash);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("role", getRoleName(user.getRoleId()))
                .issuedAt(new Date())
                .signWith(signingKey)
                .compact();
    }

    private String getRoleName(int roleId) {
        return switch (roleId) {
            case 1 -> "PATIENT";
            case 2 -> "DOCTOR";
            case 3 -> "ADMIN";
            default -> "UNKNOWN";
        };
    }
}