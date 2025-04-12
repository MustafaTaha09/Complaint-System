package com.example.complaintsystem.dto.Users;

// UserDetailsDTO.java

import com.example.complaintsystem.dto.Departments.DepartmentDTO;
import com.example.complaintsystem.dto.Roles.RoleDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDTO {
    private Integer userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private RoleDTO role;
    private DepartmentDTO department;
}