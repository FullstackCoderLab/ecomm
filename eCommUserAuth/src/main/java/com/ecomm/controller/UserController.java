package com.ecomm.controller;

import com.ecomm.dto.ApiResponse;
import com.ecomm.dto.UserProfileResponse;
import com.ecomm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Protected REST Controller requiring valid JWT Bearer authentication.
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "Protected endpoints requiring valid Bearer JWT Authentication")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 1. GET CURRENT USER PROFILE
     * Endpoint: GET /api/v1/users/me
     * Demonstrates accessing authenticated user data from the JWT token and database.
     */
    @Operation(
            summary = "Get current authenticated user profile",
            description = "Returns the details of the user whose JWT token is passed in the Authorization header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved successfully", profile));
    }

    /**
     * 2. GET ALL USERS (ADMIN ONLY)
     * Endpoint: GET /api/v1/users
     * Demonstrates Role-Based Access Control (RBAC) with Spring Security.
     */
    @Operation(
            summary = "Get all registered users (ROLE_ADMIN only)",
            description = "Restricted endpoint accessible only by users having the ROLE_ADMIN authority."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All users retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserProfileResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ROLE_ADMIN privilege"
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        List<UserProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", users));
    }
}
