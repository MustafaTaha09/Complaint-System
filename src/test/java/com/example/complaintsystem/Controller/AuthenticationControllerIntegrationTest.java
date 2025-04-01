//// Example path: src/test/java/com/example/complaintsystem/Controller/AuthenticationControllerIntegrationTest.java
//
//package com.example.complaintsystem.Controller;
//
//import com.example.complaintsystem.Repository.RoleRepository; // Assuming Role entity exists
//import com.example.complaintsystem.Repository.UserRepository;
//import com.example.complaintsystem.Entity.Role; // Import your Role entity
//import com.example.complaintsystem.DTO.Users.CreateUserDTO; // Import your DTO
//import com.fasterxml.jackson.databind.ObjectMapper; // For JSON conversion
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.transaction.annotation.Transactional; // For DB rollback
//
//import static org.hamcrest.Matchers.is; // For JSON path assertions
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // For MockMvc assertions (status, jsonPath)
//
//@SpringBootTest // Load the full application context
//@AutoConfigureMockMvc // Configure MockMvc bean
//@Transactional // Roll back database changes after each test
//public class AuthenticationControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private Role userRole;
//
//    @BeforeEach // Runs before each test method
//    void setUp() {
//        // Ensure the basic "ROLE_USER" exists for registration.
//        // This makes the test less brittle if the DB is empty.
//        userRole = roleRepository.findByRoleName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role role = new Role();
//                    role.setRoleName("ROLE_USER");
//                    // Use the repository to save, as we are in an integration test context
//                    return roleRepository.save(role);
//                });
//        // Optional: Cleanup specific test users if @Transactional causes issues (usually not needed)
//        // userRepository.deleteByUsername("testregisteruser");
//    }
//
//    @Test
//    public void registerUser_WhenValidInput_ShouldCreateUserAndReturn201() throws Exception {
//        // --- Arrange ---
//        CreateUserDTO newUserDto = new CreateUserDTO();
//        newUserDto.setUsername("testregisteruser");
//        newUserDto.setPassword("password123");
//        newUserDto.setEmail("register@example.com");
//        newUserDto.setFirstName("Test");
//        newUserDto.setLastName("Register");
//        newUserDto.setRoleId(userRole.getRoleId()); // Use the ID of the role created/fetched in setUp
//        // Set DepartmentId if it's mandatory in your DTO/logic
//
//        // --- Act & Assert ---
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register") // Target endpoint
//                        .contentType(MediaType.APPLICATION_JSON) // Set content type header
//                        .content(objectMapper.writeValueAsString(newUserDto))) // Set request body as JSON string
//                // Basic Assertions (Quickest to add)
//                .andExpect(status().isCreated()) // Expect HTTP 201 Created status
//
//                // More Detailed Assertions (Optional but recommended)
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // Check response content type
//                .andExpect(jsonPath("$.username", is("testregisteruser"))) // Check username in response body
//                .andExpect(jsonPath("$.userId").exists()); // Check if a userId is present in response
//        // Add checks for other fields returned in your UserDTO if necessary
//    }
//
//    // --- Add more @Test methods for other simple cases ---
//    // e.g., test registration with missing mandatory fields (expect 400 Bad Request)
//    // e.g., test registration with duplicate username (expect 400 Bad Request)
//    // e.g., test a simple GET endpoint using @WithMockUser
//}