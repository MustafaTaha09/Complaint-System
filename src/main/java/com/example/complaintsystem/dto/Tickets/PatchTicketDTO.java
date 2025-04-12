package com.example.complaintsystem.dto.Tickets;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class PatchTicketDTO {
    //optional because it might or might not exist so that we can use ifPresent() in the Service Layer
    private Optional<String> title;
    private Optional<String> description;
    private Optional<Integer> userId;
    private Optional<Integer> departmentId;
    private Optional<Integer> statusId;
}
