package com.example.complaintsystem.Presentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.complaintsystem.DTO.Tickets.V2.CreateTicketDTOV2;
import com.example.complaintsystem.DTO.Tickets.V2.GetTicketDTOV2;
import com.example.complaintsystem.Service.TicketService;
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

@RestController
@RequestMapping("/api/v2/tickets")
@Tag(name = "Tickets (v2)", description = "Version 2 APIs for specific ticket operations")
@SecurityRequirement(name = "Bearer Authentication") // Apply JWT requirement
public class TicketControllerV2 {

    private static final Logger log = LoggerFactory.getLogger(TicketControllerV2.class);
    private final TicketService ticketService;

    @Autowired
    public TicketControllerV2(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // V2: GET Ticket By ID
    @Operation(summary = "Get Ticket by ID (v2)", description = "Retrieves V2 details (including default priority) for a specific ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved V2 ticket",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetTicketDTOV2.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content)
    })

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<GetTicketDTOV2> getTicketV2(
            @Parameter(description = "Unique ID of the ticket to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("V2 request received to get ticket ID: {}", id);
        GetTicketDTOV2 getTicketDTO = ticketService.getTicketByIdV2(id);
        return ResponseEntity.ok(getTicketDTO);
    }

    // V2: Create Ticket
    @Operation(summary = "Create New Ticket (v2)", description = "Creates a new ticket using the V2 format (requires priority). Priority is not saved to the database.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "V2 Details of the ticket (includes priority)", required = true,
                    content = @Content(schema = @Schema(implementation = CreateTicketDTOV2.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket created successfully (V2 response)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetTicketDTOV2.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "User, Department, or Status not found", content = @Content)
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GetTicketDTOV2> createTicketV2(
            @Valid @RequestBody CreateTicketDTOV2 createDTOV2) {
        log.info("V2 request received to create ticket with title: '{}'", createDTOV2.getTitle());
        GetTicketDTOV2 savedTicketDTO = ticketService.createTicketV2(createDTOV2);
        return ResponseEntity.created(URI.create("/api/v2/tickets/" + savedTicketDTO.getTicketId())).body(savedTicketDTO);
    }

}