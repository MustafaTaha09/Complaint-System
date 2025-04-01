package com.example.complaintsystem.Service;

import com.example.complaintsystem.DTO.Comments.CommentDTO;
import com.example.complaintsystem.DTO.Departments.DepartmentDTO;
import com.example.complaintsystem.DTO.Roles.RoleDTO;
import com.example.complaintsystem.DTO.Tickets.GetTicketDTO;
import com.example.complaintsystem.DTO.Users.*;
import com.example.complaintsystem.Entity.Comment;
import com.example.complaintsystem.Entity.Department;
import com.example.complaintsystem.Entity.Role;
import com.example.complaintsystem.Entity.User;
import com.example.complaintsystem.Exceptions.BadRequestException;
import com.example.complaintsystem.Repository.DepartmentRepository;
import com.example.complaintsystem.Repository.RoleRepository;
import com.example.complaintsystem.Repository.UserRepository;
import com.example.complaintsystem.Exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private PasswordEncoder passwordEncoder; // for bcrypt pw encoding
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDTO getUserByUserName(String userName) {
        return userRepository.findByUsername(userName)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Username: " + userName));
    }
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        log.info("Attempting to create user with username: '{}'", createUserDTO.getUsername()); // Log entry
        if(userRepository.findByUsername(createUserDTO.getUsername()).isPresent()){
            log.warn("Username '{}' already exists. Throwing BadRequestException.", createUserDTO.getUsername());
            throw new BadRequestException("Username with " + createUserDTO.getUsername() + " already exists!");
        }

        User user = new User();
        user.setUsername(createUserDTO.getUsername());
        //Hashing the PW before saving (bcrypt)
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

        user.setEmail(createUserDTO.getEmail());
        user.setFirstName(createUserDTO.getFirstName());
        user.setLastName(createUserDTO.getLastName());


        Role role = roleRepository.findById(createUserDTO.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + createUserDTO.getRoleId()));
        user.setRole(role);
        log.debug("Role set successfully for username: {}", user.getUsername());

        if (createUserDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(createUserDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + createUserDTO.getDepartmentId()));
            user.setDepartment(department);
            log.debug("Department set successfully for username: {}", user.getUsername());

        }

        User savedUser = userRepository.save(user);
        log.info("Successfully created user '{}' with ID: {}", savedUser.getUsername(), savedUser.getUserId()); // Log success
        return convertToDTO(savedUser);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId") // Security check
    public UserDTO updateUser(Integer id, UpdateUserDTO updateUserDTO) {
        log.info("Attempting to Update user with username: '{}'", updateUserDTO.getUsername());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if(user == null) // handling logging
            log.warn("Username '{}' not found. Throwing ResourceNotFoundException.", updateUserDTO.getUsername());


        // Allow admins to update anything. Regular users can only update certain fields.
        if (updateUserDTO.getUsername() != null) {
            if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(
                    r -> r.getAuthority().equals("ROLE_ADMIN"))
            )
            {
                log.warn("Only admins can change username.");
                throw new AccessDeniedException("Only Admin can change username");
            }
            user.setUsername(updateUserDTO.getUsername());
        }
        if (updateUserDTO.getEmail() != null) {
            user.setEmail(updateUserDTO.getEmail());
        }
        if (updateUserDTO.getFirstName() != null) {
            user.setFirstName(updateUserDTO.getFirstName());
        }
        if (updateUserDTO.getLastName() != null) {
            user.setLastName(updateUserDTO.getLastName());
        }
        if (updateUserDTO.getRoleId() != null) {
            if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(
                    r -> r.getAuthority().equals("ROLE_ADMIN"))
            )
            {
                log.warn("Only admins can change Role.");
                throw new AccessDeniedException("Only Admin can change role");
            }
            Role role = roleRepository.findById(updateUserDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + updateUserDTO.getRoleId()));
            user.setRole(role);
        }

        if (updateUserDTO.getDepartmentId() != null) {
            if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(
                    r -> r.getAuthority().equals("ROLE_ADMIN"))
            )
            {
                throw new AccessDeniedException("Only Admin can change department");
            }
            Department department = departmentRepository.findById(updateUserDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + updateUserDTO.getDepartmentId()));
            user.setDepartment(department);
        }
        User updatedUser = userRepository.save(user);
        log.info("User was updated successfully and saved");
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Integer id) {
           User user = userRepository.findById(id)
                   .orElseThrow(() ->{
                       log.error("User not found with id: '{}'", id);
                       // Now create and return the exception to be thrown
                       return new ResourceNotFoundException("User not found with id: " + id);});

        userRepository.deleteById(id);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        if (user.getRole() != null) {
            dto.setRole(convertToRoleDTO(user.getRole()));
        }
        if (user.getDepartment() != null) {
            dto.setDepartment(convertToDepartmentDTO(user.getDepartment()));
        }
        if (user.getTickets() != null) {
            dto.setTickets(user.getTickets().stream().map(ticketService::convertTicketToDTO).collect(Collectors.toList()));
        }
        if (user.getComments() != null) {
            dto.setComments(user.getComments().stream().map(ticketService::convertToCommentDTO).collect(Collectors.toList()));
        }

        return dto;
    }


    public RoleDTO convertToRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        return dto;
    }

    public DepartmentDTO convertToDepartmentDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        return dto;
    }
//    public CommentDTO convertToCommentDTO(Comment comment){
//        CommentDTO commentDTO = new CommentDTO();
//        commentDTO.setId(comment.getCommentId());
//        commentDTO.setText(comment.getComment());
//        commentDTO.setTicketId(comment.getTicket().getTicketId());
//        commentDTO.setId(comment.getUser().getUserId());
//        return commentDTO;
//    }

    // UserDetailsDTO Conversion
    public UserDetailsDTO getUserDetails(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return convertToUserDetailsDTO(user);
    }

    public UserProfileDTO getUserProfile(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserProfileDTO(user);
    }

    private UserDetailsDTO convertToUserDetailsDTO(User user) {
        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());

        if (user.getDepartment() != null) {
            dto.setDepartment(convertToDepartmentDTO(user.getDepartment()));
        }

        // Convert related Tickets to TicketDTOs
        if (user.getTickets() != null) {
            List<GetTicketDTO> ticketDTOs = user.getTickets().stream()
                    .map(ticketService::convertTicketToDTO) // Reuse existing method
                    .collect(Collectors.toList());
            dto.setTickets(ticketDTOs);
        }

        // Convert related Comments to CommentDTOs
        if (user.getComments() != null) {
            List<CommentDTO> commentDTOs = user.getComments().stream()
                    .map(ticketService::convertToCommentDTO)
                    .collect(Collectors.toList());
            dto.setComments(commentDTOs);
        }
        return dto;
    }

    //UserProfileDTO
    public UserProfileDTO convertToUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        if (user.getRole() != null) {
            dto.setRole(convertToRoleDTO(user.getRole()));
        }
        if (user.getDepartment() != null) {
            dto.setDepartment(convertToDepartmentDTO(user.getDepartment()));
        }
        return dto;
    }

    @Transactional
//    @PreAuthorize("hasRole('ADMIN')") // Only admins can change usernames
    public void changeUsername(Integer id, String newUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setUsername(newUsername);
        userRepository.save(user);
    }

    // (changePassword)
    @Transactional
//    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId") // Allow Admins OR Users to change their own password
    public void changePassword(Integer userId, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the old password is correct (if not admin)
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(
                r -> r.getAuthority().equals("ROLE_ADMIN")
        )) {
            if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("Incorrect old password");
            }
        }


        // Set the new (hashed) password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }


    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}