package com.job.tracker.service;

import com.job.tracker.dto.AuthDTO;
import com.job.tracker.dto.JobDTO;
import com.job.tracker.dto.JobDTO.CreateJobRequest;
import com.job.tracker.dto.JobDTO.JobListResponse;
import com.job.tracker.dto.JobDTO.JobResponse;
import com.job.tracker.dto.JobDTO.UpdateJobRequest;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.User;
import com.job.tracker.repository.UserRepository;
import com.job.tracker.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JobService {

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
                null,
                user.getEmail(),
                "USER"
        );
    }

    // ================= LOGIN =================
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

    public JobResponse createJob(Long userId, CreateJobRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createJob'");
    }

    public JobListResponse getAllJobs(Long userId, String status) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllJobs'");
    }

     public JobListResponse getJobs(Long userId, String status) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllJobs'");
    }

     public JobResponse updateJob(Long id, Long userId, UpdateJobRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateJob'");
     }

     public void deleteJob(Long id, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteJob'");
     }

     public long getJobCount(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJobCount'");
     }

     public JobResponse getJob(Long id, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJob'");
     }

    
}