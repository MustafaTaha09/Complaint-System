package com.example.complaintsystem.dto.Comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating or updating a Comment")
public class CommentRequestDTO {

    @Schema(description = "Text content of the comment. Cannot be blank.",
            example = "This issue has been resolved.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment text cannot be blank")
    @Size(max = 2000, message = "Comment text cannot exceed 2000 characters")
    private String text;
}