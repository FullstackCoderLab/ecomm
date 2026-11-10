package com.ecomm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned upon successful registration or login containing JWT.
 */
@Schema(description = "Authentication response containing JWT bearer token and user info")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token validity in milliseconds", example = "86400000")
    private long expiresIn;

    @Schema(description = "User unique ID", example = "1")
    private Long id;

    @Schema(description = "User full name", example = "John Doe")
    private String name;

    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Assigned user role", example = "ROLE_USER")
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, long expiresIn, Long id, String name, String email, String role) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
