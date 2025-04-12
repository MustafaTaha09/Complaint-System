package com.example.complaintsystem.service;


import com.example.complaintsystem.dto.Comments.CommentDTO;
import com.example.complaintsystem.dto.Tickets.CreateTicketDTO;
import com.example.complaintsystem.dto.Tickets.PatchTicketDTO;
import com.example.complaintsystem.dto.Tickets.GetTicketDTO;
import com.example.complaintsystem.dto.Tickets.UpdateTicketDTO;
import com.example.complaintsystem.dto.Tickets.V2.CreateTicketDTOV2;
import com.example.complaintsystem.dto.Tickets.V2.GetTicketDTOV2;
import com.example.complaintsystem.entity.*;
import com.example.complaintsystem.exception.BadRequestException;
import com.example.complaintsystem.exception.ResourceNotFoundException;
import com.example.complaintsystem.repository.DepartmentRepository;
import com.example.complaintsystem.repository.TicketRepository;
import com.example.complaintsystem.repository.TicketStatusRepository;
import com.example.complaintsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private static final Logger log = LoggerFactory.getLogger(TicketService.class);


    @Autowired
    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, DepartmentRepository departmentRepository, TicketStatusRepository ticketStatusRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.ticketStatusRepository = ticketStatusRepository;
    }

    public GetTicketDTO getTicketById(Integer id) {
        log.info("Attempting to fetch ticket with ID: {}", id);
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);

        GetTicketDTO getTicketDto = new GetTicketDTO();
        getTicketDto = convertTicketToDTO(ticketOptional.get());

        log.info("Successfully fetched ticket with ID: {}", id);
        return getTicketDto;
    }

    public GetTicketDTO createTicket(CreateTicketDTO createDTO) {
        log.info("Attempting to create new ticket with title: '{}' for user ID: {}", createDTO.getTitle(), createDTO.getUserId());

        Ticket ticket = new Ticket();
        ticket.setTitle(createDTO.getTitle());
        ticket.setDescription(createDTO.getDescription());

        log.debug("Fetching user with ID: {}", createDTO.getUserId());
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> {
                    log.error("User not found for ticket creation with ID: {}", createDTO.getUserId());
                    return new ResourceNotFoundException("User not found with id: " + createDTO.getUserId());
                });
        log.debug("Fetching department with ID: {}", createDTO.getDepartmentId());
        Department department = departmentRepository.findById(createDTO.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("Department not found for ticket creation with ID: {}", createDTO.getDepartmentId());
                    return new ResourceNotFoundException("Department not found");
                });

        log.debug("Fetching status with ID: {}", createDTO.getStatusId());
        TicketStatus status = ticketStatusRepository.findById(createDTO.getStatusId())
                .orElseThrow(() -> {
                    log.error("Ticket status not found for ticket creation with ID: {}", createDTO.getStatusId());
                    return new ResourceNotFoundException("Status not found");
                });


        ticket.setUser(user);
        ticket.setDepartment(department);
        ticket.setTicketStatus(status);

        // Save the new ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Successfully created ticket with ID: {}", savedTicket.getTicketId());

        // Convert the saved entity to a DTO for the response
        return convertTicketToDTO(savedTicket);
    }

    //Convert only Status to its DTO
