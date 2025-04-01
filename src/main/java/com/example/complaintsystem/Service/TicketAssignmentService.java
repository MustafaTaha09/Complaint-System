package com.example.complaintsystem.Service;

import com.example.complaintsystem.DTO.TicketAssignments.CreateTicketAssignmentDTO;
import com.example.complaintsystem.DTO.TicketAssignments.TicketAssignmentDTO;
import com.example.complaintsystem.Entity.Ticket;
import com.example.complaintsystem.Entity.TicketAssignment;
import com.example.complaintsystem.Entity.User;
import com.example.complaintsystem.Exceptions.BadRequestException;
import com.example.complaintsystem.Exceptions.ResourceNotFoundException;
import com.example.complaintsystem.Repository.TicketAssignmentRepository;
import com.example.complaintsystem.Repository.TicketRepository;
import com.example.complaintsystem.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(TicketAssignmentService.class);

    private final TicketAssignmentRepository assignmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Autowired
    public TicketAssignmentService(TicketAssignmentRepository assignmentRepository,
                                   TicketRepository ticketRepository,
                                   UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // Get Assignment By ID
    @Transactional(readOnly = true)
    public TicketAssignmentDTO getAssignmentById(Integer id) {
        log.info("Fetching ticket assignment with ID: {}", id);
        TicketAssignment assignment = findAssignmentByIdOrThrow(id);
        log.info("Found assignment for ticket ID: {} and user ID: {}",
                assignment.getTicket().getTicketId(), assignment.getUser().getUserId());
        return convertToDTO(assignment);
    }

    // Get Assignments by Ticket ID
    @Transactional(readOnly = true)
    public List<TicketAssignmentDTO> getAssignmentsByTicketId(Integer ticketId) {
        log.info("Fetching assignments for ticket ID: {}", ticketId);
        // Check if ticket exists
        if (!ticketRepository.existsById(ticketId)) {
            log.warn("Attempted to fetch assignments for non-existent ticket ID: {}", ticketId);
            throw new ResourceNotFoundException("Ticket not found with id: " + ticketId);
        }
        List<TicketAssignment> assignments = assignmentRepository.findByTicketTicketId(ticketId);
        List<TicketAssignmentDTO> dtos = assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
        log.info("Found {} assignments for ticket ID: {}", dtos.size(), ticketId);
        return dtos;
    }

    // Get Assignments by User ID
    @Transactional(readOnly = true)
    public List<TicketAssignmentDTO> getAssignmentsByUserId(Integer userId) {
        log.info("Fetching assignments for user ID: {}", userId);

        // Check if user exists
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to fetch assignments for non-existent user ID: {}", userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        List<TicketAssignment> assignments = assignmentRepository.findByUserUserId(userId);
        List<TicketAssignmentDTO> dtos = assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
        log.info("Found {} assignments for user ID: {}", dtos.size(), userId);
        return dtos;
    }

    // Create Assignment
    @Transactional
    public TicketAssignmentDTO createAssignment(CreateTicketAssignmentDTO createDTO) {
        Integer ticketId = createDTO.getTicketId();
        Integer userId = createDTO.getUserId();
        log.info("Attempting to create assignment for ticket ID: {} to user ID: {}", ticketId, userId);

        // Validate Ticket existence
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Assignment creation failed: Ticket not found with ID: {}", ticketId);
                    return new ResourceNotFoundException("Ticket not found with id: " + ticketId);
                });

        // Validate User existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Assignment creation failed: User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        // Check if assignment already exists
        if (assignmentRepository.existsByTicketTicketIdAndUserUserId(ticketId, userId)) {
            log.warn("Assignment creation failed: User ID {} is already assigned to ticket ID {}", userId, ticketId);
            throw new BadRequestException("User with ID " + userId + " is already assigned to ticket with ID " + ticketId);
        }

        // Create and save
        TicketAssignment newAssignment = new TicketAssignment();
        newAssignment.setTicket(ticket);
        newAssignment.setUser(user);

        TicketAssignment savedAssignment = assignmentRepository.save(newAssignment);
        log.info("Successfully created assignment with ID: {} for ticket ID: {} and user ID: {}",
                savedAssignment.getId(), ticketId, userId);
        return convertToDTO(savedAssignment);
    }

    // Delete Assignment by its ID
    @Transactional
    public void deleteAssignment(Integer assignmentId) {
        log.info("Attempting to delete assignment with ID: {}", assignmentId);

        // Check if assignment exists before deleting
        if (!assignmentRepository.existsById(assignmentId)) {
            log.error("Assignment not found for deletion with ID: {}", assignmentId);
            throw new ResourceNotFoundException("Ticket Assignment not found with id: " + assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
        log.info("Successfully deleted assignment with ID: {}", assignmentId);
    }

    // (Optional) Delete Assignment by Ticket and User ID
    @Transactional
    public void deleteAssignmentByTicketAndUser(Integer ticketId, Integer userId) {
        log.info("Attempting to delete assignment for ticket ID: {} and user ID: {}", ticketId, userId);
        TicketAssignment assignment = assignmentRepository.findByTicketTicketIdAndUserUserId(ticketId, userId)
                .orElseThrow(() -> {
                    log.error("Assignment not found for deletion for ticket {} and user {}", ticketId, userId);
                    return new ResourceNotFoundException("Assignment not found for ticket " + ticketId + " and user " + userId);
                });
        assignmentRepository.delete(assignment);
        log.info("Successfully deleted assignment with ID: {} (Ticket: {}, User: {})", assignment.getId(), ticketId, userId);
    }


    // Helper: Find Assignment or Throw
    private TicketAssignment findAssignmentByIdOrThrow(Integer assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    log.error("Ticket Assignment not found with ID: {}", assignmentId);
                    return new ResourceNotFoundException("Ticket Assignment not found with id: " + assignmentId);
                });
    }

    // Helper: Convert Entity to DTO
    public TicketAssignmentDTO convertToDTO(TicketAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        TicketAssignmentDTO dto = new TicketAssignmentDTO();
        dto.setId(assignment.getId());

        // Avoid NullPointerExceptions if relations are somehow null
        dto.setTicketId(assignment.getTicket() != null ? assignment.getTicket().getTicketId() : null);
        dto.setUserId(assignment.getUser() != null ? assignment.getUser().getUserId() : null);
        return dto;
    }
}