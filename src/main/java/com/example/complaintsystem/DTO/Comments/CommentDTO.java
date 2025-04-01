package com.example.complaintsystem.DTO.Comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Data Transfer Object for Comment details")
public class CommentDTO {

    @Schema(description = "Unique identifier of the comment", example = "10")
    private Integer id;

    @Schema(description = "The actual text content of the comment", example = "Checked the logs, issue seems intermittent.")
    private String text;

    @Schema(description = "ID of the ticket this comment belongs to", example = "1")
    private Integer ticketId;

    @Schema(description = "ID of the user who posted the comment", example = "5")
    private Integer userId;

    @Schema(description = "Timestamp when the comment was created")
    private LocalDateTime createdAt;
}