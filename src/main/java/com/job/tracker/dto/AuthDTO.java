package com.job.tracker.dto;

public class AuthDTO {

    // ================= SIGN UP =================
    public static class SignUpRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String targetRoles;
        private String targetLocations;

        public SignUpRequest() {}

        public SignUpRequest(String email, String password, String firstName,
                             String lastName, String targetRoles, String targetLocations) {
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.targetRoles = targetRoles;
            this.targetLocations = targetLocations;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getTargetRoles() { return targetRoles; }
        public void setTargetRoles(String targetRoles) { this.targetRoles = targetRoles; }

        public String getTargetLocations() { return targetLocations; }
        public void setTargetLocations(String targetLocations) { this.targetLocations = targetLocations; }
    }

    // ================= LOGIN =================
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ================= AUTH RESPONSE =================
    public static class AuthResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String token;
        private String tokenType;
        private String refreshToken;
        private String username;
        private String role;

        public AuthResponse() {}

        public AuthResponse(Long id, String email, String firstName, String lastName,
                            String token, String tokenType,
                            String refreshToken, String username, String role) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.token = token;
            this.tokenType = tokenType;
            this.refreshToken = refreshToken;
            this.username = username;
            this.role = role;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    // ================= USER PROFILE =================
    public static class UserProfile {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String targetRoles;
        private String targetLocations;

        public UserProfile() {}

        public UserProfile(Long id, String email, String firstName,
                           String lastName, String targetRoles, String targetLocations) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.targetRoles = targetRoles;
            this.targetLocations = targetLocations;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getTargetRoles() { return targetRoles; }
        public void setTargetRoles(String targetRoles) { this.targetRoles = targetRoles; }

        public String getTargetLocations() { return targetLocations; }
        public void setTargetLocations(String targetLocations) { this.targetLocations = targetLocations; }
    }
}