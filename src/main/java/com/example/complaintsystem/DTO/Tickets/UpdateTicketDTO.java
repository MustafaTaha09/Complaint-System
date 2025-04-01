package com.example.complaintsystem.DTO.Tickets;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketDTO {
    @NotNull
    private Integer ticketId; //MUST have the ID for PUT
    private String title;
    private String description;
    private Integer userId;
    private Integer departmentId;
    private Integer statusId;

}