package com.example.complaintsystem.Service;

import com.example.complaintsystem.DTO.Roles.RoleDTO;
import com.example.complaintsystem.DTO.Roles.RoleRequestDTO;
import com.example.complaintsystem.Entity.Role;
import com.example.complaintsystem.Exceptions.BadRequestException;
import com.example.complaintsystem.Exceptions.ResourceNotFoundException;
import com.example.complaintsystem.Repository.RoleRepository;
import com.example.complaintsystem.Repository.UserRepository; // Import UserRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository; // Inject UserRepository

    @Autowired
    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository; // Initialize
    }

    // --- Get All Roles ---
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles");
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Found {} roles", roleDTOs.size());
        return roleDTOs;
    }

    // --- Get Role By ID ---
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Integer id) {
        log.info("Fetching role with ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role not found with ID: {}", id);
                    return new ResourceNotFoundException("Role not found with id: " + id);
                });
        log.info("Found role: {}", role.getRoleName());
        return convertToDTO(role);
    }

    // --- Create Role ---
    @Transactional
    public RoleDTO createRole(RoleRequestDTO roleRequestDTO) {
        String roleName = roleRequestDTO.getRoleName().trim(); // Trim whitespace
        log.info("Attempting to create role with name: '{}'", roleName);

//       validation for prefix if needed
         if (!roleName.startsWith("ROLE_")) {
             log.warn("Role creation failed: Name '{}' doesn't start with ROLE_", roleName);
             throw new BadRequestException("Role name must start with ROLE_");
         }

        if (roleRepository.existsByRoleName(roleName)) {
            log.warn("Role creation failed: Name '{}' already exists", roleName);
            throw new BadRequestException("Role with name '" + roleName + "' already exists.");
        }

        Role newRole = new Role();
        newRole.setRoleName(roleName);

        Role savedRole = roleRepository.save(newRole);
        log.info("Successfully created role '{}' with ID: {}", savedRole.getRoleName(), savedRole.getRoleId());
        return convertToDTO(savedRole);
    }

    // --- Update Role ---
    @Transactional
    public RoleDTO updateRole(Integer id, RoleRequestDTO roleRequestDTO) {
        String newRoleName = roleRequestDTO.getRoleName().trim();
        log.info("Attempting to update role ID: {} to name: '{}'", id, newRoleName);

        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role not found for update with ID: {}", id);
                    return new ResourceNotFoundException("Role not found with id: " + id);
                });

        // Check if the new name is already taken by a *different* role
        Optional<Role> roleWithNewName = roleRepository.findByRoleName(newRoleName);
        if (roleWithNewName.isPresent() && !roleWithNewName.get().getRoleId().equals(id)) {
            log.warn("Role update failed: Name '{}' is already used by role ID: {}", newRoleName, roleWithNewName.get().getRoleId());
            throw new BadRequestException("Role name '" + newRoleName + "' is already in use.");
        }

//        validation for prefix if needed
        if (!newRoleName.startsWith("ROLE_")) {
            log.warn("Role creation failed: Name '{}' doesn't start with ROLE_", newRoleName);
            throw new BadRequestException("Role name must start with ROLE_");
        }

        if (roleRepository.existsByRoleName(newRoleName)) {
            log.warn("Role creation failed: Name '{}' already exists", newRoleName);
            throw new BadRequestException("Role with name '" + newRoleName + "' already exists.");
        }

        existingRole.setRoleName(newRoleName);
        Role updatedRole = roleRepository.save(existingRole); // save updates existing entity
        log.info("Successfully updated role ID: {} to name: '{}'", updatedRole.getRoleId(), updatedRole.getRoleName());
        return convertToDTO(updatedRole);
    }

    // --- Delete Role ---
    @Transactional
    public void deleteRole(Integer id) {
        log.info("Attempting to delete role with ID: {}", id);

        // Check if role exists first
        if (!roleRepository.existsById(id)) {
            log.error("Role not found for deletion with ID: {}", id);
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }

        // Check if any users are assigned this role
        long userCount = userRepository.countByRoleRoleId(id); // Use the count query
        if (userCount > 0) {
            log.warn("Deletion failed: Role ID {} is assigned to {} user(s).", id, userCount);
            throw new BadRequestException("Role cannot be deleted because it is currently assigned to " + userCount + " user(s).");
        }

        roleRepository.deleteById(id);
        log.info("Successfully deleted role with ID: {}", id);
    }

    public RoleDTO convertToDTO(Role role) {
        if (role == null) {
            return null;
        }
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        return dto;
    }
}