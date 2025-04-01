package com.example.complaintsystem.Presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.complaintsystem.DTO.TicketAssignments.CreateTicketAssignmentDTO;
import com.example.complaintsystem.DTO.TicketAssignments.TicketAssignmentDTO;
import com.example.complaintsystem.Service.TicketAssignmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@Tag(name = "Ticket Assignments", description = "APIs for managing user assignments to tickets (ADMIN Access Required)")
@PreAuthorize("hasRole('ADMIN')") // Secure ALL endpoints for ADMIN only
@SecurityRequirement(name = "Bearer Authentication") // Require JWT for all endpoints
public class TicketAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(TicketAssignmentController.class);
    private final TicketAssignmentService assignmentService;

    @Autowired
    public TicketAssignmentController(TicketAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // GET Assignments (Filtered)
    @Operation(summary = "Get Ticket Assignments", description = "Retrieves a list of assignments, optionally filtered by ticket ID or user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TicketAssignmentDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket or User not found if filtering by ID", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TicketAssignmentDTO>> getAssignments(
            @Parameter(description = "Filter assignments by Ticket ID", required = false, example = "55")
            @RequestParam(required = false) Integer ticketId,
            @Parameter(description = "Filter assignments by User ID", required = false, example = "12")
            @RequestParam(required = false) Integer userId) {

        log.info("Request received to get assignments with filters - ticketId: {}, userId: {}", ticketId, userId);
        List<TicketAssignmentDTO> assignments;
        if (ticketId != null) {
            assignments = assignmentService.getAssignmentsByTicketId(ticketId);
        } else if (userId != null) {
            assignments = assignmentService.getAssignmentsByUserId(userId);
        } else {

            log.warn("Attempted to get all assignments without filter - returning empty list for now.");
            assignments = List.of();
        }
        return ResponseEntity.ok(assignments);
    }

    // GET Assignment By ID
    @Operation(summary = "Get Assignment by ID", description = "Retrieves details of a specific assignment record by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignment",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TicketAssignmentDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Assignment not found with the specified ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketAssignmentDTO> getAssignmentById(
            @Parameter(description = "Unique ID of the assignment record to retrieve", required = true, example = "101")
            @PathVariable Integer id) {
        log.info("Request received to get assignment by ID: {}", id);
        TicketAssignmentDTO assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    // Create Assignment
    @Operation(summary = "Create New Assignment", description = "Assigns a user to a ticket.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the assignment (ticket ID and user ID)", required = true,
                    content = @Content(schema = @Schema(implementation = CreateTicketAssignmentDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TicketAssignmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or assignment already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket or User not found for the provided IDs", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TicketAssignmentDTO> createAssignment(
            @Valid @RequestBody CreateTicketAssignmentDTO createDTO) {
        log.info("Request received to create assignment for ticket {} to user {}", createDTO.getTicketId(), createDTO.getUserId());
        TicketAssignmentDTO savedAssignment = assignmentService.createAssignment(createDTO);
        URI location = URI.create("/api/assignments/" + savedAssignment.getId());
        return ResponseEntity.created(location).body(savedAssignment);
    }

    // Delete Assignment by its ID
    @Operation(summary = "Delete Assignment by ID", description = "Deletes a specific assignment record by its unique ID, effectively unassigning the user from the ticket for this specific assignment instance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Assignment not found with the specified ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @Parameter(description = "ID of the assignment record to delete", required = true, example = "101")
            @PathVariable Integer id) {
        log.info("Request received to delete assignment ID: {}", id);
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // Delete Assignment by Ticket and User
    @Operation(summary = "Delete Assignment by Ticket and User", description = "Deletes the assignment linking a specific user to a specific ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Assignment not found for the specified ticket and user", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAssignmentByTicketAndUser(
            @Parameter(description = "ID of the ticket", required = true, example = "55")
            @RequestParam Integer ticketId,
            @Parameter(description = "ID of the user", required = true, example = "12")
            @RequestParam Integer userId) {
        log.info("Request received to delete assignment for ticket {} and user {}", ticketId, userId);
        assignmentService.deleteAssignmentByTicketAndUser(ticketId, userId);
        return ResponseEntity.noContent().build();
    }

}