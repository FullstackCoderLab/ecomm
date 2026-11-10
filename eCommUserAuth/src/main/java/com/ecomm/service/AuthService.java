package com.ecomm.service;

import com.ecomm.dto.AuthResponse;
import com.ecomm.dto.LoginRequest;
import com.ecomm.dto.RegisterRequest;
import com.ecomm.exception.UserAlreadyExistsException;
import com.ecomm.model.Role;
import com.ecomm.model.User;
import com.ecomm.repository.UserRepository;
import com.ecomm.security.CustomUserDetails;
import com.ecomm.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user registration and login workflows with JWT token issuance.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new user account in the database and issues a JWT token.
     *
     * @param request User registration payload
     * @return AuthResponse containing the JWT token and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Check if email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Email is already registered: " + normalizedEmail);
        }

        // 2. Hash password with BCrypt
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Determine role (default to ROLE_USER)
        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_USER;

        // 4. Save to Database
        User user = new User(request.getName().trim(), normalizedEmail, encodedPassword, role);
        User savedUser = userRepository.save(user);

        // 5. Generate JWT token
        String token = jwtUtils.generateTokenFromUser(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponse(
                token,
                jwtUtils.getExpirationMs(),
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    /**
     * Authenticates an existing user with email and password and returns a JWT token.
     *
     * @param request User login payload
     * @return AuthResponse containing the JWT token and user info
     */
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Authenticate credentials via Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        // 2. Set authentication context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate JWT token
        String token = jwtUtils.generateJwtToken(authentication);

        // 4. Extract authenticated principal details
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        String role = userPrincipal.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");

        return new AuthResponse(
                token,
                jwtUtils.getExpirationMs(),
                userPrincipal.getId(),
                userPrincipal.getName(),
                userPrincipal.getUsername(),
                role
        );
    }
}
