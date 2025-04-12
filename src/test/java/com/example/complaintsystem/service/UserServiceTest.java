// UserServiceTest.java (Place in src/test/java/.../Service)
package com.example.complaintsystem.service;

import com.example.complaintsystem.dto.Departments.DepartmentDTO;
import com.example.complaintsystem.dto.Roles.RoleDTO;
import com.example.complaintsystem.entity.Department;
import com.example.complaintsystem.entity.Role;
import com.example.complaintsystem.entity.User;
import com.example.complaintsystem.repository.DepartmentRepository;
import com.example.complaintsystem.repository.RoleRepository;
import com.example.complaintsystem.repository.UserRepository;
import com.example.complaintsystem.dto.Users.CreateUserDTO;
import com.example.complaintsystem.dto.Users.UserDTO;
import com.example.complaintsystem.exception.BadRequestException;
import com.example.complaintsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository; // Now needed again

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TicketService ticketService;

    @Mock
    private DepartmentService departmentService;


    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        // Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setRoleId(1);
        createUserDTO.setDepartmentId(2);

        Role mockRole = new Role(); // Create a mock Role
        mockRole.setRoleId(1);
        mockRole.setRoleName("ROLE_USER");

        Department mockDepartment = new Department();
        mockDepartment.setDepartmentId(2);

        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setDepartmentId(2);
        departmentDTO.setDepartmentName("Test Department");

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setRoleId(1);
        roleDTO.setRoleName("ROLE_USER");



        when(userRepository.findByUsername(createUserDTO.getUsername())).thenReturn(Optional.empty());
//        when(userRepository.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findById(createUserDTO.getRoleId())).thenReturn(Optional.of(mockRole));
        when(departmentRepository.findById(createUserDTO.getDepartmentId())).thenReturn(Optional.of(mockDepartment));
        when(passwordEncoder.encode(createUserDTO.getPassword())).thenReturn("hashedPassword");
//        when(departmentService.convertToDepartmentDTO(mockDepartment)).thenReturn(departmentDTO); // Mock the conversion

        User savedUser = new User();
        savedUser.setUserId(1);
        savedUser.setUsername(createUserDTO.getUsername());
        savedUser.setPassword("hashedPassword");
        savedUser.setEmail(createUserDTO.getEmail());
        savedUser.setRole(mockRole); // Set the mock Role
        savedUser.setDepartment(mockDepartment);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDTO result = userService.createUser(createUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(1, result.getRole().getRoleId()); // Check role ID
        assertEquals(2, result.getDepartment().getDepartmentId()); //check department id
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password");
        verify(roleRepository).findById(1); // Verify findById was called on roleRepository
        verify(departmentRepository).findById(2); // Verify findById was called on departmentRepository
    }
    @Test
    void createUser_UsernameExists_ThrowsException() {
        //Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setRoleId(1);
        createUserDTO.setDepartmentId(1);
        when(userRepository.findByUsername(createUserDTO.getUsername())).thenReturn(Optional.of(new User()));
        //Act and Assert
        assertThrows(BadRequestException.class, ()->{
            userService.createUser(createUserDTO);
        });
    }
    @Test
    void createUser_EmailExists_ThrowsException(){
        //Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setRoleId(1);
        createUserDTO.setDepartmentId(1);
//        when(userRepository.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.of(new User()));

        //Act and Assert
        assertThrows(ResourceNotFoundException.class, ()->{
            userService.createUser(createUserDTO);
        });

    }

    @Test
    void createUser_RoleNotFound_ThrowsException(){
        //Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setRoleId(1);
        createUserDTO.setDepartmentId(1);
        when(userRepository.findByUsername(createUserDTO.getUsername())).thenReturn(Optional.empty()); // User doesn't exist
//        when(userRepository.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.empty()); //Email Doesn't exist
        when(roleRepository.findById(anyInt())).thenReturn(Optional.empty()); //role not found
        assertThrows(ResourceNotFoundException.class, ()->{
            userService.createUser(createUserDTO);
        });

    }
    @Test
    void createUser_DepartmentNotFound_ThrowsException(){ //Changed
        //Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setRoleId(1);
        createUserDTO.setDepartmentId(1);

        Role mockRole = new Role();
        mockRole.setRoleId(1);
        mockRole.setRoleName("ROLE_USER");


        when(userRepository.findByUsername(createUserDTO.getUsername())).thenReturn(Optional.empty()); // User doesn't exist
//        when(userRepository.findByEmail(createUserDTO.getEmail())).thenReturn(Optional.empty()); //Email Doesn't exist
        when(roleRepository.findById(createUserDTO.getRoleId())).thenReturn(Optional.of(mockRole));
        when(departmentRepository.findById(anyInt())).thenReturn(Optional.empty()); //department not found
        assertThrows(ResourceNotFoundException.class, ()->{
            userService.createUser(createUserDTO);
        });

    }

}