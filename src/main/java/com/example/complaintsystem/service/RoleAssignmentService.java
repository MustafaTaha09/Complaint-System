// RoleAssignmentService.java (Corrected)
package com.example.complaintsystem.service;

import com.example.complaintsystem.entity.Role;
import com.example.complaintsystem.entity.User;
import com.example.complaintsystem.repository.RoleRepository;
import com.example.complaintsystem.repository.UserRepository;
import com.example.complaintsystem.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleAssignmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Inject RoleRepository

    @Transactional
    @PreAuthorize("hasRole('ADMIN')") // Only admins can change roles
    public void changeUserRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByRoleName(roleName) // Find the Role entity
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));

        user.setRole(role);
        userRepository.save(user);
    }
}