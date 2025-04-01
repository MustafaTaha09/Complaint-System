package com.example.complaintsystem.Presentation;

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
@Tag(name = "Authentication", description = "User Login and Registration")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserLoginDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
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