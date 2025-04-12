package com.example.complaintsystem.service;

import com.example.complaintsystem.entity.TicketStatus;
import com.example.complaintsystem.repository.TicketStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketStatusService {

    private final TicketStatusRepository ticketStatusRepository;

    @Autowired
    public TicketStatusService(TicketStatusRepository ticketStatusRepository) {
        this.ticketStatusRepository = ticketStatusRepository;
    }

    // Get all ticket statuses
    public List<TicketStatus> getAllTicketStatuses() {
        return ticketStatusRepository.findAll();
    }

    // Get ticket status by ID
    public Optional<TicketStatus> getTicketStatusById(Integer statusId) {
        return ticketStatusRepository.findById(statusId);
    }

    // Create or update ticket status
    public TicketStatus saveTicketStatus(TicketStatus ticketStatus) {
        return ticketStatusRepository.save(ticketStatus);
    }

    // Delete ticket status by ID
    public void deleteTicketStatus(Integer statusId) {
        ticketStatusRepository.deleteById(statusId);
    }
}
