package com.example.complaintsystem.DTO.Tickets.V2;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "V2 Data Transfer Object for creating a new Ticket (includes non-persistent priority)")
public class CreateTicketDTOV2 {

    @Schema(description = "ID of the user creating the ticket", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @Schema(description = "ID of the department", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "Department ID cannot be null")
    private Integer departmentId;

    @Schema(description = "Initial status ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Status ID cannot be null")
    private Integer statusId;

    @Schema(description = "Title for the ticket", requiredMode = Schema.RequiredMode.REQUIRED, example = "Cannot print documents")
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255)
    private String title;

    @Schema(description = "Detailed description", requiredMode = Schema.RequiredMode.REQUIRED, example = "Printer offline.")
    @NotBlank(message = "Description cannot be blank")
    private String description;

    // This field is for v2 API contract
    @Schema(description = "Priority level for the new ticket (Required for V2 API)", requiredMode = Schema.RequiredMode.REQUIRED, example = "Medium")
    @NotBlank(message = "Priority cannot be blank for V2")
    @Size(max = 50)
    private String priority; // Added field for V2. This will be ignored by service during saving
}