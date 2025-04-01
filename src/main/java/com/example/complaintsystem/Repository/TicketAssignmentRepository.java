package com.example.complaintsystem.Repository;

import com.example.complaintsystem.Entity.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Integer> {

    // Find all assignments for a specific ticket
    List<TicketAssignment> findByTicketTicketId(Integer ticketId);

    // Find all assignments for a specific user
    List<TicketAssignment> findByUserUserId(Integer userId);

    // Check if a specific user is already assigned to a specific ticket
    boolean existsByTicketTicketIdAndUserUserId(Integer ticketId, Integer userId);

    // Find a specific assignment by ticket and user (useful for targeted deletion)
    Optional<TicketAssignment> findByTicketTicketIdAndUserUserId(Integer ticketId, Integer userId);
}