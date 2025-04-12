package com.example.complaintsystem.dto.Departments;

import io.swagger.v3.oas.annotations.media.Schema; // Import
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for Department details")
public class DepartmentDTO{
    @Schema(description = "Unique identifier of the department", example = "1")
    private Integer departmentId;

    @Schema(description = "Name of the department", example = "IT or HR")
    private String departmentName;
}