package com.example.complaintsystem.dto.Users;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}