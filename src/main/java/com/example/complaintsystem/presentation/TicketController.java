package com.example.complaintsystem.presentation;

import com.example.complaintsystem.dto.Tickets.CreateTicketDTO;
import com.example.complaintsystem.dto.Tickets.PatchTicketDTO;
import com.example.complaintsystem.dto.Tickets.GetTicketDTO;
import com.example.complaintsystem.dto.Tickets.UpdateTicketDTO;
import com.example.complaintsystem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets (v1)", description = "APIs for managing support tickets - Version 1")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "Get Ticket by ID (v1)", description = "Retrieves the details of a specific ticket by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetTicketDTO.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found with the specified ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content)
    })

    @GetMapping("/{id}")
    public ResponseEntity<GetTicketDTO> getTicket(@Parameter(description = "Unique ID of the ticket to retrieve", required = true, example = "1") @PathVariable Integer id) {
        GetTicketDTO getTicketDTO = ticketService.getTicketById(id);
        return ResponseEntity.ok(getTicketDTO);
    }

    @Operation(summary = "Create New Ticket (v1)", description = "Creates a new support ticket. Requires authentication.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update for the ticket (only include fields to change)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateTicketDTO.class))
            ))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetTicketDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data (e.g., missing fields, invalid user/dept/status ID)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges (if specific roles were required)", content = @Content)
    })
//    @SecurityRequirement(name = "Bearer Authentication") // it requires authentication.
    @PostMapping
    public ResponseEntity<GetTicketDTO> createTicket(@Valid @RequestBody CreateTicketDTO createDTO) {
        GetTicketDTO savedTicketDTO = ticketService.createTicket(createDTO);
        //return ResponseEntity.ok(savedTicketDTO); // Returns 200 OK
        return ResponseEntity.created(URI.create("/api/tickets/" + savedTicketDTO.getTicketId())).body(savedTicketDTO); // Return 201 Created with Location header
    }

    @Operation(summary = "Update Existing Ticket (Full Replace - Admin Only)", description = "Completely replaces an existing ticket's details using its ID. Requires authentication and potentially ownership/admin role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Complete new details for the ticket", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateTicketDTO.class))
            ))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetTicketDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or ID mismatch", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges (e.g., not owner or admin)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket, User, Department, or Status not found", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication") // Requires JWT
    @PreAuthorize("hasRole('ADMIN')") // Only admins can access
    @PutMapping("/{id}")
    public ResponseEntity<GetTicketDTO> updateTicket(@Parameter(description = "ID of the ticket to update", required = true, example = "1")
                                                     @PathVariable Integer id,
                                                     @Valid @RequestBody UpdateTicketDTO updateDTO) {
        GetTicketDTO updatedTicketDTO = ticketService.updateTicket(id, updateDTO);
        return ResponseEntity.ok(updatedTicketDTO);
    }

    @Operation(summary = "Partially Update Existing Ticket (Admin Only)", description = "Updates specific fields of an existing ticket. Requires authentication.",
            // Describe Request Body within @Operation
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update for the ticket (only include fields to change)", required = true,
                    content = @Content(schema = @Schema(implementation = PatchTicketDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket patched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetTicketDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket, User, Department, or Status not found for referenced IDs", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication") // Requires JWT
    @PreAuthorize("hasRole('ADMIN')") // Only admins can access
    @PatchMapping("/{id}")
    public ResponseEntity<GetTicketDTO> patchTicket(@Parameter(description = "ID of the ticket to patch", required = true, example = "1")
                                                    @PathVariable Integer id,
                                                    @RequestBody PatchTicketDTO patchDTO) {
        GetTicketDTO updatedTicketDTO = ticketService.patchTicket(id, patchDTO);
        return ResponseEntity.ok(updatedTicketDTO);
    }

    @Operation(summary = "Delete Ticket (Admin Only)", description = "Deletes a specific ticket by its ID. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication") // Requires JWT
    @PreAuthorize("hasRole('ADMIN')") // Only admins can access
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@Parameter(description = "ID of the ticket to delete", required = true, example = "1")
                                             @PathVariable Integer id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping
    @Operation(summary = "Get All Tickets (Paginated - Admin Only)", description = "Retrieves a paginated list of all tickets. Requires ADMIN privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of tickets",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication") // Requires JWT because @PreAuthorize requires ADMIN role
    @PreAuthorize("hasRole('ADMIN')") // Only admins can access
    public ResponseEntity<Page<GetTicketDTO>> getAllTickets(@Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
                                                            @Parameter(description = "Number of tickets per page", example = "3") @RequestParam(defaultValue = "3") int size,
                                                            @Parameter(description = "Field to sort by (e.g., ticketId, title, createdAt)", example = "createdAt") @RequestParam(defaultValue = "ticketId") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<GetTicketDTO> ticketPage = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(ticketPage);
    }

    @Operation(summary = "Get Ticket History (v1)", description = "Retrieves ticket details including creation/update times and associated comments ordered by date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket details",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetTicketDTO.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content)
            // @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid/Missing JWT", content = @Content),
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<GetTicketDTO> getTicketHistory(@Parameter(description = "ID of the ticket whose history is to be retrieved", required = true, example = "1")
                                                         @PathVariable Integer id) {
        GetTicketDTO ticketDetails = ticketService.getTicketWithDetailsForHistory(id);
        return ResponseEntity.ok(ticketDetails);
    }
}
