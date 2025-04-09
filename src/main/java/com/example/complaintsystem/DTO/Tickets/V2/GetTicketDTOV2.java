package com.example.complaintsystem.DTO.Tickets.V2;

import com.example.complaintsystem.DTO.Comments.CommentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "V2 Data Transfer Object for detailed Ticket information (includes non-persistent priority)")
public class GetTicketDTOV2 {

    @Schema(description = "Unique identifier of the ticket", example = "101")
    private Integer ticketId;

    @Schema(description = "ID of the user who created/owns the ticket", example = "12")
    private Integer userId;

    @Schema(description = "ID of the assigned department", example = "3")
    private Integer departmentId;
    @Schema(description = "Name of the assigned department", example = "IT Support")
    private String departmentName;

    @Schema(description = "ID of the ticket's current status", example = "2")
    private Integer statusId;
    @Schema(description = "Name of the ticket's current status", example = "In Progress")
    private String statusName;

    @Schema(description = "Title of the ticket", example = "Email server down")
    private String title;

    @Schema(description = "Detailed description of the issue", example = "Users cannot send or receive emails.")
    private String description;

    @Schema(description = "Priority level (API v2 only, not persisted)", example = "Medium")
    private String priority; // Added field for V2

    @Schema(description = "Timestamp when the ticket was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the ticket was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "List of comments associated with the ticket")
    private List<CommentDTO> comments;
}