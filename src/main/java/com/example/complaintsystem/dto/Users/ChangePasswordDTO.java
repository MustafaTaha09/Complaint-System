package com.example.complaintsystem.dto.Users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    private String oldPassword; // Needed if not admin
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}