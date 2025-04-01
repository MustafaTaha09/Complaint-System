package com.example.complaintsystem.Service;

import com.example.complaintsystem.DTO.Comments.CommentDTO;
import com.example.complaintsystem.DTO.Comments.CommentRequestDTO;
import com.example.complaintsystem.Entity.Comment;
import com.example.complaintsystem.Entity.Ticket;
import com.example.complaintsystem.Entity.User;
import com.example.complaintsystem.Exceptions.ResourceNotFoundException;
import com.example.complaintsystem.Repository.CommentRepository;
import com.example.complaintsystem.Repository.TicketRepository;
import com.example.complaintsystem.Repository.UserRepository; // Not strictly needed if using Principal User obj
import com.example.complaintsystem.Security.CustomUserDetails; // Assuming this is your UserDetails impl
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }

    // Get Comments for a specific Ticket
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByTicketId(Integer ticketId) {
        log.info("Fetching comments for ticket ID: {}", ticketId);
        // Check if ticket exists first
        if (!ticketRepository.existsById(ticketId)) {
            log.warn("Attempted to fetch comments for non-existent ticket ID: {}", ticketId);
            throw new ResourceNotFoundException("Ticket not found with id: " + ticketId);
        }
        List<Comment> comments = commentRepository.findByTicketTicketIdOrderByCreatedAtAsc(ticketId);
        List<CommentDTO> dtos = comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Found {} comments for ticket ID: {}", dtos.size(), ticketId);
        return dtos;
    }

    // Get Single Comment By ID
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Integer commentId) {
        log.info("Fetching comment with ID: {}", commentId);
        Comment comment = findCommentByIdOrThrow(commentId);
        log.info("Found comment ID: {}", commentId);
        return convertToDTO(comment);
    }

    // Create Comment
    @Transactional
    public CommentDTO createComment(Integer ticketId, CommentRequestDTO commentRequestDTO, CustomUserDetails currentUser) {
        log.info("Attempting to create comment for ticket ID: {} by user ID: {}", ticketId, currentUser.getUserId());

        // Find the ticket the comment belongs to
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found for comment creation with ID: {}", ticketId);
                    return new ResourceNotFoundException("Cannot add comment: Ticket not found with id: " + ticketId);
                });

        // 2. Get the User entity from the principal
        User user = currentUser.getUser(); // Assumes getUser() returns the managed User entity

        // 3. Create and save the comment
        Comment newComment = new Comment();
        newComment.setComment(commentRequestDTO.getText());
        newComment.setTicket(ticket);
        newComment.setUser(user);

        Comment savedComment = commentRepository.save(newComment);
        log.info("Successfully created comment ID: {} for ticket ID: {} by user ID: {}",
                savedComment.getCommentId(), ticketId, user.getUserId());
        return convertToDTO(savedComment);
    }

    // Update Comment
    @Transactional
    public CommentDTO updateComment(Integer commentId, CommentRequestDTO commentRequestDTO, CustomUserDetails currentUser) {
        log.info("Attempting to update comment ID: {} by user ID: {}", commentId, currentUser.getUserId());

        Comment existingComment = findCommentByIdOrThrow(commentId);

        // Authorization Check
        checkCommentOwnershipOrAdmin(existingComment, currentUser, "update");

        // Update the text
        existingComment.setComment(commentRequestDTO.getText());

        Comment updatedComment = commentRepository.save(existingComment);
        log.info("Successfully updated comment ID: {}", updatedComment.getCommentId());
        return convertToDTO(updatedComment);
    }

    // Delete Comment
    @Transactional
    public void deleteComment(Integer commentId, CustomUserDetails currentUser) {
        log.info("Attempting to delete comment ID: {} by user ID: {}", commentId, currentUser.getUserId());

        Comment commentToDelete = findCommentByIdOrThrow(commentId);

        // Authorization Check
        checkCommentOwnershipOrAdmin(commentToDelete, currentUser, "delete");

        commentRepository.delete(commentToDelete);
        log.info("Successfully deleted comment ID: {}", commentId);
    }

    // Helper: Find Comment or Throw
    private Comment findCommentByIdOrThrow(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Comment not found with ID: {}", commentId);
                    return new ResourceNotFoundException("Comment not found with id: " + commentId);
                });
    }

    // Helper Method: Authorization Check
    private void checkCommentOwnershipOrAdmin(Comment comment, CustomUserDetails currentUser, String action) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = comment.getUser().getUserId().equals(currentUser.getUserId());

        if (!isAdmin && !isOwner) {
            log.warn("Access Denied: User {} attempted to {} comment ID {} owned by user {}",
                    currentUser.getUserId(), action, comment.getCommentId(), comment.getUser().getUserId());
            throw new AccessDeniedException("User does not have permission to " + action + " this comment");
        }
        log.debug("Authorization check passed for user {} to {} comment ID {}", currentUser.getUserId(), action, comment.getCommentId());
    }

    // Helper: Convert Entity to DTO
    public CommentDTO convertToDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getCommentId());
        dto.setText(comment.getComment());
        dto.setCreatedAt(comment.getCreatedAt());

        // Avoid NullPointerExceptions if relations are somehow null (shouldn't happen with non-null constraints)
        dto.setTicketId(comment.getTicket() != null ? comment.getTicket().getTicketId() : null);
        dto.setUserId(comment.getUser() != null ? comment.getUser().getUserId() : null);
        return dto;
    }
}