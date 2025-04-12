package com.example.complaintsystem.dto.TicketAssignments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating a new Ticket Assignment (assigning a user to a ticket)")
public class CreateTicketAssignmentDTO {

    @Schema(description = "ID of the Ticket to assign", requiredMode = Schema.RequiredMode.REQUIRED, example = "55")
    @NotNull(message = "Ticket ID cannot be null")
    private Integer ticketId;

    @Schema(description = "ID of the User to assign", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "User ID cannot be null")
    private Integer userId;
}