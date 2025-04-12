package com.example.complaintsystem.presentation;


import com.example.complaintsystem.entity.TicketStatus;
import com.example.complaintsystem.service.TicketStatusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ticket-statuses")
@Tag(name = "Ticket Status", description = "Managing Ticket Statuses (Open, Closed, etc..)")

public class TicketStatusController {

    private final TicketStatusService ticketStatusService;

    @Autowired
    public TicketStatusController(TicketStatusService ticketStatusService) {
        this.ticketStatusService = ticketStatusService;
    }

    // Get all ticket statuses
    @GetMapping
    public List<TicketStatus> getAllTicketStatuses() {
        return ticketStatusService.getAllTicketStatuses();
    }

    // Get ticket status by ID
    @GetMapping("/{statusId}")
    public Optional<TicketStatus> getTicketStatusById(@PathVariable Integer statusId) {
        return ticketStatusService.getTicketStatusById(statusId);
    }

    // Create or update ticket status
    @PostMapping
    public TicketStatus createOrUpdateTicketStatus(@RequestBody TicketStatus ticketStatus) {
        return ticketStatusService.saveTicketStatus(ticketStatus);
    }

    // Delete ticket status by ID
    @DeleteMapping("/{statusId}")
    public void deleteTicketStatus(@PathVariable Integer statusId) {
        ticketStatusService.deleteTicketStatus(statusId);
    }
}

