package com.job.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.job.tracker.dto.AuthDTO;
import com.job.tracker.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })

public class AuthController {

    @Autowired
    private UserService userService;

    

    @PostMapping("/api/signup")
    public ResponseEntity<AuthDTO.AuthResponse> signup(@RequestBody AuthDTO.SignUpRequest request) {
        AuthDTO.AuthResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/login")
    public ResponseEntity<AuthDTO.AuthResponse> login(@RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/profile")
    public ResponseEntity<AuthDTO.UserProfile> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        AuthDTO.UserProfile profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/api/verify")
    public ResponseEntity<Boolean> verifyToken(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userId != null);
    }
}
