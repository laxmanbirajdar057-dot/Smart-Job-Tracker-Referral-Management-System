package com.job.tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.job.tracker.dto.AuthDTO;
import com.job.tracker.entity.User;
import com.job.tracker.repository.UserRepository;
import com.job.tracker.security.JwtTokenProvider;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // ================= SIGNUP =================
    public AuthDTO.AuthResponse signup(AuthDTO.SignUpRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTargetRoles(request.getTargetRoles());
        user.setTargetLocations(request.getTargetLocations());

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return new AuthDTO.AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                token,
                "Bearer",
                null,                    // refreshToken (if not implemented yet)
                user.getEmail(),        // username (or change if you have username field)
                "USER"                  // role (default)
        );
    }

    // ================= LOGIN =================
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // FIXED PASSWORD CHECK
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return new AuthDTO.AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                token,
                "Bearer",
                null,
                user.getEmail(),
                "USER"
        );
    }

    // ================= PROFILE =================
    public AuthDTO.UserProfile getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthDTO.UserProfile(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getTargetRoles(),
                user.getTargetLocations()
        );
    }
}