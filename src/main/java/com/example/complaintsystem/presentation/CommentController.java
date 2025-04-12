package com.example.complaintsystem.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.complaintsystem.dto.Comments.CommentDTO;
import com.example.complaintsystem.dto.Comments.CommentRequestDTO;
import com.example.complaintsystem.service.CommentService;
import com.example.complaintsystem.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Comments", description = "APIs for managing comments on tickets (User must be Logged in Only)")
@SecurityRequirement(name = "Bearer Authentication") // Require JWT for all comment actions
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // GET Comments for a specific Ticket
    @Operation(summary = "Get Comments by Ticket ID", description = "Retrieves all comments associated with a specific ticket, ordered by creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CommentDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    @GetMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("isAuthenticated()") // Must be logged in
    public ResponseEntity<List<CommentDTO>> getCommentsForTicket(
            @Parameter(description = "ID of the ticket to retrieve comments for", required = true, example = "1")
            @PathVariable Integer ticketId) {
        log.info("Request received to get comments for ticket ID: {}", ticketId);
        List<CommentDTO> comments = commentService.getCommentsByTicketId(ticketId);
        return ResponseEntity.ok(comments);
    }

    // GET Single Comment By ID
    @Operation(summary = "Get Comment by ID", description = "Retrieves details of a specific comment by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comment",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @GetMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") // Must be logged in
    public ResponseEntity<CommentDTO> getCommentById(
            @Parameter(description = "Unique ID of the comment to retrieve", required = true, example = "15")
            @PathVariable Integer commentId) {
        log.info("Request received to get comment by ID: {}", commentId);
        CommentDTO comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    // Create Comment
    @Operation(summary = "Create New Comment", description = "Adds a new comment to a specific ticket. The comment author is the authenticated user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The text content of the comment", required = true,
                    content = @Content(schema = @Schema(implementation = CommentRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data (e.g., blank text)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket not found with the specified ID", content = @Content)
    })
    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("isAuthenticated()") // Must be logged in
    public ResponseEntity<CommentDTO> createComment(
            @Parameter(description = "ID of the ticket to add the comment to", required = true, example = "1")
            @PathVariable Integer ticketId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) { // Inject authenticated user details
        log.info("Request received to create comment for ticket ID: {} by user ID: {}", ticketId, currentUser.getUserId());
        CommentDTO savedComment = commentService.createComment(ticketId, commentRequestDTO, currentUser);

        URI location = URI.create("/api/comments/" + savedComment.getId());
        return ResponseEntity.created(location).body(savedComment);
    }

    // Update Comment (PUT)
    @Operation(summary = "Update Existing Comment", description = "Updates the text content of an existing comment. Requires user to be the owner or an ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The updated text content for the comment", required = true,
                    content = @Content(schema = @Schema(implementation = CommentRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data (e.g., blank text)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner or an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") // Let the service handle ownership/admin check
    public ResponseEntity<CommentDTO> updateComment(
            @Parameter(description = "ID of the comment to update", required = true, example = "15")
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Request received to update comment ID: {} by user ID: {}", commentId, currentUser.getUserId());
        CommentDTO updatedComment = commentService.updateComment(commentId, commentRequestDTO, currentUser);
        return ResponseEntity.ok(updatedComment);
    }

    // --- Delete Comment ---
    @Operation(summary = "Delete Comment", description = "Deletes a specific comment by its ID. Requires user to be the owner or an ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not the owner or an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID of the comment to delete", required = true, example = "15")
            @PathVariable Integer commentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) { // Inject authenticated user
        log.info("Request received to delete comment ID: {} by user ID: {}", commentId, currentUser.getUserId());
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }
}