package com.example.complaintsystem.DTO.Tickets;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
public class CreateTicketDTO {

    @NotBlank(message = "Title cannot be blank") // Validation
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer departmentId;

    @NotNull
    private Integer statusId=1; //as it is the default for any new ticket (OPEN)

}
