package com.example.complaintsystem.Presentation;

import com.example.complaintsystem.DTO.Security.RefreshTokenRequestDTO;
import com.example.complaintsystem.Entity.RefreshToken;
import com.example.complaintsystem.Exceptions.TokenRefreshException;
import com.example.complaintsystem.Security.CustomUserDetails;
import com.example.complaintsystem.Service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.complaintsystem.DTO.Security.JwtAuthenticationResponse;
import com.example.complaintsystem.DTO.Users.CreateUserDTO;
import com.example.complaintsystem.DTO.Users.UserDTO;
import com.example.complaintsystem.DTO.Users.UserLoginDTO;
import com.example.complaintsystem.Service.UserService;
import com.example.complaintsystem.Exceptions.BadRequestException;
import com.example.complaintsystem.Security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User Login, Registration, and Token Management")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Operation(summary = "User Login", description = "Authenticates a user based on username and password, returns a JWT token upon success.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( // Describe request body
                    description = "User credentials for login", required = true,
                    content = @Content(schema = @Schema(implementation = UserLoginDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, JWT token returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid username or password",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody UserLoginDTO loginDto) {
        log.info("Login attempt for user: {}", loginDto.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUserId());

        log.info("Login successful for user: {}", loginDto.getUsername());
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, refreshToken.getToken()));
    }

    @Operation(summary = "Refresh Access Token", description = "Generates a new short-lived JWT access token using a valid, non-expired refresh token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The currently valid refresh token", required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token refreshed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Refresh token is invalid, expired, or not found", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String requestRefreshToken = request.getRefreshToken();
        log.info("Refresh token request received");

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    CustomUserDetails userDetails = new CustomUserDetails(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    String newAccessToken = tokenProvider.generateToken(authentication);
                    log.info("Generated new access token via refresh for user: {}", user.getUsername());
                    // Return new access token and the same refresh token
                    return ResponseEntity.ok(new JwtAuthenticationResponse(newAccessToken, requestRefreshToken));
                })
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in database during refresh request");

                    return new TokenRefreshException(requestRefreshToken, "Refresh token not found!");
                });
    }


    @Operation(summary = "Register New User", description = "Creates a new user account. This endpoint is public.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( // Describe request body
                    description = "Details for the new user account", required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data (validation errors) or username/email already exists",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        if (userService.existsByUsername(createUserDTO.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userService.existsByEmail(createUserDTO.getEmail())) {
            throw new BadRequestException("Email is already taken!");
        }
        UserDTO savedUser = userService.createUser(createUserDTO);
        return ResponseEntity.created(URI.create("/api/users/" + savedUser.getUserId())).body(savedUser);
    }
}
