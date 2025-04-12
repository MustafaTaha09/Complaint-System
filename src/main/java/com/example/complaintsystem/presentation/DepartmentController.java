package com.example.complaintsystem.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.complaintsystem.dto.Departments.DepartmentDTO;
import com.example.complaintsystem.dto.Departments.DepartmentRequestDTO;
import com.example.complaintsystem.service.DepartmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "APIs for managing departments (ADMIN Access Required)")
@PreAuthorize("hasRole('ADMIN')") // Secure ALL endpoints for ADMIN only
@SecurityRequirement(name = "Bearer Authentication") // Require JWT for all endpoints
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Get All Departments", description = "Retrieves a list of all available departments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of departments",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DepartmentDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        log.info("Request received to get all departments");
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "Get Department by ID", description = "Retrieves details of a specific department by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved department",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found with the specified ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(
            @Parameter(description = "Unique ID of the department to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("Request received to get department by ID: {}", id);
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Create New Department", description = "Creates a new department.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the department to be created", required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or department name already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(
            @Valid @RequestBody DepartmentRequestDTO departmentRequestDTO) {
        log.info("Request received to create department with name: '{}'", departmentRequestDTO.getDepartmentName());
        DepartmentDTO savedDepartment = departmentService.createDepartment(departmentRequestDTO);
        URI location = URI.create("/api/departments/" + savedDepartment.getDepartmentId());
        return ResponseEntity.created(location).body(savedDepartment);
    }

    @Operation(summary = "Update Existing Department", description = "Updates the name of an existing department.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New details for the department", required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or department name already exists for another department", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found with the specified ID", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @Parameter(description = "ID of the department to update", required = true, example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody DepartmentRequestDTO departmentRequestDTO) {
        log.info("Request received to update department ID: {} to name: '{}'", id, departmentRequestDTO.getDepartmentName());
        DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentRequestDTO);
        return ResponseEntity.ok(updatedDepartment);
    }

    @Operation(summary = "Delete Department", description = "Deletes a specific department by its ID. Fails if the department is assigned to any users or tickets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Department deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Department is currently in use and cannot be deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found with the specified ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "ID of the department to delete", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("Request received to delete department ID: {}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}