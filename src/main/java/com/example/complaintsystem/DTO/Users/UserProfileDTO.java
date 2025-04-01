package com.example.complaintsystem.DTO.Users;

// UserDetailsDTO.java

import com.example.complaintsystem.DTO.Comments.CommentDTO;
import com.example.complaintsystem.DTO.Departments.DepartmentDTO;
import com.example.complaintsystem.DTO.Roles.RoleDTO;
import com.example.complaintsystem.DTO.Tickets.GetTicketDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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