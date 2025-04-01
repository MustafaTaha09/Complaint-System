package com.example.complaintsystem.DTO.Users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDTO {
    @NotNull(message = "User ID is required")
    private Integer userId;

    private String username;

    @Email(message = "Invalid email format")
    private String email;

    private String firstName;
    private String lastName;
    private Integer roleId;
    private Integer departmentId;

}