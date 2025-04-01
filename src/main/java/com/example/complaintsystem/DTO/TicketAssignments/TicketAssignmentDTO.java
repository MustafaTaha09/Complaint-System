package com.example.complaintsystem.DTO.TicketAssignments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object representing a User assigned to a Ticket")
public class TicketAssignmentDTO {

    @Schema(description = "Unique identifier for the assignment record itself", example = "101")
    private Integer id;

    @Schema(description = "ID of the Ticket being assigned", example = "55")
    private Integer ticketId;

    @Schema(description = "ID of the User assigned to the ticket", example = "12")
    private Integer userId;

}