//    private TicketStatusDTO convertStatusToDTO(TicketStatus ticketStatus) { //separate method for ticket status
//        if (ticketStatus == null) {
//            return null;
//        }
//        TicketStatusDTO dto = new TicketStatusDTO();
//        dto.setStatusId(ticketStatus.getStatusId());
//        dto.setStatusName(ticketStatus.getStatusName());
//        return dto;
//    }



    @Transactional
    public GetTicketDTO updateTicket(Integer id, UpdateTicketDTO updateDTO) {
        log.info("Attempting to update ticket with ID: {}", id);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket not found for update with ID: '{}'", id);
                    return new ResourceNotFoundException("Ticket not found with id: " + id);
                });

        // Validate that the ID in the path matches the ID in the DTO (for PUT)
        log.debug("Validating ticket ID match for update. Path ID: {}, Body ID: {}", id, updateDTO.getTicketId());
        if (!id.equals(updateDTO.getTicketId())) {
            throw new BadRequestException("Ticket ID in path does not match ID in body");
        }

        // Update ALL fields from the DTO
        ticket.setTitle(updateDTO.getTitle());
        ticket.setDescription(updateDTO.getDescription());
        log.debug("Basic fields updated for ticket ID: {}", id);

        // Fetching related entities
        if (updateDTO.getUserId() != null) {
            log.debug("Attempting to update user for ticket ID: {} to User ID: {}", id, updateDTO.getUserId());
            User user = userRepository.findById(updateDTO.getUserId())
                    .orElseThrow(() -> {
                        log.error("User not found during ticket update with ID: {}", updateDTO.getUserId());
                        return new ResourceNotFoundException("User not found with id: " + updateDTO.getUserId());
                    });
            ticket.setUser(user);
        }
        if (updateDTO.getDepartmentId() != null) {
            log.debug("Attempting to update department for ticket ID: {} to Department ID: {}", id, updateDTO.getDepartmentId());
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department not found during ticket update with ID: {}", updateDTO.getDepartmentId());
                        return new ResourceNotFoundException("Department not found");
                    });
            ticket.setDepartment(department);
        }
        if (updateDTO.getStatusId() != null) {
            log.debug("Attempting to update status for ticket ID: {} to Status ID: {}", id, updateDTO.getStatusId());
            TicketStatus status = ticketStatusRepository.findById(updateDTO.getStatusId())
                    .orElseThrow(() -> {
                        log.error("Ticket status not found during ticket update with ID: {}", updateDTO.getStatusId());
                        return new ResourceNotFoundException("Status not found");
                    });
            ticket.setTicketStatus(status);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("Successfully updated ticket with ID: {}", updatedTicket.getTicketId());
        return convertTicketToDTO(updatedTicket);
    }


    public GetTicketDTO patchTicket(Integer id, PatchTicketDTO patchDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));


        patchDTO.getTitle().ifPresent(ticket::setTitle);
        patchDTO.getDescription().ifPresent(ticket::setDescription);

        patchDTO.getUserId().ifPresent(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            ticket.setUser(user);
        });

        patchDTO.getDepartmentId().ifPresent(departmentId -> {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            ticket.setDepartment(department);
        });

        patchDTO.getStatusId().ifPresent(statusId -> {
            TicketStatus status = ticketStatusRepository.findById(statusId)
                    .orElseThrow(() -> new ResourceNotFoundException("Status Not Found"));
            ticket.setTicketStatus(status);
        });

        Ticket updatedTicket = ticketRepository.save(ticket);
        return convertTicketToDTO(updatedTicket);
    }

    @Transactional
    public void deleteTicket(Integer id) {
        log.info("Attempting to delete ticket with ID: {}", id);
        if (!ticketRepository.existsById(id)) {
            log.error("Ticket not found for deletion with ID: '{}'", id);
            throw new ResourceNotFoundException("Ticket not found with id: " + id);
        }
        ticketRepository.deleteById(id);
        log.info("Successfully deleted ticket with ID: {}", id);

    }

    public GetTicketDTO getTicketWithDetailsForHistory(Integer id) {
        log.info("Attempting to fetch ticket with full details for history, ID: {}", id);
        Ticket ticket = ticketRepository.findTicketWithCommentsById(id)
                .orElseThrow(() -> {
                    log.error("Ticket not found for history view with ID: '{}'", id);
                    return new ResourceNotFoundException("Ticket not found with id: " + id);
                });

        log.info("Successfully fetched ticket with details for history, ID: {}", id);
        return convertTicketToDTO(ticket); // Convert the fully loaded ticket
    }

    // Paginated retrieval
    public Page<GetTicketDTO> getAllTickets(Pageable pageable) {
        log.info("Fetching all tickets with pagination: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Ticket> ticketPage = ticketRepository.findAll(pageable); // Use the repository's findAll method
        return ticketPage.map(this::convertTicketToDTO); // Convert Page<Ticket> to Page<TicketDTO>
    }

    @Transactional // Add transactional if not already covered
    public GetTicketDTOV2 createTicketV2(CreateTicketDTOV2 createDTOV2) {
        // Note: We receive priority in the DTO, but DON'T save it to the Ticket entity
        log.info("Attempting to create V2 ticket (priority='{}' ignored in persistence) with title: '{}' for user ID: {}",
                createDTOV2.getPriority(), createDTOV2.getTitle(), createDTOV2.getUserId());

        Ticket ticket = new Ticket(); // Ticket entity still doesn't have priority field
        ticket.setTitle(createDTOV2.getTitle());
        ticket.setDescription(createDTOV2.getDescription());
        // DO NOT set ticket.setPriority(...) as the entity field doesn't exist

        // Fetch related entities (same as V1 create)
        User user = userRepository.findById(createDTOV2.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createDTOV2.getUserId()));
        Department department = departmentRepository.findById(createDTOV2.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        TicketStatus status = ticketStatusRepository.findById(createDTOV2.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));

        ticket.setUser(user);
        ticket.setDepartment(department);
        ticket.setTicketStatus(status);

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Successfully created ticket ID: {} (V2 DTO requested, priority not saved)", savedTicket.getTicketId());
        // Convert using the V2 converter, which will add the default priority
        return convertToTicketDTOV2(savedTicket);
    }

    @Transactional(readOnly = true) // Good for read operations
    public GetTicketDTOV2 getTicketByIdV2(Integer id) {
        log.info("Attempting to fetch ticket ID: {} for V2 response", id);
        Ticket ticket = getTicketEntityById(id); // Use existing helper to get entity
        log.info("Successfully fetched ticket ID: {}, converting to V2 DTO", id);
        return convertToTicketDTOV2(ticket); // Use V2 conversion
    }
    // --- Helper Methods (Ensure these exist and are accessible) ---
    @Transactional(readOnly = true) // Make sure helper is transactional if it triggers lazy loading
    public Ticket getTicketEntityById(Integer id) {
        log.debug("Fetching raw ticket entity with ID: {}", id);
        return ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket entity not found with ID: '{}'", id);
                    return new ResourceNotFoundException("Ticket not found with id: " + id);
                });
    }

    // NEW V2 Conversion Method
    public GetTicketDTOV2 convertToTicketDTOV2(Ticket ticket) {
        log.debug("Converting Ticket entity ID: {} to V2 DTO", ticket != null ? ticket.getTicketId() : "null");
        if (ticket == null) {
            return null;
        }
        GetTicketDTOV2 dto = new GetTicketDTOV2();

        dto.setTicketId(ticket.getTicketId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        if (ticket.getTicketStatus() != null) {
            dto.setStatusId(ticket.getTicketStatus().getStatusId());
            dto.setStatusName(ticket.getTicketStatus().getStatusName());
        }
        if (ticket.getUser() != null) {
            dto.setUserId(ticket.getUser().getUserId());
        }
        if (ticket.getDepartment() != null) {
            dto.setDepartmentId(ticket.getDepartment().getDepartmentId());
            dto.setDepartmentName(ticket.getDepartment().getDepartmentName());

        }
        if (ticket.getComments() != null) {
            dto.setComments(ticket.getComments().stream()
                    .map(this::convertToCommentDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setComments(Collections.emptyList());
        }

        // Set V2 specific field (priority) since I haven't placed it in the Entity
        dto.setPriority("Medium"); // Set a default value for V2

        return dto;
    }


    //Converting a ticket to its DTO.
    public GetTicketDTO convertTicketToDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        GetTicketDTO dto = new GetTicketDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        if (ticket.getTicketStatus() != null) {
            dto.setStatusId(ticket.getTicketStatus().getStatusId());
            dto.setStatusName(ticket.getTicketStatus().getStatusName());
        }
        if (ticket.getUser() != null) {
            dto.setUserId(ticket.getUser().getUserId());
        }
        if (ticket.getDepartment() != null) {
            dto.setDepartmentId(ticket.getDepartment().getDepartmentId());
            dto.setDepartmentName(ticket.getDepartment().getDepartmentName());

        }
        if (ticket.getComments() != null) {
            List<CommentDTO> commentDTOs = new ArrayList<>();
            for (Comment comment : ticket.getComments()) {
                commentDTOs.add(convertToCommentDTO(comment));  // Use the helper method
            }
            dto.setComments(commentDTOs);
        }

        return dto;
    }
    // Converting a comment to its DTO
    public CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getCommentId());
        commentDTO.setText(comment.getComment());
        commentDTO.setTicketId(comment.getTicket().getTicketId());
        commentDTO.setUserId(comment.getUser().getUserId());
        commentDTO.setCreatedAt(comment.getCreatedAt());

        return commentDTO;
    }

}
