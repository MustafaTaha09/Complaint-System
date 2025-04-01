package com.example.complaintsystem.DTO.Departments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating or updating a Department")
public class DepartmentRequestDTO {

    @Schema(description = "Name for the department. Cannot be blank.",
            example = "Human Resources", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department name cannot be blank")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String departmentName;
}